package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointStockLockDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointFreezeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionEligibilitySnapshotMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionGiftMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointStockLockMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionBatchStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionGiftStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointStockLockStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointFreezeServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountRebuildAllReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountRebuildReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerAdjustReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionApplyReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_GIFT_STOCK_NOT_ENOUGH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointRedemptionApplicationServiceImpl.class, ClubPointRedemptionEligibilityServiceImpl.class,
        ClubPointRedemptionGiftServiceImpl.class, ClubPointFreezeServiceImpl.class, ClubScopeServiceImpl.class,
        ClubPointRedemptionApplicationServiceImplTest.TestLedgerService.class,
        ClubPointRedemptionApplicationServiceImplTest.TestAuditService.class,
        ClubPointRedemptionApplicationServiceImplTest.TestNotifyService.class})
class ClubPointRedemptionApplicationServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 8, 1, 9, 0);
    private static final Long USER_ID = 1001L;

    @Resource
    private ClubPointRedemptionApplicationService redemptionApplicationService;
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
    private ClubPointFreezeMapper freezeMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;

    @Test
    void listAvailableGiftsShouldValidateBatchAndQualificationAndReturnOnShelfGifts() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        insertEligibility(batch.getId(), USER_ID, true, 1);
        ClubPointRedemptionGiftDO onShelf = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 60, 10, 0, 0, 1);
        insertGift(batch.getId(), ClubPointRedemptionGiftStatusEnum.OFF_SHELF.getStatus(), 80, 10, 0, 0, 2);

        List<ClubPointRedemptionGiftDO> gifts =
                redemptionApplicationService.listAvailableGifts(batch.getId(), USER_ID);

        assertEquals(1, gifts.size());
        assertEquals(onShelf.getId(), gifts.get(0).getId());
    }

    @Test
    void listAvailableGiftsShouldReturnEmptyWhenUserHasNoEligibilitySnapshot() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        insertGift(batch.getId(), ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 60, 10, 0, 0, 1);

        List<ClubPointRedemptionGiftDO> gifts =
                redemptionApplicationService.listAvailableGifts(batch.getId(), USER_ID);

        assertTrue(gifts.isEmpty());
    }

    @Test
    void listAvailableGiftsShouldReturnEmptyWhenUserIsNotQualified() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        insertEligibility(batch.getId(), USER_ID, false, 201);
        insertGift(batch.getId(), ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 60, 10, 0, 0, 1);

        List<ClubPointRedemptionGiftDO> gifts =
                redemptionApplicationService.listAvailableGifts(batch.getId(), USER_ID);

        assertTrue(gifts.isEmpty());
    }

    @Test
    void applyShouldFreezePointsLockStockAndCreatePendingApplication() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        insertEligibility(batch.getId(), USER_ID, true, 7);
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 60, 2, 0, 0, 1);
        insertAccount(USER_ID, 100, 0);

        Long applicationId = redemptionApplicationService.apply(buildApplyReq(batch.getId(), gift.getId(), "REQ-M9-5001"));

        ClubPointRedemptionApplicationDO application = applicationMapper.selectById(applicationId);
        assertEquals("REQ-M9-5001", application.getRequestNo());
        assertEquals(batch.getId(), application.getBatchId());
        assertEquals(gift.getId(), application.getGiftId());
        assertEquals(USER_ID, application.getUserId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus(), application.getStatus());
        assertEquals(60, application.getPointsCost());
        assertEquals(1, application.getQuantity());
        assertNotNull(application.getFreezeId());
        assertNotNull(application.getStockLockId());
        assertEquals(7, application.getQualificationRankSnapshot());
        assertEquals(100, application.getBeforeNetPoints());
        assertEquals(0, application.getBeforeFrozenPoints());
        assertEquals(100, application.getBeforeAvailablePoints());
        assertTrue(application.getBatchSnapshotJson().contains("\"name\":\"2026 夏季兑换批次\""));
        assertTrue(application.getGiftSnapshotJson().contains("\"name\":\"运动水杯\""));

        ClubPointFreezeDO freeze = freezeMapper.selectById(application.getFreezeId());
        assertEquals(ClubPointFreezeStatusEnum.FROZEN.getStatus(), freeze.getStatus());
        assertEquals(ClubPointFreezeSourceTypeEnum.REDEMPTION_APPLICATION.getType(), freeze.getSourceType());
        assertEquals(applicationId, freeze.getSourceId());
        assertEquals(60, freeze.getPoints());

        ClubPointStockLockDO stockLock = stockLockMapper.selectById(application.getStockLockId());
        assertEquals(gift.getId(), stockLock.getGiftId());
        assertEquals(applicationId, stockLock.getApplicationId());
        assertEquals(USER_ID, stockLock.getUserId());
        assertEquals(1, stockLock.getQuantity());
        assertEquals(ClubPointStockLockStatusEnum.LOCKED.getStatus(), stockLock.getStatus());

        ClubPointAccountDO account = accountMapper.selectByUserId(USER_ID);
        assertEquals(100, account.getNetPoints());
        assertEquals(60, account.getFrozenPoints());
        assertEquals(40, account.getAvailablePoints());
        ClubPointRedemptionGiftDO lockedGift = giftMapper.selectById(gift.getId());
        assertEquals(1, lockedGift.getStockLocked());
        assertEquals(0, lockedGift.getStockUsed());
    }

    @Test
    void applyShouldReturnExistingWhenSameRequestSubmittedAgain() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        insertEligibility(batch.getId(), USER_ID, true, 1);
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 60, 2, 0, 0, 1);
        insertAccount(USER_ID, 100, 0);
        ClubPointRedemptionApplyReqBO reqBO = buildApplyReq(batch.getId(), gift.getId(), "REQ-M9-5002");

        Long firstId = redemptionApplicationService.apply(reqBO);
        Long secondId = redemptionApplicationService.apply(reqBO);

        assertEquals(firstId, secondId);
        assertEquals(1L, applicationMapper.selectCount());
        assertEquals(1L, freezeMapper.selectCount());
        assertEquals(1L, stockLockMapper.selectCount());
        assertEquals(1, giftMapper.selectById(gift.getId()).getStockLocked());
        assertEquals(60, accountMapper.selectByUserId(USER_ID).getFrozenPoints());
    }

    @Test
    void applyShouldRollbackFreezeAndApplicationWhenStockNotEnough() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        insertEligibility(batch.getId(), USER_ID, true, 1);
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 60, 0, 0, 0, 1);
        insertAccount(USER_ID, 100, 0);

        assertServiceException(() -> redemptionApplicationService.apply(buildApplyReq(batch.getId(), gift.getId(),
                "REQ-M9-5003")), CLUB_REDEMPTION_GIFT_STOCK_NOT_ENOUGH);

        assertEquals(0L, applicationMapper.selectCount());
        assertEquals(0L, freezeMapper.selectCount());
        assertEquals(0L, stockLockMapper.selectCount());
        assertEquals(0, giftMapper.selectById(gift.getId()).getStockLocked());
        ClubPointAccountDO account = accountMapper.selectByUserId(USER_ID);
        assertEquals(0, account.getFrozenPoints());
        assertEquals(100, account.getAvailablePoints());
    }

    @Test
    void applyShouldRollbackApplicationWhenAvailablePointsNotEnough() {
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        insertEligibility(batch.getId(), USER_ID, true, 1);
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 60, 1, 0, 0, 1);
        insertAccount(USER_ID, 50, 0);

        assertServiceException(() -> redemptionApplicationService.apply(buildApplyReq(batch.getId(), gift.getId(),
                "REQ-M9-5004")), CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH);

        assertEquals(0L, applicationMapper.selectCount());
        assertEquals(0L, freezeMapper.selectCount());
        assertEquals(0L, stockLockMapper.selectCount());
        assertEquals(0, giftMapper.selectById(gift.getId()).getStockLocked());
        ClubPointAccountDO account = accountMapper.selectByUserId(USER_ID);
        assertEquals(0, account.getFrozenPoints());
        assertEquals(50, account.getAvailablePoints());
    }

    @Test
    void concurrentApplyShouldNotOversellAndRollbackFailedApplicant() throws Exception {
        Long competitorUserId = USER_ID + 1;
        ClubPointRedemptionBatchDO batch = insertBatch(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus());
        insertEligibility(batch.getId(), USER_ID, true, 1);
        insertEligibility(batch.getId(), competitorUserId, true, 2);
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(),
                ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), 60, 1, 0, 0, 1);
        insertAccount(USER_ID, 100, 0);
        insertAccount(competitorUserId, 100, 0);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger stockNotEnoughCount = new AtomicInteger();
        Queue<Throwable> unexpectedErrors = new ConcurrentLinkedQueue<>();

        executorService.submit(() -> applyConcurrently(startLatch, successCount, stockNotEnoughCount,
                unexpectedErrors, batch.getId(), gift.getId(), USER_ID, "REQ-M9-5005-A"));
        executorService.submit(() -> applyConcurrently(startLatch, successCount, stockNotEnoughCount,
                unexpectedErrors, batch.getId(), gift.getId(), competitorUserId, "REQ-M9-5005-B"));

        startLatch.countDown();
        executorService.shutdown();
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
        assertTrue(unexpectedErrors.isEmpty(), unexpectedErrors.toString());

        ClubPointRedemptionGiftDO giftAfterApply = giftMapper.selectById(gift.getId());
        List<ClubPointAccountDO> accounts = Arrays.asList(accountMapper.selectByUserId(USER_ID),
                accountMapper.selectByUserId(competitorUserId));
        assertEquals(1, successCount.get());
        assertEquals(1, stockNotEnoughCount.get());
        assertEquals(1L, applicationMapper.selectCount());
        assertEquals(1L, freezeMapper.selectCount());
        assertEquals(1L, stockLockMapper.selectCount());
        assertEquals(1, giftAfterApply.getStockLocked());
        assertEquals(0, giftAfterApply.getStockUsed());
        assertEquals(1, accounts.stream()
                .filter(account -> account.getFrozenPoints() == 60 && account.getAvailablePoints() == 40)
                .count());
        assertEquals(1, accounts.stream()
                .filter(account -> account.getFrozenPoints() == 0 && account.getAvailablePoints() == 100)
                .count());
    }

    private void applyConcurrently(CountDownLatch startLatch, AtomicInteger successCount,
                                   AtomicInteger stockNotEnoughCount, Queue<Throwable> unexpectedErrors,
                                   Long batchId, Long giftId, Long userId, String requestNo) {
        try {
            startLatch.await(5, TimeUnit.SECONDS);
            redemptionApplicationService.apply(buildApplyReq(batchId, giftId, requestNo)
                    .setUserId(userId));
            successCount.incrementAndGet();
        } catch (ServiceException ex) {
            if (CLUB_REDEMPTION_GIFT_STOCK_NOT_ENOUGH.getCode() == ex.getCode()) {
                stockNotEnoughCount.incrementAndGet();
            } else {
                unexpectedErrors.add(ex);
            }
        } catch (Throwable ex) {
            unexpectedErrors.add(ex);
        }
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
                .setSnapshotGenerated(true)
                .setSnapshotGeneratedTime(BASE_TIME)
                .setRuleVersionId(6001L)
                .setRuleSnapshotJson("{\"rule\":\"redemption\"}");
        batchMapper.insert(batch);
        return batch;
    }

    private ClubPointRedemptionGiftDO insertGift(Long batchId, Integer status, Integer pointsCost,
                                                 Integer stockTotal, Integer stockLocked, Integer stockUsed,
                                                 Integer sort) {
        ClubPointRedemptionGiftDO gift = new ClubPointRedemptionGiftDO()
                .setBatchId(batchId)
                .setName("运动水杯")
                .setDescription("礼品说明")
                .setPointsCost(pointsCost)
                .setTierMinPoints(50)
                .setTierMaxPoints(100)
                .setReferenceAmountCent(1999L)
                .setStockTotal(stockTotal)
                .setStockLocked(stockLocked)
                .setStockUsed(stockUsed)
                .setStatus(status)
                .setImageFileId(3001L)
                .setSort(sort)
                .setGiftSnapshotJson("{\"name\":\"运动水杯\"}");
        giftMapper.insert(gift);
        return gift;
    }

    private ClubPointRedemptionEligibilitySnapshotDO insertEligibility(Long batchId, Long userId,
                                                                       Boolean qualified, Integer rankNo) {
        ClubPointRedemptionEligibilitySnapshotDO snapshot = new ClubPointRedemptionEligibilitySnapshotDO()
                .setBatchId(batchId)
                .setUserId(userId)
                .setUserNameSnapshot("员工" + userId)
                .setDeptNameSnapshot("运营部")
                .setNetPointsSnapshot(100)
                .setFrozenPointsSnapshot(0)
                .setAvailablePointsSnapshot(100)
                .setAnnualEarnedPointsSnapshot(100)
                .setRankNo(rankNo)
                .setQualified(qualified)
                .setQualificationReason(Boolean.TRUE.equals(qualified) ? "满足资格规则" : "低于最低可用积分")
                .setTieAtCutoff(false)
                .setGeneratedTime(BASE_TIME);
        eligibilitySnapshotMapper.insert(snapshot);
        return snapshot;
    }

    private void insertAccount(Long userId, Integer netPoints, Integer frozenPoints) {
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(netPoints)
                .setTotalNegativePoints(0)
                .setNetPoints(netPoints)
                .setFrozenPoints(frozenPoints)
                .setAvailablePoints(Math.max(netPoints - frozenPoints, 0))
                .setAnnualEarnedPoints(netPoints)
                .setVersion(1));
    }

    private static ClubPointRedemptionApplyReqBO buildApplyReq(Long batchId, Long giftId, String requestNo) {
        return new ClubPointRedemptionApplyReqBO()
                .setBatchId(batchId)
                .setGiftId(giftId)
                .setUserId(USER_ID)
                .setQuantity(1)
                .setRequestNo(requestNo)
                .setApplyTime(BASE_TIME.plusHours(1))
                .setRemark("申请兑换");
    }

    static class TestLedgerService implements ClubPointLedgerService {

        @Override
        public Long createTransaction(ClubPointLedgerCreateReqBO reqBO) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long reverseTransaction(ClubPointLedgerReverseReqBO reqBO) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long adjustPoints(ClubPointLedgerAdjustReqBO reqBO) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long rebuildUserAccount(ClubPointAccountRebuildReqBO reqBO) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long rebuildAllAccounts(ClubPointAccountRebuildAllReqBO reqBO) {
            throw new UnsupportedOperationException();
        }

    }

    static class TestAuditService implements ClubAuditService {

        @Override
        public Long createAuditLog(ClubAuditCreateReqBO reqBO) {
            return 1L;
        }

    }

    static class TestNotifyService implements ClubNotifyService {

        @Override
        public void notifyActivityReviewResult(Long userId, String activityTitle, String result, String reason) {
        }

        @Override
        public void notifyPointsChanged(Long userId, String reason, String direction, Integer points,
                                        Integer availablePoints) {
        }

        @Override
        public void notifyRedemptionReviewResult(Long userId, String applicationNo, String result, String reason) {
        }

        @Override
        public void notifyDisputeReplied(Long userId, String title, String replyContent) {
        }

    }

}
