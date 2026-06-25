# M1 数据库和 Seed Implementation Plan

**Status:** `[x]` M1 已放行。正式 schema、默认 seed、测试 DDL、clean SQL 已落地；MySQL 导入、关键唯一键、seed 覆盖性和幂等、测试 DDL/H2 加载、正式/测试字段与唯一键比对均有证据。

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

- [x] 创建 `ruoyi-vue-pro-github/sql/mysql/club-points-schema.sql`。
- [x] 创建 `ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql`。
- [x] 填写文件分区注释：规则、俱乐部、活动、账本、非签到、兑换、年度、支撑。
- [x] 不把 schema 混进 `ruoyi-vue-pro.sql`。
- [x] 不把 seed 混进 `club-points-schema.sql`。

验收：

- [x] 正式 schema 和 seed 独立存在。
- [x] 文件没有示例表、租户表、旧业务模块残留。

证据：

- RED：`Test-Path ruoyi-vue-pro-github/sql/mysql/club-points-schema.sql` 返回 `False`；`Test-Path ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql` 返回 `False`；测试 `create_tables.sql` 与 `clean.sql` 均为 1 字节占位。
- GREEN：创建 `club-points-schema.sql` 和 `club-points-seed.sql`，schema 文件只包含 `club_points_*` DDL，seed 文件只包含菜单、按钮、字典、规则、通知、定时任务分区注释。
- 验证：`Select-String club-points-schema.sql -Pattern 'tenant_id','bpm_','pay_','mall_'` 无命中；`member_` 仅出现在合法表名 `club_points_club_member`。

## 任务 M1.2 规则表 DDL

- [x] 建 `club_points_rule_version`。
- [x] 建 `club_points_rule_item`。
- [x] 建 `club_points_rule_publish_record`。
- [x] 加状态字段、发布时间、生效时间、停用时间。
- [x] 加规则项编码唯一键。
- [x] 加版本状态查询索引。

验收：

- [x] 能表达草稿、已发布、已停用状态机。
- [x] 同一版本内规则项编码不可重复。
- [x] 历史发布记录不可被业务覆盖。

证据：

- GREEN：`docker exec yudao-mysql mysql -uroot -p123456 -e "DROP DATABASE IF EXISTS club_points_m1_partial; CREATE DATABASE club_points_m1_partial DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"` 后导入 `club-points-schema.sql`，退出码为 0。
- 验证：`SHOW TABLES LIKE 'club_points_%'` 返回 `club_points_rule_version`、`club_points_rule_item`、`club_points_rule_publish_record` 等 6 张当前批次表。
- 唯一键验证：重复插入 `club_points_rule_version.version_no = 'VTEST'` 返回 `ERROR 1062 ... uk_club_points_rule_version_no`。
- 唯一键验证：重复插入 `club_points_rule_item(rule_version_id,item_code) = (1,'ACTIVITY_SMALL_BASE')` 返回 `ERROR 1062 ... uk_club_points_rule_item_version_code`。

## 任务 M1.3 俱乐部表 DDL

- [x] 建 `club_points_club`。
- [x] 建 `club_points_club_member`。
- [x] 建 `club_points_club_leader`。
- [x] 会员和负责人表保存用户、部门、俱乐部快照字段。
- [x] 物理删除俱乐部前必须有快照承接字段。
- [x] 加成员唯一约束和负责人唯一约束。

验收：

- [x] 同一用户不能重复加入同一俱乐部有效成员记录。
- [x] 负责人不能重复设置。
- [x] 删除俱乐部不会导致历史业务记录不可读。

证据：

