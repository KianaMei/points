package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 签到签退来源类型枚举
 */
public enum ClubPointAttendanceSourceTypeEnum {

    SELF(1, "自助"),
    SUPPLEMENT(2, "补录"),
    CORRECTION(3, "修正");

    private final Integer sourceType;
    private final String name;

    ClubPointAttendanceSourceTypeEnum(Integer sourceType, String name) {
        this.sourceType = sourceType;
        this.name = name;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public String getName() {
        return name;
    }

}
