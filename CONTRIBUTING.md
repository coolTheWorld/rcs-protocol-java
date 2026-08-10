# 贡献指南

本仓库接受面向 `rcs-protocol-java` 与独立协议模块的代码和文档贡献。协议事实、实现边界和任务优先级由 [`rcs-protocol-spec`](https://github.com/coolTheWorld/rcs-protocol-spec) 统一维护；开始实现前应先确认规格和任务状态。

## 开始之前

- 安装 JDK 21，并确认 `java -version` 指向兼容运行时。
- 使用仓库提交的 Maven Wrapper，不依赖本机 Maven 版本。
- 阅读 [开发规范](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/DEVELOPMENT.md)、[完成定义](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/DEFINITION-OF-DONE.md) 和 [当前任务](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/tasks/vda5050-java/todo.md)。
- 检查工作树，保留与本次贡献无关的已有修改。

```powershell
git status --short
.\mvnw.cmd validate
```

## 开发流程

1. 在 Spec 仓库确认或补充需求、架构决策与一致性条目。
2. 将工作拆成一个可独立验证的小任务，并确认依赖已经满足。
3. 行为变更先写失败测试，确认失败原因正确后实现最小改动。
4. 运行聚焦测试；阶段检查点运行完整 `verify`。
5. 同步 README、API 使用说明、Changelog、规格与进度记录。
6. 检查 diff、生成文件、秘密信息和无关格式化修改后再提交。

常用命令：

```powershell
.\mvnw.cmd -pl rcs-protocol-vda5050 -Dtest=<测试类> test
.\mvnw.cmd -pl rcs-protocol-vda5050 test
.\mvnw.cmd verify
.\mvnw.cmd -pl rcs-protocol-vda5050 dependency:tree
```

## 代码与测试约定

- 生产代码与测试分别位于 `src/main/java` 和 `src/test/java`，测试包必须镜像生产包。
- 标准协议对象保持不可变和强类型；未知字段仅由 `ExtensionFields` 保存。
- Fleet Control 与 Mobile Robot 保持独立的 State、Event、Effect 和 Transition。
- 核心模块不连接 Spring、MQTT、Redis、数据库或设备 I/O。
- 不通过删除、禁用或跳过失败测试使构建通过。
- 新增强制协议分支时，同时补充一致性追踪和测试证据。

更完整的编码、安全、文档和操作边界见 Spec 仓库的[开发规范](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/DEVELOPMENT.md)。

## 提交与 Pull Request

提交应小而原子，主题使用简短祈使句，例如 `添加 connection 模型校验`。Pull Request 至少说明：

- 受影响的协议和版本；
- 行为或公共 API 变化；
- 对应规格、ADR、任务和一致性条目；
- 执行过的验证命令及结果；
- 已知限制、兼容性影响与后续任务。

不要把无关重构、格式化和新行为放在同一个提交中。

## 许可证状态

项目根许可证尚未由维护者确认。在许可证明确前，贡献和制品不得被描述为已经获得某个开源许可证授权；发布前必须完成该决定并同步仓库元数据。
