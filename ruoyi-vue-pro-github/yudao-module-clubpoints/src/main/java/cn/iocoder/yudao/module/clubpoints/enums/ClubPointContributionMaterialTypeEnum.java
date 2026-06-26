package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 非签到积分材料类型枚举
 */
public enum ClubPointContributionMaterialTypeEnum {

    MONTHLY_DUTY(1, ClubPointRuleItemCodeEnum.MONTHLY_DUTY.getCode(),
            ClubPointCategoryEnum.ACTIVE_CONTRIBUTION.getCategory(),
            ClubPointTransactionDirectionEnum.INCREASE.getDirection()),
    PLAN_EXECUTION(2, ClubPointRuleItemCodeEnum.PLAN_EXECUTION.getCode(),
            ClubPointCategoryEnum.ACTIVE_CONTRIBUTION.getCategory(),
            ClubPointTransactionDirectionEnum.INCREASE.getDirection()),
    PUBLICITY_SUGGESTION(3, ClubPointRuleItemCodeEnum.PUBLICITY_SUGGESTION.getCode(),
            ClubPointCategoryEnum.ACTIVE_CONTRIBUTION.getCategory(),
            ClubPointTransactionDirectionEnum.INCREASE.getDirection()),
    AWARD_REWARD(4, ClubPointRuleItemCodeEnum.AWARD_REWARD.getCode(),
            ClubPointCategoryEnum.SPECIAL_REWARD.getCategory(),
            ClubPointTransactionDirectionEnum.INCREASE.getDirection()),
    SPECIAL_CONTRIBUTION(5, ClubPointRuleItemCodeEnum.SPECIAL_CONTRIBUTION.getCode(),
            ClubPointCategoryEnum.SPECIAL_REWARD.getCategory(),
            ClubPointTransactionDirectionEnum.INCREASE.getDirection()),
    VIOLATION_DEDUCT(6, ClubPointRuleItemCodeEnum.VIOLATION_DEDUCT_RANGE.getCode(),
            ClubPointCategoryEnum.DEDUCTION.getCategory(),
            ClubPointTransactionDirectionEnum.DECREASE.getDirection()),
    FRAUD_HANDLE(7, ClubPointRuleItemCodeEnum.FRAUD_CLEAR_ALL.getCode(),
            ClubPointCategoryEnum.DEDUCTION.getCategory(),
            ClubPointTransactionDirectionEnum.DECREASE.getDirection());

    private final Integer type;
    private final String ruleItemCode;
    private final Integer pointCategory;
    private final Integer direction;

    ClubPointContributionMaterialTypeEnum(Integer type, String ruleItemCode, Integer pointCategory,
                                          Integer direction) {
        this.type = type;
        this.ruleItemCode = ruleItemCode;
        this.pointCategory = pointCategory;
        this.direction = direction;
    }

    public Integer getType() {
        return type;
    }

    public String getRuleItemCode() {
        return ruleItemCode;
    }

    public Integer getPointCategory() {
        return pointCategory;
    }

    public Integer getDirection() {
        return direction;
    }

    public static ClubPointContributionMaterialTypeEnum of(Integer type) {
        return Arrays.stream(values())
                .filter(value -> value.type.equals(type))
                .findFirst()
                .orElse(null);
    }

}