- GREEN：`club_points_club`、`club_points_club_member`、`club_points_club_leader` 随 `club-points-schema.sql` 导入临时库成功。
- 唯一键验证：重复插入 `club_points_club.code = 'CLUB001'` 返回 `ERROR 1062 ... uk_club_points_club_code`。
- 唯一键验证：重复插入 `club_points_club.name = 'club C'` 返回 `ERROR 1062 ... uk_club_points_club_name`。
- 唯一键验证：重复插入 `club_points_club_member.active_unique_key = '1:100'` 返回 `ERROR 1062 ... uk_club_points_club_member_active`。
- 唯一键验证：重复插入 `club_points_club_leader.active_unique_key = '1:200'` 返回 `ERROR 1062 ... uk_club_points_club_leader_active`。

## 任务 M1.4 活动表 DDL

- [x] 建 `club_points_activity`。
- [x] 建 `club_points_activity_review_record`。
- [x] 建 `club_points_activity_point_config_version`。
- [x] 建 `club_points_activity_registration`。
- [x] 建 `club_points_attendance_record`。
- [x] 建 `club_points_attendance_correction`。
- [x] 建 `club_points_activity_settlement_run`。
- [x] 把特殊缺席放在报名或签到相关字段，不拆独立表。
- [x] 加报名唯一键、签到唯一键、结算运行唯一键。

验收：

- [x] 活动状态机字段完整。
- [x] 报名、签到、修正、结算都能幂等。
- [x] 活动积分配置版本能保存规则快照。

证据：

- GREEN：重新创建临时库 `club_points_m1_partial` 并导入当前 `club-points-schema.sql`，退出码为 0。
- 验证：`Select-String club-points-schema.sql -Pattern 'CREATE TABLE IF NOT EXISTS `club_points_' | Measure-Object` 返回 `13`，包含 M1.1-M1.4 当前批次表。
- 特殊缺席验证：schema 中只有 `club_points_activity_registration` 的 `no_absence_deduct`、`special_absence_flag`、`special_absence_reason`、`special_absence_time`、`special_absence_operator_id` 字段，没有拆独立表。
- 唯一键验证：重复插入 `club_points_activity_point_config_version(activity_id,version_no) = (1,1)` 返回 `ERROR 1062 ... uk_club_points_activity_config_version`。
- 唯一键验证：重复插入 `club_points_activity_registration.active_unique_key = '1:100'` 返回 `ERROR 1062 ... uk_club_points_activity_registration_active`。
- 唯一键验证：重复插入 `club_points_attendance_record(registration_id,target_type) = (1,1)` 返回 `ERROR 1062 ... uk_club_points_attendance_registration_target`。
- 唯一键验证：重复插入 `club_points_activity_settlement_run.run_key = 'ACTIVITY_SETTLEMENT:1:20260625'` 返回 `ERROR 1062 ... uk_club_points_activity_settlement_run_key`。

## 任务 M1.5 账本表 DDL

- [x] 建 `club_points_transaction`。
- [x] 建 `club_points_point_account`。
- [x] 建 `club_points_freeze`。
- [x] 建 `club_points_user_year_status`。
- [x] `club_points_transaction.idempotency_key` 加唯一键。
- [x] 流水保存来源类型、来源 ID、规则项、分值、年度、快照 JSON。
- [x] 账户缓存按数据库设计加 `user_id` 唯一键；年度唯一性由 `club_points_user_year_status(user_id, year)` 承接。
- [x] 冻结记录加业务来源唯一键。

验收：

- [x] 流水是唯一积分事实源。
- [x] 余额缓存可以重算。
- [x] 冻结不是流水。
- [x] 重复发分、重复扣分、重复清零被数据库拦截。

证据：

