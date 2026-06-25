# M5 俱乐部、成员、负责人 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现俱乐部主数据、成员关系、负责人关系，为活动、积分范围、负责人权限提供组织基础。

**Architecture:** 俱乐部、成员、负责人是独立主数据域。数据范围由 `ClubScopeService` 读取成员和负责人关系判断，历史业务记录依赖快照字段，不依赖当前主数据实时 join。

**Tech Stack:** Spring Service、MyBatis、RuoYi Controller/VO、强审计、JUnit。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 负责人权限不能只靠前端菜单控制。
- 物理删除俱乐部必须强确认和强审计。
- 删除或移除不能破坏历史快照。
- Java 行为必须 TDD。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/club/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/club/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/club/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/app/club/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/club/`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ErrorCodeConstants.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/club/`

**Interfaces:**

- Consumes: M1 俱乐部表、M2 数据范围和强审计、system 用户部门数据。
- Produces: 俱乐部、成员、负责人 Service 和查询 API，供 M6 活动范围、M10 排名使用。

**Verification:**

- Run: 俱乐部成员负责人 Service 单测。
- Expected: 重复加入失败、负责人越权失败、删除强确认和快照测试通过。
- Run: 数据范围测试。
- Expected: 移除负责人后权限范围立即收缩。

## 目标

实现俱乐部主数据、成员关系、负责人关系，作为后续活动、积分、范围权限的组织基础。

## 前置条件

- M4 已放行。
- `ClubScopeService` 可用。
- 强审计可用。
- 账本查询能力可用。

## 任务 M5.1 DO 和 Mapper

- [ ] 创建 `ClubPointClubDO`。
- [ ] 创建 `ClubPointClubMemberDO`。
- [ ] 创建 `ClubPointClubLeaderDO`。
- [ ] 创建对应 Mapper。
- [ ] 字段和 M1 DDL 一致。
- [ ] 保存用户、部门、俱乐部快照字段。

验收：

- [ ] 成员关系可查询。
- [ ] 负责人关系可查询。
- [ ] 历史快照不依赖当前用户资料实时变化。

## 任务 M5.2 枚举和错误码

- [ ] 创建俱乐部状态枚举。
- [ ] 创建成员状态枚举。
- [ ] 创建负责人状态枚举。
- [ ] 补充错误码：俱乐部不存在、俱乐部已停用、成员已存在、成员不存在、负责人已存在、负责人不存在、无权限操作俱乐部。

验收：

- [ ] 业务失败有明确错误码。
- [ ] 不用通用异常糊弄业务状态。

## 任务 M5.3 ClubService

- [ ] 创建俱乐部。
- [ ] 修改俱乐部基础信息。
- [ ] 停用俱乐部。
- [ ] 启用俱乐部。
- [ ] 物理删除俱乐部。
- [ ] 物理删除俱乐部必须强确认。
- [ ] 物理删除俱乐部必须写强审计。
- [ ] 有历史活动、历史流水、历史兑换关联时按设计限制删除或保留快照。

验收：

- [ ] 普通停用不需要强确认。
- [ ] 物理删除必须输入强确认文本。
- [ ] 删除后历史记录仍可读。

## 任务 M5.4 MemberService

- [ ] 员工申请加入俱乐部。
- [ ] 管理员添加成员。
- [ ] 管理员移除成员。
- [ ] 员工退出俱乐部。
- [ ] 成员状态变更写审计。
- [ ] 成员变更刷新负责人数据范围查询基础。

验收：

- [ ] 同一用户不能重复成为同一俱乐部有效成员。
- [ ] 退出后不能继续报名仅限成员的活动。
- [ ] 移除成员不删除历史活动和流水。

## 任务 M5.5 LeaderService

- [ ] 管理员设置负责人。
- [ ] 管理员移除负责人。
- [ ] 负责人必须是有效用户。
- [ ] 负责人不一定必须是普通成员，但必须按产品口径固定。
- [ ] 设置和移除负责人写强审计。
- [ ] 负责人数据范围立即生效。

验收：

- [ ] 负责人只能管理负责俱乐部。
- [ ] 负责人不能设置或移除负责人。
- [ ] 移除负责人后权限范围立即收缩。

## 任务 M5.6 查询 API

- [ ] 员工查询可加入俱乐部列表。
- [ ] 员工查询本人俱乐部。
- [ ] 负责人查询负责俱乐部。
- [ ] 管理员查询俱乐部列表。
- [ ] 管理员查询俱乐部详情。
- [ ] 管理员查询成员列表。
- [ ] 管理员查询负责人列表。

验收：

- [ ] 员工只能看本人相关数据。
- [ ] 负责人只能看负责俱乐部数据。
- [ ] 管理员可全局查询。

## 任务 M5.7 测试

- [ ] 测试创建俱乐部。
- [ ] 测试停用俱乐部后不能新增业务。
- [ ] 测试物理删除强确认。
- [ ] 测试成员加入和退出。
- [ ] 测试重复加入失败。
- [ ] 测试负责人设置和移除。
- [ ] 测试负责人越权失败。
- [ ] 测试删除后历史快照仍可读。

验收：

- [ ] 主数据测试通过。
- [ ] 数据范围测试通过。
- [ ] 强审计测试通过。

## M5 放行标准

- [ ] 俱乐部 CRUD 可用。
- [ ] 成员关系可用。
- [ ] 负责人关系可用。
- [ ] 数据范围可基于负责人关系判断。
- [ ] 删除和移除都有审计和快照策略。

## M5 不通过时禁止

- [ ] 禁止写活动报名。
- [ ] 禁止写负责人活动管理。
- [ ] 禁止写俱乐部排名。
