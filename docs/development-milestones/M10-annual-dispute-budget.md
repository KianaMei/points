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

- [ ] 创建 `ClubPointAnnualRankingRecordDO`。
- [ ] 创建对应 Mapper。
- [ ] 按年度积分获得值统计排名。
- [ ] 排名不受兑换扣分影响。
- [ ] 保存用户、部门、俱乐部快照。
- [ ] 保存排名生成时间和规则快照。
- [ ] 支持重新生成排名。

验收：

- [ ] 兑换扣分不会降低年度排名贡献值。
- [ ] 排名可追溯到年度和统计口径。

## 任务 M10.6 激励建议

- [ ] 创建 `ClubPointIncentiveRecordDO`。
- [ ] 创建对应 Mapper。
- [ ] 根据排名生成激励建议。
- [ ] 保存建议金额或激励等级。
- [ ] 管理员确认激励。
- [ ] 管理员取消激励。
- [ ] 确认和取消写强审计。

验收：

- [ ] 激励建议不自动等于发放。
- [ ] 管理员确认后状态固定。

## 任务 M10.7 预算记录

- [ ] 创建 `ClubPointBudgetRecordDO`。
- [ ] 创建对应 Mapper。
- [ ] 管理员新增预算记录。
- [ ] 管理员修改预算记录。
- [ ] 管理员停用预算记录。
- [ ] 预算记录关联年度、俱乐部或全局范围。
- [ ] 修改预算写强审计。

验收：

- [ ] 预算记录可查询。
- [ ] 激励和兑换可引用预算统计。

## 任务 M10.8 API

- [ ] 员工异议提交接口。
- [ ] 员工异议列表接口。
- [ ] 管理员异议处理接口。
- [ ] 管理员年度清零接口。
- [ ] 管理员年度清零记录接口。
- [ ] 管理员排名生成接口。
- [ ] 管理员排名查询接口。
- [ ] 管理员激励确认接口。
- [ ] 管理员预算管理接口。

验收：

- [ ] API 路径和 `club-points-api-design.md` 一致。
- [ ] 员工只能查本人异议。
- [ ] 负责人不能执行年度清零。

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
