package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 积分分类枚举
 */
public enum ClubPointCategoryEnum implements ArrayValuable<Integer> {

    BASIC_PARTICIPATION(10, "基础参与积分"),
    FULL_PARTICIPATION_EXTRA(11, "全程参与额外积分"),
    ACTIVE_CONTRIBUTION(20, "主动贡献积分"),
    SPECIAL_REWARD(30, "特殊奖励积分"),
    DEDUCTION(40, "扣分"),
    REDEMPTION_DEDUCTION(50, "兑换扣减"),
    ANNUAL_CLEARING(60, "年度清零"),
    ADMIN_ADJUSTMENT(70, "管理员调整"),
    REVERSAL(80, "撤销流水");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointCategoryEnum::getCategory)
            .toArray(Integer[]::new);

    private final Integer category;
    private final String name;

    ClubPointCategoryEnum(Integer category, String name) {
        this.category = category;
        this.name = name;
    }

    public Integer getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
