# M13 可用性与前后端契约硬化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `subagent-driven-development` (recommended) or `executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把 M11/M12 后遗留的前后端契约缺口、技术语言外露、三角色入口不清、按钮假可用问题收口到机器可验证状态。

**Architecture:** M13 不新增新平台、不改变芋道动态菜单架构、不重做账本或活动结算模型。先用 hardening 测试把前端 API wrapper、页面直接请求、权限 seed、后端 mapping、页面可见语言、菜单路由全部纳入机器门禁，再补后端缺失接口，最后产品化前端三角色体验和端到端验收。

**Tech Stack:** Maven / JUnit 5 / Spring MVC reflection / Java file scanner / Vue3 / Element Plus / Vite 8889 / Playwright or browser verification / MySQL seed SQL.

## Global Constraints

- 不提交 git，除非用户明确要求。
- 不跑 full build，除非用户明确要求。
- 不添加 co-author 或 AI 元数据。
- 不引入新框架、新平台、新迁移工具。
- 不恢复已删除的芋道业务模块。
- 路由和菜单继续以后端菜单 seed 为事实源。
- 页面不得散写 `request.get` / `request.post` / `request.put` / `request.delete` / `request.download` 或业务 URL，必须经 `src/api/clubpoints/**/*.ts` 调用。
- 前端按钮隐藏不算安全边界；后端 Controller 必须有 `@PreAuthorize`，Service 必须做数据范围、强审计和事务边界。
- seed 幂等机制不是 M13 的问题源，也不是 M13 的改造目标；M13 只校准菜单/权限事实源、当前 seed 结果和前端动态菜单显示。

---

## M13 放行标准

以下标准全部满足才允许 M13 标记完成。每一条都必须有机器可判定断言，不能写成“体验正常”“不能假成功”这种口号。

| 门禁 | 机器断言 | 失败含义 |
| --- | --- | --- |
| API wrapper 契约 | `ClubPointFrontendApiWrapperContractHardeningTest` 扫描 `src/api/clubpoints/**/*.ts`，每个 `request.get/post/put/delete/download` 的 `method + /clubpoints/**` 必须存在后端 Controller mapping；缺失列表长度必须为 0 | 前端按钮可能打到 404/405，属于假可用 |
| 页面不得散写请求 | `ClubPointViewNoDirectRequestHardeningTest` 独立扫描 `src/views/clubpoints/**/*.vue`，`@/config/axios`、`request.get/post/put/delete/download`、`url: '/clubpoints/` 命中数必须为 0；这个门禁和 API wrapper mapping 门禁必须同时跑 | 页面绕过 API wrapper，未来 contract 测试会漏 |
| 用户语言 | `ClubPointVisibleLanguageHardeningTest` 扫描 `src/views/clubpoints/**/*.vue` 的 template，可见禁用词命中数必须为 0；允许项必须逐条写入精确白名单 | 页面把数据库字段、研发变量、ID 语言暴露给用户 |
| 活动积分发放产品化 | `ClubPointSettlementProductLanguageHardeningTest` 同时断言前端 settlement 页面文案、菜单 seed、后端 `@Tag` / `@Operation(summary)`、后端可见枚举名、错误码文案；旧词 `活动结算`、`待结算`、`已结算`、`手动生成活动积分`、`手动生成积分`、`待发放活动` 命中数必须为 0 | 前后端命名不一致，用户不知道按钮用途 |
| 菜单路由 | `ClubPointMenuRouteContractHardeningTest` 扫描 seed 菜单名称、component、permission，三角色入口必须是 `员工积分中心`、`负责人工作台`、`积分管理后台`，component 文件必须存在 | 动态菜单事实源错误或三角色入口不清 |
| 权限一致 | `ClubPointPermissionEndpointContractHardeningTest` 扫描前端 `v-hasPermi`、seed 权限码、后端 `@PreAuthorize`，三者集合必须一致 | 按钮能看不能点、能点但后端拒绝或后端裸奔 |
| 静默失败 | `ClubPointFrontendSilentFailureHardeningTest` 扫描 `src/views/clubpoints/**/*.vue`，业务页面裸 `catch {}` 命中数必须为 0；取消弹窗必须显式识别取消，不得吞业务异常 | 点按钮失败后无提示、无状态变化、用户以为成功 |
| 放行命令 | M13 hardening tests、补接口 Controller/Service tests、clubpoints 前端路径类型过滤全部通过 | 任何一项失败，M13 不得标记 `[x]` |

