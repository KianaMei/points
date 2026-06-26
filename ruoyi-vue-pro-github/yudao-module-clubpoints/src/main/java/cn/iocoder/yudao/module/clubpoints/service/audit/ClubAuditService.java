package cn.iocoder.yudao.module.clubpoints.service.audit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditPageReqBO;

/**
 * 俱乐部积分强审计服务
 */
public interface ClubAuditService {

    Long createAuditLog(ClubAuditCreateReqBO reqBO);

    default PageResult<ClubAuditLogDO> getAuditPage(ClubAuditPageReqBO reqBO) {
        throw new UnsupportedOperationException("Audit page query is not implemented");
    }

}
