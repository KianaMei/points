package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 异议关联处理动作枚举
 */
public enum ClubPointDisputeRelatedActionTypeEnum {

    NO_ACTION(1, "无积分动作"),
    ADJUSTMENT(2, "调整流水"),
    REVERSE(3, "撤销流水");

    private final Integer type;
    private final String name;

    ClubPointDisputeRelatedActionTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static ClubPointDisputeRelatedActionTypeEnum of(Integer type) {
        return Arrays.stream(values())
                .filter(value -> value.type.equals(type))
                .findFirst()
                .orElse(null);
    }

}
