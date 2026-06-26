# M11 报表、任务监控、通知、前端收口 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将后端闭环暴露给三类角色前端页面，完成报表导出、任务监控、通知待办和工作台。

**Architecture:** 后端菜单 seed 是路由事实源，前端按角色组织 API 和页面。报表基于流水和业务快照查询，导出只写强审计，不新增导出主表。前端照抄现有 `src/api/system` 和 `src/views/system` 写法，复用已有共用组件。

**Tech Stack:** Vue3、Vite、Element Plus、RuoYi axios 封装、Spring Excel、MyBatis、强审计、Playwright 或页面验证。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 前端页面单一输入是 `docs/club-points-frontend-page-design.md`，路由、字段、权限以它为准。
- 前端入口端口固定 `8889`。
- 路由来自后端菜单 seed，前端不写静态业务路由。
- API 文件按 `src/api/system/role/index.ts` 风格，页面按 `src/views/system/role/index.vue` 风格，复用 `ContentWrap`、`Dialog`、`Pagination`、`UploadFile`。
- `v-hasPermi` 只改善体验，不是安全边界，后端 `@PreAuthorize` 和数据范围校验必须仍然生效。
- 导出必须写强审计。
- 前后端行为变更必须有测试或页面验证。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/report/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/report/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/jobrun/`
- Create: `ruoyi-vue-pro-github/yudao-ui/yudao-ui-admin-vue3/src/api/clubpoints/`
- Create: `ruoyi-vue-pro-github/yudao-ui/yudao-ui-admin-vue3/src/views/clubpoints/`
- Modify: `ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/report/`

**Interfaces:**

- Consumes: M4 流水、M5 俱乐部、M6 活动、M7 结算、M8 非签到、M9 兑换、M10 年度排名和预算、M2 强审计和通知、后端菜单 seed。
- Produces: 报表查询和导出接口、任务监控接口、三类角色前端 API 和页面入口。

**Verification:**

- Run: 报表导出审计测试。
- Expected: 导出成功写强审计，审计失败导出失败。
- Run: `Invoke-WebRequest http://127.0.0.1:8889`。
- Expected: 前端入口 HTTP `200`。
- Run: 三类角色页面主流程验证。
- Expected: 员工、负责人、管理员只看到各自权限范围内页面和按钮。

## 目标

把后端业务闭环暴露给前端页面，完成报表导出、任务监控、通知待办和三类角色工作台。

## 前置条件

- M10 已放行，M8 非签到、M9 兑换、M10 年度运营的后端 API 和 VO 字段已稳定。
- `docs/club-points-frontend-page-design.md` 是前端页面单一输入。
- 后端菜单 seed 已有，路由由菜单下发。
- 前端工程 `yudao-ui-admin-vue3` 可正常 `pnpm dev`。

## 任务 M11.1 前端基建和共用组件

- [x] 建 `src/api/clubpoints/` 下 `app`、`leader`、`admin`、`shared` 目录。
- [x] 建 `src/views/clubpoints/` 下 `app`、`leader`、`admin`、`components` 目录。
- [x] 建 `shared/requestNo.ts`，按业务类型生成请求号，重试复用同号，重开生成新号。
- [x] 建 `components/PointAmount.vue`，展示正负积分、冻结和年度清零提示。
- [x] 建 `components/StatusTag.vue`，按字典展示俱乐部、活动、报名、材料、兑换、任务状态。
- [x] 建 `components/AttachmentInput.vue`，复用 infra 文件上传，输出 `AttachmentInputVO[]`。
- [x] 建 `components/StrongConfirmDialog.vue`，仅管理员物理删除俱乐部使用。
- [x] 建 `components/ReviewDialog.vue`，活动、材料、兑换审核复用，输出 `ReviewReqVO`。
- [x] 建 `components/RuleItemSelect.vue`，选择已发布规则版本和规则项并展示分值区间。
- [x] 建 `components/UserPicker.vue` 和 `components/ClubSelect.vue`，复用 system 用户和按角色俱乐部数据。
- [x] 核对页面用到的字典类型已在 seed 存在，缺失的补 `club-points-seed.sql`，前端不硬编码中文状态。

