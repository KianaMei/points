# M10 异议、年度清零、排名、激励、预算 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成异议处理、年度清零、年度排名、激励建议、预算记录，确保年度清零幂等且排名不受兑换扣分影响。

**Architecture:** 异议不直接改积分，需要变化时走账本撤销或调整。年度清零按用户和年度生成负向流水；排名基于年度获得积分统计，激励和预算是运营记录。

**Tech Stack:** Spring Job、Spring 事务、MyBatis、LedgerService、强审计、JUnit。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 年度清零必须按北京时间 1 月 1 日口径。
- 冻结积分不清零。
- 跨年冻结释放回账户，不追加过期清零。
- 排名不受兑换扣分影响。
- Java 行为必须 TDD。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/dispute/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/annual/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/budget/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/dispute/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/annual/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/budget/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/annual/`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/annual/`

**Interfaces:**

- Consumes: M4 LedgerService、M9 冻结口径、M2 强审计、M1 年度和预算表。
- Produces: 异议处理、年度清零 Job、年度排名、激励建议、预算记录接口。

**Verification:**

- Run: 年度清零单测。
- Expected: 同一年同一用户只清零一次，冻结积分不清零。
- Run: 年度排名测试。
- Expected: 兑换扣分不影响年度获得积分排名。
- Run: 异议调整测试。
- Expected: 积分变化走账本撤销或调整，不直接改余额。

## 目标

完成异议处理、年度清零、年度排名、激励建议、预算记录。年度清零必须幂等，排名不受兑换扣分影响。

## 前置条件

- M9 已放行。
- LedgerService 可用。
- 兑换冻结和跨年释放口径已确定。
- 强审计可用。
- `club_points_job_run` 可用。

## 任务 M10.1 异议 DO 和 Service

- [x] 创建 `ClubPointDisputeDO`。
- [x] 创建对应 Mapper。
- [x] 员工提交异议。
- [x] 员工上传附件。
- [x] 管理员受理异议。
- [x] 管理员驳回异议。
- [x] 管理员处理异议。
- [x] 需要改积分时调用撤销或调整流水。
- [x] 异议本身不直接改积分。
- [x] 处理异议写强审计。

验收：

- [x] 异议状态机可控。
- [x] 异议处理可追溯。
- [x] 积分变化仍走账本。

验证记录：

- RED：新增 `ClubPointDisputeServiceImplTest`，运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointDisputeServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`，失败原因为异议 DO、Mapper、Service、BO、枚举、`BIZ_TYPE_DISPUTE`、`DISPUTE_HANDLE` 缺失。
- GREEN：新增 `ClubPointDisputeDO`、`ClubPointDisputeMapper`、异议状态 / 目标类型 / 关联动作枚举、提交 / 受理 / 处理 BO 和 `ClubPointDisputeService` 实现；同一命令返回 `BUILD SUCCESS`，`ClubPointDisputeServiceImplTest` 运行 `8` 个测试，失败 `0`，错误 `0`。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointDisputeServiceImplTest,ClubPointLedgerAdjustmentServiceImplTest,ClubAttachmentServiceImplTest,ClubAuditServiceImplTest,ClubNotifyServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `29` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中；异议 Service 和测试范围 Redis 模式无命中；异议主代码无直接写流水或账户命中。

实现边界：

- M10.1 不实现 Controller，员工端和管理员端 API 留到 M10.8。
- 异议状态机固定为 `PENDING(1)`、`REPLIED(2)`、`CLOSED(3)`；受理只设置处理人和处理时间，状态仍为 `PENDING`；处理后进入 `REPLIED`；驳回作为无积分动作终态进入 `CLOSED`。
- 异议附件绑定到 `BIZ_TYPE_DISPUTE`；提交、受理、驳回和处理均不直接写 `club_points_transaction` 或积分账户。
- 调整积分走 `ClubPointLedgerService.adjustPoints(...)`，撤销积分走 `ClubPointLedgerService.reverseTransaction(...)`；处理异议自身写 `DISPUTE_HANDLE` 强审计，审计失败回滚异议处理和同事务账本动作，通知失败不回滚业务。

## 任务 M10.2 年度清零模型

- [x] 创建 `ClubPointAnnualClearingRecordDO`。
- [x] 创建对应 Mapper。
- [x] 定义年度清零状态。
- [x] 定义年度清零流水来源类型。
- [x] 定义 `ANNUAL_CLEARING:{year}:{userId}` 幂等键。
- [x] 明确每年 1 月 1 日北京时间执行。
- [x] 跨年冻结兑换拒绝时释放回账户，不追加过期清零。

验收：

- [x] 清零规则有明确时间口径。
- [x] 跨年冻结释放口径固定。

验证记录：

- RED：新增 `ClubPointAnnualClearingModelTest`，运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointAnnualClearingModelTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`，失败原因为年度清零 DO、Mapper 包和类缺失。
- GREEN：新增 `ClubPointAnnualClearingRecordDO`、`ClubPointAnnualClearingRecordMapper`、`ClubPointAnnualClearingConstants`；同一命令返回 `BUILD SUCCESS`，`ClubPointAnnualClearingModelTest` 运行 `2` 个测试，失败 `0`，错误 `0`。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointAnnualClearingModelTest,ClubPointLedgerEnumTest,ClubPointLedgerMapperTest,ClubPointFreezeServiceImplTest,ClubPointRedemptionCancelServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `22` 个测试，失败 `0`，错误 `0`。
- 质量验证：年度清零新增文件范围 `tenant_id|TenantBaseDO` 无命中；精确元数据模式无命中；Redis 模式无命中；年度清零主代码无直接写流水或账户命中。

