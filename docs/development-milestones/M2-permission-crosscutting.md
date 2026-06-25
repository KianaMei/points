# M2 权限范围和横切能力 Implementation Plan

**Status:** `[~]` M2 进行中。M2.1 权限码、菜单、三类角色建议授权已完成；M2.2 字典常量和 seed 字典已完成；M2.3 数据范围服务已完成；M2.4 强审计服务已完成；M2.5-M2.7 尚未完成。

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立权限码、数据范围、强审计、附件绑定、通知封装，给后续所有业务模块提供统一横切能力。

**Architecture:** Controller 负责动作权限，Service 负责数据范围和事务边界。强审计与关键业务写同事务，通知封装为非阻断能力，附件只保存业务绑定不复制 infra 文件表。

**Tech Stack:** Spring Security `@PreAuthorize`、RuoYi system 菜单权限、infra 文件能力、system 站内信、MyBatis、JUnit。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 权限单一输入是 `docs/club-points-functions-and-permissions.md`。
- 按钮隐藏不是安全边界，后端必须校验动作权限和数据范围。
- 强审计失败必须回滚业务。
- 通知失败不回滚业务。
- Java 行为必须 TDD。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Modify: `ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/DictTypeConstants.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/scope/ClubScopeService.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/audit/ClubAuditService.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/attachment/ClubAttachmentService.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/notify/ClubNotifyService.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/`

**Interfaces:**

- Consumes: `system` 菜单权限、`infra` 文件能力、`system` 站内信能力、当前登录用户。
- Produces: `ClubScopeService`、`ClubAuditService`、`ClubAttachmentService`、`ClubNotifyService` 给 M3-M12 复用。

**Verification:**

- Run: M2 相关 Service 单测。
- Expected: 员工、负责人、管理员数据范围测试通过。
- Run: 强审计失败回滚测试。
- Expected: 审计失败时业务写入回滚。
- Run: 通知失败测试。
- Expected: 通知失败不回滚主业务。

## 目标

先建立权限、数据范围、强审计、附件绑定、通知封装，避免后续业务模块重复写散装逻辑。

## 前置条件

- M1 已放行。
- 菜单、按钮、字典、默认规则 seed 已存在。
- `clubpoints` 模块已能被后端扫描。

## 任务 M2.1 权限码和菜单

- [x] 按 `docs/club-points-functions-and-permissions.md` 梳理全部权限码。
- [x] 在 `club-points-seed.sql` 写菜单。
- [x] 在 `club-points-seed.sql` 写按钮权限。
- [x] 在 `club-points-seed.sql` 写员工、负责人、管理员角色建议授权。
- [x] 权限码统一 `clubpoints:<resource>:<action>`。
- [x] 前端菜单路径和 `docs/club-points-frontend-page-design.md` 一致。

验收：

- [x] 管理员菜单完整。
- [x] 负责人没有全局审核、全局导出、规则配置权限。
- [x] 员工没有管理权限。

证据：

