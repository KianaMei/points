package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 兑换批次状态枚举
 */
public enum ClubPointRedemptionBatchStatusEnum {

    DRAFT(1, "草稿"),
    OPENED(2, "已开启"),
    CLOSED(3, "已关闭"),
    CANCELLED(4, "已取消");

    private final Integer status;
    private final String name;

    ClubPointRedemptionBatchStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static ClubPointRedemptionBatchStatusEnum of(Integer status) {
        return Arrays.stream(values())
                .filter(value -> value.status.equals(status))
                .findFirst()
                .orElse(null);
    }

}
