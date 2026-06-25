package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 年度清零状态枚举
 */
public enum ClubPointAnnualClearingStatusEnum implements ArrayValuable<Integer> {

    SUCCESS(1, "成功"),
    FAILED(2, "失败"),
    SKIPPED(3, "跳过");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointAnnualClearingStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    ClubPointAnnualClearingStatusEnum(Integer status, String name) {
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
