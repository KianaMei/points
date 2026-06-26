-- 俱乐部员工积分系统 - 默认 Seed
-- Source of truth: docs/club-points-database-design.md,
-- docs/club-points-api-design.md,
-- docs/club-points-functions-and-permissions.md

SET NAMES utf8mb4;

-- ----------------------------
-- 角色
-- ----------------------------

INSERT INTO `system_role` (`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1300000000, '俱乐部积分员工', 'club_points_employee', 1300, 1, '', 0, 2, '俱乐部积分员工端默认角色', '1', NOW(), '1', NOW(), b'0'),
(1300000001, '俱乐部积分负责人', 'club_points_leader', 1301, 1, '', 0, 2, '俱乐部积分负责人默认角色', '1', NOW(), '1', NOW(), b'0'),
(1300000002, '俱乐部积分管理员', 'club_points_admin', 1302, 1, '', 0, 2, '俱乐部积分管理员默认角色', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `code` = VALUES(`code`),
  `sort` = VALUES(`sort`),
  `data_scope` = VALUES(`data_scope`),
  `data_scope_dept_ids` = VALUES(`data_scope_dept_ids`),
  `status` = VALUES(`status`),
  `type` = VALUES(`type`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = VALUES(`update_time`),
  `deleted` = VALUES(`deleted`);

-- ----------------------------
-- 菜单
-- ----------------------------

DELETE FROM `system_role_menu`
WHERE `role_id` IN (1300000000, 1300000001, 1300000002)
  AND `menu_id` BETWEEN 1300010000 AND 1300012999;

DELETE FROM `system_menu`
WHERE `id` BETWEEN 1300010000 AND 1300012999;

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1300010000, '俱乐部积分', '', 1, 1300, 0, '/clubpoints', 'ep:trophy', NULL, NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010100, '我的积分', '', 1, 10, 1300010000, 'app', 'ep:user', NULL, NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010110, '员工工作台', '', 2, 10, 1300010100, 'dashboard', 'ep:odometer', 'clubpoints/app/dashboard/index', 'ClubPointsAppDashboard', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010120, '我的积分', '', 2, 20, 1300010100, 'ledger', 'ep:tickets', 'clubpoints/app/ledger/index', 'ClubPointsAppLedger', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010130, '我的俱乐部', 'clubpoints:club-member:query', 2, 30, 1300010100, 'club', 'ep:office-building', 'clubpoints/app/club/index', 'ClubPointsAppClub', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010140, '活动报名', 'clubpoints:registration:create', 2, 40, 1300010100, 'activity', 'ep:calendar', 'clubpoints/app/activity/index', 'ClubPointsAppActivity', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010150, '积分兑换', 'clubpoints:redemption:apply', 2, 50, 1300010100, 'redemption', 'ep:present', 'clubpoints/app/redemption/index', 'ClubPointsAppRedemption', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010160, '我的异议', '', 2, 60, 1300010100, 'dispute', 'ep:chat-line-round', 'clubpoints/app/dispute/index', 'ClubPointsAppDispute', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010170, '我的通知', '', 2, 70, 1300010100, 'notify', 'ep:bell', 'clubpoints/app/notify/index', 'ClubPointsAppNotify', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010200, '负责人工作台', 'clubpoints:leader', 1, 20, 1300010000, 'leader', 'ep:user-filled', NULL, NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010210, '负责人首页', 'clubpoints:leader', 2, 10, 1300010200, 'dashboard', 'ep:odometer', 'clubpoints/leader/dashboard/index', 'ClubPointsLeaderDashboard', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010220, '负责俱乐部', 'clubpoints:club-leader', 2, 20, 1300010200, 'club', 'ep:office-building', 'clubpoints/leader/club/index', 'ClubPointsLeaderClub', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010230, '负责人活动', 'clubpoints:activity:query', 2, 30, 1300010200, 'activity', 'ep:calendar', 'clubpoints/leader/activity/index', 'ClubPointsLeaderActivity', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010240, '签到修正', 'clubpoints:attendance:query', 2, 40, 1300010200, 'attendance', 'ep:checked', 'clubpoints/leader/attendance/index', 'ClubPointsLeaderAttendance', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010250, '非签到材料', 'clubpoints:contribution:query', 2, 50, 1300010200, 'contribution', 'ep:document-checked', 'clubpoints/leader/contribution/index', 'ClubPointsLeaderContribution', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010300, '积分管理', '', 1, 30, 1300010000, 'admin', 'ep:setting', NULL, NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010310, '管理员工作台', 'clubpoints:dashboard:query', 2, 10, 1300010300, 'dashboard', 'ep:odometer', 'clubpoints/admin/dashboard/index', 'ClubPointsAdminDashboard', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010320, '俱乐部和负责人', 'clubpoints:club:query', 2, 20, 1300010300, 'club', 'ep:office-building', 'clubpoints/admin/club/index', 'ClubPointsAdminClub', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010330, '活动管理', 'clubpoints:activity:query', 2, 30, 1300010300, 'activity', 'ep:calendar', 'clubpoints/admin/activity/index', 'ClubPointsAdminActivity', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010340, '活动结算', 'clubpoints:settlement:query', 2, 40, 1300010300, 'settlement', 'ep:finished', 'clubpoints/admin/settlement/index', 'ClubPointsAdminSettlement', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010350, '积分账户', 'clubpoints:ledger:query', 2, 50, 1300010300, 'ledger/account', 'ep:wallet', 'clubpoints/admin/ledger/account/index', 'ClubPointsAdminLedgerAccount', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010360, '积分流水', 'clubpoints:ledger:query', 2, 60, 1300010300, 'ledger/transaction', 'ep:tickets', 'clubpoints/admin/ledger/transaction/index', 'ClubPointsAdminLedgerTransaction', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010370, '规则版本', 'clubpoints:rule:manage', 2, 70, 1300010300, 'rule', 'ep:setting', 'clubpoints/admin/rule/index', 'ClubPointsAdminRule', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010380, '材料审核', 'clubpoints:contribution:review', 2, 80, 1300010300, 'contribution-review', 'ep:document-checked', 'clubpoints/admin/contribution-review/index', 'ClubPointsAdminContributionReview', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010390, '管理员代录', 'clubpoints:contribution:direct-create', 2, 90, 1300010300, 'contribution-direct', 'ep:edit-pen', 'clubpoints/admin/contribution-direct/index', 'ClubPointsAdminContributionDirect', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010400, '兑换批次', 'clubpoints:redemption-batch:manage', 2, 100, 1300010300, 'redemption-batch', 'ep:collection', 'clubpoints/admin/redemption-batch/index', 'ClubPointsAdminRedemptionBatch', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010410, '礼品维护', 'clubpoints:redemption-gift:manage', 2, 110, 1300010300, 'redemption-gift', 'ep:present', 'clubpoints/admin/redemption-gift/index', 'ClubPointsAdminRedemptionGift', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010420, '兑换审核', 'clubpoints:redemption:review', 2, 120, 1300010300, 'redemption-application', 'ep:shopping-cart', 'clubpoints/admin/redemption-application/index', 'ClubPointsAdminRedemptionApplication', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010430, '异议处理', 'clubpoints:dispute:handle', 2, 130, 1300010300, 'dispute', 'ep:chat-line-round', 'clubpoints/admin/dispute/index', 'ClubPointsAdminDispute', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010440, '年度清零', 'clubpoints:annual:clear', 2, 140, 1300010300, 'annual-clearing', 'ep:refresh-left', 'clubpoints/admin/annual-clearing/index', 'ClubPointsAdminAnnualClearing', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010450, '年度排名和激励', 'clubpoints:annual:query', 2, 150, 1300010300, 'annual-ranking', 'ep:medal', 'clubpoints/admin/annual-ranking/index', 'ClubPointsAdminAnnualRanking', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010460, '预算记录', 'clubpoints:budget:manage', 2, 160, 1300010300, 'budget', 'ep:money', 'clubpoints/admin/budget/index', 'ClubPointsAdminBudget', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010470, '报表中心', 'clubpoints:report:query', 2, 170, 1300010300, 'report', 'ep:download', 'clubpoints/admin/report/index', 'ClubPointsAdminReport', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010480, '审计日志', 'clubpoints:audit:query', 2, 180, 1300010300, 'audit', 'ep:document-copy', 'clubpoints/admin/audit/index', 'ClubPointsAdminAudit', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300010490, '任务运行', 'clubpoints:job:query', 2, 190, 1300010300, 'job-run', 'ep:operation', 'clubpoints/admin/job-run/index', 'ClubPointsAdminJobRun', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `permission` = VALUES(`permission`),
  `type` = VALUES(`type`),
  `sort` = VALUES(`sort`),
  `parent_id` = VALUES(`parent_id`),
  `path` = VALUES(`path`),
  `icon` = VALUES(`icon`),
  `component` = VALUES(`component`),
  `component_name` = VALUES(`component_name`),
  `status` = VALUES(`status`),
  `visible` = VALUES(`visible`),
  `keep_alive` = VALUES(`keep_alive`),
  `always_show` = VALUES(`always_show`),
  `updater` = VALUES(`updater`),
  `update_time` = VALUES(`update_time`),
  `deleted` = VALUES(`deleted`);

-- ----------------------------
-- 按钮权限
-- ----------------------------

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1300012001, '取消活动', 'clubpoints:activity:cancel', 3, 1, 1300010230, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012002, '创建活动', 'clubpoints:activity:create', 3, 2, 1300010230, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012003, '删除活动', 'clubpoints:activity:delete', 3, 3, 1300010230, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012004, '发布活动', 'clubpoints:activity:publish', 3, 4, 1300010330, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012005, '查询活动', 'clubpoints:activity:query', 3, 5, 1300010230, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012006, '审核活动', 'clubpoints:activity:review', 3, 6, 1300010330, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012007, '提交活动', 'clubpoints:activity:submit', 3, 7, 1300010230, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012008, '修改活动', 'clubpoints:activity:update', 3, 8, 1300010230, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012009, '年度清零', 'clubpoints:annual:clear', 3, 9, 1300010440, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012010, '年度管理', 'clubpoints:annual:manage', 3, 10, 1300010450, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012011, '年度查询', 'clubpoints:annual:query', 3, 11, 1300010450, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012012, '签到', 'clubpoints:attendance:check-in', 3, 12, 1300010140, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012013, '签退', 'clubpoints:attendance:check-out', 3, 13, 1300010140, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012014, '修正签到', 'clubpoints:attendance:correct', 3, 14, 1300010240, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012015, '查询签到', 'clubpoints:attendance:query', 3, 15, 1300010240, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012016, '查询审计', 'clubpoints:audit:query', 3, 16, 1300010480, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012017, '预算管理', 'clubpoints:budget:manage', 3, 17, 1300010460, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012018, '负责人入口', 'clubpoints:club-leader', 3, 18, 1300010220, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012019, '维护负责人', 'clubpoints:club-leader:update', 3, 19, 1300010320, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012020, '退出俱乐部', 'clubpoints:club-member:exit', 3, 20, 1300010130, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012021, '加入俱乐部', 'clubpoints:club-member:join', 3, 21, 1300010130, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012022, '查询成员', 'clubpoints:club-member:query', 3, 22, 1300010130, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012023, '移除成员', 'clubpoints:club-member:remove', 3, 23, 1300010320, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012055, '添加成员', 'clubpoints:club-member:add', 3, 24, 1300010320, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012024, '创建俱乐部', 'clubpoints:club:create', 3, 24, 1300010320, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012025, '删除俱乐部', 'clubpoints:club:delete', 3, 25, 1300010320, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012026, '停用俱乐部', 'clubpoints:club:disable', 3, 26, 1300010320, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012027, '查询俱乐部', 'clubpoints:club:query', 3, 27, 1300010320, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012028, '修改俱乐部', 'clubpoints:club:update', 3, 28, 1300010220, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012029, '删除材料', 'clubpoints:contribution:delete', 3, 29, 1300010250, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012030, '管理员代录', 'clubpoints:contribution:direct-create', 3, 30, 1300010390, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012031, '查询材料', 'clubpoints:contribution:query', 3, 31, 1300010250, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012032, '审核材料', 'clubpoints:contribution:review', 3, 32, 1300010380, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012033, '提交材料', 'clubpoints:contribution:submit', 3, 33, 1300010250, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012034, '撤回材料', 'clubpoints:contribution:withdraw', 3, 34, 1300010250, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012035, '处理异议', 'clubpoints:dispute:handle', 3, 35, 1300010430, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012036, '处理任务', 'clubpoints:job:handle', 3, 36, 1300010490, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012037, '查询任务', 'clubpoints:job:query', 3, 37, 1300010490, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012038, '负责人工作台', 'clubpoints:leader', 3, 38, 1300010210, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012039, '调整积分', 'clubpoints:ledger:adjust', 3, 39, 1300010360, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012040, '查询账本', 'clubpoints:ledger:query', 3, 40, 1300010350, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012041, '撤销流水', 'clubpoints:ledger:reverse', 3, 41, 1300010360, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012042, '兑换批次管理', 'clubpoints:redemption-batch:manage', 3, 42, 1300010400, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012043, '兑换礼品管理', 'clubpoints:redemption-gift:manage', 3, 43, 1300010410, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012044, '提交兑换', 'clubpoints:redemption:apply', 3, 44, 1300010150, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012045, '取消兑换', 'clubpoints:redemption:cancel-own', 3, 45, 1300010150, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012046, '审核兑换', 'clubpoints:redemption:review', 3, 46, 1300010420, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012047, '取消报名', 'clubpoints:registration:cancel', 3, 47, 1300010140, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012048, '活动报名', 'clubpoints:registration:create', 3, 48, 1300010140, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012049, '查询报名', 'clubpoints:registration:query', 3, 49, 1300010240, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012050, '导出报表', 'clubpoints:report:export', 3, 50, 1300010470, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012051, '规则管理', 'clubpoints:rule:manage', 3, 51, 1300010370, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012052, '查询结算', 'clubpoints:settlement:query', 3, 52, 1300010340, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012053, '运行结算', 'clubpoints:settlement:run', 3, 53, 1300010340, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012054, '特殊缺席', 'clubpoints:registration:special-absence', 3, 54, 1300010240, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012056, '违规扣分', 'clubpoints:contribution:violation-deduct', 3, 56, 1300010390, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012057, '弄虚作假处理', 'clubpoints:contribution:fraud-handle', 3, 57, 1300010390, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012058, '查询报表', 'clubpoints:report:query', 3, 58, 1300010470, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'),
(1300012059, '查询工作台', 'clubpoints:dashboard:query', 3, 59, 1300010310, '', '#', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `permission` = VALUES(`permission`),
  `type` = VALUES(`type`),
  `sort` = VALUES(`sort`),
  `parent_id` = VALUES(`parent_id`),
  `path` = VALUES(`path`),
  `icon` = VALUES(`icon`),
  `component` = VALUES(`component`),
  `component_name` = VALUES(`component_name`),
  `status` = VALUES(`status`),
  `visible` = VALUES(`visible`),
  `keep_alive` = VALUES(`keep_alive`),
  `always_show` = VALUES(`always_show`),
  `updater` = VALUES(`updater`),
  `update_time` = VALUES(`update_time`),
  `deleted` = VALUES(`deleted`);

-- ----------------------------
-- 角色授权
-- ----------------------------

DELETE FROM `system_role_menu`
WHERE `role_id` IN (1300000000, 1300000001, 1300000002)
  AND `menu_id` BETWEEN 1300010000 AND 1300012999;

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1300000002, `id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu`
WHERE `id` BETWEEN 1300010000 AND 1300012999;

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1300000001, `id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu`
WHERE `id` IN (
  1300010000, 1300010100, 1300010110, 1300010120, 1300010130,
  1300010140, 1300010150, 1300010160, 1300010170,
  1300010200, 1300010210, 1300010220, 1300010230, 1300010240,
  1300010250,
  1300012001, 1300012002, 1300012003, 1300012005, 1300012007,
  1300012008, 1300012012, 1300012013, 1300012014, 1300012015,
  1300012018, 1300012020, 1300012021, 1300012022, 1300012027,
  1300012028, 1300012029, 1300012031, 1300012033, 1300012034,
  1300012038, 1300012044, 1300012045, 1300012047, 1300012048,
  1300012049, 1300012054
);

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1300000000, `id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu`
WHERE `id` IN (
  1300010000, 1300010100, 1300010110, 1300010120, 1300010130,
  1300010140, 1300010150, 1300010160, 1300010170,
  1300012012, 1300012013, 1300012020, 1300012021, 1300012022,
  1300012044, 1300012045, 1300012047, 1300012048
);

-- ----------------------------
-- 字典类型和值
-- ----------------------------

INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES
(1300030001, '俱乐部状态', 'club_points_club_status', 0, '1=启用；2=停用；3=已删除快照', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030002, '俱乐部成员状态', 'club_points_member_status', 0, '1=有效；2=自主退出；3=管理员移除', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030003, '活动状态', 'club_points_activity_status', 0, '1=草稿；2=待审核；3=已驳回；4=已发布；5=已取消；6=已结束；7=已结算；8=已删除快照', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030004, '报名状态', 'club_points_registration_status', 0, '1=已报名；2=已取消', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030005, '签到签退目标', 'club_points_attendance_target_type', 0, '1=签到；2=签退', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030006, '签到签退来源', 'club_points_attendance_source_type', 0, '1=自助；2=补录；3=修正', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030007, '材料状态', 'club_points_material_status', 0, '1=草稿；2=待审核；3=已撤回；4=已驳回；5=已通过；6=已删除快照', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030008, '流水方向', 'club_points_transaction_direction', 0, '1=增加；2=扣减', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030009, '流水状态', 'club_points_transaction_status', 0, '1=有效；2=已被撤销；3=撤销流水', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030010, '冻结状态', 'club_points_freeze_status', 0, '1=冻结中；2=已转扣减；3=已释放', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030011, '兑换批次状态', 'club_points_redemption_batch_status', 0, '1=草稿；2=已开启；3=已关闭；4=已取消', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030012, '礼品状态', 'club_points_redemption_gift_status', 0, '1=上架；2=下架', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030013, '兑换申请状态', 'club_points_redemption_application_status', 0, '1=待审核；2=审核前取消；3=已通过并直接发放；4=已拒绝', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030014, '异议状态', 'club_points_dispute_status', 0, '1=待处理；2=已回复；3=已关闭', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030015, '业务任务状态', 'club_points_job_status', 0, '1=待运行；2=运行中；3=成功；4=可重试失败；5=最终失败；6=人工处理中；7=已关闭', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030016, '审核结果', 'club_points_review_result', 0, '1=通过；2=驳回/拒绝', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030017, '积分分类', 'club_points_point_category', 0, '10=基础参与；11=全程额外；20=主动贡献；30=特殊奖励；40=扣分；50=兑换扣减；60=年度清零；70=管理员调整；80=撤销流水', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030018, '负责人状态', 'club_points_leader_status', 0, '1=有效；2=解除', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030019, '报名取消原因', 'club_points_registration_cancel_reason', 0, '1=员工自助；2=退出俱乐部自动；3=管理员移除；4=活动取消', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030020, '规则版本状态', 'club_points_rule_version_status', 0, '1=草稿；2=已发布；3=已撤回；4=已停用', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030021, '规则项状态', 'club_points_rule_item_status', 0, '1=启用；2=停用', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030022, '流水来源类型', 'club_points_transaction_source_type', 0, '1=活动结算；2=非签到材料；3=管理员代录；4=兑换；5=年度清零；6=调整；7=撤销；8=异议处理', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030023, '年度清零状态', 'club_points_annual_clearing_status', 0, '1=成功；2=失败；3=跳过', '1', NOW(), '1', NOW(), b'0', NULL),
(1300030024, '活动结算状态', 'club_points_activity_settlement_status', 0, '1=待结算；2=结算中；3=已结算；4=结算失败；5=人工处理', '1', NOW(), '1', NOW(), b'0', NULL)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `status` = VALUES(`status`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = VALUES(`update_time`),
  `deleted` = VALUES(`deleted`),
  `deleted_time` = VALUES(`deleted_time`);

INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1300031001, 1, '启用', '1', 'club_points_club_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031002, 2, '停用', '2', 'club_points_club_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031003, 3, '已删除快照', '3', 'club_points_club_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031011, 1, '有效', '1', 'club_points_member_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031012, 2, '自主退出', '2', 'club_points_member_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031013, 3, '管理员移除', '3', 'club_points_member_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031021, 1, '草稿', '1', 'club_points_activity_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031022, 2, '待审核', '2', 'club_points_activity_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031023, 3, '已驳回', '3', 'club_points_activity_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031024, 4, '已发布', '4', 'club_points_activity_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031025, 5, '已取消', '5', 'club_points_activity_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031026, 6, '已结束', '6', 'club_points_activity_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031027, 7, '已结算', '7', 'club_points_activity_status', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031028, 8, '已删除快照', '8', 'club_points_activity_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031031, 1, '已报名', '1', 'club_points_registration_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031032, 2, '已取消', '2', 'club_points_registration_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031041, 1, '签到', '1', 'club_points_attendance_target_type', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031042, 2, '签退', '2', 'club_points_attendance_target_type', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031051, 1, '自助', '1', 'club_points_attendance_source_type', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031052, 2, '补录', '2', 'club_points_attendance_source_type', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031053, 3, '修正', '3', 'club_points_attendance_source_type', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031061, 1, '草稿', '1', 'club_points_material_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031062, 2, '待审核', '2', 'club_points_material_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031063, 3, '已撤回', '3', 'club_points_material_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031064, 4, '已驳回', '4', 'club_points_material_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031065, 5, '已通过', '5', 'club_points_material_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031066, 6, '已删除快照', '6', 'club_points_material_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031071, 1, '增加', '1', 'club_points_transaction_direction', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031072, 2, '扣减', '2', 'club_points_transaction_direction', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031081, 1, '有效', '1', 'club_points_transaction_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031082, 2, '已被撤销', '2', 'club_points_transaction_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031083, 3, '撤销流水', '3', 'club_points_transaction_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031091, 1, '冻结中', '1', 'club_points_freeze_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031092, 2, '已转扣减', '2', 'club_points_freeze_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031093, 3, '已释放', '3', 'club_points_freeze_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031101, 1, '草稿', '1', 'club_points_redemption_batch_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031102, 2, '已开启', '2', 'club_points_redemption_batch_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031103, 3, '已关闭', '3', 'club_points_redemption_batch_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031104, 4, '已取消', '4', 'club_points_redemption_batch_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031111, 1, '上架', '1', 'club_points_redemption_gift_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031112, 2, '下架', '2', 'club_points_redemption_gift_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031121, 1, '待审核', '1', 'club_points_redemption_application_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031122, 2, '审核前取消', '2', 'club_points_redemption_application_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031123, 3, '已通过并直接发放', '3', 'club_points_redemption_application_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031124, 4, '已拒绝', '4', 'club_points_redemption_application_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031131, 1, '待处理', '1', 'club_points_dispute_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031132, 2, '已回复', '2', 'club_points_dispute_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031133, 3, '已关闭', '3', 'club_points_dispute_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031141, 1, '待运行', '1', 'club_points_job_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031142, 2, '运行中', '2', 'club_points_job_status', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031143, 3, '成功', '3', 'club_points_job_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031144, 4, '可重试失败', '4', 'club_points_job_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031145, 5, '最终失败', '5', 'club_points_job_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031146, 6, '人工处理中', '6', 'club_points_job_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031147, 7, '已关闭', '7', 'club_points_job_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031151, 1, '通过', '1', 'club_points_review_result', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031152, 2, '驳回/拒绝', '2', 'club_points_review_result', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031161, 10, '基础参与积分', '10', 'club_points_point_category', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031162, 11, '全程参与额外积分', '11', 'club_points_point_category', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031163, 20, '主动贡献积分', '20', 'club_points_point_category', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031164, 30, '特殊奖励积分', '30', 'club_points_point_category', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031165, 40, '扣分', '40', 'club_points_point_category', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031166, 50, '兑换扣减', '50', 'club_points_point_category', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031167, 60, '年度清零', '60', 'club_points_point_category', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031168, 70, '管理员调整', '70', 'club_points_point_category', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031169, 80, '撤销流水', '80', 'club_points_point_category', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031171, 1, '有效', '1', 'club_points_leader_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031172, 2, '解除', '2', 'club_points_leader_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031181, 1, '员工自助', '1', 'club_points_registration_cancel_reason', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031182, 2, '退出俱乐部自动', '2', 'club_points_registration_cancel_reason', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031183, 3, '管理员移除', '3', 'club_points_registration_cancel_reason', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031184, 4, '活动取消', '4', 'club_points_registration_cancel_reason', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031191, 1, '草稿', '1', 'club_points_rule_version_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031192, 2, '已发布', '2', 'club_points_rule_version_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031193, 3, '已撤回', '3', 'club_points_rule_version_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031194, 4, '已停用', '4', 'club_points_rule_version_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031201, 1, '启用', '1', 'club_points_rule_item_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031202, 2, '停用', '2', 'club_points_rule_item_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031211, 1, '活动结算', '1', 'club_points_transaction_source_type', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031212, 2, '非签到材料', '2', 'club_points_transaction_source_type', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031213, 3, '管理员代录', '3', 'club_points_transaction_source_type', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031214, 4, '兑换', '4', 'club_points_transaction_source_type', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031215, 5, '年度清零', '5', 'club_points_transaction_source_type', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031216, 6, '调整', '6', 'club_points_transaction_source_type', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031217, 7, '撤销', '7', 'club_points_transaction_source_type', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031218, 8, '异议处理', '8', 'club_points_transaction_source_type', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031221, 1, '成功', '1', 'club_points_annual_clearing_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031222, 2, '失败', '2', 'club_points_annual_clearing_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031223, 3, '跳过', '3', 'club_points_annual_clearing_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031231, 1, '待结算', '1', 'club_points_activity_settlement_status', 0, 'info', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031232, 2, '结算中', '2', 'club_points_activity_settlement_status', 0, 'primary', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031233, 3, '已结算', '3', 'club_points_activity_settlement_status', 0, 'success', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031234, 4, '结算失败', '4', 'club_points_activity_settlement_status', 0, 'danger', '', NULL, '1', NOW(), '1', NOW(), b'0'),
(1300031235, 5, '人工处理', '5', 'club_points_activity_settlement_status', 0, 'warning', '', NULL, '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `sort` = VALUES(`sort`),
  `label` = VALUES(`label`),
  `value` = VALUES(`value`),
  `dict_type` = VALUES(`dict_type`),
  `status` = VALUES(`status`),
  `color_type` = VALUES(`color_type`),
  `css_class` = VALUES(`css_class`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = VALUES(`update_time`),
  `deleted` = VALUES(`deleted`);

-- ----------------------------
-- 默认规则版本
-- ----------------------------

INSERT INTO `club_points_rule_version` (`id`, `version_no`, `name`, `status`, `publicity_time`, `effective_time`, `published_time`, `summary`, `content`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1300040000, 'V2026.01', '俱乐部积分默认制度', 2, NOW(), '2026-01-01 00:00:00', NOW(), '俱乐部员工积分系统默认制度配置', '默认规则用于系统初始化，后续通过规则配置模块发布新版本。', 'M1 seed 初始化', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `version_no` = VALUES(`version_no`),
  `name` = VALUES(`name`),
  `status` = VALUES(`status`),
  `publicity_time` = VALUES(`publicity_time`),
  `effective_time` = VALUES(`effective_time`),
  `published_time` = VALUES(`published_time`),
  `summary` = VALUES(`summary`),
  `content` = VALUES(`content`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = VALUES(`update_time`),
  `deleted` = VALUES(`deleted`);

-- ----------------------------
-- 默认规则项
-- ----------------------------

INSERT INTO `club_points_rule_item` (`id`, `rule_version_id`, `item_code`, `item_name`, `item_type`, `category`, `min_points`, `max_points`, `default_points`, `int_value`, `decimal_value`, `text_value`, `json_value`, `status`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1300040001, 1300040000, 'ACTIVITY_SMALL_BASE', '小型活动基础分', 1, 10, 5, 5, 5, NULL, NULL, NULL, NULL, 1, 1, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040002, 1300040000, 'ACTIVITY_MEDIUM_BASE', '中型活动基础分', 1, 10, 8, 8, 8, NULL, NULL, NULL, NULL, 1, 2, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040003, 1300040000, 'ACTIVITY_LARGE_BASE', '大型活动基础分', 1, 10, 10, 10, 10, NULL, NULL, NULL, NULL, 1, 3, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040004, 1300040000, 'ACTIVITY_FULL_EXTRA', '全程参与额外分', 1, 11, 2, 2, 2, NULL, NULL, NULL, NULL, 1, 4, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040005, 1300040000, 'ACTIVITY_SETTLEMENT_GRACE_MINUTES', '活动结算缓冲分钟', 2, 10, NULL, NULL, NULL, 30, NULL, NULL, NULL, 1, 5, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040006, 1300040000, 'ABSENCE_SINGLE_DEDUCT', '无故缺席单次扣分', 1, 40, 2, 2, 2, NULL, NULL, NULL, NULL, 1, 6, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040007, 1300040000, 'ABSENCE_MONTHLY_THRESHOLD', '月度累计缺席阈值', 2, 40, NULL, NULL, NULL, 3, NULL, NULL, NULL, 1, 7, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040008, 1300040000, 'ABSENCE_MONTHLY_DEDUCT', '月度累计缺席扣分', 1, 40, 5, 5, 5, NULL, NULL, NULL, NULL, 1, 8, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040009, 1300040000, 'VIOLATION_DEDUCT_RANGE', '违规扣分区间', 6, 40, 5, 20, 10, NULL, NULL, NULL, JSON_OBJECT('min', 5, 'max', 20), 1, 9, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040010, 1300040000, 'FRAUD_CLEAR_ALL', '弄虚作假清零', 3, 40, NULL, NULL, NULL, 1, NULL, NULL, NULL, 1, 10, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040011, 1300040000, 'MONTHLY_DUTY', '月度履职积分', 1, 20, 5, 5, 5, NULL, NULL, NULL, NULL, 1, 11, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040012, 1300040000, 'PLAN_EXECUTION', '策划执行积分', 1, 20, 5, 15, 10, NULL, NULL, NULL, NULL, 1, 12, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040013, 1300040000, 'PUBLICITY_SUGGESTION', '宣传建议积分', 1, 20, 2, 10, 5, NULL, NULL, NULL, NULL, 1, 13, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040014, 1300040000, 'AWARD_REWARD', '获奖积分', 1, 30, 10, 50, 20, NULL, NULL, NULL, NULL, 1, 14, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040015, 1300040000, 'RECOMMEND_MEMBER_LIMIT', '推荐新会员年度上限', 2, 30, NULL, NULL, NULL, 5, NULL, NULL, NULL, 1, 15, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040016, 1300040000, 'SPECIAL_CONTRIBUTION', '特殊贡献积分', 1, 30, 10, 100, 30, NULL, NULL, NULL, NULL, 1, 16, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040017, 1300040000, 'REDEMPTION_MIN_POINTS', '兑换最低可用积分', 2, 50, NULL, NULL, NULL, 50, NULL, NULL, NULL, 1, 17, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040018, 1300040000, 'REDEMPTION_DEFAULT_QUALIFIED_COUNT', '默认兑换资格人数', 2, 50, NULL, NULL, NULL, 180, NULL, NULL, NULL, 1, 18, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040019, 1300040000, 'REDEMPTION_INCLUDE_TIE', '并列同分全进', 3, 50, NULL, NULL, NULL, 1, NULL, NULL, NULL, 1, 19, NULL, '1', NOW(), '1', NOW(), b'0'),
(1300040020, 1300040000, 'ANNUAL_RANKING_INCENTIVE_AMOUNT', '年度排名激励金额', 4, 60, NULL, NULL, NULL, NULL, 10000.000000, NULL, NULL, 1, 20, '单位分', '1', NOW(), '1', NOW(), b'0'),
(1300040021, 1300040000, 'ANNUAL_INNOVATION_AWARD_AMOUNT', '特色创新奖金额', 4, 60, NULL, NULL, NULL, NULL, 5000.000000, NULL, NULL, 1, 21, '单位分', '1', NOW(), '1', NOW(), b'0'),
(1300040022, 1300040000, 'CROSS_YEAR_FREEZE_RELEASE_POLICY', '跨年冻结释放口径', 5, 60, NULL, NULL, NULL, NULL, NULL, '跨年仍冻结的积分不参与清零，释放后回到当年可用余额。', NULL, 1, 22, NULL, '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `rule_version_id` = VALUES(`rule_version_id`),
  `item_code` = VALUES(`item_code`),
  `item_name` = VALUES(`item_name`),
  `item_type` = VALUES(`item_type`),
  `category` = VALUES(`category`),
  `min_points` = VALUES(`min_points`),
  `max_points` = VALUES(`max_points`),
  `default_points` = VALUES(`default_points`),
  `int_value` = VALUES(`int_value`),
  `decimal_value` = VALUES(`decimal_value`),
  `text_value` = VALUES(`text_value`),
  `json_value` = VALUES(`json_value`),
  `status` = VALUES(`status`),
  `sort` = VALUES(`sort`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = VALUES(`update_time`),
  `deleted` = VALUES(`deleted`);

-- ----------------------------
-- 通知模板
-- ----------------------------

INSERT INTO `system_notify_template` (`id`, `name`, `code`, `nickname`, `content`, `type`, `params`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1300050001, '积分变动通知', 'club_points_transaction_changed', '俱乐部积分', '你的积分发生变动：${reason}，${direction}${points} 分，当前可用积分 ${availablePoints} 分。', 2, '["reason","direction","points","availablePoints"]', 0, '积分流水生成后通知员工', '1', NOW(), '1', NOW(), b'0'),
(1300050002, '兑换审核结果通知', 'club_points_redemption_reviewed', '俱乐部积分', '你的兑换申请 ${applicationNo} 审核结果：${result}。${reason}', 2, '["applicationNo","result","reason"]', 0, '兑换审核后通知员工', '1', NOW(), '1', NOW(), b'0'),
(1300050003, '异议处理结果通知', 'club_points_dispute_replied', '俱乐部积分', '你的异议 ${title} 已处理：${replyContent}', 2, '["title","replyContent"]', 0, '异议回复后通知员工', '1', NOW(), '1', NOW(), b'0'),
(1300050004, '活动结算完成通知', 'club_points_activity_settled', '俱乐部积分', '活动 ${activityTitle} 已完成积分结算，本次获得 ${points} 分。', 2, '["activityTitle","points"]', 0, '活动结算后通知员工', '1', NOW(), '1', NOW(), b'0'),
(1300050005, '活动审核结果通知', 'club_points_activity_reviewed', '俱乐部积分', '你提交的活动 ${activityTitle} 审核结果：${result}。${reason}', 2, '["activityTitle","result","reason"]', 0, '活动审核后通知提交人', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `code` = VALUES(`code`),
  `nickname` = VALUES(`nickname`),
  `content` = VALUES(`content`),
  `type` = VALUES(`type`),
  `params` = VALUES(`params`),
  `status` = VALUES(`status`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = VALUES(`update_time`),
  `deleted` = VALUES(`deleted`);

-- ----------------------------
-- 定时任务
-- ----------------------------

INSERT INTO `infra_job` (`id`, `name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES
(1300060001, '俱乐部活动结算 Job', 2, 'clubPointsActivitySettlementJob', '', '0 */10 * * * ?', 3, 60, 300, '1', NOW(), '1', NOW(), b'0'),
(1300060002, '俱乐部年度清零 Job', 2, 'clubPointsAnnualClearingJob', '', '0 0 2 1 1 ?', 3, 300, 1800, '1', NOW(), '1', NOW(), b'0'),
(1300060003, '俱乐部年度排名激励 Job', 2, 'clubPointsAnnualRankingJob', '', '0 0 3 1 1 ?', 3, 300, 1800, '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `status` = VALUES(`status`),
  `handler_name` = VALUES(`handler_name`),
  `handler_param` = VALUES(`handler_param`),
  `cron_expression` = VALUES(`cron_expression`),
  `retry_count` = VALUES(`retry_count`),
  `retry_interval` = VALUES(`retry_interval`),
  `monitor_timeout` = VALUES(`monitor_timeout`),
  `updater` = VALUES(`updater`),
  `update_time` = VALUES(`update_time`),
  `deleted` = VALUES(`deleted`);
