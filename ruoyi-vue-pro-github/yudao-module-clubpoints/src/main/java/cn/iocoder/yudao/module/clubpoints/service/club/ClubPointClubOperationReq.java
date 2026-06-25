package cn.iocoder.yudao.module.clubpoints.service.club;

/**
 * 俱乐部操作审计参数
 */
public interface ClubPointClubOperationReq {

    Long getOperatorUserId();

    String getOperatorNameSnapshot();

    String getOperatorRoleSnapshot();

    String getClientIp();

    String getUserAgent();

    String getReason();

}
