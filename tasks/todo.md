# VDA 5050 v3.0 Java 核心库任务清单

## 执行规则

- 状态：`[ ]` 待处理、`[~]` 处理中、`[x]` 已完成。
- 行为任务严格执行“失败测试 -> 最小实现 -> 重构”。
- 每个实现增量尽量不超过五个文件；模型较多的任务必须继续拆成小提交。
- 聚焦测试：`.\mvnw.cmd -pl rcs-protocol-vda5050 -Dtest=<测试类> test`。
- 模块验证：`.\mvnw.cmd -pl rcs-protocol-vda5050 verify`。
- 完整验证：`.\mvnw.cmd verify`。
- 本计划不修改上游 `coolTheWorld/VDA5050` 仓库，也不实现 `rcs-protocol-spring-boot-starter`。

## 阶段 1：构建基础

- [ ] **F01 Maven Parent 与 Wrapper**
  - 验收：坐标与规格一致，只聚合 VDA 模块，Windows/Unix Wrapper 均可运行。
  - 验证：`.\mvnw.cmd -version`；`.\mvnw.cmd -N validate`。
  - 依赖：无。文件：根 `pom.xml`、Wrapper 文件、`.gitignore`。

- [ ] **F02 VDA 模块与 jar 身份**
  - 验收：制品为 `rcs-protocol-vda5050:0.1.0-SNAPSHOT`，Java Release 为 21，Manifest 包含约定模块名。
  - 验证：`.\mvnw.cmd -pl rcs-protocol-vda5050 package` 并检查 Manifest。
  - 依赖：F01。文件：父/模块 POM 与 Manifest 配置。

- [ ] **F03 质量门禁**
  - 验收：启用 JUnit、JaCoCo、Enforcer、依赖收敛、漏洞和公共 API 检查，并禁止核心基础设施依赖。
  - 验证：`.\mvnw.cmd verify`，每项门禁做一次临时反向验证后撤销测试改动。
  - 依赖：F02。文件：父/模块 POM 与检查配置。

- [ ] **F04 Schema 与许可证资源**
  - 验收：八份 Schema 与 VDA MIT 许可证进入 classpath，内容校验和与上游一致。
  - 验证：`SchemaResourcesTest` 与 jar 条目检查。
  - 依赖：F02。文件：`src/main/resources/vda5050/v3.0.0/` 与资源测试。

- [ ] **F05 协议一致性追踪清单**
  - 验收：规范条款、Validator、Transition、Fixture 与测试使用稳定 ID 关联，已知 Schema 差异有记录。
  - 验证：文档链接检查与一致性清单测试骨架。
  - 依赖：F04。文件：`rcs-protocol-spec/vda5050-conformance.md`、测试资源说明与清单测试。

## 阶段 2：公共基础能力与 Connection 闭环

- [ ] **C01 协议版本与时间戳**
  - 验收：3.0.0 是唯一受支持配置；未知版本仍可表示；时间严格往返为 UTC 三位毫秒 `Z` 格式。
  - 验证：值对象聚焦测试。
  - 依赖：F03。文件：版本、配置、时间戳类型及测试。

- [ ] **C02 Robot Identity、消息头与 uint32 计数器**
  - 验收：身份精确匹配且不规范化；序列号字符合法；计数器从零开始并在最大值后回绕。
  - 验证：身份、消息头与计数器测试。
  - 依赖：C01。文件：公共模型、内部 `Unsigned32` 与测试。

- [ ] **C03 不透明扩展字段保留**
  - 验收：未知字段和未知 `null` 可往返；公共 API 不暴露动态 JSON；标准字段不能被扩展覆盖。
  - 验证：扩展往返与冲突测试。
  - 依赖：F03。文件：`ExtensionFields`、内部 Jackson 支持与测试。

- [ ] **C04 结构化校验与类型门禁**
  - 验收：普通非法输入以数据表达；调用方无法伪造校验成功凭证；拒绝对象只携带安全强类型上下文。
  - 验证：校验 API 与构造边界测试。
  - 依赖：C01-C03。文件：Issue、Result、`ValidatedMessage`、`RejectedInboundMessage` 与测试。

- [ ] **C05 有资源上限的 Jackson Codec**
  - 验收：可选 `null` 输出时省略；标准显式 `null` 被报告；禁用 Default Typing；超限输入在完整建模前失败。
  - 验证：Codec 上限与 `null` 语义测试。
  - 依赖：C03-C04。文件：Codec、Jackson Module、限制类型与测试。

