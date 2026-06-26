package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointStockLockDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointFreezeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionEligibilitySnapshotMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionGiftMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionReviewRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointStockLockMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionGiftStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionReviewResultEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointStockLockStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointFreezeServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.REDEMPTION_REVIEW;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_APPLICATION_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Import({ClubPointRedemptionApplicationServiceImpl.class, ClubPointRedemptionEligibilityServiceImpl.class,
        ClubPointRedemptionGiftServiceImpl.class, ClubPointFreezeServiceImpl.class, ClubPointLedgerServiceImpl.class,
        ClubPointRuleServiceImpl.class, ClubScopeServiceImpl.class,
        ClubPointRedemptionReviewServiceImplTest.TestAuditService.class,
        ClubPointRedemptionReviewServiceImplTest.TestNotifyService.class})
class ClubPointRedemptionReviewServiceImplTest extends BaseDbUnitTest {

    private static final Long USER_ID = 1001L;
    private static final Long REVIEWER_ID = 9001L;
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 8, 5, 10, 0);
    private static final Integer STATUS_PUBLISHED = 2;
    private static final Integer RULE_ITEM_ENABLED = 1;

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
    private ClubPointRedemptionReviewRecordMapper reviewRecordMapper;
    @Resource
    private ClubPointRedemptionEligibilitySnapshotMapper eligibilitySnapshotMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;

    private long nextUserOffset;

    @BeforeEach
    void setUp() {
        TestAuditService.reset();
        TestNotifyService.reset();
        nextUserOffset = 0;
    }

    @Test
    void listPendingReviewApplicationsShouldRequireGlobalScopeAndReturnPendingOnly() {
        RedemptionFixture pending = insertPendingApplication("REQ-M9-6001");
        RedemptionFixture approved = insertPendingApplication("REQ-M9-6002");
        applicationMapper.updateById(new ClubPointRedemptionApplicationDO()
                .setId(approved.application.getId())
                .setStatus(ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus()));

        assertServiceException(() -> redemptionApplicationService.listPendingReviewApplications(false),
                CLUB_SCOPE_DENIED);

        List<ClubPointRedemptionApplicationDO> applications =
                redemptionApplicationService.listPendingReviewApplications(true);
        assertEquals(1, applications.size());
        assertEquals(pending.application.getId(), applications.get(0).getId());
    }

    @Test
    void approveShouldConvertFreezeUseStockCreateTransactionAuditRecordAndNotify() {
        insertPublishedRedemptionRule();
        RedemptionFixture fixture = insertPendingApplication("REQ-M9-6003");

        redemptionApplicationService.review(buildReviewReq(fixture.application.getId(),
                ClubPointRedemptionReviewResultEnum.APPROVED.getResult(), "审核通过"));

        ClubPointRedemptionApplicationDO application = applicationMapper.selectById(fixture.application.getId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus(), application.getStatus());
        assertEquals(REVIEWER_ID, application.getReviewerUserId());
        assertEquals("审核通过", application.getReviewReason());
        assertNotNull(application.getReviewTime());
        assertNotNull(application.getDirectIssueTime());
        assertNotNull(application.getDeductTransactionId());
        assertEquals(40, application.getAfterNetPoints());
        assertEquals(0, application.getAfterFrozenPoints());
        assertEquals(40, application.getAfterAvailablePoints());

        ClubPointFreezeDO freeze = freezeMapper.selectById(fixture.freeze.getId());
        assertEquals(ClubPointFreezeStatusEnum.CONVERTED.getStatus(), freeze.getStatus());
        assertEquals(application.getDeductTransactionId(), freeze.getConvertedTransactionId());

        ClubPointStockLockDO stockLock = stockLockMapper.selectById(fixture.stockLock.getId());
        assertEquals(ClubPointStockLockStatusEnum.USED.getStatus(), stockLock.getStatus());
        assertNotNull(stockLock.getUsedTime());

        ClubPointRedemptionGiftDO gift = giftMapper.selectById(fixture.gift.getId());
        assertEquals(0, gift.getStockLocked());
        assertEquals(1, gift.getStockUsed());

        ClubPointTransactionDO transaction = transactionMapper.selectById(application.getDeductTransactionId());
        assertEquals("REDEMPTION_APPROVE:" + application.getId(), transaction.getIdempotencyKey());
        assertEquals(ClubPointTransactionDirectionEnum.DECREASE.getDirection(), transaction.getDirection());
        assertEquals(ClubPointCategoryEnum.REDEMPTION_DEDUCTION.getCategory(), transaction.getPointCategory());
        assertEquals(ClubPointTransactionSourceTypeEnum.REDEMPTION.getType(), transaction.getSourceType());

        List<ClubPointRedemptionReviewRecordDO> records =
                reviewRecordMapper.selectListByApplicationId(application.getId());
        assertEquals(1, records.size());
        assertEquals(ClubPointRedemptionReviewResultEnum.APPROVED.getResult(), records.get(0).getResult());
        assertTrue(records.get(0).getApplicationSnapshotJson().contains(application.getApplicationNo()));
        assertEquals(REDEMPTION_REVIEW, TestAuditService.requests.get(0).getActionType());
        assertEquals(1, TestNotifyService.redemptionNotifications.size());
    }

    @Test
    void rejectShouldReleaseFreezeAndStockWithoutCreatingTransaction() {
        RedemptionFixture fixture = insertPendingApplication("REQ-M9-6004");

        redemptionApplicationService.review(buildReviewReq(fixture.application.getId(),
                ClubPointRedemptionReviewResultEnum.REJECTED.getResult(), "库存损坏"));

        ClubPointRedemptionApplicationDO application = applicationMapper.selectById(fixture.application.getId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.REJECTED.getStatus(), application.getStatus());
        assertEquals("库存损坏", application.getReviewReason());
        assertNull(application.getDeductTransactionId());

        ClubPointFreezeDO freeze = freezeMapper.selectById(fixture.freeze.getId());
        assertEquals(ClubPointFreezeStatusEnum.RELEASED.getStatus(), freeze.getStatus());
        assertEquals("库存损坏", freeze.getReleaseReason());

        ClubPointStockLockDO stockLock = stockLockMapper.selectById(fixture.stockLock.getId());
        assertEquals(ClubPointStockLockStatusEnum.RELEASED.getStatus(), stockLock.getStatus());
        assertEquals("库存损坏", stockLock.getReleaseReason());

        ClubPointAccountDO account = accountMapper.selectByUserId(USER_ID);
        assertEquals(100, account.getNetPoints());
        assertEquals(0, account.getFrozenPoints());
        assertEquals(100, account.getAvailablePoints());
        assertEquals(0, giftMapper.selectById(fixture.gift.getId()).getStockLocked());
        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(1, TestNotifyService.redemptionNotifications.size());
    }

    @Test
    void reviewShouldReturnWhenSameResultSubmittedAgainWithoutDuplicateDeduction() {
        insertPublishedRedemptionRule();
        RedemptionFixture fixture = insertPendingApplication("REQ-M9-6005");
        ClubPointRedemptionReviewReqBO reqBO = buildReviewReq(fixture.application.getId(),
                ClubPointRedemptionReviewResultEnum.APPROVED.getResult(), "审核通过");

        redemptionApplicationService.review(reqBO);
        redemptionApplicationService.review(reqBO);

        ClubPointRedemptionApplicationDO application = applicationMapper.selectById(fixture.application.getId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus(), application.getStatus());
        assertEquals(1L, transactionMapper.selectCount());
        assertEquals(1, reviewRecordMapper.selectListByApplicationId(application.getId()).size());
        assertEquals(0, giftMapper.selectById(fixture.gift.getId()).getStockLocked());
        assertEquals(1, giftMapper.selectById(fixture.gift.getId()).getStockUsed());
    }

    @Test
    void auditFailureShouldRollbackApproval() {
        insertPublishedRedemptionRule();
        RedemptionFixture fixture = insertPendingApplication("REQ-M9-6006");
        TestAuditService.fail = true;

        assertServiceException(() -> redemptionApplicationService.review(buildReviewReq(fixture.application.getId(),
                ClubPointRedemptionReviewResultEnum.APPROVED.getResult(), "审核通过")), CLUB_AUDIT_WRITE_FAILED);

        ClubPointRedemptionApplicationDO application = applicationMapper.selectById(fixture.application.getId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus(), application.getStatus());
        assertNull(application.getDeductTransactionId());
        assertEquals(ClubPointFreezeStatusEnum.FROZEN.getStatus(), freezeMapper.selectById(fixture.freeze.getId()).getStatus());
        assertEquals(ClubPointStockLockStatusEnum.LOCKED.getStatus(),
                stockLockMapper.selectById(fixture.stockLock.getId()).getStatus());
        assertEquals(1, giftMapper.selectById(fixture.gift.getId()).getStockLocked());
        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(0L, reviewRecordMapper.selectCount());
    }

    @Test
    void notifyFailureShouldNotRollbackReview() {
        RedemptionFixture fixture = insertPendingApplication("REQ-M9-6007");
        TestNotifyService.fail = true;

        redemptionApplicationService.review(buildReviewReq(fixture.application.getId(),
                ClubPointRedemptionReviewResultEnum.REJECTED.getResult(), "库存损坏"));

        ClubPointRedemptionApplicationDO application = applicationMapper.selectById(fixture.application.getId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.REJECTED.getStatus(), application.getStatus());
        assertEquals(ClubPointFreezeStatusEnum.RELEASED.getStatus(), freezeMapper.selectById(fixture.freeze.getId()).getStatus());
        assertEquals(ClubPointStockLockStatusEnum.RELEASED.getStatus(),
                stockLockMapper.selectById(fixture.stockLock.getId()).getStatus());
    }

    @Test
    void rejectShouldRequireReason() {
        RedemptionFixture fixture = insertPendingApplication("REQ-M9-6008");

        assertServiceException(() -> redemptionApplicationService.review(buildReviewReq(fixture.application.getId(),
                ClubPointRedemptionReviewResultEnum.REJECTED.getResult(), "")),
                CLUB_REDEMPTION_APPLICATION_STATUS_INVALID);
    }

    private RedemptionFixture insertPendingApplication(String requestNo) {
        Long userId = USER_ID + nextUserOffset++;
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
                .setApplyTime(BASE_TIME)
                .setIdempotencyKey("REDEMPTION_APPLY:3001:" + gift.getId() + ":" + userId + ":" + requestNo);
        applicationMapper.insert(application);

        ClubPointFreezeDO freeze = new ClubPointFreezeDO()
                .setFreezeNo("RDF-" + requestNo)
                .setUserId(userId)
                .setPoints(60)
                .setStatus(ClubPointFreezeStatusEnum.FROZEN.getStatus())
                .setSourceType(ClubPointFreezeSourceTypeEnum.REDEMPTION_APPLICATION.getType())
                .setSourceId(application.getId())
                .setFrozenAt(BASE_TIME)
                .setIdempotencyKey("RDF-" + requestNo);
        freezeMapper.insert(freeze);

        ClubPointStockLockDO stockLock = new ClubPointStockLockDO()
                .setGiftId(gift.getId())
                .setApplicationId(application.getId())
                .setUserId(userId)
                .setQuantity(1)
                .setStatus(ClubPointStockLockStatusEnum.LOCKED.getStatus())
                .setLockedTime(BASE_TIME)
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

    private void insertPublishedRedemptionRule() {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("V-M9-6")
                .setName("M9.6 规则")
                .setStatus(STATUS_PUBLISHED)
                .setPublicityTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setEffectiveTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setPublishedTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setSummary("summary")
                .setContent("content");
        ruleVersionMapper.insert(version);

        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(version.getId())
                .setItemCode(ClubPointRuleItemCodeEnum.REDEMPTION_MIN_POINTS.getCode())
                .setItemName("兑换最低可用积分")
                .setItemType(1)
                .setCategory(ClubPointCategoryEnum.REDEMPTION_DEDUCTION.getCategory())
                .setMinPoints(1)
                .setMaxPoints(100)
                .setDefaultPoints(50)
                .setStatus(RULE_ITEM_ENABLED)
                .setSort(1));
    }

    private static ClubPointRedemptionReviewReqBO buildReviewReq(Long applicationId, Integer result, String reason) {
        return new ClubPointRedemptionReviewReqBO()
                .setId(applicationId)
                .setResult(result)
                .setReason(reason)
                .setOperatorGlobalScope(true)
                .setOperatorUserId(REVIEWER_ID)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("系统管理员")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
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

    static class TestAuditService implements ClubAuditService {

        static boolean fail;
        static long nextAuditId;
        static List<ClubAuditCreateReqBO> requests = new ArrayList<>();

        static void reset() {
            fail = false;
            nextAuditId = 10000L;
            requests = new ArrayList<>();
        }

        @Override
        public Long createAuditLog(ClubAuditCreateReqBO reqBO) {
            if (fail) {
                throw exception(CLUB_AUDIT_WRITE_FAILED);
            }
            requests.add(reqBO);
            return nextAuditId++;
        }

    }

    static class TestNotifyService implements ClubNotifyService {

        static boolean fail;
        static List<String> redemptionNotifications = new ArrayList<>();

        static void reset() {
            fail = false;
            redemptionNotifications = new ArrayList<>();
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
            if (fail) {
                throw new IllegalStateException("notify failed");
            }
            redemptionNotifications.add(userId + ":" + applicationNo + ":" + result + ":" + reason);
        }

        @Override
        public void notifyDisputeReplied(Long userId, String title, String replyContent) {
        }

    }

}