- GREEN：重新创建临时库 `club_points_m1_partial` 并导入当前 `club-points-schema.sql`，退出码为 0。
- 验证：当前 `club_points_*` 表数为 `17`。
- 冲突处理：本任务原文写“账户缓存加 `user_id, year` 唯一键”，但 `docs/club-points-database-design.md` 的 `club_points_point_account` 没有 `year` 字段，且索引约束明确为 `user_id` 唯一；年度状态按 `club_points_user_year_status(user_id, year)` 唯一处理。因此 schema 按数据库设计执行，不硬造 `year`。
- 唯一键验证：重复插入 `club_points_transaction.transaction_no = 'TXN001'` 返回 `ERROR 1062 ... uk_club_points_transaction_no`。
- 唯一键验证：重复插入 `club_points_transaction.idempotency_key = 'IDEMP001'` 返回 `ERROR 1062 ... uk_club_points_transaction_idempotency`。
- 唯一键验证：重复插入 `club_points_transaction.reverse_of_transaction_id = 900` 返回 `ERROR 1062 ... uk_club_points_transaction_reverse`。
- 唯一键验证：重复插入 `club_points_point_account.user_id = 100` 返回 `ERROR 1062 ... uk_club_points_point_account_user`。
- 唯一键验证：重复插入 `club_points_freeze.freeze_no = 'FRZ001'` 返回 `ERROR 1062 ... uk_club_points_freeze_no`。
- 唯一键验证：重复插入 `club_points_freeze.idempotency_key = 'FREEZE001'` 返回 `ERROR 1062 ... uk_club_points_freeze_idempotency`。
- 唯一键验证：重复插入 `club_points_freeze(source_type,source_id) = (1,700)` 返回 `ERROR 1062 ... uk_club_points_freeze_source`。
- 唯一键验证：重复插入 `club_points_user_year_status(user_id,year) = (100,2026)` 返回 `ERROR 1062 ... uk_club_points_user_year_status_user_year`。

## 任务 M1.6 非签到表 DDL

- [x] 建 `club_points_contribution_material`。
- [x] 建 `club_points_contribution_item`。
- [x] 建 `club_points_contribution_review_record`。
- [x] 保存提交人、审核人、俱乐部、规则项、附件锁定快照。
- [x] 加材料编号或请求号唯一键。
- [x] 加审核记录索引。

验收：

- [x] 一个材料可以包含多条明细。
- [x] 审核通过后能按明细生成流水。
- [x] 弄虚作假处理有来源记录。

证据：

- GREEN：重新创建临时库 `club_points_m1_partial` 并导入当前 `club-points-schema.sql`，退出码为 0。
- 验证：当前 `club_points_*` 表数为 `20`。
- 唯一键验证：重复插入 `club_points_contribution_material.request_no = 'REQ001'` 返回 `ERROR 1062 ... uk_club_points_contribution_material_request`。
- 唯一键验证：重复插入 `club_points_contribution_item.idempotency_key = 'CONTRIBUTION:1:1:100'` 返回 `ERROR 1062 ... uk_club_points_contribution_item_idempotency`。
- 唯一键验证：重复插入 `club_points_contribution_item.effective_unique_key = 'MONTHLY_DUTY:1:100:202606'` 返回 `ERROR 1062 ... uk_club_points_contribution_item_effective`。

## 任务 M1.7 兑换表 DDL

- [x] 建 `club_points_redemption_batch`。
- [x] 建 `club_points_redemption_gift`。
- [x] 建 `club_points_redemption_eligibility_snapshot`。
- [x] 建 `club_points_redemption_application`。
- [x] 建 `club_points_stock_lock`。
- [x] 建 `club_points_redemption_review_record`。
- [x] 申请表加 `idempotency_key` 唯一键。
- [x] 库存锁表加来源唯一键。
- [x] 礼品表保存总库存、已锁定、已兑换字段。

验收：

- [x] 同一兑换申请重复提交被唯一键拦截。
- [x] 库存条件更新有字段支撑。
- [x] 资格快照可追溯。

证据：

