package cn.iocoder.yudao.module.clubpoints.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 预算分类
 */
@Getter
@AllArgsConstructor
public enum ClubPointBudgetCategoryEnum {

    ACTIVITY(1, "活动经费"),
    INCENTIVE(2, "专项激励"),
    REDEMPTION_GIFT(3, "积分兑换奖品"),
    OTHER(4, "其他");

    private final Integer category;
    private final String name;

    public static boolean isValid(Integer category) {
        return Arrays.stream(values()).anyMatch(item -> item.getCategory().equals(category));
    }

}
