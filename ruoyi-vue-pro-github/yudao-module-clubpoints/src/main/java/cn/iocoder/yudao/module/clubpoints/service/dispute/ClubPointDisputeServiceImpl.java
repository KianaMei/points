package cn.iocoder.yudao.module.clubpoints.service.dispute;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.dispute.ClubPointDisputeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.dispute.ClubPointDisputeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeRelatedActionTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentService;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeAcceptReqBO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeSubmitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerService;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyService;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.BIZ_TYPE_DISPUTE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.DISPUTE_HANDLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_DISPUTE_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_DISPUTE_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_DISPUTE_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;

/**
 * 员工积分异议服务实现
 */
@Service
public class ClubPointDisputeServiceImpl implements ClubPointDisputeService {

    @Resource
    private ClubPointDisputeMapper disputeMapper;
    @Resource
    private ClubAttachmentService clubAttachmentService;
    @Resource
    private ClubAuditService clubAuditService;
    @Resource
    private ClubPointLedgerService ledgerService;
    @Resource
    private ClubNotifyService clubNotifyService;
    @Resource
    private ClubScopeService clubScopeService;
    @Resource
    private ClubPointTransactionMapper transactionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitDispute(ClubPointDisputeSubmitReqBO reqBO) {
        validateSubmitReq(reqBO);
        validateTargetOwnerIfKnown(reqBO);
        LocalDateTime submitTime = reqBO.getSubmitTime() == null ? LocalDateTime.now() : reqBO.getSubmitTime();
        ClubPointDisputeDO dispute = new ClubPointDisputeDO()
                .setUserId(reqBO.getUserId())
                .setTitle(reqBO.getTitle())
                .setContent(reqBO.getContent())
                .setTargetType(reqBO.getTargetType())
                .setTargetId(reqBO.getTargetId())
                .setStatus(ClubPointDisputeStatusEnum.PENDING.getStatus())
                .setSubmitTime(submitTime);
        disputeMapper.insert(dispute);
        bindAttachments(dispute.getId(), reqBO.getUserId(), reqBO.getAttachments());
        return dispute.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptDispute(ClubPointDisputeAcceptReqBO reqBO) {
        validateAcceptReq(reqBO);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointDisputeDO dispute = validatePendingDispute(reqBO.getId());
        dispute.setHandlerUserId(reqBO.getOperatorUserId())
                .setHandleTime(handleTime(reqBO.getHandleTime()));
        disputeMapper.updateById(dispute);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long handleDispute(ClubPointDisputeHandleReqBO reqBO) {
        return completeDispute(reqBO, ClubPointDisputeStatusEnum.REPLIED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectDispute(ClubPointDisputeHandleReqBO reqBO) {
        completeDispute(reqBO, ClubPointDisputeStatusEnum.CLOSED);
    }

    private Long completeDispute(ClubPointDisputeHandleReqBO reqBO, ClubPointDisputeStatusEnum targetStatus) {
        validateHandleReq(reqBO);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointDisputeDO dispute = validatePendingDispute(reqBO.getId());
        String beforeJson = snapshot(dispute);
        ClubPointDisputeRelatedActionTypeEnum actionType = validateRelatedAction(reqBO, targetStatus);
        Long relatedTransactionId = executeRelatedAction(reqBO, actionType);
        LocalDateTime handleTime = handleTime(reqBO.getHandleTime());

        dispute.setHandlerUserId(reqBO.getOperatorUserId())
                .setHandleTime(handleTime)
                .setReplyContent(reqBO.getReplyContent())
                .setRelatedActionType(actionType.getType())
                .setRelatedTransactionId(relatedTransactionId)
                .setStatus(targetStatus.getStatus());
        if (targetStatus == ClubPointDisputeStatusEnum.CLOSED) {
            dispute.setCloseTime(handleTime);
        }
        Long auditLogId = createHandleAudit(reqBO, dispute, beforeJson, snapshot(dispute), handleTime);
        dispute.setAuditLogId(auditLogId);
        disputeMapper.updateById(dispute);
        notifyDisputeReplied(dispute);
        return relatedTransactionId;
    }

    private Long executeRelatedAction(ClubPointDisputeHandleReqBO reqBO,
                                      ClubPointDisputeRelatedActionTypeEnum actionType) {
        if (actionType == ClubPointDisputeRelatedActionTypeEnum.NO_ACTION) {
            return null;
        }
        if (actionType == ClubPointDisputeRelatedActionTypeEnum.ADJUSTMENT) {
            return ledgerService.adjustPoints(reqBO.getAdjustReqBO());
        }
        if (actionType == ClubPointDisputeRelatedActionTypeEnum.REVERSE) {
            return ledgerService.reverseTransaction(reqBO.getReverseReqBO());
        }
        throw exception(CLUB_DISPUTE_INVALID);
    }

    private Long createHandleAudit(ClubPointDisputeHandleReqBO reqBO, ClubPointDisputeDO dispute,
                                   String beforeJson, String afterJson, LocalDateTime handleTime) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(DISPUTE_HANDLE)
                .setBizType(BIZ_TYPE_DISPUTE)
                .setBizId(dispute.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(handleTime)
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(afterJson)
                .setSuccess(true));
    }

    private void notifyDisputeReplied(ClubPointDisputeDO dispute) {
        try {
            clubNotifyService.notifyDisputeReplied(dispute.getUserId(), dispute.getTitle(), dispute.getReplyContent());
        } catch (Exception ignored) {
            // 通知是告知链路，失败不能回滚异议处理业务。
        }
    }

    private void bindAttachments(Long disputeId, Long uploadedBy, List<ClubAttachmentBindReqBO> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        for (ClubAttachmentBindReqBO attachment : attachments) {
            clubAttachmentService.bindAttachment(attachment
                    .setBizType(BIZ_TYPE_DISPUTE)
                    .setBizId(disputeId)
                    .setUploadedBy(uploadedBy)
                    .setAdminAppend(false));
        }
    }

    private ClubPointDisputeDO validatePendingDispute(Long disputeId) {
        ClubPointDisputeDO dispute = disputeMapper.selectByIdForUpdate(disputeId);
        if (dispute == null) {
            throw exception(CLUB_DISPUTE_NOT_EXISTS);
        }
        if (!ClubPointDisputeStatusEnum.PENDING.getStatus().equals(dispute.getStatus())) {
            throw exception(CLUB_DISPUTE_STATUS_INVALID);
        }
        return dispute;
    }

    private void validateTargetOwnerIfKnown(ClubPointDisputeSubmitReqBO reqBO) {
        if (!ClubPointDisputeTargetTypeEnum.TRANSACTION.getType().equals(reqBO.getTargetType())
                || reqBO.getTargetId() == null) {
            return;
        }
        ClubPointTransactionDO transaction = transactionMapper.selectById(reqBO.getTargetId());
        if (transaction != null && !reqBO.getUserId().equals(transaction.getUserId())) {
            throw exception(CLUB_SCOPE_DENIED);
        }
    }

    private static void validateSubmitReq(ClubPointDisputeSubmitReqBO reqBO) {
        if (reqBO == null || reqBO.getUserId() == null || !StringUtils.hasText(reqBO.getTitle())
                || !StringUtils.hasText(reqBO.getContent()) || reqBO.getTargetType() == null
                || reqBO.getTargetId() == null || ClubPointDisputeTargetTypeEnum.of(reqBO.getTargetType()) == null) {
            throw exception(CLUB_DISPUTE_INVALID);
        }
    }

    private static void validateAcceptReq(ClubPointDisputeAcceptReqBO reqBO) {
        if (reqBO == null || reqBO.getId() == null || reqBO.getOperatorUserId() == null) {
            throw exception(CLUB_DISPUTE_INVALID);
        }
    }

    private static void validateHandleReq(ClubPointDisputeHandleReqBO reqBO) {
        if (reqBO == null || reqBO.getId() == null || reqBO.getOperatorUserId() == null
                || !StringUtils.hasText(reqBO.getReplyContent())) {
            throw exception(CLUB_DISPUTE_INVALID);
        }
    }

    private static ClubPointDisputeRelatedActionTypeEnum validateRelatedAction(
            ClubPointDisputeHandleReqBO reqBO, ClubPointDisputeStatusEnum targetStatus) {
        Integer relatedActionType = reqBO.getRelatedActionType() == null
                ? ClubPointDisputeRelatedActionTypeEnum.NO_ACTION.getType() : reqBO.getRelatedActionType();
        ClubPointDisputeRelatedActionTypeEnum actionType = ClubPointDisputeRelatedActionTypeEnum.of(relatedActionType);
        if (actionType == null) {
            throw exception(CLUB_DISPUTE_INVALID);
        }
        if (targetStatus == ClubPointDisputeStatusEnum.CLOSED
                && actionType != ClubPointDisputeRelatedActionTypeEnum.NO_ACTION) {
            throw exception(CLUB_DISPUTE_INVALID);
        }
        if (actionType == ClubPointDisputeRelatedActionTypeEnum.ADJUSTMENT && reqBO.getAdjustReqBO() == null) {
            throw exception(CLUB_DISPUTE_INVALID);
        }
        if (actionType == ClubPointDisputeRelatedActionTypeEnum.REVERSE && reqBO.getReverseReqBO() == null) {
            throw exception(CLUB_DISPUTE_INVALID);
        }
        return actionType;
    }

    private static LocalDateTime handleTime(LocalDateTime handleTime) {
        return handleTime == null ? LocalDateTime.now() : handleTime;
    }

    private static String snapshot(ClubPointDisputeDO dispute) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", dispute.getId());
        snapshot.put("userId", dispute.getUserId());
        snapshot.put("title", dispute.getTitle());
        snapshot.put("targetType", dispute.getTargetType());
        snapshot.put("targetId", dispute.getTargetId());
        snapshot.put("status", dispute.getStatus());
        snapshot.put("submitTime", dispute.getSubmitTime());
        snapshot.put("handlerUserId", dispute.getHandlerUserId());
        snapshot.put("handleTime", dispute.getHandleTime());
        snapshot.put("replyContent", dispute.getReplyContent());
        snapshot.put("relatedActionType", dispute.getRelatedActionType());
        snapshot.put("relatedTransactionId", dispute.getRelatedTransactionId());
        snapshot.put("closeTime", dispute.getCloseTime());
        return JsonUtils.toJsonString(snapshot);
    }

}
