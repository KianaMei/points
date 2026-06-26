# M7 活动自动结算 Implementation Plan

**Status:** `[~]` M7.1-M7.5 已完成并有 RED/GREEN 证据；当前入口是 M7.6 管理员接口。

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 根据活动、报名、签到签退、特殊缺席、规则配置生成活动积分和缺席扣分流水，并保证重跑不重复。

**Architecture:** 结算服务读取 M6 事实和 M3 规则，通过 M4 LedgerService 写流水。Job Handler 只触发 Service，不写业务逻辑；结算结果和任务运行记录分开保存。

**Tech Stack:** Spring 事务、MyBatis、RuoYi Job、LedgerService、数据库唯一键、JUnit。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 活动积分和缺席扣分必须用规则配置。
- 结算重跑和并发结算必须幂等。
- Job 不能直接写业务逻辑。
- Java 行为必须 TDD。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/settlement/ClubPointActivitySettlementService.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/job/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/settlement/`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ErrorCodeConstants.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/settlement/`

**Interfaces:**

- Consumes: M6 活动事实、M3 规则项、M4 LedgerService、M2 强审计、`club_points_job_run`。
- Produces: 活动积分流水、缺席扣分流水、结算运行记录、结算 Job Handler。

**Verification:**

- Run: 活动结算单测。
- Expected: 正常签到发分、无故缺席扣分、特殊缺席不扣分。
- Run: 重复结算和并发结算测试。
- Expected: 不重复发分，不重复扣分。

## 目标

根据活动、报名、签到签退、特殊缺席、规则配置生成活动积分和缺席扣分流水，保证重跑不重复。

## 前置条件

- M6 已放行。
- LedgerService 可用。
- 规则读取可用。
- `club_points_activity_settlement_run` 已建表。
- 结算相关唯一键已落库。

## 任务 M7.1 结算模型

- [x] 定义活动结算状态。
- [x] 定义结算运行状态。
- [x] 定义结算来源类型。
- [x] 定义活动积分流水来源类型。
- [x] 定义无故缺席扣分来源类型。
- [x] 定义月度累计缺席扣分来源类型。

验收：

- [x] 结算状态和字典一致。
- [x] 流水来源类型可追溯到活动和用户。

证据：

- RED：新增 `ClubPointActivitySettlementEnumTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivitySettlementEnumTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointActivitySettlementStatusEnum`、`ClubPointSettlementRunStatusEnum`、`ClubPointActivitySettlementTriggerSourceEnum`、`ClubPointActivitySettlementItemTypeEnum` 和 `DictTypeConstants.ACTIVITY_SETTLEMENT_STATUS` 不存在，符合 M7.1 RED 预期。
- GREEN：新增活动结算状态枚举、结算运行状态枚举、结算触发来源枚举和结算项类型枚举；结算项类型统一使用 `ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT`，通过活动 ID、用户 ID、业务月份和结算项生成稳定幂等键。
- Seed 同步：`club-points-seed.sql` 新增 `club_points_activity_settlement_status` 字典类型和值，状态为 `1` 待结算、`2` 结算中、`3` 已结算、`4` 结算失败、`5` 人工处理；`DictTypeConstants` 同步新增常量。
- 边界：M7.1 只定义模型，不创建 SettlementService，不写 Job，不生成积分流水；活动结算、单次无故缺席和月度累计缺席都继续使用账本唯一事实源 `club_points_transaction`。
- M7.1 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivitySettlementEnumTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointActivitySettlementEnumTest` 运行 `4` 个测试，失败 `0`，错误 `0`。
- M7 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=DictTypeConstantsTest,ClubPointLedgerEnumTest,ClubPointActivitySettlementEnumTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `13` 个测试，失败 `0`，错误 `0`。

## 任务 M7.2 SettlementService

- [x] 创建 `service/settlement/ClubPointActivitySettlementService.java`。
- [x] 创建 `service/settlement/ClubPointActivitySettlementServiceImpl.java`。
- [x] 读取已结束未结算活动。
- [x] 读取活动报名记录。
- [x] 读取签到签退记录。
- [x] 读取特殊缺席标记。
- [x] 读取活动积分配置版本。
- [x] 计算应得活动积分。
- [x] 计算无故缺席扣分。
- [x] 调用 LedgerService 写流水。
- [x] 写结算运行记录。

验收：

- [x] 活动积分用规则配置，不硬编码。
- [x] 缺席扣分用规则配置，不硬编码。
- [x] 结算结果和运行记录一致。

证据：

- RED：新增 `ClubPointActivitySettlementServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivitySettlementServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement`、`cn.iocoder.yudao.module.clubpoints.dal.mysql.settlement`、`ClubPointActivitySettlementRunReqBO`、`ClubPointActivitySettlementService`、`ClubPointActivitySettlementRunMapper` 和 `ClubPointActivitySettlementServiceImpl` 不存在，符合 M7.2 RED 预期。
- GREEN：新增 `ClubPointActivitySettlementRunDO`、`ClubPointActivitySettlementRunMapper`、`ClubPointActivitySettlementRunReqBO`、`ClubPointActivitySettlementService` 和 `ClubPointActivitySettlementServiceImpl`；活动结算读取 `ENDED` 活动、当前活动积分配置版本、报名、签到、签退和特殊缺席标记，通过 `ClubPointLedgerService.createTransaction(...)` 生成活动基础分、全程额外分和单次无故缺席扣分流水。
- 实现边界：M7.2 不直接写 `club_points_transaction`，不实现月度累计缺席扣分，不写 Job Handler，不写管理员接口；特殊缺席、取消报名和 `noAbsenceDeduct=true` 报名只跳过，不发分不扣分；成功结算后活动状态更新为 `SETTLED(7)`。
- 规则边界：活动基础分和全程额外分使用活动积分配置版本中的 `basePoints`、`fullExtraPoints` 和 `ruleVersionId`；单次无故缺席扣分通过 `ClubPointRuleResolveService.getFixedPoints(ruleVersionId, ABSENCE_SINGLE_DEDUCT)` 读取规则默认分，不硬编码。
- M7.2 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivitySettlementServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointActivitySettlementServiceImplTest` 运行 `1` 个测试，失败 `0`，错误 `0`。
- M7 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivitySettlementEnumTest,ClubPointActivitySettlementServiceImplTest,ClubPointLedgerServiceImplTest,ClubPointActivityServiceImplTest,ClubPointRegistrationServiceImplTest,ClubPointAttendanceServiceImplTest,ClubPointAttendanceCorrectionServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `35` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中。

