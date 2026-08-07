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

需要复用应用现有 Jackson `ObjectMapper` 时，可以显式注册 `Vda5050JacksonModule`。该 Module 只注册协议值类型的线路表示，不修改调用方的 null、未知字段、资源限制或多态配置。

## 项目文档

- [VDA 5050 Java 实现规格](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/vda5050-java-implementation.md)
- [实施计划](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/tasks/vda5050-java/plan.md)
- [任务清单](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/tasks/vda5050-java/todo.md)
- [当前进度](https://github.com/coolTheWorld/rcs-protocol-spec/blob/main/tasks/vda5050-java/progress.md)
