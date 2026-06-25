package cn.iocoder.yudao.module.clubpoints.service.rule.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 积分规则版本发布、停用操作参数
 */
@Data
@Accessors(chain = true)
public class ClubPointRuleOperationReqBO {

    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;

}
