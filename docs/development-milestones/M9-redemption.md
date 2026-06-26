# M9 兑换闭环 Implementation Plan

**Status:** `[~]` M9.4 已完成并有 RED/GREEN 证据；当前入口是 M9.5 申请 Service。

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

- [ ] 员工查看可兑换礼品。
- [ ] 员工提交兑换申请。
- [ ] 校验批次状态。
- [ ] 校验资格快照。
- [ ] 校验礼品状态。
- [ ] 校验可用积分。
- [ ] 同事务冻结积分。
- [ ] 同事务锁定库存。
- [ ] 同事务创建申请。
- [ ] 使用 requestNo 或 idempotency key 防重复提交。

验收：

- [ ] 申请成功后可用积分减少，冻结积分增加。
- [ ] 礼品已锁库存增加。
- [ ] 重复提交返回同一结果或被幂等拦截。

## 任务 M9.6 审核 Service

- [ ] 管理员查看待审核申请。
- [ ] 管理员审核通过。
- [ ] 审核通过冻结转扣减。
- [ ] 审核通过库存锁转已兑。
- [ ] 审核通过写兑换负向流水。
- [ ] 管理员审核拒绝。
- [ ] 审核拒绝释放冻结。
- [ ] 审核拒绝释放库存锁。
- [ ] 审核写强审计。
- [ ] 通知员工审核结果。

验收：

- [ ] 通过后积分真正扣减。
- [ ] 拒绝后积分和库存恢复。
- [ ] 重复审核不重复扣分。

## 任务 M9.7 取消和超时

- [ ] 员工在允许状态取消申请。
- [ ] 取消释放冻结。
- [ ] 取消释放库存锁。
- [ ] 超时未审核按规则处理。
- [ ] 取消和超时写记录。

验收：

- [ ] 取消不生成扣减流水。
- [ ] 释放后库存和积分一致。

## 任务 M9.8 API

- [ ] 管理员批次管理接口。
- [ ] 管理员礼品管理接口。
- [ ] 管理员资格快照接口。
- [ ] 管理员审核接口。
- [ ] 员工可兑换列表接口。
- [ ] 员工提交申请接口。
- [ ] 员工申请记录接口。
- [ ] 员工取消申请接口。

验收：

- [ ] API 路径和 `club-points-api-design.md` 一致。
- [ ] 负责人不能审核兑换。

## 任务 M9.9 测试

- [ ] 测试批次状态机。
- [ ] 测试资格快照生成。
- [ ] 测试库存不足失败。
- [ ] 测试积分不足失败。
- [ ] 测试并发申请不超兑。
- [ ] 测试重复申请幂等。
- [ ] 测试审核通过扣分。
- [ ] 测试审核拒绝释放。
- [ ] 测试取消释放。
- [ ] 测试跨年冻结释放口径。

验收：

- [ ] 并发测试通过。
- [ ] 冻结、库存、申请、流水一致。

## M9 放行标准

- [ ] 批次可用。
- [ ] 礼品可用。
- [ ] 资格快照可用。
- [ ] 申请冻结库存闭环可用。
- [ ] 审核扣分和释放闭环可用。
- [ ] 并发不超兑。

## M9 不通过时禁止

- [ ] 禁止做兑换前端验收。
- [ ] 禁止做 MVP 闭环演示。
