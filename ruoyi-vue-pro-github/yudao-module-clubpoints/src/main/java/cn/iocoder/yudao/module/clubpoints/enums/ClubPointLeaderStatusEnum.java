package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 俱乐部负责人状态枚举
 */
public enum ClubPointLeaderStatusEnum implements ArrayValuable<Integer> {

    ACTIVE(1, "有效"),
    REMOVED(2, "解除");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointLeaderStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    ClubPointLeaderStatusEnum(Integer status, String name) {
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
