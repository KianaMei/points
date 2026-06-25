# M4 积分账本 Implementation Plan

**Status:** `[x]` M4.1-M4.8 已完成并有 RED/GREEN、组合测试和质量门禁证据；下一入口是 M5 俱乐部成员负责人闭环。

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立积分流水、账户缓存、冻结、撤销、调整、余额重算能力，让 `club_points_transaction` 成为唯一积分事实源。

**Architecture:** 所有积分变化只允许通过 LedgerService。流水写入、账户缓存更新、冻结变化在事务内保持一致；余额缓存可由流水重算，冻结不是流水。

**Tech Stack:** Spring 事务、MyBatis、数据库唯一键、JUnit、RuoYi PageResult、强审计。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 流水是唯一积分事实源。
- 不能直接改积分余额。
- 不能用 Redis 当事实源。
- 重复发分、扣分、撤销、清零必须靠数据库唯一键兜底。
- Java 行为必须 TDD。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/ledger/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/ledger/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointLedgerService.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/app/ledger/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/ledger/`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ErrorCodeConstants.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/ledger/`

**Interfaces:**

- Consumes: M1 账本表、M3 规则快照、M2 强审计。
- Produces: 正向流水、负向流水、冻结、释放、冻结转扣减、撤销、调整、余额重算接口。

**Verification:**

- Run: LedgerService 单测。
- Expected: 重复幂等键不重复流水，扣分不产生非法余额，冻结释放转扣减一致。
- Run: 调整强审计失败回滚测试。
- Expected: 审计失败时流水和账户缓存都不提交。

## 目标

建立积分流水、账户缓存、冻结、撤销、调整、余额重算能力。`club_points_transaction` 是唯一积分事实源。

## 前置条件

- M3 已放行。
- 当前已发布规则可读取。
- 强审计可用。
- 数据库唯一键已落地。

## 任务 M4.1 DO 和 Mapper

- [x] 创建 `ClubPointTransactionDO`。
- [x] 创建 `ClubPointAccountDO`。
- [x] 创建 `ClubPointFreezeDO`。
- [x] 创建 `ClubPointUserYearStatusDO`。
- [x] 创建对应 Mapper。
- [x] DO 字段和 DDL 保持一致。

验收：

- [x] DO 继承 `BaseDO`。
- [x] Mapper 继承 `BaseMapperX<T>`。
- [x] 不直接暴露余额修改 Mapper 给业务层乱用。

证据：

- RED：新增 `ClubPointLedgerMapperTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointLedgerMapperTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `dal.dataobject.ledger` 包和四个 Ledger Mapper 不存在。
- GREEN：新增 `ClubPointTransactionDO`、`ClubPointAccountDO`、`ClubPointFreezeDO`、`ClubPointUserYearStatusDO`，新增 `ClubPointTransactionMapper`、`ClubPointAccountMapper`、`ClubPointFreezeMapper`、`ClubPointUserYearStatusMapper`。
- 修复记录：H2 单测环境中 `year` 是保留字，`ClubPointUserYearStatusDO.year` 使用 ``@TableField("`year`")`` 显式映射，保持正式 DDL 字段名不漂移。
- M4.1 单测验证：同一命令返回 `BUILD SUCCESS`；`ClubPointLedgerMapperTest` 运行 `1` 个测试，失败 `0`，错误 `0`。
- 边界：本任务只提供基础查询 Mapper，不实现余额变更业务方法；后续积分变动必须从 M4.3 `LedgerService` 统一进入。

## 任务 M4.2 枚举和错误码

- [x] 创建流水类型枚举。
- [x] 创建来源类型枚举。
- [x] 创建冻结状态枚举。
- [x] 创建年度状态枚举。
- [x] 补充错误码：余额不足、冻结不足、流水重复、流水不存在、年度已清零、撤销非法。

验收：

- [x] 所有账本状态都有枚举。
- [x] 错误码归入 `clubpoints` 段。

证据：

- RED：新增 `ClubPointLedgerEnumTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointLedgerEnumTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointTransactionDirectionEnum`、`ClubPointTransactionStatusEnum`、`ClubPointTransactionSourceTypeEnum`、`ClubPointFreezeStatusEnum`、`ClubPointCategoryEnum`、`ClubPointAnnualClearingStatusEnum` 和 6 个 `CLUB_LEDGER_*` 错误码不存在。
- GREEN：新增流水方向、流水状态、流水来源类型、冻结状态、积分分类、年度清零状态 6 个枚举，并补 `CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH`、`CLUB_LEDGER_FROZEN_POINTS_NOT_ENOUGH`、`CLUB_LEDGER_TRANSACTION_DUPLICATED`、`CLUB_LEDGER_TRANSACTION_NOT_EXISTS`、`CLUB_LEDGER_YEAR_ALREADY_CLEARED`、`CLUB_LEDGER_REVERSE_INVALID`。
- M4.2 单测验证：同一命令返回 `BUILD SUCCESS`；`ClubPointLedgerEnumTest` 运行 `7` 个测试，失败 `0`，错误 `0`。
- M4 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointLedgerMapperTest,ClubPointLedgerEnumTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `8` 个测试，失败 `0`，错误 `0`。
- 决策：`club_points_user_year_status` 没有 `status` 字段，M4.2 的“年度状态枚举”按已有 `club_points_annual_clearing_status` 字典落地为 `ClubPointAnnualClearingStatusEnum`，不硬造年度状态字段或无事实源枚举。

## 任务 M4.3 LedgerService 写流水

- [x] 创建 `service/ledger/ClubPointLedgerService.java`。
- [x] 创建 `service/ledger/ClubPointLedgerServiceImpl.java`。
- [x] 实现正向积分流水。
- [x] 实现负向积分流水。
- [x] 实现 idempotency key 唯一冲突处理。
- [x] 同事务更新账户缓存。
- [x] 写入规则快照。
- [x] 写入来源快照。
- [x] 禁止外部直接改账户余额。

验收：

- [x] 重复请求不会重复发分。
- [x] 负向扣分不会把可用余额扣成负数。
- [x] 流水和账户缓存同事务。

证据：

- RED：新增 `ClubPointLedgerServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointLedgerServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointLedgerService` 和 `ClubPointLedgerCreateReqBO` 不存在。
- GREEN：新增 `ClubPointLedgerService`、`ClubPointLedgerServiceImpl`、`ClubPointLedgerCreateReqBO`；`ClubPointAccountMapper` 增加 `selectByUserIdForUpdate`；`createTransaction` 只追加流水并同事务更新账户缓存。
- M4.3 首轮 GREEN 调试：首次实现后测试上下文未导入 `ClubPointLedgerServiceImpl`，按既有 Service 单测模式补 `@Import({ClubPointLedgerServiceImpl.class, ClubPointRuleServiceImpl.class, ClubAuditServiceImpl.class})`，不启用全量扫描。
- M4.3 单测验证：同一命令返回 `BUILD SUCCESS`；`ClubPointLedgerServiceImplTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- M4 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointLedgerMapperTest,ClubPointLedgerEnumTest,ClubPointLedgerServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `13` 个测试，失败 `0`，错误 `0`。
- 边界：本任务不实现冻结、撤销、调整、查询 API；这些仍分别属于 M4.4-M4.7。

## 任务 M4.4 冻结服务

### Task M4.4: 冻结服务

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointFreezeService.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointFreezeServiceImpl.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointFreezeCreateReqBO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointFreezeReleaseReqBO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointFreezeConvertReqBO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ClubPointFreezeSourceTypeEnum.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/ledger/ClubPointFreezeMapper.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ErrorCodeConstants.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointFreezeServiceImplTest.java`

**Interfaces:**

- Consumes: `club_points_freeze`、`club_points_point_account`、`ClubPointFreezeMapper`、`ClubPointAccountMapper`、`ClubPointLedgerService.createTransaction(...)`、`ClubPointFreezeStatusEnum`、`ClubPointTransactionSourceTypeEnum.REDEMPTION`
- Produces: `freezePoints(...)`、`releaseFreeze(...)`、`convertFreezeToDeduction(...)`，供后续 M9 兑换申请和兑换审核复用
- Decision: `club_points_freeze.source_type` 是冻结来源枚举，兑换冻结按数据库设计取 `1`；它不是 `ClubPointTransactionSourceTypeEnum.REDEMPTION(4)`。冻结转扣减生成流水时才使用流水来源类型 `REDEMPTION(4)`。

- [x] RED: 写失败测试或失败验证
- [x] Verify RED: 运行命令，确认失败原因正确
- [x] GREEN: 写最小实现
- [x] Verify GREEN: 运行命令，确认通过
- [x] REFACTOR: 只在绿色后清理命名、重复、结构
- [x] Checkpoint: 列出变更文件和验证证据

RED 证据：

- 新增 `ClubPointFreezeServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointFreezeServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败。
- 失败原因为 `ClubPointFreezeService`、`ClubPointFreezeServiceImpl`、`ClubPointFreezeCreateReqBO`、`ClubPointFreezeReleaseReqBO`、`ClubPointFreezeConvertReqBO`、`ClubPointFreezeSourceTypeEnum` 和冻结错误码不存在，符合 M4.4 RED 预期。

