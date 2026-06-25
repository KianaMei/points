package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 积分规则项编码枚举
 */
public enum ClubPointRuleItemCodeEnum {

    ACTIVITY_SMALL_BASE("ACTIVITY_SMALL_BASE", "小型活动基础分"),
    ACTIVITY_MEDIUM_BASE("ACTIVITY_MEDIUM_BASE", "中型活动基础分"),
    ACTIVITY_LARGE_BASE("ACTIVITY_LARGE_BASE", "大型活动基础分"),
    ACTIVITY_FULL_EXTRA("ACTIVITY_FULL_EXTRA", "全程参与额外分"),
    ACTIVITY_SETTLEMENT_GRACE_MINUTES("ACTIVITY_SETTLEMENT_GRACE_MINUTES", "活动结算缓冲分钟"),
    ABSENCE_SINGLE_DEDUCT("ABSENCE_SINGLE_DEDUCT", "无故缺席单次扣分"),
    ABSENCE_MONTHLY_THRESHOLD("ABSENCE_MONTHLY_THRESHOLD", "月度累计缺席阈值"),
    ABSENCE_MONTHLY_DEDUCT("ABSENCE_MONTHLY_DEDUCT", "月度累计缺席扣分"),
    VIOLATION_DEDUCT_RANGE("VIOLATION_DEDUCT_RANGE", "违规扣分区间"),
    FRAUD_CLEAR_ALL("FRAUD_CLEAR_ALL", "弄虚作假清零"),
    MONTHLY_DUTY("MONTHLY_DUTY", "月度履职积分"),
    PLAN_EXECUTION("PLAN_EXECUTION", "策划执行积分"),
    PUBLICITY_SUGGESTION("PUBLICITY_SUGGESTION", "宣传建议积分"),
    AWARD_REWARD("AWARD_REWARD", "获奖积分"),
    RECOMMEND_MEMBER_LIMIT("RECOMMEND_MEMBER_LIMIT", "推荐新会员年度上限"),
    SPECIAL_CONTRIBUTION("SPECIAL_CONTRIBUTION", "特殊贡献积分"),
    REDEMPTION_MIN_POINTS("REDEMPTION_MIN_POINTS", "兑换最低可用积分"),
    REDEMPTION_DEFAULT_QUALIFIED_COUNT("REDEMPTION_DEFAULT_QUALIFIED_COUNT", "默认兑换资格人数"),
    REDEMPTION_INCLUDE_TIE("REDEMPTION_INCLUDE_TIE", "并列同分全进"),
    ANNUAL_RANKING_INCENTIVE_AMOUNT("ANNUAL_RANKING_INCENTIVE_AMOUNT", "年度排名激励金额"),
    ANNUAL_INNOVATION_AWARD_AMOUNT("ANNUAL_INNOVATION_AWARD_AMOUNT", "特色创新奖金额"),
    CROSS_YEAR_FREEZE_RELEASE_POLICY("CROSS_YEAR_FREEZE_RELEASE_POLICY", "跨年冻结释放口径");

    private final String code;
    private final String name;

    ClubPointRuleItemCodeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
