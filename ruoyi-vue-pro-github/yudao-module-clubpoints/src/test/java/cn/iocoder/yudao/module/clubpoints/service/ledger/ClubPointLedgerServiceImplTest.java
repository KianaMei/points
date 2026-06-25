package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_TRANSACTION_DUPLICATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Import({ClubPointLedgerServiceImpl.class, ClubPointRuleServiceImpl.class, ClubAuditServiceImpl.class})
class ClubPointLedgerServiceImplTest extends BaseDbUnitTest {

    private static final Integer STATUS_PUBLISHED = 2;
    private static final Integer RULE_ITEM_ENABLED = 1;

    @Resource
    private ClubPointLedgerService clubPointLedgerService;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;

    @Test
    void createPositiveTransactionShouldPersistSnapshotAndIncreaseAccount() {
        insertPublishedRule(5, 10, 5);

        Long transactionId = clubPointLedgerService.createTransaction(buildPositiveReq("TX-M4-3001", "IDEMP-M4-3001", 8));

        ClubPointTransactionDO transaction = transactionMapper.selectById(transactionId);
        assertNotNull(transaction);
        assertEquals("TX-M4-3001", transaction.getTransactionNo());
        assertEquals(100L, transaction.getUserId());
        assertEquals("员工A", transaction.getUserNameSnapshot());
        assertEquals(ClubPointTransactionDirectionEnum.INCREASE.getDirection(), transaction.getDirection());
        assertEquals(8, transaction.getPoints());
        assertEquals(ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(), transaction.getPointCategory());
        assertEquals(ClubPointTransactionStatusEnum.VALID.getStatus(), transaction.getStatus());
        assertEquals(ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType(), transaction.getSourceType());
        assertEquals(2026, transaction.getBusinessYear());
        assertEquals(202606, transaction.getBusinessMonth());
        assertEquals(ClubPointRuleItemCodeEnum.ACTIVITY_SMALL_BASE.getCode(), transaction.getRuleItemCodeSnapshot());
        assertTrue(transaction.getRuleSnapshotJson().contains("\"pointsSnapshot\":8"));
        assertEquals("{\"activityId\":300}", transaction.getSnapshotJson());

        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertNotNull(account);
        assertEquals(8, account.getTotalPositivePoints());
        assertEquals(0, account.getTotalNegativePoints());
        assertEquals(8, account.getNetPoints());
        assertEquals(0, account.getFrozenPoints());
        assertEquals(8, account.getAvailablePoints());
        assertEquals(8, account.getAnnualEarnedPoints());
        assertEquals(transactionId, account.getLastTransactionId());
        assertEquals(LocalDateTime.of(2026, 6, 1, 10, 0), account.getLastTransactionTime());
    }

    @Test
    void createNegativeTransactionShouldDecreaseExistingAccount() {
        insertPublishedRule(1, 20, 5);
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(100L)
                .setTotalPositivePoints(30)
                .setTotalNegativePoints(0)
                .setNetPoints(30)
                .setFrozenPoints(5)
                .setAvailablePoints(25)
                .setAnnualEarnedPoints(30)
                .setVersion(1));

        Long transactionId = clubPointLedgerService.createTransaction(buildNegativeReq("TX-M4-3002", "IDEMP-M4-3002", 6));