- [ ] **C06 Draft 2020-12 Schema Validator**
  - 验收：Schema Issue 具有稳定路径与代码；`uint32` 仍由 Long 语义校验；八份 Schema 均可编译。
  - 验证：有效/无效 Fixture 的 Schema 测试。
  - 依赖：F04、C04-C05。文件：Schema 注册、Validator、Issue 映射与测试。

- [ ] **C07 Topic 元数据与布局**
  - 验收：八个 Topic 的方向、QoS、Retained、Last Will 正确；默认布局可往返身份；不安全或不一致身份被拒绝。
  - 验证：Topic 描述与布局测试。
  - 依赖：C02、C04。文件：Topic 类型、描述、布局实现与测试。

- [ ] **C08 Connection 模型与 Codec**
  - 验收：有效 Fixture 可往返；扩展保留；必填、时间与数值字段遵循公共规则。
  - 验证：先失败后通过的 `ConnectionCodecTest` 与 Schema 测试。
  - 依赖：C01-C07。文件：Connection 模型、Builder、值类型、Fixture 与测试。

- [ ] **C09 Connection 语义校验**
  - 验收：分层校验相互独立；不支持版本与 Topic/消息头不一致有稳定 Issue；只有合法消息产生凭证。
  - 验证：`ConnectionValidatorTest`。
  - 依赖：C08。文件：Connection Validator、Issue 定义与测试。

- [ ] **C10 Fleet Control 连接状态机**
  - 验收：ONLINE、OFFLINE、CONNECTIONBROKEN 与拒绝输入路径符合规范；日志不改变 Transition。
  - 验证：先编写失败的转换表测试，再实现并运行聚焦测试。
  - 依赖：C09。文件：角色 State/Event/Effect/Transition 与测试，按小增量提交。

- [ ] **C11 Mobile Robot 连接状态机**
  - 验收：连接发布、Last Will 与重试消息确定；重试复用消息头与时间；不引用 MQTT 客户端。
  - 验证：失败转换测试 -> 实现 -> 聚焦测试。
  - 依赖：C09。文件：角色 State/Event/Effect/Transition 与测试。

- [ ] **C12 跨角色连接对话**
  - 验收：正常、Last Will 和重复/重试流程不依赖 Spring、MQTT、Redis，消息数据精确保留。
  - 验证：跨角色测试与阶段 2 完整 `verify`。
  - 依赖：C10-C11。文件：跨角色测试、Fixture 与一致性清单。

## 阶段 3：Factsheet 与能力闭环

- [ ] **FS01 类型说明与物理参数**
  - 验收：运动学、类别、定位、导航、Zone 与物理参数均有明确字段和正确数值包装类型。
  - 验证：Builder 与 Codec 片段测试。
  - 依赖：C12。文件：模型与测试，按不超过五个文件拆分。

- [ ] **FS02 Protocol Limits**
  - 验收：全部字符串、数组、时序限制有明确 accessor；零/缺失表示未声明；有效限制不超过部署上限。
  - 验证：限制取交集与属性测试。
  - 依赖：FS01、C05。文件：限制模型、有效限制计算与测试。

- [ ] **FS03 协议能力与 Action 声明**
  - 验收：Scope、Blocking、数据类型与参数定义强类型化；重复或冲突声明被报告。
  - 验证：协议能力语义测试。
  - 依赖：FS01。文件：能力与 Action 声明模型、Validator 与测试。

- [ ] **FS04 Mobile Robot 几何**
  - 验收：车轮、位置、二维/三维包络字段完整；有限数值、URL/Data 条件与集合不可变性通过校验。
  - 验证：几何模型与语义测试。
  - 依赖：FS01。文件：几何模型、Validator 与测试。

- [ ] **FS05 载荷说明**
  - 验收：载荷位置、集合、包围盒与尺寸全部强类型；引用关系和范围合法。
  - 验证：载荷说明测试。
  - 依赖：FS01。文件：载荷模型、Validator 与测试。

- [ ] **FS06 机器人配置**
  - 验收：版本、网络元数据与电池充电参数字段完整；网络仅作为数据；充电范围合法。
  - 验证：配置模型与校验测试。
  - 依赖：FS01。文件：配置模型、Validator 与测试。

