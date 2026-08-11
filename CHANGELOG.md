# 变更日志

本文件记录 `rcs-protocol-java` 面向使用方的重要变化。项目尚未发布稳定版本，当前内容统一归入 `[Unreleased]`；版本号、发布日期和发布链接只在实际发布时填写。

## [Unreleased]

### Added（新增）

- 建立 JDK 21、Maven Wrapper 与独立 `rcs-protocol-vda5050` 模块基线。
- 提供 VDA 5050 v3.0.0 强类型公共值对象、安全 JSON Codec、八个 Topic 的本地 Draft 2020-12 Schema Registry 与 Schema Validator。
- 提供 VDA 5050 Topic 元数据、默认 Topic 布局和可替换布局契约。
- 提供 `Connection` 模型、前三层入站 Validator、Fleet Control 与 Mobile Robot 角色专属连接状态机及跨角色对话测试。
- 提供 Factsheet `TypeSpecification`、`PhysicalParameters`、`ProtocolLimits`、`ProtocolFeatures` 与 `MobileRobotGeometry` 模型片段和确定性 JSON 往返，并校验最大载荷质量、物理参数有限数、Schema 非负下限、成对边界与二维简单多边形语义。
- 提供 Factsheet `LoadSpecification`、`LoadDimensions`、`BoundingBoxReference` 与完整 `LoadSet` 强类型对象图、确定性 Codec 和上下文无关语义 Validator，保留可选集合的缺失/空列表和未知扩展语义。
- 提供 Factsheet `VersionInfo`、`NetworkConfiguration`、`BatteryCharging` 与 `MobileRobotConfiguration` 不可变配置模型、确定性 Codec，以及有限数、百分比、期望区间和正文 `uint32` 充电时间语义 Validator；网络元数据只作为数据。
- 提供 Factsheet 强类型根模型、头部无关的 `FactsheetContent` 能力聚合、平铺确定性 Codec 与完整前三层入站 Validator；Mobile Robot 在活跃连接会话中确定性生成强类型发布 Effect，Fleet Control 保存身份/版本一致的能力、抑制完整重复变化 Effect、冻结首次非空网络基线，并以不泄露扩展键值的诊断 Effect 观察根级和子级扩展；无基础设施跨角色对话验证 Codec、Topic、Validator、重复与拒绝闭环。
- 提供大小写敏感的 `ActionParameter` 与封闭递归 `ActionParameterValue`，以强类型表达六类协议参数值并保持递归集合不可变；提供 `ActionDefinition<P>`、封闭结果 `ActionParameterAdapter<P>`、不可变 `ActionRegistry`、纯 `ActionAdmission` 与八 Topic 角色感知 `ResidualExtensionAdmission`，拒绝重复注册、参数 Class 错配、不允许的 Scope/Blocking 和 Mobile Robot 无法处理的控制扩展，同时只以无内容标记观察 Fleet Control 遥测扩展。
- 提供公共不可变 `Action` 线路聚合，复用强类型参数与 `BlockingType`，保留可选参数缺失/空数组和 `retriable` 缺失/显式 `false` 的不同语义；提供独立七值 `ActionStatus` 供后续状态消息复用，Scope 与 Status 不进入 Action 命令对象。
- 提供不可变 `NodePosition` 与 `AllowedDeviationXY` Order 位置对象图，使用 `Double` 保留坐标、角度、偏差和待 Validator 检查的程序化边界值，地图标识保持原文，未知字段继续不透明保存。
- 提供不可变 `Node` Order 聚合，强类型组合 sequenceId、released、可选位置与公共 `Action`；必填 actions 允许空列表并保持输入顺序，集合防御性复制且拒绝 `null` 元素。新增 `NodeValidator` 以固定安全 Issue 校验单节点 `uint32`、有限数和位置闭区间，不提前执行 Order 图级规则。
- 提供不可变 `Corridor` 及独立封闭的车体参考点和授权丢失行为词汇，精确保存左右宽度、可选字段缺失/显式值和未知扩展；数值语义保留给 Edge Validator。
- 提供不可变 `Edge` 非 Trajectory 聚合和 `EdgeOrientationType`，强类型保存必填序列/Released/Action 与全部可选标量、方向和 Corridor 字段；按正文使用 `maximumRotationSpeed`，不伪造起终节点字段，Trajectory 留给 O04 回接。
- 新增 `EdgeValidator` 以固定安全 Issue 校验单 Edge `uint32`、标量有限性、orientation 闭区间和 Corridor 非负/非双零语义；不增加未声明的非负范围、可选字段依赖或 Order 图级规则。
- 新增共享不可变 `TrajectoryControlPoint`，以必填 `Double x/y`、可选 `Double weight` 和不透明扩展表达 NURBS 控制点；缺失权重保持线路缺失语义，数值范围留给 Trajectory Validator。
- 新增共享不可变 `Trajectory`，精确保存可选 degree/knot vector、必填控制点列表和扩展，区分缺失与空 knot 列表并防御性复制集合；`Edge` 新增可选强类型 Trajectory 字段和值语义。
- 新增 `TrajectoryValidator`，校验 degree `uint32`、有限坐标、严格正权重、knot 范围/非递减、控制点与 knot 基数及 clamped 重数；`EdgeValidator` 组合结果并提升 `/trajectory` 路径，全部新增分支具有测试证据。
- 新增不可变 `Order` 根模型，强类型组合公共 Header、订单标识、更新号、可选说明、Node/Edge 列表和不透明扩展；列表防御性复制并保留待图级 Validator 检查的原始值。
- 提供结构化校验问题、拒绝消息安全上下文和显式协议版本配置。

### Changed（变更）

- 在首个发布前把公共模型迁入 `model.common`、`model.action`、`model.connection` 与 `model.factsheet`，并把双角色 Event/Effect 迁入各自子包；旧扁平包不保留兼容类型。
- 从公共模型移除 Jackson 注解与节点类型，不透明 JSON 只在显式 `Vda5050JacksonModule` 集成中转换。

### Security（安全）

- 在完整对象绑定前限制 payload、嵌套深度、字符串、字段名、数值文本、数组、对象属性和 Token 数量。
- 禁止远程 Schema 获取、Jackson Default Typing 和不可信 payload 的完整日志记录。

### Release status（发布状态）

- Maven 坐标仍为 `0.1.0-SNAPSHOT`，不得将本节内容解释为可用的正式发布。
- 首个稳定版本仍需满足规格仓库的[完成定义](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/DEFINITION-OF-DONE.md)和[发布门禁](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/RELEASE.md)。
