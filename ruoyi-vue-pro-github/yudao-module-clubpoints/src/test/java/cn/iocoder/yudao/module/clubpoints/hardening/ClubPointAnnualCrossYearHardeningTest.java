package cn.iocoder.yudao.module.clubpoints.hardening;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualClearingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointStockLockDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualClearingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualRankingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointFreezeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionEligibilitySnapshotMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionGiftMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionReviewRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointStockLockMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAnnualClearingStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionGiftStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionReviewResultEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointStockLockStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingConstants;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingService;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualRankingService;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualRankingServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearUserReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualRankingGenerateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointFreezeServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionApplicationService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionApplicationServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionEligibilityServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionGiftServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Import({
        ClubPointAnnualClearingServiceImpl.class,
        ClubPointAnnualRankingServiceImpl.class,
        ClubPointRedemptionApplicationServiceImpl.class,
        ClubPointRedemptionEligibilityServiceImpl.class,
        ClubPointRedemptionGiftServiceImpl.class,
        ClubPointFreezeServiceImpl.class,
        ClubPointLedgerServiceImpl.class,
        ClubPointRuleServiceImpl.class,
        ClubScopeServiceImpl.class,
        ClubPointAnnualCrossYearHardeningTest.TestAuditService.class,
        ClubPointAnnualCrossYearHardeningTest.TestNotifyService.class
})
class ClubPointAnnualCrossYearHardeningTest extends BaseDbUnitTest {

    private static final Integer YEAR = 2026;
    private static final Long USER_ID = 120501L;
    private static final Long REVIEWER_ID = 9001L;

    @Resource
    private ClubPointAnnualClearingService annualClearingService;
    @Resource
    private ClubPointAnnualRankingService annualRankingService;
    @Resource
    private ClubPointRedemptionApplicationService redemptionApplicationService;
    @Resource
    private ClubPointAnnualClearingRecordMapper clearingRecordMapper;
    @Resource
    private ClubPointAnnualRankingRecordMapper rankingRecordMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointFreezeMapper freezeMapper;
    @Resource
    private ClubPointRedemptionApplicationMapper applicationMapper;
    @Resource
    private ClubPointRedemptionGiftMapper giftMapper;
    @Resource
    private ClubPointRedemptionEligibilitySnapshotMapper eligibilitySnapshotMapper;
    @Resource
    private ClubPointStockLockMapper stockLockMapper;
    @Resource
    private ClubPointRedemptionReviewRecordMapper reviewRecordMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;

    @BeforeEach
    void setUp() {
        TestAuditService.reset();
        TestNotifyService.reset();
    }