实现边界：

- 年度清零状态复用已有 `ClubPointAnnualClearingStatusEnum`：`SUCCESS(1)`、`FAILED(2)`、`SKIPPED(3)`。
- 年度清零流水来源类型复用已有 `ClubPointTransactionSourceTypeEnum.ANNUAL_CLEARING(5)`，流水方向固定为扣减，积分分类固定为 `ClubPointCategoryEnum.ANNUAL_CLEARING(60)`。
- 幂等键模型固定为 `ANNUAL_CLEARING:{year}:{userId}`；时间口径固定为 `Asia/Shanghai` 下每年 `1 月 1 日 00:00`。
- 跨年冻结兑换拒绝或取消时继续释放回账户可用分，不追加过期清零流水；该口径由 M9.9 既有跨年释放测试和 M10.2 组合验证固定。
- M10.2 不实现年度清零 Service、Job 或 API，分别留到 M10.3、M10.4 和 M10.8。

## 任务 M10.3 年度清零 Service

- [x] 扫描指定年度用户账户。
- [x] 只清未冻结可用积分。
- [x] 生成年度清零负向流水。
- [x] 写年度清零记录。
- [x] 更新账户缓存。
- [x] 支持单用户重试。
- [x] 支持全量重跑。
- [x] 使用数据库唯一键防重复。

验收：

- [x] 同一年同一用户只清零一次。
- [x] 已冻结积分不被清零。
- [x] 清零不删除历史流水。

验证记录：

