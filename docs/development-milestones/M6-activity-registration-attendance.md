# M6 活动、报名、签到签退 Implementation Plan

**Status:** `[~]` M6.1-M6.5 已完成并有 RED/GREEN 证据；当前入口是 M6.6 修正和特殊缺席。

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现活动发布、报名、签到、签退、修正、特殊缺席；本阶段只记录事实，不生成积分流水。

**Architecture:** 活动域保存业务事实和状态机，结算留给 M7。负责人只能管理负责俱乐部活动，管理员负责审核和修正；已发布活动关键修改必须强审计。

**Tech Stack:** Spring Service、MyBatis、RuoYi Controller/VO、附件绑定、强审计、JUnit。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- M6 不生成积分流水。
- 特殊缺席不拆独立表。
- 报名、签到、修正必须有数据库幂等约束。
- Java 行为必须 TDD。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/activity/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/activity/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/activity/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/app/activity/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/leader/activity/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/activity/`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/activity/`

**Interfaces:**

- Consumes: M3 规则快照、M5 俱乐部和成员关系、M2 附件和审计。
- Produces: 活动事实、报名事实、签到签退事实、特殊缺席事实，供 M7 结算读取。

**Verification:**

- Run: 活动状态机单测。
- Expected: 未发布不能报名，已取消不能签到，已结算不能改关键字段。
- Run: 报名和签到幂等测试。
- Expected: 重复报名、重复签到、重复签退被数据库或业务校验拦截。

## 目标

实现活动发布、报名、签到、签退、修正、特殊缺席。此阶段只记录事实，不做自动发分。

## 前置条件

- M5 已放行。
- 规则读取可用。
- 俱乐部、成员、负责人范围可用。
- 附件和审计可用。

## 任务 M6.1 DO 和 Mapper

- [x] 创建 `ClubPointActivityDO`。
- [x] 创建 `ClubPointActivityReviewRecordDO`。
- [x] 创建 `ClubPointActivityPointConfigVersionDO`。
- [x] 创建 `ClubPointActivityRegistrationDO`。
- [x] 创建 `ClubPointAttendanceRecordDO`。
- [x] 创建 `ClubPointAttendanceCorrectionDO`。
- [x] 创建对应 Mapper。
- [x] 字段和 M1 DDL 一致。

验收：

- [x] 活动、报名、签到、修正事实都可落库。
- [x] 活动积分配置版本保存规则快照。

证据：

