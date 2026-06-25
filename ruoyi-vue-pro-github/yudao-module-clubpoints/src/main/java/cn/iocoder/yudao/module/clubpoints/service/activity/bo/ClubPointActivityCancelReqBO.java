package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubOperationReq;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 活动取消参数
 */
@Data
@Accessors(chain = true)
public class ClubPointActivityCancelReqBO implements ClubPointClubOperationReq {

    private Long id;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;

}