GREEN 证据：

- GREEN：新增 `ClubPointFreezeService`、`ClubPointFreezeServiceImpl`、`ClubPointFreezeCreateReqBO`、`ClubPointFreezeReleaseReqBO`、`ClubPointFreezeConvertReqBO` 和 `ClubPointFreezeSourceTypeEnum`；`ClubPointFreezeMapper` 增加 `selectByIdForUpdate`；`ErrorCodeConstants` 补冻结重复、不存在、状态非法错误码。
- 实现边界：冻结只更新 `club_points_freeze` 和账户缓存；释放冻结不生成流水；冻结转扣减先释放冻结占用，再复用 `ClubPointLedgerService.createTransaction(...)` 生成兑换负向流水。
- M4.4 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointFreezeServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointFreezeServiceImplTest` 运行 `7` 个测试，失败 `0`，错误 `0`。
- M4 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointLedgerMapperTest,ClubPointLedgerEnumTest,ClubPointLedgerServiceImplTest,ClubPointFreezeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `20` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` exit `0`，仅 CRLF 提示；租户字段、租户基类和 AI 元数据模式检查均无命中；生产账户缓存更新入口只在 `ClubPointLedgerServiceImpl` 和 `ClubPointFreezeServiceImpl`。

- [x] 实现冻结积分。
- [x] 实现释放冻结。
- [x] 实现冻结转扣减。
- [x] 冻结操作关联来源类型和来源 ID。
- [x] 冻结转扣减生成负向流水。
- [x] 释放冻结不生成扣减流水。

验收：

- [x] 可用积分减少，冻结积分增加。
- [x] 审核拒绝释放冻结后可用积分恢复。
- [x] 审核通过转扣减后冻结归零并生成流水。

## 任务 M4.5 撤销和调整

### Task M4.5: 撤销和调整

**Files:**

- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointLedgerService.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointLedgerServiceImpl.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointLedgerReverseReqBO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointLedgerAdjustReqBO.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/ledger/ClubPointTransactionMapper.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ClubAuditActionTypeConstants.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ErrorCodeConstants.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointLedgerAdjustmentServiceImplTest.java`

**Interfaces:**

- Consumes: `ClubPointLedgerService.createTransaction(...)`、`club_points_transaction`、`club_points_point_account`、`ClubPointTransactionMapper`、`ClubPointAccountMapper`、`ClubAuditService.createAuditLog(...)`、`ClubPointRuleResolveService`、`ClubPointTransactionStatusEnum`
- Produces: `reverseTransaction(...)` 和 `adjustPoints(...)`，供管理员撤销流水、管理员手工调整和后续 M10 异议处理复用
- Decision: 撤销以新增反向流水为唯一事实修正，不删除原流水、不修改原流水积分字段；是否已撤销可由 `reverse_of_transaction_id` 关联查询判断，避免“排除原流水 + 新增反向流水”双重抵消。
- Decision: M4.5 的附件要求先以 `attachmentSnapshotJson` 作为强制材料快照进入流水/审计；真实附件绑定由后续 Admin API 或业务入口使用 M2 `ClubAttachmentService` 绑定并锁定。

- [x] RED: 写失败测试或失败验证
- [x] Verify RED: 运行命令，确认失败原因正确
- [x] GREEN: 写最小实现
- [x] Verify GREEN: 运行命令，确认通过
- [x] REFACTOR: 只在绿色后清理命名、重复、结构
- [x] Checkpoint: 列出变更文件和验证证据

RED 证据：

- 新增 `ClubPointLedgerAdjustmentServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointLedgerAdjustmentServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败。
- 失败原因为 `ClubPointLedgerReverseReqBO`、`ClubPointLedgerAdjustReqBO`、`POINT_REVERSE` 和 `CLUB_LEDGER_ADJUST_INVALID` 不存在，符合 M4.5 RED 预期。

GREEN 证据：

