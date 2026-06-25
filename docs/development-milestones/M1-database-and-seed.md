# M1 数据库和 Seed Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将数据库设计落成正式 MySQL schema、默认 seed、测试 DDL 和清表 SQL，并保证唯一键、状态、快照字段一致。

**Architecture:** 正式 SQL 独立放在 `sql/mysql/club-points-*.sql`，测试 SQL 放在 `yudao-module-clubpoints/src/test/resources/sql/`。数据库是幂等、并发和事实源的最终边界，不能由 Redis 或前端逻辑替代。

**Tech Stack:** MySQL 8、RuoYi MyBatis、BaseDO 通用字段、JUnit/DBUnit 测试 SQL、芋道菜单/权限/字典 seed。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 数据库单一输入是 `docs/club-points-database-design.md`。
- 业务表统一 `club_points_*`。
- 主键固定 `bigint NOT NULL AUTO_INCREMENT`。
- 不添加 `tenant_id`。
- SQL 和测试 DDL 必须同步。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/sql/mysql/club-points-schema.sql`
- Create: `ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/resources/sql/create_tables.sql`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/resources/sql/clean.sql`
- Reference: `docs/club-points-database-design.md`
- Reference: `docs/club-points-api-design.md`
- Reference: `docs/club-points-functions-and-permissions.md`

**Interfaces:**

- Consumes: 数据库设计里的表、字段、索引、唯一键、状态枚举、快照字段。
- Produces: `club_points_*` 正式表结构、默认菜单/权限/字典/规则 seed、测试 DDL 和清表 SQL。

**Verification:**

- Run: 导入 `club-points-schema.sql` 到本地 MySQL 测试库。
- Expected: 所有 `club_points_*` 表创建成功。
- Run: 对关键幂等字段执行重复插入验证。
- Expected: 数据库唯一键拒绝重复记录。
- Run: 比对正式 schema 和 `create_tables.sql` 字段清单。
- Expected: 核心测试表字段、类型、唯一键一致。

## 目标

把数据库设计落成正式 MySQL schema、默认 seed、测试 DDL，并保证三者字段、状态、唯一键一致。

## 前置条件

- M0 已放行。
- `docs/club-points-database-design.md` 是数据库单一输入。
- `club_points_*` 表统一使用 `bigint NOT NULL AUTO_INCREMENT` 主键。
- 业务表不加 `tenant_id`。

## 任务 M1.1 建 SQL 文件骨架

- [ ] 创建 `ruoyi-vue-pro-github/sql/mysql/club-points-schema.sql`。
- [ ] 创建 `ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql`。
- [ ] 填写文件分区注释：规则、俱乐部、活动、账本、非签到、兑换、年度、支撑。
- [ ] 不把 schema 混进 `ruoyi-vue-pro.sql`。
- [ ] 不把 seed 混进 `club-points-schema.sql`。

验收：

- [ ] 正式 schema 和 seed 独立存在。
- [ ] 文件没有示例表、租户表、旧业务模块残留。

## 任务 M1.2 规则表 DDL

- [ ] 建 `club_points_rule_version`。
- [ ] 建 `club_points_rule_item`。
- [ ] 建 `club_points_rule_publish_record`。
- [ ] 加状态字段、发布时间、生效时间、停用时间。
- [ ] 加规则项编码唯一键。
- [ ] 加版本状态查询索引。

验收：

- [ ] 能表达草稿、已发布、已停用状态机。
- [ ] 同一版本内规则项编码不可重复。
- [ ] 历史发布记录不可被业务覆盖。

## 任务 M1.3 俱乐部表 DDL

- [ ] 建 `club_points_club`。
- [ ] 建 `club_points_club_member`。
- [ ] 建 `club_points_club_leader`。
- [ ] 会员和负责人表保存用户、部门、俱乐部快照字段。
- [ ] 物理删除俱乐部前必须有快照承接字段。
- [ ] 加成员唯一约束和负责人唯一约束。

验收：

- [ ] 同一用户不能重复加入同一俱乐部有效成员记录。
- [ ] 负责人不能重复设置。
- [ ] 删除俱乐部不会导致历史业务记录不可读。

## 任务 M1.4 活动表 DDL

- [ ] 建 `club_points_activity`。
- [ ] 建 `club_points_activity_review_record`。
- [ ] 建 `club_points_activity_point_config_version`。
- [ ] 建 `club_points_activity_registration`。
- [ ] 建 `club_points_attendance_record`。
- [ ] 建 `club_points_attendance_correction`。
- [ ] 建 `club_points_activity_settlement_run`。
- [ ] 把特殊缺席放在报名或签到相关字段，不拆独立表。
- [ ] 加报名唯一键、签到唯一键、结算运行唯一键。

验收：

