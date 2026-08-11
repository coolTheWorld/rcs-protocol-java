# rcs-protocol

移动机器人调度协议的 Java 实现，规划支持 VDA 5050 和 GB/T 47864。项目使用 JDK 21 与 Maven，并按协议发布独立 jar。

## 快速开始

当前制品尚未发布，需要 JDK 21 并先在本地构建：

```powershell
.\mvnw.cmd verify
.\mvnw.cmd install
```

本地安装后引入独立 VDA 5050 制品：

```xml
<dependency>
  <groupId>io.github.cooltheworld</groupId>
  <artifactId>rcs-protocol-vda5050</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

从 MQTT Topic 与不可信 payload 进入核心校验、状态机和 Effect 的完整示例见[使用说明](./docs/usage.md)。

## 版本兼容性

| Maven 制品 | Maven 版本 | 协议版本 | 状态 |
|---|---|---|---|
| `io.github.cooltheworld:rcs-protocol-vda5050` | `0.1.0-SNAPSHOT` | VDA 5050 `3.0.0` | 实现中，尚未发布 |

Maven 制品版本与协议版本独立演进。只有达到项目规范定义的一致性门槛后，制品才会发布首个稳定版本 `1.0.0`。新增或移除协议版本支持时，必须同步更新本表。

## 常用命令

项目要求 JDK 21，Maven Wrapper 固定使用 Maven 3.9.14。

| 目标 | Windows 命令 |
|---|---|
| 校验父项目 | `.\mvnw.cmd validate` |
| 完整质量门禁 | `.\mvnw.cmd verify` |
| 核心模块测试 | `.\mvnw.cmd -pl rcs-protocol-vda5050 test` |
| 聚焦测试 | `.\mvnw.cmd -pl rcs-protocol-vda5050 -Dtest=<测试类> test` |
| 构建核心 jar | `.\mvnw.cmd -pl rcs-protocol-vda5050 package` |
| 检查依赖边界 | `.\mvnw.cmd -pl rcs-protocol-vda5050 dependency:tree` |

Unix-like 环境使用对应的 `./mvnw` 命令，例如：

```shell
./mvnw verify
```

## 架构边界

父项目只聚合独立协议模块；当前唯一模块 `rcs-protocol-vda5050` 负责 VDA 5050 v3.0.0 强类型模型、安全 Codec、Schema 与语义校验、Topic 元数据以及双角色纯状态机。后续协议使用新的独立 jar，不把不同协议模型合并进一个制品。

核心库不连接 Spring、MQTT、Redis、数据库或机器人设备。外部 Adapter 接收字节和 Topic、调度并发、原子持久化 State 与 Effect，并执行实际 I/O；核心只执行确定性的 `state + event -> state + effects + issues`。Fleet Control 与 Mobile Robot 的公共边界彼此独立，一个运行时实例只能选择一个角色。

公共模型按语义分入 `model.common`、`model.action`、`model.connection` 与 `model.factsheet`；Fleet Control 和 Mobile Robot 的 Event/Effect 分别位于各自的 `event`、`effect` 子包。协议模型和状态机不引用 Jackson 类型，线路表示只由 Codec 集成实现持有。

## JSON Codec

`Vda5050JsonCodec.createDefault()` 提供默认的安全 UTF-8 编解码边界。入站解码先执行 payload、深度、字符串、字段名、数值、数组、对象和 Token 资源上限，再创建完整协议对象；普通输入错误以 `DecodingResult<T>` 的拒绝分支返回。解码成功只表示完成语法与基础类型处理，仍须经过 Schema 和协议语义校验才能获得 `ValidatedMessage<T>`。

需要复用应用现有 Jackson `ObjectMapper` 时，可以显式注册 `Vda5050JacksonModule`。该 Module 注册协议值类型、不透明 JSON 值以及已建模消息和子对象（目前包括 `Connection`、`Factsheet`、`TypeSpecification`、`PhysicalParameters`、`ProtocolLimits`、`ProtocolFeatures`、`MobileRobotGeometry`、`LoadSpecification` 与 `MobileRobotConfiguration` 对象图）的线路表示，不修改调用方的 null、未知字段、资源限制或多态配置。

## Factsheet 类型与物理参数

`TypeSpecification` 以独立值类型区分运动学、机器人类别、定位与导航能力。四类可扩展枚举提供 VDA 5050 v3.0.0 标准常量，同时保留未知字符串值；封闭的 `supportedZones` 使用 `ZoneType`。必填能力列表和可选 Zone 列表均执行防御性复制，可选列表继续区分缺失与空数组。

`PhysicalParameters` 的速度、角速度、加速度、减速度及尺寸字段全部使用 `Double`，可选角速度以 `null` 表示缺失。两个对象都使用不可变 Builder，并以 `ExtensionFields` 不透明保存未知字段；默认 Codec 支持确定性片段往返。非负范围、有限数值及最小值/最大值关系将在 Factsheet 组合 Validator 中统一报告。

## Factsheet Protocol Limits

`ProtocolLimits` 以 `MaximumStringLengths`、`MaximumArrayLengths` 和 `ProtocolTiming` 强类型表达正式字段；数组字段使用 `orderNodes`、`trajectoryControlPoints` 等 Java accessor，Codec 仍精确使用 `order.nodes`、`trajectory.controlPoints` 等线路名。原始模型保留字段缺失与显式零值的区别，两者在有效计算中都表示未声明能力上限。

`EffectiveProtocolLimits.resolve(...)` 把 `maximumMessageLength` 与部署 `maxPayloadBytes` 取交集，特定字符串与 `maxStringCharacters` 取交集，特定数组与 `maxArrayElements` 取交集，保证 Factsheet 只能收紧部署硬上限。Timing 不是 JSON 资源上限，因此只将零归一为未声明，不与 `JsonCodecLimits` 取最小值。负数、超过 `uint32` 的长度和非有限 timing 会 fail closed。

## Factsheet 协议能力

`ProtocolFeatures` 强类型表达可选参数和 Mobile Robot Action 能力。`OptionalParameter` 保留参数名、支持说明和可选描述；`MobileRobotAction` 表达 Action 类型、描述、作用域、参数定义与 Blocking Type。集合进行防御性复制，未知字段由 `ExtensionFields` 不透明保存。

`ProtocolFeaturesValidator` 按输入顺序报告重复或冲突的参数、Action、Scope、参数定义和 Blocking Type，不修改或静默去重原始声明。默认 Codec 与 `Vda5050JacksonModule` 支持确定性 JSON 片段往返。

## Factsheet 移动机器人几何

`MobileRobotGeometry` 强类型表达轮定义、二维包络与三维包络；可选集合继续区分缺失与空数组。轮位置、尺寸和二维顶点使用 `Double`，三维包络可以携带内联数据或绝对 URL，未知字段同样透明保存。

`MobileRobotGeometryValidator` 检查有限数值、固定轮必需的朝向，以及三维包络内容来源和 URL 形式。它只返回结构化 `ValidationIssue`，不会下载或打开外部几何资源。

## Factsheet 载荷说明

`LoadSpecification` 强类型表达载荷处理位置和可处理的 `LoadSet` 集合。`LoadSet` 使用必填 `setName`、`loadType` 引用载荷集合，并可携带适用位置、`BoundingBoxReference`、`LoadDimensions`、质量、处理高度/深度/倾角、速度、加减速度、取放时间和描述。全部协议数值使用 `Double`，可选集合保持缺失与空数组的不同线路语义并执行防御性复制。

模型只强制正文与 Schema 明确声明的必填字段，不在 Builder 中执行有限值、范围或字段关系校验。默认 Codec 和独立 Jackson Module 可确定性往返完整载荷对象图；`LoadSpecificationValidator` 以不可变 Issue 列表报告非有限数、违反 Schema 非负下限的数值、倒置边界、重复 `setName` 和未知位置引用，不修改输入。`maximumDeceleration` 在正文与 Schema 中没有非负下限，因此保留有限负值。上游 Schema 把 `LoadSet.maximumSpeed` 的单位元数据误写为 `m/s²`，公共模型按正文使用 `m/s`，差异由 `VDA3-FACTSHEET-002` 跟踪。

## Factsheet 机器人配置

`VersionInfo` 使用必填原始 `key`/`value` 表达软件或硬件版本。`NetworkConfiguration` 保存可选 DNS/NTP 服务器、本地地址、子网掩码和默认网关；服务器列表防御性复制，并保持字段缺失与空列表的不同线路语义。网络值只作为不透明配置数据存在，核心不会解析地址、查询 DNS、发起连接或把它们转换为 MQTT 客户端配置。

`BatteryCharging` 使用三个可选 `Double` 保存临界低电量、最小期望电量和最大期望电量百分比；`minimumChargingTime` 按规范正文的 `uint32` 语义使用 `Long`。`MobileRobotConfiguration` 聚合可选版本列表、网络元数据和充电参数，保持缺失与空版本列表的不同语义。默认 Codec 与独立 Jackson Module 可确定性往返完整配置对象图，并在绑定前识别全部嵌套标准字段，使标准显式 `null` 与非法形状结构化拒绝、未知扩展及扩展 `null` 透明保存。

模型不在 Builder 中丢弃非法原始边界；`MobileRobotConfigurationValidator` 以不可变 Issue 列表报告非有限值、百分比越界、倒置期望区间和超出 `uint32` 的充电时间，不修改输入，也不解析网络字符串。运行期网络信息不变规则需要历史状态，由 Factsheet 角色流程执行。

## Factsheet 根模型

`Factsheet` 强类型组合公共 `ProtocolHeader`、头部无关的 `FactsheetContent` 和根级 `ExtensionFields`。`FactsheetContent` 聚合类型说明、物理参数、协议限制、协议能力、机器人几何、载荷说明和可选机器人配置，供角色 Event 在不伪造 Header 的情况下提交能力内容。它不是额外 JSON 层级；默认 Codec 与独立 Jackson Module 会在 Factsheet 根对象中平铺五个 Header 字段和七个内容字段。

完整与边界 Fixture 同时通过确定性往返和 Draft 2020-12 Schema 校验，覆盖 `headerId` 的 `uint32` 两个端点、缺失可选配置、根与嵌套扩展。绑定前字段元数据覆盖全部根字段，标准显式 `null`、缺失必填和非法对象形状均通过封闭解码结果拒绝。

## Schema Validator

`Vda5050SchemaValidator.createDefault()` 提供八个 Topic 的 Draft 2020-12 Schema 校验。它会在 NetworkNT 解析前执行与默认 Codec 相同的 JSON 资源硬上限，关闭远程 Schema 获取，并为 `date-time` 启用 Format Assertion。语法、资源和 Schema 失败统一返回不可变 `ValidationIssue` 列表；说明文本不复制不可信输入值。

Validator 在创建时检查并缓存八份 classpath Schema，设计为线程安全复用。`uint32` 范围不依赖上游自定义 Schema Format，仍由后续强类型 `Long` 语义 Validator 检查闭区间 `[0, 4294967295]`。

## Connection Validator

`ConnectionValidator.createDefault()` 是 `connection` 入站消息获得 `ValidatedMessage<Connection>` 的公共入口。调用方传入部署使用的 `TopicLayout`、实际 MQTT Topic 路径和原始 UTF-8 payload；Validator 依次执行有界 JSON 与 Schema 校验、强类型解码，以及 `headerId`、显式协议版本、Topic 类型和 Topic/Header 身份一致性检查。

普通非法输入返回 `RejectedInboundMessage<Connection>`，不会抛出协议异常或保留原始 payload。需要收紧部署资源上限时使用 `ConnectionValidator.create(JsonCodecLimits)`；同一组限制会同时用于 Schema 前置解析和强类型 Codec。裸 `Connection` 不能通过公共 API 包装为成功凭证。

## Fleet Control Connection 状态机

`FleetControlStateMachine.createDefault()` 提供按 Robot Identity 聚合的 Fleet Control 纯状态机。正常事件只接受 `ValidatedMessage<Connection>`，拒绝事件接受 `RejectedInboundMessage<Connection>`；事件时间由调用方显式传入，状态机不访问系统时钟或外部 I/O。

状态机观察 `ONLINE`、`OFFLINE`、`HIBERNATING` 与 `CONNECTION_BROKEN`。相同连接状态的 retained 或重复消息会更新最近消息但不重复产生状态变化 Effect；Last Will 即使携带陈旧的 `headerId` 和 `timestamp`，仍会被观察为 `CONNECTION_BROKEN`。前三层拒绝和会话身份误投保持 State 不变，并返回结构化 Issue 与不含 payload/扩展值的诊断 Effect。状态机内部仅通过 SLF4J API 输出旁路安全日志，日志不参与 Transition 计算。

## Mobile Robot Connection 状态机

`MobileRobotStateMachine.createDefault()` 提供按 Robot Identity 聚合的 Mobile Robot 纯状态机。上线请求使用调用方显式传入的时间，先产生携带 `CONNECTION_BROKEN` 完整消息的 `ConfigureConnectionLastWill`，再产生 `PublishConnection` 发布 `ONLINE`；两条消息使用相邻且可回绕的 Connection Topic `headerId`。

已建立连接会话后可主动发布 `ONLINE`、`OFFLINE` 或 `HIBERNATING`，不能主动发布 `CONNECTION_BROKEN`。初始状态和发布 `OFFLINE` 后必须重新执行上线序列，否则 Transition 保持 State 并返回结构化 Issue。Effect 保存完整不可变 Connection，Outbox 重试应重交付同一持久化 Effect，从而复用消息头与时间。核心只描述协议 Effect，不引用 MQTT 客户端；连接、保留发布、确认和重试调度由外部 Adapter 负责。

## Connection 跨角色对话

核心边界可以在同一进程中直接组合：Mobile Robot 的 `PublishConnection` 或 Last Will 消息经 `Vda5050JsonCodec` 编码，使用 `TopicLayout` 形成路径，由 `ConnectionValidator` 铸造 `ValidatedMessage<Connection>`，再作为 `FleetControlEvent.ConnectionReceived` 进入 Fleet Control 状态机。该路径覆盖上线、正常下线、意外断线以及重复/重试交付，编码前后的完整 Connection 保持相等。

核心不提供额外的 Broker 模拟器或跨角色编排器，也不依赖 Spring、MQTT 客户端或 Redis；真实传输、retained 发布和交付确认继续属于 Adapter。仓库中的 `ConnectionDialogueTest` 使用真实核心实现验证上述边界。

## 项目文档

- [核心库使用说明](./docs/usage.md)
- [贡献指南](./CONTRIBUTING.md)
- [变更日志](./CHANGELOG.md)
- [VDA 5050 Java 实现规格](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/vda5050-java-implementation.md)
- [开发规范与完成定义](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/DEVELOPMENT.md)
- [实施计划](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/tasks/vda5050-java/plan.md)
- [任务清单](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/tasks/vda5050-java/todo.md)
- [当前进度](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/tasks/vda5050-java/progress.md)

## 贡献

行为变更先在 Spec 仓库确认需求和追踪条目，再按测试驱动方式实现。提交或 Pull Request 前请阅读[贡献指南](./CONTRIBUTING.md)，并运行与改动范围相称的测试；阶段检查点必须通过完整 `verify`。

## 许可证状态

项目根许可证尚未由维护者确认，当前不得推断或宣称某个开源许可证。`rcs-protocol-vda5050` 中随上游 Schema 打包的许可证只覆盖相应上游材料，不等同于本仓库整体许可；正式发布前必须补齐项目许可证和 Maven 发布元数据。
