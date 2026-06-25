package cn.iocoder.yudao.module.clubpoints.service.audit;

import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;

/**
 * 俱乐部积分强审计服务
 */
public interface ClubAuditService {

    Long createAuditLog(ClubAuditCreateReqBO reqBO);

}
