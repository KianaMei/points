package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Club Points 错误码枚举类
 *
 * clubpoints 俱乐部积分，使用 1-300-000-000 段。
 */
public interface ErrorCodeConstants {

    ErrorCode CLUB_POINTS_MODULE_NOT_INITIALIZED = new ErrorCode(1_300_000_000, "俱乐部积分模块尚未初始化");
    ErrorCode CLUB_SCOPE_DENIED = new ErrorCode(1_300_000_001, "无权访问该俱乐部积分数据");
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

}
