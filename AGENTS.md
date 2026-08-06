# Java 核心仓库协作指南

## 目标与事实来源

本仓库用于维护移动机器人调度协议的 Java 核心实现。当前编码目标是使用 JDK 21 与 Maven 实现 VDA 5050 v3.0.0；Fleet Control 与 Mobile Robot 两个角色都要实现，但一个运行时只能启用一个角色。后续协议使用独立 Maven jar，不把所有协议模型塞进一个制品。

事实来源按以下优先级使用：

1. VDA 正式发布文档。
2. [`coolTheWorld/VDA5050`](https://github.com/coolTheWorld/VDA5050) 中的规范正文。
3. VDA 仓库 `json_schemas/*.schema` 中的 Draft 2020-12 Schema。
4. [`rcs-protocol-spec`](https://github.com/coolTheWorld/rcs-protocol-spec) 中已确认的实现规格。
5. `rcs-protocol-spec/docs/adr/` 中已接受的架构决策。

规范正文与 JSON Schema 不一致时，以规范正文为准，在一致性清单和测试中记录差异，不得修改上游文件来掩盖差异。

本文件是 `rcs-protocol-spec` 当前 Spec 的派生执行说明，不是独立事实来源。本文件与 Spec 冲突时必须以 Spec 为准并同步更正；Spec 更新了本文件中也存在的声明时，必须在同一工作周期更新本文件。不得在本文件中自行引入 Spec 未确认的项目决定。

相关仓库地址：

- Java 核心库：`https://github.com/coolTheWorld/rcs-protocol-java.git`
- Spring Boot Starter：`https://github.com/coolTheWorld/rcs-protocol-spring-boot-starter.git`
- Spec：`https://github.com/coolTheWorld/rcs-protocol-spec.git`

## 技术栈与版本

- Java：JDK 21，编译使用 `--release 21`。
- 构建：Maven 多模块父项目并提交 Maven Wrapper；当前环境 Maven 为 3.9.14。
- 核心 JSON：Jackson 2.21.4，使用 `com.fasterxml.jackson.*`。
- JSON Schema：NetworkNT JSON Schema Validator 2.0.1，Draft 2020-12。
- 日志：SLF4J API 2.0.18；日志可以位于状态机内部，但不得改变转换结果。
- 测试：JUnit 5、属性测试、跨角色进程内测试、JaCoCo。
- 后续 Starter：Spring Boot 3.5.16；本轮不在核心仓库实现 Starter。

禁止动态版本、版本范围、`LATEST` 或 `RELEASE`。核心 Parent 不导入 Spring Boot BOM，依赖与插件版本都必须显式固定。

## 目录与模块

- 仓库根目录：Maven 多模块父项目。父制品为 `io.github.cooltheworld:rcs-protocol-parent`。
- `rcs-protocol-vda5050/`：VDA 5050 独立核心 jar，坐标为 `io.github.cooltheworld:rcs-protocol-vda5050:0.1.0-SNAPSHOT`。
- 外部参考仓库 `coolTheWorld/VDA5050`：VDA 5050 v3.0.0 规范正文、Schema 与图示，不因本仓库实现而修改。
- 外部规格仓库 `coolTheWorld/rcs-protocol-spec`：中文实现规格、领域词汇、一致性清单、ADR，以及位于 `tasks/vda5050-java/` 的实施计划、任务清单和项目进度。
- 外部 Starter 仓库 `coolTheWorld/rcs-protocol-spring-boot-starter`：Spring Boot、MQTT 与状态存储基础设施集成，不承载协议业务。

生产代码和测试分别放在模块的 `src/main/java` 与 `src/test/java`，测试包必须镜像生产包。Fixture 放在 `src/test/resources`，打包 Schema 放在 `src/main/resources/vda5050/v3.0.0`。

嵌套仓库独立版本化。检查状态时明确指定仓库，例如 `git -C rcs-protocol-java status --short`，不要假定工作区根目录是同一个 Git 仓库。

## 标准命令

工程引导完成前可执行：

- `rg --files`：快速列出文件。
- `git status --short`：检查核心仓库改动。
- `git diff --check`：检查空白错误。

F01 完成后统一使用 Wrapper：

- `.\mvnw.cmd verify`：完整构建与全部质量门禁。
- `.\mvnw.cmd -pl rcs-protocol-vda5050 test`：运行核心模块测试。
- `.\mvnw.cmd -pl rcs-protocol-vda5050 -Dtest=<测试类> test`：运行聚焦测试。
- `.\mvnw.cmd -pl rcs-protocol-vda5050 package`：构建核心 jar。
- `.\mvnw.cmd -pl rcs-protocol-vda5050 dependency:tree`：检查运行时依赖边界。

在行为代码的 RED 阶段允许聚焦测试按预期失败；完成增量前必须转为通过。每个阶段检查点运行完整 `verify`，代码未变化时不要重复执行同一命令。

## Java 编码规范

- 四空格缩进；包名全小写；类型使用 `PascalCase`；方法与字段使用 `camelCase`；协议 JSON 属性保持规范中的 `camelCase`。
- 根包为 `io.github.cooltheworld.rcs.protocol.vda5050.v3`，按 `model`、`codec`、`validation`、`topic`、`extension`、`snapshot`、`fleetcontrol`、`mobilerobot` 分包。
- 标准协议 JSON 对象使用不可变 `final class` 与手写 Builder，不提供 setter、全字段公共构造器或 Lombok。
- 稳定的小型非数值值对象可以使用 Java 21 `record`。
- 所有协议整数使用 `Long`，所有协议浮点数使用 `Double`；不得使用数值 primitive、`Integer`、`Float` 或 `BigDecimal` 代替协议字段。
- 可选字段用 `null` 表示缺失，不使用 `Optional<T>` 作为字段或 accessor；可选集合的 `null` 与空集合必须保持不同语义。
- 标准字段不得使用 `Map<String, Object>`、`JSONObject`、`JsonObject` 或通用 JSON 节点代替。未知字段只能封装在不可变 `ExtensionFields` 中，并且公共 API 不提供动态业务读取。
- 公共 Event、Effect 和状态机接口按角色分离；禁止公开万能泛型状态机。
- 实现细节放在 `.internal` 包，调用方不得依赖。

风格示例：

```java
public final class Connection {
    private final Long headerId;
    private final ProtocolTimestamp timestamp;
    private final ConnectionState connectionState;

    private Connection(Builder builder) {
        this.headerId = Objects.requireNonNull(builder.headerId, "headerId");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "timestamp");
        this.connectionState = Objects.requireNonNull(
            builder.connectionState,
            "connectionState"
        );
    }

    public static Builder builder() {
        return new Builder();
    }
}
```

## 协议与状态机边界

- 核心库负责全部协议业务：强类型模型、Codec、Schema/语义校验、Topic 元数据、双角色状态机、协议消息生成、状态快照 Codec 和 Effect Codec。
- 核心库不得连接 Spring、MQTT、Redis、数据库或机器人设备；外部环境只能通过强类型 Fact、Event 和 Effect 边界交互。
- 状态机是纯模型：`当前状态 + 输入事件 -> 新状态 + Effect 列表`。不得读取隐式时钟或执行外部 I/O；事件显式携带时间。
- Fleet Control 与 Mobile Robot 使用独立 State/Event/Effect/Transition。一个实例只能选择一个 `(protocolType, role)`，不存在 `BOTH`。
- 正常入站 Event 只接受 `ValidatedMessage<T>`；前三层校验失败通过 `RejectedInboundMessage` 进入状态机；普通协议错误不用异常表达。
- 状态按 Robot Identity 聚合。相同角色、Robot Identity 与协议版本内串行转换，不同机器人可以并行。
- State 与所有 Effect 必须原子提交；Outbox 至少一次交付；Adapter 使用确定性 `EffectId` 幂等，不承诺跨 Redis、MQTT 和设备的 exactly-once。
- 快照与 Effect 使用核心拥有的版本化确定性 UTF-8 JSON Codec；禁止 Java 原生序列化和任意类多态。版本不兼容直接失败，不迁移旧快照。
- 缺失历史快照时进入 `RECOVERING`，不得假定机器人空闲。

## JSON、兼容与安全规则

- 解码采用“宽容解析、独立严格校验、未知扩展透明保存”；可解析不等于可以执行。
- 首版只正式支持 VDA 5050 3.0.0。未知 3.x 可保留数据，但必须报告 `UNSUPPORTED_PROTOCOL_VERSION`，不能进入正常状态转换。
- Validator 分四层：JSON 语法/基础类型、Draft 2020-12 Schema、上下文无关协议语义、结合 Protocol Session 的状态机语义。
- `date-time` 启用标准 Format Assertion；`ProtocolTimestamp` 额外要求 UTC、三位毫秒和 `Z` 后缀。
- `uint32` 绑定为 `Long` 后校验闭区间 `[0, 4294967295]`，不依赖自定义 Schema Format。
- MQTT payload 视为不可信输入。完整建模前执行默认上限：8 MiB payload、深度 64、字符串 256 Ki 字符、字段名 256 字符、数值文本 128 字符、数组 10000、对象属性 1024、Token 1000000。
- Factsheet Protocol Limits 只能收紧部署上限，不能放宽。禁止无界队列；快照消息可以保留最新值，控制消息不得静默丢弃。
- 禁止 Jackson Default Typing。日志不得记录完整不可信 payload、Extension 值、动作参数、下载链接或凭据。

## 测试策略与完成门槛

- 行为实现严格使用测试驱动：先写失败测试，确认失败原因正确，再写最小实现并重构。
- 测试类位于 `src/test/java` 并命名为 `*Test`；Fixture 包含有效、无效与边界 JSON。
- 主要测试为无 I/O 的小型单元测试；使用真实核心实现优先于 Mock。
- 八个 Topic 都必须具有模型往返、Schema 校验、语义校验和未知字段保留测试。
- Fleet Control 与 Mobile Robot 必须具有不依赖 Spring、MQTT、Redis 的直接跨角色对话测试。
- 属性测试覆盖 Sequence、Base/Horizon、uint32 回绕、确定性重放和状态机不变量。
- 核心代码行覆盖率不低于 90%，分支覆盖率不低于 85%；每个已识别的强制规范分支必须有场景测试。
- 不得跳过、禁用或删除失败测试来通过构建。未满足全部一致性门槛时只能发布里程碑版本，不能标记稳定 `1.0.0`。

## 文档与规格维护

- 本工作区中新生成或更新的说明文档、规格、ADR、计划、任务清单和仓库指南必须使用中文正文。
- Maven 坐标、Java 标识符、协议正式术语、代码、命令、路径和引用标题可以保留原文；首次出现时尽量给出中文含义。
- Markdown 标题应描述清晰，段落之间保留空行，链接使用相对路径，相关图像放在对应 `assets/`。
- 规格是活文档。发现范围、模型或架构变化时，先更新 `rcs-protocol-spec`、适用 ADR 和 `tasks/vda5050-java/`，再修改代码。
- 每条实现或新发现的强制协议规则都要更新一致性追踪清单。
- `rcs-protocol-java/README.md` 持续维护 Maven 制品版本与协议版本兼容矩阵；两种版本独立演进。

## 操作边界

### 始终执行

- 修改前阅读相关规格、ADR、任务与规范正文。
- 保留用户已有改动，限制改动范围，使用 `rg` 搜索，使用 `apply_patch` 编辑文本文件。
- 每个行为增量先写测试，并在完成时运行聚焦测试；阶段检查点运行完整 `verify`。
- 保持公共模型强类型、状态机纯度、核心无基础设施依赖以及文档中文规则。
- 提交前检查 diff、秘密信息、生成文件和无关格式化修改。

### 需要先询问

- 改变已确认的公共 API、Maven 坐标、协议/角色单选规则、持久化格式或版本兼容承诺。
- 添加已确认依赖集合以外的生产依赖，或者改变 Spring Boot/Jackson 主版本。
- 修改 VDA 5050 上游参考仓库、扩大到 Starter 实现、增加 JDBC/其他数据库或事件溯源。
- 删除、迁移或清空用户数据，执行破坏性 Git 操作，或者改变发布版本/标签。

### 永不执行

- 不用 Map、通用 JSON 对象或字符串分派代替标准协议 Java 字段。
- 不让 Starter 承载协议业务、自动消费协议消息或自动驱动核心状态机。
- 不在一个实例中同时启用 Fleet Control 与 Mobile Robot，或同时启用多个协议。
- 不在 Redis 显式模式失败时静默降级到内存，也不自动迁移不兼容快照。
- 不让状态机访问系统时钟、MQTT、Spring、Redis、数据库或设备 I/O。
- 不提交秘密信息、IDE 工作区、`target/` 或其他生成构建输出。
- 不未经明确授权执行 `git reset --hard`、覆盖用户改动或修改上游发布语义。

## 提交与 Pull Request

保持提交小而原子，一个提交只处理一个可验证增量。提交主题使用简短祈使句，例如 `添加 connection 模型校验` 或 `修复 headerId 回绕`。不要把格式化、重构和新行为混在一个提交中。

Pull Request 必须说明受影响的协议及版本、行为变化、对应规格/ADR/一致性清单条目、验证命令和结果，并链接相关问题。视觉资源变化需附更新后的图示。对 VDA 上游检出的贡献应提交到其 `development` 分支；`main` 表示最新发布版本。

## 成功标准

- `rcs-protocol-spec/tasks/vda5050-java/plan.md` 和 `todo.md` 中对应任务的验收标准全部满足。
- 使用 JDK 21 在干净检出目录执行 `.\mvnw.cmd verify` 成功。
- 八个 Topic、双角色状态机、版本化 State/Effect Codec、安全上限和跨角色一致性测试完整。
- 生成 jar 包含正确 Schema、许可证与 `Automatic-Module-Name`，依赖图符合核心边界。
- README 兼容矩阵准确，所有项目文档使用中文正文，且在满足发布门槛前不提前宣称 `1.0.0`。