## 任务 M7.3 结算幂等

- [x] 活动结算运行加 request key。
- [x] 单个用户活动发分生成稳定 idempotency key。
- [x] 单个用户活动缺席扣分生成稳定 idempotency key。
- [x] 月度累计缺席扣分生成稳定 idempotency key。
- [x] 重跑时识别已处理流水。
- [x] 数据库唯一键冲突按已处理返回。

验收：

- [x] 同一活动重复结算不重复发分。
- [x] 同一缺席重复结算不重复扣分。
- [x] 并发结算不会生成重复流水。

证据：

- RED：在 `ClubPointActivitySettlementServiceImplTest` 增加同 `runKey` 重跑、不同 `runKey` 重跑和并发结算测试后，运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivitySettlementServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；`ClubPointActivitySettlementServiceImplTest` 运行 `4` 个测试，失败 `0`，错误 `3`，原因是首次结算后活动状态已变为 `SETTLED(7)`，后续重跑仍按仅允许 `ENDED(6)` 处理并抛 `CLUB_ACTIVITY_STATUS_INVALID`，符合 M7.3 RED 预期。
- GREEN：`ClubPointActivitySettlementServiceImpl` 在入口按 `runKey` 查询既有运行记录并直接返回；活动状态校验允许 `ENDED(6)` 首次结算和 `SETTLED(7)` 重跑；运行记录插入捕获 `DuplicateKeyException` 后按 `runKey` 查询既有记录返回；流水仍统一通过 `ClubPointLedgerService.createTransaction(...)` 的 `idempotencyKey` 幂等复用。
- 实现边界：M7.3 不直接写 `club_points_transaction`，不实现月度累计缺席扣分业务，不写 Job Handler，不写管理员接口；月度累计缺席扣分当前只保留 `ABSENCE_MONTHLY` 的稳定幂等键形态，实际统计和扣分进入 M7.4。
- M7.3 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivitySettlementServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointActivitySettlementServiceImplTest` 运行 `4` 个测试，失败 `0`，错误 `0`。
- M7 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivitySettlementEnumTest,ClubPointActivitySettlementServiceImplTest,ClubPointLedgerServiceImplTest,ClubPointActivityServiceImplTest,ClubPointRegistrationServiceImplTest,ClubPointAttendanceServiceImplTest,ClubPointAttendanceCorrectionServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `38` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中。

## 任务 M7.4 月度累计缺席

- [x] 统计用户当月无故缺席次数。
- [x] 读取月度累计缺席规则。
- [x] 达到阈值后生成扣分流水。
- [x] 同一用户同一年月只扣一次。
- [x] 记录统计快照。

验收：

- [x] 未达到阈值不扣分。
- [x] 达到阈值扣分一次。
- [x] 重算不重复扣。

证据：

- RED：新增 `ClubPointActivitySettlementServiceImplTest#settleActivityShouldDeductMonthlyAbsenceOnceWhenThresholdReached` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivitySettlementServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；`ClubPointActivitySettlementServiceImplTest` 运行 `5` 个测试，失败 `1`，错误 `0`，失败原因是第三次无故缺席后 `ABSENCE_MONTHLY:202607:7201` 流水仍为 `null`，符合 M7.4 RED 预期。
- GREEN：`ClubPointActivityRegistrationMapper` 增加按用户、自然月、活动 `ENDED/SETTLED`、有效报名、非特殊缺席、无签到事实查询当月无故缺席列表；`ClubPointActivitySettlementServiceImpl` 在单次缺席扣分后读取 `ABSENCE_MONTHLY_THRESHOLD.intValue` 和 `ABSENCE_MONTHLY_DEDUCT.defaultPoints`，达到阈值后通过 `ClubPointLedgerService.createTransaction(...)` 写月度累计缺席扣分流水。
- 实现边界：M7.4 不新增表，不直接写 `club_points_transaction`，不写 Job Handler，不写管理员接口；月度累计缺席流水使用全局月度来源，`issuingClubId` 和 `activityId` 为空，避免负责人通过某个俱乐部看到跨俱乐部累计来源；统计快照写入流水 `snapshotJson`，包含 `businessMonth`、`absenceCount`、`threshold`、触发活动和触发报名。
- M7.4 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivitySettlementServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointActivitySettlementServiceImplTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- M7 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivitySettlementEnumTest,ClubPointActivitySettlementServiceImplTest,ClubPointLedgerServiceImplTest,ClubPointActivityServiceImplTest,ClubPointRegistrationServiceImplTest,ClubPointAttendanceServiceImplTest,ClubPointAttendanceCorrectionServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `39` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中。

