# M12 硬化和验收 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 验证系统在并发、幂等、权限、年度、兑换、审计、前端回归和 MVP 演示下可靠。

**Architecture:** M12 不新增核心业务能力，集中做约束复查、失败路径测试、端到端验证、演示数据和文档收口。任何验收失败都回到对应里程碑修正。

**Tech Stack:** Maven 测试、接口验证、Playwright 或手工页面验证、MySQL 约束检查、Docker MySQL/Redis、Vite `8889`。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- M12 不允许用手工改库绕过业务流程。
- MVP 演示必须从页面或 API 完整走通。
- 权限、并发、年度、审计失败不能降级为“已知问题”后放行。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Modify: `docs/club-points-api-design.md`
- Modify: `docs/club-points-database-design.md`
- Modify: `docs/club-points-frontend-page-design.md`
- Modify: `docs/club-points-development-plan.md`
- Reference: `ruoyi-vue-pro-github/sql/mysql/club-points-schema.sql`
- Reference: `ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql`
- Reference: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/`
- Reference: `ruoyi-vue-pro-github/yudao-ui/yudao-ui-admin-vue3/`

**Interfaces:**

- Consumes: M0-M11 全部后端、前端、SQL、seed、测试和文档产物。
- Produces: MVP 验收证据、回归测试结果、已知风险清单、最终文档一致性记录。

**Verification:**

- Run: 权限矩阵接口验证。
- Expected: 员工、负责人、管理员越权场景全部按预期失败。
- Run: 幂等和并发测试。
- Expected: 发分、结算、兑换、年度清零不重复。
- Run: MVP 演示脚本。
- Expected: 活动发分 -> 查账 -> 兑换 -> 排名 -> 清零完整走通，无手工改库。

## 目标

验证系统不是“页面能点”，而是在并发、幂等、权限、年度、兑换、审计、回归场景下可靠。

## 前置条件

- M11 已放行。
- 后端、前端、MySQL、Redis 均可运行。
- 测试数据可重置。

## 任务 M12.1 数据库约束复查

- [x] 复查所有 `club_points_*` 主键。
- [x] 复查所有幂等唯一键。
- [x] 复查账户缓存唯一键。
- [x] 复查库存锁唯一键。
- [x] 复查年度清零唯一键。
- [x] 复查逻辑删除和物理删除策略。
- [x] 复查正式 schema 和测试 DDL 一致性。

验收：

- [x] 没有靠 Redis 替代数据库事实约束。
- [x] 没有正式表字段缺失测试覆盖。

证据：

- RED：`Test-Path ruoyi-vue-pro-github\yudao-module-clubpoints\src\test\java\cn\iocoder\yudao\module\clubpoints\hardening\ClubPointSchemaHardeningTest.java` 返回 `False`，确认 M12.1 缺少可重复硬化测试。
- GREEN：新增 `ClubPointSchemaHardeningTest`，解析正式 MySQL schema 和 H2 测试 DDL，断言正式 schema 为 34 张 `club_points_*` 表，逐表校验主键、字段、`deleted` 字段和全部预期唯一键；H2 DDL 仅过滤比较 `club_points_*`，不把 `system_notify_message` 辅助表误判为 clubpoints 正式 schema 漂移。
- Verify：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointSchemaHardeningTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointSchemaHardeningTest` 运行 2 个测试，失败 0，错误 0。
- Quality：禁用元数据扫描无命中；新增 hardening 测试范围内租户字段、租户基类和 Redis 关键字扫描无命中。

## 任务 M12.2 权限矩阵复查

- [x] 员工访问本人数据成功。
- [x] 员工访问他人数据失败。
- [x] 负责人访问负责俱乐部成功。
- [x] 负责人访问其他俱乐部失败。
- [x] 负责人访问全局兑换审核失败。
- [x] 负责人访问报表导出失败。
- [x] 管理员访问全局功能成功。

验收：

- [x] 每类越权场景有接口验证。
- [x] 后端拦截独立于前端按钮隐藏。

证据：

