# M8 非签到积分、违规扣分、弄虚作假 Implementation Plan

**Status:** `[~]` M8.1-M8.5 已完成并有 RED/GREEN 证据；当前入口是 M8.6 违规扣分。

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现负责人提交非签到材料、管理员审核发分、管理员代录、违规扣分、严重违规和弄虚作假处理。

**Architecture:** 非签到材料保存材料主表和明细，审核通过后每条明细通过 LedgerService 生成流水。违规和弄虚作假不得直接改原流水，只能走扣分、撤销或调整流水。

**Tech Stack:** Spring 事务、MyBatis、LedgerService、附件锁定、强审计、JUnit。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 所有发分和扣分必须走 LedgerService。
- 分值必须按规则项区间校验。
- 审核通过、代录、违规、弄虚作假必须强审计。
- Java 行为必须 TDD。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/contribution/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/contribution/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/contribution/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/leader/contribution/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/contribution/`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/contribution/`

**Interfaces:**

- Consumes: M3 规则项、M4 LedgerService、M2 附件锁定和强审计、M5 负责人范围。
- Produces: 非签到材料审核发分、管理员代录、违规扣分、弄虚作假处理能力。

**Verification:**

- Run: 非签到审核单测。
- Expected: 审核通过按明细生成流水，重复审核不重复发分。
- Run: 弄虚作假处理测试。
- Expected: 原流水反向撤销，额外扣分通过 LedgerService 生成。

## 目标

实现负责人提交非签到材料、管理员审核发分、违规扣分、严重违规和弄虚作假处理。

## 前置条件

- M7 已放行。
- LedgerService 可用。
- 规则读取可用。
- 附件锁定可用。
- 强审计可用。

## 任务 M8.1 DO 和 Mapper

- [x] 创建 `ClubPointContributionMaterialDO`。
- [x] 创建 `ClubPointContributionItemDO`。
- [x] 创建 `ClubPointContributionReviewRecordDO`。
- [x] 创建对应 Mapper。
- [x] 字段和 M1 DDL 一致。
- [x] 材料和明细分开保存。

验收：

- [x] 一个材料支持多条积分明细。
- [x] 审核记录可追溯。
- [x] 附件绑定可追溯。

证据：