## 数据库 seed 处理

M13 不把 seed 幂等当成新风险点。M1/M2 已经验证过 seed 幂等，M13 的 seed 工作只做三件事：

- 更新菜单名称、组件路径、权限码，保持芋道动态菜单作为前端路由事实源。
- 用机器测试验证 seed 结果符合三角色菜单口径，而不是重新设计 seed 写入机制。
- 如果修改 seed，只验证“重复导入不破坏当前结果”和“查询结果符合 M13 菜单标准”，不再把幂等本身展开成独立改造任务。

## 用户语言标准

禁止普通业务页把数据库视角或研发变量名作为用户可见文案。

### 禁止作为可见文案出现

```text
runKey
resultJson
requestNo
bizId
idempotencyKey
settlementRunId
活动ID
俱乐部ID
员工ID
报名ID
流水ID
运行ID
业务ID
幂等键
```

### 精确白名单

白名单不能泛化为“管理员页允许”，也不能写“技术字段”等模糊词。每条白名单必须登记精确文件、精确词、可见位置和业务原因；列表、搜索区、按钮、弹窗标题默认不得白名单。

| 文件 | 允许词 | 允许位置 | 原因 |
| --- | --- | --- | --- |
| `src/views/clubpoints/admin/audit/index.vue` | `bizId` | 默认折叠的“技术诊断信息”区域 | 审计追溯需要定位原始业务对象，普通列表和搜索区必须改为业务语言 |
| `src/views/clubpoints/admin/audit/index.vue` | `targetSnapshotJson` | 默认折叠的“技术诊断信息”区域 | 审计详情需要查看原始快照，必须默认折叠 |
| `src/views/clubpoints/admin/audit/index.vue` | `beforeJson` | 默认折叠的“技术诊断信息”区域 | 审计详情需要查看变更前快照，必须默认折叠 |
| `src/views/clubpoints/admin/audit/index.vue` | `afterJson` | 默认折叠的“技术诊断信息”区域 | 审计详情需要查看变更后快照，必须默认折叠 |
| `src/views/clubpoints/admin/job-run/index.vue` | `runKey` | 默认折叠的“技术诊断信息”区域 | 任务异常处理需要定位后台批次，列表和搜索区必须改为“任务批次” |
| `src/views/clubpoints/admin/job-run/index.vue` | `bizId` | 默认折叠的“技术诊断信息”区域 | 任务异常处理需要定位后台业务对象，列表和搜索区必须改为业务语言 |
| `src/views/clubpoints/admin/job-run/index.vue` | `idempotencyKey` | 默认折叠的“技术诊断信息”区域 | 任务异常处理需要排查重复执行，必须默认折叠 |
| `src/views/clubpoints/admin/job-run/index.vue` | `resultJson` | 默认折叠的“技术诊断信息”区域 | 任务异常处理需要查看原始运行结果，必须默认折叠 |

## 三角色菜单最终口径

```text
俱乐部积分
- 员工积分中心
  - 我的积分
  - 我的俱乐部
  - 活动报名签到
  - 积分兑换
  - 我的异议
  - 我的通知

- 负责人工作台
  - 负责人首页
  - 负责俱乐部
  - 活动管理
  - 报名与签到
  - 非签到积分材料

- 积分管理后台
  - 管理员首页
  - 规则配置
  - 俱乐部管理
  - 活动审核与管理
  - 活动积分发放
  - 积分账户
  - 积分流水
  - 非签到材料审核
  - 管理员代录
  - 兑换批次
  - 礼品维护
  - 兑换审核
  - 异议处理
  - 年度清零
  - 年度排名与激励
  - 预算记录
  - 报表中心
  - 审计日志
  - 任务异常处理
```

