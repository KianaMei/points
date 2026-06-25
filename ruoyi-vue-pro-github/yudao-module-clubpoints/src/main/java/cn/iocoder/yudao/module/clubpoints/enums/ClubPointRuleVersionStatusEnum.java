package cn.iocoder.yudao.module.clubpoints.enums;

/**
 * 积分规则版本状态枚举
 */
public enum ClubPointRuleVersionStatusEnum {

    DRAFT(1, "草稿"),
    PUBLISHED(2, "已发布"),
    WITHDRAWN(3, "已撤回"),
    DISABLED(4, "已停用");

    private final Integer status;
    private final String name;

    ClubPointRuleVersionStatusEnum(Integer status, String name) {
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
