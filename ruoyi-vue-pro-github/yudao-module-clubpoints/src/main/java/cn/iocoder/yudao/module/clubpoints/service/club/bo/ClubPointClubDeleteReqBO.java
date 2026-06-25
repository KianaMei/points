package cn.iocoder.yudao.module.clubpoints.service.club.bo;

import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubOperationReq;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 俱乐部物理删除参数
 */
@Data
@Accessors(chain = true)
public class ClubPointClubDeleteReqBO implements ClubPointClubOperationReq {

    private Long id;
    private ClubStrongConfirmReqBO strongConfirm;
    private String reason;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;

}
