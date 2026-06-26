package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 非签到积分材料状态枚举
 */
public enum ClubPointContributionMaterialStatusEnum {

    DRAFT(1, "草稿"),
    PENDING_REVIEW(2, "待审核"),
    WITHDRAWN(3, "已撤回"),
    REJECTED(4, "已驳回"),
    APPROVED(5, "已通过"),
    DELETED_SNAPSHOT(6, "已删除快照");

    private final Integer status;
    private final String name;

    ClubPointContributionMaterialStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static ClubPointContributionMaterialStatusEnum of(Integer status) {
        return Arrays.stream(values())
                .filter(value -> value.status.equals(status))
                .findFirst()
                .orElse(null);
    }

    public boolean canTransitionTo(ClubPointContributionMaterialStatusEnum target) {
        if (target == null || this == target || this == DELETED_SNAPSHOT) {
            return false;
        }
        switch (this) {
            case DRAFT:
                return target == PENDING_REVIEW || target == DELETED_SNAPSHOT;
            case PENDING_REVIEW:
                return target == WITHDRAWN || target == REJECTED
                        || target == APPROVED || target == DELETED_SNAPSHOT;
            case WITHDRAWN:
            case REJECTED:
                return target == PENDING_REVIEW || target == DELETED_SNAPSHOT;
            case APPROVED:
                return target == DELETED_SNAPSHOT;
            default:
                return false;
        }
    }

    public boolean canEditContent() {
        return this == DRAFT || this == WITHDRAWN || this == REJECTED;
    }

    public boolean canReview() {
        return this == PENDING_REVIEW;
    }

    public boolean canWithdraw() {
        return this == PENDING_REVIEW;
    }

}
