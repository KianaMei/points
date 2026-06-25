package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubOperationReq;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 活动提交审核参数
 */
@Data
@Accessors(chain = true)
public class ClubPointActivitySubmitReqBO implements ClubPointClubOperationReq {

    private Long id;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;

}
