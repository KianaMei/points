# M4 积分账本 Implementation Plan

**Status:** `[~]` M4.1 已完成并有 RED/GREEN 证据；当前入口是 M4.2 枚举和错误码。

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

- [ ] 创建流水类型枚举。
- [ ] 创建来源类型枚举。
- [ ] 创建冻结状态枚举。
- [ ] 创建年度状态枚举。
- [ ] 补充错误码：余额不足、冻结不足、流水重复、流水不存在、年度已清零、撤销非法。

验收：

- [ ] 所有账本状态都有枚举。
- [ ] 错误码归入 `clubpoints` 段。

## 任务 M4.3 LedgerService 写流水

- [ ] 创建 `service/ledger/ClubPointLedgerService.java`。
- [ ] 创建 `service/ledger/ClubPointLedgerServiceImpl.java`。
- [ ] 实现正向积分流水。
- [ ] 实现负向积分流水。
- [ ] 实现 idempotency key 唯一冲突处理。
- [ ] 同事务更新账户缓存。
- [ ] 写入规则快照。
- [ ] 写入来源快照。
- [ ] 禁止外部直接改账户余额。

验收：

- [ ] 重复请求不会重复发分。
- [ ] 负向扣分不会把可用余额扣成负数。
- [ ] 流水和账户缓存同事务。

## 任务 M4.4 冻结服务

- [ ] 实现冻结积分。
- [ ] 实现释放冻结。
- [ ] 实现冻结转扣减。
- [ ] 冻结操作关联来源类型和来源 ID。
- [ ] 冻结转扣减生成负向流水。
- [ ] 释放冻结不生成扣减流水。

验收：

- [ ] 可用积分减少，冻结积分增加。
- [ ] 审核拒绝释放冻结后可用积分恢复。
- [ ] 审核通过转扣减后冻结归零并生成流水。

## 任务 M4.5 撤销和调整

- [ ] 实现按原流水撤销。
- [ ] 撤销只能生成反向流水。
- [ ] 原流水不删除不修改积分。
- [ ] 实现管理员调整积分。
- [ ] 调整必须写强审计。
- [ ] 调整必须带原因和附件。

验收：

- [ ] 同一原流水只能撤销一次。
- [ ] 撤销有 idempotency key。
- [ ] 调整不绕过账本。

## 任务 M4.6 余额重算

- [ ] 实现按用户和年度重算账户缓存。
- [ ] 实现按全量用户重算账户缓存。
- [ ] 重算只读流水事实源。
- [ ] 重算结果写 `club_points_job_run`。

验收：

- [ ] 删除或修改缓存后可从流水恢复。
- [ ] 重算不改变流水。

## 任务 M4.7 查询 API

- [ ] 员工查询本人积分明细。
- [ ] 员工查询本人余额。
- [ ] 负责人查询负责俱乐部成员积分摘要。
- [ ] 管理员查询全局积分流水。
- [ ] 管理员查询账户缓存。
- [ ] 所有敏感读接口校验数据范围。

验收：

- [ ] 员工不能看别人流水。
- [ ] 负责人不能看非负责俱乐部流水。
- [ ] 管理员可以全局查询。

## 任务 M4.8 测试

- [ ] 测试正向发分。
- [ ] 测试负向扣分。
- [ ] 测试重复 idempotency key。
- [ ] 测试余额不足。
- [ ] 测试冻结、释放、转扣减。
- [ ] 测试撤销。
- [ ] 测试调整强审计失败回滚。
- [ ] 测试余额重算。

验收：

- [ ] 账本核心测试通过。
- [ ] 并发重复请求不会重复流水。

## M4 放行标准

- [ ] 所有积分变动都只能通过 LedgerService。
- [ ] 流水是唯一事实源。
- [ ] 账户缓存可重算。
- [ ] 冻结闭环可用。
- [ ] 撤销和调整闭环可用。

## M4 不通过时禁止

- [ ] 禁止写活动结算。
- [ ] 禁止写非签到审核通过发分。
- [ ] 禁止写兑换冻结和扣分。