验收：

- [x] 共用组件能被页面 import 并渲染，无控制台报错。
- [x] `pnpm dev` 起在端口 `8889`，`Invoke-WebRequest http://127.0.0.1:8889` 返回 `200`。
- [x] 请求号重试复用、重开换号符合预期。

M11.1 证据：

- RED：运行 `Test-Path` 检查 `src/api/clubpoints/{app,leader,admin,shared}`、`src/views/clubpoints/{app,leader,admin,components}`、`shared/requestNo.ts` 和 8 个共用组件，全部返回 `False`。
- GREEN：新增 `shared/requestNo.ts`、`shared/types.ts`、8 个共用组件和目录占位；`Test-Path` 复核上述目录和文件全部返回 `True`。
- 请求号验证：用 Node + TypeScript `transpileModule` 执行 `requestNo.ts`，断言同一业务上下文重试复用同号，不同上下文、`resetRequestNo`、`clearRequestNo` 后重开均换号，命令退出码 `0`。
- 前端类型验证：`pnpm --dir ruoyi-vue-pro-github\yudao-ui\yudao-ui-admin-vue3 exec vue-tsc --noEmit --pretty false` 退出码 `1`，输出为前端工程存量 TypeScript 债；二次过滤 `clubpoints` 路径命中 `0` 行。
- 前端入口验证：临时启动 `pnpm dev` 后 `Invoke-WebRequest http://127.0.0.1:8889` 返回 HTTP `200`；停止临时 job 后端口仍由 2026-06-25 15:44:52 已存在的 `node` 进程监听，未杀用户已有进程。
- 字典验证：新增 `DICT_TYPE.CLUB_POINTS_*` 共 24 个，全部能在 `club-points-seed.sql` 找到对应 `system_dict_type`，`MissingInSeed=0`。
- 质量门禁：`git diff --check` 无输出；M11.1 前端范围精确元数据模式无命中；`tenant_id|TenantBaseDO`、Redis 模式和静态 `/clubpoints/...` 业务路由扫描无命中。

## 任务 M11.2 前端 API 模块

- [x] 建 `api/clubpoints/app/` 下 dashboard、ledger、club、activity、redemption、dispute、notify 模块。
- [x] 建 `api/clubpoints/leader/` 下 club、activity、attendance、contribution 模块。
- [x] 建 `api/clubpoints/admin/` 下 club、activity、settlement、ledger、rule、contribution、redemption、operation 模块。
- [x] 请求封装复用 `@/config/axios`，类型定义和后端 VO 一致。
- [x] 兑换申请、管理员代录、积分调整提交前生成并复用 `requestNo`。
- [x] 页面不散写 URL，所有请求经 API 模块函数。

验收：

- [x] API 文件按角色和领域分组，路径和 `club-points-api-design.md` 一致。
- [x] 不在页面里散写 URL。

M11.2 证据：

- RED：运行 `Test-Path` 检查 19 个 API 模块文件，全部返回 `False`。
- GREEN：新增 app 7 个模块、leader 4 个模块、admin 8 个模块，并扩展 `shared/types.ts` 的分页、基础响应、原因请求和强确认请求类型；复核 19 个 API 文件全部返回 `True`。
- 请求封装验证：扫描 19 个业务 API 文件，`MissingAxiosImport=0`，全部复用 `@/config/axios`。
- 请求号验证：`app/redemption.ts` 暴露 `getRedemptionApplyRequestNo/resetRedemptionApplyRequestNo`；`admin/contribution.ts` 暴露 `getDirectContributionRequestNo/resetDirectContributionRequestNo`；`admin/ledger.ts` 暴露 `getLedgerAdjustRequestNo/resetLedgerAdjustRequestNo`。
- 前端类型验证：`pnpm --dir ruoyi-vue-pro-github\yudao-ui\yudao-ui-admin-vue3 exec vue-tsc --noEmit --pretty false` 退出码 `1`，输出为前端工程存量 TypeScript 债；二次过滤 `clubpoints` 路径命中 `0` 行。
- 质量门禁：`git diff --check` 无空白错误，仅 CRLF 提示；M11.2 API 范围精确元数据模式无命中；`tenant_id|TenantBaseDO` 和 Redis 模式无命中；`views/clubpoints` 直接 `request` 调用和散写业务 URL 无命中。
- 边界说明：`dashboard`、`notify` 等 M11 后续能力按 `club-points-api-design.md` 预封装路径，后端实现与接口功能验证归 M11.6 / M11.12 / M11.13，不在 M11.2 冒充已完成后端能力。