    @Test
    void annualClearingShouldUseBeijingJanFirstClearOnlyAvailableAndKeepHistoricalTransactions() {
        insertPublishedRuleVersion("M12-5-CLEARING");
        insertAccount(USER_ID, 120, 0, 120, 30, 90);
        ClubPointTransactionDO historical = insertTransaction(1L, "CLUB-M12-A", "年度测试俱乐部A",
                120, ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(),
                ClubPointTransactionDirectionEnum.INCREASE.getDirection(),
                ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType(), "M12-HISTORY-001");

        LocalDateTime scheduledClearTime = ClubPointAnnualClearingConstants.buildScheduledClearTime(YEAR);
        assertEquals(ZoneId.of("Asia/Shanghai"), ClubPointAnnualClearingConstants.CLEARING_ZONE);
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), scheduledClearTime);

        Long recordId = annualClearingService.clearUser(new ClubPointAnnualClearUserReqBO()
                .setYear(YEAR)
                .setUserId(USER_ID)
                .setRunId(1205L)
                .setOperatorUserId(REVIEWER_ID)
                .setReason("年度清零"));

        ClubPointAnnualClearingRecordDO record = clearingRecordMapper.selectById(recordId);
        assertEquals(ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus(), record.getStatus());
        assertEquals(scheduledClearTime, record.getClearTime());
        assertEquals(120, record.getNetPointsBefore());
        assertEquals(30, record.getFrozenPointsBefore());
        assertEquals(90, record.getAvailablePointsBefore());
        assertEquals(90, record.getClearablePoints());

        ClubPointTransactionDO clearingTransaction = transactionMapper.selectById(record.getClearTransactionId());
        assertEquals(90, clearingTransaction.getPoints());
        assertEquals(ClubPointCategoryEnum.ANNUAL_CLEARING.getCategory(), clearingTransaction.getPointCategory());
        assertEquals(ClubPointTransactionSourceTypeEnum.ANNUAL_CLEARING.getType(),
                clearingTransaction.getSourceType());
        assertEquals(2026, clearingTransaction.getBusinessYear());
        assertEquals(202601, clearingTransaction.getBusinessMonth());

        ClubPointAccountDO account = accountMapper.selectByUserId(USER_ID);
        assertEquals(30, account.getNetPoints());
        assertEquals(30, account.getFrozenPoints());
        assertEquals(0, account.getAvailablePoints());
        assertEquals(120, account.getAnnualEarnedPoints());
        assertNotNull(transactionMapper.selectById(historical.getId()));
        assertEquals(2L, transactionMapper.selectCount());
    }

    @Test
    void crossYearRejectedRedemptionShouldReleaseBackWithoutExpiredClearingTransaction() {
        LocalDateTime frozenAt = LocalDateTime.of(2026, 12, 30, 10, 0);
        LocalDateTime rejectedAt = LocalDateTime.of(2027, 1, 3, 9, 0);
        RedemptionFixture fixture = insertPendingApplication("REQ-M12-5001", frozenAt);

        redemptionApplicationService.review(buildReviewReq(fixture.application.getId(), rejectedAt));

        ClubPointRedemptionApplicationDO application = applicationMapper.selectById(fixture.application.getId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.REJECTED.getStatus(), application.getStatus());
        assertEquals(rejectedAt, application.getReviewTime());
        assertNull(application.getDeductTransactionId());

        ClubPointFreezeDO freeze = freezeMapper.selectById(fixture.freeze.getId());
        assertEquals(ClubPointFreezeStatusEnum.RELEASED.getStatus(), freeze.getStatus());
        assertEquals(rejectedAt, freeze.getReleasedAt());
        assertEquals("跨年拒绝释放", freeze.getReleaseReason());

        ClubPointStockLockDO stockLock = stockLockMapper.selectById(fixture.stockLock.getId());
        assertEquals(ClubPointStockLockStatusEnum.RELEASED.getStatus(), stockLock.getStatus());
        assertEquals(rejectedAt, stockLock.getReleasedTime());

        ClubPointAccountDO account = accountMapper.selectByUserId(USER_ID);
        assertEquals(100, account.getNetPoints());
        assertEquals(0, account.getFrozenPoints());
        assertEquals(100, account.getAvailablePoints());
        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(0L, clearingRecordMapper.selectCount());
        assertEquals(1L, reviewRecordMapper.selectCount());
    }

    @Test
    void annualRankingShouldIgnoreRedemptionAndAnnualClearingDeductions() {
        insertPublishedRuleVersion("M12-5-RANKING");
        insertTransaction(1L, "CLUB-M12-A", "年度测试俱乐部A", 100,
                ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(),
                ClubPointTransactionDirectionEnum.INCREASE.getDirection(),
                ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType(), "M12-RANK-A-100");
        insertTransaction(1L, "CLUB-M12-A", "年度测试俱乐部A", 200,
                ClubPointCategoryEnum.REDEMPTION_DEDUCTION.getCategory(),
                ClubPointTransactionDirectionEnum.DECREASE.getDirection(),
                ClubPointTransactionSourceTypeEnum.REDEMPTION.getType(), "M12-RANK-A-RED");
        insertTransaction(1L, "CLUB-M12-A", "年度测试俱乐部A", 50,
                ClubPointCategoryEnum.ANNUAL_CLEARING.getCategory(),
                ClubPointTransactionDirectionEnum.DECREASE.getDirection(),
                ClubPointTransactionSourceTypeEnum.ANNUAL_CLEARING.getType(), "M12-RANK-A-CLEAR");
        insertTransaction(2L, "CLUB-M12-B", "年度测试俱乐部B", 90,
                ClubPointCategoryEnum.SPECIAL_REWARD.getCategory(),
                ClubPointTransactionDirectionEnum.INCREASE.getDirection(),
                ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType(), "M12-RANK-B-90");

        annualRankingService.generateRanking(new ClubPointAnnualRankingGenerateReqBO()
                .setYear(YEAR)
                .setGeneratedTime(LocalDateTime.of(2027, 1, 1, 3, 0)));

        ClubPointAnnualRankingRecordDO clubA = rankingRecordMapper.selectByYearAndClubCode(YEAR, "CLUB-M12-A");
        ClubPointAnnualRankingRecordDO clubB = rankingRecordMapper.selectByYearAndClubCode(YEAR, "CLUB-M12-B");
        assertNotNull(clubA);
        assertNotNull(clubB);
        assertEquals(100, clubA.getTotalIssuedPoints());
        assertEquals(1, clubA.getRankNo());
        assertEquals(90, clubB.getTotalIssuedPoints());
        assertEquals(2, clubB.getRankNo());
        assertEquals(4L, transactionMapper.selectCount());
    }

    private ClubPointRuleVersionDO insertPublishedRuleVersion(String versionNo) {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo(versionNo)
                .setName(versionNo)
                .setStatus(2)
                .setPublicityTime(LocalDateTime.of(2025, 12, 1, 0, 0))
                .setEffectiveTime(LocalDateTime.of(2025, 12, 1, 0, 0))
                .setPublishedTime(LocalDateTime.of(2025, 12, 1, 0, 0))
                .setSummary("summary")
                .setContent("content");
        ruleVersionMapper.insert(version);
        return version;
    }

    private void insertAccount(Long userId, Integer totalPositivePoints, Integer totalNegativePoints,
                               Integer netPoints, Integer frozenPoints, Integer availablePoints) {
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(totalPositivePoints)
                .setTotalNegativePoints(totalNegativePoints)
                .setNetPoints(netPoints)
                .setFrozenPoints(frozenPoints)
                .setAvailablePoints(availablePoints)
                .setAnnualEarnedPoints(totalPositivePoints)
                .setVersion(1));
    }

    private ClubPointTransactionDO insertTransaction(Long clubId, String clubCode, String clubName,
                                                     Integer points, Integer pointCategory, Integer direction,
                                                     Integer sourceType, String transactionNo) {
        ClubPointTransactionDO transaction = new ClubPointTransactionDO()
                .setTransactionNo(transactionNo)
                .setUserId(USER_ID)
                .setUserNameSnapshot("年度硬化用户")
                .setDirection(direction)
                .setPoints(points)
                .setPointCategory(pointCategory)
                .setPointTypeCode("M12_ANNUAL")
                .setStatus(ClubPointTransactionStatusEnum.VALID.getStatus())
                .setSourceType(sourceType)
                .setSourceId(1L)
                .setSourceTitleSnapshot("M12 年度硬化")
                .setIssuingClubId(clubId)
                .setIssuingClubCodeSnapshot(clubCode)
                .setIssuingClubNameSnapshot(clubName)
                .setRuleVersionId(1L)
                .setRuleItemCodeSnapshot("M12_ANNUAL")
                .setRuleSnapshotJson("{}")
                .setMaterialSummary("M12 年度硬化")
                .setReason("M12 年度硬化")
                .setOccurredAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .setBusinessYear(YEAR)
                .setBusinessMonth(YEAR * 100 + 6)
                .setIdempotencyKey("M12-ANNUAL:" + transactionNo);
        transactionMapper.insert(transaction);
        return transaction;
    }

    private RedemptionFixture insertPendingApplication(String requestNo, LocalDateTime frozenAt) {
        insertAccount(USER_ID, 100, 0, 100, 60, 40);
        ClubPointRedemptionGiftDO gift = insertGift();
        ClubPointRedemptionEligibilitySnapshotDO eligibilitySnapshot = new ClubPointRedemptionEligibilitySnapshotDO()
                .setBatchId(3001L)
                .setUserId(USER_ID)
                .setUserNameSnapshot("年度硬化用户")
                .setDeptNameSnapshot("运营部")
                .setNetPointsSnapshot(100)
                .setFrozenPointsSnapshot(0)
                .setAvailablePointsSnapshot(100)
                .setAnnualEarnedPointsSnapshot(100)
                .setRankNo(1)
                .setQualified(true)
                .setQualificationReason("满足资格规则")
                .setTieAtCutoff(false)
                .setGeneratedTime(frozenAt);
        eligibilitySnapshotMapper.insert(eligibilitySnapshot);

        ClubPointRedemptionApplicationDO application = new ClubPointRedemptionApplicationDO()
                .setApplicationNo("RDA-" + requestNo)
                .setRequestNo(requestNo)
                .setBatchId(3001L)
                .setGiftId(gift.getId())
                .setEligibilitySnapshotId(eligibilitySnapshot.getId())
                .setUserId(USER_ID)
                .setStatus(ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus())
                .setPointsCost(60)
                .setQuantity(1)
                .setQualificationRankSnapshot(1)
                .setBeforeNetPoints(100)
                .setBeforeFrozenPoints(0)
                .setBeforeAvailablePoints(100)
                .setBatchSnapshotJson("{\"name\":\"2026 年末兑换批次\"}")
                .setGiftSnapshotJson("{\"name\":\"跨年礼品\"}")
                .setApplyTime(frozenAt)
                .setIdempotencyKey("REDEMPTION_APPLY:3001:" + gift.getId() + ":" + USER_ID + ":" + requestNo);
        applicationMapper.insert(application);

        ClubPointFreezeDO freeze = new ClubPointFreezeDO()
                .setFreezeNo("RDF-" + requestNo)
                .setUserId(USER_ID)
                .setPoints(60)
                .setStatus(ClubPointFreezeStatusEnum.FROZEN.getStatus())
                .setSourceType(ClubPointFreezeSourceTypeEnum.REDEMPTION_APPLICATION.getType())
                .setSourceId(application.getId())
                .setFrozenAt(frozenAt)
                .setIdempotencyKey("RDF-" + requestNo);
        freezeMapper.insert(freeze);

        ClubPointStockLockDO stockLock = new ClubPointStockLockDO()
                .setGiftId(gift.getId())
                .setApplicationId(application.getId())
                .setUserId(USER_ID)
                .setQuantity(1)
                .setStatus(ClubPointStockLockStatusEnum.LOCKED.getStatus())
                .setLockedTime(frozenAt)
                .setIdempotencyKey("STOCK_LOCK:" + application.getId());
        stockLockMapper.insert(stockLock);

        application.setFreezeId(freeze.getId()).setStockLockId(stockLock.getId());
        applicationMapper.updateById(application);
        return new RedemptionFixture(application, freeze, stockLock);
    }

    private ClubPointRedemptionGiftDO insertGift() {
        ClubPointRedemptionGiftDO gift = new ClubPointRedemptionGiftDO()
                .setBatchId(3001L)
                .setName("跨年礼品")
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
                .setGiftSnapshotJson("{\"name\":\"跨年礼品\"}");
        giftMapper.insert(gift);
        return gift;
    }

    private static ClubPointRedemptionReviewReqBO buildReviewReq(Long applicationId, LocalDateTime reviewTime) {
        return new ClubPointRedemptionReviewReqBO()
                .setId(applicationId)
                .setResult(ClubPointRedemptionReviewResultEnum.REJECTED.getResult())
                .setReason("跨年拒绝释放")
                .setReviewTime(reviewTime)
                .setOperatorGlobalScope(true)
                .setOperatorUserId(REVIEWER_ID)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("系统管理员")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

    private static class RedemptionFixture {

        private final ClubPointRedemptionApplicationDO application;
        private final ClubPointFreezeDO freeze;
        private final ClubPointStockLockDO stockLock;

        RedemptionFixture(ClubPointRedemptionApplicationDO application, ClubPointFreezeDO freeze,
                          ClubPointStockLockDO stockLock) {
            this.application = application;
            this.freeze = freeze;
            this.stockLock = stockLock;
        }

    }

    static class TestAuditService implements ClubAuditService {

        private static long nextAuditId;

        static void reset() {
            nextAuditId = 120500L;
        }

        @Override
        public Long createAuditLog(ClubAuditCreateReqBO reqBO) {
            return nextAuditId++;
        }

    }

    static class TestNotifyService implements ClubNotifyService {

        static void reset() {
        }

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
