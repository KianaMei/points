# 数据库规则

## 基线

- 复用芋道 `system_*` 和 `infra_*` 表。
- 不重复造用户、部门、角色、菜单、文件、任务技术日志表。
- 业务表统一 `club_points_*`。

## 主键

- MySQL 下 `club_points_*` 主键固定为 `bigint NOT NULL AUTO_INCREMENT`。
- Java DO 使用 `@TableId private Long id;`。
- 不自行改成雪花 ID 方案。

## 通用字段

- 业务 DO 继承 `BaseDO`。
- 使用芋道通用字段：`creator`、`create_time`、`updater`、`update_time`、`deleted`。
- 不加 `tenant_id`。

## 事实源表

- 积分流水：`club_points_transaction`
- 账户缓存：`club_points_point_account`
- 积分冻结：`club_points_freeze`
- 活动报名：`club_points_activity_registration`
- 活动积分配置版本：`club_points_activity_point_config_version`
- 兑换资格快照：`club_points_redemption_eligibility_snapshot`
- 库存锁：`club_points_stock_lock`
- 附件绑定：`club_points_attachment_ref`
- 强审计：`club_points_audit_log`
- 业务任务运行：`club_points_job_run`

## 禁止旧方案漂移

- 不要创建数据库设计表清单之外的旧方案表。
- 不要把特殊缺席拆成独立表；它属于活动报名记录字段。
- 不要把导出留痕拆成独立业务表；导出写强审计。
- 不要把账本幂等拆成独立表；流水幂等键和业务表唯一键兜底。

## 幂等和并发

- 重复发分、扣分、清零、兑换扣减靠 `club_points_transaction.idempotency_key` 唯一。
- 兑换申请靠 `club_points_redemption_application.idempotency_key` 唯一。
- 库存靠 `club_points_redemption_gift` 条件更新兜底。
- 年度清零靠 `year,user_id` 和流水幂等键兜底。

详细字段查 `docs/club-points-database-design.md`。