## 任务 M11.3 报表查询

- [x] 管理员查询积分明细报表。
- [x] 管理员查询总台账报表。
- [x] 管理员查询兑换记录报表。
- [x] 管理员查询俱乐部排名报表。
- [x] 管理员查询预算报表。
- [x] 报表基于流水和业务快照查询。
- [x] 不新增导出主表。

验收：

- [x] 报表数据来源明确。
- [x] 员工和负责人不能导出报表。

M11.3 证据：

- RED：新增 `ClubPointReportControllerTest` 和 `ClubPointReportServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest="ClubPointReportControllerTest,ClubPointReportServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`，返回 `BUILD FAILURE`；失败原因是 `ClubPointReportAdminController`、`ClubPointReportServiceImpl`、报表 VO/BO 包不存在。
- GREEN：新增 `service/report`、`controller/admin/report`、五类报表 BO/VO 和 Mapper 查询方法；五个查询接口统一 `GET /clubpoints/report/*-page`，权限统一 `clubpoints:report:query`。
- 查询接口：`/point-detail-page` 读 `club_points_transaction`；`/ledger-summary-page` 读账户缓存并按 `club_points_transaction` 聚合；`/redemption-page` 读兑换申请的批次/礼品快照；`/club-ranking-page` 读 `club_points_annual_ranking_record`；`/budget-page` 读 `club_points_budget_record`。
- 验证：同一测试命令最终返回 `BUILD SUCCESS`；`ClubPointReportControllerTest` 1 个测试、`ClubPointReportServiceImplTest` 5 个测试，合计 6 个测试，失败 0，错误 0。
- 权限 seed：`报表中心` 菜单权限改为 `clubpoints:report:query`，新增 `查询报表` 按钮权限；`导出报表` 仍为 `clubpoints:report:export`，员工和负责人角色授权列表不包含查询/导出报表。
- 前端 API：`api/clubpoints/admin/operation.ts` 新增五个报表查询函数，页面仍不散写业务 URL。
- 边界说明：M11.3 只做页面查询，不做 Excel 导出、不写强审计、不新增导出业务表；导出强审计属于 M11.4。

## 任务 M11.4 报表导出

- [x] 使用芋道 Excel 能力导出。
- [x] 导出前校验管理员权限。
- [x] 导出动作写强审计。
- [x] 审计失败则导出失败。
- [x] 导出文件不落业务主表。
- [x] 导出字段和前端页面设计一致。

验收：

- [x] 导出成功有审计记录。
- [x] 越权导出失败。
- [x] 导出不产生独立业务表。

M11.4 证据：

