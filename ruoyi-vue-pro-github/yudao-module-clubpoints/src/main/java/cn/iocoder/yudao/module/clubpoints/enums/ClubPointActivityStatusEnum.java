package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 活动状态枚举
 */
public enum ClubPointActivityStatusEnum implements ArrayValuable<Integer> {

    DRAFT(1, "草稿", 2, 4, 8),
    PENDING_REVIEW(2, "待审核", 3, 4, 8),
    REJECTED(3, "已驳回", 2, 8),
    PUBLISHED(4, "已发布", 5, 6, 8),
    CANCELED(5, "已取消", 8),
    ENDED(6, "已结束", 7, 8),
    SETTLED(7, "已发放", 8),
    DELETED_SNAPSHOT(8, "已删除快照");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointActivityStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;
    private final Set<Integer> nextStatuses;

    ClubPointActivityStatusEnum(Integer status, String name, Integer... nextStatuses) {
        this.status = status;
        this.name = name;
        this.nextStatuses = new HashSet<>(Arrays.asList(nextStatuses));
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public boolean canTransitionTo(ClubPointActivityStatusEnum targetStatus) {
        return targetStatus != null && nextStatuses.contains(targetStatus.getStatus());
    }

    public boolean canRegister() {
        return this == PUBLISHED;
    }

    public boolean canCheckAttendance() {
        return this == PUBLISHED;
    }

    public boolean canUpdateKeyFields() {
        return this == DRAFT || this == PENDING_REVIEW || this == REJECTED || this == PUBLISHED;
    }

    public static ClubPointActivityStatusEnum of(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
