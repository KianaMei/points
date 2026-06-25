package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 俱乐部状态枚举
 */
public enum ClubPointClubStatusEnum implements ArrayValuable<Integer> {

    ENABLED(1, "启用"),
    DISABLED(2, "停用"),
    DELETED_SNAPSHOT(3, "已删除快照");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointClubStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    ClubPointClubStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
