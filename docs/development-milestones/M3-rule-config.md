# M3 规则版本和配置后台 Implementation Plan

**Status:** `[~]` M3.1-M3.4 已完成并有 RED/GREEN 与质量门禁证据；当前入口是 M3.5 业务读取封装。

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现规则版本、规则项、发布、停用和业务读取接口，确保所有分值、阈值、区间来自已发布规则。

**Architecture:** 规则版本控制生效周期，规则项控制可执行参数。业务服务只读取已发布版本并保存规则快照，不允许把制度分值硬编码进 Java。

**Tech Stack:** Spring Boot Service、MyBatis Mapper、RuoYi VO/Controller、强审计、JUnit。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 规则默认值来自 seed，不写死进业务代码。
- 发布和停用必须写强审计。
- 已发布版本不可直接修改规则项。
- Java 行为必须 TDD。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/rule/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/rule/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/rule/ClubPointRuleService.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/rule/`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ErrorCodeConstants.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/rule/`

**Interfaces:**

- Consumes: M1 规则表、M2 强审计、默认规则 seed。
- Produces: `getCurrentRuleVersion()`、`getRuleItemByCode(String code)`、`validatePointInRange(String code, Integer points)`、`buildRuleSnapshot(...)`。

**Verification:**

- Run: 规则版本状态机单测。
- Expected: 草稿、发布、停用、已发布不可改全部通过。
- Run: 分值区间校验单测。
- Expected: 越界失败，固定分值规则通过。

## 目标

实现规则版本、规则项、发布、停用、业务读取接口。后续所有分值、阈值、上限都从已发布规则读取。

## 前置条件

- M2 已放行。
- 默认规则 seed 已存在。
- 强审计可用。
- 权限码和菜单已存在。

## 任务 M3.1 DO 和 Mapper

- [x] 创建 `dal/dataobject/rule/ClubPointRuleVersionDO.java`。
- [x] 创建 `dal/dataobject/rule/ClubPointRuleItemDO.java`。
- [x] 创建 `dal/dataobject/rule/ClubPointRulePublishRecordDO.java`。
- [x] 创建 `dal/mysql/rule/ClubPointRuleVersionMapper.java`。
- [x] 创建 `dal/mysql/rule/ClubPointRuleItemMapper.java`。
- [x] 创建 `dal/mysql/rule/ClubPointRulePublishRecordMapper.java`。
- [x] DO 继承 `BaseDO`。
- [x] Mapper 继承 `BaseMapperX<T>`。

验收：

- [x] DO 字段和 M1 DDL 一致。
- [x] 不出现 `TenantBaseDO`。

证据：

