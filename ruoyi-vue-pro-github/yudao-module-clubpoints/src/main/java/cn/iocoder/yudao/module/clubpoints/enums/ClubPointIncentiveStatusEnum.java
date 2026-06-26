package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 运营激励状态枚举
 */
public enum ClubPointIncentiveStatusEnum {

    SUGGESTED(1, "建议"),
    CONFIRMED(2, "已确认"),
    CANCELED(3, "已取消");

    private final Integer status;
    private final String name;

    ClubPointIncentiveStatusEnum(Integer status, String name) {
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
