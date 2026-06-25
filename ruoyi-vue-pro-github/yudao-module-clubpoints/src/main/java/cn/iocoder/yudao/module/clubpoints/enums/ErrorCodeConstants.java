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

}
