package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 活动结算展示状态枚举
 */
public enum ClubPointActivitySettlementStatusEnum implements ArrayValuable<Integer> {

    PENDING(1, "待结算"),
    PROCESSING(2, "结算中"),
    SETTLED(3, "已结算"),
    FAILED(4, "结算失败"),
    MANUAL_HANDLING(5, "人工处理");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointActivitySettlementStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    ClubPointActivitySettlementStatusEnum(Integer status, String name) {
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
