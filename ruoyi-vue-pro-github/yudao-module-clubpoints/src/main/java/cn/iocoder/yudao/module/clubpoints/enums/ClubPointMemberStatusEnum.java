package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 俱乐部成员状态枚举
 */
public enum ClubPointMemberStatusEnum implements ArrayValuable<Integer> {

    ACTIVE(1, "有效"),
    SELF_EXITED(2, "自主退出"),
    ADMIN_REMOVED(3, "管理员移除");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointMemberStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    ClubPointMemberStatusEnum(Integer status, String name) {
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
