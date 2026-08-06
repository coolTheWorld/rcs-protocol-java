# 实施计划：VDA 5050 v3.0 Java 核心库

## 概述

在 `rcs-protocol-java` 中建立 JDK 21 + Maven 多模块父项目，并以独立制品 `io.github.cooltheworld:rcs-protocol-vda5050` 实现 VDA 5050 v3.0.0。交付采用纵向协议闭环：每个阶段同时完成强类型模型、Jackson Codec、Draft 2020-12 Schema 校验、独立语义校验、双角色纯状态转换和测试。当前计划不实现 `rcs-protocol-spring-boot-starter`，也不修改上游 `coolTheWorld/VDA5050` 仓库。

## 事实来源与优先级

1. VDA 官方发布文档；以 [`coolTheWorld/VDA5050/VDA5050_EN.md`](https://github.com/coolTheWorld/VDA5050/blob/main/VDA5050_EN.md) 作为实现参考。
2. VDA 仓库的 [`json_schemas/*.schema`](https://github.com/coolTheWorld/VDA5050/tree/main/json_schemas) 作为结构校验资产。
3. [`rcs-protocol-spec/vda5050-java-implementation.md`](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/vda5050-java-implementation.md) 中已确认的 Java 与架构决策。
4. [`rcs-protocol-spec/docs/adr/`](https://github.com/coolTheWorld/rcs-protocol-spec/tree/main/docs/adr) 中已接受的架构决策。

正文与 Schema 冲突时以正文为准，并把差异记录到一致性清单和测试中；不得修改上游参考目录来隐藏差异。

## 架构决策

- 父制品为 `rcs-protocol-parent`，VDA 制品为 `rcs-protocol-vda5050:0.1.0-SNAPSHOT`；Java 根包为 `io.github.cooltheworld.rcs.protocol.vda5050.v3`。
- 核心 jar 独立可用，不依赖 Spring、MQTT、Redis 或数据库；Starter 不是运行前提。
- Fleet Control 与 Mobile Robot 使用角色专属 State/Event/Effect/Transition API，一个运行时只启用一个角色。
- 状态机是确定性的 `state + event -> state + effects`；时间和环境事实显式输入，内部 SLF4J 只做旁路诊断。
- 所有标准协议字段使用明确 Java 类型；整数为 `Long`，浮点数为 `Double`，可选字段用 `null`，模型不可变且无 Lombok。
- Jackson 2 负责 JSON，NetworkNT 2.x 负责 Draft 2020-12 Schema；宽容解码、严格分层校验、未知扩展透明保存相互分离。
- 正常状态机事件只接受 `ValidatedMessage<T>`；非法入站消息通过 `RejectedInboundMessage` 进入状态机。
- 协议核心拥有状态和 Effect 的版本化确定性 Codec；基础设施只持久化不透明字节。
- 首个稳定版必须覆盖全部八个 Topic、规范分支场景、跨角色流程和资源安全边界，达到行覆盖率 90%、分支覆盖率 85%。

## 依赖关系

```text
Maven/JDK/质量门禁
  └─ 公共值类型与扩展容器
      ├─ Jackson Codec 与 Schema/语义校验
      └─ Topic 元数据与布局
          └─ connection 纵向闭环
              └─ factsheet 与能力
                  └─ 订单图、准入与更新
                      └─ Action、Instant Action 与取消
                          └─ state、visualization 与发布
                              └─ Zone、Request 与 Response
                                  └─ 状态快照与 Effect Codec
                                      └─ 安全、属性测试、跨角色与发布门禁
```

## 实施阶段

### 阶段 1：构建基础

- [ ] F01 建立 Maven Parent、VDA 模块、JDK 21 配置和 Maven Wrapper。
- [ ] F02 固定可复现的依赖、插件管理和 jar 元数据。
- [ ] F03 建立测试、覆盖率、依赖收敛、漏洞和 API 兼容门禁。
- [ ] F04 打包八份上游 Schema 和必需许可证声明。
- [ ] F05 建立协议一致性追踪清单和 Fixture 约定。

### 检查点 1

- [ ] 在干净检出目录执行 `mvnw.cmd verify` 成功。
- [ ] 生成的 jar 包含预期 Schema、许可证和 `Automatic-Module-Name`。
- [ ] 运行时依赖图不包含 Spring、MQTT、Redis 或数据库依赖。

### 阶段 2：公共基础能力与 Connection 闭环

- [ ] C01 实现协议版本与时间戳值类型。
- [ ] C02 实现 Robot Identity、公共消息头和 uint32 计数器基础。
- [ ] C03 实现不透明扩展字段保留。
- [ ] C04 实现结构化校验结果以及通过/拒绝输入边界。
- [ ] C05 实现有资源上限的 Jackson 解码和协议 Module。
- [ ] C06 实现 Draft 2020-12 Schema 校验与语义 Validator 组合。
- [ ] C07 实现 Topic 描述和默认/自定义 Topic 布局。
- [ ] C08 实现 Connection 模型与 Codec。
- [ ] C09 实现 Connection 语义校验。
- [ ] C10 实现 Fleet Control 连接状态机。
- [ ] C11 实现 Mobile Robot 连接状态机。
- [ ] C12 实现不依赖基础设施的跨角色连接对话。

### 检查点 2

- [ ] Connection 消息在无基础设施环境完成“模型 -> 编码 -> 解码 -> 校验 -> 对端转换”。
- [ ] 重试和 Last Will 场景保持 timestamp 与消息头语义。
- [ ] 聚焦测试和完整测试全部通过。

### 阶段 3：Factsheet 与能力闭环

- [ ] FS01 实现类型说明与物理参数。
- [ ] FS02 实现 Protocol Limits 与部署级上限取交集。
- [ ] FS03 实现协议能力、可选参数与受支持 Action。
- [ ] FS04 实现 Mobile Robot 车轮与包络几何。
- [ ] FS05 实现载荷说明与载荷集合几何。
- [ ] FS06 实现机器人配置、网络元数据与充电限制。
- [ ] FS07 实现 Factsheet 根对象 Codec、Schema/语义校验和角色流程。
- [ ] FS08 实现强类型厂商 Action 注册与扩展准入。

### 检查点 3

- [ ] Factsheet 有效、无效和边界 Fixture 通过往返与校验测试。
- [ ] Factsheet 限制只能收紧部署级上限。
- [ ] 未知可执行语义被拒绝，遥测扩展仍可观察。

### 阶段 4：订单图与准入闭环

- [ ] O01 实现公共 Action 参数与 Blocking 类型。
- [ ] O02 实现 Node、Node Position 与节点 Action 模型。
- [ ] O03 实现 Edge、Corridor 与边 Action 模型。
- [ ] O04 实现 Trajectory、NURBS Control Point 与 Knot Vector 模型。
- [ ] O05 实现 Order 根对象 Codec 与 Schema 校验。
- [ ] O06 实现 Sequence、图、Base/Horizon 与拼接语义 Validator。
- [ ] O07 实现 Mobile Robot Admission Facts 和初始订单接受/拒绝。
- [ ] O08 实现 Mobile Robot 订单更新、重复与冲突处理。
- [ ] O09 实现 Fleet Control 订单提交与对端状态跟踪。
- [ ] O10 实现跨角色订单与订单更新对话。

### 检查点 4

- [ ] 初始订单、更新扩展/替换、重复与拒绝流程图均有可执行测试。
- [ ] 相同订单比较排除传输头，但执行强类型深比较。
- [ ] 状态转换在重放时保持确定性。

### 阶段 5：Action 与取消闭环

- [ ] A01 实现标准 Action 目录与强类型参数校验。
- [ ] A02 实现 Mobile Robot 节点/边 Action 执行与 Blocking 语义。
- [ ] A03 实现 Instant Actions 模型、Codec 与准入。
- [ ] A04 实现暂停、恢复与取消订单转换。
- [ ] A05 实现 Action 反馈与 Fleet Control 跟踪。
- [ ] A06 通过已注册强类型定义执行厂商 Action。
- [ ] A07 实现跨角色 Action、取消与重试对话。

### 检查点 5

- [ ] Action 状态转换与取消订单规范流程可执行。
- [ ] 设备交互只体现为强类型 Effect 和反馈 Event。
- [ ] Blocking 规则与重复 Action ID 均有覆盖。

### 阶段 6：State 与 Visualization 闭环

- [ ] S01 实现订单进度、节点/边状态和路径模型。
- [ ] S02 实现位置、速度、载荷、电源与安全模型。
- [ ] S03 实现 Error、Information、Map、Zone 与请求状态模型。
- [ ] S04 实现 State 根对象 Codec 与完整语义校验。
- [ ] S05 实现 Visualization 模型、Codec 与校验。
- [ ] S06 实现脏状态合并、最小间隔与 30 秒心跳。
- [ ] S07 实现 Fleet Control State/Visualization 跟踪与诊断。

### 检查点 6

- [ ] State 与 Visualization Fixture 覆盖全部标准字段和可选 `null` 语义。
- [ ] 定时行为使用显式 Event/Effect，绝不读取隐式时钟。
- [ ] 快照 Topic 可以合并，同时不丢弃控制消息。

### 阶段 7：Zone、Request 与 Response 闭环

- [ ] Z01 实现 Zone、几何、Corridor 与 Map Reference 模型。
- [ ] Z02 实现 ZoneSet 根对象 Codec 与语义校验。
- [ ] Z03 实现带租约语义的 Zone/Edge Request 与 Response 模型。
- [ ] Z04 实现 Mobile Robot 请求生命周期。
- [ ] Z05 实现 Fleet Control 授权/拒绝生命周期。
- [ ] Z06 实现跨角色 Zone、协同重规划与 Response 对话。

### 检查点 7

- [ ] Zone Access 与协同重规划流程图由确定性测试覆盖。
- [ ] Request ID、授权与租约到期结合会话状态校验。
- [ ] 八个 Topic 均具备完整模型、Codec 与校验覆盖。

### 阶段 8：持久化、恢复与发布门禁

- [ ] P01 实现 Fleet Control 与 Mobile Robot 快照 Codec。
- [ ] P02 实现 Fleet Control 与 Mobile Robot Effect Codec。
- [ ] P03 实现恢复状态、不变量和版本不兼容失败行为。
- [ ] P04 验证消息头计数回绕、确定性 Effect 顺序与重放属性。
- [ ] P05 验证解码资源上限、畸形 payload 语料和模糊/属性测试。
- [ ] P06 验证公共 API、二进制兼容与禁止依赖。
- [ ] P07 完成跨角色一致性套件与规范分支追踪清单。
- [ ] P08 更新 README 兼容矩阵、使用文档与发布证据。

### 最终检查点

- [ ] 使用 JDK 21 在干净检出目录执行 `mvnw.cmd verify` 成功。
- [ ] 行覆盖率不低于 90%，分支覆盖率不低于 85%。
- [ ] 每个已识别的规范状态机分支都关联一个通过的场景。
- [ ] jar 中包含八个 Topic Schema 与许可证声明。
- [ ] 公共 API 和依赖检查通过；核心没有 Spring、MQTT、Redis 或数据库依赖。
- [ ] 在满足一致性门禁前，制品保持 `0.1.0-SNAPSHOT`。

## 项目完成定义

每个实现切片必须：

- 行为代码从一个聚焦的失败测试开始；
- 以聚焦测试通过且受影响模块编译成功结束；
- 保持不可变模型、强类型字段和核心无基础设施依赖边界；
- 实现或发现规范规则时更新一致性追踪清单；
- 不包含跳过测试、生成构建输出、秘密信息或无关修改；
- 在每个阶段检查点通过完整 `mvnw.cmd verify`。

## 风险与缓解措施

| 风险 | 影响 | 缓解措施 |
|---|---|---|
| 规范正文与 JSON Schema 不一致 | 高 | 以规范正文为权威，记录差异，并同时覆盖结构和语义行为。 |
| VDA 模型范围过大导致 DTO 优先的水平构建 | 高 | 坚持 connection 优先的纵向切片，每组模型必须同时提供行为或校验测试。 |
| 前向兼容解码误接受未知可执行语义 | 高 | 分离解码与角色感知的严格校验，正常 Event 必须经过 `ValidatedMessage<T>` 门禁。 |
| State/Effect 快照变化破坏 Redis 部署 | 高 | 使用版本化确定性 Codec、校验不变量、失败关闭且不提供隐式迁移。 |
| 封闭 Event/Effect API 在 1.0 前不稳定 | 中 | 按纵向切片定义变体，在检查点审查公共包并执行 API 兼容门禁。 |
| MQTT payload 或扩展导致资源耗尽 | 高 | 完整绑定前执行字节、Token、深度与集合上限，并加入对抗性测试。 |
| 依赖版本或漏洞数据库需要网络 | 中 | 固定全部版本并缓存依赖，把可复现构建检查与数据库刷新策略分离。 |
| Starter 职责泄漏到核心实现 | 高 | 执行禁止依赖检查和不使用 Spring 的直接跨角色测试。 |

## 待解决问题

当前没有阻塞问题。实施中发现的新歧义必须先依据规范来源解决并记录到 `rcs-protocol-spec`，然后才能改变公共行为。
