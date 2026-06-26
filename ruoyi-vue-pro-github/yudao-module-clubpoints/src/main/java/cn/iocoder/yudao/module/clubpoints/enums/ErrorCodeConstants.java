package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Club Points 错误码枚举类
 *
 * clubpoints 俱乐部积分，使用 1-300-000-000 段。
 */
public interface ErrorCodeConstants {

    ErrorCode CLUB_POINTS_MODULE_NOT_INITIALIZED = new ErrorCode(1_300_000_000, "俱乐部积分模块尚未初始化");
    ErrorCode CLUB_SCOPE_DENIED = new ErrorCode(1_300_000_001, "无权访问该俱乐部数据");
    ErrorCode CLUB_AUDIT_WRITE_FAILED = new ErrorCode(1_300_000_002, "强审计写入失败");
    ErrorCode CLUB_ATTACHMENT_INVALID = new ErrorCode(1_300_000_003, "附件参数无效");
    ErrorCode CLUB_ATTACHMENT_NOT_EXISTS = new ErrorCode(1_300_000_004, "附件不存在");
    ErrorCode CLUB_ATTACHMENT_LOCKED = new ErrorCode(1_300_000_005, "附件已锁定，不能删除");
    ErrorCode CLUB_RULE_VERSION_NOT_EXISTS = new ErrorCode(1_300_000_006, "规则版本不存在");
    ErrorCode CLUB_RULE_VERSION_STATUS_INVALID = new ErrorCode(1_300_000_007, "规则版本状态不允许当前操作");
    ErrorCode CLUB_RULE_ITEM_NOT_EXISTS = new ErrorCode(1_300_000_008, "规则项不存在");
    ErrorCode CLUB_RULE_ITEM_CODE_DUPLICATED = new ErrorCode(1_300_000_009, "规则项编码重复");
    ErrorCode CLUB_RULE_VALUE_OUT_OF_RANGE = new ErrorCode(1_300_000_010, "规则值超出允许范围");
    ErrorCode CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH = new ErrorCode(1_300_000_011, "可用积分不足");
    ErrorCode CLUB_LEDGER_FROZEN_POINTS_NOT_ENOUGH = new ErrorCode(1_300_000_012, "冻结积分不足");
    ErrorCode CLUB_LEDGER_TRANSACTION_DUPLICATED = new ErrorCode(1_300_000_013, "积分流水重复");
    ErrorCode CLUB_LEDGER_TRANSACTION_NOT_EXISTS = new ErrorCode(1_300_000_014, "积分流水不存在");
    ErrorCode CLUB_LEDGER_YEAR_ALREADY_CLEARED = new ErrorCode(1_300_000_015, "年度积分已清零");
    ErrorCode CLUB_LEDGER_REVERSE_INVALID = new ErrorCode(1_300_000_016, "积分流水撤销不允许当前操作");
    ErrorCode CLUB_LEDGER_FREEZE_DUPLICATED = new ErrorCode(1_300_000_017, "积分冻结记录重复");
    ErrorCode CLUB_LEDGER_FREEZE_NOT_EXISTS = new ErrorCode(1_300_000_018, "积分冻结记录不存在");
    ErrorCode CLUB_LEDGER_FREEZE_STATUS_INVALID = new ErrorCode(1_300_000_019, "积分冻结状态不允许当前操作");
    ErrorCode CLUB_LEDGER_ADJUST_INVALID = new ErrorCode(1_300_000_020, "积分调整参数无效");
    ErrorCode CLUB_NOT_FOUND = new ErrorCode(1_300_000_021, "俱乐部不存在");
    ErrorCode CLUB_DISABLED = new ErrorCode(1_300_000_022, "俱乐部已停用");
    ErrorCode CLUB_ALREADY_JOINED = new ErrorCode(1_300_000_023, "成员已存在");
    ErrorCode CLUB_NOT_MEMBER = new ErrorCode(1_300_000_024, "成员不存在");
    ErrorCode CLUB_LEADER_ALREADY_EXISTS = new ErrorCode(1_300_000_025, "负责人已存在");
    ErrorCode CLUB_LEADER_NOT_EXISTS = new ErrorCode(1_300_000_026, "负责人不存在");
    ErrorCode CLUB_STRONG_CONFIRM_INVALID = new ErrorCode(1_300_000_027, "俱乐部强确认无效");
    ErrorCode CLUB_DELETE_HAS_REFERENCES = new ErrorCode(1_300_000_028, "俱乐部已有历史关联，不能物理删除");
    ErrorCode CLUB_CODE_DUPLICATED = new ErrorCode(1_300_000_029, "俱乐部编号重复");
    ErrorCode CLUB_NAME_DUPLICATED = new ErrorCode(1_300_000_030, "俱乐部名称重复");
    ErrorCode CLUB_ACTIVITY_STATUS_INVALID = new ErrorCode(1_300_000_031, "活动状态不允许当前操作");
    ErrorCode CLUB_ACTIVITY_NOT_FOUND = new ErrorCode(1_300_000_032, "活动不存在");
    ErrorCode CLUB_ACTIVITY_TIME_INVALID = new ErrorCode(1_300_000_033, "活动时间配置无效");
    ErrorCode CLUB_ACTIVITY_LEVEL_INVALID = new ErrorCode(1_300_000_034, "活动等级无效");
    ErrorCode CLUB_ACTIVITY_REGISTRATION_CLOSED = new ErrorCode(1_300_000_035, "活动报名已截止");
    ErrorCode CLUB_ACTIVITY_CANCEL_WINDOW_CLOSED = new ErrorCode(1_300_000_036, "活动取消报名窗口已关闭");
    ErrorCode CLUB_ACTIVITY_REGISTRATION_DUPLICATED = new ErrorCode(1_300_000_037, "活动报名重复");
    ErrorCode CLUB_ACTIVITY_REGISTRATION_NOT_FOUND = new ErrorCode(1_300_000_038, "活动报名记录不存在");
    ErrorCode CLUB_ATTENDANCE_WINDOW_CLOSED = new ErrorCode(1_300_000_039, "签到签退不在允许窗口内");
    ErrorCode CLUB_ATTENDANCE_ALREADY_EXISTS = new ErrorCode(1_300_000_040, "签到签退记录已存在");
    ErrorCode CLUB_ATTENDANCE_CHECKIN_REQUIRED = new ErrorCode(1_300_000_041, "请先完成签到");
    ErrorCode CLUB_ATTENDANCE_NOT_FOUND = new ErrorCode(1_300_000_042, "签到签退记录不存在");
    ErrorCode CLUB_ATTENDANCE_TARGET_INVALID = new ErrorCode(1_300_000_043, "签到签退类型无效");
    ErrorCode CLUB_CONTRIBUTION_MATERIAL_NOT_FOUND = new ErrorCode(1_300_000_044, "非签到材料不存在");
    ErrorCode CLUB_CONTRIBUTION_STATUS_INVALID = new ErrorCode(1_300_000_045, "非签到材料状态不允许当前操作");
    ErrorCode CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE = new ErrorCode(1_300_000_046, "非签到材料分值超出规则范围");
    ErrorCode CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED = new ErrorCode(1_300_000_047, "非签到材料附件缺失");
    ErrorCode CLUB_CONTRIBUTION_SUBMIT_DUPLICATED = new ErrorCode(1_300_000_048, "非签到材料重复提交");
    ErrorCode CLUB_CONTRIBUTION_REVIEW_DENIED = new ErrorCode(1_300_000_049, "无权审核非签到材料");
    ErrorCode CLUB_REDEMPTION_BATCH_NOT_EXISTS = new ErrorCode(1_300_000_050, "兑换批次不存在");
    ErrorCode CLUB_REDEMPTION_BATCH_STATUS_INVALID = new ErrorCode(1_300_000_051, "兑换批次状态不允许当前操作");
    ErrorCode CLUB_REDEMPTION_BATCH_TIME_INVALID = new ErrorCode(1_300_000_052, "兑换批次时间配置无效");
    ErrorCode CLUB_REDEMPTION_BATCH_RULE_INVALID = new ErrorCode(1_300_000_053, "兑换批次资格规则无效");
    ErrorCode CLUB_REDEMPTION_ELIGIBILITY_SNAPSHOT_DUPLICATED = new ErrorCode(1_300_000_054, "兑换资格快照已生成");
    ErrorCode CLUB_REDEMPTION_BATCH_CLOSED = new ErrorCode(1_300_000_055, "兑换批次未开放或已关闭");
    ErrorCode CLUB_REDEMPTION_GIFT_NOT_EXISTS = new ErrorCode(1_300_000_056, "兑换礼品不存在");
    ErrorCode CLUB_REDEMPTION_GIFT_INVALID = new ErrorCode(1_300_000_057, "兑换礼品参数无效");
    ErrorCode CLUB_REDEMPTION_GIFT_STATUS_INVALID = new ErrorCode(1_300_000_058, "兑换礼品状态不允许当前操作");
    ErrorCode CLUB_REDEMPTION_GIFT_STOCK_INVALID = new ErrorCode(1_300_000_059, "兑换礼品库存状态无效");
    ErrorCode CLUB_REDEMPTION_GIFT_STOCK_NOT_ENOUGH = new ErrorCode(1_300_000_060, "兑换礼品库存不足");
    ErrorCode CLUB_REDEMPTION_ELIGIBILITY_NOT_EXISTS = new ErrorCode(1_300_000_061, "兑换资格快照不存在");
    ErrorCode CLUB_REDEMPTION_ELIGIBILITY_NOT_QUALIFIED = new ErrorCode(1_300_000_062, "不具备本批次兑换资格");
    ErrorCode CLUB_REDEMPTION_APPLICATION_NOT_EXISTS = new ErrorCode(1_300_000_063, "兑换申请不存在");
    ErrorCode CLUB_REDEMPTION_APPLICATION_STATUS_INVALID = new ErrorCode(1_300_000_064, "兑换申请状态不允许当前操作");

}
