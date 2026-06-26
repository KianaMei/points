package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointStockLockDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionGiftMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointStockLockMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionBatchStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionGiftStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointStockLockStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointFreezeService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionApplyReqBO;
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
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_CLOSED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_NOT_EXISTS;
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

    @Resource
    private ClubPointRedemptionEligibilityService eligibilityService;
    @Resource
    private ClubPointRedemptionGiftService giftService;
    @Resource
    private ClubPointFreezeService freezeService;
    @Resource
    private ClubPointRedemptionBatchMapper batchMapper;
    @Resource
    private ClubPointRedemptionGiftMapper giftMapper;
    @Resource
    private ClubPointRedemptionApplicationMapper applicationMapper;
    @Resource
    private ClubPointStockLockMapper stockLockMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;

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

    private static void validateApplyReq(ClubPointRedemptionApplyReqBO reqBO) {
        if (reqBO == null || reqBO.getBatchId() == null || reqBO.getGiftId() == null
                || reqBO.getUserId() == null || !APPLY_QUANTITY.equals(reqBO.getQuantity())
                || !StringUtils.hasText(reqBO.getRequestNo())) {
            throw exception(CLUB_REDEMPTION_GIFT_INVALID);
        }
    }

    private static String buildApplicationIdempotencyKey(ClubPointRedemptionApplyReqBO reqBO) {
        return APPLICATION_IDEMPOTENCY_PREFIX + reqBO.getBatchId() + ":" + reqBO.getGiftId()
                + ":" + reqBO.getUserId() + ":" + reqBO.getRequestNo();
    }

    private static LocalDateTime applyTime(ClubPointRedemptionApplyReqBO reqBO) {
        return reqBO.getApplyTime() == null ? LocalDateTime.now() : reqBO.getApplyTime();
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
