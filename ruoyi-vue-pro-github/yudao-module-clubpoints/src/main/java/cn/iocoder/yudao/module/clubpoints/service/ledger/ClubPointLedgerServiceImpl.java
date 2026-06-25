package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
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
import java.util.Objects;

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

    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
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

}
