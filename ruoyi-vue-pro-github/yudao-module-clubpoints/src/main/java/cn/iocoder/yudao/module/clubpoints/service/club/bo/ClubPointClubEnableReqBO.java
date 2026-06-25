package cn.iocoder.yudao.module.clubpoints.service.club.bo;

import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubOperationReq;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 俱乐部启用参数
 */
@Data
@Accessors(chain = true)
public class ClubPointClubEnableReqBO implements ClubPointClubOperationReq {

    private Long id;
    private String reason;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;

}
