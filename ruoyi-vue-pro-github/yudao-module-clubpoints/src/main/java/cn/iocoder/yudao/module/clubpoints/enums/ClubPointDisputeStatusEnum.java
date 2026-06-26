package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 异议状态枚举
 */
public enum ClubPointDisputeStatusEnum {

    PENDING(1, "待处理"),
    REPLIED(2, "已回复"),
    CLOSED(3, "已关闭");

    private final Integer status;
    private final String name;

    ClubPointDisputeStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static ClubPointDisputeStatusEnum of(Integer status) {
        return Arrays.stream(values())
                .filter(value -> value.status.equals(status))
                .findFirst()
                .orElse(null);
    }

}