## 活动积分发放命名口径

前后端必须使用同一套业务语言：

- 菜单：`活动积分发放`
- 页面标题：`活动积分发放`
- Tab：`待自动发放`、`发放记录`
- 按钮：`异常补发/重跑`
- 弹窗标题：`异常补发活动积分`
- 后端 `@Tag`：`管理后台 - 活动积分发放`
- 后端 `@Operation(summary)`：`待自动发放活动分页`、`异常补发或重跑活动积分`、`活动积分发放记录分页`、`活动积分发放明细`
- 后端可见枚举：活动状态 `已发放`；活动积分发放状态 `待发放 / 发放中 / 已发放 / 发放失败 / 人工处理`；流水来源 `活动积分发放`；规则项名 `活动发放缓冲分钟`
- 后端错误码文案：不得出现 `活动结算`、`待结算`、`已结算`、`手动生成活动积分`、`手动生成积分`
- 页面提示：正常活动由系统自动发放，本入口只用于自动任务失败、签到/签退修正后补发、活动状态异常收口。

---

### Task M13.1: 文档和里程碑入口

**Files:**
- Create: `docs/development-milestones/M13-usability-contract-hardening.md`
- Modify: `docs/development-milestones/00-index.md`
- Modify: `docs/development-milestones/01-superpowers-execution-rules.md`
- Modify: `docs/startup/08-implementation-order.md`
- Modify: `docs/club-points-development-plan.md`
- Modify: `docs/club-points-api-design.md`
- Modify: `docs/club-points-frontend-page-design.md`
- Modify: `docs/club-points-functions-and-permissions.md`
- Modify: `docs/club-points-flow-design.md`

**Interfaces:**
- Consumes: M0-M12 完成事实、M13 用户校准要求。
- Produces: M13 执行入口、三角色菜单标准、契约测试标准。

- [ ] RED: 检查 `docs/development-milestones/00-index.md` 不包含 M13，确认当前计划入口缺失。
- [ ] GREEN: 新增本 M13 文件，更新索引和总开发计划。
- [ ] Verify GREEN: `Select-String` 确认 M13 在索引、总计划、启动实现顺序中可见。
- [ ] Checkpoint: 列出文档变更，不提交 git。

### Task M13.2: 前端 API wrapper 到后端 mapping 契约测试

**Files:**
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/hardening/ClubPointFrontendApiWrapperContractHardeningTest.java`
- Modify: backend Controller only when RED exposes missing mapping.

**Interfaces:**
- Consumes: `src/api/clubpoints/**/*.ts` 的 `request.get/post/put/delete/download`。
- Produces: `method + normalizedPath -> Controller mapping` 契约。

- [ ] RED: 写测试扫描 API wrapper，当前必须暴露至少这些缺口：`POST /clubpoints/app/club/join`、`POST /clubpoints/app/club/exit`、`GET /clubpoints/leader/registration/page`、`POST /clubpoints/leader/registration/mark-special-absence`、`GET /clubpoints/leader/attendance/page`、`POST /clubpoints/leader/attendance/supplement`、`POST /clubpoints/leader/attendance/correct`、`PUT /clubpoints/leader/club/update`、`GET /clubpoints/leader/member/page`、`POST /clubpoints/ledger/adjust`、`POST /clubpoints/ledger/reverse`。
- [ ] Verify RED: 运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointFrontendApiWrapperContractHardeningTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`，确认失败原因是前端 API 缺后端 mapping。
- [ ] GREEN: 按 Task M13.5 补齐后端接口或删除确认为死入口的 API wrapper。
- [ ] Verify GREEN: 同一测试返回 `BUILD SUCCESS`。
- [ ] Checkpoint: 缺口清单数量必须从 RED 的非 0 变为 0。

