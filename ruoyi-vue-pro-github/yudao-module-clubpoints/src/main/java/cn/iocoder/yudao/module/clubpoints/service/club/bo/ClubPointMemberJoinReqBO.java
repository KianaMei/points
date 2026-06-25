package cn.iocoder.yudao.module.clubpoints.service.club.bo;

import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubOperationReq;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 员工加入俱乐部参数
 */
@Data
@Accessors(chain = true)
public class ClubPointMemberJoinReqBO implements ClubPointClubOperationReq {

    private Long clubId;
    private Long userId;
    private Long deptIdSnapshot;
    private String userNameSnapshot;
    private String deptNameSnapshot;
    private String mobileSnapshot;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;

}