- GREEN：重新创建临时库 `club_points_m1_partial` 并导入当前 `club-points-schema.sql`，退出码为 0。
- 验证：当前 `club_points_*` 表数为 `26`。
- 字段验证：`club_points_redemption_gift` 包含 `stock_total`、`stock_locked`、`stock_used`，支撑库存条件更新。
- 唯一键验证：重复插入 `club_points_redemption_eligibility_snapshot(batch_id,user_id) = (1,100)` 返回 `ERROR 1062 ... uk_club_points_redemption_eligibility_user`。
- 唯一键验证：重复插入 `club_points_redemption_application.application_no = 'APP001'` 返回 `ERROR 1062 ... uk_club_points_redemption_application_no`。
- 唯一键验证：重复插入 `club_points_redemption_application.idempotency_key = 'REDEMPTION_APPLY:1:1:100:REQ001'` 返回 `ERROR 1062 ... uk_club_points_redemption_application_idempotency`。
- 唯一键验证：重复插入 `club_points_stock_lock.application_id = 500` 返回 `ERROR 1062 ... uk_club_points_stock_lock_application`。
- 唯一键验证：重复插入 `club_points_stock_lock.idempotency_key = 'STOCK_LOCK:500'` 返回 `ERROR 1062 ... uk_club_points_stock_lock_idempotency`。

## 任务 M1.8 年度和支撑表 DDL

- [x] 建 `club_points_dispute`。
- [x] 建 `club_points_annual_clearing_record`。
- [x] 建 `club_points_annual_ranking_record`。
- [x] 建 `club_points_incentive_record`。
- [x] 建 `club_points_budget_record`。
- [x] 建 `club_points_attachment_ref`。
- [x] 建 `club_points_audit_log`。
- [x] 建 `club_points_job_run`。
- [x] 不新增导出主表，导出写 `club_points_audit_log`。

验收：

- [x] 年度清零按 `year,user_id` 幂等。
- [x] 审计表能记录操作者、动作、对象、前后快照、结果。
- [x] 任务运行表能支持重试和幂等。

证据：

- GREEN：重新创建临时库 `club_points_m1_partial` 并导入当前 `club-points-schema.sql`，退出码为 0。
- 验证：当前 `club_points_*` 表数为 `34`，覆盖数据库设计表清单的 34 张业务表。
- 验证：`Select-String club-points-schema.sql -Pattern 'export','导出'` 无命中，没有新增导出主表。
- 唯一键验证：重复插入 `club_points_annual_clearing_record(year,user_id) = (2026,100)` 返回 `ERROR 1062 ... uk_club_points_annual_clearing_user_year`。
- 唯一键验证：重复插入 `club_points_annual_clearing_record.idempotency_key = 'ANNUAL_CLEARING:2026:100'` 返回 `ERROR 1062 ... uk_club_points_annual_clearing_idempotency`。
- 唯一键验证：重复插入 `club_points_annual_ranking_record(year,club_code_snapshot) = (2026,'CLUB001')` 返回 `ERROR 1062 ... uk_club_points_annual_ranking_year_club`。
- 唯一键验证：重复插入 `club_points_job_run.idempotency_key = 'JOB:ANNUAL_CLEARING:2026'` 返回 `ERROR 1062 ... uk_club_points_job_run_idempotency`。

## 任务 M1.9 默认 Seed

- [x] 写菜单 seed。
- [x] 写按钮权限 seed。
- [x] 写字典类型和字典值 seed。
- [x] 写默认规则版本 seed。
- [x] 写默认规则项 seed。
- [x] 写通知模板 seed。
- [x] 写定时任务 seed。
- [x] 不写示例云密钥。
- [x] 不恢复租户 seed。

验收：

- [x] 默认规则来自制度配置，不写死在 Java。
- [x] 三类角色权限建议可初始化。
- [x] 菜单权限码和 API 文档一致。

证据：

