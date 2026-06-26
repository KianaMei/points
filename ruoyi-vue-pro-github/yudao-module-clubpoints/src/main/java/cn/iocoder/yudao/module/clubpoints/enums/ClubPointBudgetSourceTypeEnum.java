package cn.iocoder.yudao.module.clubpoints.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 预算记录来源类型
 */
@Getter
@AllArgsConstructor
public enum ClubPointBudgetSourceTypeEnum {

    MANUAL(1, "手工登记"),
    RANKING_INCENTIVE(2, "排名激励"),
    INNOVATION_AWARD(3, "创新奖"),
    REDEMPTION(4, "积分兑换");

    private final Integer type;
    private final String name;

    public static boolean isValid(Integer type) {
        return Arrays.stream(values()).anyMatch(item -> item.getType().equals(type));
    }

}
