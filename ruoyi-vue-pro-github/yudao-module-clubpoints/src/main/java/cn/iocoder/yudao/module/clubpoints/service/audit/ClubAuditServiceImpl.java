package cn.iocoder.yudao.module.clubpoints.service.audit;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;

/**
 * 俱乐部积分强审计服务实现
 */
@Service
public class ClubAuditServiceImpl implements ClubAuditService {

    @Resource
    private ClubAuditLogMapper clubAuditLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAuditLog(ClubAuditCreateReqBO reqBO) {
        validateCreateReq(reqBO);
        ClubAuditLogDO auditLog = buildAuditLog(reqBO);
        try {
            int inserted = clubAuditLogMapper.insert(auditLog);
            if (inserted != 1 || auditLog.getId() == null) {
                throw exception(CLUB_AUDIT_WRITE_FAILED);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw exception(CLUB_AUDIT_WRITE_FAILED);
        }
        return auditLog.getId();
    }

    private static void validateCreateReq(ClubAuditCreateReqBO reqBO) {
        if (reqBO == null || !StringUtils.hasText(reqBO.getActionType())
                || !StringUtils.hasText(reqBO.getBizType())
                || reqBO.getOperatorUserId() == null
                || !StringUtils.hasText(reqBO.getOperatorNameSnapshot())
                || reqBO.getSuccess() == null) {
            throw exception(CLUB_AUDIT_WRITE_FAILED);
        }
    }

    private static ClubAuditLogDO buildAuditLog(ClubAuditCreateReqBO reqBO) {
        return new ClubAuditLogDO()
                .setActionType(reqBO.getActionType())
                .setBizType(reqBO.getBizType())
                .setBizId(reqBO.getBizId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(reqBO.getOperationTime() != null ? reqBO.getOperationTime() : LocalDateTime.now())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(reqBO.getBeforeJson())
                .setAfterJson(reqBO.getAfterJson())
                .setTargetSnapshotJson(reqBO.getTargetSnapshotJson())
                .setSuccess(reqBO.getSuccess())
                .setErrorMessage(reqBO.getErrorMessage());
    }

}
