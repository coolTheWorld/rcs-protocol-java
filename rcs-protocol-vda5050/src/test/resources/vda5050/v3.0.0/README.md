# VDA 5050 v3.0.0 测试资源约定

本目录保存 VDA 5050 v3.0.0 的一致性清单和 JSON Fixture。生产代码不得读取这里的测试资源；运行时 Schema 位于 `src/main/resources/vda5050/v3.0.0/`。

## 一致性清单

`conformance/requirements.tsv` 是测试可读取的追踪清单。每行使用稳定 ID `VDA3-<TOPIC>-NNN`，并记录规范来源、Schema 差距、Validator、Transition、Fixture、测试和状态。

- `PLANNED`：尚未实现，四个制品引用列必须为 `-`。
- `PARTIAL`：至少已有一个制品引用，但尚未形成完整验证证据。
- `VERIFIED`：四个制品引用列都必须填写；不适用时使用 `N/A:<原因>`。
- 非 `-` 的制品引用必须包含同一稳定 ID，避免清单与实现失联。

## Fixture 目录

后续 Fixture 使用以下布局：

```text
fixtures/<topic>/<valid|invalid|boundary>/<case>.json
fixtures/<topic>/<valid|invalid|boundary>/<case>.meta.json
```

原始 `.json` 只保存协议 payload，不包装测试元数据。相邻的 `.meta.json` 至少记录 `caseId`、`requirementIds`、场景说明和预期 Issue 代码；`requirementIds` 必须引用一致性清单中的稳定 ID。文件名使用小写 ASCII 与连字符，避免依赖平台相关编码或路径规则。

每个 Topic 最终都要覆盖有效、无效、边界和未知字段保留场景。Fixture 只描述输入与预期，不包含 Java 类名或环境地址，也不得包含凭据、真实下载链接或生产数据。
