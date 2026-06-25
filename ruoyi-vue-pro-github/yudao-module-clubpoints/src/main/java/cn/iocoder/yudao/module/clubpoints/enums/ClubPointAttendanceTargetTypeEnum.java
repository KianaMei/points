package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 签到签退目标类型枚举
 */
public enum ClubPointAttendanceTargetTypeEnum {

    CHECK_IN(1, "签到"),
    CHECK_OUT(2, "签退");

    private final Integer targetType;
    private final String name;

    ClubPointAttendanceTargetTypeEnum(Integer targetType, String name) {
        this.targetType = targetType;
        this.name = name;
    }

    public Integer getTargetType() {
        return targetType;
    }

    public String getName() {
        return name;
    }

}