- RED：扩展 `ClubPointReportControllerTest` 和 `ClubPointReportServiceImplTest`，新增 `/export-excel` 路径和 `clubpoints:report:export` 权限、Excel 响应、`REPORT_EXPORT` 强审计、审计失败阻断导出测试；运行 `mvn -pl yudao-module-clubpoints -am -Dtest="ClubPointReportControllerTest,ClubPointReportServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`，失败原因为 `AdminReportExportReqVO`、`ClubPointReportExportTypeEnum`、`ClubPointReportExportReqBO`、`ClubPointReportExportResultBO` 不存在。
- GREEN：新增报表导出类型枚举、导出 BO、导出请求 VO、`ClubPointReportService.exportReport` 和 `GET /clubpoints/report/export-excel`；Controller 使用 `ExcelUtils.write` 输出 Excel，权限为 `clubpoints:report:export`，并标记 `@ApiAccessLog(operateType = EXPORT)`。
- 强审计：`exportReport` 按白名单导出类型取数后写 `ClubAuditService`，动作类型 `REPORT_EXPORT`、业务类型 `REPORT`，记录导出人、角色、IP、UA、导出类型、筛选条件和 `rowCount`；审计写入失败时抛出异常，Controller 不写 Excel。
- 导出类型：`1=积分明细报表`、`2=兑换记录报表`、`3=积分总台账报表`、`4=俱乐部发放积分排名报表`、`5=预算和经费统计报表`。
- 导出字段：五类报表响应 VO 增加 `@ExcelProperty`，导出字段与页面查询响应字段保持一致。
- 验证：同一测试命令最终返回 `BUILD SUCCESS`；`ClubPointReportControllerTest` 2 个测试、`ClubPointReportServiceImplTest` 7 个测试，合计 9 个测试，失败 0，错误 0。
- 前端类型验证：`pnpm --dir ruoyi-vue-pro-github\yudao-ui\yudao-ui-admin-vue3 exec vue-tsc --noEmit --pretty false` 退出码 `1`，二次过滤 `clubpoints` 路径命中 `0` 行，判定为前端存量 TypeScript 债。
- 质量门禁：`git diff --check` 仅 LF/CRLF 提示；M11.4 源码/测试范围 `tenant_id|TenantBaseDO`、Redis、AI 元数据扫描均 0 命中；报表导出实现没有新增导出业务主表或直接写积分流水/账户事实源。
- 权限 seed：管理员角色继续拿到报表菜单和查询/导出按钮；员工、负责人授权列表不包含 `1300010470`、`1300012050`、`1300012058`。
- 边界说明：导出文件直接写 HTTP 响应，不新增导出业务主表；员工和负责人没有报表导出权限，越权由后端 `@PreAuthorize` 阻断。

## 任务 M11.5 任务监控

- [x] 管理员查看 `club_points_job_run` 列表。
- [x] 管理员查看任务运行详情。
- [x] 管理员重试失败任务。
- [x] 重试必须保持幂等。
- [x] 人工重试写强审计。

验收：

- [x] 结算任务可追踪。
- [x] 年度清零任务可追踪。
- [x] 失败任务可重试。

M11.5 证据：

- RED：新增 `ClubJobRunAdminControllerTest` 和 `ClubJobRunAdminServiceImplTest`，覆盖 `/clubpoints/job-run/page`、`/detail`、`/handle` 权限、活动结算失败任务重试、年度清零失败用户重试、强审计和非失败状态拒绝；运行 `mvn -pl yudao-module-clubpoints -am -Dtest="ClubJobRunAdminControllerTest,ClubJobRunAdminServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`，失败原因为 M11.5 Controller、VO、BO、Service 不存在。
- GREEN：新增 `ClubJobRunAdminController`、任务运行 VO/BO、`ClubJobRunAdminService`、`ClubJobRunAdminServiceImpl`、`ClubJobRunRetryDispatcher` 和生产委派实现；`ClubJobRunMapper` 增加分页过滤；错误码补任务不存在、状态不允许和任务类型不支持重试；强审计动作补 `JOB_RUN_RETRY`。
- 接口：`GET /clubpoints/job-run/page` 和 `GET /clubpoints/job-run/detail` 使用 `clubpoints:job:query`；`POST /clubpoints/job-run/handle` 使用 `clubpoints:job:handle`，人工处理原因必填。
- 重试口径：只允许 `RETRYABLE_FAILED` / `FINAL_FAILED` 任务；活动结算按原 `runKey`、`bizId` 和 `retryCount+1` 调用活动结算 Job Service；年度清零从 `resultJson.failedUserIds` 提取失败用户后按 `retryCount+1` 调用年度清零 Job Service；底层 Job 幂等键继续使用 `taskType + runKey + retryCount`。
- 验证：同一 Maven 命令最终返回 `BUILD SUCCESS`；`ClubJobRunAdminControllerTest` 1 个测试、`ClubJobRunAdminServiceImplTest` 4 个测试，合计 5 个测试，失败 0，错误 0。

