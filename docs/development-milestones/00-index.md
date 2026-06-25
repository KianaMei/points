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

| 里程碑 | 文件 | 放行目标 |
| --- | --- | --- |
| 规则 | `01-superpowers-execution-rules.md` | 所有里程碑执行前必读 |
| M0 | `M0-engineering-foundation.md` | `clubpoints` 空模块被 `yudao-server` 扫描，底座能轻量验证 |
| M1 | `M1-database-and-seed.md` | 正式 schema、seed、测试 DDL 一致 |
| M2 | `M2-permission-crosscutting.md` | 权限、范围、审计、附件、通知横切能力可复用 |
| M3 | `M3-rule-config.md` | 规则版本和规则项可发布、停用、读取 |
| M4 | `M4-ledger.md` | 积分流水、冻结、余额缓存形成账本脊柱 |
| M5 | `M5-club-member-leader.md` | 俱乐部、成员、负责人闭环可用 |
| M6 | `M6-activity-registration-attendance.md` | 活动、报名、签到签退、特殊缺席闭环可用 |
| M7 | `M7-activity-settlement.md` | 活动积分和缺席扣分可幂等结算 |
| M8 | `M8-contribution-violation.md` | 非签到积分、违规扣分、弄虚作假闭环可用 |
| M9 | `M9-redemption.md` | 兑换资格、冻结、库存、审核闭环可用 |
| M10 | `M10-annual-dispute-budget.md` | 异议、年度清零、排名、激励、预算闭环可用 |
| M11 | `M11-report-job-frontend.md` | 报表、任务监控、通知、前端页面收口 |
| M12 | `M12-hardening-acceptance.md` | 并发、权限、回归、演示验收完成 |

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

当前最近入口是 M0：

- `ruoyi-vue-pro-github/yudao-module-clubpoints` 还不存在。
- `sql/mysql/club-points-schema.sql` 还不存在。
- `sql/mysql/club-points-seed.sql` 还不存在。
- 根 POM 和 `yudao-server/pom.xml` 还没有接入 `yudao-module-clubpoints`。
