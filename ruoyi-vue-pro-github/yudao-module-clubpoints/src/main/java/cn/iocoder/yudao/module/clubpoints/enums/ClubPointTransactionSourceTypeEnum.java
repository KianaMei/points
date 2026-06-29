package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 积分流水来源类型枚举
 */
public enum ClubPointTransactionSourceTypeEnum implements ArrayValuable<Integer> {

    ACTIVITY_SETTLEMENT(1, "活动积分发放"),
    CONTRIBUTION_MATERIAL(2, "非签到材料"),
    ADMIN_DIRECT(3, "管理员代录"),
    REDEMPTION(4, "兑换"),
    ANNUAL_CLEARING(5, "年度清零"),
    ADJUSTMENT(6, "调整"),
    REVERSAL(7, "撤销"),
    DISPUTE_HANDLING(8, "异议处理");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointTransactionSourceTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    ClubPointTransactionSourceTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
