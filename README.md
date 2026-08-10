# rcs-protocol

移动机器人调度协议的 Java 实现，规划支持 VDA 5050 和 GB/T 47864。项目使用 JDK 21 与 Maven，并按协议发布独立 jar。

## 版本兼容性

| Maven 制品 | Maven 版本 | 协议版本 | 状态 |
|---|---|---|---|
| `io.github.cooltheworld:rcs-protocol-vda5050` | `0.1.0-SNAPSHOT` | VDA 5050 `3.0.0` | 实现中，尚未发布 |

Maven 制品版本与协议版本独立演进。只有达到项目规范定义的一致性门槛后，制品才会发布首个稳定版本 `1.0.0`。新增或移除协议版本支持时，必须同步更新本表。

## 构建

项目要求 JDK 21，Maven Wrapper 固定使用 Maven 3.9.14。

```powershell
.\mvnw.cmd validate
```

```shell
./mvnw validate
```

## JSON Codec

`Vda5050JsonCodec.createDefault()` 提供默认的安全 UTF-8 编解码边界。入站解码先执行 payload、深度、字符串、字段名、数值、数组、对象和 Token 资源上限，再创建完整协议对象；普通输入错误以 `DecodingResult<T>` 的拒绝分支返回。解码成功只表示完成语法与基础类型处理，仍须经过 Schema 和协议语义校验才能获得 `ValidatedMessage<T>`。

需要复用应用现有 Jackson `ObjectMapper` 时，可以显式注册 `Vda5050JacksonModule`。该 Module 注册协议值类型以及已建模消息（目前包括 `Connection`）的线路表示，不修改调用方的 null、未知字段、资源限制或多态配置。

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

## 项目文档

- [VDA 5050 Java 实现规格](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/vda5050-java-implementation.md)
- [实施计划](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/tasks/vda5050-java/plan.md)
- [任务清单](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/tasks/vda5050-java/todo.md)
- [当前进度](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/tasks/vda5050-java/progress.md)
