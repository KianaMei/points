package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 积分冻结状态枚举
 */
public enum ClubPointFreezeStatusEnum implements ArrayValuable<Integer> {

    FROZEN(1, "冻结中"),
    CONVERTED(2, "已转扣减"),
    RELEASED(3, "已释放");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointFreezeStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    ClubPointFreezeStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
