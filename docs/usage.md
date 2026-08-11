# VDA 5050 核心库使用说明

本文说明当前 `0.1.0-SNAPSHOT` 的公共入口和集成边界。该制品尚未正式发布；示例应在本地构建后使用，并以当前源码和 Javadoc 为准。

## 本地构建与依赖

```powershell
.\mvnw.cmd verify
.\mvnw.cmd install
```

本地安装后，应用可以声明 VDA 5050 独立制品：

```xml
<dependency>
  <groupId>io.github.cooltheworld</groupId>
  <artifactId>rcs-protocol-vda5050</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

核心库不建立 MQTT、Spring、Redis、数据库或设备连接。Adapter 负责接收字节与 Topic 路径，核心负责有界解码、校验和纯状态转换，调用方再持久化 State 与 Effect 并执行外部 I/O。

## 入站 Connection 校验

`ConnectionValidator` 是 `connection` 入站消息获得成功凭证的公共入口。普通非法输入返回 `RejectedInboundMessage`，不通过异常表达协议错误。

```java
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.connection.Connection;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.DefaultTopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ConnectionValidator;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.RejectedInboundMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidatedMessage;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.validation.ValidationResult;

ConnectionValidator validator = ConnectionValidator.createDefault();
ValidationResult<Connection> result = validator.validate(
    DefaultTopicLayout.standard(),
    mqttTopicPath,
    payloadBytes
);

if (result instanceof ValidatedMessage<Connection> accepted) {
    Connection connection = accepted.message();
    // 交给对应 Robot Identity 和版本的角色状态机。
} else {
    RejectedInboundMessage<Connection> rejected =
        (RejectedInboundMessage<Connection>) result;
    // 处理 rejected.issues()；不要记录原始 payload。
}
```

校验顺序包括 JSON 资源限制、Draft 2020-12 Schema、强类型绑定、`uint32`、版本配置、Topic 类型和 Topic/Header 身份一致性。`Vda5050JsonCodec.decode(...)` 单独使用时只完成语法与基础类型解码，不能代替 `ValidatedMessage<T>`。

## Fleet Control 状态转换

以下代码接续上节 `accepted` 成功分支。缺失历史快照时必须从 `recovering` 状态开始；事件时间由外部显式传入，状态机不读取系统时钟。

```java
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.event.FleetControlEvent;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlState;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlStateMachine;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.fleetcontrol.FleetControlTransition;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.ProtocolVersionProfile;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import java.time.Instant;

RobotIdentity robot = new RobotIdentity("acme", "robot-001");
FleetControlState state = FleetControlState.recovering(
    robot,
    ProtocolVersionProfile.V3_0_0
);

FleetControlTransition transition = FleetControlStateMachine.createDefault()
    .transition(
        state,
        new FleetControlEvent.ConnectionReceived(accepted, Instant.now())
    );

FleetControlState nextState = transition.state();
// 以一个原子提交保存 nextState 与 transition.effects()。
```

示例中的 `Instant.now()` 位于应用 Adapter，不在状态机内部。生产集成应使用事件实际采集时间，并按相同角色、Robot Identity 和协议版本串行处理。

## Topic 与身份

默认 Topic 路径为 `vda5050/v3/{manufacturer}/{serialNumber}/{topic}`：

```java
import io.github.cooltheworld.rcs.protocol.vda5050.v3.model.common.RobotIdentity;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.DefaultTopicLayout;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicAddress;
import io.github.cooltheworld.rcs.protocol.vda5050.v3.topic.TopicName;

