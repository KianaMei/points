package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 活动积分发放触发来源枚举
 */
public enum ClubPointActivitySettlementTriggerSourceEnum implements ArrayValuable<Integer> {

    SCHEDULED(1, "定时"),
    ADMIN_MANUAL(2, "管理员手动");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointActivitySettlementTriggerSourceEnum::getSource)
            .toArray(Integer[]::new);

    private final Integer source;
    private final String name;

    ClubPointActivitySettlementTriggerSourceEnum(Integer source, String name) {
        this.source = source;
        this.name = name;
    }

    public Integer getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