- RED：新增 `ClubPointContributionMapperTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointContributionMapperTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution` 包、`ClubPointContributionMaterialMapper`、`ClubPointContributionItemMapper` 和 `ClubPointContributionReviewRecordMapper` 不存在，符合 M8.1 RED 预期。
- GREEN：新增非签到材料、材料明细和材料审核记录 3 个 DO；新增对应 Mapper，并提供按 `requestNo`、`materialId`、`idempotencyKey`、`effectiveUniqueKey` 查询方法；字段按 M1 DDL 映射，DO 继承 `BaseDO`，未新增 `tenant_id` 或 `TenantBaseDO`。
- 实现边界：M8.1 只落 DO/Mapper，不实现状态机、Service、Ledger 发分扣分、审核逻辑或 API；附件实际绑定继续复用 M2 `club_points_attachment_ref`，本次测试用 `BIZ_TYPE_CONTRIBUTION_MATERIAL + materialId` 验证附件可追溯。
- M8.1 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointContributionMapperTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointContributionMapperTest` 运行 `1` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中。

## 任务 M8.2 状态机和错误码

- [x] 定义材料草稿状态。
- [x] 定义待审核状态。
- [x] 定义审核通过状态。
- [x] 定义审核驳回状态。
- [x] 定义已作废状态。
- [x] 补充错误码：材料不存在、状态不允许、规则越界、附件缺失、重复提交、无权限审核。

验收：

- [x] 状态跳转受控。
- [x] 审核后材料不可随意修改。

证据：

- RED：新增 `ClubPointContributionEnumTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointContributionEnumTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `ClubPointContributionMaterialStatusEnum` 和 6 个 contribution 错误码不存在，符合 M8.2 RED 预期。
- GREEN：新增 `ClubPointContributionMaterialStatusEnum`，与 seed 字典 `club_points_material_status` 对齐；补 `canTransitionTo(...)`、`canEditContent()`、`canReview()`、`canWithdraw()` 状态 guard；`ErrorCodeConstants` 新增材料不存在、状态不允许、规则越界、附件缺失、重复提交、无权限审核 6 个错误码。
- 冲突处理：M8.2 清单写“已作废状态”，但 API 设计、流程设计和 seed 均为 `6=已删除/已删除快照`；本次按更具体的 API/seed 落为 `DELETED_SNAPSHOT(6)`，物理删除快照语义后续在材料删除任务中继续使用。
- M8.2 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointContributionEnumTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointContributionEnumTest` 运行 `4` 个测试，失败 `0`，错误 `0`。
- M8 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointContributionMapperTest,ClubPointContributionEnumTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `5` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中。

## 任务 M8.3 负责人提交材料

- [x] 负责人创建材料草稿。
- [x] 负责人添加积分明细。
- [x] 负责人上传附件。
- [x] 负责人提交审核。
- [x] 校验负责人数据范围。
- [x] 校验规则项存在。
- [x] 校验录入分值在规则区间内。
- [x] 提交后锁定提交内容。

验收：

- [x] 负责人不能给非负责俱乐部提交材料。
- [x] 分值越界失败。
- [x] 附件缺失按规则失败。

证据：

- RED：新增 `ClubPointContributionServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointContributionServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `ClubPointContributionService` / `ClubPointContributionServiceImpl`、材料保存/提交 BO 和 `ClubPointContributionMaterialTypeEnum` 不存在，符合 M8.3 RED 预期。
- GREEN：新增 `ClubPointContributionService` / `ClubPointContributionServiceImpl`、`ClubPointContributionMaterialSaveReqBO`、`ClubPointContributionItemSaveReqBO`、`ClubPointContributionSubmitReqBO` 和 `ClubPointContributionMaterialTypeEnum`；负责人范围使用 `ClubScopeService.validateManagedClub(...)`，草稿保存材料主表和多条明细，附件通过 `ClubAttachmentService.bindAttachment(...)` 绑定到 `BIZ_TYPE_CONTRIBUTION_MATERIAL + materialId`，提交审核将状态从 `DRAFT(1)` 推进到 `PENDING_REVIEW(2)`。
- 规则边界：材料类型映射当前正向非签到积分规则项 `MONTHLY_DUTY`、`PLAN_EXECUTION`、`PUBLICITY_SUGGESTION`、`AWARD_REWARD`、`SPECIAL_CONTRIBUTION`；规则项缺失保留 `CLUB_RULE_ITEM_NOT_EXISTS`，分值越界转换为 contribution 专用 `CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE`。
- 实现边界：M8.3 不生成审核记录、不锁定附件、不调用 LedgerService、不写 `club_points_transaction`；提交后锁定按状态机语义实现为 `PENDING_REVIEW` 不允许 `updateDraft(...)` 修改内容，审核通过后的材料/附件实际锁定进入 M8.4。
- M8.3 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointContributionServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointContributionServiceImplTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- M8 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointContributionMapperTest,ClubPointContributionEnumTest,ClubPointContributionServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `10` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中；M8.3 contribution 主实现范围 `ClubPointLedgerService|createTransaction|club_points_transaction` 无命中。

## 任务 M8.4 管理员审核材料

- [x] 管理员查看待审核材料。
- [x] 管理员审核通过。
- [x] 管理员审核驳回。
- [x] 审核通过锁定附件。
- [x] 每条明细生成一条积分流水。
- [x] 审核通过写强审计。
- [x] 审核驳回写审核记录。

验收：

- [x] 审核通过生成流水。
- [x] 重复审核不会重复发分。
- [x] 审核失败不会生成半截流水。

证据：

