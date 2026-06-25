package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 签到签退修正类型枚举
 */
public enum ClubPointAttendanceCorrectionTypeEnum {

    SUPPLEMENT(1, "补录"),
    CORRECTION(2, "修正");

    private final Integer correctionType;
    private final String name;

    ClubPointAttendanceCorrectionTypeEnum(Integer correctionType, String name) {
        this.correctionType = correctionType;
        this.name = name;
    }

    public Integer getCorrectionType() {
        return correctionType;
    }

    public String getName() {
        return name;
    }

}