- GREEN：新增 `ClubPointLedgerReverseReqBO`、`ClubPointLedgerAdjustReqBO`，`ClubPointLedgerService` 增加 `reverseTransaction(...)` 和 `adjustPoints(...)`；`ClubPointLedgerServiceImpl` 实现撤销反向流水、管理员调整、幂等键、强审计和账户缓存同事务更新。
- Mapper/常量：`ClubPointTransactionMapper` 增加 `selectByIdForUpdate(...)`；`ClubAuditActionTypeConstants` 增加 `POINT_REVERSE`；`ErrorCodeConstants` 增加 `CLUB_LEDGER_ADJUST_INVALID`。
- M4.5 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointLedgerAdjustmentServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointLedgerAdjustmentServiceImplTest` 运行 `6` 个测试，失败 `0`，错误 `0`。
- M4 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointLedgerMapperTest,ClubPointLedgerEnumTest,ClubPointLedgerServiceImplTest,ClubPointFreezeServiceImplTest,ClubPointLedgerAdjustmentServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `26` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` exit `0`，仅 CRLF 提示；租户字段、租户基类和 AI 元数据模式检查均无命中；生产账户缓存更新入口仍只在 `ClubPointLedgerServiceImpl` 和 `ClubPointFreezeServiceImpl`。
- 边界：撤销新增 `status=REVERSAL` 的反向流水，原流水仍保持原积分、方向和 `VALID` 状态；调整先写强审计，再通过 `createTransaction(...)` 追加调整流水，不绕开账本。

- [x] 实现按原流水撤销。
- [x] 撤销只能生成反向流水。
- [x] 原流水不删除不修改积分。
- [x] 实现管理员调整积分。
- [x] 调整必须写强审计。
- [x] 调整必须带原因和附件。

验收：

- [x] 同一原流水只能撤销一次。
- [x] 撤销有 idempotency key。
- [x] 调整不绕过账本。

## 任务 M4.6 余额重算

### Task M4.6: 余额重算

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/job/ClubJobRunDO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/job/ClubJobRunMapper.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointAccountRebuildReqBO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointAccountRebuildAllReqBO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointAccountRebuildServiceImplTest.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointLedgerService.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointLedgerServiceImpl.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/ledger/ClubPointAccountMapper.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/ledger/ClubPointTransactionMapper.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/ledger/ClubPointFreezeMapper.java`

**Interfaces:**

- Consumes: `club_points_transaction`、`club_points_freeze`、`club_points_point_account`、`club_points_job_run`、`ClubPointTransactionStatusEnum`、`ClubPointFreezeStatusEnum`
- Produces: `ClubPointLedgerService.rebuildUserAccount(...)`、`ClubPointLedgerService.rebuildAllAccounts(...)`，供管理员修复账户缓存、年度维护和后续任务监控复用
- Decision: `club_points_transaction` 是唯一积分事实源，重算只从有效流水和冻结事实源重建 `club_points_point_account` 缓存；`club_points_job_run` 记录重算结果，不依赖 `infra_job_log` 当业务事实。

- [x] RED: 写失败测试或失败验证
- [x] Verify RED: 运行命令，确认失败原因正确
- [x] GREEN: 写最小实现
- [x] Verify GREEN: 运行命令，确认通过
- [x] REFACTOR: 只在绿色后清理命名、重复、结构
- [x] Checkpoint: 列出变更文件和验证证据，不提交 git

RED 证据：

- 新增 `ClubPointAccountRebuildServiceImplTest`，覆盖单用户缓存修复、全量缓存修复、冻结中积分重算、已撤销流水排除、撤销流水纳入、流水总数不变和 `club_points_job_run` 写入。
- RED 命令：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointAccountRebuildServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`。
- 失败原因为 `dal.dataobject.job`、`dal.mysql.job`、`ClubPointAccountRebuildReqBO`、`ClubPointAccountRebuildAllReqBO`、`ClubJobRunMapper` 等 M4.6 产物不存在，符合 RED 预期。

GREEN 证据：

