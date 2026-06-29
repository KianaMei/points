# Superpowers 里程碑执行规则

## 适用范围

本规则适用于 `docs/development-milestones/M0-*.md` 到 `M13-*.md`。

这些文件不是聊天备忘录，而是执行入口。执行任何里程碑前，必须先加载当前里程碑文件，并按 Superpowers 工作流拆成可验证任务。

## 必用技能

- 开始任务前使用 `using-superpowers` 判断需要的技能。
- 编写或细化计划时使用 `writing-plans`。
- 实现代码行为时使用 `test-driven-development`。
- 执行已有计划时使用 `subagent-driven-development` 或 `executing-plans`。
- 完成前使用 `verification-before-completion`。

## 任务格式

每个实际开发任务必须补齐这些信息，缺一项就不能开写代码：

```markdown
### Task Mx.y: 任务名

**Files:**
- Create: `精确文件路径`
- Modify: `精确文件路径`
- Test: `精确测试文件路径`

**Interfaces:**
- Consumes: `上游接口、表、Service、Mapper、VO、权限码`
- Produces: `下游依赖的方法签名、表结构、VO、权限码、seed`

- [ ] RED: 写失败测试或失败验证
- [ ] Verify RED: 运行命令，确认失败原因正确
- [ ] GREEN: 写最小实现
- [ ] Verify GREEN: 运行命令，确认通过
- [ ] REFACTOR: 只在绿色后清理命名、重复、结构
- [ ] Checkpoint: 列出变更文件和验证证据，不提交 git
```

## TDD 规则

Java Service、Controller、Mapper 查询、状态机、权限、审计、幂等、并发行为必须 TDD：

```text
RED -> Verify RED -> GREEN -> Verify GREEN -> REFACTOR
```

禁止先写实现再补测试。测试如果第一次就通过，说明测试无效，必须改测试。

## SQL 和配置例外

DDL、seed、POM、端口配置不适合纯单元 TDD，但不能无验证。必须使用对应失败验证替代 RED：

- SQL：先写导入或约束冲突验证，再补 DDL。
- seed：先写菜单、权限、字典查询验证，再补 seed。
- POM：先跑模块识别或轻量编译失败，再补依赖。
- 前端配置：先验证旧端口或旧入口行为，再改配置并验证新入口。

## 命令规则

- 后端轻量编译优先：`mvn -pl yudao-server -am -DskipTests -Dflatten.skip=true compile`。
- 模块测试优先：`mvn -pl yudao-module-clubpoints -Dtest=测试类名 test`。
- 前端入口验证优先：`Invoke-WebRequest http://127.0.0.1:8889`。
- 不跑 full build，除非用户明确要求。
- 不提交 git，除非用户明确要求。

## Checkpoint 规则

Superpowers 默认计划里常见 `Commit`，本项目不使用。统一改为 `Checkpoint`：

- 列出变更文件。
- 列出测试或验证命令。
- 列出通过、失败、跳过原因。
- 不执行 `git add`。
- 不执行 `git commit`。
- 不添加 co-author。

## 阻塞规则

遇到以下情况必须停下：

- 当前任务没有明确测试或验证方式。
- 任务需要越过当前里程碑边界。
- 发现数据库设计、API 设计、前端页面设计冲突。
- 发现需要跑 full build 才能判断但用户没授权。
- 发现已有用户改动和当前修改直接冲突。