- RED：新增 `ClubPointRuleMapperTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRuleMapperTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `dal.dataobject.rule` 包和三个 Rule Mapper 不存在。
- GREEN：创建三张规则表对应 DO、三个 Mapper 和基础查询后，同一命令返回 `BUILD SUCCESS`；`ClubPointRuleMapperTest` 运行 `1` 个测试，失败 `0`，错误 `0`。
- 字段对齐：测试覆盖 `club_points_rule_version`、`club_points_rule_item`、`club_points_rule_publish_record` 除通用字段外的全部业务字段落库与读取。
- 质量门禁：`git diff --check` 无输出；规则包内租户字段、租户基类和 AI 元数据模式检查均无命中。
- 继承检查：`rg -n "extends BaseDO|extends BaseMapperX<" .../rule` 命中 3 个 DO 继承 `BaseDO`、3 个 Mapper 继承 `BaseMapperX<T>`。

## 任务 M3.2 枚举和错误码

- [x] 创建规则版本状态枚举。
- [x] 创建规则项值类型枚举。
- [x] 创建规则项编码枚举。
- [x] 补充错误码：版本不存在、版本状态错误、规则项不存在、规则项编码重复、规则值越界。
- [x] 固定分值也用区间表达。

验收：

- [x] 不在 service 写死制度分值。
- [x] 规则项编码和 seed 一致。

证据：

- RED：新增 `ClubPointRuleEnumTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRuleEnumTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是规则版本状态枚举、规则项值类型枚举、规则项编码枚举和 5 个 M3 规则错误码不存在。
- GREEN：新增 `ClubPointRuleVersionStatusEnum`、`ClubPointRuleItemTypeEnum`、`ClubPointRuleItemCodeEnum`，并补 `CLUB_RULE_VERSION_NOT_EXISTS`、`CLUB_RULE_VERSION_STATUS_INVALID`、`CLUB_RULE_ITEM_NOT_EXISTS`、`CLUB_RULE_ITEM_CODE_DUPLICATED`、`CLUB_RULE_VALUE_OUT_OF_RANGE` 后，同一命令返回 `BUILD SUCCESS`；`ClubPointRuleEnumTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRuleMapperTest,ClubPointRuleEnumTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `6` 个测试，失败 `0`，错误 `0`。
- 规则项编码一致性：`ClubPointRuleEnumTest.itemCodeEnumShouldMatchSeedRuleItemCodes` 从 `club-points-seed.sql` 解析 22 个 `item_code` 并与枚举值集合全量比对。
- 固定分值表达：`ClubPointRuleEnumTest.fixedPointSeedRowsShouldUseMinMaxRange` 验证固定分值规则在 seed 中使用 `min_points=max_points=default_points`。
- Service 硬编码检查：精确搜索 22 个规则项编码在 `service` 包内无命中。
- 质量门禁：`git diff --check` 无空白错误；源码和本测试内租户字段、租户基类和 AI 元数据模式检查均无命中。

## 任务 M3.3 Service

- [x] 创建 `service/rule/ClubPointRuleService.java`。
- [x] 创建 `service/rule/ClubPointRuleServiceImpl.java`。
- [x] 实现创建草稿版本。
- [x] 实现复制已有版本。
- [x] 实现新增规则项。
- [x] 实现修改草稿规则项。
- [x] 实现发布版本。
- [x] 实现停用版本。
- [x] 实现读取当前已发布版本。
- [x] 实现按编码读取规则项。
- [x] 发布和停用必须写强审计。

验收：

- [x] 同一时间只有一个当前有效版本。
- [x] 已发布版本不可直接改规则项。
- [x] 业务读取只读已发布版本。

证据：

- RED：新增 `ClubPointRuleServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRuleServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`，目标失败确认为规则 Service、ServiceImpl、BO、审计动作常量等 M3.3 产物不存在。
- GREEN：新增 `ClubPointRuleService`、`ClubPointRuleServiceImpl`、三个规则 Service BO，补 Mapper 当前发布版本和规则项启用状态查询、补 `RULE_PUBLISH` 与 `RULE_DISABLE` 审计动作常量；同一命令返回 `BUILD SUCCESS`，`ClubPointRuleServiceImplTest` 运行 `11` 个测试，失败 `0`，错误 `0`。
- 回归 RED：自审发现发布新版本替代旧发布版本时，旧版本 `publish_record` 使用 `operator_user_id=0` 且 reason 为默认值；补断言后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRuleServiceImplTest#publishVersionShouldPublishDraftDisableOldPublishedAndWriteAudit "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，错误为 `expected: <100> but was: <0>`。
- 回归 GREEN：`disableOtherPublishedVersions(...)` 传入 `ClubPointRuleOperationReqBO`，替代记录复用真实操作人和原因；同一单方法命令返回 `BUILD SUCCESS`，测试 `1` 个，失败 `0`，错误 `0`。
- M3.3 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRuleServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRuleServiceImplTest` 运行 `11` 个测试，失败 `0`，错误 `0`。
- M3 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRuleMapperTest,ClubPointRuleEnumTest,ClubPointRuleServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `17` 个测试，失败 `0`，错误 `0`。
- 质量门禁：`git diff --check` 无 whitespace error，仅 CRLF 提示；源码和测试内租户字段、租户基类和 AI 元数据模式检查均无命中；精确搜索 22 个规则项编码在 main `service` 包内无命中。

## 任务 M3.4 Admin API