- GREEN：新增 `ClubJobRunDO`、`ClubJobRunMapper`、`ClubPointAccountRebuildReqBO`、`ClubPointAccountRebuildAllReqBO`；`ClubPointLedgerService` 增加单用户和全量账户重算入口；`ClubPointLedgerServiceImpl` 从流水和冻结事实源重建账户缓存并写任务运行记录。
- Mapper：`ClubPointAccountMapper` 增加账户列表和按用户查询，`ClubPointTransactionMapper` 增加有效重算流水和用户集合查询，`ClubPointFreezeMapper` 增加冻结中积分和用户集合查询。
- M4.6 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointAccountRebuildServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointAccountRebuildServiceImplTest` 运行 `2` 个测试，失败 `0`，错误 `0`。
- M4 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointLedgerMapperTest,ClubPointLedgerEnumTest,ClubPointLedgerServiceImplTest,ClubPointFreezeServiceImplTest,ClubPointLedgerAdjustmentServiceImplTest,ClubPointAccountRebuildServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `28` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` exit `0`，仅 CRLF 提示；租户字段、租户基类和 AI 元数据模式检查均无命中。
- 边界：重算会更新账户缓存，但不新增、不删除、不修改任何积分流水；`status=VALID` 和 `status=REVERSAL` 参与重算，`status=REVERSED` 不参与；仅 `FROZEN` 状态冻结记录计入冻结积分。

- [x] 实现按用户和年度重算账户缓存。
- [x] 实现按全量用户重算账户缓存。
- [x] 重算只读流水事实源。
- [x] 重算结果写 `club_points_job_run`。

验收：

- [x] 删除或修改缓存后可从流水恢复。
- [x] 重算不改变流水。

## 任务 M4.7 查询 API

### Task M4.7: 查询 API

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointLedgerQueryService.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/ClubPointLedgerQueryServiceImpl.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointLedgerPageReqBO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointAccountPageReqBO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointLedgerSummaryBO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/ledger/bo/ClubPointLedgerMemberSummaryBO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/app/ledger/ClubPointLedgerAppController.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/app/ledger/vo/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/leader/ledger/ClubPointLedgerLeaderController.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/leader/ledger/vo/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/ledger/ClubPointLedgerAdminController.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/ledger/vo/`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/ledger/ClubPointTransactionMapper.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/ledger/ClubPointAccountMapper.java`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/club/ClubMemberMapper.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/controller/ledger/ClubPointLedgerQueryControllerTest.java`

**Interfaces:**

- Consumes: `club_points_transaction`、`club_points_point_account`、`club_points_club_member`、`club_points_club_leader`、`ClubScopeService.validateSelf(...)`、`ClubScopeService.validateManagedClub(...)`、`ClubPointTransactionStatusEnum`、`ClubPointCategoryEnum`
- Produces:
  - `GET /clubpoints/app/ledger/summary`
  - `GET /clubpoints/app/ledger/page`
  - `GET /clubpoints/leader/ledger/member-summary-page`
  - `GET /clubpoints/leader/ledger/transaction-page`
  - `GET /clubpoints/ledger/account-page`
  - `GET /clubpoints/ledger/transaction-page`
- Decision: 负责人不是全局账本角色；负责人只能查看自己负责俱乐部作为 `issuing_club_id` 发出的积分流水，以及该负责俱乐部成员在本俱乐部来源下汇总出的积分摘要。负责人不得查看同一员工在其他俱乐部或无俱乐部来源下的积分流水、原因、材料或来源快照。
- Decision: 员工端账本接口不接收 `userId`；当前登录人从 `SecurityFrameworkUtils.getLoginUserId()` 获取。
- Decision: 管理员查询使用 `clubpoints:ledger:query`，员工自助账本按登录态本人范围，负责人账本查询使用 `clubpoints:leader` 并由服务层强制 `validateManagedClub(...)`。

- [x] RED: 写失败测试或失败验证
- [x] Verify RED: 运行命令，确认失败原因正确
- [x] GREEN: 写最小实现
- [x] Verify GREEN: 运行命令，确认通过
- [x] REFACTOR: 只在绿色后清理命名、重复、结构
- [x] Checkpoint: 列出变更文件和验证证据

RED 证据：

- 新增 `ClubPointLedgerQueryControllerTest`，覆盖员工本人账本、负责人负责俱乐部发放来源账本、管理员全局账本和接口路径/权限声明。
- RED 命令：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointLedgerQueryControllerTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`。
- 失败原因为 `controller.admin.ledger`、`controller.app.ledger`、`controller.leader.ledger` 和 `ClubPointLedgerQueryServiceImpl` 等查询 API 产物不存在，符合 M4.7 RED 预期。

GREEN 证据：