        ClubPointTransactionDO transaction = transactionMapper.selectById(transactionId);
        assertEquals(ClubPointTransactionDirectionEnum.DECREASE.getDirection(), transaction.getDirection());
        assertEquals(6, transaction.getPoints());
        assertEquals(ClubPointCategoryEnum.DEDUCTION.getCategory(), transaction.getPointCategory());

        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(30, account.getTotalPositivePoints());
        assertEquals(6, account.getTotalNegativePoints());
        assertEquals(24, account.getNetPoints());
        assertEquals(5, account.getFrozenPoints());
        assertEquals(19, account.getAvailablePoints());
        assertEquals(30, account.getAnnualEarnedPoints());
        assertEquals(transactionId, account.getLastTransactionId());
    }

    @Test
    void createDuplicateIdempotencyShouldReturnExistingTransactionWithoutMutatingAccount() {
        insertPublishedRule(5, 10, 5);
        ClubPointLedgerCreateReqBO reqBO = buildPositiveReq("TX-M4-3003", "IDEMP-M4-3003", 8);
        Long firstId = clubPointLedgerService.createTransaction(reqBO);

        Long secondId = clubPointLedgerService.createTransaction(reqBO);

        assertEquals(firstId, secondId);
        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(8, account.getTotalPositivePoints());
        assertEquals(8, account.getAvailablePoints());
    }

    @Test
    void createDuplicateIdempotencyConcurrentlyShouldPersistOnce() throws Exception {
        insertPublishedRule(5, 10, 5);
        ClubPointLedgerCreateReqBO reqBO = buildPositiveReq("TX-M4-3007", "IDEMP-M4-3007", 8);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Callable<Long> task = () -> {
            ready.countDown();
            assertTrue(start.await(5, TimeUnit.SECONDS));
            return clubPointLedgerService.createTransaction(reqBO);
        };

        Future<Long> first = executorService.submit(task);
        Future<Long> second = executorService.submit(task);
        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        Long firstId = first.get(10, TimeUnit.SECONDS);
        Long secondId = second.get(10, TimeUnit.SECONDS);
        executorService.shutdownNow();

        assertEquals(firstId, secondId);
        assertEquals(1L, transactionMapper.selectList().stream()
                .filter(transaction -> "IDEMP-M4-3007".equals(transaction.getIdempotencyKey()))
                .count());
        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(8, account.getTotalPositivePoints());
        assertEquals(8, account.getAvailablePoints());
    }

    @Test
    void createDuplicateIdempotencyShouldRejectDifferentRequest() {
        insertPublishedRule(5, 10, 5);
        clubPointLedgerService.createTransaction(buildPositiveReq("TX-M4-3004", "IDEMP-M4-3004", 8));

        ClubPointLedgerCreateReqBO conflictReq = buildPositiveReq("TX-M4-3005", "IDEMP-M4-3004", 9);

        assertServiceException(() -> clubPointLedgerService.createTransaction(conflictReq),
                CLUB_LEDGER_TRANSACTION_DUPLICATED);
    }

    @Test
    void createNegativeTransactionShouldRejectWhenAvailablePointsNotEnough() {
        insertPublishedRule(1, 20, 5);

        assertServiceException(() -> clubPointLedgerService.createTransaction(buildNegativeReq("TX-M4-3006", "IDEMP-M4-3006", 6)),
                CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH);
        assertNull(transactionMapper.selectByIdempotencyKey("IDEMP-M4-3006"));
        assertNull(accountMapper.selectByUserId(100L));
    }

    private static ClubPointLedgerCreateReqBO buildPositiveReq(String transactionNo, String idempotencyKey, Integer points) {
        return baseReq(transactionNo, idempotencyKey, points)
                .setDirection(ClubPointTransactionDirectionEnum.INCREASE.getDirection())
                .setPointCategory(ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory())
                .setSourceType(ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType())
                .setSourceId(300L)
                .setSourceItemId(301L)
                .setSourceTitleSnapshot("活动结算")
                .setIssuingClubId(400L)
                .setIssuingClubCodeSnapshot("CLUB001")
                .setIssuingClubNameSnapshot("篮球俱乐部")
                .setActivityId(300L)
                .setActivityTitleSnapshot("周末活动")
                .setSourceSnapshotJson("{\"activityId\":300}");
    }

    private static ClubPointLedgerCreateReqBO buildNegativeReq(String transactionNo, String idempotencyKey, Integer points) {
        return baseReq(transactionNo, idempotencyKey, points)
                .setDirection(ClubPointTransactionDirectionEnum.DECREASE.getDirection())
                .setPointCategory(ClubPointCategoryEnum.DEDUCTION.getCategory())
                .setSourceType(ClubPointTransactionSourceTypeEnum.CONTRIBUTION_MATERIAL.getType())
                .setSourceId(500L)
                .setSourceItemId(501L)
                .setSourceTitleSnapshot("违规扣分")
                .setSourceSnapshotJson("{\"materialId\":500}");
    }

    private static ClubPointLedgerCreateReqBO baseReq(String transactionNo, String idempotencyKey, Integer points) {
        return new ClubPointLedgerCreateReqBO()
                .setTransactionNo(transactionNo)
                .setUserId(100L)
                .setUserNameSnapshot("员工A")
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("综合部")
                .setPoints(points)
                .setRuleItemCode(ClubPointRuleItemCodeEnum.ACTIVITY_SMALL_BASE.getCode())
                .setEvidenceType(1)
                .setMaterialSummary("材料摘要")
                .setReason("测试原因")
                .setOccurredAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .setIdempotencyKey(idempotencyKey)
                .setOperatorUserId(900L);
    }

    private ClubPointRuleVersionDO insertPublishedRule(Integer minPoints, Integer maxPoints, Integer defaultPoints) {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("V-M4-3")
                .setName("M4.3 规则")
                .setStatus(STATUS_PUBLISHED)
                .setPublicityTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setEffectiveTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setPublishedTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setSummary("summary")
                .setContent("content");
        ruleVersionMapper.insert(version);

        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(version.getId())
                .setItemCode(ClubPointRuleItemCodeEnum.ACTIVITY_SMALL_BASE.getCode())
                .setItemName("小型活动基础积分")
                .setItemType(1)
                .setCategory(ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory())
                .setMinPoints(minPoints)
                .setMaxPoints(maxPoints)
                .setDefaultPoints(defaultPoints)
                .setStatus(RULE_ITEM_ENABLED)
                .setSort(1));
        return version;
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

}