- RED：新增 `ClubPointAnnualClearingServiceImplTest`，运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointAnnualClearingServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`，失败原因为 `ClubPointAnnualClearingService`、实现类和清零请求 / 结果 BO 缺失。
- GREEN：新增 `ClubPointAnnualClearingService` / `ClubPointAnnualClearingServiceImpl`、`ClubPointAnnualClearUserReqBO`、`ClubPointAnnualClearAllReqBO`、`ClubPointAnnualClearResultBO`；补 `ClubPointAccountMapper.selectListForAnnualClearing()`，并在账本服务中为年度清零来源生成非规则项快照；同一命令返回 `BUILD SUCCESS`，`ClubPointAnnualClearingServiceImplTest` 运行 `4` 个测试，失败 `0`，错误 `0`。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointAnnualClearingServiceImplTest,ClubPointAnnualClearingModelTest,ClubPointLedgerServiceImplTest,ClubPointLedgerMapperTest,ClubPointFreezeServiceImplTest,ClubPointRedemptionCancelServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `25` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；年度清零 Service / 测试、账户 Mapper 和账本适配范围 `tenant_id|TenantBaseDO` 无命中；精确元数据模式无命中；Redis 模式无命中；年度清零 Service 范围无 `transactionMapper`、账户直接更新或删除历史流水命中。

实现边界：

- M10.3 只实现年度清零 Service，不实现 Job 或 API；Job 进入 M10.4，接口进入 M10.8。
- 单用户清零先按 `year,user_id` 行锁读取年度清零记录，已 `SUCCESS` 或 `SKIPPED` 时直接返回既有记录；新增记录和流水幂等键共同兜底重复请求。
- 清零金额只取账户当前 `availablePoints`，冻结积分只写入清零前快照，不参与扣减；无可用积分写 `SKIPPED` 记录且不创建流水。
- 积分扣减只通过 `ClubPointLedgerService.createTransaction(...)` 生成年度清零负向流水，由账本同事务更新账户缓存；年度清零 Service 不直接写 `club_points_transaction`，不直接更新 `club_points_point_account`，不删除历史流水。
- 年度清零不是分值规则项，但 `club_points_transaction.rule_version_id` 必填；账本服务仅在 `sourceType=ANNUAL_CLEARING` 时用当前生效规则版本生成 `ANNUAL_CLEARING` 非规则项快照，其他来源仍走规则项快照和分值区间校验。

## 任务 M10.4 年度清零 Job

- [x] 创建年度清零 Job Handler。
- [x] Job 只调 Service。
- [x] 写 `club_points_job_run`。
- [x] 失败记录错误摘要。
- [x] 支持人工重跑失败用户。
- [x] 人工处理写强审计。

验收：

- [x] Job 可手动触发。
- [x] Job 重跑幂等。
- [x] 失败用户可定位。

验证记录：

- RED：新增 `ClubPointAnnualClearingJobTest`，运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointAnnualClearingJobTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`，失败原因为 `ClubPointAnnualClearingJob`、`ClubPointAnnualClearingJobService` 和 `ClubPointAnnualClearingJobReqBO` 缺失。
- GREEN：新增 `ClubPointAnnualClearingJob`、`ClubPointAnnualClearingJobService`、`ClubPointAnnualClearingJobReqBO`；同一命令返回 `BUILD SUCCESS`，`ClubPointAnnualClearingJobTest` 运行 `4` 个测试，失败 `0`，错误 `0`。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointAnnualClearingJobTest,ClubPointAnnualClearingServiceImplTest,ClubPointAnnualClearingModelTest,ClubPointLedgerServiceImplTest,ClubPointLedgerMapperTest,ClubAuditServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `22` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无输出；年度清零 Job / Service / 测试范围 `tenant_id|TenantBaseDO` 无命中；精确元数据模式无命中；Redis 模式无命中；年度清零 Job / Service 范围无 `transactionMapper`、账户直接更新或删除历史记录命中。

实现边界：

- `ClubPointAnnualClearingJob` 只解析 JSON 参数并调用 `ClubPointAnnualClearingJobService`，Bean 名固定为 seed 中的 `clubPointsAnnualClearingJob`。
- Job 幂等键固定为 `ANNUAL_CLEARING_JOB:{runKey}:{retryCount}`；同一运行键和重试次数重复触发直接返回既有 `club_points_job_run`，不会重复清零或重复流水。
- 全量任务默认扫描积分账户；人工重跑失败用户可通过 `userIds` 指定目标用户，只对这些用户逐个调用 `ClubPointAnnualClearingService.clearUser(...)`。
- 每个用户单独调用清零 Service；单用户失败不会阻断其他用户，失败用户 ID 和错误摘要写入 `club_points_job_run.result_json`、`error_type`、`error_message`，任务状态置为 `RETRYABLE_FAILED` 并设置 `next_retry_time`。
- 手动触发或重试触发会先写 `ANNUAL_CLEARING_MANUAL` 强审计；审计失败时不创建 Job Run、不调用年度清零 Service、不生成流水。
- M10.4 不实现管理员 API；年度清零触发和记录查询接口进入 M10.8。

## 任务 M10.5 年度排名

- [x] 创建 `ClubPointAnnualRankingRecordDO`。
- [x] 创建对应 Mapper。
- [x] 按年度积分获得值统计排名。
- [x] 排名不受兑换扣分影响。
- [x] 保存俱乐部快照，并保留流水 ID 追溯用户、部门快照。
- [x] 保存排名生成时间和规则快照。
- [x] 支持重新生成排名。

验收：

- [x] 兑换扣分不会降低年度排名贡献值。
- [x] 排名可追溯到年度和统计口径。

验证记录：

