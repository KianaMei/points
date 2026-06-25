package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 活动报名状态枚举
 */
public enum ClubPointRegistrationStatusEnum {

    REGISTERED(1, "已报名"),
    CANCELED(2, "已取消");

    private final Integer status;
    private final String name;

    ClubPointRegistrationStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

}
