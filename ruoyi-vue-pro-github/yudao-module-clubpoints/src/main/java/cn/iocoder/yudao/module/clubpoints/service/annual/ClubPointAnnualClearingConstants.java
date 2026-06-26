package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 年度清零模型常量
 */
public final class ClubPointAnnualClearingConstants {

    public static final ZoneId CLEARING_ZONE = ZoneId.of("Asia/Shanghai");
    public static final String IDEMPOTENCY_PREFIX = "ANNUAL_CLEARING";
    public static final String POINT_TYPE_CODE = "ANNUAL_CLEARING";
    public static final String SOURCE_TITLE = "年度清零";
    public static final String CROSS_YEAR_FREEZE_RELEASE_POLICY = "RELEASE_TO_ACCOUNT_WITHOUT_OVERDUE_CLEARING";
    public static final Integer TRANSACTION_SOURCE_TYPE = ClubPointTransactionSourceTypeEnum.ANNUAL_CLEARING.getType();
    public static final Integer TRANSACTION_DIRECTION = ClubPointTransactionDirectionEnum.DECREASE.getDirection();
    public static final Integer POINT_CATEGORY = ClubPointCategoryEnum.ANNUAL_CLEARING.getCategory();

    private ClubPointAnnualClearingConstants() {
    }

    public static String buildIdempotencyKey(Integer year, Long userId) {
        return IDEMPOTENCY_PREFIX + ":" + year + ":" + userId;
    }

    public static LocalDateTime buildScheduledClearTime(Integer year) {
        return LocalDate.of(year, 1, 1).atStartOfDay();
    }

}