- GREEN：创建临时库 `club_points_m1_seed`，依次导入 `ruoyi-vue-pro.sql`、`club-points-schema.sql`、`club-points-seed.sql`，退出码为 0。
- 覆盖性查询：`system_menu` 本模块菜单/按钮 `67` 条，`system_role` 本模块角色 `3` 条，`system_role_menu` 本模块授权 `105` 条，`system_dict_type` 本模块字典类型 `17` 条，`system_dict_data` 本模块字典值 `66` 条。
- 覆盖性查询：默认规则版本 `V2026.01` 为 `1` 条，默认规则项 `22` 条，站内信模板 `4` 条，定时任务 `3` 条。
- 权限一致性：从 `club-points-api-design.md` 和 `club-points-functions-and-permissions.md` 提取 API 权限码 `53` 个；从 seed 的按钮权限提取 `53` 个；`Compare-Object` 返回空，输出 `permission_sets_match`。
- 幂等验证：重复导入 `club-points-seed.sql` 后菜单 `67`、角色 `3`、角色授权 `105`、规则项 `22`、通知模板 `4`、定时任务 `3`，计数不膨胀。
- 敏感 seed 验证：精确搜索 `tenant_id|system_tenant|access_key|secret_key|api_secret|oss_access|mail_account|sms_channel|sms_template` 无命中。

## 任务 M1.10 测试 DDL

- [x] 把核心表同步到 `yudao-module-clubpoints/src/test/resources/sql/create_tables.sql`。
- [x] 把清表顺序写入 `yudao-module-clubpoints/src/test/resources/sql/clean.sql`。
- [x] 清表顺序先子表后主表。
- [x] 测试 DDL 字段名、类型、唯一键与正式 schema 保持一致。

验收：

- [x] 单测能加载测试 DDL。
- [x] 不存在正式 schema 有字段、测试 DDL 缺字段。
- [x] 不存在测试 DDL 自造字段。

证据：

- 生成策略：从正式 `club-points-schema.sql` 机械转换测试 DDL，保留表、字段、主键、唯一键和普通索引；MySQL `json` 按数据库设计降级为 H2 兼容 `longtext`；移除字符集、COLLATE、AUTO_INCREMENT、BTREE 和列注释。
- 验证：`create_tables.sql` 中 `CREATE TABLE IF NOT EXISTS "club_points_` 计数为 `34`。
- 验证：`clean.sql` 中 `DELETE FROM "club_points_` 计数为 `34`。
- 字段比对：正式 schema 与测试 DDL 的 34 张表字段集合比对结果 `column_diff_count=0`。
- 唯一键比对：正式 schema 与测试 DDL 的唯一键集合比对结果 `mysql_unique=30 h2_unique=30 unique_diff_count=0`。
- H2 加载验证：`java -cp h2-2.1.214.jar org.h2.tools.RunScript -url "jdbc:h2:file:C:/jobs/pointsmall/.tmp/clubpoints_m1_h2;MODE=MYSQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=value" -script create_tables.sql` 返回 `h2_file_create_EXIT=0`。
- H2 清表验证：同一 file H2 库执行 `clean.sql` 返回 `h2_file_clean_EXIT=0`。

## M1 放行标准

- [x] 正式 schema 存在。
- [x] 正式 seed 存在。
- [x] 测试 DDL 存在。
- [x] 测试清表 SQL 存在。
- [x] 高风险唯一键全部落库。
- [x] 默认规则 seed 全部落库。
- [x] 数据库设计文档与 SQL 无冲突。

放行证据：

- 正式 schema：`club-points-schema.sql` 导入临时 MySQL 库成功，`club_points_*` 表数为 `34`。
- 正式 seed：导入芋道基线 SQL、clubpoints schema、clubpoints seed 到临时库成功；重复导入 seed 后核心计数不膨胀。
- 高风险唯一键：规则、俱乐部、活动、账本、非签到、兑换、年度、任务运行关键幂等字段均做过重复插入验证，MySQL 返回 `ERROR 1062`。
- 默认规则 seed：`V2026.01` 默认规则版本 `1` 条，默认规则项 `22` 条。
- 权限一致性：API/权限文档提取 `53` 个 `clubpoints:*` 权限码，seed 按钮权限 `53` 个，集合一致。
- 测试 DDL：H2 file 库建表和清表均返回退出码 0；正式 schema 与测试 DDL 字段差异 0、唯一键差异 0。

## M1 不通过时禁止

- 禁止进入 M2 写权限 Controller。
- 禁止写积分账本服务。
- 禁止写兑换库存逻辑。
