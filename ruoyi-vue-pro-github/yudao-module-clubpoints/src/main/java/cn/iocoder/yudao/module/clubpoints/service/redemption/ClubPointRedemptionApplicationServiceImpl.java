package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointStockLockDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointFreezeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionEligibilitySnapshotMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionGiftMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionReviewRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointStockLockMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionBatchStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionGiftStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionReviewResultEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointStockLockStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointFreezeService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeConvertReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeReleaseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionApplyReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionTimeoutReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.REDEMPTION_REVIEW;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_CLOSED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_APPLICATION_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_APPLICATION_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_STATUS_INVALID;

/**
 * 兑换申请服务实现
 */
@Service
public class ClubPointRedemptionApplicationServiceImpl implements ClubPointRedemptionApplicationService {

    private static final Integer APPLY_QUANTITY = 1;
    private static final String APPLICATION_NO_PREFIX = "RDA-";
    private static final String FREEZE_NO_PREFIX = "RDF-";
    private static final String APPLICATION_IDEMPOTENCY_PREFIX = "REDEMPTION_APPLY:";
    private static final String STOCK_LOCK_IDEMPOTENCY_PREFIX = "STOCK_LOCK:";
    private static final String BIZ_TYPE_REDEMPTION_APPLICATION = "REDEMPTION_APPLICATION";
    private static final String REDEMPTION_APPROVE_PREFIX = "REDEMPTION_APPROVE:";
    private static final String DEFAULT_CANCEL_REASON = "员工取消兑换申请";
    private static final String DEFAULT_TIMEOUT_REASON = "审核超时自动取消";

    @Resource
    private ClubPointRedemptionEligibilityService eligibilityService;
    @Resource
    private ClubPointRedemptionGiftService giftService;
    @Resource
    private ClubPointFreezeService freezeService;
    @Resource
    private ClubAuditService clubAuditService;
    @Resource
    private ClubNotifyService clubNotifyService;
    @Resource
    private ClubScopeService clubScopeService;
    @Resource
    private ClubPointRedemptionBatchMapper batchMapper;
    @Resource
    private ClubPointRedemptionGiftMapper giftMapper;
    @Resource
    private ClubPointRedemptionEligibilitySnapshotMapper eligibilitySnapshotMapper;
    @Resource
    private ClubPointRedemptionApplicationMapper applicationMapper;
    @Resource
    private ClubPointStockLockMapper stockLockMapper;
    @Resource
    private ClubPointRedemptionReviewRecordMapper reviewRecordMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointFreezeMapper freezeMapper;