- [ ] 活动状态机字段完整。
- [ ] 报名、签到、修正、结算都能幂等。
- [ ] 活动积分配置版本能保存规则快照。

## 任务 M1.5 账本表 DDL

- [ ] 建 `club_points_transaction`。
- [ ] 建 `club_points_point_account`。
- [ ] 建 `club_points_freeze`。
- [ ] 建 `club_points_user_year_status`。
- [ ] `club_points_transaction.idempotency_key` 加唯一键。
- [ ] 流水保存来源类型、来源 ID、规则项、分值、年度、快照 JSON。
- [ ] 账户缓存加 `user_id, year` 唯一键。
- [ ] 冻结记录加业务来源唯一键。

验收：

- [ ] 流水是唯一积分事实源。
- [ ] 余额缓存可以重算。
- [ ] 冻结不是流水。
- [ ] 重复发分、重复扣分、重复清零被数据库拦截。

## 任务 M1.6 非签到表 DDL

- [ ] 建 `club_points_contribution_material`。
- [ ] 建 `club_points_contribution_item`。
- [ ] 建 `club_points_contribution_review_record`。
- [ ] 保存提交人、审核人、俱乐部、规则项、附件锁定快照。
- [ ] 加材料编号或请求号唯一键。
- [ ] 加审核记录索引。

验收：

- [ ] 一个材料可以包含多条明细。
- [ ] 审核通过后能按明细生成流水。
- [ ] 弄虚作假处理有来源记录。

## 任务 M1.7 兑换表 DDL

- [ ] 建 `club_points_redemption_batch`。
- [ ] 建 `club_points_redemption_gift`。
- [ ] 建 `club_points_redemption_eligibility_snapshot`。
- [ ] 建 `club_points_redemption_application`。
- [ ] 建 `club_points_stock_lock`。
- [ ] 建 `club_points_redemption_review_record`。
- [ ] 申请表加 `idempotency_key` 唯一键。
- [ ] 库存锁表加来源唯一键。
- [ ] 礼品表保存总库存、已锁定、已兑换字段。

验收：

- [ ] 同一兑换申请重复提交被唯一键拦截。
- [ ] 库存条件更新有字段支撑。
- [ ] 资格快照可追溯。

## 任务 M1.8 年度和支撑表 DDL

- [ ] 建 `club_points_dispute`。
- [ ] 建 `club_points_annual_clearing_record`。
- [ ] 建 `club_points_annual_ranking_record`。
- [ ] 建 `club_points_incentive_record`。
- [ ] 建 `club_points_budget_record`。
- [ ] 建 `club_points_attachment_ref`。
- [ ] 建 `club_points_audit_log`。
- [ ] 建 `club_points_job_run`。
- [ ] 不新增导出主表，导出写 `club_points_audit_log`。

验收：

- [ ] 年度清零按 `year,user_id` 幂等。
- [ ] 审计表能记录操作者、动作、对象、前后快照、结果。
- [ ] 任务运行表能支持重试和幂等。

## 任务 M1.9 默认 Seed

- [ ] 写菜单 seed。
- [ ] 写按钮权限 seed。
- [ ] 写字典类型和字典值 seed。
- [ ] 写默认规则版本 seed。
- [ ] 写默认规则项 seed。
- [ ] 写通知模板 seed。
- [ ] 写定时任务 seed。
- [ ] 不写示例云密钥。
- [ ] 不恢复租户 seed。

验收：

- [ ] 默认规则来自制度配置，不写死在 Java。
- [ ] 三类角色权限建议可初始化。
- [ ] 菜单权限码和 API 文档一致。

## 任务 M1.10 测试 DDL

- [ ] 把核心表同步到 `yudao-module-clubpoints/src/test/resources/sql/create_tables.sql`。
- [ ] 把清表顺序写入 `yudao-module-clubpoints/src/test/resources/sql/clean.sql`。
- [ ] 清表顺序先子表后主表。
- [ ] 测试 DDL 字段名、类型、唯一键与正式 schema 保持一致。

验收：

- [ ] 单测能加载测试 DDL。
- [ ] 不存在正式 schema 有字段、测试 DDL 缺字段。
- [ ] 不存在测试 DDL 自造字段。

## M1 放行标准

- [ ] 正式 schema 存在。
- [ ] 正式 seed 存在。
- [ ] 测试 DDL 存在。
- [ ] 测试清表 SQL 存在。
- [ ] 高风险唯一键全部落库。
- [ ] 默认规则 seed 全部落库。
- [ ] 数据库设计文档与 SQL 无冲突。

## M1 不通过时禁止

- [ ] 禁止进入 M2 写权限 Controller。
- [ ] 禁止写积分账本服务。
- [ ] 禁止写兑换库存逻辑。
