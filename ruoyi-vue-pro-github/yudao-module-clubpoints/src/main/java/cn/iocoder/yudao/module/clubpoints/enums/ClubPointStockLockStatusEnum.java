package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 兑换库存锁状态枚举
 */
public enum ClubPointStockLockStatusEnum {

    LOCKED(1, "锁定中"),
    USED(2, "已使用"),
    RELEASED(3, "已释放");

    private final Integer status;
    private final String name;

    ClubPointStockLockStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static ClubPointStockLockStatusEnum of(Integer status) {
        return Arrays.stream(values())
                .filter(value -> value.status.equals(status))
                .findFirst()
                .orElse(null);
    }

}
