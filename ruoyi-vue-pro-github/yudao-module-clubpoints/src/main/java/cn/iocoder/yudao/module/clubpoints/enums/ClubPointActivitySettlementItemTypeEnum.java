package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 活动积分发放流水明细类型枚举
 */
public enum ClubPointActivitySettlementItemTypeEnum {

    BASE("BASE", ClubPointTransactionDirectionEnum.INCREASE.getDirection(),
            ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(), null),
    FULL_EXTRA("FULL_EXTRA", ClubPointTransactionDirectionEnum.INCREASE.getDirection(),
            ClubPointCategoryEnum.FULL_PARTICIPATION_EXTRA.getCategory(),
            ClubPointRuleItemCodeEnum.ACTIVITY_FULL_EXTRA.getCode()),
    ABSENCE_SINGLE("ABSENCE_SINGLE", ClubPointTransactionDirectionEnum.DECREASE.getDirection(),
            ClubPointCategoryEnum.DEDUCTION.getCategory(),
            ClubPointRuleItemCodeEnum.ABSENCE_SINGLE_DEDUCT.getCode()),
    ABSENCE_MONTHLY("ABSENCE_MONTHLY", ClubPointTransactionDirectionEnum.DECREASE.getDirection(),
            ClubPointCategoryEnum.DEDUCTION.getCategory(),
            ClubPointRuleItemCodeEnum.ABSENCE_MONTHLY_DEDUCT.getCode());

    private static final String ACTIVITY_SETTLEMENT_KEY_PREFIX = "ACTIVITY_SETTLEMENT";
    private static final String MONTHLY_ABSENCE_KEY_PREFIX = "ABSENCE_MONTHLY";

    private final String itemType;
    private final Integer direction;
    private final Integer pointCategory;
    private final String ruleItemCode;

    ClubPointActivitySettlementItemTypeEnum(String itemType, Integer direction,
                                            Integer pointCategory, String ruleItemCode) {
        this.itemType = itemType;
        this.direction = direction;
        this.pointCategory = pointCategory;
        this.ruleItemCode = ruleItemCode;
    }

    public String getItemType() {
        return itemType;
    }

    public Integer getSourceType() {
        return ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType();
    }

    public Integer getDirection() {
        return direction;
    }

    public Integer getPointCategory() {
        return pointCategory;
    }

    public String getRuleItemCode() {
        return ruleItemCode;
    }

    public String buildIdempotencyKey(Long activityId, Long userId, Integer businessMonth) {
        if (this == ABSENCE_MONTHLY) {
            return MONTHLY_ABSENCE_KEY_PREFIX + ":" + businessMonth + ":" + userId;
        }
        return ACTIVITY_SETTLEMENT_KEY_PREFIX + ":" + activityId + ":" + userId + ":" + itemType;
    }

}