- RED：新增 `ClubPointActivityMapperTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivityMapperTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `dal.dataobject.activity` 包和 6 个 activity Mapper 不存在，符合 M6.1 RED 预期。
- GREEN：新增活动、活动审核记录、活动积分配置版本、活动报名、签到签退有效事实、签到签退补录修正 6 个 DO；新增对应 6 个 Mapper，均继承 `BaseMapperX`，字段按 M1 DDL 映射，不新增表和字段。
- M6.1 单测验证：同一命令返回 `BUILD SUCCESS`；`ClubPointActivityMapperTest` 运行 `1` 个测试，失败 `0`，错误 `0`。
- M6 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivityMapperTest,ClubPointClubMapperTest,ClubPointClubEnumTest,ClubPointClubServiceImplTest,ClubPointMemberServiceImplTest,ClubPointLeaderServiceImplTest,ClubPointClubQueryControllerTest,ClubScopeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `44` 个测试，失败 `0`，错误 `0`。

## 任务 M6.2 活动状态机

- [x] 定义草稿状态。
- [x] 定义待审核状态。
- [x] 定义已发布状态。
- [x] 定义已驳回状态。
- [x] 定义已取消状态。
- [x] 定义已结束状态。
- [x] 定义已结算状态。
- [x] 限制非法状态跳转。

验收：

- [x] 未发布活动不能报名。
- [x] 已取消活动不能签到。
- [x] 已结算活动不能再改关键字段。

证据：

- RED：新增 `ClubPointActivityEnumTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivityEnumTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointActivityStatusEnum` 和 `CLUB_ACTIVITY_STATUS_INVALID` 不存在，符合 M6.2 RED 预期。
- GREEN：新增 `ClubPointActivityStatusEnum`，状态编号按 seed 和数据库设计执行：`1` 草稿、`2` 待审核、`3` 已驳回、`4` 已发布、`5` 已取消、`6` 已结束、`7` 已结算、`8` 已删除快照；补 `CLUB_ACTIVITY_STATUS_INVALID` 错误码。
- 状态机边界：只允许草稿提交审核或管理员直发；待审核可发布或驳回；驳回可重提；已发布可取消或结束；已结束可结算；未发布不能报名，已取消和已结算不能签到，已结算不能改关键字段。
- M6.2 单测验证：同一命令返回 `BUILD SUCCESS`；`ClubPointActivityEnumTest` 运行 `4` 个测试，失败 `0`，错误 `0`。
- M6 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivityMapperTest,ClubPointActivityEnumTest,ClubPointClubMapperTest,ClubPointClubEnumTest,ClubPointClubServiceImplTest,ClubPointMemberServiceImplTest,ClubPointLeaderServiceImplTest,ClubPointClubQueryControllerTest,ClubScopeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `48` 个测试，失败 `0`，错误 `0`。

## 任务 M6.3 活动 Service

- [x] 负责人创建活动草稿。
- [x] 负责人提交活动审核。
- [x] 管理员审核通过活动。
- [x] 管理员驳回活动。
- [x] 负责人修改未发布活动。
- [x] 管理员修改已发布活动关键信息。
- [x] 修改已发布活动关键信息写强审计。
- [x] 取消活动写强审计。
- [x] 活动附件绑定和锁定。

验收：

- [x] 负责人只能操作负责俱乐部活动。
- [x] 审核记录完整。
- [x] 已发布活动关键修改可追溯。

证据：

- RED：新增 `ClubPointActivityServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointActivityServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointActivityService`、活动 Service BO、`BIZ_TYPE_ACTIVITY`、`ACTIVITY_CANCEL`、`ACTIVITY_KEY_FIELD_UPDATE` 等产物不存在，符合 M6.3 RED 预期。
- GREEN：新增 `ClubPointActivityService` / `ClubPointActivityServiceImpl` 和保存、提交、审核、取消 BO；实现负责人创建草稿、提交审核、管理员审核通过/驳回、负责人修改未发布活动、已发布关键字段修改强审计、取消活动强审计；审核通过生成活动积分配置版本并锁定 `ACTIVITY` 附件。
- 实现边界：负责人操作通过 `ClubScopeService.validateManagedClub(...)` 限制负责俱乐部；管理员审核通过/驳回通过全局范围校验；活动待发布积分配置保存在活动 `snapshotJson`，审核通过或已发布关键字段变更时生成正式配置版本；M6.3 不生成积分流水。
- M6.3 单测验证：同一命令返回 `BUILD SUCCESS`；`ClubPointActivityServiceImplTest` 运行 `7` 个测试，失败 `0`，错误 `0`。
- M6 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivityMapperTest,ClubPointActivityEnumTest,ClubPointActivityServiceImplTest,ClubPointClubMapperTest,ClubPointClubEnumTest,ClubPointClubServiceImplTest,ClubPointMemberServiceImplTest,ClubPointLeaderServiceImplTest,ClubPointClubQueryControllerTest,ClubScopeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `55` 个测试，失败 `0`，错误 `0`。

## 任务 M6.4 报名 Service

- [x] 员工报名活动。
- [x] 员工取消报名。
- [x] 负责人查看报名名单。
- [x] 管理员查看报名名单。
- [x] 校验员工是否满足报名范围。
- [x] 校验活动报名时间。
- [x] 报名唯一键防重复。

验收：

- [x] 员工不能重复报名同一活动。
- [x] 非成员不能报名仅限成员活动。
- [x] 报名截止后不能报名。

证据：

- RED：新增 `ClubPointRegistrationServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRegistrationServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointRegistrationService`、`ClubPointRegistrationServiceImpl`、报名 BO、报名枚举和报名错误码不存在，符合 M6.4 RED 预期。
- GREEN：新增 `ClubPointRegistrationService` / `ClubPointRegistrationServiceImpl` 和创建、取消、分页 BO；新增报名状态枚举和取消原因枚举；补报名截止、取消窗口关闭、重复报名、报名记录不存在错误码；报名 Mapper 增加有效报名查询和分页查询。
- 实现边界：员工只能报名已发布活动，且必须是活动所属俱乐部有效成员；报名时间不能晚于 `registrationDeadline`；重复有效报名先业务校验，再由 `active_unique_key` 唯一键兜底；报名保存成员、俱乐部、活动标题和活动时间快照；员工取消本人报名会清空 `activeUniqueKey`，设置 `noAbsenceDeduct=true`；负责人报名分页以 `validateManagedClub(...)` 限制负责俱乐部；管理员报名分页全局查询；M6.4 不生成积分流水。
- M6.4 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointRegistrationServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointRegistrationServiceImplTest` 运行 `6` 个测试，失败 `0`，错误 `0`。
- M6 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivityMapperTest,ClubPointActivityEnumTest,ClubPointActivityServiceImplTest,ClubPointRegistrationServiceImplTest,ClubPointClubMapperTest,ClubPointClubEnumTest,ClubPointClubServiceImplTest,ClubPointMemberServiceImplTest,ClubPointLeaderServiceImplTest,ClubPointClubQueryControllerTest,ClubScopeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `61` 个测试，失败 `0`，错误 `0`。