- [ ] **FS07 Factsheet 根对象闭环**
  - 验收：完整有效/无效/边界 Fixture 通过；Mobile Robot 确定发布；Fleet Control 保存强类型能力。
  - 验证：跨角色 Factsheet 测试与阶段 3 `verify`。
  - 依赖：FS02-FS06。文件：根模型、Validator、角色转换、Fixture 与测试。

- [ ] **FS08 强类型扩展与厂商 Action 注册**
  - 验收：注册定义可解码为调用方类型；未注册控制语义被拒绝；未知遥测仍可往返。
  - 验证：注册表与角色感知准入测试。
  - 依赖：FS03、FS07。文件：Registry、Definition、Adapter 契约与测试。

## 阶段 4：订单图与准入闭环

- [ ] **O01 公共 Action 模型**
  - 验收：Action、ActionParameter、Scope、Status、Blocking 字段明确，保留 `List<ActionParameter>`。
  - 验证：Action 模型测试。
  - 依赖：FS08。文件：Action 模型、值类型与测试。

- [ ] **O02 Node 模型**
  - 验收：Node、NodePosition、Sequence、Released 与节点 Action 语义明确，集合不可变。
  - 验证：Node 模型测试。
  - 依赖：O01。文件：Node/Position 模型、Builder 与测试。

- [ ] **O03 Edge 与 Corridor 模型**
  - 验收：起终点、Sequence、Released、Corridor 与边 Action 字段完整，范围有限且合法。
  - 验证：Edge 模型测试。
  - 依赖：O01-O02。文件：Edge/Corridor 模型、Builder 与测试。

- [ ] **O04 Trajectory 模型**
  - 验收：Trajectory、NURBS Control Point、Weight、Degree、Knot Vector 使用 Long/Double 并满足基数约束。
  - 验证：Trajectory 单元与属性测试。
  - 依赖：O03。文件：Trajectory 模型、Validator 与测试。

- [ ] **O05 Order Codec 与 Schema 校验**
  - 验收：完整订单可往返；显式 `null` 和类型错误结构化报告；扩展保留但不自动准入。
  - 验证：`OrderCodecTest` 与 `OrderSchemaTest`。
  - 依赖：O02-O04。文件：Order 根模型、Builder、Codec 注册、Fixture 与测试。

- [ ] **O06 订单图语义校验**
  - 验收：Sequence、图连接、Base/Horizon、Released、拼接与订单内容身份均有稳定 Issue 和测试。
  - 验证：图 Validator 与属性测试。
  - 依赖：O05。文件：图 Validator 组件、Issue 与测试。

- [ ] **O07 Mobile Robot 初始订单准入**
  - 验收：最终接受/拒绝由状态机决定；非法输入产生协议 Effect；环境只通过 Facts/Effects 交互。
  - 验证：先编写失败的准入转换表测试。
  - 依赖：O06、C04。文件：Admission Facts、Event、Effect、状态机切片与测试。

- [ ] **O08 Mobile Robot 更新与重复处理**
  - 验收：支持扩展/替换与拼接；相同重复为无操作；相同版本内容冲突产生 `SAME_ORDER_UPDATE_ID`。
  - 验证：规范流程图与属性测试。
  - 依赖：O07。文件：更新转换、Issue、测试与一致性清单。

- [ ] **O09 Fleet Control 订单生命周期**
  - 验收：按 Robot Identity 提交并跟踪订单；队列和分配保持非目标；重发复用已物化消息。
  - 验证：Fleet Control 订单测试。
  - 依赖：O06。文件：Fleet Event/Effect/State 转换与测试。

- [ ] **O10 跨角色订单对话**
  - 验收：初始、更新、重复与拒绝对话无需基础设施；状态、Issue、Effect 符合规范且可确定重放。
  - 验证：跨角色测试与阶段 4 `verify`。
  - 依赖：O08-O09。文件：跨角色测试、Fixture 与一致性清单。

## 阶段 5：Action 与取消闭环

- [ ] **A01 标准 Action 目录**
  - 验收：标准 Action、Scope、参数与语义可发现且强类型；参数错误使用稳定 Issue。
  - 验证：参数化 Action 目录测试。
  - 依赖：O10。文件：目录分组、Validator 与测试。