- RED：导入 M1 seed 到临时库 `club_points_m2_perm_red` 后，`clubpoints:registration:special-absence` 权限数为 `0`；`clubpoints/app`、`clubpoints/leader`、`clubpoints/admin` 三端组件路径计数均只有 `1`；员工角色误拿 `clubpoints:redemption:review`。
- GREEN：重写 `club-points-seed.sql` 菜单为员工端 `/clubpoints/app/*`、负责人端 `/clubpoints/leader/*`、管理员端 `/clubpoints/admin/*` 三段结构；补 `clubpoints:registration:special-absence`；重写员工、负责人、管理员角色授权。
- 验证：临时库 `club_points_m2_perm_green` 依次导入 `ruoyi-vue-pro.sql`、`club-points-schema.sql`、`club-points-seed.sql` 成功。
- 菜单验证：`system_menu` 中本模块菜单和按钮共 `89` 条，按钮权限 `54` 个，`clubpoints:registration:special-absence` 为 `1` 条。
- 路径验证：组件路径 `clubpoints/app/%` 为 `7` 条，`clubpoints/leader/%` 为 `5` 条，`clubpoints/admin/%` 为 `19` 条，覆盖前端页面设计的三端入口。
- 权限集合验证：从 `club-points-api-design.md` 和 `club-points-frontend-page-design.md` 提取权限码并取并集为 `54` 个；seed 权限码 `54` 个；缺失 `0`，多余 `0`。
- 角色边界验证：负责人禁止权限计数为 `0`，覆盖非签到审核、兑换审核、报表导出、规则配置、负责人维护、全局账本、年度、预算、审计、任务、结算运行等全局能力；员工管理权限计数为 `0`。
- 幂等验证：重复导入 `club-points-seed.sql` 后，本模块菜单仍为 `89` 条，员工授权 `18` 条，负责人授权 `42` 条，管理员授权 `89` 条，计数不膨胀。
- 冲突处理：前端页面设计和功能清单都包含“特殊缺席”，API 文档未列出独立权限码；M2.1 按功能存在和前端按钮权限补充 `clubpoints:registration:special-absence`，后续 API 实现必须补齐对应 Controller 权限。

## 任务 M2.2 字典常量

- [x] 创建 `enums/DictTypeConstants.java`。
- [x] 添加活动状态字典。
- [x] 添加报名状态字典。
- [x] 添加签到状态字典。
- [x] 添加规则版本状态字典。
- [x] 添加积分流水类型字典。
- [x] 添加冻结状态字典。
- [x] 添加兑换状态字典。
- [x] 添加异议状态字典。
- [x] 添加年度处理状态字典。
- [x] 同步 seed 字典类型和字典值。

验收：

- [x] 后端常量和 seed 字典类型一致。
- [x] 不在前端硬编码中文状态。

证据：

- RED：新增 `DictTypeConstantsTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=DictTypeConstantsTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`，失败于 `DictTypeConstants` 不存在；测试有效。中间两次命令失败分别是未带 `-am` 导致依赖解析失败，以及 PowerShell 未给 Maven 属性加引号导致 lifecycle phase 错误，不计入 RED。
- GREEN：创建 `DictTypeConstants.java`，补齐 `23` 个 `club_points_*` 字典类型常量；补 seed 中缺失的负责人状态、报名取消原因、规则版本状态、规则项状态、流水来源类型、年度清零状态字典类型和值。
- 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=DictTypeConstantsTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`DictTypeConstantsTest` 运行 `2` 个测试，失败 `0`、错误 `0`。
- MySQL 验证：临时库 `club_points_m2_dict_green` 导入 `ruoyi-vue-pro.sql`、`club-points-schema.sql`、`club-points-seed.sql` 成功。
- 字典覆盖验证：`system_dict_type` 中本模块字典类型 `23` 个，`system_dict_data` 中本模块字典值 `89` 个；M2 必需字典类型命中 `11` 个。
- 新增字典值验证：`club_points_rule_version_status` 有 `4` 个值，`club_points_rule_item_status` 有 `2` 个值，`club_points_transaction_source_type` 有 `8` 个值，`club_points_annual_clearing_status` 有 `3` 个值，`club_points_leader_status` 有 `2` 个值，`club_points_registration_cancel_reason` 有 `4` 个值。
- 幂等验证：重复导入 `club-points-seed.sql` 后，本模块字典类型仍为 `23` 个，字典值仍为 `89` 个。
- 前端硬编码验证：当前 `yudao-ui-admin-vue3/src/views/clubpoints` 不存在，`rg --files` 只发现后端模块和 SQL 中的 `clubpoints` 文件；现阶段没有前端中文状态硬编码落地，后续前端实现必须使用 seed 字典类型。

## 任务 M2.3 ClubScopeService

