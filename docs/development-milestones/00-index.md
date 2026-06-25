# 开发里程碑执行索引

## Superpowers 入口

本目录按 Superpowers 执行方式维护：

- 共享执行规则：`01-superpowers-execution-rules.md`
- 每个 M 文件都是一个 Implementation Plan 入口。
- 执行具体 M 文件前，必须先读共享执行规则。
- 实际写代码时必须使用 `test-driven-development` 的 RED/GREEN/REFACTOR。
- 执行计划时优先使用 `subagent-driven-development`；不能用子任务时使用 `executing-plans`。
- Superpowers 默认的 commit 步骤在本项目改为 Checkpoint，因为项目禁止未授权提交。

## 用法

每次准备开发时先读本索引，再只读当前里程碑文件。不要一次性读完 M0-M12。

执行规则：

- 一个里程碑没有通过验收，不进入下一个里程碑。
- 一个任务没有测试或验证证据，不算完成。
- 数据库约束、权限、审计、幂等属于硬门禁，不能用前端逻辑或口头约定替代。
- 不跑 full build，除非明确要求。
- 不提交 git，除非明确要求。
- 不添加 co-author 或 AI 元数据。

## 状态标记

| 标记 | 含义 |
| --- | --- |
| `[ ]` | 未开始 |
| `[~]` | 进行中或部分完成 |
| `[x]` | 已完成并有验证证据 |
| `[!]` | 阻塞，必须处理后继续 |

## 里程碑文件

| 状态 | 里程碑 | 文件 | 放行目标 |
| --- | --- | --- | --- |
| `[x]` | 规则 | `01-superpowers-execution-rules.md` | 所有里程碑执行前必读 |
| `[x]` | M0 | `M0-engineering-foundation.md` | `clubpoints` 空模块被 `yudao-server` 扫描，底座能轻量验证 |
| `[x]` | M1 | `M1-database-and-seed.md` | 正式 schema、seed、测试 DDL 一致 |
| `[x]` | M2 | `M2-permission-crosscutting.md` | 权限、范围、审计、附件、通知横切能力可复用 |
| `[~]` | M3 | `M3-rule-config.md` | 规则版本和规则项可发布、停用、读取 |
| `[ ]` | M4 | `M4-ledger.md` | 积分流水、冻结、余额缓存形成账本脊柱 |
| `[ ]` | M5 | `M5-club-member-leader.md` | 俱乐部、成员、负责人闭环可用 |
| `[ ]` | M6 | `M6-activity-registration-attendance.md` | 活动、报名、签到签退、特殊缺席闭环可用 |
| `[ ]` | M7 | `M7-activity-settlement.md` | 活动积分和缺席扣分可幂等结算 |
| `[ ]` | M8 | `M8-contribution-violation.md` | 非签到积分、违规扣分、弄虚作假闭环可用 |
| `[ ]` | M9 | `M9-redemption.md` | 兑换资格、冻结、库存、审核闭环可用 |
| `[ ]` | M10 | `M10-annual-dispute-budget.md` | 异议、年度清零、排名、激励、预算闭环可用 |
| `[ ]` | M11 | `M11-report-job-frontend.md` | 报表、任务监控、通知、前端页面收口 |
| `[ ]` | M12 | `M12-hardening-acceptance.md` | 并发、权限、回归、演示验收完成 |

## 依赖链

```text
M0 空模块
-> M1 表和 seed
-> M2 横切能力
-> M3 规则配置
-> M4 账本
-> M5 俱乐部成员
-> M6 活动报名签到
-> M7 活动结算
-> M8 非签到和扣分
-> M9 兑换
-> M10 年度运营
-> M11 前端和报表收口
-> M12 硬化验收
```

## 每个任务的最低完成标准

一个任务必须同时满足：

- 代码或 SQL 落到指定文件。
- 单元测试、集成测试、接口验证或命令验证至少一种通过。
- 影响文档同步更新。
- 失败路径有明确处理。
- 没有绕过数据库唯一键、后端权限、强审计、事务边界。

## 当前最近入口

当前最近入口是 M3：

- M0 已创建 `ruoyi-vue-pro-github/yudao-module-clubpoints` 空模块。
- M0 已在根 POM 和 `yudao-server/pom.xml` 接入 `yudao-module-clubpoints`。
- M0 已通过 `mvn -pl yudao-server -am -DskipTests "-Dflatten.skip=true" compile` 轻量编译验证。
- M1 已创建 `sql/mysql/club-points-schema.sql` 和 `sql/mysql/club-points-seed.sql`。
- M1 已完成正式 schema 的 34 张 `club_points_*` 表、默认 seed、测试 DDL、clean SQL。
- M1 已通过临时 MySQL 库导入、关键唯一键、权限集合、seed 幂等、H2 测试 DDL、正式/测试字段与唯一键比对验证。
- M2.1 已完成权限码、三端菜单、按钮权限和三类角色建议授权，并通过临时库导入、权限集合、角色边界和幂等验证。
- M2.2 已完成 `DictTypeConstants`、seed 字典补齐、常量/seed 一致性测试、MySQL 导入和幂等验证。
- M2.3 已完成 `ClubScopeService`、成员/负责人 DO 与 Mapper、本人/已加入俱乐部/负责俱乐部/全局范围校验，并通过数据范围单测和 M2 组合验证。
- M2.4 已完成 `ClubAuditService`、审计日志 DO 与 Mapper、审计创建 BO、强审计动作常量和审计失败回滚业务测试，并通过 M2 组合验证。
- M2.5 已完成 `ClubAttachmentService`、附件绑定 DO 与 Mapper、infra 文件复用、附件锁定、删除前校验和软删除，并通过 M2 组合验证。
- M2.6 已完成 `ClubNotifyService`、通知模板常量、活动审核/积分变动/兑换审核/异议回复通知封装、通知失败不回滚业务测试，并通过 M2 组合验证。
- M2.7 已完成员工越权、负责人越权、管理员全局访问、强审计失败回滚、通知失败不回滚、附件锁定不可删除横切测试，并通过 M2 放行组合验证。
- M3.1 已完成规则版本、规则项、发布记录 DO 与 Mapper，并通过 RED/GREEN 映射测试和规则包质量门禁。
- M3.2 已完成规则版本状态枚举、规则项值类型枚举、规则项编码枚举和 5 个规则错误码，并通过 seed 编码一致性、固定分值区间表达和 M3 当前组合测试验证。
- M3.3 已完成规则版本 Service、规则项 Service、发布/停用状态机、当前已发布版本读取、按编码读取规则项、发布和停用强审计，并通过 M3.3 RED/GREEN、替代记录回归 RED/GREEN 和 M3 当前组合测试验证。
- M3.4 已完成管理员规则版本分页、详情、创建、更新、复制、发布、撤回、停用、规则项列表和规则项保存接口，并通过 M3.4 RED/GREEN、值类型校验补强 RED/GREEN、M3 当前组合测试和质量门禁。
- M3 下一步入口是 M3.5 业务读取封装。