## 任务 M11.6 通知和待办

- [x] 封装本人通知分页和标记已读接口。
- [x] 聚合待审核活动、材料、兑换和待处理异议的待办计数。
- [x] 活动审核结果、积分到账、兑换审核结果、异议处理结果通知可读取。
- [x] 待办按角色返回，点击带筛选进入对应页面。

验收：

- [x] 通知失败不回滚主业务。
- [x] 待办数量和业务状态一致。

M11.6 证据：

- RED：新增 `ClubPointNotifyAppControllerTest` 和 `ClubPointDashboardControllerTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest="ClubPointNotifyAppControllerTest,ClubPointDashboardControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD FAILURE`，失败原因为通知 App Controller、dashboard Controller、VO 和 Service 包不存在。
- GREEN：新增员工通知分页和标记已读接口，复用 system `NotifyMessageService` 和 `system_notify_message`，不新建 clubpoints 通知表；新增员工、负责人、管理员三端工作台汇总接口和待办项 VO。
- 权限和范围：员工通知和员工工作台使用当前登录用户；负责人工作台使用 `clubpoints:leader` 并只统计当前登录用户有效负责俱乐部；管理员工作台使用 `clubpoints:dashboard:query`，seed 同步管理员工作台菜单和查询工作台按钮，员工 / 负责人授权列表不包含该权限。
- 待办口径：员工统计已报名未结束活动、待审核兑换、未读通知；负责人统计负责俱乐部草稿活动、驳回活动、签到异常、待提交材料；管理员统计待审核活动、待审核材料、待审核兑换、待处理异议。
- 验证：`mvn -pl yudao-module-clubpoints -am -Dtest="ClubPointNotifyAppControllerTest,ClubPointDashboardControllerTest,ClubNotifyServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubPointDashboardControllerTest` 4 个测试、`ClubPointNotifyAppControllerTest` 2 个测试、`ClubNotifyServiceImplTest` 5 个测试，合计 11 个测试，失败 0，错误 0。
- 前端 API：`app/dashboard.ts` 字段同步后端 `AppDashboardSummaryRespVO`，`app/notify.ts` 补模板昵称、类型和创建时间；新增 `leader/dashboard.ts`、`admin/dashboard.ts`，三端工作台共用 `ClubPointDashboardTodoItemVO`。
- 前端类型验证：`pnpm --dir ruoyi-vue-pro-github\yudao-ui\yudao-ui-admin-vue3 exec vue-tsc --noEmit --pretty false` 退出码 `1`，二次过滤 `clubpoints` 路径命中 `0` 行，仍为前端工程存量 TypeScript 债。
- 质量门禁：`git diff --check` 仅 LF/CRLF 提示；M11.6 源码 / 测试范围 `tenant_id|TenantBaseDO`、Redis、AI 元数据扫描均 0 命中；事实源扫描只命中 `accountMapper.selectByUserId` 读取账户缓存，没有新增流水 / 账户写入。

## 任务 M11.7 管理员基础数据页

- [x] 规则版本页 `/clubpoints/admin/rule`，展示版本、规则项、状态、生效时间、附件，发布、撤回、停用二次确认并填原因，按钮权限 `clubpoints:rule:manage`。
- [x] 俱乐部和负责人页 `/clubpoints/admin/club`，支持增改、停用、删除、设置负责人、增删成员；物理删除走强确认弹窗，其他操作普通确认加原因。
- [x] 积分账户页 `/clubpoints/admin/ledger/account`，分页查询可用、冻结、累计，按钮权限 `clubpoints:ledger:query`。
- [x] 积分流水页 `/clubpoints/admin/ledger/transaction`，支持流水分页、调整、撤销；调整带 `requestNo`、规则版本、原因、附件，撤销选原流水并填原因。
- [x] 活动结算页 `/clubpoints/admin/settlement`，手动触发结算并查看结算运行记录，按钮权限 `clubpoints:settlement:run`、`clubpoints:settlement:query`。