- [x] 创建规则版本分页接口。
- [x] 创建规则版本详情接口。
- [x] 创建规则版本草稿接口。
- [x] 创建规则版本复制接口。
- [x] 创建规则版本发布接口。
- [x] 创建规则版本撤回接口。
- [x] 创建规则版本停用接口。
- [x] 创建规则项列表接口。
- [x] 创建规则项保存接口。
- [x] Controller 使用 `@PreAuthorize`。
- [x] 写 VO 校验规则值类型、区间、默认值。

验收：

- [x] API 路径和 `club-points-api-design.md` 一致。
- [x] 管理员权限才能发布和停用。
- [x] 发布接口不接受前端传当前用户 ID。

证据：

- 文档冲突处理：M3.4 原清单未列撤回接口，但 `club-points-api-design.md`、`club-points-frontend-page-design.md`、`club-points-functions-and-permissions.md`、`club-points-flow-design.md` 均要求规则版本撤回；本任务按更具体文档补 `POST /clubpoints/rule/withdraw` 和 `RULE_WITHDRAW` 强审计。
- RED：运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRuleAdminControllerTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointRuleVersionSaveReqBO` 缺少 `setId(Long)` 和 `getId()`，证明规则版本更新路径未闭合。
- GREEN：补 `ClubPointRuleVersionSaveReqBO.id` 后，同一命令返回 `BUILD SUCCESS`；`ClubPointRuleAdminControllerTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- 覆盖补强 RED：新增 `ruleItemSaveReqVOShouldRejectInvalidItemType` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRuleAdminControllerTest#ruleItemSaveReqVOShouldRejectInvalidItemType "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，错误为 `expected: <true> but was: <false>`，证明规则项值类型缺少枚举校验。
- 覆盖补强 GREEN：`ClubPointRuleItemTypeEnum` 实现 `ArrayValuable<Integer>`，`RuleItemSaveReqVO.itemType` 增加 `@InEnum(ClubPointRuleItemTypeEnum.class)` 后，同一单方法命令返回 `BUILD SUCCESS`；测试 `1` 个，失败 `0`，错误 `0`。
- M3.4 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRuleAdminControllerTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRuleAdminControllerTest` 运行 `6` 个测试，失败 `0`，错误 `0`。
- M3 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointRuleMapperTest,ClubPointRuleEnumTest,ClubPointRuleServiceImplTest,ClubPointRuleAdminControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `23` 个测试，失败 `0`，错误 `0`。
- 安全边界：`RuleOperationReqVO` 只暴露 `id` 和 `reason`；发布、撤回、停用操作人 ID 和昵称均从 `SecurityFrameworkUtils` 取，IP/UA 从 `HttpServletRequest` 取。
- 质量门禁：`git diff --check` exit `0`，仅 CRLF 提示；源码和测试内租户字段、租户基类、AI 元数据模式均无命中；精确搜索 22 个规则项编码在 main `service` 包内无命中。

## 任务 M3.5 业务读取封装

- [ ] 提供 `getCurrentRuleVersion()`。
- [ ] 提供 `getRuleItemByCode(String code)`。
- [ ] 提供 `validatePointInRange(String code, Integer points)`。
- [ ] 提供 `buildRuleSnapshot(...)`。
- [ ] 对不存在规则项抛业务错误。

验收：

- [ ] M4-M10 可以统一读取规则。
- [ ] 业务记录能保存规则快照 JSON。

## 任务 M3.6 测试

- [ ] 测试草稿创建。
- [ ] 测试发布成功。
- [ ] 测试已发布版本不可修改。
- [ ] 测试停用成功。
- [ ] 测试分值越界失败。
- [ ] 测试固定分值规则。
- [ ] 测试无已发布版本时业务读取失败。

验收：

- [ ] 规则状态机测试通过。
- [ ] 规则值边界测试通过。

## M3 放行标准

- [ ] 规则版本可发布。
- [ ] 规则项可配置。
- [ ] 已发布规则可被业务读取。
- [ ] 发布和停用写强审计。
- [ ] 没有制度分值硬编码。

## M3 不通过时禁止

- [ ] 禁止写账本发分逻辑。
- [ ] 禁止写活动结算。
- [ ] 禁止写非签到积分和兑换资格规则。