- [x] 创建 `service/scope/ClubScopeService.java`。
- [x] 创建 `service/scope/ClubScopeServiceImpl.java`。
- [x] 实现本人范围校验。
- [x] 实现已加入俱乐部范围校验。
- [x] 实现负责俱乐部范围校验。
- [x] 实现管理员全局范围判断。
- [x] 统一抛出 clubpoints 错误码。
- [x] 写负责人越权访问其他俱乐部的测试。

验收：

- [x] 权限判断先动作权限，再数据范围。
- [x] Service 层能复用范围校验。
- [x] 前端隐藏按钮不影响后端拦截。

证据：

- RED：新增 `ClubScopeServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubScopeServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`，失败于 `ClubScopeService`、`ClubScopeServiceImpl`、`ClubMemberDO`、`ClubLeaderDO`、`ClubMemberMapper`、`ClubLeaderMapper`、`CLUB_SCOPE_DENIED` 不存在；测试有效。
- GREEN：新增 `ClubMemberDO`、`ClubLeaderDO`、`ClubMemberMapper`、`ClubLeaderMapper`；新增 `ClubScopeService` 和 `ClubScopeServiceImpl`；补 `CLUB_SCOPE_DENIED` 错误码。
- 范围实现：`validateSelf` 只允许本人；`validateJoinedClub` 只允许 `club_points_club_member.status = 1` 的已加入俱乐部；`validateManagedClub` 只允许 `club_points_club_leader.status = 1` 的负责俱乐部；`hasGlobalScope` / `validateGlobal` 由调用方传入全局范围判断结果，避免在数据范围服务中硬绑角色体系。
- 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubScopeServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubScopeServiceImplTest` 运行 `10` 个测试，失败 `0`，错误 `0`。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=DictTypeConstantsTest,ClubScopeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`DictTypeConstantsTest` 和 `ClubScopeServiceImplTest` 合计 `12` 个测试，失败 `0`，错误 `0`。
- 测试基础设施：新增 `src/test/resources/application-unit-test.yaml`，让 `clubpoints` 模块自己的 H2 DB 单测能解析 `yudao.info.base-package` 并加载本模块 `sql/create_tables.sql`。
- 质量验证：`git diff --check` 无空白错误；`rg -n "tenant_id|TenantBaseDO|Co-authored|co-author|AI" yudao-module-clubpoints` 无命中。

## 任务 M2.4 ClubAuditService

- [x] 创建 `service/audit/ClubAuditService.java`。
- [x] 创建 `service/audit/ClubAuditServiceImpl.java`。
- [x] 支持记录动作类型。
- [x] 支持记录业务对象类型和 ID。
- [x] 支持记录操作者和 IP。
- [x] 支持记录请求参数摘要。
- [x] 支持记录变更前后快照。
- [x] 支持记录成功或失败结果。
- [x] 审计写入失败时抛异常。
- [x] 写事务回滚测试：强审计失败则业务失败。

验收：

- [x] 强审计和业务写入在同一事务。
- [x] 通知失败不影响审计。
- [x] 导出、删除、审核、调整、清零都有动作枚举。

证据：

- RED：新增 `ClubAuditServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubAuditServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`，失败于 `ClubAuditService`、`ClubAuditServiceImpl`、`ClubAuditLogDO`、`ClubAuditLogMapper`、`ClubAuditCreateReqBO`、`ClubAuditActionTypeConstants`、`CLUB_AUDIT_WRITE_FAILED` 不存在；测试有效。
- GREEN：新增 `ClubAuditLogDO`、`ClubAuditLogMapper`、`ClubAuditCreateReqBO`、`ClubAuditService`、`ClubAuditServiceImpl`、`ClubAuditActionTypeConstants`，补 `CLUB_AUDIT_WRITE_FAILED` 错误码。
- 字段覆盖：`createAuditLog` 支持 `action_type`、`biz_type`、`biz_id`、`operator_user_id`、`operator_name_snapshot`、`operator_role_snapshot`、`operation_time`、`client_ip`、`user_agent`、`reason`、`before_json`、`after_json`、`target_snapshot_json`、`success`、`error_message`。M2.4 清单中的“请求参数摘要”按数据库设计落入 `target_snapshot_json`，没有擅自新增表字段。
- 事务验证：测试内 `TransactionalProbeService.writeBusinessThenFailAudit()` 先插入 `club_points_club_member`，再用缺失 `action_type` 的审计请求触发 `CLUB_AUDIT_WRITE_FAILED`；方法退出后成员表和审计表计数均为 `0`，证明审计失败回滚业务写入。
- 动作枚举验证：`ClubAuditActionTypeConstants` 覆盖 `REPORT_EXPORT`、`PHYSICAL_DELETE`、`REVIEW_APPROVE`、`POINT_ADJUST`、`ANNUAL_CLEARING_MANUAL`，对应导出、删除、审核、调整、清零。
- 通知边界：`ClubAuditServiceImpl` 不依赖通知服务，审计成功后不会被通知失败反向影响；通知失败不回滚主业务的行为在 M2.6 `ClubNotifyService` 中继续用测试验证。
- 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubAuditServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubAuditServiceImplTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=DictTypeConstantsTest,ClubScopeServiceImplTest,ClubAuditServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `17` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无空白错误；`rg -n "tenant_id|TenantBaseDO" yudao-module-clubpoints` 无命中；`rg -n "Co-authored|co-author|AI-generated|Generated by AI|ChatGPT" yudao-module-clubpoints` 无命中。

## 任务 M2.5 AttachmentService

- [ ] 创建 `service/attachment/ClubAttachmentService.java`。
- [ ] 创建 `service/attachment/ClubAttachmentServiceImpl.java`。
- [ ] 复用 infra 文件能力。
- [ ] 写附件绑定记录。
- [ ] 支持业务对象类型和 ID。
- [ ] 支持附件锁定。
- [ ] 支持附件删除前校验。
- [ ] 审核通过后锁定材料附件。

验收：

- [ ] 业务只保存附件绑定，不复制文件表。
- [ ] 已锁定附件不能被普通删除。
- [ ] 物理删除材料要写强审计。

## 任务 M2.6 ClubNotifyService

- [ ] 创建 `service/notify/ClubNotifyService.java`。
- [ ] 创建 `service/notify/ClubNotifyServiceImpl.java`。
- [ ] 复用 system 站内信能力。
- [ ] 封装活动审核结果通知。
- [ ] 封装积分到账通知。
- [ ] 封装兑换审核通知。
- [ ] 封装异议处理通知。
- [ ] 通知失败只记录日志，不回滚主业务。

验收：

- [ ] 业务服务不直接散落调用通知实现。
- [ ] 通知失败不会破坏账本一致性。

## 任务 M2.7 横切测试

- [ ] 写员工越权访问他人数据测试。
- [ ] 写负责人越权访问其他俱乐部测试。
- [ ] 写管理员全局访问测试。
- [ ] 写强审计失败回滚测试。
- [ ] 写通知失败不回滚测试。
- [ ] 写附件锁定后不可删除测试。

验收：

- [ ] M2 横切能力可被 M3-M10 复用。
- [ ] 每个高风险能力至少有一个失败路径测试。

## M2 放行标准

- [ ] 权限 seed 完成。
- [ ] 字典 seed 完成。
- [x] `ClubScopeService` 可用。
- [x] `ClubAuditService` 可用。
- [ ] `ClubAttachmentService` 可用。
- [ ] `ClubNotifyService` 可用。
- [ ] 横切测试通过。

## M2 不通过时禁止

- [ ] 禁止写规则发布接口。
- [ ] 禁止写账本。
- [ ] 禁止写活动、兑换、年度业务。