验收：

- [x] 强确认只出现在物理删除俱乐部，其他删除只有普通确认。
- [x] 调整带请求号，撤销必须选原流水并填原因。
- [x] 手动重跑结算不重复发分。

M11.7 证据：

- RED：运行 `Test-Path` 检查 `views/clubpoints/admin/rule/index.vue`、`club/index.vue`、`ledger/account/index.vue`、`ledger/transaction/index.vue`、`settlement/index.vue`，全部返回 `False`。
- GREEN：新增 5 个管理员基础页面；页面复用 `api/clubpoints/admin/rule.ts`、`club.ts`、`ledger.ts`、`settlement.ts`，没有在页面散写后端业务 URL。
- 页面口径：规则页展示规则版本和规则项，发布 / 撤回 / 停用必须填原因；俱乐部页支持增改停用、成员和负责人维护，只有物理删除俱乐部使用 `StrongConfirmDialog`；账户页只读分页展示可用、冻结、累计积分；流水页调整积分生成 `requestNo`，撤销只能从原流水操作并填原因；结算页提示幂等由后端兜底，前端只发起运行请求。
- 前端类型验证：`pnpm --dir ruoyi-vue-pro-github\yudao-ui\yudao-ui-admin-vue3 exec vue-tsc --noEmit --pretty false` 退出码 `1`，二次过滤 `clubpoints` 路径命中 `0` 行，仍为前端工程存量 TypeScript 债。
- 质量门禁：强确认扫描只命中管理员俱乐部删除页；`requestNo` 扫描命中积分调整页；页面范围 `request.get|post|put|delete` 无命中；新增页面不触碰后端事实源、租户字段或 Redis。

## 任务 M11.8 员工前端页面

- [ ] 员工工作台 `/clubpoints/app/dashboard`，展示积分、俱乐部、活动、兑换、通知卡片，卡片跳转对应页面。
- [ ] 我的积分 `/clubpoints/app/ledger`，展示账户概览和流水筛选分页，展示来源统计不代表当前余额构成的文案。
- [ ] 我的俱乐部 `/clubpoints/app/club`，展示我的俱乐部、可加入俱乐部、成员名单，退出提示自动取消可取消报名。
- [ ] 活动报名签到 `/clubpoints/app/activity`，展示活动列表、详情、我的报名，支持报名、取消、签到、签退，提交带 `registrationId` 和 `clientTime`。
- [ ] 兑换 `/clubpoints/app/redemption`，展示批次、礼品、我的兑换，申请生成 `requestNo`，资格、库存、积分不足按后端错误码展示。
- [ ] 我的异议 `/clubpoints/app/dispute`，支持异议提交和记录，展示管理员回复和关联流水。
- [ ] 我的通知 `/clubpoints/app/notify`，支持已读未读切换，不提供删除。

验收：

- [ ] 员工只看到本人数据。
- [ ] 重试提交不重新生成 requestNo。
- [ ] 页面按钮和权限一致。

## 任务 M11.9 负责人前端页面

- [ ] 负责人工作台 `/clubpoints/leader/dashboard`，展示负责俱乐部、草稿活动、被驳回活动、签到异常、待提交材料、待办，待办带筛选跳转。
- [ ] 负责俱乐部和成员 `/clubpoints/leader/club`，只能修改自己负责俱乐部基础信息并查看成员，不能创建、停用、删除、设置负责人、移除成员。
- [ ] 活动管理 `/clubpoints/leader/activity`，支持创建、修改、提交审核、撤回、取消、删除草稿，规则版本必选，时间顺序前端即时校验。
- [ ] 报名签到和特殊缺席 `/clubpoints/leader/attendance`，展示报名名单和签到签退，支持修正和标记特殊缺席，结算后修正提示走调整流程。
- [ ] 非签到材料 `/clubpoints/leader/contribution`，支持材料创建、提交、撤回、删除，积分明细至少一条，通过后锁定只读。