- RED：新增 `ClubPointAnnualRankingServiceImplTest`，运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointAnnualRankingServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`，失败原因为 `ClubPointAnnualRankingRecordDO`、`ClubPointAnnualRankingRecordMapper`、`ClubPointAnnualRankingGenerateReqBO`、`ClubPointAnnualRankingService` 和 `ClubPointAnnualRankingServiceImpl` 缺失。
- GREEN：新增年度排名 DO、Mapper、生成请求 BO、Service 和实现，补 `ClubPointTransactionMapper.selectListForAnnualRanking(...)`；同一命令返回 `BUILD SUCCESS`，`ClubPointAnnualRankingServiceImplTest` 运行 `2` 个测试，失败 `0`，错误 `0`。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointAnnualRankingServiceImplTest,ClubPointAnnualClearingJobTest,ClubPointAnnualClearingServiceImplTest,ClubPointLedgerServiceImplTest,ClubPointLedgerMapperTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `17` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅有 `ClubPointTransactionMapper.java` 的 CRLF/LF 提示；M10.5 源码和测试范围 `tenant_id|TenantBaseDO` 无命中；精确元数据模式无命中；Redis 模式无命中；年度排名 Service 范围无账户直接更新、流水直接插入或删除流水命中。

实现边界：

- M10.5 只实现年度排名记录和生成服务，不生成激励记录、不登记预算、不实现 API、不实现 Job；激励建议记录进入 M10.6，预算进入 M10.7，接口进入 M10.8。
- 年度排名对象按数据库设计固定为俱乐部，不是员工账户；排名记录保存 `club_id`、`club_code_snapshot`、`club_name_snapshot`，用户和部门快照不复制到排名表，而是通过 `snapshot_json.positiveTransactionIds` 和 `snapshot_json.reversalTransactionIds` 回查流水事实源追溯。
- 排名从 `club_points_transaction` 聚合，不读取账户缓存；纳入年度正向发放流水时只统计基础参与、全程额外、主动贡献和特殊奖励分类，并按发放俱乐部快照聚合。
- 撤销扣减只统计 `sourceType=REVERSAL` 且指向已纳入正向发放流水的撤销流水，避免把兑换扣分、年度清零、普通扣分或账户余额变化混入俱乐部发放量。
- 重新生成会物理删除同年度旧排名快照后重建，以避开派生表 `year,club_code_snapshot` 唯一键和逻辑删除冲突；该操作只影响排名快照，不删除或改写积分流水事实源。
- 排名规则快照保存当前生效规则版本 ID、版本号、统计公式、分类积分、撤销积分和参与计算的流水 ID；激励建议金额按当前 PRD 固定为第 `1-3` 名 `2000` 元、第 `4-6` 名 `1000` 元，确认状态初始为待确认。

## 任务 M10.6 激励建议

- [x] 创建 `ClubPointIncentiveRecordDO`。
- [x] 创建对应 Mapper。
- [x] 根据排名生成激励建议。
- [x] 保存建议金额或激励等级。
- [x] 管理员确认激励。
- [x] 管理员取消激励。
- [x] 确认和取消写强审计。

验收：

- [x] 激励建议不自动等于发放。
- [x] 管理员确认后状态固定。

验证记录：

- RED：新增 `ClubPointIncentiveServiceImplTest`，运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointIncentiveServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`，失败原因为激励记录 DO、Mapper、状态 / 类型 / 来源枚举、建议 / 操作 BO、Service / 实现、`INCENTIVE_CONFIRM`、`INCENTIVE_CANCEL` 和激励错误码缺失。
- GREEN：新增 `ClubPointIncentiveRecordDO`、`ClubPointIncentiveRecordMapper`、激励状态 / 类型 / 来源枚举、`ClubPointIncentiveSuggestReqBO`、`ClubPointIncentiveOperationReqBO`、`ClubPointIncentiveService` / `ClubPointIncentiveServiceImpl`；补确认和取消强审计动作以及激励错误码；同一命令返回 `BUILD SUCCESS`，`ClubPointIncentiveServiceImplTest` 运行 `3` 个测试，失败 `0`，错误 `0`。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointIncentiveServiceImplTest,ClubPointAnnualRankingServiceImplTest,ClubPointAnnualClearingJobTest,ClubPointAnnualClearingServiceImplTest,ClubAuditServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `18` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅常量文件有 CRLF/LF 提示；M10.6 源码和测试范围 `tenant_id|TenantBaseDO` 无命中；精确元数据模式无命中；Redis 模式无命中；激励 Service 范围无账本 Service、流水 Mapper、账户 Mapper、流水表或账户表绕行命中。

实现边界：

