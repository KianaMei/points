# M9 兑换闭环 Implementation Plan

**Status:** `[x]` M9 已完成并有 RED/GREEN、组合测试和质量门禁证据；下一步入口是 M10 异议、年度清零、排名、激励、预算闭环。

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现兑换批次、礼品、资格快照、申请、冻结、库存锁、审核发放，保证并发不超兑、重复提交不重复扣分。

**Architecture:** 兑换申请同事务冻结积分、锁库存、创建申请；审核通过冻结转扣减并生成兑换流水，拒绝或取消释放冻结和库存。库存事实源是数据库条件更新，不是 Redis。

**Tech Stack:** Spring 事务、MyBatis、LedgerService、Lock4j 短锁、数据库条件更新、强审计、JUnit。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 不能用 Redis 当库存事实源。
- 兑换申请、库存锁、审核扣分必须数据库幂等。
- 资格判断使用快照。
- 审核必须强审计。
- Java 行为必须 TDD。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/redemption/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/redemption/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/redemption/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/app/redemption/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/redemption/`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/redemption/`

**Interfaces:**

- Consumes: M4 LedgerService 冻结能力、M3 规则项、M2 强审计、M1 库存条件更新字段。
- Produces: 批次、礼品、资格快照、申请、库存锁、审核发放接口。

**Verification:**

- Run: 兑换申请并发测试。
- Expected: 不超兑，不重复冻结。
- Run: 兑换审核测试。
- Expected: 通过后冻结转扣减并生成流水，拒绝或取消后释放冻结和库存。
- Run: 跨年冻结释放测试。
- Expected: 释放回账户，不追加过期清零。

## 目标

实现兑换批次、礼品、资格快照、申请、冻结、库存锁、审核发放。重点是并发不超兑、重复提交不重复扣分。

## 前置条件

- M8 已放行。
- LedgerService 冻结和冻结转扣减可用。
- 规则读取可用。
- 库存相关唯一键和字段已落库。
- 强审计可用。

## 任务 M9.1 DO 和 Mapper

- [x] 创建 `ClubPointRedemptionBatchDO`。
- [x] 创建 `ClubPointRedemptionGiftDO`。
- [x] 创建 `ClubPointRedemptionEligibilitySnapshotDO`。
- [x] 创建 `ClubPointRedemptionApplicationDO`。
- [x] 创建 `ClubPointStockLockDO`。
- [x] 创建 `ClubPointRedemptionReviewRecordDO`。
- [x] 创建对应 Mapper。
- [x] 字段和 M1 DDL 一致。

验收：

- [x] 批次、礼品、资格、申请、库存锁、审核记录都可落库。
- [x] 申请幂等键存在。

证据：

- RED：新增 `ClubPointRedemptionMapperTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionMapperTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `dal.dataobject.redemption` 包、6 个兑换 DO 和 6 个兑换 Mapper 不存在，符合 M9.1 RED 预期。
- GREEN：新增兑换批次、礼品、资格快照、兑换申请、库存锁、审核记录 6 个 DO 和对应 Mapper；字段按 M1 DDL 映射，JSON 字段按字符串承载，DO 继承 `BaseDO`，未新增 `tenant_id` 或 `TenantBaseDO`。
- H2 兼容修正：`club_points_redemption_batch.year` 是正式 schema 字段，H2 测试库把 `year` 视为保留字；本次复用既有 `ClubPointUserYearStatusDO` 模式，在 `ClubPointRedemptionBatchDO#year` 加反引号列名映射，不改正式 schema。
- 实现边界：M9.1 只落 DO/Mapper 和映射测试，不实现批次状态机、资格生成、库存条件更新、积分冻结、审核扣分、释放或 API；没有引入 Redis 库存事实源。
- M9.1 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionMapperTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRedemptionMapperTest` 运行 `1` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；redemption DO/Mapper/Test 范围 `tenant_id|TenantBaseDO` 无命中；redemption DO/Mapper/Test 精确元数据模式无命中；本次无 `service/redemption`、LedgerService、冻结 Service 或 Redis 库存实现。

## 任务 M9.2 批次 Service

- [x] 创建兑换批次。
- [x] 修改未开启批次。
- [x] 开启批次。
- [x] 关闭批次。
- [x] 生成资格快照。
- [x] 修改资格规则写强审计。
- [x] 批次开启后关键规则不可随意变更。

验收：

- [x] 批次状态机受控。
- [x] 资格快照可追溯。
- [x] 批次关闭后不能新增申请。

证据：

- RED：新增 `ClubPointRedemptionBatchServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionBatchServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `ClubPointRedemptionBatchService`、`ClubPointRedemptionBatchServiceImpl`、批次 BO、`ClubPointRedemptionBatchStatusEnum`、批次错误码、`REDEMPTION_BATCH_RULE_UPDATE` 和快照 mapper 查询方法不存在，符合 M9.2 RED 预期。
- GREEN：新增兑换批次 Service/BO、批次状态枚举、批次错误码和 `REDEMPTION_BATCH_RULE_UPDATE` 强审计动作；批次创建默认为草稿，草稿可修改，开启批次同事务生成账户资格快照，关闭后 `validateBatchOpenForApply(...)` 拒绝后续申请入口。
- 资格快照口径：按账户缓存 `available_points` 降序、`user_id` 升序生成快照，保存用户、部门、净积分、冻结积分、可用积分、年度获取积分、排名、资格结果和 cutoff 同分标记；资格快照只作为申请资格事实源，M9.2 不做申请实时余额校验。
- 强审计与事务边界：修改最低可用积分、资格人数、并列规则、资格规则 JSON、规则版本或规则快照会写强审计；审计失败会回滚批次规则修改。
- 实现边界：M9.2 只实现批次 Service，不实现礼品 Service、库存条件更新、员工申请、积分冻结、库存锁、审核扣分、释放或 API；没有引入 Redis 库存事实源，也没有调用 LedgerService 或直接写积分流水。
- M9.2 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionBatchServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRedemptionBatchServiceImplTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- M9 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRedemptionMapperTest,ClubPointRedemptionBatchServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `6` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中；clubpoints 主代码 Redis 库存事实源模式无命中；redemption Service 范围无 `transactionMapper.insert`、`club_points_transaction`、`createTransaction(...)` 或 `reverseTransaction(...)` 命中。

## 任务 M9.3 礼品 Service

- [x] 新增礼品。
- [x] 修改礼品。
- [x] 上架礼品。
- [x] 下架礼品。
- [x] 设置积分价格。
- [x] 设置库存。
- [x] 使用数据库条件更新锁库存。
- [x] 不用 Redis 当库存事实源。

验收：

- [x] 已锁库存和已兑库存字段准确。
- [x] 库存不足时申请失败。
- [x] 并发申请不会超兑。

证据：

- RED：新增 `ClubPointRedemptionGiftServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionGiftServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `ClubPointRedemptionGiftService`、`ClubPointRedemptionGiftServiceImpl`、礼品 BO、`ClubPointRedemptionGiftStatusEnum` 和礼品库存错误码不存在，符合 M9.3 RED 预期。
- GREEN：新增兑换礼品 Service/BO、礼品状态枚举、礼品错误码，并补礼品 Mapper 条件更新方法；创建礼品默认下架且 `stock_locked=0`、`stock_used=0`，修改礼品可维护积分价格、库存、档位、图片和排序，上架/下架只允许有效状态。
- 库存事实源：`lockStock(...)` 使用数据库条件更新，要求礼品上架且 `stock_total - stock_locked - stock_used >= quantity`；`releaseLockedStock(...)` 条件减少已锁库存，`useLockedStock(...)` 条件减少已锁库存并增加已兑库存；库存不足会抛礼品库存不足错误，作为 M9.5 申请失败入口。
- 实现边界：M9.3 只实现礼品 Service 和库存计数能力，不创建员工兑换申请、不冻结积分、不创建库存锁记录、不审核扣分、不释放冻结、不实现 API；没有引入 Redis 库存事实源，也没有调用 LedgerService 或直接写积分流水。
- M9.3 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionGiftServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRedemptionGiftServiceImplTest` 运行 `6` 个测试，失败 `0`，错误 `0`。
- M9 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRedemptionMapperTest,ClubPointRedemptionBatchServiceImplTest,ClubPointRedemptionGiftServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `12` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中；clubpoints 主代码 Redis 库存事实源模式无命中；redemption Service 范围无 `transactionMapper.insert`、`club_points_transaction`、`createTransaction(...)` 或 `reverseTransaction(...)` 命中。

