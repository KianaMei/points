package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 异议目标类型枚举
 */
public enum ClubPointDisputeTargetTypeEnum {

    TRANSACTION(1, "积分流水"),
    REDEMPTION_APPLICATION(2, "兑换申请"),
    ACTIVITY_REGISTRATION(3, "活动报名"),
    OTHER(4, "其他");

    private final Integer type;
    private final String name;

    ClubPointDisputeTargetTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static ClubPointDisputeTargetTypeEnum of(Integer type) {
        return Arrays.stream(values())
                .filter(value -> value.type.equals(type))
                .findFirst()
                .orElse(null);
    }

}