- M10.6 只实现激励建议记录和确认 / 取消服务，不登记预算记录、不实现 API、不生成员工积分流水、不修改积分账户；预算进入 M10.7，接口进入 M10.8。
- 年度排名激励建议从 `club_points_annual_ranking_record` 读取，只为 `incentive_amount_cent > 0` 的排名记录创建 `club_points_incentive_record`；按 `source_type=ANNUAL_RANKING` 和 `source_id=ranking.id` 查重，重复生成直接跳过。
- 激励类型固定为俱乐部排名激励，状态初始为 `SUGGESTED`；标题和金额来自排名快照，`budget_record_id` 为空，表示建议还没有等于预算登记或实际发放。
- 确认和取消都要求管理员全局范围，只允许从 `SUGGESTED` 状态操作；确认写 `confirmed_by`、`confirmed_time` 和备注后状态变 `CONFIRMED`，取消状态变 `CANCELED`，确认后再次取消会抛 `CLUB_INCENTIVE_STATUS_INVALID`。
- 确认和取消均写 `INCENTIVE_RECORD` 强审计，动作分别为 `INCENTIVE_CONFIRM` 和 `INCENTIVE_CANCEL`；审计失败时同事务回滚激励状态更新。

## 任务 M10.7 预算记录

- [x] 创建 `ClubPointBudgetRecordDO`。
- [x] 创建对应 Mapper。
- [x] 管理员新增预算记录。
- [x] 管理员修改预算记录。
- [x] 管理员停用预算记录。
- [x] 预算记录关联年度、俱乐部或全局范围。
- [x] 修改预算写强审计。

验收：

- [x] 预算记录可查询。
- [x] 激励和兑换可引用预算统计。

验证记录：

- RED：新增 `ClubPointBudgetServiceImplTest`，运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointBudgetServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`，失败原因为预算 DO、Mapper、分类 / 来源枚举、保存 / 操作 / 查询 BO、Service / 实现、`BUDGET_CREATE`、`BUDGET_UPDATE`、`BUDGET_DISABLE` 和预算错误码缺失。
- GREEN：新增 `ClubPointBudgetRecordDO`、`ClubPointBudgetRecordMapper`、预算分类 / 来源枚举、`ClubPointBudgetSaveReqBO`、`ClubPointBudgetOperationReqBO`、`ClubPointBudgetQueryReqBO`、`ClubPointBudgetService` / `ClubPointBudgetServiceImpl`；补预算创建 / 修改 / 停用强审计动作和预算错误码；同一命令返回 `BUILD SUCCESS`，`ClubPointBudgetServiceImplTest` 运行 `4` 个测试，失败 `0`，错误 `0`。
- 边界补强：新增非法 `source_type` 更新拒绝测试；初次运行失败为数据库 `tinyint` 溢出，修复后由业务校验返回 `CLUB_BUDGET_INVALID`，并断言原预算来源不变。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointBudgetServiceImplTest,ClubPointIncentiveServiceImplTest,ClubPointAnnualRankingServiceImplTest,ClubPointRedemptionReviewServiceImplTest,ClubAuditServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `21` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅常量文件有 CRLF/LF 提示；M10.7 源码和测试范围 `tenant_id|TenantBaseDO` 无命中；精确元数据模式无命中；Redis 模式无命中；预算 Service 范围无账本 Service、流水 Mapper、账户 Mapper、流水表或账户表绕行命中。

实现边界：

- M10.7 只实现预算记录 DO、Mapper 和 Service，不实现 Controller、前端页面或报表导出；管理员预算管理 API 进入 M10.8，报表统计进入 M11。
- 数据库设计中的 `club_points_budget_record` 没有独立 `year`、`club_id` 或 `status` 字段；M10.7 不擅自改表。年度统计按 `occur_date` 年份过滤；全局手工记录用 `source_type=MANUAL` 且 `source_id=null` 表达；俱乐部排名激励预算通过 `source_type=RANKING_INCENTIVE`、`source_id=incentive.id` 追溯到激励记录里的俱乐部和年度快照。
- 停用预算记录使用 `BaseDO.deleted` 逻辑删除表达，不新增状态字段；停用后常规查询不再返回该预算记录。
- 预算创建、修改和停用都要求管理员全局范围并写强审计，动作分别为 `BUDGET_CREATE`、`BUDGET_UPDATE`、`BUDGET_DISABLE`；审计失败时同事务回滚预算变更。
- 预算来源类型包含手工、排名激励、创新奖和积分兑换；排名激励来源只允许引用已确认激励，创建预算后回写激励记录 `budget_record_id`，防止激励建议被重复登记为经费记录。
- M10.7 不生成员工积分流水、不更新积分账户、不写兑换申请；兑换预算统计通过预算记录 `source_type=REDEMPTION` 和 `source_id` 预留追溯能力。

## 任务 M10.8 API

- [x] 员工异议提交接口。
- [x] 员工异议列表接口。
- [x] 管理员异议处理接口。
- [x] 管理员年度清零接口。
- [x] 管理员年度清零记录接口。
- [x] 管理员排名生成接口。
- [x] 管理员排名查询接口。
- [x] 管理员激励确认接口。
- [x] 管理员预算管理接口。

验收：

- [x] API 路径和 `club-points-api-design.md` 一致。
- [x] 员工只能查本人异议。
- [x] 负责人不能执行年度清零。

验证记录：

- RED：新增 `ClubPointAnnualOperationControllerTest`，运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointAnnualOperationControllerTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`，失败原因为 M10.8 的员工异议、管理员异议、年度运营和预算 Controller / VO 包不存在。
- GREEN：新增 `ClubPointDisputeAppController`、`ClubPointDisputeAdminController`、`ClubPointAnnualAdminController`、`ClubPointBudgetAdminController` 及对应 VO；补异议本人 / 管理员分页查询、年度清零记录分页和年度排名分页查询能力；同一命令返回 `BUILD SUCCESS`，`ClubPointAnnualOperationControllerTest` 运行 `6` 个测试，失败 `0`，错误 `0`。
- 组合修复：M10.8 初次组合验证失败在旧年度清零 / 排名 / Job 测试上下文缺少 `ClubScopeService` Bean；根因是查询分页方法把后台查询权限范围校验下沉到年度核心 Service。修复为年度清零记录和年度排名分页 Service 只做查询，管理员动作权限继续由 Controller 的 `@PreAuthorize` 承担。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointAnnualOperationControllerTest,ClubPointDisputeServiceImplTest,ClubPointAnnualClearingJobTest,ClubPointAnnualClearingServiceImplTest,ClubPointAnnualRankingServiceImplTest,ClubPointIncentiveServiceImplTest,ClubPointBudgetServiceImplTest,ClubAuditServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `36` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 LF/CRLF 警告；M10.8 源码和测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中；M10.8 源码和测试范围 Redis 模式无命中；新 Controller 范围无直接写流水 / 账户表命中；本次 Service diff 无新增直接写流水 / 账户表命中。

