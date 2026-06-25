# M11 报表、任务监控、通知、前端收口 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将后端闭环暴露给三类角色前端页面，完成报表导出、任务监控、通知待办和工作台。

**Architecture:** 后端菜单 seed 是路由事实源，前端按角色组织 API 和页面。报表基于流水和业务快照查询，导出只写强审计，不新增导出主表。

**Tech Stack:** Vue3、Vite、Element Plus、RuoYi axios 封装、Spring Excel、MyBatis、强审计、Playwright 或页面验证。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 前端页面单一输入是 `docs/club-points-frontend-page-design.md`。
- 前端入口端口固定 `8889`。
- 路由来自后端菜单 seed。
- `v-hasPermi` 只改善体验，不是安全边界。
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

- Consumes: M4 流水、M5 俱乐部、M9 兑换、M10 年度排名和预算、M2 强审计、后端菜单 seed。
- Produces: 报表查询和导出接口、任务监控接口、三类角色前端 API 和页面入口。

**Verification:**

- Run: 报表导出审计测试。
- Expected: 导出成功写强审计，审计失败导出失败。
- Run: `Invoke-WebRequest http://127.0.0.1:8889`
- Expected: 前端入口 HTTP `200`。
- Run: 三类角色页面主流程验证。
- Expected: 员工、负责人、管理员只看到各自权限范围内页面和按钮。

## 目标

把后端业务闭环暴露给前端页面，完成报表导出、任务监控、通知待办和三类角色工作台。

## 前置条件

- M10 已放行。
- `docs/club-points-frontend-page-design.md` 是前端页面单一输入。
- 后端 API 路径和 VO 字段已稳定。
- 菜单 seed 已有。

## 任务 M11.1 报表查询

- [ ] 管理员查询积分明细报表。
- [ ] 管理员查询总台账报表。
- [ ] 管理员查询兑换记录报表。
- [ ] 管理员查询俱乐部排名报表。
- [ ] 管理员查询预算报表。
- [ ] 报表基于流水和业务快照查询。
- [ ] 不新增导出主表。

验收：

- [ ] 报表数据来源明确。
- [ ] 员工和负责人不能导出报表。

## 任务 M11.2 报表导出

- [ ] 使用芋道 Excel 能力导出。
- [ ] 导出前校验管理员权限。
- [ ] 导出动作写强审计。
- [ ] 审计失败则导出失败。
- [ ] 导出文件不落业务主表。
- [ ] 导出字段和前端页面设计一致。

验收：

- [ ] 导出成功有审计记录。
- [ ] 越权导出失败。
- [ ] 导出不产生独立业务表。

## 任务 M11.3 任务监控

- [ ] 管理员查看 `club_points_job_run` 列表。
- [ ] 管理员查看任务运行详情。
- [ ] 管理员重试失败任务。
- [ ] 重试必须保持幂等。
- [ ] 人工重试写强审计。

验收：

- [ ] 结算任务可追踪。
- [ ] 年度清零任务可追踪。
- [ ] 失败任务可重试。

## 任务 M11.4 通知和待办

- [ ] 活动审核结果通知。
- [ ] 积分到账通知。
- [ ] 兑换审核结果通知。
- [ ] 异议处理结果通知。
- [ ] 待审核材料待办。
- [ ] 待审核兑换待办。
- [ ] 待处理异议待办。

验收：

- [ ] 通知失败不回滚主业务。
- [ ] 待办数量和业务状态一致。

## 任务 M11.5 前端 API 模块

- [ ] 创建 `src/api/clubpoints/app/`。
- [ ] 创建 `src/api/clubpoints/leader/`。
- [ ] 创建 `src/api/clubpoints/admin/`。
- [ ] 创建 `src/api/clubpoints/shared/`。
- [ ] 请求封装复用 `@/config/axios`。
- [ ] 类型定义和后端 VO 一致。
- [ ] 兑换申请、管理员代录、积分调整生成并复用 `requestNo`。

验收：

- [ ] API 文件按角色和领域分组。
- [ ] 不在页面里散写 URL。

## 任务 M11.6 员工前端页面

- [ ] 员工首页工作台。
- [ ] 本人积分余额。
- [ ] 本人积分明细。
- [ ] 可加入俱乐部。
- [ ] 本人俱乐部。
- [ ] 活动列表和详情。
- [ ] 报名、取消报名、签到、签退。
- [ ] 兑换列表、申请、记录。
- [ ] 异议提交和记录。

验收：

- [ ] 员工只看到本人数据。
- [ ] 重试提交不重新生成 requestNo。
- [ ] 页面按钮和权限一致。

## 任务 M11.7 负责人前端页面

- [ ] 负责人工作台。
- [ ] 负责俱乐部概览。
- [ ] 成员列表。
- [ ] 活动管理。
- [ ] 报名名单。
- [ ] 签到情况。
- [ ] 非签到材料提交。
- [ ] 材料记录。

验收：

- [ ] 负责人不能看到其他俱乐部数据。
- [ ] 负责人没有兑换审核、报表导出、规则配置入口。

## 任务 M11.8 管理员前端页面

- [ ] 管理员工作台。
- [ ] 俱乐部管理。
- [ ] 成员管理。
- [ ] 负责人管理。
- [ ] 活动审核和修正。
- [ ] 规则版本配置。
- [ ] 积分流水和调整。
- [ ] 非签到审核。
- [ ] 兑换批次、礼品、资格、审核。
- [ ] 异议处理。
- [ ] 年度清零、排名、激励、预算。
- [ ] 报表导出。
- [ ] 审计日志。
- [ ] 任务监控。

验收：

- [ ] 页面和 `club-points-frontend-page-design.md` 一致。
- [ ] 强确认只用于物理删除俱乐部。
- [ ] 按钮使用 `v-hasPermi`，但后端仍做权限校验。

## 任务 M11.9 前端验证

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

- [ ] 报表查询可用。
- [ ] 导出写强审计。
- [ ] 任务监控可用。
- [ ] 通知待办可用。
- [ ] 员工、负责人、管理员前端闭环可用。

## M11 不通过时禁止

- [ ] 禁止做最终演示。
- [ ] 禁止说 MVP 可交付。