- [ ] **A02 订单 Action 执行**
  - 验收：节点/边 Action 通过 `ExecuteAction` Effect 执行，反馈 Event 推进状态，Blocking 不变量符合规范。
  - 验证：失败的 Action 转换表测试 -> 实现 -> 聚焦测试。
  - 依赖：A01、O07。文件：Event/Effect/State 转换与测试。

- [ ] **A03 Instant Actions 闭环**
  - 验收：消息 Codec、Topic、ID 与准入规则正确；注册 Action 可执行；不支持 Action 产生协议 Error。
  - 验证：Codec、Validator 与转换测试。
  - 依赖：A01-A02。文件：根模型、Validator、角色转换与测试。

- [ ] **A04 暂停、恢复与取消**
  - 验收：取消行为结合状态；尊重不可取消 Action；不引入隐式时钟或设备 I/O。
  - 验证：取消订单流程图与边界测试。
  - 依赖：A02-A03。文件：转换规则、Issue、Effect、测试与清单。

- [ ] **A05 Fleet Control Action 跟踪**
  - 验收：跟踪订单、即时和 Zone Action；重复反馈幂等；非法回退被诊断。
  - 验证：Fleet Control Action 跟踪测试。
  - 依赖：A02-A04。文件：Fleet State/Event 转换与测试。

- [ ] **A06 厂商 Action 执行**
  - 验收：强类型参数到达 Adapter 边界；未注册定义不能执行；注册输出确定。
  - 验证：自定义 Action 聚焦测试。
  - 依赖：FS08、A02-A04。文件：注册执行桥接与测试。

- [ ] **A07 跨角色 Action 对话**
  - 验收：覆盖 Blocking、即时、暂停/恢复、取消、失败与重试；Effect 重试不重复改变状态。
  - 验证：跨角色 Action 套件与阶段 5 `verify`。
  - 依赖：A05-A06。文件：跨角色测试、Fixture 与清单。

## 阶段 6：State 与 Visualization 闭环

- [ ] **S01 进度与路径状态模型**
  - 验收：Node/Edge 进度、计划/中间路径与订单引用强类型化，顺序和跨字段引用合法。
  - 验证：状态片段测试。
  - 依赖：A07。文件：模型、Validator 与测试。

- [ ] **S02 运动、载荷、电源与安全模型**
  - 验收：位置、速度、载荷、行驶/暂停、电源、运行模式与安全字段完整，范围与缺失语义正确。
  - 验证：状态片段与属性测试。
  - 依赖：S01。文件：模型、Validator 与测试。

- [ ] **S03 诊断、Map、Zone 与请求状态模型**
  - 验收：Error、Information、Map、ZoneSet、ZoneRequest、EdgeRequest 均使用明确对象；Information 不参与业务逻辑。
  - 验证：状态片段测试。
  - 依赖：S01。文件：模型、Validator 与测试。

- [ ] **S04 State 根对象闭环**
  - 验收：完整 Fixture 可往返；State 反映真实会话与 Action；Fleet Control 处理已知遥测并保留扩展。
  - 验证：State Codec、Schema、语义与角色测试。
  - 依赖：S01-S03。文件：根模型、Builder、Validator、转换与测试。

- [ ] **S05 Visualization 闭环**
  - 验收：Reference State Header 语义合法；Visualization 不替代 State；可选遥测保持强类型。
  - 验证：Visualization 模型、Codec、Validator 与角色测试。
  - 依赖：S02、S04。文件：模型、Builder、Validator 与测试。

- [ ] **S06 State 发布调度**
  - 验收：实现脏状态合并、`ScheduleStatePublish` Effect、`StatePublishDue` Event、最小间隔与 30 秒心跳，无隐式时钟。
  - 验证：确定性定时与属性测试。
  - 依赖：S04。文件：定时 Event/Effect、转换与测试。

- [ ] **S07 Fleet Control 遥测跟踪**
  - 验收：已知遥测更新强类型状态；未知扩展不驱动逻辑；启停日志不改变 State/Effects/Issues。
  - 验证：日志开关状态等价测试与阶段 6 `verify`。
  - 依赖：S05-S06。文件：Fleet 转换、诊断辅助、测试与清单。

## 阶段 7：Zone、Request 与 Response 闭环

- [ ] **Z01 Zone 几何与 Corridor 模型**
  - 验收：Zone、几何、Map Reference、Corridor 与 Zone Action 字段完整，坐标和关系合法。
  - 验证：Zone 模型测试。
  - 依赖：S07。文件：模型、Validator 与测试。