实现边界：

- 员工端异议接口固定为 `/clubpoints/app/dispute/create`、`/my-page`、`/get`；当前登录人从安全上下文读取，不允许前端传 `userId`。
- 管理员异议、年度、预算接口均使用 `@PreAuthorize`；年度清零只在 `/clubpoints/annual/clear` 使用 `clubpoints:annual:clear`，清零记录和排名查询使用 `clubpoints:annual:query`，排名生成、激励建议、激励确认 / 取消使用 `clubpoints:annual:manage`。
- 管理员写接口统一由 Controller 注入 `operatorGlobalScope=true`、登录用户、昵称、角色快照、IP 和 UA；请求 VO 不暴露操作人字段。
- 预算接口暴露分页、创建、修改和停用；停用仍走 M10.7 的逻辑删除 Service，不新增预算状态字段。
- 年度清零记录和年度排名分页是管理员后台查询能力，Controller 已有动作权限；年度清零 / 年度排名核心 Service 不新增 `ClubScopeService` 依赖，避免让清零、排名、Job 等核心测试上下文被查询权限横切能力污染。

## 任务 M10.9 测试

- [ ] 测试异议提交和处理。
- [ ] 测试异议调整积分走账本。
- [ ] 测试年度清零。
- [ ] 测试重复清零幂等。
- [ ] 测试冻结积分不清零。
- [ ] 测试跨年冻结释放。
- [ ] 测试年度排名不受兑换影响。
- [ ] 测试激励确认。
- [ ] 测试预算修改强审计。

验收：

- [ ] 年度运营测试通过。
- [ ] 清零、排名、激励、预算事实一致。

## M10 放行标准

- [ ] 异议闭环可用。
- [ ] 年度清零可用且幂等。
- [ ] 年度排名可用。
- [ ] 激励建议和确认可用。
- [ ] 预算记录可用。

## M10 不通过时禁止

- [ ] 禁止做最终 MVP 验收。
- [ ] 禁止输出“年度闭环完成”结论。
