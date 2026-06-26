package cn.iocoder.yudao.module.clubpoints.service.redemption.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 兑换审核请求
 */
@Data
@Accessors(chain = true)
public class ClubPointRedemptionReviewReqBO {

    private Long id;
    private Integer result;
    private String reason;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;

}
