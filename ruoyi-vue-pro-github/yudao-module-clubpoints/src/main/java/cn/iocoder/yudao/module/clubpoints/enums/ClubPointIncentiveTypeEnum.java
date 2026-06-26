package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 运营激励类型枚举
 */
public enum ClubPointIncentiveTypeEnum {

    RANKING(1, "俱乐部排名"),
    INNOVATION(2, "特色活动创新奖"),
    PERSONAL_HONOR(3, "个人荣誉");

    private final Integer type;
    private final String name;

    ClubPointIncentiveTypeEnum(Integer type, String name) {
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