- GREEN：新增员工端、负责人端、管理员端账本查询 Controller、VO、`ClubPointLedgerQueryService`、读侧 BO；补充 `ClubPointTransactionMapper`、`ClubPointAccountMapper`、`ClubMemberMapper` 查询能力。
- 权限范围：员工端从登录态取本人；负责人端以 `validateManagedClub(...)` 和 `issuing_club_id` 为硬边界；管理员端使用 `clubpoints:ledger:query` 全局查询。
- M4.7 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointLedgerQueryControllerTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointLedgerQueryControllerTest` 运行 `4` 个测试，失败 `0`，错误 `0`。
- M4 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointLedgerMapperTest,ClubPointLedgerEnumTest,ClubPointLedgerServiceImplTest,ClubPointFreezeServiceImplTest,ClubPointLedgerAdjustmentServiceImplTest,ClubPointAccountRebuildServiceImplTest,ClubPointLedgerQueryControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `33` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` exit `0`，仅 CRLF 提示；源码范围 `tenant_id|TenantBaseDO` 无命中；源码范围 AI/co-author 元数据模式无命中；账户缓存写入口仍只在 `ClubPointLedgerServiceImpl` 和 `ClubPointFreezeServiceImpl`，查询服务只读账户缓存。

验收：

- [x] 员工查询本人积分明细。
- [x] 员工查询本人余额。
- [x] 负责人查询负责俱乐部成员积分摘要。
- [x] 负责人查询负责俱乐部作为发放俱乐部的积分流水。
- [x] 负责人不能看非负责俱乐部流水。
- [x] 负责人不能看负责俱乐部成员在其他俱乐部或无俱乐部来源下的流水来源。
- [x] 管理员查询全局积分流水。
- [x] 管理员查询账户缓存。
- [x] 所有敏感读接口校验数据范围。
- [x] 员工不能看别人流水。
- [x] 管理员可以全局查询。

## 任务 M4.8 测试

- [x] 测试正向发分。
- [x] 测试负向扣分。
- [x] 测试重复 idempotency key。
- [x] 测试余额不足。
- [x] 测试冻结、释放、转扣减。
- [x] 测试撤销。
- [x] 测试调整强审计失败回滚。
- [x] 测试余额重算。

验收：

- [x] 账本核心测试通过。
- [x] 并发重复请求不会重复流水。

证据：

- 正向发分、负向扣分、重复幂等键、余额不足由 `ClubPointLedgerServiceImplTest` 覆盖。
- 并发重复幂等键由 `ClubPointLedgerServiceImplTest#createDuplicateIdempotencyConcurrentlyShouldPersistOnce` 覆盖，单方法验证返回 `BUILD SUCCESS`；测试 `1` 个，失败 `0`，错误 `0`。
- 冻结、释放、转扣减由 `ClubPointFreezeServiceImplTest` 覆盖。
- 撤销和调整强审计失败回滚由 `ClubPointLedgerAdjustmentServiceImplTest` 覆盖。
- 余额重算由 `ClubPointAccountRebuildServiceImplTest` 覆盖。
- M4 收口组合验证返回 `BUILD SUCCESS`；合计 `33` 个测试，失败 `0`，错误 `0`。

## M4 放行标准

- [x] 所有积分变动都只能通过 LedgerService。
- [x] 流水是唯一事实源。
- [x] 账户缓存可重算。
- [x] 冻结闭环可用。
- [x] 撤销和调整闭环可用。

放行证据：

- 生产账户缓存写入口检查只命中 `ClubPointLedgerServiceImpl` 和 `ClubPointFreezeServiceImpl`；`ClubPointLedgerQueryServiceImpl` 只读 `availablePoints`、`frozenPoints`。
- `ClubPointAccountRebuildServiceImplTest` 验证删除或修改账户缓存后可从有效流水和冻结事实源恢复，且重算不改变流水。
- `ClubPointFreezeServiceImplTest` 验证冻结减少可用并增加冻结、释放恢复可用且不生成流水、冻结转扣减生成兑换负向流水。
- `ClubPointLedgerAdjustmentServiceImplTest` 验证撤销只能生成反向流水、原流水事实不改、管理员调整写强审计且审计失败回滚。
- M4 收口组合验证和质量门禁均已通过；未跑 full build。

## M4 不通过时禁止

- [x] 禁止写活动结算。
- [x] 禁止写非签到审核通过发分。
- [x] 禁止写兑换冻结和扣分。

执行记录：M4 放行前未进入 M5-M9 业务实现；本次只完成 M4 账本查询 API、测试收口、文档同步和放行记录。
