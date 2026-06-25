package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.job.ClubJobRunMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointFreezeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountRebuildAllReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountRebuildReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerAdjustReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleResolveService;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleSnapshotBO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_ADJUST_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_REVERSE_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_TRANSACTION_DUPLICATED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_TRANSACTION_NOT_EXISTS;

/**
 * 积分账本服务实现
 */
@Service
public class ClubPointLedgerServiceImpl implements ClubPointLedgerService {

    private static final String JOB_TASK_LEDGER_ACCOUNT_REBUILD = "LEDGER_ACCOUNT_REBUILD";
    private static final String JOB_BIZ_USER_ACCOUNT = "USER_ACCOUNT";
    private static final String JOB_BIZ_ALL_ACCOUNT = "ALL_ACCOUNT";
    private static final Integer JOB_STATUS_SUCCESS = 3;
    private static final Integer DEFAULT_MANUAL_TRIGGER_SOURCE = 2;

    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointFreezeMapper freezeMapper;
    @Resource
    private ClubJobRunMapper jobRunMapper;
    @Resource
    private ClubPointRuleResolveService ruleResolveService;
    @Resource
    private ClubAuditService auditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransaction(ClubPointLedgerCreateReqBO reqBO) {
        ClubPointTransactionDO existing = transactionMapper.selectByIdempotencyKey(reqBO.getIdempotencyKey());
        if (existing != null) {
            return handleExistingTransaction(existing, reqBO);
        }

        ClubPointRuleSnapshotBO ruleSnapshot = buildRuleSnapshot(reqBO);
        ClubPointAccountDO account = accountMapper.selectByUserIdForUpdate(reqBO.getUserId());
        if (isDecrease(reqBO) && getAvailablePoints(account) < reqBO.getPoints()) {
            throw exception(CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH);
        }

        ClubPointTransactionDO transaction = buildTransaction(reqBO, ruleSnapshot);
        try {
            transactionMapper.insert(transaction);
        } catch (DuplicateKeyException ex) {
            ClubPointTransactionDO duplicated = transactionMapper.selectByIdempotencyKey(reqBO.getIdempotencyKey());
            if (duplicated != null) {
                return handleExistingTransaction(duplicated, reqBO);
            }
            throw exception(CLUB_LEDGER_TRANSACTION_DUPLICATED);
        }

        upsertAccount(reqBO, transaction, account);
        return transaction.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reverseTransaction(ClubPointLedgerReverseReqBO reqBO) {
        ClubPointTransactionDO source = transactionMapper.selectByIdForUpdate(reqBO.getSourceTransactionId());
        if (source == null) {
            throw exception(CLUB_LEDGER_TRANSACTION_NOT_EXISTS);
        }
        validateCanReverse(source);

        ClubPointTransactionDO existingReverse = transactionMapper.selectByReverseOfTransactionId(source.getId());
        if (existingReverse != null) {
            return handleExistingReverse(existingReverse, reqBO);
        }

        Long auditLogId = auditService.createAuditLog(buildAuditReq(
                ClubAuditActionTypeConstants.POINT_REVERSE, source.getId(), reqBO.getOperatorUserId(),
                reqBO.getOperatorNameSnapshot(), reqBO.getOperatorRoleSnapshot(), reqBO.getOccurredAt(),
                reqBO.getClientIp(), reqBO.getUserAgent(), reqBO.getReason(),
                "{\"sourceTransactionId\":" + source.getId() + "}", reqBO.getAttachmentSnapshotJson()));
        ClubPointAccountDO account = accountMapper.selectByUserIdForUpdate(source.getUserId());
        ClubPointTransactionDO reverse = buildReverseTransaction(reqBO, source, auditLogId);
        try {
            transactionMapper.insert(reverse);
        } catch (DuplicateKeyException ex) {
            ClubPointTransactionDO duplicated = transactionMapper.selectByReverseOfTransactionId(source.getId());
            if (duplicated != null) {
                return handleExistingReverse(duplicated, reqBO);
            }
            throw exception(CLUB_LEDGER_REVERSE_INVALID);
        }

        applyTransactionToAccount(account, reverse);
        accountMapper.updateById(account);
        return reverse.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long adjustPoints(ClubPointLedgerAdjustReqBO reqBO) {
        validateAdjustReq(reqBO);
        String idempotencyKey = buildAdjustIdempotencyKey(reqBO.getRequestNo());
        ClubPointTransactionDO existing = transactionMapper.selectByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return existing.getId();
        }

        Long auditLogId = auditService.createAuditLog(buildAuditReq(
                ClubAuditActionTypeConstants.POINT_ADJUST, null, reqBO.getOperatorUserId(),
                reqBO.getOperatorNameSnapshot(), reqBO.getOperatorRoleSnapshot(), reqBO.getOccurredAt(),
                reqBO.getClientIp(), reqBO.getUserAgent(), reqBO.getReason(),
                "{\"requestNo\":\"" + reqBO.getRequestNo() + "\"}", reqBO.getAttachmentSnapshotJson()));
        return createTransaction(buildAdjustmentTransactionReq(reqBO, idempotencyKey, auditLogId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long rebuildUserAccount(ClubPointAccountRebuildReqBO reqBO) {
        String idempotencyKey = buildUserAccountRebuildIdempotencyKey(
                reqBO.getBusinessYear(), reqBO.getUserId(), reqBO.getRunKey());
        ClubJobRunDO existing = jobRunMapper.selectByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return existing.getId();
        }

        LocalDateTime startTime = LocalDateTime.now();
        rebuildSingleAccount(reqBO.getUserId(), reqBO.getBusinessYear(), startTime);
        ClubJobRunDO jobRun = buildJobRun(JOB_BIZ_USER_ACCOUNT, reqBO.getUserId(), reqBO.getRunKey(),
                idempotencyKey, reqBO.getPlannedTime(), startTime, reqBO.getTriggerSource(),
                reqBO.getHandlerUserId(), 1, 1, reqBO.getManualHandleReason(),
                "{\"businessYear\":" + reqBO.getBusinessYear() + ",\"userId\":" + reqBO.getUserId() + "}");
        return insertJobRun(jobRun);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long rebuildAllAccounts(ClubPointAccountRebuildAllReqBO reqBO) {
        String idempotencyKey = buildAllAccountRebuildIdempotencyKey(reqBO.getBusinessYear(), reqBO.getRunKey());
        ClubJobRunDO existing = jobRunMapper.selectByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return existing.getId();
        }

        LocalDateTime startTime = LocalDateTime.now();
        Set<Long> userIds = collectRebuildUserIds();
        for (Long userId : userIds) {
            rebuildSingleAccount(userId, reqBO.getBusinessYear(), startTime);
        }
        int totalCount = userIds.size();
        ClubJobRunDO jobRun = buildJobRun(JOB_BIZ_ALL_ACCOUNT, null, reqBO.getRunKey(),
                idempotencyKey, reqBO.getPlannedTime(), startTime, reqBO.getTriggerSource(),
                reqBO.getHandlerUserId(), totalCount, totalCount, reqBO.getManualHandleReason(),
                "{\"businessYear\":" + reqBO.getBusinessYear() + ",\"rebuiltUsers\":" + totalCount + "}");
        return insertJobRun(jobRun);
    }

    private Long handleExistingTransaction(ClubPointTransactionDO existing, ClubPointLedgerCreateReqBO reqBO) {
        if (isSameRequest(existing, reqBO)) {
            return existing.getId();
        }
        throw exception(CLUB_LEDGER_TRANSACTION_DUPLICATED);
    }

    private Long handleExistingReverse(ClubPointTransactionDO existing, ClubPointLedgerReverseReqBO reqBO) {
        if (Objects.equals(existing.getTransactionNo(), reqBO.getTransactionNo())
                && Objects.equals(existing.getIdempotencyKey(), buildReverseIdempotencyKey(reqBO.getSourceTransactionId()))) {
            return existing.getId();
        }
        throw exception(CLUB_LEDGER_REVERSE_INVALID);
    }

    private ClubPointRuleSnapshotBO buildRuleSnapshot(ClubPointLedgerCreateReqBO reqBO) {
        if (reqBO.getRuleVersionId() != null) {
            return ruleResolveService.snapshotRuleItem(reqBO.getRuleVersionId(), reqBO.getRuleItemCode(), reqBO.getPoints());
        }
        ClubPointRuleVersionDO version = ruleResolveService.getEffectiveVersion(reqBO.getOccurredAt());
        return ruleResolveService.snapshotRuleItem(version.getId(), reqBO.getRuleItemCode(), reqBO.getPoints());
    }

    private static ClubPointTransactionDO buildTransaction(ClubPointLedgerCreateReqBO reqBO,
                                                          ClubPointRuleSnapshotBO ruleSnapshot) {
        LocalDateTime occurredAt = reqBO.getOccurredAt();
        return new ClubPointTransactionDO()
                .setTransactionNo(reqBO.getTransactionNo())
                .setUserId(reqBO.getUserId())
                .setUserNameSnapshot(reqBO.getUserNameSnapshot())
                .setDeptIdSnapshot(reqBO.getDeptIdSnapshot())
                .setDeptNameSnapshot(reqBO.getDeptNameSnapshot())
                .setDirection(reqBO.getDirection())
                .setPoints(reqBO.getPoints())
                .setPointCategory(reqBO.getPointCategory())
                .setPointTypeCode(reqBO.getPointTypeCode() != null ? reqBO.getPointTypeCode() : ruleSnapshot.getRuleItemCode())
                .setStatus(ClubPointTransactionStatusEnum.VALID.getStatus())
                .setSourceType(reqBO.getSourceType())
                .setSourceId(reqBO.getSourceId())
                .setSourceItemId(reqBO.getSourceItemId())
                .setSourceTitleSnapshot(reqBO.getSourceTitleSnapshot())
                .setIssuingClubId(reqBO.getIssuingClubId())
                .setIssuingClubCodeSnapshot(reqBO.getIssuingClubCodeSnapshot())
                .setIssuingClubNameSnapshot(reqBO.getIssuingClubNameSnapshot())
                .setActivityId(reqBO.getActivityId())
                .setActivityTitleSnapshot(reqBO.getActivityTitleSnapshot())
                .setRuleVersionId(ruleSnapshot.getRuleVersionId())
                .setRuleItemId(ruleSnapshot.getRuleItemId())
                .setRuleItemCodeSnapshot(ruleSnapshot.getRuleItemCode())
                .setRuleSnapshotJson(ruleSnapshot.getRuleSnapshotJson())
                .setEvidenceType(reqBO.getEvidenceType())
                .setMaterialSummary(reqBO.getMaterialSummary())
                .setReason(reqBO.getReason())
                .setOccurredAt(occurredAt)
                .setBusinessYear(occurredAt.getYear())
                .setBusinessMonth(occurredAt.getYear() * 100 + occurredAt.getMonthValue())
                .setIdempotencyKey(reqBO.getIdempotencyKey())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(reqBO.getAuditLogId())
                .setSnapshotJson(reqBO.getSourceSnapshotJson());
    }

    private static ClubPointTransactionDO buildReverseTransaction(ClubPointLedgerReverseReqBO reqBO,
                                                                  ClubPointTransactionDO source,
                                                                  Long auditLogId) {
        Integer direction = isIncrease(source)
                ? ClubPointTransactionDirectionEnum.DECREASE.getDirection()
                : ClubPointTransactionDirectionEnum.INCREASE.getDirection();
        return new ClubPointTransactionDO()
                .setTransactionNo(reqBO.getTransactionNo())
                .setUserId(source.getUserId())
                .setUserNameSnapshot(source.getUserNameSnapshot())
                .setDeptIdSnapshot(source.getDeptIdSnapshot())
                .setDeptNameSnapshot(source.getDeptNameSnapshot())
                .setDirection(direction)
                .setPoints(source.getPoints())
                .setPointCategory(ClubPointCategoryEnum.REVERSAL.getCategory())
                .setPointTypeCode(source.getPointTypeCode())
                .setStatus(ClubPointTransactionStatusEnum.REVERSAL.getStatus())
                .setSourceType(ClubPointTransactionSourceTypeEnum.REVERSAL.getType())
                .setSourceId(source.getId())
                .setSourceItemId(source.getSourceItemId())
                .setSourceTitleSnapshot(source.getSourceTitleSnapshot())
                .setIssuingClubId(source.getIssuingClubId())
                .setIssuingClubCodeSnapshot(source.getIssuingClubCodeSnapshot())
                .setIssuingClubNameSnapshot(source.getIssuingClubNameSnapshot())
                .setActivityId(source.getActivityId())
                .setActivityTitleSnapshot(source.getActivityTitleSnapshot())
                .setActivityDateSnapshot(source.getActivityDateSnapshot())
                .setRuleVersionId(source.getRuleVersionId())
                .setRuleItemId(source.getRuleItemId())
                .setRuleItemCodeSnapshot(source.getRuleItemCodeSnapshot())
                .setRuleSnapshotJson(source.getRuleSnapshotJson())
                .setEvidenceType(source.getEvidenceType())
                .setMaterialSummary(source.getMaterialSummary())
                .setReason(reqBO.getReason())
                .setOccurredAt(reqBO.getOccurredAt())
                .setBusinessYear(reqBO.getOccurredAt().getYear())
                .setBusinessMonth(reqBO.getOccurredAt().getYear() * 100 + reqBO.getOccurredAt().getMonthValue())
                .setIdempotencyKey(buildReverseIdempotencyKey(source.getId()))
                .setReverseOfTransactionId(source.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(auditLogId)
                .setSnapshotJson(reqBO.getAttachmentSnapshotJson());
    }

    private static ClubPointLedgerCreateReqBO buildAdjustmentTransactionReq(ClubPointLedgerAdjustReqBO reqBO,
                                                                            String idempotencyKey,
                                                                            Long auditLogId) {
        return new ClubPointLedgerCreateReqBO()
                .setTransactionNo(reqBO.getTransactionNo())
                .setUserId(reqBO.getUserId())
                .setUserNameSnapshot(reqBO.getUserNameSnapshot())
                .setDeptIdSnapshot(reqBO.getDeptIdSnapshot())
                .setDeptNameSnapshot(reqBO.getDeptNameSnapshot())
                .setDirection(reqBO.getDirection())
                .setPoints(reqBO.getPoints())
                .setPointCategory(ClubPointCategoryEnum.ADMIN_ADJUSTMENT.getCategory())
                .setPointTypeCode("ADJUST:" + reqBO.getAdjustType())
                .setSourceType(ClubPointTransactionSourceTypeEnum.ADJUSTMENT.getType())
                .setSourceTitleSnapshot("管理员调整积分")
                .setIssuingClubId(reqBO.getIssuingClubId())
                .setIssuingClubCodeSnapshot(reqBO.getIssuingClubCodeSnapshot())
                .setIssuingClubNameSnapshot(reqBO.getIssuingClubNameSnapshot())
                .setMaterialSummary(reqBO.getMaterialSummary())
                .setReason(reqBO.getReason())
                .setOccurredAt(reqBO.getOccurredAt())
                .setIdempotencyKey(idempotencyKey)
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(auditLogId)
                .setRuleItemCode(reqBO.getRuleItemCode())
                .setRuleVersionId(reqBO.getRuleVersionId())
                .setSourceSnapshotJson(reqBO.getAttachmentSnapshotJson());
    }

    private static ClubAuditCreateReqBO buildAuditReq(String actionType, Long bizId, Long operatorUserId,
                                                      String operatorNameSnapshot, String operatorRoleSnapshot,
                                                      LocalDateTime operationTime, String clientIp, String userAgent,
                                                      String reason, String beforeJson, String targetSnapshotJson) {
        return new ClubAuditCreateReqBO()
                .setActionType(actionType)
                .setBizType("LEDGER_TRANSACTION")
                .setBizId(bizId)
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot(operatorNameSnapshot)
                .setOperatorRoleSnapshot(operatorRoleSnapshot)
                .setOperationTime(operationTime)
                .setClientIp(clientIp)
                .setUserAgent(userAgent)
                .setReason(reason)
                .setBeforeJson(beforeJson)
                .setTargetSnapshotJson(targetSnapshotJson)
                .setSuccess(true);
    }

    private Set<Long> collectRebuildUserIds() {
        Set<Long> userIds = new LinkedHashSet<>();
        for (ClubPointTransactionDO transaction : transactionMapper.selectEffectiveListForRebuild()) {
            userIds.add(transaction.getUserId());
        }
        for (ClubPointFreezeDO freeze : freezeMapper.selectFrozenListForRebuild()) {
            userIds.add(freeze.getUserId());
        }
        for (ClubPointAccountDO account : accountMapper.selectListForRebuild()) {
            userIds.add(account.getUserId());
        }
        return userIds;
    }

    private void rebuildSingleAccount(Long userId, Integer businessYear, LocalDateTime rebuildTime) {
        List<ClubPointTransactionDO> transactions = transactionMapper.selectEffectiveListByUserId(userId);
        int totalPositivePoints = 0;
        int totalNegativePoints = 0;
        int annualEarnedPoints = 0;
        ClubPointTransactionDO lastTransaction = null;
        for (ClubPointTransactionDO transaction : transactions) {
            if (isIncrease(transaction)) {
                totalPositivePoints += transaction.getPoints();
                if (Objects.equals(transaction.getBusinessYear(), businessYear)) {
                    annualEarnedPoints += transaction.getPoints();
                }
            } else {
                totalNegativePoints += transaction.getPoints();
            }
            lastTransaction = transaction;
        }

        int netPoints = totalPositivePoints - totalNegativePoints;
        int frozenPoints = sumFrozenPoints(userId);
        ClubPointAccountDO account = accountMapper.selectByUserIdForUpdate(userId);
        if (account == null) {
            account = new ClubPointAccountDO()
                    .setUserId(userId)
                    .setVersion(0);
        }
        applyRebuildResult(account, totalPositivePoints, totalNegativePoints, netPoints, frozenPoints,
                annualEarnedPoints, lastTransaction, rebuildTime);
        if (account.getId() == null) {
            accountMapper.insert(account);
        } else {
            accountMapper.updateById(account);
        }
    }

    private int sumFrozenPoints(Long userId) {
        int frozenPoints = 0;
        for (ClubPointFreezeDO freeze : freezeMapper.selectFrozenListByUserId(userId)) {
            if (ClubPointFreezeStatusEnum.FROZEN.getStatus().equals(freeze.getStatus())) {
                frozenPoints += freeze.getPoints();
            }
        }
        return frozenPoints;
    }

    private static void applyRebuildResult(ClubPointAccountDO account, int totalPositivePoints,
                                           int totalNegativePoints, int netPoints, int frozenPoints,
                                           int annualEarnedPoints, ClubPointTransactionDO lastTransaction,
                                           LocalDateTime rebuildTime) {
        account.setTotalPositivePoints(totalPositivePoints)
                .setTotalNegativePoints(totalNegativePoints)
                .setNetPoints(netPoints)
                .setFrozenPoints(frozenPoints)
                .setAvailablePoints(Math.max(netPoints - frozenPoints, 0))
                .setAnnualEarnedPoints(annualEarnedPoints)
                .setLastTransactionId(lastTransaction == null ? null : lastTransaction.getId())
                .setLastTransactionTime(lastTransaction == null ? null : lastTransaction.getOccurredAt())
                .setLastRebuildTime(rebuildTime)
                .setVersion((account.getVersion() == null ? 0 : account.getVersion()) + 1);
    }

    private Long insertJobRun(ClubJobRunDO jobRun) {
        try {
            jobRunMapper.insert(jobRun);
            return jobRun.getId();
        } catch (DuplicateKeyException ex) {
            ClubJobRunDO duplicated = jobRunMapper.selectByIdempotencyKey(jobRun.getIdempotencyKey());
            if (duplicated != null) {
                return duplicated.getId();
            }
            throw ex;
        }
    }

    private static ClubJobRunDO buildJobRun(String bizType, Long bizId, String runKey, String idempotencyKey,
                                            LocalDateTime plannedTime, LocalDateTime startTime,
                                            Integer triggerSource, Long handlerUserId, Integer totalCount,
                                            Integer successCount, String manualHandleReason, String resultJson) {
        return new ClubJobRunDO()
                .setTaskType(JOB_TASK_LEDGER_ACCOUNT_REBUILD)
                .setBizType(bizType)
                .setBizId(bizId)
                .setRunKey(runKey)
                .setIdempotencyKey(idempotencyKey)
                .setStatus(JOB_STATUS_SUCCESS)
                .setPlannedTime(plannedTime)
                .setStartTime(startTime)
                .setEndTime(LocalDateTime.now())
                .setTriggerSource(triggerSource != null ? triggerSource : DEFAULT_MANUAL_TRIGGER_SOURCE)
                .setHandlerUserId(handlerUserId)
                .setTotalCount(totalCount)
                .setSuccessCount(successCount)
                .setSkipCount(0)
                .setFailedCount(0)
                .setRetryCount(0)
                .setResultJson(resultJson)
                .setManualHandleReason(manualHandleReason);
    }

    private void upsertAccount(ClubPointLedgerCreateReqBO reqBO, ClubPointTransactionDO transaction,
                               ClubPointAccountDO account) {
        if (account == null) {
            account = new ClubPointAccountDO()
                    .setUserId(reqBO.getUserId())
                    .setTotalPositivePoints(0)
                    .setTotalNegativePoints(0)
                    .setNetPoints(0)
                    .setFrozenPoints(0)
                    .setAvailablePoints(0)
                    .setAnnualEarnedPoints(0)
                    .setVersion(0);
            applyTransactionToAccount(account, reqBO, transaction);
            accountMapper.insert(account);
            return;
        }
        applyTransactionToAccount(account, reqBO, transaction);
        accountMapper.updateById(account);
    }

    private static void applyTransactionToAccount(ClubPointAccountDO account, ClubPointLedgerCreateReqBO reqBO,
                                                  ClubPointTransactionDO transaction) {
        if (isIncrease(reqBO)) {
            account.setTotalPositivePoints(account.getTotalPositivePoints() + reqBO.getPoints())
                    .setNetPoints(account.getNetPoints() + reqBO.getPoints())
                    .setAnnualEarnedPoints(account.getAnnualEarnedPoints() + reqBO.getPoints());
        } else {
            account.setTotalNegativePoints(account.getTotalNegativePoints() + reqBO.getPoints())
                    .setNetPoints(account.getNetPoints() - reqBO.getPoints());
        }
        account.setAvailablePoints(Math.max(account.getNetPoints() - account.getFrozenPoints(), 0))
                .setLastTransactionId(transaction.getId())
                .setLastTransactionTime(transaction.getOccurredAt())
                .setVersion(account.getVersion() + 1);
    }

    private static void applyTransactionToAccount(ClubPointAccountDO account, ClubPointTransactionDO transaction) {
        if (isIncrease(transaction)) {
            account.setTotalPositivePoints(account.getTotalPositivePoints() + transaction.getPoints())
                    .setNetPoints(account.getNetPoints() + transaction.getPoints())
                    .setAnnualEarnedPoints(account.getAnnualEarnedPoints() + transaction.getPoints());
        } else {
            if (account.getAvailablePoints() < transaction.getPoints()) {
                throw exception(CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH);
            }
            account.setTotalNegativePoints(account.getTotalNegativePoints() + transaction.getPoints())
                    .setNetPoints(account.getNetPoints() - transaction.getPoints());
        }
        account.setAvailablePoints(Math.max(account.getNetPoints() - account.getFrozenPoints(), 0))
                .setLastTransactionId(transaction.getId())
                .setLastTransactionTime(transaction.getOccurredAt())
                .setVersion(account.getVersion() + 1);
    }

    private static void validateCanReverse(ClubPointTransactionDO source) {
        if (!ClubPointTransactionStatusEnum.VALID.getStatus().equals(source.getStatus())
                || source.getReverseOfTransactionId() != null) {
            throw exception(CLUB_LEDGER_REVERSE_INVALID);
        }
    }

    private static void validateAdjustReq(ClubPointLedgerAdjustReqBO reqBO) {
        if (reqBO == null || !StringUtils.hasText(reqBO.getRequestNo())
                || !StringUtils.hasText(reqBO.getTransactionNo())
                || reqBO.getUserId() == null
                || reqBO.getDirection() == null
                || reqBO.getPoints() == null || reqBO.getPoints() <= 0
                || !StringUtils.hasText(reqBO.getRuleItemCode())
                || !StringUtils.hasText(reqBO.getReason())
                || !StringUtils.hasText(reqBO.getAttachmentSnapshotJson())) {
            throw exception(CLUB_LEDGER_ADJUST_INVALID);
        }
    }

    private static boolean isSameRequest(ClubPointTransactionDO existing, ClubPointLedgerCreateReqBO reqBO) {
        return Objects.equals(existing.getTransactionNo(), reqBO.getTransactionNo())
                && Objects.equals(existing.getUserId(), reqBO.getUserId())
                && Objects.equals(existing.getDirection(), reqBO.getDirection())
                && Objects.equals(existing.getPoints(), reqBO.getPoints())
                && Objects.equals(existing.getPointCategory(), reqBO.getPointCategory())
                && Objects.equals(existing.getSourceType(), reqBO.getSourceType())
                && Objects.equals(existing.getSourceId(), reqBO.getSourceId())
                && Objects.equals(existing.getSourceItemId(), reqBO.getSourceItemId())
                && Objects.equals(existing.getOccurredAt(), reqBO.getOccurredAt())
                && Objects.equals(existing.getIdempotencyKey(), reqBO.getIdempotencyKey());
    }

    private static boolean isIncrease(ClubPointLedgerCreateReqBO reqBO) {
        return ClubPointTransactionDirectionEnum.INCREASE.getDirection().equals(reqBO.getDirection());
    }

    private static boolean isIncrease(ClubPointTransactionDO transaction) {
        return ClubPointTransactionDirectionEnum.INCREASE.getDirection().equals(transaction.getDirection());
    }

    private static boolean isDecrease(ClubPointLedgerCreateReqBO reqBO) {
        return ClubPointTransactionDirectionEnum.DECREASE.getDirection().equals(reqBO.getDirection());
    }

    private static Integer getAvailablePoints(ClubPointAccountDO account) {
        return account == null ? 0 : account.getAvailablePoints();
    }

    private static String buildReverseIdempotencyKey(Long sourceTransactionId) {
        return "LEDGER_REVERSE:" + sourceTransactionId;
    }

    private static String buildAdjustIdempotencyKey(String requestNo) {
        return "LEDGER_ADJUST:" + requestNo;
    }

    private static String buildUserAccountRebuildIdempotencyKey(Integer businessYear, Long userId, String runKey) {
        return "LEDGER_ACCOUNT_REBUILD:USER:" + businessYear + ":" + userId + ":" + runKey;
    }

    private static String buildAllAccountRebuildIdempotencyKey(Integer businessYear, String runKey) {
        return "LEDGER_ACCOUNT_REBUILD:ALL:" + businessYear + ":" + runKey;
    }

}
