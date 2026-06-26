package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionGiftMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionGiftStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionGiftOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionGiftSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_STOCK_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_STOCK_NOT_ENOUGH;

/**
 * 兑换礼品服务实现
 */
@Service
public class ClubPointRedemptionGiftServiceImpl implements ClubPointRedemptionGiftService {

    @Resource
    private ClubPointRedemptionBatchMapper batchMapper;
    @Resource
    private ClubPointRedemptionGiftMapper giftMapper;
    @Resource
    private ClubScopeService clubScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGift(ClubPointRedemptionGiftSaveReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        validateSaveReq(reqBO);
        validateBatchExists(reqBO.getBatchId());
        ClubPointRedemptionGiftDO gift = buildGift(reqBO)
                .setStockLocked(0)
                .setStockUsed(0)
                .setStatus(ClubPointRedemptionGiftStatusEnum.OFF_SHELF.getStatus());
        gift.setGiftSnapshotJson(snapshot(gift));
        giftMapper.insert(gift);
        return gift.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGift(ClubPointRedemptionGiftSaveReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        validateSaveReq(reqBO);
        validateBatchExists(reqBO.getBatchId());
        ClubPointRedemptionGiftDO gift = validateGiftExistsForUpdate(reqBO.getId());
        validateStockTotalCanCoverExistingCounters(reqBO.getStockTotal(), gift);
        updateGiftFields(gift, reqBO);
        gift.setGiftSnapshotJson(snapshot(gift));
        giftMapper.updateById(gift);
    }

    @Override
    public void updateGiftStatus(Long giftId, Integer status, ClubPointRedemptionGiftOperationReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointRedemptionGiftStatusEnum statusEnum = ClubPointRedemptionGiftStatusEnum.of(status);
        if (statusEnum == null) {
            throw exception(CLUB_REDEMPTION_GIFT_STATUS_INVALID);
        }
        ClubPointRedemptionGiftDO gift = validateGiftExists(giftId);
        gift.setStatus(statusEnum.getStatus())
                .setGiftSnapshotJson(snapshot(gift));
        giftMapper.updateById(gift);
    }

    @Override
    public void lockStock(Long giftId, Integer quantity) {
        validatePositiveQuantity(quantity);
        int updated = giftMapper.increaseLockedStock(giftId,
                ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), quantity);
        if (updated != 1) {
            validateGiftExists(giftId);
            throw exception(CLUB_REDEMPTION_GIFT_STOCK_NOT_ENOUGH);
        }
    }

    @Override
    public void releaseLockedStock(Long giftId, Integer quantity) {
        validatePositiveQuantity(quantity);
        int updated = giftMapper.decreaseLockedStock(giftId, quantity);
        if (updated != 1) {
            validateGiftExists(giftId);
            throw exception(CLUB_REDEMPTION_GIFT_STOCK_INVALID);
        }
    }

    @Override
    public void useLockedStock(Long giftId, Integer quantity) {
        validatePositiveQuantity(quantity);
        int updated = giftMapper.convertLockedStockToUsed(giftId, quantity);
        if (updated != 1) {
            validateGiftExists(giftId);
            throw exception(CLUB_REDEMPTION_GIFT_STOCK_INVALID);
        }
    }

    private ClubPointRedemptionBatchDO validateBatchExists(Long batchId) {
        ClubPointRedemptionBatchDO batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw exception(CLUB_REDEMPTION_BATCH_NOT_EXISTS);
        }
        return batch;
    }

    private ClubPointRedemptionGiftDO validateGiftExists(Long giftId) {
        ClubPointRedemptionGiftDO gift = giftMapper.selectById(giftId);
        if (gift == null) {
            throw exception(CLUB_REDEMPTION_GIFT_NOT_EXISTS);
        }
        return gift;
    }

    private ClubPointRedemptionGiftDO validateGiftExistsForUpdate(Long giftId) {
        ClubPointRedemptionGiftDO gift = giftMapper.selectByIdForUpdate(giftId);
        if (gift == null) {
            throw exception(CLUB_REDEMPTION_GIFT_NOT_EXISTS);
        }
        return gift;
    }

    private static void validateSaveReq(ClubPointRedemptionGiftSaveReqBO reqBO) {
        if (reqBO == null || reqBO.getBatchId() == null || !StringUtils.hasText(reqBO.getName())
                || reqBO.getPointsCost() == null || reqBO.getPointsCost() <= 0
                || reqBO.getStockTotal() == null || reqBO.getStockTotal() < 0
                || reqBO.getReferenceAmountCent() != null && reqBO.getReferenceAmountCent() < 0
                || reqBO.getSort() == null) {
            throw exception(CLUB_REDEMPTION_GIFT_INVALID);
        }
        if (reqBO.getTierMinPoints() != null && reqBO.getTierMinPoints() < 0
                || reqBO.getTierMaxPoints() != null && reqBO.getTierMaxPoints() < 0
                || reqBO.getTierMinPoints() != null && reqBO.getTierMaxPoints() != null
                && reqBO.getTierMinPoints() > reqBO.getTierMaxPoints()) {
            throw exception(CLUB_REDEMPTION_GIFT_INVALID);
        }
    }

    private static void validateStockTotalCanCoverExistingCounters(Integer stockTotal, ClubPointRedemptionGiftDO gift) {
        if (stockTotal < safeInt(gift.getStockLocked()) + safeInt(gift.getStockUsed())) {
            throw exception(CLUB_REDEMPTION_GIFT_STOCK_INVALID);
        }
    }

    private static void validatePositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw exception(CLUB_REDEMPTION_GIFT_STOCK_INVALID);
        }
    }

    private static ClubPointRedemptionGiftDO buildGift(ClubPointRedemptionGiftSaveReqBO reqBO) {
        return updateGiftFields(new ClubPointRedemptionGiftDO(), reqBO);
    }

    private static ClubPointRedemptionGiftDO updateGiftFields(ClubPointRedemptionGiftDO gift,
                                                             ClubPointRedemptionGiftSaveReqBO reqBO) {
        return gift.setBatchId(reqBO.getBatchId())
                .setName(reqBO.getName())
                .setDescription(reqBO.getDescription())
                .setPointsCost(reqBO.getPointsCost())
                .setTierMinPoints(reqBO.getTierMinPoints())
                .setTierMaxPoints(reqBO.getTierMaxPoints())
                .setReferenceAmountCent(reqBO.getReferenceAmountCent())
                .setStockTotal(reqBO.getStockTotal())
                .setImageFileId(reqBO.getImageFileId())
                .setSort(reqBO.getSort());
    }

    private static String snapshot(ClubPointRedemptionGiftDO gift) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", gift.getId());
        snapshot.put("batchId", gift.getBatchId());
        snapshot.put("name", gift.getName());
        snapshot.put("description", gift.getDescription());
        snapshot.put("pointsCost", gift.getPointsCost());
        snapshot.put("tierMinPoints", gift.getTierMinPoints());
        snapshot.put("tierMaxPoints", gift.getTierMaxPoints());
        snapshot.put("referenceAmountCent", gift.getReferenceAmountCent());
        snapshot.put("stockTotal", gift.getStockTotal());
        snapshot.put("stockLocked", gift.getStockLocked());
        snapshot.put("stockUsed", gift.getStockUsed());
        snapshot.put("status", gift.getStatus());
        snapshot.put("imageFileId", gift.getImageFileId());
        snapshot.put("sort", gift.getSort());
        return JsonUtils.toJsonString(snapshot);
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

}
