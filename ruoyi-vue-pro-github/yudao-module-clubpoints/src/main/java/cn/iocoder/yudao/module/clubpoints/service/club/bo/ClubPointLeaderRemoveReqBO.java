package cn.iocoder.yudao.module.clubpoints.service.club.bo;

import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubOperationReq;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 移除俱乐部负责人参数
 */
@Data
@Accessors(chain = true)
public class ClubPointLeaderRemoveReqBO implements ClubPointClubOperationReq {

    private Long clubId;
    private Long userId;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;

}
