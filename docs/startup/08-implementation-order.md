# 实现顺序

## 总原则

不能按页面一页页堆。

正确顺序：

```text
源码地基
-> 数据库和 seed
-> 权限范围 / 审计 / 附件 / 通知
-> 规则版本
-> 积分账本
-> 俱乐部成员
-> 活动报名签到
-> 自动结算
-> 非签到积分
-> 兑换
-> 年度 / 预算 / 异议
-> 报表 / 任务 / 前端收口
-> 验收硬化
-> 可用性 / 前后端契约硬化
```

## 里程碑

- M0：芋道地基、空模块、最小运行回归。
- M1：数据库 schema、seed、测试 DDL。
- M2：权限菜单、范围校验、审计、附件、通知。
- M3：规则版本和规则项后台。
- M4：账本、冻结、余额推导、来源统计。
- M5：俱乐部、成员、负责人。
- M6：活动、报名、签到签退、特殊缺席。
- M7：活动自动结算、无故缺席、月度累计缺席。
- M8：非签到积分、违规扣分、弄虚作假。
- M9：兑换批次、资格、礼品、冻结、审核。
- M10：异议、年度清零、排名、激励、预算。
- M11：报表、审计、任务监控、前端工作台。
- M12：测试矩阵、性能、MVP 验收。
- M13：可用性、前后端契约、用户语言、三角色 E2E 硬化。

## 当前硬门禁

- `club-points-database-design.md` 是数据库单一输入。
- `club-points-frontend-page-design.md` 是前端页面单一输入。
- `clubpoints` 空模块必须先被 `yudao-server` 扫描。
- SQL 正式脚本和测试 DDL 必须一致。

## 详细执行清单

- 总索引：`docs/development-milestones/00-index.md`
- Superpowers 执行规则：`docs/development-milestones/01-superpowers-execution-rules.md`
- M0：`docs/development-milestones/M0-engineering-foundation.md`
- M1：`docs/development-milestones/M1-database-and-seed.md`
- M2：`docs/development-milestones/M2-permission-crosscutting.md`
- M3：`docs/development-milestones/M3-rule-config.md`
- M4：`docs/development-milestones/M4-ledger.md`
- M5：`docs/development-milestones/M5-club-member-leader.md`
- M6：`docs/development-milestones/M6-activity-registration-attendance.md`
- M7：`docs/development-milestones/M7-activity-settlement.md`
- M8：`docs/development-milestones/M8-contribution-violation.md`
- M9：`docs/development-milestones/M9-redemption.md`
- M10：`docs/development-milestones/M10-annual-dispute-budget.md`
- M11：`docs/development-milestones/M11-report-job-frontend.md`
- M12：`docs/development-milestones/M12-hardening-acceptance.md`
- M13：`docs/development-milestones/M13-usability-contract-hardening.md`

## 禁止倒序

- 不要先写活动结算再写账本。
- 不要先写兑换再写冻结和库存锁。
- 不要先写页面再补接口和权限。
- 不要先写业务逻辑再补唯一键。
- 不要用 Redis 幂等替代数据库唯一约束。