- RED：在 `ClubPointContributionServiceImplTest` 增加 M8.4 审核用例后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointContributionServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `ClubPointContributionReviewReqBO`、`listPendingReviewMaterials(boolean)`、`reviewMaterial(...)` 和 `CONTRIBUTION_REVIEW` 不存在，符合 M8.4 RED 预期。
- GREEN：新增 `ClubPointContributionReviewReqBO`，`ClubPointContributionService` 增加待审核列表和审核方法；`ClubPointContributionMaterialMapper` 增加待审核状态查询和 `selectByIdForUpdate(...)`；`ClubAuditActionTypeConstants` 新增 `CONTRIBUTION_REVIEW`；`ClubPointContributionServiceImpl` 实现管理员全局范围审核、通过/驳回状态推进、审核记录、强审计、附件锁定和逐明细账本流水创建。
- 账本边界：审核通过每条明细只通过 `ClubPointLedgerService.createTransaction(...)` 创建流水，`sourceType=CONTRIBUTION_MATERIAL`，`sourceId=materialId`，`sourceItemId=itemId`，`idempotencyKey` 使用材料明细已有稳定键；实现不直接写 `club_points_transaction`。
- 事务边界：审核方法使用同一事务，强审计失败会回滚材料状态、审核记录、流水和附件锁定；测试用 `operatorNameSnapshot=null` 触发 `CLUB_AUDIT_WRITE_FAILED`，验证材料仍为 `PENDING_REVIEW`，无审核记录、无流水、无审计、附件未锁。
- M8.4 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointContributionServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointContributionServiceImplTest` 运行 `9` 个测试，失败 `0`，错误 `0`。
- M8 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointContributionMapperTest,ClubPointContributionEnumTest,ClubPointContributionServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `14` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中。

## 任务 M8.5 管理员代录

- [x] 管理员创建代录申请。
- [x] 管理员选择用户和规则项。
- [x] 管理员录入实际分值。
- [x] 校验规则区间。
- [x] 生成积分流水。
- [x] 使用前端 requestNo 或后端请求号做幂等。
- [x] 写强审计。

验收：

- [x] 重复提交不重复发分。
- [x] 代录原因必填。
- [x] 代录附件按规则绑定。

证据：

- RED：在 `ClubPointContributionServiceImplTest` 增加管理员代录用例后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointContributionServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`；失败原因是 `ClubPointContributionDirectCreateReqBO` 和 `CONTRIBUTION_DIRECT_CREATE` 不存在，符合 M8.5 RED 预期。
- GREEN：新增 `ClubPointContributionDirectCreateReqBO`，`ClubPointContributionService` 增加 `directCreate(...)`；`ClubAuditActionTypeConstants` 新增 `CONTRIBUTION_DIRECT_CREATE`；管理员代录要求全局范围，使用 `requestNo` 和 `DIRECT_CONTRIBUTION:{requestNo}` 幂等，创建已审核通过并锁定的代录材料、单条材料明细、附件绑定与锁定、强审计和账本流水。
- 账本边界：代录发分只通过 `ClubPointLedgerService.createTransaction(...)` 创建流水，`sourceType=ADMIN_DIRECT`，`sourceId=materialId`，`sourceItemId=itemId`；实现不直接写 `club_points_transaction`。
- 事务边界：代录方法使用同一事务，强审计失败会回滚材料、明细、附件、流水和审计；重复 `requestNo` 直接返回既有材料 ID，不重复发分。
- 实现边界：M8.5 只落 Service 能力，不新增 Controller/API；管理员代录接口进入 M8.8。
- M8.5 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointContributionServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointContributionServiceImplTest` 运行 `13` 个测试，失败 `0`，错误 `0`。
- M8 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointContributionMapperTest,ClubPointContributionEnumTest,ClubPointContributionServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `18` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误，仅 CRLF 提示；源码与测试范围 `tenant_id|TenantBaseDO` 无命中；源码、测试和本次文档范围精确元数据模式无命中。

## 任务 M8.6 违规扣分

- [ ] 管理员创建违规扣分。
- [ ] 选择违规规则项。
- [ ] 录入扣分值。
- [ ] 校验扣分值在规则区间内。
- [ ] 调用 LedgerService 生成负向流水。
- [ ] 写强审计。

验收：

- [ ] 扣分不会使可用余额变成非法负数，除非规则明确允许。
- [ ] 违规扣分可追溯到规则和原因。

## 任务 M8.7 弄虚作假处理

- [ ] 管理员标记材料弄虚作假。
- [ ] 撤销原材料已发流水。
- [ ] 按弄虚作假规则扣分。
- [ ] 锁定相关附件。
- [ ] 写强审计。
- [ ] 通知相关员工或负责人。

验收：

- [ ] 原流水用反向流水撤销。
- [ ] 不直接删除原流水。
- [ ] 弄虚作假扣分和撤销都可追溯。

## 任务 M8.8 API

- [ ] 负责人材料列表。
- [ ] 负责人材料详情。
- [ ] 负责人提交材料。
- [ ] 负责人撤回草稿。
- [ ] 管理员审核列表。
- [ ] 管理员审核接口。
- [ ] 管理员代录接口。
- [ ] 管理员违规扣分接口。
- [ ] 管理员弄虚作假处理接口。

验收：

- [ ] API 路径和 `club-points-api-design.md` 一致。
- [ ] 负责人和管理员权限边界清楚。

## 任务 M8.9 测试

- [ ] 测试负责人提交材料。
- [ ] 测试负责人越权失败。
- [ ] 测试分值越界失败。
- [ ] 测试审核通过发分。
- [ ] 测试重复审核幂等。
- [ ] 测试管理员代录幂等。
- [ ] 测试违规扣分。
- [ ] 测试弄虚作假撤销加扣分。

验收：

- [ ] 非签到闭环测试通过。
- [ ] 所有发分扣分都走 LedgerService。

## M8 放行标准

- [ ] 非签到材料提交可用。
- [ ] 非签到审核发分可用。
- [x] 管理员代录可用。
- [ ] 违规扣分可用。
- [ ] 弄虚作假处理可用。

## M8 不通过时禁止

- [ ] 禁止做积分来源统计验收。
- [ ] 禁止做 MVP 全量演示。
