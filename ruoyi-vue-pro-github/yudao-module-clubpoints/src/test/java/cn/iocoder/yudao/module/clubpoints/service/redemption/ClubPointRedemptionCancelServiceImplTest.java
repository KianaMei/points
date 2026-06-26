package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointStockLockDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointFreezeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionEligibilitySnapshotMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionGiftMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointStockLockMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
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
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionTimeoutReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_APPLICATION_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@Import({ClubPointRedemptionApplicationServiceImpl.class, ClubPointRedemptionEligibilityServiceImpl.class,
        ClubPointRedemptionGiftServiceImpl.class, ClubPointFreezeServiceImpl.class, ClubScopeServiceImpl.class,
        ClubPointRedemptionCancelServiceImplTest.TestLedgerService.class,
        ClubPointRedemptionCancelServiceImplTest.TestAuditService.class,
        ClubPointRedemptionCancelServiceImplTest.TestNotifyService.class})
class ClubPointRedemptionCancelServiceImplTest extends BaseDbUnitTest {

    private static final Long USER_ID = 1001L;
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 8, 10, 10, 0);

    @Resource
    private ClubPointRedemptionApplicationService redemptionApplicationService;
    @Resource
    private ClubPointRedemptionApplicationMapper applicationMapper;
    @Resource
    private ClubPointRedemptionGiftMapper giftMapper;
    @Resource
    private ClubPointStockLockMapper stockLockMapper;
    @Resource
    private ClubPointFreezeMapper freezeMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointRedemptionEligibilitySnapshotMapper eligibilitySnapshotMapper;

    @Test
    void cancelOwnApplicationShouldReleaseFreezeAndStockWithoutCreatingTransaction() {
        RedemptionFixture fixture = insertPendingApplication(USER_ID, "REQ-M9-7001", BASE_TIME);

        redemptionApplicationService.cancelOwnApplication(new ClubPointRedemptionCancelReqBO()
                .setId(fixture.application.getId())
                .setUserId(USER_ID)
                .setReason("员工主动取消")
                .setCancelTime(BASE_TIME.plusHours(1)));

        ClubPointRedemptionApplicationDO application = applicationMapper.selectById(fixture.application.getId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.CANCELED_BEFORE_REVIEW.getStatus(), application.getStatus());
        assertEquals(BASE_TIME.plusHours(1), application.getCancelTime());
        assertEquals("员工主动取消", application.getCancelReason());
        assertNull(application.getDeductTransactionId());

        ClubPointFreezeDO freeze = freezeMapper.selectById(fixture.freeze.getId());
        assertEquals(ClubPointFreezeStatusEnum.RELEASED.getStatus(), freeze.getStatus());
        assertEquals("员工主动取消", freeze.getReleaseReason());

        ClubPointStockLockDO stockLock = stockLockMapper.selectById(fixture.stockLock.getId());
        assertEquals(ClubPointStockLockStatusEnum.RELEASED.getStatus(), stockLock.getStatus());
        assertEquals("员工主动取消", stockLock.getReleaseReason());

        ClubPointAccountDO account = accountMapper.selectByUserId(USER_ID);
        assertEquals(100, account.getNetPoints());
        assertEquals(0, account.getFrozenPoints());
        assertEquals(100, account.getAvailablePoints());
        assertEquals(0, giftMapper.selectById(fixture.gift.getId()).getStockLocked());
        assertEquals(0, giftMapper.selectById(fixture.gift.getId()).getStockUsed());
        assertEquals(0L, transactionMapper.selectCount());
    }

    @Test
    void cancelCrossYearFrozenApplicationShouldReleaseBackWithoutExpiredClearing() {
        LocalDateTime frozenAt = LocalDateTime.of(2026, 12, 30, 10, 0);
        LocalDateTime releasedAt = LocalDateTime.of(2027, 1, 3, 9, 0);
        RedemptionFixture fixture = insertPendingApplication(USER_ID, "REQ-M9-7006", frozenAt);
        ClubPointAccountDO accountAfterAnnualClearing = accountMapper.selectByUserId(USER_ID)
                .setTotalNegativePoints(40)
                .setNetPoints(60)
                .setFrozenPoints(60)
                .setAvailablePoints(0)
                .setAnnualEarnedPoints(100);
        accountMapper.updateById(accountAfterAnnualClearing);

        redemptionApplicationService.cancelOwnApplication(new ClubPointRedemptionCancelReqBO()
                .setId(fixture.application.getId())
                .setUserId(USER_ID)
                .setReason("跨年取消释放")
                .setCancelTime(releasedAt));

        ClubPointAccountDO account = accountMapper.selectByUserId(USER_ID);
        assertEquals(60, account.getNetPoints());
        assertEquals(0, account.getFrozenPoints());
        assertEquals(60, account.getAvailablePoints());
        assertEquals(0L, transactionMapper.selectCount());
        ClubPointFreezeDO freeze = freezeMapper.selectById(fixture.freeze.getId());
        assertEquals(ClubPointFreezeStatusEnum.RELEASED.getStatus(), freeze.getStatus());
        assertEquals(releasedAt, freeze.getReleasedAt());
        assertEquals("跨年取消释放", freeze.getReleaseReason());
        ClubPointStockLockDO stockLock = stockLockMapper.selectById(fixture.stockLock.getId());
        assertEquals(ClubPointStockLockStatusEnum.RELEASED.getStatus(), stockLock.getStatus());
        assertEquals(0, giftMapper.selectById(fixture.gift.getId()).getStockLocked());
    }

    @Test
    void cancelOwnApplicationShouldBeIdempotentForSameUser() {
        RedemptionFixture fixture = insertPendingApplication(USER_ID, "REQ-M9-7002", BASE_TIME);
        ClubPointRedemptionCancelReqBO reqBO = new ClubPointRedemptionCancelReqBO()
                .setId(fixture.application.getId())
                .setUserId(USER_ID)
                .setReason("员工主动取消")
                .setCancelTime(BASE_TIME.plusHours(1));

        redemptionApplicationService.cancelOwnApplication(reqBO);
        redemptionApplicationService.cancelOwnApplication(reqBO);

        ClubPointRedemptionApplicationDO application = applicationMapper.selectById(fixture.application.getId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.CANCELED_BEFORE_REVIEW.getStatus(), application.getStatus());
        assertEquals(0, accountMapper.selectByUserId(USER_ID).getFrozenPoints());
        assertEquals(0, giftMapper.selectById(fixture.gift.getId()).getStockLocked());
        assertEquals(0L, transactionMapper.selectCount());
    }

    @Test
    void cancelOwnApplicationShouldRejectOtherUserAndReviewedApplication() {
        RedemptionFixture fixture = insertPendingApplication(USER_ID, "REQ-M9-7003", BASE_TIME);

        assertServiceException(() -> redemptionApplicationService.cancelOwnApplication(new ClubPointRedemptionCancelReqBO()
                .setId(fixture.application.getId())
                .setUserId(USER_ID + 1)
                .setReason("越权取消")
                .setCancelTime(BASE_TIME.plusHours(1))), CLUB_SCOPE_DENIED);

        applicationMapper.updateById(new ClubPointRedemptionApplicationDO()
                .setId(fixture.application.getId())
                .setStatus(ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus()));
        assertServiceException(() -> redemptionApplicationService.cancelOwnApplication(new ClubPointRedemptionCancelReqBO()
                .setId(fixture.application.getId())
                .setUserId(USER_ID)
                .setReason("已审核后取消")
                .setCancelTime(BASE_TIME.plusHours(2))), CLUB_REDEMPTION_APPLICATION_STATUS_INVALID);
    }

    @Test
    void timeoutPendingApplicationsShouldReleaseExpiredPendingOnly() {
        RedemptionFixture expired = insertPendingApplication(USER_ID, "REQ-M9-7004", BASE_TIME.minusDays(2));
        RedemptionFixture fresh = insertPendingApplication(USER_ID + 1, "REQ-M9-7005", BASE_TIME.plusHours(2));

        int handled = redemptionApplicationService.timeoutPendingApplications(new ClubPointRedemptionTimeoutReqBO()
                .setOperatorGlobalScope(true)
                .setAppliedBefore(BASE_TIME)
                .setTimeoutTime(BASE_TIME.plusDays(1))
                .setReason("审核超时自动取消"));

        assertEquals(1, handled);
        ClubPointRedemptionApplicationDO expiredApplication = applicationMapper.selectById(expired.application.getId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.CANCELED_BEFORE_REVIEW.getStatus(),
                expiredApplication.getStatus());
        assertEquals("审核超时自动取消", expiredApplication.getCancelReason());
        assertEquals(ClubPointFreezeStatusEnum.RELEASED.getStatus(),
                freezeMapper.selectById(expired.freeze.getId()).getStatus());
        assertEquals(ClubPointStockLockStatusEnum.RELEASED.getStatus(),
                stockLockMapper.selectById(expired.stockLock.getId()).getStatus());
        assertEquals(0, giftMapper.selectById(expired.gift.getId()).getStockLocked());

        ClubPointRedemptionApplicationDO freshApplication = applicationMapper.selectById(fresh.application.getId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus(), freshApplication.getStatus());
        assertEquals(ClubPointFreezeStatusEnum.FROZEN.getStatus(), freezeMapper.selectById(fresh.freeze.getId()).getStatus());
        assertEquals(ClubPointStockLockStatusEnum.LOCKED.getStatus(), stockLockMapper.selectById(fresh.stockLock.getId()).getStatus());
        assertEquals(1, giftMapper.selectById(fresh.gift.getId()).getStockLocked());
        assertEquals(0L, transactionMapper.selectCount());
    }

    private RedemptionFixture insertPendingApplication(Long userId, String requestNo, LocalDateTime applyTime) {
        insertAccount(userId);
        ClubPointRedemptionGiftDO gift = insertGift();
        ClubPointRedemptionEligibilitySnapshotDO eligibilitySnapshot = insertEligibility(userId);
        ClubPointRedemptionApplicationDO application = new ClubPointRedemptionApplicationDO()
                .setApplicationNo("RDA-" + requestNo)
                .setRequestNo(requestNo)
                .setBatchId(3001L)
                .setGiftId(gift.getId())
                .setEligibilitySnapshotId(eligibilitySnapshot.getId())
                .setUserId(userId)
                .setStatus(ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus())
                .setPointsCost(60)
                .setQuantity(1)
                .setQualificationRankSnapshot(1)
                .setBeforeNetPoints(100)
                .setBeforeFrozenPoints(0)
                .setBeforeAvailablePoints(100)
                .setBatchSnapshotJson("{\"name\":\"2026 夏季兑换批次\"}")
                .setGiftSnapshotJson("{\"name\":\"运动水杯\"}")
                .setApplyTime(applyTime)
                .setIdempotencyKey("REDEMPTION_APPLY:3001:" + gift.getId() + ":" + userId + ":" + requestNo);
        applicationMapper.insert(application);

        ClubPointFreezeDO freeze = new ClubPointFreezeDO()
                .setFreezeNo("RDF-" + requestNo)
                .setUserId(userId)
                .setPoints(60)
                .setStatus(ClubPointFreezeStatusEnum.FROZEN.getStatus())
                .setSourceType(ClubPointFreezeSourceTypeEnum.REDEMPTION_APPLICATION.getType())
                .setSourceId(application.getId())
                .setFrozenAt(applyTime)
                .setIdempotencyKey("RDF-" + requestNo);
        freezeMapper.insert(freeze);

        ClubPointStockLockDO stockLock = new ClubPointStockLockDO()
                .setGiftId(gift.getId())
                .setApplicationId(application.getId())
                .setUserId(userId)
                .setQuantity(1)
                .setStatus(ClubPointStockLockStatusEnum.LOCKED.getStatus())
                .setLockedTime(applyTime)
                .setIdempotencyKey("STOCK_LOCK:" + application.getId());
        stockLockMapper.insert(stockLock);

        application.setFreezeId(freeze.getId()).setStockLockId(stockLock.getId());
        applicationMapper.updateById(application);
        return new RedemptionFixture(application, gift, freeze, stockLock);
    }

    private void insertAccount(Long userId) {
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(100)
                .setTotalNegativePoints(0)
                .setNetPoints(100)
                .setFrozenPoints(60)
                .setAvailablePoints(40)
                .setAnnualEarnedPoints(100)
                .setVersion(1));
    }

    private ClubPointRedemptionGiftDO insertGift() {
        ClubPointRedemptionGiftDO gift = new ClubPointRedemptionGiftDO()
                .setBatchId(3001L)
                .setName("运动水杯")
                .setDescription("礼品说明")
                .setPointsCost(60)
                .setTierMinPoints(50)
                .setTierMaxPoints(100)
                .setReferenceAmountCent(1999L)
                .setStockTotal(2)
                .setStockLocked(1)
                .setStockUsed(0)
                .setStatus(ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus())
                .setImageFileId(3001L)
                .setSort(1)
                .setGiftSnapshotJson("{\"name\":\"运动水杯\"}");
        giftMapper.insert(gift);
        return gift;
    }

    private ClubPointRedemptionEligibilitySnapshotDO insertEligibility(Long userId) {
        ClubPointRedemptionEligibilitySnapshotDO snapshot = new ClubPointRedemptionEligibilitySnapshotDO()
                .setBatchId(3001L)
                .setUserId(userId)
                .setUserNameSnapshot("员工" + userId)
                .setDeptNameSnapshot("运营部")
                .setNetPointsSnapshot(100)
                .setFrozenPointsSnapshot(0)
                .setAvailablePointsSnapshot(100)
                .setAnnualEarnedPointsSnapshot(100)
                .setRankNo(1)
                .setQualified(true)
                .setQualificationReason("满足资格规则")
                .setTieAtCutoff(false)
                .setGeneratedTime(BASE_TIME);
        eligibilitySnapshotMapper.insert(snapshot);
        return snapshot;
    }

    private static void assertServiceException(Runnable runnable, ErrorCode errorCode) {
        try {
            runnable.run();
            fail("Expected ServiceException");
        } catch (ServiceException ex) {
            assertEquals(errorCode.getCode(), ex.getCode());
            assertEquals(errorCode.getMsg(), ex.getMessage());
        }
    }

    private static class RedemptionFixture {

        private final ClubPointRedemptionApplicationDO application;
        private final ClubPointRedemptionGiftDO gift;
        private final ClubPointFreezeDO freeze;
        private final ClubPointStockLockDO stockLock;

        RedemptionFixture(ClubPointRedemptionApplicationDO application, ClubPointRedemptionGiftDO gift,
                          ClubPointFreezeDO freeze, ClubPointStockLockDO stockLock) {
            this.application = application;
            this.gift = gift;
            this.freeze = freeze;
            this.stockLock = stockLock;
        }

    }

    static class TestLedgerService implements ClubPointLedgerService {

        @Override
        public Long createTransaction(ClubPointLedgerCreateReqBO reqBO) {
            throw new UnsupportedOperationException("cancel should not create transaction");
        }

        @Override
        public Long reverseTransaction(ClubPointLedgerReverseReqBO reqBO) {
            throw new UnsupportedOperationException("cancel should not reverse transaction");
        }

        @Override
        public Long adjustPoints(ClubPointLedgerAdjustReqBO reqBO) {
            throw new UnsupportedOperationException("cancel should not adjust points");
        }

        @Override
        public Long rebuildUserAccount(ClubPointAccountRebuildReqBO reqBO) {
            return 0L;
        }

        @Override
        public Long rebuildAllAccounts(ClubPointAccountRebuildAllReqBO reqBO) {
            return 0L;
        }

    }

    static class TestAuditService implements ClubAuditService {

        @Override
        public Long createAuditLog(ClubAuditCreateReqBO reqBO) {
            throw new UnsupportedOperationException("cancel should not create audit");
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