- [ ] **Z02 ZoneSet Codec 与校验**
  - 验收：完整 Fixture 可往返；重复 ID、非法 Map/几何有稳定 Issue；未知扩展不能执行。
  - 验证：ZoneSet Codec、Schema 与语义测试。
  - 依赖：Z01。文件：根模型、Builder、Validator、Fixture 与测试。

- [ ] **Z03 Response 模型与校验**
  - 验收：Grant Type 与 Lease Expiry 语义正确；适用 uint32 字段由 Long 校验；Request ID 属于会话范围。
  - 验证：Responses Codec、Schema 与语义测试。
  - 依赖：C06、Z02。文件：Response 模型、Validator 与测试。

- [ ] **Z04 Mobile Robot 请求生命周期**
  - 验收：Zone Access 与 Edge/Replanning Request 状态确定；未收到响应不能静默授权；过期结合会话处理。
  - 验证：规范请求转换测试。
  - 依赖：Z03。文件：Mobile Event/Effect/Transition 与测试。

- [ ] **Z05 Fleet Control 请求生命周期**
  - 验收：外部交通决策通过强类型 Facts/Event 输入；状态机生成确定的授权/拒绝响应，不内嵌外部策略。
  - 验证：Fleet Control 请求测试。
  - 依赖：Z03。文件：Fleet Event/Effect/Transition 与测试。

- [ ] **Z06 跨角色 Zone/Request 对话**
  - 验收：覆盖访问、协同重规划、拒绝与租约到期；非法/过期响应安全；八个 Topic 均被执行。
  - 验证：跨角色套件与阶段 7 `verify`。
  - 依赖：Z04-Z05。文件：跨角色测试、Fixture 与清单。

## 阶段 8：持久化、恢复与发布

- [ ] **P01 状态快照 Codec**
  - 验收：Fleet Control/Mobile Robot 快照确定性往返；角色、版本、不变量严格校验；损坏或不兼容失败关闭。
  - 验证：往返、损坏与确定性测试。
  - 依赖：Z06。文件：角色快照 Codec、版本类型与测试。

- [ ] **P02 Effect Codec**
  - 验收：全部封闭 Effect 变体确定性往返；未知/不兼容变体失败关闭；消息头和时间保持。
  - 验证：参数化角色 Effect Codec 测试。
  - 依赖：P01。文件：角色 Effect Codec、版本类型与测试。

- [ ] **P03 恢复与状态不变量**
  - 验收：快照缺失进入 `RECOVERING`；两个角色要求强类型恢复事实；状态 Schema 不兼容为致命错误。
  - 验证：恢复转换与损坏快照测试。
  - 依赖：P01-P02。文件：恢复 Event、转换、不变量 Validator 与测试。

- [ ] **P04 确定性与重放属性**
  - 验收：相同 State/Event 得到相等 Transition；CAS 重算稳定；消息头回绕、Sequence、revision 与 Effect 顺序满足属性。
  - 验证：使用固定 Seed 的属性测试。
  - 依赖：P03。文件：属性测试、Generator 与清单。

- [ ] **P05 对抗性解码语料**
  - 验收：字节、深度、Token、字符串、数值、数组、对象上限在危险构造前失败；日志不复制完整 payload。
  - 验证：对抗性与模糊/属性测试。
  - 依赖：C05、P03。文件：语料、Generator 与安全测试。

- [ ] **P06 公共 API 与依赖验证**
  - 验收：`.internal` 排除在公共 API 外；Event/Effect 封闭规则有文档；核心无禁止依赖。
  - 验证：`.\mvnw.cmd verify`、API 报告和依赖树检查。
  - 依赖：P01-P05。文件：POM 门禁、API 基线与包文档。

- [ ] **P07 完成协议一致性覆盖**
  - 验收：八个 Topic 均有有效/无效/边界 Fixture；全部已识别强制分支有测试；覆盖率门槛通过。
  - 验证：干净检出目录执行完整 `.\mvnw.cmd verify`。
  - 依赖：P06。文件：测试、Fixture 与一致性清单。

- [ ] **P08 发布文档与证据**
  - 验收：README 包含 Maven/协议版本、独立使用方式与 Wrapper 命令；一致性状态如实，不提前声明 1.0。
  - 验证：文档链接、干净构建、jar 检查与 `git diff --check`。
  - 依赖：P07。文件：README、兼容矩阵、一致性文档与发布证据。
