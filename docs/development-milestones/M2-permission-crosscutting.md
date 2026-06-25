# M2 权限范围和横切能力 Implementation Plan

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

- [ ] 按 `docs/club-points-functions-and-permissions.md` 梳理全部权限码。
- [ ] 在 `club-points-seed.sql` 写菜单。
- [ ] 在 `club-points-seed.sql` 写按钮权限。
- [ ] 在 `club-points-seed.sql` 写员工、负责人、管理员角色建议授权。
- [ ] 权限码统一 `clubpoints:<resource>:<action>`。
- [ ] 前端菜单路径和 `docs/club-points-frontend-page-design.md` 一致。

验收：

- [ ] 管理员菜单完整。
- [ ] 负责人没有全局审核、全局导出、规则配置权限。
- [ ] 员工没有管理权限。

## 任务 M2.2 字典常量

- [ ] 创建 `enums/DictTypeConstants.java`。
- [ ] 添加活动状态字典。
- [ ] 添加报名状态字典。
- [ ] 添加签到状态字典。
- [ ] 添加规则版本状态字典。
- [ ] 添加积分流水类型字典。
- [ ] 添加冻结状态字典。
- [ ] 添加兑换状态字典。
- [ ] 添加异议状态字典。
- [ ] 添加年度处理状态字典。
- [ ] 同步 seed 字典类型和字典值。

验收：

- [ ] 后端常量和 seed 字典类型一致。
- [ ] 不在前端硬编码中文状态。

## 任务 M2.3 ClubScopeService

- [ ] 创建 `service/scope/ClubScopeService.java`。
- [ ] 创建 `service/scope/ClubScopeServiceImpl.java`。
- [ ] 实现本人范围校验。
- [ ] 实现已加入俱乐部范围校验。
- [ ] 实现负责俱乐部范围校验。
- [ ] 实现管理员全局范围判断。
- [ ] 统一抛出 clubpoints 错误码。
- [ ] 写负责人越权访问其他俱乐部的测试。

验收：

- [ ] 权限判断先动作权限，再数据范围。
- [ ] Service 层能复用范围校验。
- [ ] 前端隐藏按钮不影响后端拦截。

## 任务 M2.4 ClubAuditService

- [ ] 创建 `service/audit/ClubAuditService.java`。
- [ ] 创建 `service/audit/ClubAuditServiceImpl.java`。
- [ ] 支持记录动作类型。
- [ ] 支持记录业务对象类型和 ID。
- [ ] 支持记录操作者和 IP。
- [ ] 支持记录请求参数摘要。
- [ ] 支持记录变更前后快照。
- [ ] 支持记录成功或失败结果。
- [ ] 审计写入失败时抛异常。
- [ ] 写事务回滚测试：强审计失败则业务失败。

验收：

- [ ] 强审计和业务写入在同一事务。
- [ ] 通知失败不影响审计。
- [ ] 导出、删除、审核、调整、清零都有动作枚举。

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
- [ ] `ClubScopeService` 可用。
- [ ] `ClubAuditService` 可用。
- [ ] `ClubAttachmentService` 可用。
- [ ] `ClubNotifyService` 可用。
- [ ] 横切测试通过。

## M2 不通过时禁止

- [ ] 禁止写规则发布接口。
- [ ] 禁止写账本。
- [ ] 禁止写活动、兑换、年度业务。