## 任务 M6.5 签到签退 Service

- [x] 员工签到。
- [x] 员工签退。
- [x] 记录签到时间、签退时间、定位或方式字段。
- [x] 校验签到窗口。
- [x] 校验签退窗口。
- [x] 防重复签到。
- [x] 防重复签退。
- [x] 不在此阶段生成积分流水。

验收：

- [x] 签到事实可查询。
- [x] 签退事实可查询。
- [x] 重复签到签退被拦截。

证据：

- RED：新增 `ClubPointAttendanceServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointAttendanceServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointAttendanceService`、`ClubPointAttendanceServiceImpl`、自助签到 BO、签到目标/来源枚举和签到错误码不存在，符合 M6.5 RED 预期。
- GREEN：新增 `ClubPointAttendanceService` / `ClubPointAttendanceServiceImpl` 和 `ClubPointAttendanceSelfReqBO`；新增签到目标类型枚举和签到来源类型枚举；补签到窗口关闭、重复签到签退、签退前必须签到错误码；签到 Mapper 增加按报名和目标类型查询。
- 实现边界：员工从本人有效报名记录签到或签退；活动必须处于已发布且允许签到状态；签到按 `checkinStartTime` 到 `checkinEndTime` 校验窗口；签退要求已有签到事实，再按 `checkoutStartTime` 到 `checkoutEndTime` 校验窗口；自助记录写 `sourceType=SELF`、操作人、记录时间、IP 和备注；重复签到/签退先业务校验，再由 `registration_id,target_type` 唯一键兜底；M6.5 不生成积分流水。
- M6.5 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointAttendanceServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointAttendanceServiceImplTest` 运行 `6` 个测试，失败 `0`，错误 `0`。
- M6 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivityMapperTest,ClubPointActivityEnumTest,ClubPointActivityServiceImplTest,ClubPointRegistrationServiceImplTest,ClubPointAttendanceServiceImplTest,ClubPointClubMapperTest,ClubPointClubEnumTest,ClubPointClubServiceImplTest,ClubPointMemberServiceImplTest,ClubPointLeaderServiceImplTest,ClubPointClubQueryControllerTest,ClubScopeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `67` 个测试，失败 `0`，错误 `0`。

## 任务 M6.6 修正和特殊缺席

- [ ] 管理员补录签到。
- [ ] 管理员修正签到签退。
- [ ] 管理员标记特殊缺席。
- [ ] 修正必须写强审计。
- [ ] 特殊缺席不拆独立表。
- [ ] 修正后保留原始签到事实。

验收：

- [ ] 补录和修正可追溯。
- [ ] 特殊缺席能被 M7 结算读取。

## 任务 M6.7 API

- [ ] 员工活动列表。
- [ ] 员工活动详情。
- [ ] 员工报名接口。
- [ ] 员工取消报名接口。
- [ ] 员工签到接口。
- [ ] 员工签退接口。
- [ ] 负责人活动管理接口。
- [ ] 管理员活动审核接口。
- [ ] 管理员签到修正接口。

验收：

- [ ] API 路径和 `club-points-api-design.md` 一致。
- [ ] 写接口都有后端权限和范围校验。

## 任务 M6.8 测试

- [ ] 测试活动状态跳转。
- [ ] 测试负责人越权创建失败。
- [ ] 测试报名重复失败。
- [ ] 测试报名时间窗口。
- [ ] 测试签到时间窗口。
- [ ] 测试补录强审计。
- [ ] 测试特殊缺席保存。

验收：

- [ ] 活动事实链测试通过。
- [ ] 还没有积分流水生成。

## M6 放行标准

- [ ] 活动管理闭环可用。
- [ ] 报名闭环可用。
- [ ] 签到签退闭环可用。
- [ ] 修正和特殊缺席可用。
- [ ] 不生成积分流水。

## M6 不通过时禁止

- [ ] 禁止写活动自动结算。
- [ ] 禁止写无故缺席扣分。