验收：

- [ ] 负责人不能看到其他俱乐部数据。
- [ ] 负责人没有兑换审核、报表导出、规则配置入口。

## 任务 M11.10 管理员活动和材料审核页

- [ ] 活动审核和全局活动 `/clubpoints/admin/activity`，支持直接发布或审核负责人活动，审核只通过或驳回，驳回填原因。
- [ ] 材料审核 `/clubpoints/admin/contribution-review`，审核通过锁定附件，通过后不能改材料内容。
- [ ] 管理员代录 `/clubpoints/admin/contribution-direct`，代录直接生效，带 `requestNo`、员工、积分、规则版本、原因、附件。

验收：

- [ ] 审核通过按明细发分，重复审核不重复发分。
- [ ] 代录带请求号落幂等。

## 任务 M11.11 管理员兑换管理页

- [ ] 兑换批次 `/clubpoints/admin/redemption-batch`，支持批次创建、修改、开放、关闭。
- [ ] 礼品维护 `/clubpoints/admin/redemption-gift`，支持礼品创建、修改、状态切换。
- [ ] 兑换审核 `/clubpoints/admin/redemption-application`，只通过或拒绝，通过展示发放时间，拒绝展示冻结释放。

验收：

- [ ] 审核不能修改礼品、数量、积分消耗。
- [ ] 批次和礼品状态可维护。

## 任务 M11.12 管理员运营页面

- [ ] 异议处理 `/clubpoints/admin/dispute`，处理结果回写并通知员工。
- [ ] 年度清零 `/clubpoints/admin/annual-clearing`，展示只清未冻结可用积分的提示。
- [ ] 年度排名和激励 `/clubpoints/admin/annual-ranking`，展示排名分页和激励建议。
- [ ] 预算记录 `/clubpoints/admin/budget`，支持预算创建、修改，附件复用附件输入组件。
- [ ] 管理员工作台 `/clubpoints/admin/dashboard`，聚合待审核活动、材料、兑换、待处理异议、异常任务、异常撤销记录。

验收：

- [ ] 年度清零提示存在，仅清未冻结可用积分。
- [ ] 工作台聚合数和对应列表一致。

## 任务 M11.13 管理员报表、审计、任务页

- [ ] 报表中心 `/clubpoints/admin/report`，支持积分明细、兑换记录、总台账查询和导出，俱乐部排名和预算查询。
- [ ] 审计日志 `/clubpoints/admin/audit`，支持强审计动作查询追溯。
- [ ] 任务运行 `/clubpoints/admin/job-run`，支持任务列表、详情、重试、人工处理，人工处理填原因。
- [ ] 员工和负责人菜单不出现导出按钮，导出带筛选条件并触发强审计。

验收：

- [ ] 只有管理员有导出按钮，导出触发审计。
- [ ] 失败任务能查看原因、重试或人工处理。

## 任务 M11.14 前端验证

- [ ] 验证 `pnpm install`。
- [ ] 验证 `pnpm dev` 端口 `8889`。
- [ ] 验证关键页面能打开。
- [ ] 验证登录后菜单来自后端 seed。
- [ ] 验证按钮权限隐藏。
- [ ] 验证接口错误提示。
- [ ] 不把上游 TypeScript 存量债误判为 clubpoints 完成阻塞，除非影响新增页面。

验收：

- [ ] 三类角色页面都能访问对应入口。
- [ ] 新增页面没有明显运行时错误。

## M11 放行标准

- [x] 报表查询可用。
- [x] 导出写强审计。
- [x] 任务监控可用。
- [x] 通知待办可用。
- [ ] 员工、负责人、管理员前端闭环可用。

## M11 不通过时禁止

- [ ] 禁止做最终演示。
- [ ] 禁止说 MVP 可交付。
