package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 积分规则项值类型枚举
 */
public enum ClubPointRuleItemTypeEnum implements ArrayValuable<Integer> {

    POINTS(1, "分值"),
    THRESHOLD(2, "阈值"),
    SWITCH(3, "开关"),
    AMOUNT(4, "金额"),
    TEXT(5, "文本"),
    JSON(6, "JSON");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointRuleItemTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    ClubPointRuleItemTypeEnum(Integer type, String name) {
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