## 任务 M9.4 资格快照

- [x] 按批次生成人员资格快照。
- [x] 保存用户、部门、积分、排名、规则快照。
- [x] 员工申请时读取快照。
- [x] 批次内资格不受后续积分变化影响，除非设计明确允许刷新。
- [x] 支持管理员查看资格快照。

验收：

- [x] 资格判断有历史依据。
- [x] 申请时不重新临时拼规则。

证据：

- RED：新增 `ClubPointRedemptionEligibilityServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionEligibilityServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `ClubPointRedemptionEligibilityService`、`ClubPointRedemptionEligibilityServiceImpl`、资格不存在和资格不合格错误码不存在，符合 M9.4 RED 预期。
- GREEN：新增兑换资格快照 Service 和实现，补资格不存在/不合格错误码，并给资格快照 Mapper 增加按批次和资格结果筛选查询；管理员查看资格快照要求全局范围，员工申请前通过 `validateUserQualifiedForApply(...)` 读取本人快照并校验 `qualified=true`。
- 快照事实源：资格快照生成仍由 M9.2 的 `openBatch(...)` 完成，M9.4 不复制生成逻辑；申请前资格判断只读取 `club_points_redemption_eligibility_snapshot`，不读取实时账户、不重新拼规则，后续积分变化不会改写批次内资格结论。
- 实现边界：M9.4 只补资格快照读侧和申请前资格校验服务，不创建申请、不冻结积分、不锁库存、不审核扣分、不实现 API；没有引入 Redis 库存事实源，也没有调用 LedgerService 或直接写积分流水。
- M9.4 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionEligibilityServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRedemptionEligibilityServiceImplTest` 运行 `3` 个测试，失败 `0`，错误 `0`。
- M9 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRedemptionMapperTest,ClubPointRedemptionBatchServiceImplTest,ClubPointRedemptionGiftServiceImplTest,ClubPointRedemptionEligibilityServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `15` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中；clubpoints 主代码 Redis 库存事实源模式无命中；redemption Service 范围无 `transactionMapper.insert`、`club_points_transaction`、`createTransaction(...)` 或 `reverseTransaction(...)` 命中。

## 任务 M9.5 申请 Service

- [x] 员工查看可兑换礼品。
- [x] 员工提交兑换申请。
- [x] 校验批次状态。
- [x] 校验资格快照。
- [x] 校验礼品状态。
- [x] 校验可用积分。
- [x] 同事务冻结积分。
- [x] 同事务锁定库存。
- [x] 同事务创建申请。
- [x] 使用 requestNo 或 idempotency key 防重复提交。

验收：

- [x] 申请成功后可用积分减少，冻结积分增加。
- [x] 礼品已锁库存增加。
- [x] 重复提交返回同一结果或被幂等拦截。

证据：

- RED：新增 `ClubPointRedemptionApplicationServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionApplicationServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `ClubPointRedemptionApplicationStatusEnum`、`ClubPointStockLockStatusEnum`、`ClubPointRedemptionApplyReqBO`、`ClubPointRedemptionApplicationService` 和 `ClubPointRedemptionApplicationServiceImpl` 不存在，符合 M9.5 RED 预期。
- GREEN：新增兑换申请 Service/BO、兑换申请状态枚举、库存锁状态枚举，并给礼品 Mapper 增加按批次和状态查询；员工可兑换列表先按批次开放状态和本人资格快照校验，再只返回本批次上架礼品。
- 申请事务：`apply(...)` 同事务创建 `PENDING_REVIEW(1)` 申请、调用 `ClubPointFreezeService.freezePoints(...)` 冻结积分、调用礼品 Service 的数据库条件库存更新锁库存、创建 `LOCKED(1)` 库存锁并回填申请 `freeze_id` 和 `stock_lock_id`；申请前账户值、资格排名、批次快照和礼品快照写入申请。
- 幂等与回滚：申请幂等键为 `REDEMPTION_APPLY:{batchId}:{giftId}:{userId}:{requestNo}`，重复提交直接返回既有申请 ID，不重复冻结或锁库存；库存不足会回滚申请、冻结、账户冻结积分和库存计数；可用积分不足不创建申请、不锁库存、不创建库存锁。
- 实现边界：M9.5 只实现员工查看可兑换礼品和提交申请 Service，不做审核通过扣分、不做拒绝/取消释放、不做超时处理、不做 API；冻结由 M4 冻结服务持账户行锁完成，不直接改积分余额；库存事实源仍是数据库条件更新，未引入 Redis；redemption 主实现未调用 LedgerService 创建或撤销流水。
- M9.5 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionApplicationServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRedemptionApplicationServiceImplTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- M9 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRedemptionMapperTest,ClubPointRedemptionBatchServiceImplTest,ClubPointRedemptionGiftServiceImplTest,ClubPointRedemptionEligibilityServiceImplTest,ClubPointRedemptionApplicationServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `20` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中；clubpoints 主代码 Redis 库存事实源模式无命中；redemption 主实现范围无直接写账本或调用账本命中，M9.5 测试中的 `createTransaction(...)` / `reverseTransaction(...)` 仅为空实现测试替身方法签名。

## 任务 M9.6 审核 Service

- [x] 管理员查看待审核申请。
- [x] 管理员审核通过。
- [x] 审核通过冻结转扣减。
- [x] 审核通过库存锁转已兑。
- [x] 审核通过写兑换负向流水。
- [x] 管理员审核拒绝。
- [x] 审核拒绝释放冻结。
- [x] 审核拒绝释放库存锁。
- [x] 审核写强审计。
- [x] 通知员工审核结果。

验收：

- [x] 通过后积分真正扣减。
- [x] 拒绝后积分和库存恢复。
- [x] 重复审核不重复扣分。

证据：

- RED：新增 `ClubPointRedemptionReviewServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionReviewServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是兑换审核结果枚举、审核请求 BO、审核 Service 方法、申请/库存锁行锁查询、审核错误码和 `REDEMPTION_REVIEW` 审计动作不存在，符合 M9.6 RED 预期。
- GREEN：新增 `ClubPointRedemptionReviewResultEnum` 和 `ClubPointRedemptionReviewReqBO`，扩展兑换申请 Service 审核能力；管理员待审核列表要求全局范围并只返回 `PENDING_REVIEW(1)`，审核入口要求管理员全局范围。
- 审核通过事务：强审计成功后调用 `ClubPointFreezeService.convertFreezeToDeduction(...)` 冻结转扣减并生成兑换负向流水，调用礼品 Service 数据库条件更新把已锁库存转已兑，库存锁转 `USED(2)`，申请转 `APPROVED_AND_ISSUED(3)`，写审核记录并通知员工。
- 审核拒绝事务：强审计成功后调用 `ClubPointFreezeService.releaseFreeze(...)` 释放冻结，调用礼品 Service 数据库条件更新释放已锁库存，库存锁转 `RELEASED(3)`，申请转 `REJECTED(4)`，写审核记录并通知员工；拒绝必须填写原因。
- 幂等和回滚：相同审核结果重复提交直接返回，不重复扣分、不重复转库存、不重复写审核记录；非待审核状态用不同结果审核会拒绝；强审计失败回滚业务，通知失败不回滚业务。
- 实现边界：M9.6 只实现审核 Service，不实现员工取消、超时处理或 API；兑换扣分只通过冻结 Service 转扣减间接走账本服务，不直接写 `club_points_transaction`；库存事实源仍是数据库条件更新，未引入 Redis。
- M9.6 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionReviewServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRedemptionReviewServiceImplTest` 运行 `7` 个测试，失败 `0`，错误 `0`。
- M9 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRedemptionMapperTest,ClubPointRedemptionBatchServiceImplTest,ClubPointRedemptionGiftServiceImplTest,ClubPointRedemptionEligibilityServiceImplTest,ClubPointRedemptionApplicationServiceImplTest,ClubPointRedemptionReviewServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `27` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中；clubpoints 主代码 Redis 库存事实源模式无命中；redemption 生产 Service 范围无直接写账本命中，测试中的 `createTransaction(...)` / `reverseTransaction(...)` 仅为空实现测试替身方法签名。

## 任务 M9.7 取消和超时

- [x] 员工在允许状态取消申请。
- [x] 取消释放冻结。
- [x] 取消释放库存锁。
- [x] 超时未审核按规则处理。
- [x] 取消和超时写记录。

验收：

- [x] 取消不生成扣减流水。
- [x] 释放后库存和积分一致。

证据：

- RED：新增 `ClubPointRedemptionCancelServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionCancelServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `ClubPointRedemptionCancelReqBO` 和 `ClubPointRedemptionTimeoutReqBO` 不存在，符合 M9.7 RED 预期。
- GREEN：新增 `ClubPointRedemptionCancelReqBO`、`ClubPointRedemptionTimeoutReqBO`，扩展兑换申请 Service，提供员工本人取消和待审核超时批量处理能力；员工取消要求本人范围，已取消同用户重复调用幂等返回。
- 取消事务：仅允许 `PENDING_REVIEW(1)` 申请取消；取消后申请转 `CANCELED_BEFORE_REVIEW(2)` 并写 `cancel_time`、`cancel_reason`，调用 `ClubPointFreezeService.releaseFreeze(...)` 释放冻结，调用礼品 Service 数据库条件更新释放已锁库存，库存锁转 `RELEASED(3)` 并写释放原因。
- 超时口径：设计文档没有单独超时状态，M9.7 按待审核申请自动取消处理；`timeoutPendingApplications(...)` 要求全局范围，只处理 `apply_time <= appliedBefore` 的待审核申请，复用取消释放链路并返回处理数量。
- 实现边界：M9.7 只实现取消和超时 Service，不实现取消 API、定时任务或领取状态；取消和超时不生成兑换扣减流水，不直接写 `club_points_transaction`；库存事实源仍是数据库条件更新，未引入 Redis。
- M9.7 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionCancelServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRedemptionCancelServiceImplTest` 运行 `4` 个测试，失败 `0`，错误 `0`。
- M9 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRedemptionCancelServiceImplTest,ClubPointRedemptionMapperTest,ClubPointRedemptionBatchServiceImplTest,ClubPointRedemptionGiftServiceImplTest,ClubPointRedemptionEligibilityServiceImplTest,ClubPointRedemptionApplicationServiceImplTest,ClubPointRedemptionReviewServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `31` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中；clubpoints 主代码 Redis 库存事实源模式无命中；redemption 生产 Service 范围无直接写账本命中，测试中的 `createTransaction(...)` / `reverseTransaction(...)` 仅为空实现测试替身方法签名。

## 任务 M9.8 API

- [x] 管理员批次管理接口。
- [x] 管理员礼品管理接口。
- [x] 管理员资格快照接口。
- [x] 管理员审核接口。
- [x] 员工可兑换列表接口。
- [x] 员工提交申请接口。
- [x] 员工申请记录接口。
- [x] 员工取消申请接口。

验收：

- [x] API 路径和 `club-points-api-design.md` 一致。
- [x] 负责人不能审核兑换。

证据：

- RED：新增 `ClubPointRedemptionControllerTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionControllerTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `controller.admin.redemption`、`controller.app.redemption` 包、兑换 Controller 和 VO 不存在，符合 M9.8 RED 预期。
- GREEN：新增员工端 `/clubpoints/app/redemption` Controller 和管理员端 `/clubpoints/redemption-batch`、`/clubpoints/redemption-gift`、`/clubpoints/redemption-application` Controller；补齐 app/admin VO、批次/礼品/资格/申请分页 BO、Mapper 分页查询和 Service 读侧入口。
- 权限边界：员工开放批次、礼品和我的兑换为登录本人入口；提交兑换使用 `clubpoints:redemption:apply`，取消本人兑换使用 `clubpoints:redemption:cancel-own`；管理员批次和资格快照使用 `clubpoints:redemption-batch:manage`，礼品使用 `clubpoints:redemption-gift:manage`，审核使用 `clubpoints:redemption:review`。未新增负责人兑换审核入口，负责人不能审核兑换。
- API 文档补齐：`club-points-api-design.md` 原表缺少资格快照路径，M9.8 将管理员资格快照分页明确为 `GET /clubpoints/redemption-batch/eligibility-page`，权限为 `clubpoints:redemption-batch:manage`。
- 实现边界：Controller 只做登录态、权限注解、VO/BO 转换和响应映射；申请、冻结、库存锁、审核扣分、取消释放仍全部复用 M9.5-M9.7 Service；前端不传当前用户 ID、operator、globalScope、IP 或 UA；库存事实源仍是数据库条件更新，未引入 Redis。
- M9.8 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRedemptionControllerTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRedemptionControllerTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- M9 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRedemptionControllerTest,ClubPointRedemptionCancelServiceImplTest,ClubPointRedemptionMapperTest,ClubPointRedemptionBatchServiceImplTest,ClubPointRedemptionGiftServiceImplTest,ClubPointRedemptionEligibilityServiceImplTest,ClubPointRedemptionApplicationServiceImplTest,ClubPointRedemptionReviewServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `36` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中；clubpoints 主代码 Redis 库存事实源模式无命中；redemption 生产 Service 范围无直接写账本命中，测试账本命中仅为空实现测试替身方法签名。