- RED：`Test-Path ruoyi-vue-pro-github\yudao-module-clubpoints\src\test\java\cn\iocoder\yudao\module\clubpoints\hardening\ClubPointPermissionMatrixHardeningTest.java` 返回 `False`，确认 M12.2 缺少集中权限矩阵硬化测试。
- GREEN：新增 `ClubPointPermissionMatrixHardeningTest`，用真实账本 Controller、Service 和 Mapper 验证员工只能取本人积分数据、负责人只能取负责俱乐部发放来源的流水与成员摘要、负责人访问其他俱乐部被 `CLUB_SCOPE_DENIED` 拒绝、管理员账本查询可见全局账户与流水。
- GREEN：同一测试通过反射校验兑换审核和报表导出接口的后端 `@PreAuthorize`，并解析 seed 确认负责人角色未被授予 `clubpoints:redemption:review` 与 `clubpoints:report:export`。
- Verify：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointPermissionMatrixHardeningTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointPermissionMatrixHardeningTest` 运行 2 个测试，失败 0，错误 0。

## 任务 M12.3 幂等和并发测试

- [x] 活动重复结算不重复发分。
- [x] 活动并发结算不重复发分。
- [x] 月度缺席重复统计不重复扣分。
- [x] 非签到材料重复审核不重复发分。
- [x] 管理员代录重复提交不重复发分。
- [x] 兑换重复提交不重复冻结。
- [x] 兑换并发申请不超兑。
- [x] 兑换重复审核不重复扣分。
- [x] 年度清零重复执行不重复扣分。

验收：

- [x] 高风险并发场景全部通过。
- [x] 数据库唯一键冲突处理符合预期。

证据：

- 覆盖复核：活动重复结算、并发结算和月度累计缺席扣分由 `ClubPointActivitySettlementServiceImplTest` 覆盖；非签到材料重复审核和管理员代录幂等由 `ClubPointContributionServiceImplTest` 覆盖；兑换重复提交和并发申请不超兑由 `ClubPointRedemptionApplicationServiceImplTest`、`ClubPointRedemptionGiftServiceImplTest` 覆盖；兑换重复审核不重复扣分由 `ClubPointRedemptionReviewServiceImplTest` 覆盖；年度清零重复执行不重复扣分由 `ClubPointAnnualClearingServiceImplTest` 覆盖；账本层数据库唯一键冲突和幂等复用由 `ClubPointLedgerServiceImplTest` 覆盖。
- Verify：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivitySettlementServiceImplTest,ClubPointContributionServiceImplTest,ClubPointRedemptionApplicationServiceImplTest,ClubPointRedemptionGiftServiceImplTest,ClubPointRedemptionReviewServiceImplTest,ClubPointAnnualClearingServiceImplTest,ClubPointLedgerServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；7 个服务测试类合计运行 54 个测试，失败 0，错误 0。
- 边界：M12.3 仅做硬化复核和组合验证，不新增生产代码；库存、流水、冻结、年度清零仍由数据库唯一键、条件更新和事务兜底，不使用缓存替代事实约束。

## 任务 M12.4 事务边界复查

- [x] 强审计失败时业务回滚。
- [x] 通知失败时业务不回滚。
- [x] 流水写入失败时账户缓存不更新。
- [x] 账户缓存更新失败时流水不提交。
- [x] 冻结失败时兑换申请不创建。
- [x] 锁库存失败时冻结释放或事务回滚。
- [x] 审核通过失败时冻结、库存、流水一致。

验收：

- [x] 没有半截业务状态。
- [x] 异常路径数据一致。

证据：

- 覆盖复核：`ClubAuditServiceImplTest` 覆盖强审计同事务失败回滚；`ClubNotifyServiceImplTest` 覆盖通知失败不回滚业务事务；`ClubPointLedgerServiceImplTest` 和 `ClubPointLedgerAdjustmentServiceImplTest` 覆盖流水与账户缓存同事务、调整审计失败回滚；`ClubPointFreezeServiceImplTest` 覆盖冻结、释放和冻结转扣减的账户缓存一致性。
- 覆盖复核：`ClubPointContributionServiceImplTest` 覆盖材料审核、管理员代录、违规扣分、弄虚作假处理的强审计失败回滚和通知失败不回滚；`ClubPointRedemptionApplicationServiceImplTest` 覆盖可用积分不足、库存不足和并发库存不足时申请、冻结、库存锁回滚；`ClubPointRedemptionReviewServiceImplTest` 覆盖兑换审核通过审计失败回滚、通知失败不回滚；`ClubPointRedemptionBatchServiceImplTest` 覆盖兑换规则审计失败回滚；`ClubPointAnnualClearingServiceImplTest` 覆盖年度清零通过账本服务同事务扣减账户。
- Verify：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubAuditServiceImplTest,ClubNotifyServiceImplTest,ClubPointLedgerServiceImplTest,ClubPointLedgerAdjustmentServiceImplTest,ClubPointFreezeServiceImplTest,ClubPointContributionServiceImplTest,ClubPointRedemptionApplicationServiceImplTest,ClubPointRedemptionReviewServiceImplTest,ClubPointRedemptionBatchServiceImplTest,ClubPointAnnualClearingServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；10 个服务测试类合计运行 70 个测试，失败 0，错误 0。
- 边界：M12.4 仅做异常路径硬化复核和组合验证，不新增生产代码；强审计失败仍作为业务回滚门禁，通知失败仍按非阻塞处理，未把任何异常路径降级为已知问题。

## 任务 M12.5 年度和跨年测试

- [ ] 1 月 1 日年度清零。
- [ ] 清零只清可用积分。
- [ ] 冻结积分不清零。
- [ ] 跨年兑换拒绝释放回账户。
- [ ] 释放回账户不追加过期清零。
- [ ] 年度排名不受兑换扣分影响。
- [ ] 年度清零后历史流水仍可查。

验收：

- [ ] 年度口径和文档一致。
- [ ] 跨年冻结口径和测试一致。

## 任务 M12.6 前端回归

- [ ] 员工活动报名签到流程。
- [ ] 员工兑换申请流程。
- [ ] 员工异议提交流程。
- [ ] 负责人活动管理流程。
- [ ] 负责人非签到材料提交流程。
- [ ] 管理员审核活动流程。
- [ ] 管理员审核非签到流程。
- [ ] 管理员审核兑换流程。
- [ ] 管理员年度清零流程。
- [ ] 管理员报表导出流程。

验收：

- [ ] 三类角色主路径可完成。
- [ ] 页面错误提示可理解。
- [ ] 端口仍为 `8889`。

## 任务 M12.7 MVP 演示脚本

- [ ] 管理员创建规则版本并发布。
- [ ] 管理员创建俱乐部和负责人。
- [ ] 负责人创建活动并提交审核。
- [ ] 管理员审核活动通过。
- [ ] 员工报名活动。
- [ ] 员工签到签退。
- [ ] 管理员或任务触发活动结算。
- [ ] 员工查看积分到账。
- [ ] 管理员创建兑换批次和礼品。
- [ ] 员工提交兑换申请。
- [ ] 管理员审核兑换通过。
- [ ] 员工查看兑换扣分。
- [ ] 管理员生成年度排名。
- [ ] 管理员执行年度清零。
- [ ] 管理员查看审计和报表。

验收：

- [ ] 完整链路无手工改库。
- [ ] 演示数据可重复准备。

## 任务 M12.8 文档收口

- [ ] 同步 API 文档实际字段。
- [ ] 同步数据库设计和最终 SQL。
- [ ] 同步前端页面设计和实际页面。
- [ ] 同步开发计划里已完成状态。
- [ ] 记录未完成项和不进入 MVP 的项。
- [ ] 记录已知风险和后续阶段。

验收：

- [ ] 文档不再有“待数据库设计解决”这类过期表述。
- [ ] 文档不包含无意义索引定位套话。

## M12 放行标准

- [ ] MVP 演示脚本通过。
- [ ] 权限矩阵通过。
- [ ] 幂等并发测试通过。
- [ ] 年度跨年测试通过。
- [ ] 强审计测试通过。
- [ ] 前端三类角色主路径通过。
- [ ] 文档与代码一致。

## M12 不通过时禁止

- [ ] 禁止说项目完成。
- [ ] 禁止上线或交付验收。
