package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 运营激励来源类型枚举
 */
public enum ClubPointIncentiveSourceTypeEnum {

    ANNUAL_RANKING(1, "年度排名记录");

    private final Integer type;
    private final String name;

    ClubPointIncentiveSourceTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}
