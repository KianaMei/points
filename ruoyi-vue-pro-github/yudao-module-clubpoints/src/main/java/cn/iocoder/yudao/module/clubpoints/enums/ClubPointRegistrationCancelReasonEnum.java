package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 活动报名取消原因枚举
 */
public enum ClubPointRegistrationCancelReasonEnum {

    SELF_CANCEL(1, "员工自助取消"),
    EXIT_CLUB(2, "退出俱乐部自动取消"),
    ADMIN_REMOVE(3, "管理员移除成员"),
    ACTIVITY_CANCEL(4, "活动取消");

    private final Integer reasonType;
    private final String name;

    ClubPointRegistrationCancelReasonEnum(Integer reasonType, String name) {
        this.reasonType = reasonType;
        this.name = name;
    }

    public Integer getReasonType() {
        return reasonType;
    }

    public String getName() {
        return name;
    }

}