### Task M13.3: 页面不得散写请求和业务 URL

**Files:**
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/hardening/ClubPointViewNoDirectRequestHardeningTest.java`

**Interfaces:**
- Consumes: `src/views/clubpoints/**/*.vue`。
- Produces: 页面只能调用 API wrapper 的静态门禁。

- [ ] RED: 测试扫描 `src/views/clubpoints/**/*.vue` 的 `import '@/config/axios'`、`request.get`、`request.post`、`request.put`、`request.delete`、`request.download`、`url: '/clubpoints/`、`url: \`/clubpoints/`。
- [ ] Verify RED: 当前应通过；如果通过，记录它是防回归门禁，不是当前缺口。
- [ ] GREEN: 无需改页面，除非扫描命中直接请求。
- [ ] Verify GREEN: 测试返回 `BUILD SUCCESS`，命中数量为 0。
- [ ] Checkpoint: 说明 `AttachmentInput.vue` 的附件 `url` 字段不属于直接请求，不误伤。

### Task M13.4: 用户语言和菜单路由契约测试

**Files:**
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/hardening/ClubPointVisibleLanguageHardeningTest.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/hardening/ClubPointMenuRouteContractHardeningTest.java`
- Modify: `ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql`
- Modify: `ruoyi-vue-pro-github/yudao-ui/yudao-ui-admin-vue3/src/views/clubpoints/**/*.vue`

**Interfaces:**
- Consumes: seed 菜单、Vue template 可见文案。
- Produces: 三角色菜单和页面文案门禁。

- [ ] RED: 用户语言测试扫描 template 可见标签、标题、placeholder、alert、table column、dialog title，暴露技术词。
- [ ] RED: 菜单路由测试扫描 seed 菜单 component，断言组件文件存在、三角色入口命名符合标准。
- [ ] Verify RED: 运行对应 hardening tests，确认失败点是技术词或菜单命名。
- [ ] GREEN: 更新 seed 菜单、页面文案、折叠技术诊断区域。
- [ ] Verify GREEN: 两个测试返回 `BUILD SUCCESS`。
- [ ] Checkpoint: 普通业务页技术词命中 0，白名单逐条登记。

### Task M13.5: 缺失后端业务接口补齐

**Files:**
- Modify/Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/app/club/*`
- Modify/Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/leader/club/*`
- Modify/Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/leader/registration/*`
- Modify/Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/leader/attendance/*`
- Modify/Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/ledger/*`
- Test: controller/service tests for each missing action.

**Interfaces:**
- Consumes: M5 Member/Leader Service、M6 Registration/Attendance Service、M4 Ledger adjustment/reverse Service、M2 scope/audit/notify。
- Produces: 前端按钮可调用的真实业务接口。

- [ ] RED: 员工加入/退出俱乐部 Controller 测试失败，原因是接口不存在。
- [ ] GREEN: 补 `POST /clubpoints/app/club/join`、`POST /clubpoints/app/club/exit`，调用本人范围 Service。
- [ ] RED: 负责人负责俱乐部修改/成员分页 Controller 测试失败，原因是接口不存在。
- [ ] GREEN: 补 `PUT /clubpoints/leader/club/update`、`GET /clubpoints/leader/member/page`，只允许负责俱乐部范围。
- [ ] RED: 负责人报名签到修正 Controller 测试失败，原因是接口不存在。
- [ ] GREEN: 补 `GET /clubpoints/leader/registration/page`、`GET /clubpoints/leader/attendance/page`、`POST /clubpoints/leader/attendance/supplement`、`POST /clubpoints/leader/attendance/correct`、`POST /clubpoints/leader/registration/mark-special-absence`。
- [ ] RED: 管理员账本调整/撤销 Controller 测试失败，原因是接口不存在。
- [ ] GREEN: 补 `POST /clubpoints/ledger/adjust`、`POST /clubpoints/ledger/reverse`，强审计、幂等、通知沿用 Service。
- [ ] Verify GREEN: 对应 Controller / Service tests 和 `ClubPointFrontendApiWrapperContractHardeningTest` 返回 `BUILD SUCCESS`。
- [ ] Checkpoint: 前端 API wrapper 缺后端 mapping 数量为 0。

### Task M13.6: 前端业务可用性和错误反馈

**Files:**
- Modify: `ruoyi-vue-pro-github/yudao-ui/yudao-ui-admin-vue3/src/views/clubpoints/**/*.vue`
- Modify: `ruoyi-vue-pro-github/yudao-ui/yudao-ui-admin-vue3/src/api/clubpoints/**/*.ts`
- Modify: `ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/hardening/ClubPointFrontendSilentFailureHardeningTest.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/hardening/ClubPointSettlementProductLanguageHardeningTest.java`

**Interfaces:**
- Consumes: Task M13.5 后端接口。
- Produces: 三角色真实业务页面。

- [ ] RED: `ClubPointFrontendSilentFailureHardeningTest` 静态扫描 `catch {}`，当前命中必须失败。
- [ ] GREEN: 所有业务提交失败必须显示错误、保留弹窗、停止 loading；不能吞异常。
- [ ] RED: 用户语言测试暴露技术词。
- [ ] GREEN: 页面改为员工、俱乐部、活动、流水编号、提交编号、任务批次等业务语言；技术字段默认隐藏到诊断折叠区。
- [ ] RED: `ClubPointSettlementProductLanguageHardeningTest` 暴露前后端 `@Tag` / `@Operation(summary)` 和前端文案不一致。
- [ ] GREEN: 菜单 seed、页面标题、Tab、按钮、弹窗、后端 `@Operation(summary)`、错误码文案统一到“活动积分发放/异常补发/重跑”。
- [ ] Verify GREEN: `ClubPointViewNoDirectRequestHardeningTest`、`ClubPointVisibleLanguageHardeningTest`、菜单路由测试通过。
- [ ] Checkpoint: 页面直接请求数量 0，`catch {}` 数量 0。

### Task M13.7: 最终验证和文档收口

**Files:**
- Modify: `docs/development-milestones/M13-usability-contract-hardening.md`
- Modify: `docs/club-points-development-plan.md`
- Modify: affected design docs.

**Interfaces:**
- Consumes: M13.1-M13.6 验证证据。
- Produces: M13 放行记录。

- [ ] Verify: 运行 M13 hardening tests。
- [ ] Verify: 运行涉及补接口的 Controller / Service tests。
- [ ] Verify: 运行 clubpoints 前端路径类型过滤。
- [ ] REFACTOR: 只清理命名、重复、测试工具函数，不新增范围。
- [ ] Checkpoint: 记录通过、失败、跳过原因；不提交 git。

---

## M13 放行记录

2026-06-29 已放行，证据如下：

- `ClubPointClubQueryControllerTest` 运行 7 个测试，失败 0，错误 0；确认 app club Controller 源码层和测试环境可用。
- `mvn -pl yudao-server -am -DskipTests "-Dflatten.skip=true" compile` 返回 `BUILD SUCCESS`；刷新后端 `target/classes` 后仅重启本项目后端进程，live `/clubpoints/app/club/my-list` 从业务 `code=500` 恢复为 `code=0`。
- live `club_points` 数据库已重放 `club-points-seed.sql`，员工菜单查询结果不再包含 `员工工作台`，`1300010140` 名称为 `活动报名签到`。
- M13 hardening 合集以 API wrapper、页面散写、权限端点、用户语言、静默失败和菜单路由为准。
- 前端类型过滤：`pnpm --dir ruoyi-vue-pro-github\yudao-ui\yudao-ui-admin-vue3 exec vue-tsc --noEmit --pretty false` 全量仍因非 clubpoints 存量债退出 1，但过滤 `src/api/clubpoints`、`src/views/clubpoints` 和 `clubpoints` 命中 0。
