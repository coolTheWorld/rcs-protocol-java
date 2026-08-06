# rcs-protocol

移动机器人调度协议的 Java 实现，规划支持 VDA 5050 和 GB/T 47864。项目使用 JDK 21 与 Maven，并按协议发布独立 jar。

## 版本兼容性

| Maven 制品 | Maven 版本 | 协议版本 | 状态 |
|---|---|---|---|
| `io.github.cooltheworld:rcs-protocol-vda5050` | `0.1.0-SNAPSHOT` | VDA 5050 `3.0.0` | 设计中，尚未发布 |

Maven 制品版本与协议版本独立演进。只有达到项目规范定义的一致性门槛后，制品才会发布首个稳定版本 `1.0.0`。新增或移除协议版本支持时，必须同步更新本表。

## 项目文档

- [VDA 5050 Java 实现规格](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/vda5050-java-implementation.md)
- [实施计划](./tasks/plan.md)
- [任务清单](./tasks/todo.md)
- [当前进度](./tasks/progress.md)
