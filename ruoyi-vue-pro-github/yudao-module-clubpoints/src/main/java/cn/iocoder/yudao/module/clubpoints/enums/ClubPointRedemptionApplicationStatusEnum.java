package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 兑换申请状态枚举
 */
public enum ClubPointRedemptionApplicationStatusEnum {

    PENDING_REVIEW(1, "待审核"),
    CANCELED_BEFORE_REVIEW(2, "审核前取消"),
    APPROVED_AND_ISSUED(3, "已通过并直接发放"),
    REJECTED(4, "已拒绝");

    private final Integer status;
    private final String name;

    ClubPointRedemptionApplicationStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static ClubPointRedemptionApplicationStatusEnum of(Integer status) {
        return Arrays.stream(values())
                .filter(value -> value.status.equals(status))
                .findFirst()
                .orElse(null);
    }

}
