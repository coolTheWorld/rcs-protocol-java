# 变更日志

本文件记录 `rcs-protocol-java` 面向使用方的重要变化。项目尚未发布稳定版本，当前内容统一归入 `[Unreleased]`；版本号、发布日期和发布链接只在实际发布时填写。

## [Unreleased]

### Added（新增）

- 建立 JDK 21、Maven Wrapper 与独立 `rcs-protocol-vda5050` 模块基线。
- 提供 VDA 5050 v3.0.0 强类型公共值对象、安全 JSON Codec、八个 Topic 的本地 Draft 2020-12 Schema Registry 与 Schema Validator。
- 提供 VDA 5050 Topic 元数据、默认 Topic 布局和可替换布局契约。
- 提供 `Connection` 模型、前三层入站 Validator、Fleet Control 与 Mobile Robot 角色专属连接状态机及跨角色对话测试。
- 提供 Factsheet `TypeSpecification`、`PhysicalParameters`、`ProtocolLimits`、`ProtocolFeatures` 与 `MobileRobotGeometry` 模型片段和确定性 JSON 往返。
- 提供 Factsheet `LoadSpecification`、`LoadDimensions`、`BoundingBoxReference` 与完整 `LoadSet` 强类型对象图、确定性 Codec 和上下文无关语义 Validator，保留可选集合的缺失/空列表和未知扩展语义。
- 提供 Factsheet `VersionInfo`、`NetworkConfiguration`、`BatteryCharging` 与 `MobileRobotConfiguration` 不可变配置模型；网络元数据只作为数据，充电时间按正文 `uint32` 使用 `Long`。
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
