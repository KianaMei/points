package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 积分冻结来源类型枚举
 */
public enum ClubPointFreezeSourceTypeEnum implements ArrayValuable<Integer> {

    REDEMPTION_APPLICATION(1, "兑换申请");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointFreezeSourceTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    ClubPointFreezeSourceTypeEnum(Integer type, String name) {
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
