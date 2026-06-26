package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionGiftMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionBatchStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionGiftStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionGiftOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionGiftSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_STOCK_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_STOCK_NOT_ENOUGH;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointRedemptionGiftServiceImpl.class, ClubScopeServiceImpl.class})
class ClubPointRedemptionGiftServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 8, 1, 10, 0);

    @Resource
    private ClubPointRedemptionGiftService redemptionGiftService;
    @Resource
    private ClubPointRedemptionBatchMapper batchMapper;
    @Resource
    private ClubPointRedemptionGiftMapper giftMapper;

    @Test
    void createGiftShouldPersistOffShelfGiftAndRequireGlobalScope() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.DRAFT.getStatus());

        Long giftId = redemptionGiftService.createGift(buildSaveReq(null, batch.getId())
                .setOperatorGlobalScope(true));

        ClubPointRedemptionGiftDO gift = giftMapper.selectById(giftId);
        assertEquals(batch.getId(), gift.getBatchId());
        assertEquals("运动水杯", gift.getName());
        assertEquals(60, gift.getPointsCost());
        assertEquals(50, gift.getTierMinPoints());
        assertEquals(100, gift.getTierMaxPoints());
        assertEquals(1999L, gift.getReferenceAmountCent());
        assertEquals(10, gift.getStockTotal());
        assertEquals(0, gift.getStockLocked());
        assertEquals(0, gift.getStockUsed());
        assertEquals(ClubPointRedemptionGiftStatusEnum.OFF_SHELF.getStatus(), gift.getStatus());
        assertEquals(3001L, gift.getImageFileId());
        assertTrue(gift.getGiftSnapshotJson().contains("\"name\":\"运动水杯\""));

        assertServiceException(() -> redemptionGiftService.createGift(buildSaveReq(null, batch.getId())
                .setOperatorGlobalScope(false)), CLUB_SCOPE_DENIED);
    }

    @Test
    void updateGiftShouldChangePriceStockAndRejectStockBelowLockedUsed() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.DRAFT.getStatus());
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.OFF_SHELF.getStatus(), 5, 1, 1);

        redemptionGiftService.updateGift(buildSaveReq(gift.getId(), batch.getId())
                .setName("运动背包")
                .setPointsCost(120)
                .setStockTotal(6)
                .setOperatorGlobalScope(true));

        ClubPointRedemptionGiftDO updated = giftMapper.selectById(gift.getId());
        assertEquals("运动背包", updated.getName());
        assertEquals(120, updated.getPointsCost());
        assertEquals(6, updated.getStockTotal());
        assertEquals(1, updated.getStockLocked());
        assertEquals(1, updated.getStockUsed());

        assertServiceException(() -> redemptionGiftService.updateGift(buildSaveReq(gift.getId(), batch.getId())
                .setStockTotal(1)
                .setOperatorGlobalScope(true)), CLUB_REDEMPTION_GIFT_STOCK_INVALID);
    }

    @Test
    void updateGiftStatusShouldShelfAndUnshelfGift() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.DRAFT.getStatus());
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.OFF_SHELF.getStatus(), 10, 0, 0);

        redemptionGiftService.updateGiftStatus(gift.getId(), ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(),
                buildOperationReq().setOperatorGlobalScope(true));
        assertEquals(ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(),
                giftMapper.selectById(gift.getId()).getStatus());

        redemptionGiftService.updateGiftStatus(gift.getId(), ClubPointRedemptionGiftStatusEnum.OFF_SHELF.getStatus(),
                buildOperationReq().setOperatorGlobalScope(true));
        assertEquals(ClubPointRedemptionGiftStatusEnum.OFF_SHELF.getStatus(),
                giftMapper.selectById(gift.getId()).getStatus());

        assertServiceException(() -> redemptionGiftService.updateGiftStatus(gift.getId(), 99,
                buildOperationReq().setOperatorGlobalScope(true)), CLUB_REDEMPTION_GIFT_STATUS_INVALID);
    }

    @Test
    void lockStockShouldUseConditionalUpdateAndRejectInsufficientStock() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 2, 0, 1);

        redemptionGiftService.lockStock(gift.getId(), 1);

        ClubPointRedemptionGiftDO locked = giftMapper.selectById(gift.getId());
        assertEquals(1, locked.getStockLocked());
        assertEquals(1, locked.getStockUsed());
        assertServiceException(() -> redemptionGiftService.lockStock(gift.getId(), 1),
                CLUB_REDEMPTION_GIFT_STOCK_NOT_ENOUGH);
    }

    @Test
    void releaseAndUseLockedStockShouldKeepStockCountersAccurate() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 5, 2, 1);

        redemptionGiftService.releaseLockedStock(gift.getId(), 1);
        ClubPointRedemptionGiftDO released = giftMapper.selectById(gift.getId());
        assertEquals(1, released.getStockLocked());
        assertEquals(1, released.getStockUsed());

        redemptionGiftService.useLockedStock(gift.getId(), 1);
        ClubPointRedemptionGiftDO used = giftMapper.selectById(gift.getId());
        assertEquals(0, used.getStockLocked());
        assertEquals(2, used.getStockUsed());

        assertServiceException(() -> redemptionGiftService.useLockedStock(gift.getId(), 1),
                CLUB_REDEMPTION_GIFT_STOCK_INVALID);
    }

    @Test
    void concurrentLockStockShouldNotOversell() throws Exception {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 1, 0, 0);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger notEnoughCount = new AtomicInteger();

        for (int i = 0; i < 2; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(5, TimeUnit.SECONDS);
                    redemptionGiftService.lockStock(gift.getId(), 1);
                    successCount.incrementAndGet();
                } catch (ServiceException ex) {
                    if (CLUB_REDEMPTION_GIFT_STOCK_NOT_ENOUGH.getCode() == ex.getCode()) {
                        notEnoughCount.incrementAndGet();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startLatch.countDown();
        executorService.shutdown();
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));

        ClubPointRedemptionGiftDO giftAfterLock = giftMapper.selectById(gift.getId());
        assertEquals(1, successCount.get());
        assertEquals(1, notEnoughCount.get());
        assertEquals(1, giftAfterLock.getStockLocked());
        assertEquals(0, giftAfterLock.getStockUsed());
    }

    private ClubPointRedemptionBatchDO insertBatch(Integer status) {
        ClubPointRedemptionBatchDO batch = new ClubPointRedemptionBatchDO()
                .setYear(2026)
                .setName("2026 夏季兑换批次")
                .setStatus(status)
                .setOpenTime(BASE_TIME)
                .setCloseTime(BASE_TIME.plusDays(10))
                .setDescription("批次说明")
                .setMinAvailablePoints(50)
                .setQualifiedCount(180)
                .setIncludeTieAtCutoff(true)
                .setQualificationRuleJson("{\"min\":50}")
                .setSnapshotGenerated(false)
                .setRuleVersionId(6001L)
                .setRuleSnapshotJson("{\"rule\":\"redemption\"}");
        batchMapper.insert(batch);
        return batch;
    }

    private ClubPointRedemptionGiftDO insertGift(Long batchId, Integer status, Integer stockTotal,
                                                 Integer stockLocked, Integer stockUsed) {
        ClubPointRedemptionGiftDO gift = new ClubPointRedemptionGiftDO()
                .setBatchId(batchId)
                .setName("运动水杯")
                .setDescription("礼品说明")
                .setPointsCost(60)
                .setTierMinPoints(50)
                .setTierMaxPoints(100)
                .setReferenceAmountCent(1999L)
                .setStockTotal(stockTotal)
                .setStockLocked(stockLocked)
                .setStockUsed(stockUsed)
                .setStatus(status)
                .setImageFileId(3001L)
                .setSort(5)
                .setGiftSnapshotJson("{\"name\":\"运动水杯\"}");
        giftMapper.insert(gift);
        return gift;
    }

    private static ClubPointRedemptionGiftSaveReqBO buildSaveReq(Long id, Long batchId) {
        return new ClubPointRedemptionGiftSaveReqBO()
                .setId(id)
                .setBatchId(batchId)
                .setName("运动水杯")
                .setDescription("礼品说明")
                .setPointsCost(60)
                .setTierMinPoints(50)
                .setTierMaxPoints(100)
                .setReferenceAmountCent(1999L)
                .setStockTotal(10)
                .setImageFileId(3001L)
                .setSort(5)
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("维护礼品");
    }

    private static ClubPointRedemptionGiftOperationReqBO buildOperationReq() {
        return new ClubPointRedemptionGiftOperationReqBO()
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("调整礼品状态");
    }

}