    @Override
    public List<ClubPointRedemptionGiftDO> listAvailableGifts(Long batchId, Long userId) {
        validateBatchOpenForApply(batchId);
        eligibilityService.validateUserQualifiedForApply(batchId, userId);
        return giftMapper.selectListByBatchIdAndStatus(batchId, ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(ClubPointRedemptionApplyReqBO reqBO) {
        validateApplyReq(reqBO);
        String idempotencyKey = buildApplicationIdempotencyKey(reqBO);
        ClubPointRedemptionApplicationDO existing = applicationMapper.selectByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return existing.getId();
        }

        ClubPointRedemptionBatchDO batch = validateBatchOpenForApply(reqBO.getBatchId());
        ClubPointRedemptionEligibilitySnapshotDO eligibilitySnapshot =
                eligibilityService.validateUserQualifiedForApply(reqBO.getBatchId(), reqBO.getUserId());
        ClubPointRedemptionGiftDO gift = validateGiftForApply(reqBO);
        ClubPointAccountDO beforeAccount = accountMapper.selectByUserId(reqBO.getUserId());

        ClubPointRedemptionApplicationDO application =
                buildPendingApplication(reqBO, idempotencyKey, batch, gift, eligibilitySnapshot, beforeAccount);
        try {
            applicationMapper.insert(application);
        } catch (DuplicateKeyException ex) {
            ClubPointRedemptionApplicationDO duplicated = applicationMapper.selectByIdempotencyKey(idempotencyKey);
            if (duplicated != null) {
                return duplicated.getId();
            }
            throw ex;
        }

        Long freezeId = freezeService.freezePoints(buildFreezeReq(reqBO, gift, application.getId()));
        giftService.lockStock(gift.getId(), APPLY_QUANTITY);
        Long stockLockId = createStockLock(reqBO, gift, application.getId());
        application.setFreezeId(freezeId)
                .setStockLockId(stockLockId);
        applicationMapper.updateById(application);
        return application.getId();
    }

    @Override
    public List<ClubPointRedemptionApplicationDO> listPendingReviewApplications(boolean operatorGlobalScope) {
        clubScopeService.validateGlobal(operatorGlobalScope);
        return applicationMapper.selectListByStatus(ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(ClubPointRedemptionReviewReqBO reqBO) {
        ClubPointRedemptionReviewResultEnum result = validateReviewReq(reqBO);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointRedemptionApplicationDO application = validateApplicationExistsForReview(reqBO.getId());
        if (handleReviewedIdempotently(application, result)) {
            return;
        }
        validateReviewable(application);
        ClubPointStockLockDO stockLock = validateStockLockForReview(application);
        LocalDateTime reviewTime = LocalDateTime.now();
        String beforeJson = applicationSnapshot(application);
        Long auditLogId = createReviewAudit(reqBO, application, result, beforeJson, reviewTime);

        if (result == ClubPointRedemptionReviewResultEnum.APPROVED) {
            approveApplication(application, stockLock, reqBO, reviewTime, auditLogId);
        } else {
            rejectApplication(application, stockLock, reqBO, reviewTime);
        }
        ClubPointFreezeDO freeze = freezeMapper.selectById(application.getFreezeId());
        ClubPointStockLockDO reviewedStockLock = stockLockMapper.selectById(stockLock.getId());
        applicationMapper.updateById(application);
        insertReviewRecord(application, reqBO, result, reviewTime, freeze, reviewedStockLock, auditLogId);
        notifyReviewResult(application, result, reqBO.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOwnApplication(ClubPointRedemptionCancelReqBO reqBO) {
        validateCancelReq(reqBO);
        ClubPointRedemptionApplicationDO application = validateApplicationExistsForReview(reqBO.getId());
        clubScopeService.validateSelf(reqBO.getUserId(), application.getUserId());
        if (ClubPointRedemptionApplicationStatusEnum.CANCELED_BEFORE_REVIEW.getStatus()
                .equals(application.getStatus())) {
            return;
        }
        cancelPendingApplication(application, cancelReason(reqBO.getReason(), DEFAULT_CANCEL_REASON),
                reqBO.getCancelTime() == null ? LocalDateTime.now() : reqBO.getCancelTime());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int timeoutPendingApplications(ClubPointRedemptionTimeoutReqBO reqBO) {
        validateTimeoutReq(reqBO);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        String reason = cancelReason(reqBO.getReason(), DEFAULT_TIMEOUT_REASON);
        LocalDateTime timeoutTime = reqBO.getTimeoutTime() == null ? LocalDateTime.now() : reqBO.getTimeoutTime();
        int handled = 0;
        for (ClubPointRedemptionApplicationDO pendingApplication :
                applicationMapper.selectListByStatusAppliedBefore(
                        ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus(), reqBO.getAppliedBefore())) {
            ClubPointRedemptionApplicationDO application =
                    applicationMapper.selectByIdForUpdate(pendingApplication.getId());
            if (application == null || !ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus()
                    .equals(application.getStatus())) {
                continue;
            }
            cancelPendingApplication(application, reason, timeoutTime);
            handled++;
        }
        return handled;
    }

    private void approveApplication(ClubPointRedemptionApplicationDO application, ClubPointStockLockDO stockLock,
                                    ClubPointRedemptionReviewReqBO reqBO, LocalDateTime reviewTime,
                                    Long auditLogId) {
        Long transactionId = freezeService.convertFreezeToDeduction(
                buildFreezeConvertReq(application, reqBO, reviewTime, auditLogId));
        giftService.useLockedStock(application.getGiftId(), application.getQuantity());
        stockLock.setStatus(ClubPointStockLockStatusEnum.USED.getStatus())
                .setUsedTime(reviewTime);
        stockLockMapper.updateById(stockLock);
        ClubPointAccountDO afterAccount = accountMapper.selectByUserId(application.getUserId());
        applyReviewedApplication(application, reqBO, reviewTime, afterAccount)
                .setStatus(ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus())
                .setDeductTransactionId(transactionId)
                .setDirectIssueTime(reviewTime);
    }

    private void rejectApplication(ClubPointRedemptionApplicationDO application, ClubPointStockLockDO stockLock,
                                   ClubPointRedemptionReviewReqBO reqBO, LocalDateTime reviewTime) {
        freezeService.releaseFreeze(new ClubPointFreezeReleaseReqBO()
                .setFreezeId(application.getFreezeId())
                .setReleasedAt(reviewTime)
                .setReleaseReason(reqBO.getReason()));
        giftService.releaseLockedStock(application.getGiftId(), application.getQuantity());
        stockLock.setStatus(ClubPointStockLockStatusEnum.RELEASED.getStatus())
                .setReleasedTime(reviewTime)
                .setReleaseReason(reqBO.getReason());
        stockLockMapper.updateById(stockLock);
        ClubPointAccountDO afterAccount = accountMapper.selectByUserId(application.getUserId());
        applyReviewedApplication(application, reqBO, reviewTime, afterAccount)
                .setStatus(ClubPointRedemptionApplicationStatusEnum.REJECTED.getStatus());
    }

    private void cancelPendingApplication(ClubPointRedemptionApplicationDO application, String reason,
                                          LocalDateTime cancelTime) {
        validateReviewable(application);
        ClubPointStockLockDO stockLock = validateStockLockForReview(application);
        freezeService.releaseFreeze(new ClubPointFreezeReleaseReqBO()
                .setFreezeId(application.getFreezeId())
                .setReleasedAt(cancelTime)
                .setReleaseReason(reason));
        giftService.releaseLockedStock(application.getGiftId(), application.getQuantity());
        stockLock.setStatus(ClubPointStockLockStatusEnum.RELEASED.getStatus())
                .setReleasedTime(cancelTime)
                .setReleaseReason(reason);
        stockLockMapper.updateById(stockLock);
        ClubPointAccountDO afterAccount = accountMapper.selectByUserId(application.getUserId());
        applyCanceledApplication(application, reason, cancelTime, afterAccount);
        applicationMapper.updateById(application);
    }

    private ClubPointRedemptionBatchDO validateBatchOpenForApply(Long batchId) {
        ClubPointRedemptionBatchDO batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw exception(CLUB_REDEMPTION_BATCH_NOT_EXISTS);
        }
        if (!ClubPointRedemptionBatchStatusEnum.OPENED.getStatus().equals(batch.getStatus())) {
            throw exception(CLUB_REDEMPTION_BATCH_CLOSED);
        }
        return batch;
    }

    private ClubPointRedemptionGiftDO validateGiftForApply(ClubPointRedemptionApplyReqBO reqBO) {
        ClubPointRedemptionGiftDO gift = giftMapper.selectById(reqBO.getGiftId());
        if (gift == null || !reqBO.getBatchId().equals(gift.getBatchId())) {
            throw exception(CLUB_REDEMPTION_GIFT_NOT_EXISTS);
        }
        if (!ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus().equals(gift.getStatus())) {
            throw exception(CLUB_REDEMPTION_GIFT_STATUS_INVALID);
        }
        return gift;
    }

    private ClubPointRedemptionApplicationDO validateApplicationExistsForReview(Long applicationId) {
        ClubPointRedemptionApplicationDO application = applicationMapper.selectByIdForUpdate(applicationId);
        if (application == null) {
            throw exception(CLUB_REDEMPTION_APPLICATION_NOT_EXISTS);
        }
        return application;
    }

    private static void validateReviewable(ClubPointRedemptionApplicationDO application) {
        if (!ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus().equals(application.getStatus())
                || application.getFreezeId() == null || application.getStockLockId() == null) {
            throw exception(CLUB_REDEMPTION_APPLICATION_STATUS_INVALID);
        }
    }

    private ClubPointStockLockDO validateStockLockForReview(ClubPointRedemptionApplicationDO application) {
        ClubPointStockLockDO stockLock = stockLockMapper.selectByApplicationIdForUpdate(application.getId());
        if (stockLock == null || !application.getStockLockId().equals(stockLock.getId())
                || !ClubPointStockLockStatusEnum.LOCKED.getStatus().equals(stockLock.getStatus())) {
            throw exception(CLUB_REDEMPTION_APPLICATION_STATUS_INVALID);
        }
        return stockLock;
    }

    private static boolean handleReviewedIdempotently(ClubPointRedemptionApplicationDO application,
                                                      ClubPointRedemptionReviewResultEnum result) {
        if (result == ClubPointRedemptionReviewResultEnum.APPROVED
                && ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus()
                .equals(application.getStatus())) {
            return true;
        }
        if (result == ClubPointRedemptionReviewResultEnum.REJECTED
                && ClubPointRedemptionApplicationStatusEnum.REJECTED.getStatus().equals(application.getStatus())) {
            return true;
        }
        if (!ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus().equals(application.getStatus())) {
            throw exception(CLUB_REDEMPTION_APPLICATION_STATUS_INVALID);
        }
        return false;
    }

    private Long createStockLock(ClubPointRedemptionApplyReqBO reqBO, ClubPointRedemptionGiftDO gift,
                                 Long applicationId) {
        ClubPointStockLockDO stockLock = new ClubPointStockLockDO()
                .setGiftId(gift.getId())
                .setApplicationId(applicationId)
                .setUserId(reqBO.getUserId())
                .setQuantity(APPLY_QUANTITY)
                .setStatus(ClubPointStockLockStatusEnum.LOCKED.getStatus())
                .setLockedTime(applyTime(reqBO))
                .setIdempotencyKey(STOCK_LOCK_IDEMPOTENCY_PREFIX + applicationId);
        stockLockMapper.insert(stockLock);
        return stockLock.getId();
    }

    private Long createReviewAudit(ClubPointRedemptionReviewReqBO reqBO,
                                   ClubPointRedemptionApplicationDO application,
                                   ClubPointRedemptionReviewResultEnum result,
                                   String beforeJson,
                                   LocalDateTime reviewTime) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(REDEMPTION_REVIEW)
                .setBizType(BIZ_TYPE_REDEMPTION_APPLICATION)
                .setBizId(application.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(reviewTime)
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(reviewAuditSnapshot(application, reqBO, result, reviewTime))
                .setTargetSnapshotJson(beforeJson)
                .setSuccess(true));
    }

    private void insertReviewRecord(ClubPointRedemptionApplicationDO application,
                                    ClubPointRedemptionReviewReqBO reqBO,
                                    ClubPointRedemptionReviewResultEnum result,
                                    LocalDateTime reviewTime,
                                    ClubPointFreezeDO freeze,
                                    ClubPointStockLockDO stockLock,
                                    Long auditLogId) {
        reviewRecordMapper.insert(new ClubPointRedemptionReviewRecordDO()
                .setApplicationId(application.getId())
                .setReviewerUserId(reqBO.getOperatorUserId())
                .setResult(result.getResult())
                .setReason(reqBO.getReason())
                .setReviewTime(reviewTime)
                .setApplicationSnapshotJson(applicationSnapshot(application))
                .setFreezeSnapshotJson(freezeSnapshot(freeze))
                .setStockSnapshotJson(stockSnapshot(stockLock))
                .setAuditLogId(auditLogId));
    }

    private void notifyReviewResult(ClubPointRedemptionApplicationDO application,
                                    ClubPointRedemptionReviewResultEnum result,
                                    String reason) {
        try {
            clubNotifyService.notifyRedemptionReviewResult(application.getUserId(), application.getApplicationNo(),
                    result.getName(), reason);
        } catch (Exception ignored) {
            // 通知是告知链路，失败不能回滚兑换审核业务。
        }
    }

    private ClubPointFreezeConvertReqBO buildFreezeConvertReq(ClubPointRedemptionApplicationDO application,
                                                              ClubPointRedemptionReviewReqBO reqBO,
                                                              LocalDateTime reviewTime,
                                                              Long auditLogId) {
        ClubPointRedemptionEligibilitySnapshotDO eligibilitySnapshot =
                eligibilitySnapshotMapper.selectById(application.getEligibilitySnapshotId());
        String transactionKey = REDEMPTION_APPROVE_PREFIX + application.getId();
        return new ClubPointFreezeConvertReqBO()
                .setFreezeId(application.getFreezeId())
                .setTransactionNo(transactionKey)
                .setTransactionIdempotencyKey(transactionKey)
                .setUserNameSnapshot(userNameSnapshot(application, eligibilitySnapshot))
                .setDeptNameSnapshot(eligibilitySnapshot == null ? null : eligibilitySnapshot.getDeptNameSnapshot())
                .setSourceTitleSnapshot("兑换审核通过")
                .setReason(reqBO.getReason())
                .setConvertedAt(reviewTime)
                .setRuleItemCode(ClubPointRuleItemCodeEnum.REDEMPTION_MIN_POINTS.getCode())
                .setSourceSnapshotJson(applicationSnapshot(application))
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(auditLogId);
    }

    private static ClubPointFreezeCreateReqBO buildFreezeReq(ClubPointRedemptionApplyReqBO reqBO,
                                                             ClubPointRedemptionGiftDO gift, Long applicationId) {
        return new ClubPointFreezeCreateReqBO()
                .setFreezeNo(FREEZE_NO_PREFIX + reqBO.getRequestNo())
                .setUserId(reqBO.getUserId())
                .setPoints(gift.getPointsCost())
                .setSourceType(ClubPointFreezeSourceTypeEnum.REDEMPTION_APPLICATION.getType())
                .setSourceId(applicationId)
                .setFrozenAt(applyTime(reqBO))
                .setIdempotencyKey(FREEZE_NO_PREFIX + reqBO.getBatchId() + ":" + reqBO.getGiftId()
                        + ":" + reqBO.getUserId() + ":" + reqBO.getRequestNo());
    }

    private static ClubPointRedemptionApplicationDO buildPendingApplication(
            ClubPointRedemptionApplyReqBO reqBO, String idempotencyKey, ClubPointRedemptionBatchDO batch,
            ClubPointRedemptionGiftDO gift, ClubPointRedemptionEligibilitySnapshotDO eligibilitySnapshot,
            ClubPointAccountDO beforeAccount) {
        return new ClubPointRedemptionApplicationDO()
                .setApplicationNo(APPLICATION_NO_PREFIX + reqBO.getRequestNo())
                .setRequestNo(reqBO.getRequestNo())
                .setBatchId(reqBO.getBatchId())
                .setGiftId(reqBO.getGiftId())
                .setEligibilitySnapshotId(eligibilitySnapshot.getId())
                .setUserId(reqBO.getUserId())
                .setStatus(ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus())
                .setPointsCost(gift.getPointsCost())
                .setQuantity(APPLY_QUANTITY)
                .setQualificationRankSnapshot(eligibilitySnapshot.getRankNo())
                .setBeforeNetPoints(accountNetPoints(beforeAccount))
                .setBeforeFrozenPoints(accountFrozenPoints(beforeAccount))
                .setBeforeAvailablePoints(accountAvailablePoints(beforeAccount))
                .setBatchSnapshotJson(batchSnapshot(batch))
                .setGiftSnapshotJson(giftSnapshot(gift))
                .setApplyTime(applyTime(reqBO))
                .setIdempotencyKey(idempotencyKey);
    }

    private static ClubPointRedemptionApplicationDO applyReviewedApplication(
            ClubPointRedemptionApplicationDO application, ClubPointRedemptionReviewReqBO reqBO,
            LocalDateTime reviewTime, ClubPointAccountDO afterAccount) {
        return application.setReviewerUserId(reqBO.getOperatorUserId())
                .setReviewTime(reviewTime)
                .setReviewReason(reqBO.getReason())
                .setAfterNetPoints(accountNetPoints(afterAccount))
                .setAfterFrozenPoints(accountFrozenPoints(afterAccount))
                .setAfterAvailablePoints(accountAvailablePoints(afterAccount));
    }

    private static ClubPointRedemptionApplicationDO applyCanceledApplication(
            ClubPointRedemptionApplicationDO application, String reason, LocalDateTime cancelTime,
            ClubPointAccountDO afterAccount) {
        return application.setStatus(ClubPointRedemptionApplicationStatusEnum.CANCELED_BEFORE_REVIEW.getStatus())
                .setCancelTime(cancelTime)
                .setCancelReason(reason)
                .setAfterNetPoints(accountNetPoints(afterAccount))
                .setAfterFrozenPoints(accountFrozenPoints(afterAccount))
                .setAfterAvailablePoints(accountAvailablePoints(afterAccount));
    }

    private static String batchSnapshot(ClubPointRedemptionBatchDO batch) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", batch.getId());
        snapshot.put("year", batch.getYear());
        snapshot.put("name", batch.getName());
        snapshot.put("status", batch.getStatus());
        snapshot.put("openTime", batch.getOpenTime());
        snapshot.put("closeTime", batch.getCloseTime());
        snapshot.put("minAvailablePoints", batch.getMinAvailablePoints());
        snapshot.put("qualifiedCount", batch.getQualifiedCount());
        snapshot.put("includeTieAtCutoff", batch.getIncludeTieAtCutoff());
        snapshot.put("ruleVersionId", batch.getRuleVersionId());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String giftSnapshot(ClubPointRedemptionGiftDO gift) {
        if (StringUtils.hasText(gift.getGiftSnapshotJson())) {
            return gift.getGiftSnapshotJson();
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", gift.getId());
        snapshot.put("batchId", gift.getBatchId());
        snapshot.put("name", gift.getName());
        snapshot.put("pointsCost", gift.getPointsCost());
        snapshot.put("referenceAmountCent", gift.getReferenceAmountCent());
        snapshot.put("imageFileId", gift.getImageFileId());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String applicationSnapshot(ClubPointRedemptionApplicationDO application) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", application.getId());
        snapshot.put("applicationNo", application.getApplicationNo());
        snapshot.put("requestNo", application.getRequestNo());
        snapshot.put("batchId", application.getBatchId());
        snapshot.put("giftId", application.getGiftId());
        snapshot.put("userId", application.getUserId());
        snapshot.put("status", application.getStatus());
        snapshot.put("pointsCost", application.getPointsCost());
        snapshot.put("quantity", application.getQuantity());
        snapshot.put("freezeId", application.getFreezeId());
        snapshot.put("stockLockId", application.getStockLockId());
        snapshot.put("deductTransactionId", application.getDeductTransactionId());
        snapshot.put("reviewerUserId", application.getReviewerUserId());
        snapshot.put("reviewTime", application.getReviewTime());
        snapshot.put("reviewReason", application.getReviewReason());
        snapshot.put("directIssueTime", application.getDirectIssueTime());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String freezeSnapshot(ClubPointFreezeDO freeze) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", freeze.getId());
        snapshot.put("freezeNo", freeze.getFreezeNo());
        snapshot.put("userId", freeze.getUserId());
        snapshot.put("points", freeze.getPoints());
        snapshot.put("status", freeze.getStatus());
        snapshot.put("sourceType", freeze.getSourceType());
        snapshot.put("sourceId", freeze.getSourceId());
        snapshot.put("convertedTransactionId", freeze.getConvertedTransactionId());
        snapshot.put("releasedAt", freeze.getReleasedAt());
        snapshot.put("releaseReason", freeze.getReleaseReason());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String stockSnapshot(ClubPointStockLockDO stockLock) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", stockLock.getId());
        snapshot.put("giftId", stockLock.getGiftId());
        snapshot.put("applicationId", stockLock.getApplicationId());
        snapshot.put("userId", stockLock.getUserId());
        snapshot.put("quantity", stockLock.getQuantity());
        snapshot.put("status", stockLock.getStatus());
        snapshot.put("usedTime", stockLock.getUsedTime());
        snapshot.put("releasedTime", stockLock.getReleasedTime());
        snapshot.put("releaseReason", stockLock.getReleaseReason());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String reviewAuditSnapshot(ClubPointRedemptionApplicationDO application,
                                              ClubPointRedemptionReviewReqBO reqBO,
                                              ClubPointRedemptionReviewResultEnum result,
                                              LocalDateTime reviewTime) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", application.getId());
        snapshot.put("applicationNo", application.getApplicationNo());
        snapshot.put("result", result.getResult());
        snapshot.put("reviewerUserId", reqBO.getOperatorUserId());
        snapshot.put("reviewTime", reviewTime);
        snapshot.put("reason", reqBO.getReason());
        return JsonUtils.toJsonString(snapshot);
    }

    private static void validateApplyReq(ClubPointRedemptionApplyReqBO reqBO) {
        if (reqBO == null || reqBO.getBatchId() == null || reqBO.getGiftId() == null
                || reqBO.getUserId() == null || !APPLY_QUANTITY.equals(reqBO.getQuantity())
                || !StringUtils.hasText(reqBO.getRequestNo())) {
            throw exception(CLUB_REDEMPTION_GIFT_INVALID);
        }
    }

    private static ClubPointRedemptionReviewResultEnum validateReviewReq(ClubPointRedemptionReviewReqBO reqBO) {
        if (reqBO == null || reqBO.getId() == null || reqBO.getOperatorUserId() == null) {
            throw exception(CLUB_REDEMPTION_APPLICATION_STATUS_INVALID);
        }
        ClubPointRedemptionReviewResultEnum result = ClubPointRedemptionReviewResultEnum.of(reqBO.getResult());
        if (result == null) {
            throw exception(CLUB_REDEMPTION_APPLICATION_STATUS_INVALID);
        }
        if (result == ClubPointRedemptionReviewResultEnum.REJECTED && !StringUtils.hasText(reqBO.getReason())) {
            throw exception(CLUB_REDEMPTION_APPLICATION_STATUS_INVALID);
        }
        return result;
    }

    private static void validateCancelReq(ClubPointRedemptionCancelReqBO reqBO) {
        if (reqBO == null || reqBO.getId() == null || reqBO.getUserId() == null) {
            throw exception(CLUB_REDEMPTION_APPLICATION_STATUS_INVALID);
        }
    }

    private static void validateTimeoutReq(ClubPointRedemptionTimeoutReqBO reqBO) {
        if (reqBO == null || reqBO.getAppliedBefore() == null) {
            throw exception(CLUB_REDEMPTION_APPLICATION_STATUS_INVALID);
        }
    }

    private static String buildApplicationIdempotencyKey(ClubPointRedemptionApplyReqBO reqBO) {
        return APPLICATION_IDEMPOTENCY_PREFIX + reqBO.getBatchId() + ":" + reqBO.getGiftId()
                + ":" + reqBO.getUserId() + ":" + reqBO.getRequestNo();
    }

    private static LocalDateTime applyTime(ClubPointRedemptionApplyReqBO reqBO) {
        return reqBO.getApplyTime() == null ? LocalDateTime.now() : reqBO.getApplyTime();
    }

    private static String cancelReason(String reason, String defaultReason) {
        return StringUtils.hasText(reason) ? reason : defaultReason;
    }

    private static String userNameSnapshot(ClubPointRedemptionApplicationDO application,
                                           ClubPointRedemptionEligibilitySnapshotDO eligibilitySnapshot) {
        if (eligibilitySnapshot != null && StringUtils.hasText(eligibilitySnapshot.getUserNameSnapshot())) {
            return eligibilitySnapshot.getUserNameSnapshot();
        }
        return "用户" + application.getUserId();
    }

    private static int accountNetPoints(ClubPointAccountDO account) {
        return account == null || account.getNetPoints() == null ? 0 : account.getNetPoints();
    }

    private static int accountFrozenPoints(ClubPointAccountDO account) {
        return account == null || account.getFrozenPoints() == null ? 0 : account.getFrozenPoints();
    }

    private static int accountAvailablePoints(ClubPointAccountDO account) {
        return account == null || account.getAvailablePoints() == null ? 0 : account.getAvailablePoints();
    }

}