String path = DefaultTopicLayout.standard().format(
    new TopicAddress(
        new RobotIdentity("acme", "robot-001"),
        TopicName.CONNECTION
    )
);
```

部署可以提供自定义 `TopicLayout`，但不能改变八个标准 Topic 的含义，也不能弱化 Robot Identity 和 MQTT 层级安全约束。发布/订阅角色、QoS、retained 与 Last Will 元数据通过 Topic Descriptor API 查询，不应在 Adapter 中重复硬编码。

## JSON 与扩展字段

- `Vda5050JsonCodec.createDefault()` 适合直接处理不可信 UTF-8 payload。
- 复用应用 `ObjectMapper` 时可以注册 `Vda5050JacksonModule`，但调用方仍须自行设置资源约束和完整校验流程。
- 未知字段只由不可变 `ExtensionFields` 透明保存；公共 API 不提供动态业务读取。
- 协议模型不引用 Jackson 类型；`ExtensionFields` 与三维包络内联数据只在显式注册的 `Vda5050JacksonModule` 内转换线路 JSON。
- 不启用 Jackson Default Typing，不把原始 payload、扩展值、动作参数、下载链接或凭据写入日志。

## 厂商 Action 与残余扩展准入

- 使用 `ActionParameter` 和封闭 `ActionParameterValue` 保存线路参数，不把 OBJECT 或 ARRAY 转成通用 Map/JSON 节点。
- 使用 `ActionDefinition<P>` 注册调用方参数 Class、允许的 Scope/Blocking Type 和返回封闭结果的 `ActionParameterAdapter<P>`；`ActionRegistry` 对原文 actionType 区分大小写并拒绝重复覆盖。
- 使用 `ActionAdmission` 依次检查注册、参数 Class、Scope、Blocking 与 Adapter。普通参数错误返回结构化 Issue；Adapter 空结果或抛错属于集成编程错误。
- 已注册 Adapter 处理之后，将仍未识别的 `ExtensionFields` 交给 `ResidualExtensionAdmission` 的角色专属入口。Mobile Robot 控制输入的非空残余扩展 fail closed；Fleet Control 遥测输入只返回不携带扩展内容的观察标记，原强类型消息继续负责不透明保留。
- 核心只执行准入，不执行设备动作，也不会从 payload 类名实例化调用方类型。

## 构造公共 Action

使用 `Action.builder()` 提供原文 `actionType`、`actionId` 和 `BlockingType`。`actionDescriptor`、`actionParameters` 与 `retriable` 均为可选字段；不要用空列表替代缺失参数，也不要用显式 `false` 替代缺失的 `retriable`，除非调用方确实要表达对应线路值。

`Action` 只表示命令对象。调用 `ActionAdmission` 时仍须单独提供 `ActionScope`；Action 的执行状态由后续状态消息模型表达。Builder 不证明 Action 已在目录注册，也不替代 Topic 的 Schema、语义或角色准入。

需要表达状态消息中的动作阶段时，复用独立 `ActionStatus` 七值词汇；不要向 `Action` 添加 Scope 或 Status 字段。

## 构造 Node Position

使用 `AllowedDeviationXY.builder()` 构造可选偏差椭圆，再通过 `NodePosition.builder()` 提供必填 x、y、mapId 和可选 theta、偏差椭圆、允许方向偏差。mapId 保持调用方原文；Builder 不解析地图、不读取坐标系统，也不执行数值范围判断。

通过程序化 API 构造的位置仍须交给 `NodeValidator.create().validate(node)` 检查。Validator 会验证 `sequenceId` 的 `uint32` 闭区间、全部位置数值的有限性，以及节点方向 `[-π, π]`、偏差椭圆方向 `[-π/2, π/2]`、非负半轴和允许方向偏差 `[0, π]`。返回列表为不可变快照，问题说明不包含原始输入值。

使用 `Node.builder()` 提供 nodeId、sequenceId、released 和 actions。没有节点动作时仍须显式传入 `List.of()`；不要用 `null` 表示空列表。可选 nodeDescriptor 与 nodePosition 缺失时保持 `null`，Node 会冻结 actions 顺序但不会判断 Action 是否已注册或可以执行。

`NodeValidator` 不增加正文未规定的 `a >= b` 关系，也不因节点 `theta` 缺失而拒绝单独出现的 `allowedDeviationTheta`。连续 sequenceId、Node/Edge 连接、Base/Horizon 及拼接语义不是单个 Node 的职责。

## 并发与持久化责任

默认 Codec、Schema Validator、Connection Validator、Topic 布局和无状态状态机可缓存复用。调用方必须：

- 对同一角色、Robot Identity 和协议版本串行执行 Transition；
- 将新 State 与全部 Effect 原子提交；
- 使用持久化 Outbox 至少一次交付 Effect，并按确定性 Effect ID 幂等；
- 在快照缺失时进入 `RECOVERING`，不假定机器人空闲；
- 由 Adapter 实现 MQTT retained、Last Will、确认、重试和设备 I/O。

完整边界、测试策略和发布条件见 Spec 仓库的[开发规范](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/DEVELOPMENT.md)与[完成定义](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/DEFINITION-OF-DONE.md)。
