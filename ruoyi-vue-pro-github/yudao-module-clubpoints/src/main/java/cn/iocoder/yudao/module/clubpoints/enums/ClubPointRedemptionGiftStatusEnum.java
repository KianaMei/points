package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 兑换礼品状态枚举
 */
public enum ClubPointRedemptionGiftStatusEnum {

    ON_SHELF(1, "上架"),
    OFF_SHELF(2, "下架");

    private final Integer status;
    private final String name;

    ClubPointRedemptionGiftStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static ClubPointRedemptionGiftStatusEnum of(Integer status) {
        return Arrays.stream(values())
                .filter(value -> value.status.equals(status))
                .findFirst()
                .orElse(null);
    }

}
