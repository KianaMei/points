package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 积分流水方向枚举
 */
public enum ClubPointTransactionDirectionEnum implements ArrayValuable<Integer> {

    INCREASE(1, "增加"),
    DECREASE(2, "扣减");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointTransactionDirectionEnum::getDirection)
            .toArray(Integer[]::new);

    private final Integer direction;
    private final String name;

    ClubPointTransactionDirectionEnum(Integer direction, String name) {
        this.direction = direction;
        this.name = name;
    }

    public Integer getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
