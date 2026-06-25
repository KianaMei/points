# M5 俱乐部、成员、负责人 Implementation Plan

**Status:** `[~]` M5.1-M5.3 已完成并有 RED/GREEN 证据；当前入口是 M5.4 MemberService。

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

### Task M5.1: DO 和 Mapper

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/club/ClubPointClubDO.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/club/ClubPointClubMapper.java`
- Existing: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/club/ClubMemberDO.java`
- Existing: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/club/ClubLeaderDO.java`
- Existing: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/club/ClubMemberMapper.java`
- Existing: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/club/ClubLeaderMapper.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/club/ClubPointClubMapperTest.java`

**Interfaces:**

- Consumes: M1 `club_points_club`、`club_points_club_member`、`club_points_club_leader` DDL，M2 已落地的 `ClubScopeService` 成员/负责人范围查询。
- Produces: `ClubPointClubDO`、`ClubPointClubMapper.selectByCode(...)`，以及可继续复用的 `ClubMemberDO`、`ClubLeaderDO`、`ClubMemberMapper`、`ClubLeaderMapper`。
- Decision: M2 已经为 `ClubScopeService` 创建 `ClubMemberDO` 和 `ClubLeaderDO`，并已被范围服务、账本查询测试使用；M5.1 不重复创建 `ClubPointClubMemberDO` / `ClubPointClubLeaderDO`，避免同表双模型和范围服务漂移。

- [x] RED: 写失败测试或失败验证
- [x] Verify RED: 运行命令，确认失败原因正确
- [x] GREEN: 写最小实现
- [x] Verify GREEN: 运行命令，确认通过
- [x] REFACTOR: 只在绿色后清理命名、重复、结构
- [x] Checkpoint: 列出变更文件和验证证据，不提交 git

- [x] 创建 `ClubPointClubDO`。
- [x] 创建成员关系 DO。M2 已创建并复用 `ClubMemberDO`。
- [x] 创建负责人关系 DO。M2 已创建并复用 `ClubLeaderDO`。
- [x] 创建对应 Mapper。
- [x] 字段和 M1 DDL 一致。
- [x] 保存用户、部门、俱乐部快照字段。

验收：

- [x] 成员关系可查询。
- [x] 负责人关系可查询。
- [x] 历史快照不依赖当前用户资料实时变化。

证据：

- RED：新增 `ClubPointClubMapperTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointClubMapperTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointClubDO` 和 `ClubPointClubMapper` 不存在，符合 M5.1 RED 预期。
- GREEN：新增 `ClubPointClubDO` 映射 `club_points_club` 全部业务字段；新增 `ClubPointClubMapper` 并提供按 `code` 查询；成员/负责人关系沿用 M2 已落地的 `ClubMemberDO`、`ClubLeaderDO` 和 Mapper。
- M5.1 单测验证：同一命令返回 `BUILD SUCCESS`；`ClubPointClubMapperTest` 运行 `1` 个测试，失败 `0`，错误 `0`。
- M5 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointClubMapperTest,ClubScopeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `11` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` 无输出；club DO/Mapper/Test 范围 `tenant_id|TenantBaseDO` 无命中；AI/co-author 元数据模式无命中。

## 任务 M5.2 枚举和错误码

- [x] 创建俱乐部状态枚举。
- [x] 创建成员状态枚举。
- [x] 创建负责人状态枚举。
- [x] 补充错误码：俱乐部不存在、俱乐部已停用、成员已存在、成员不存在、负责人已存在、负责人不存在、无权限操作俱乐部。

验收：

- [x] 业务失败有明确错误码。
- [x] 不用通用异常糊弄业务状态。

证据：

- RED：新增 `ClubPointClubEnumTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointClubEnumTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointClubStatusEnum`、`ClubPointMemberStatusEnum`、`ClubPointLeaderStatusEnum` 和 M5 俱乐部错误码不存在，符合 M5.2 RED 预期。
- GREEN：新增俱乐部状态、成员状态、负责人状态 3 个枚举，枚举值与 `club-points-seed.sql` 字典保持一致；补充 `CLUB_NOT_FOUND`、`CLUB_DISABLED`、`CLUB_ALREADY_JOINED`、`CLUB_NOT_MEMBER`、`CLUB_LEADER_ALREADY_EXISTS`、`CLUB_LEADER_NOT_EXISTS`，并将 `CLUB_SCOPE_DENIED` 文案泛化为俱乐部数据权限。
- M5.2 单测验证：同一命令返回 `BUILD SUCCESS`；`ClubPointClubEnumTest` 运行 `4` 个测试，失败 `0`，错误 `0`。
- M5 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointClubMapperTest,ClubPointClubEnumTest,ClubScopeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `15` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` exit `0`，仅 CRLF 提示；枚举范围 `tenant_id|TenantBaseDO` 无命中；枚举范围 AI/co-author 元数据模式无命中。

## 任务 M5.3 ClubService

- [x] 创建俱乐部。
- [x] 修改俱乐部基础信息。
- [x] 停用俱乐部。
- [x] 启用俱乐部。
- [x] 物理删除俱乐部。
- [x] 物理删除俱乐部必须强确认。
- [x] 物理删除俱乐部必须写强审计。
- [x] 有历史活动、历史流水、历史兑换关联时按设计限制删除或保留快照。

验收：

- [x] 普通停用不需要强确认。
- [x] 物理删除必须输入强确认文本。
- [x] 删除后历史记录仍可读。

证据：

- RED：新增 `ClubPointClubServiceImplTest` 后运行 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointClubServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 失败，原因是 `ClubPointClubService`、服务 BO、俱乐部操作错误码和俱乐部审计动作常量不存在，符合 M5.3 RED 预期。
- GREEN：新增 `ClubPointClubService` / `ClubPointClubServiceImpl` 和保存、停用、启用、物理删除、强确认 BO；`ClubPointClubMapper` 增加按名称查询、下游引用计数和物理删除 SQL；补充俱乐部修改、停用、启用审计动作和 M5.3 错误码。
- 实现边界：创建默认启用；修改和停用写强审计；停用不需要强确认；启用清理停用字段；物理删除必须匹配 `确认删除俱乐部：{俱乐部名称}` 并写 `PHYSICAL_DELETE` 强审计；存在成员、负责人、活动、报名、积分流水、非签到材料/明细、年度排名或激励等引用时拒绝物理删除。
- M5.3 单测验证：同一命令返回 `BUILD SUCCESS`；`ClubPointClubServiceImplTest` 运行 `9` 个测试，失败 `0`，错误 `0`。
- M5 当前组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointClubMapperTest,ClubPointClubEnumTest,ClubPointClubServiceImplTest,ClubScopeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `24` 个测试，失败 `0`，错误 `0`。
- 质量验证：`git diff --check` exit `0`，仅 CRLF 提示；源码范围 `tenant_id|TenantBaseDO` 无命中；源码和本次文档范围 AI/co-author 元数据模式无命中。

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
