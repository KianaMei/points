package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointFreezeMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeConvertReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeReleaseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_FREEZE_DUPLICATED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_FREEZE_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_FREEZE_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_FROZEN_POINTS_NOT_ENOUGH;

/**
 * 积分冻结服务实现
 */
@Service
public class ClubPointFreezeServiceImpl implements ClubPointFreezeService {

    @Resource
    private ClubPointFreezeMapper freezeMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointLedgerService ledgerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long freezePoints(ClubPointFreezeCreateReqBO reqBO) {
        ClubPointFreezeDO existing = freezeMapper.selectByIdempotencyKey(reqBO.getIdempotencyKey());
        if (existing != null) {
            return handleExistingFreeze(existing, reqBO);
        }

        ClubPointAccountDO account = accountMapper.selectByUserIdForUpdate(reqBO.getUserId());
        if (account == null || account.getAvailablePoints() < reqBO.getPoints()) {
            throw exception(CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH);
        }

        ClubPointFreezeDO freeze = buildFreeze(reqBO);
        try {
            freezeMapper.insert(freeze);
        } catch (DuplicateKeyException ex) {
            ClubPointFreezeDO duplicated = freezeMapper.selectByIdempotencyKey(reqBO.getIdempotencyKey());
            if (duplicated != null) {
                return handleExistingFreeze(duplicated, reqBO);
            }
            throw exception(CLUB_LEDGER_FREEZE_DUPLICATED);
        }

        increaseFrozenPoints(account, reqBO.getPoints());
        accountMapper.updateById(account);
        return freeze.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseFreeze(ClubPointFreezeReleaseReqBO reqBO) {
        ClubPointFreezeDO freeze = getFreezeForUpdate(reqBO.getFreezeId());
        if (isReleased(freeze)) {
            return;
        }
        validateFrozenStatus(freeze);

        ClubPointAccountDO account = accountMapper.selectByUserIdForUpdate(freeze.getUserId());
        decreaseFrozenPoints(account, freeze.getPoints());
        accountMapper.updateById(account);

        freeze.setStatus(ClubPointFreezeStatusEnum.RELEASED.getStatus())
                .setReleasedAt(reqBO.getReleasedAt())
                .setReleaseReason(reqBO.getReleaseReason());
        freezeMapper.updateById(freeze);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long convertFreezeToDeduction(ClubPointFreezeConvertReqBO reqBO) {
        ClubPointFreezeDO freeze = getFreezeForUpdate(reqBO.getFreezeId());
        if (isConverted(freeze) && freeze.getConvertedTransactionId() != null) {
            return freeze.getConvertedTransactionId();
        }
        validateFrozenStatus(freeze);

        ClubPointAccountDO account = accountMapper.selectByUserIdForUpdate(freeze.getUserId());
        decreaseFrozenPoints(account, freeze.getPoints());
        accountMapper.updateById(account);

        Long transactionId = ledgerService.createTransaction(buildDeductionReq(reqBO, freeze));
        freeze.setStatus(ClubPointFreezeStatusEnum.CONVERTED.getStatus())
                .setConvertedAt(reqBO.getConvertedAt())
                .setConvertedTransactionId(transactionId);
        freezeMapper.updateById(freeze);
        return transactionId;
    }

    private Long handleExistingFreeze(ClubPointFreezeDO existing, ClubPointFreezeCreateReqBO reqBO) {
        if (isSameFreezeRequest(existing, reqBO)) {
            return existing.getId();
        }
        throw exception(CLUB_LEDGER_FREEZE_DUPLICATED);
    }

    private ClubPointFreezeDO getFreezeForUpdate(Long freezeId) {
        ClubPointFreezeDO freeze = freezeMapper.selectByIdForUpdate(freezeId);
        if (freeze == null) {
            throw exception(CLUB_LEDGER_FREEZE_NOT_EXISTS);
        }
        return freeze;
    }

    private static ClubPointFreezeDO buildFreeze(ClubPointFreezeCreateReqBO reqBO) {
        return new ClubPointFreezeDO()
                .setFreezeNo(reqBO.getFreezeNo())
                .setUserId(reqBO.getUserId())
                .setPoints(reqBO.getPoints())
                .setStatus(ClubPointFreezeStatusEnum.FROZEN.getStatus())
                .setSourceType(reqBO.getSourceType())
                .setSourceId(reqBO.getSourceId())
                .setFrozenAt(reqBO.getFrozenAt())
                .setIdempotencyKey(reqBO.getIdempotencyKey());
    }

    private static ClubPointLedgerCreateReqBO buildDeductionReq(ClubPointFreezeConvertReqBO reqBO,
                                                                ClubPointFreezeDO freeze) {
        return new ClubPointLedgerCreateReqBO()
                .setTransactionNo(reqBO.getTransactionNo())
                .setUserId(freeze.getUserId())
                .setUserNameSnapshot(reqBO.getUserNameSnapshot())
                .setDeptIdSnapshot(reqBO.getDeptIdSnapshot())
                .setDeptNameSnapshot(reqBO.getDeptNameSnapshot())
                .setDirection(ClubPointTransactionDirectionEnum.DECREASE.getDirection())
                .setPoints(freeze.getPoints())
                .setPointCategory(ClubPointCategoryEnum.REDEMPTION_DEDUCTION.getCategory())
                .setSourceType(ClubPointTransactionSourceTypeEnum.REDEMPTION.getType())
                .setSourceId(freeze.getSourceId())
                .setSourceTitleSnapshot(reqBO.getSourceTitleSnapshot())
                .setReason(reqBO.getReason())
                .setOccurredAt(reqBO.getConvertedAt())
                .setIdempotencyKey(reqBO.getTransactionIdempotencyKey())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(reqBO.getAuditLogId())
                .setRuleItemCode(reqBO.getRuleItemCode())
                .setRuleVersionId(reqBO.getRuleVersionId())
                .setSourceSnapshotJson(reqBO.getSourceSnapshotJson());
    }

    private static void increaseFrozenPoints(ClubPointAccountDO account, Integer points) {
        account.setFrozenPoints(account.getFrozenPoints() + points);
        refreshAvailablePoints(account);
    }

    private static void decreaseFrozenPoints(ClubPointAccountDO account, Integer points) {
        if (account == null || account.getFrozenPoints() < points) {
            throw exception(CLUB_LEDGER_FROZEN_POINTS_NOT_ENOUGH);
        }
        account.setFrozenPoints(account.getFrozenPoints() - points);
        refreshAvailablePoints(account);
    }

    private static void refreshAvailablePoints(ClubPointAccountDO account) {
        account.setAvailablePoints(Math.max(account.getNetPoints() - account.getFrozenPoints(), 0))
                .setVersion(account.getVersion() + 1);
    }

    private static void validateFrozenStatus(ClubPointFreezeDO freeze) {
        if (!ClubPointFreezeStatusEnum.FROZEN.getStatus().equals(freeze.getStatus())) {
            throw exception(CLUB_LEDGER_FREEZE_STATUS_INVALID);
        }
    }

    private static boolean isReleased(ClubPointFreezeDO freeze) {
        return ClubPointFreezeStatusEnum.RELEASED.getStatus().equals(freeze.getStatus());
    }

    private static boolean isConverted(ClubPointFreezeDO freeze) {
        return ClubPointFreezeStatusEnum.CONVERTED.getStatus().equals(freeze.getStatus());
    }

    private static boolean isSameFreezeRequest(ClubPointFreezeDO existing, ClubPointFreezeCreateReqBO reqBO) {
        return Objects.equals(existing.getFreezeNo(), reqBO.getFreezeNo())
                && Objects.equals(existing.getUserId(), reqBO.getUserId())
                && Objects.equals(existing.getPoints(), reqBO.getPoints())
                && Objects.equals(existing.getSourceType(), reqBO.getSourceType())
                && Objects.equals(existing.getSourceId(), reqBO.getSourceId())
                && Objects.equals(existing.getFrozenAt(), reqBO.getFrozenAt())
                && Objects.equals(existing.getIdempotencyKey(), reqBO.getIdempotencyKey());
    }

}
