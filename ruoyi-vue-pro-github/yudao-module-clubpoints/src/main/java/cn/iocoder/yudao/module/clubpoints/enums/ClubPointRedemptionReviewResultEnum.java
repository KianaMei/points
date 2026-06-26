package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 兑换审核结果枚举
 */
public enum ClubPointRedemptionReviewResultEnum {

    APPROVED(1, "通过"),
    REJECTED(2, "拒绝");

    private final Integer result;
    private final String name;

    ClubPointRedemptionReviewResultEnum(Integer result, String name) {
        this.result = result;
        this.name = name;
    }

    public Integer getResult() {
        return result;
    }

    public String getName() {
        return name;
    }

    public static ClubPointRedemptionReviewResultEnum of(Integer result) {
        return Arrays.stream(values())
                .filter(value -> value.result.equals(result))
                .findFirst()
                .orElse(null);
    }

}
