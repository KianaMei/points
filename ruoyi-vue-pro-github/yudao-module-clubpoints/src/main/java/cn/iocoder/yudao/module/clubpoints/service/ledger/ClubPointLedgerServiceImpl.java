package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleResolveService;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleSnapshotBO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_TRANSACTION_DUPLICATED;

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

    private Long handleExistingTransaction(ClubPointTransactionDO existing, ClubPointLedgerCreateReqBO reqBO) {
        if (isSameRequest(existing, reqBO)) {
            return existing.getId();
        }
        throw exception(CLUB_LEDGER_TRANSACTION_DUPLICATED);
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

    private static boolean isDecrease(ClubPointLedgerCreateReqBO reqBO) {
        return ClubPointTransactionDirectionEnum.DECREASE.getDirection().equals(reqBO.getDirection());
    }

    private static Integer getAvailablePoints(ClubPointAccountDO account) {
        return account == null ? 0 : account.getAvailablePoints();
    }

}