## 任务 M9.9 测试

- [x] 测试批次状态机。
- [x] 测试资格快照生成。
- [x] 测试库存不足失败。
- [x] 测试积分不足失败。
- [x] 测试并发申请不超兑。
- [x] 测试重复申请幂等。
- [x] 测试审核通过扣分。
- [x] 测试审核拒绝释放。
- [x] 测试取消释放。
- [x] 测试跨年冻结释放口径。

验收：

- [x] 并发测试通过。
- [x] 冻结、库存、申请、流水一致。

证据：

- RED：运行 `mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRedemptionApplicationServiceImplTest#concurrentApplyShouldNotOversellAndRollbackFailedApplicant,ClubPointRedemptionCancelServiceImplTest#cancelCrossYearFrozenApplicationShouldReleaseBackWithoutExpiredClearing" "-Dsurefire.failIfNoSpecifiedTests=true" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `No tests matching pattern`，说明 M9.9 的并发申请入口和跨年冻结释放收口测试尚不存在。
- GREEN：在 `ClubPointRedemptionApplicationServiceImplTest` 增加 `concurrentApplyShouldNotOversellAndRollbackFailedApplicant`，两个合格员工并发申请库存为 1 的同一礼品，断言只有 1 个申请成功、1 个库存不足失败，最终只存在 1 条申请、1 条冻结、1 条库存锁，成功员工冻结 60 分，失败员工账户回滚为可用 100 分。
- GREEN：在 `ClubPointRedemptionCancelServiceImplTest` 增加 `cancelCrossYearFrozenApplicationShouldReleaseBackWithoutExpiredClearing`，模拟 2026-12-30 冻结、2027-01-01 年度清零后账户为 `net=60/frozen=60/available=0`，2027-01-03 取消释放后账户变为 `net=60/frozen=0/available=60`，不生成兑换扣减或过期清零流水。
- 收口复核：批次状态机和资格快照生成由 `ClubPointRedemptionBatchServiceImplTest` 覆盖；库存不足、积分不足、重复申请幂等和申请事务一致性由 `ClubPointRedemptionApplicationServiceImplTest` 覆盖；审核通过扣分和审核拒绝释放由 `ClubPointRedemptionReviewServiceImplTest` 覆盖；取消释放和超时释放由 `ClubPointRedemptionCancelServiceImplTest` 覆盖；礼品库存条件更新和礼品层并发不超兑由 `ClubPointRedemptionGiftServiceImplTest` 覆盖。
- M9.9 新增测试验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRedemptionApplicationServiceImplTest#concurrentApplyShouldNotOversellAndRollbackFailedApplicant,ClubPointRedemptionCancelServiceImplTest#cancelCrossYearFrozenApplicationShouldReleaseBackWithoutExpiredClearing" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；新增 2 个测试，失败 `0`，错误 `0`。
- M9 收口组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRedemptionControllerTest,ClubPointRedemptionCancelServiceImplTest,ClubPointRedemptionMapperTest,ClubPointRedemptionBatchServiceImplTest,ClubPointRedemptionGiftServiceImplTest,ClubPointRedemptionEligibilityServiceImplTest,ClubPointRedemptionApplicationServiceImplTest,ClubPointRedemptionReviewServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `38` 个测试，失败 `0`，错误 `0`。

## M9 放行标准

- [x] 批次可用。
- [x] 礼品可用。
- [x] 资格快照可用。
- [x] 申请冻结库存闭环可用。
- [x] 审核扣分和释放闭环可用。
- [x] 并发不超兑。

## M9 不通过时禁止

- [x] 禁止做兑换前端验收。
- [x] 禁止做 MVP 闭环演示。
