package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 积分流水状态枚举
 */
public enum ClubPointTransactionStatusEnum implements ArrayValuable<Integer> {

    VALID(1, "有效"),
    REVERSED(2, "已被撤销"),
    REVERSAL(3, "撤销流水");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointTransactionStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    ClubPointTransactionStatusEnum(Integer status, String name) {
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