## 任务 M7.5 Job Handler

- [x] 创建活动结算 Job Handler。
- [x] Job 只调 Service，不写业务逻辑。
- [x] 写 `club_points_job_run` 记录。
- [x] 支持失败重试。
- [x] 失败记录错误摘要。

验收：

- [x] Job 可手动触发。
- [x] Job 重试不重复发分。
- [x] Job 失败能在任务运行记录中追踪。

证据：

- RED：新增 `ClubPointActivitySettlementJobTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivitySettlementJobTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `ClubPointActivitySettlementJobService`、`ClubPointActivitySettlementJobReqBO` 和 `ClubPointActivitySettlementJob` 不存在，符合 M7.5 RED 预期。
- GREEN：新增 `ClubPointActivitySettlementJobReqBO`、`ClubPointActivitySettlementJobService` 和 `ClubPointActivitySettlementJob`；Job Handler 只解析 JSON 参数并调用 Job Service；Job Service 写入 `club_points_job_run`，以 `ACTIVITY_SETTLEMENT_JOB:{runKey}:{retryCount}` 作为任务幂等键，并以 `ACTIVITY_SETTLEMENT_JOB:{runKey}:{retryCount}:{activityId}` 作为结算运行键调用 `settleActivity(...)`。
- 实现边界：M7.5 不新增表，不直接写 `club_points_transaction`，不写管理员接口；结算流水仍只通过 `ClubPointLedgerService.createTransaction(...)` 写入；`club_points_activity_settlement_run.job_run_id` 由结算请求透传并落库，任务失败时 `club_points_job_run` 记录 `RETRYABLE_FAILED(4)`、错误类型、错误摘要和下次重试时间，然后继续向外抛异常给调度框架。
- M7.5 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivitySettlementJobTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointActivitySettlementJobTest` 运行 `3` 个测试，失败 `0`，错误 `0`。
- M7 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivitySettlementEnumTest,ClubPointActivitySettlementServiceImplTest,ClubPointActivitySettlementJobTest,ClubPointLedgerServiceImplTest,ClubPointActivityServiceImplTest,ClubPointRegistrationServiceImplTest,ClubPointAttendanceServiceImplTest,ClubPointAttendanceCorrectionServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `42` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中。

## 任务 M7.6 管理员接口

- [ ] 管理员查看待结算活动。
- [ ] 管理员手动触发结算。
- [ ] 管理员查看结算运行记录。
- [ ] 管理员查看结算明细。
- [ ] 手动结算写强审计。

验收：

- [ ] 手动重跑不重复流水。
- [ ] 非管理员不能触发全局结算。

## 任务 M7.7 测试

- [ ] 测试正常签到发分。
- [ ] 测试未签到缺席扣分。
- [ ] 测试特殊缺席不扣分。
- [ ] 测试重复结算。
- [ ] 测试并发结算。
- [ ] 测试月度累计缺席。
- [ ] 测试余额不足时扣分规则。
- [ ] 测试 Job 失败记录。

验收：

- [ ] 所有结算测试通过。
- [ ] 流水、账户缓存、结算记录一致。

## M7 放行标准

- [ ] 自动结算可用。
- [ ] 手动结算可用。
- [ ] 重跑幂等。
- [ ] 月度缺席扣分可用。
- [ ] 结算记录可追溯。

## M7 不通过时禁止

- [ ] 禁止写活动相关前端收口。
- [ ] 禁止做全链路积分演示。
