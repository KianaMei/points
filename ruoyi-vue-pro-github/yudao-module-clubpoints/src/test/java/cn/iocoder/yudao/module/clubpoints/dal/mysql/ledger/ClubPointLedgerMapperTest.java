package cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointUserYearStatusDO;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointLedgerMapperTest extends BaseDbUnitTest {

    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointFreezeMapper freezeMapper;
    @Resource
    private ClubPointUserYearStatusMapper userYearStatusMapper;

    @Test
    void ledgerMappersShouldPersistAndQueryTransactionAccountFreezeAndYearStatus() {
        ClubPointTransactionDO transaction = buildTransaction();
        transactionMapper.insert(transaction);

        ClubPointTransactionDO savedTransaction = transactionMapper.selectByTransactionNo("TXN-M4-001");
        assertNotNull(savedTransaction);
        assertEquals(100L, savedTransaction.getUserId());
        assertEquals("员工A", savedTransaction.getUserNameSnapshot());
        assertEquals(10L, savedTransaction.getDeptIdSnapshot());
        assertEquals("综合部", savedTransaction.getDeptNameSnapshot());
        assertEquals(1, savedTransaction.getDirection());
        assertEquals(20, savedTransaction.getPoints());
        assertEquals(1, savedTransaction.getPointCategory());
        assertEquals("MONTHLY_DUTY", savedTransaction.getPointTypeCode());
        assertEquals(1, savedTransaction.getStatus());
        assertEquals(2, savedTransaction.getSourceType());
        assertEquals(300L, savedTransaction.getSourceId());
        assertEquals(301L, savedTransaction.getSourceItemId());
        assertEquals("月度履职", savedTransaction.getSourceTitleSnapshot());
        assertEquals(400L, savedTransaction.getIssuingClubId());
        assertEquals("CLUB001", savedTransaction.getIssuingClubCodeSnapshot());
        assertEquals("篮球俱乐部", savedTransaction.getIssuingClubNameSnapshot());
        assertEquals(500L, savedTransaction.getActivityId());
        assertEquals("活动标题", savedTransaction.getActivityTitleSnapshot());
        assertEquals(LocalDate.of(2026, 6, 1), savedTransaction.getActivityDateSnapshot());
        assertEquals(600L, savedTransaction.getRuleVersionId());
        assertEquals(601L, savedTransaction.getRuleItemId());
        assertEquals("MONTHLY_DUTY", savedTransaction.getRuleItemCodeSnapshot());
        assertEquals("{\"ruleVersionId\":600}", savedTransaction.getRuleSnapshotJson());
        assertEquals(1, savedTransaction.getEvidenceType());
        assertEquals("材料摘要", savedTransaction.getMaterialSummary());
        assertEquals("发放原因", savedTransaction.getReason());
        assertEquals(LocalDateTime.of(2026, 6, 1, 10, 0), savedTransaction.getOccurredAt());
        assertEquals(2026, savedTransaction.getBusinessYear());
        assertEquals(202606, savedTransaction.getBusinessMonth());
        assertEquals("IDEMP-M4-001", savedTransaction.getIdempotencyKey());
        assertEquals(700L, savedTransaction.getReverseOfTransactionId());
        assertEquals(800L, savedTransaction.getOperatorUserId());
        assertEquals(900L, savedTransaction.getAuditLogId());
        assertEquals("{\"source\":\"test\"}", savedTransaction.getSnapshotJson());
        assertEquals(savedTransaction.getId(), transactionMapper.selectByIdempotencyKey("IDEMP-M4-001").getId());
        assertEquals(savedTransaction.getId(), transactionMapper.selectByReverseOfTransactionId(700L).getId());

        ClubPointAccountDO account = buildAccount(savedTransaction.getId());
        accountMapper.insert(account);
        ClubPointAccountDO savedAccount = accountMapper.selectByUserId(100L);
        assertNotNull(savedAccount);
        assertEquals(120, savedAccount.getTotalPositivePoints());
        assertEquals(20, savedAccount.getTotalNegativePoints());
        assertEquals(100, savedAccount.getNetPoints());
        assertEquals(30, savedAccount.getFrozenPoints());
        assertEquals(70, savedAccount.getAvailablePoints());
        assertEquals(60, savedAccount.getAnnualEarnedPoints());
        assertEquals(savedTransaction.getId(), savedAccount.getLastTransactionId());
        assertEquals(LocalDateTime.of(2026, 6, 1, 10, 0), savedAccount.getLastTransactionTime());
        assertEquals(LocalDateTime.of(2026, 6, 1, 11, 0), savedAccount.getLastRebuildTime());
        assertEquals(3, savedAccount.getVersion());

        ClubPointFreezeDO freeze = buildFreeze(savedTransaction.getId());
        freezeMapper.insert(freeze);
        ClubPointFreezeDO savedFreeze = freezeMapper.selectByFreezeNo("FRZ-M4-001");
        assertNotNull(savedFreeze);
        assertEquals(100L, savedFreeze.getUserId());
        assertEquals(30, savedFreeze.getPoints());
        assertEquals(1, savedFreeze.getStatus());
        assertEquals(1, savedFreeze.getSourceType());
        assertEquals(1000L, savedFreeze.getSourceId());
        assertEquals(LocalDateTime.of(2026, 6, 2, 9, 0), savedFreeze.getFrozenAt());
        assertEquals(LocalDateTime.of(2026, 6, 3, 9, 0), savedFreeze.getConvertedAt());
        assertEquals(LocalDateTime.of(2026, 6, 4, 9, 0), savedFreeze.getReleasedAt());
        assertEquals("释放原因", savedFreeze.getReleaseReason());
        assertEquals(savedTransaction.getId(), savedFreeze.getConvertedTransactionId());
        assertEquals("FREEZE-IDEMP-M4-001", savedFreeze.getIdempotencyKey());
        assertEquals(savedFreeze.getId(), freezeMapper.selectByIdempotencyKey("FREEZE-IDEMP-M4-001").getId());
        assertEquals(savedFreeze.getId(), freezeMapper.selectBySource(1, 1000L).getId());

        ClubPointUserYearStatusDO yearStatus = buildYearStatus(savedTransaction.getId());
        userYearStatusMapper.insert(yearStatus);
        ClubPointUserYearStatusDO savedYearStatus = userYearStatusMapper.selectByUserIdAndYear(100L, 2026);
        assertNotNull(savedYearStatus);
        assertTrue(savedYearStatus.getHonorEligible());
        assertEquals("取消原因", savedYearStatus.getHonorCancelReason());
        assertEquals(savedTransaction.getId(), savedYearStatus.getHonorCancelTransactionId());
        assertEquals(LocalDateTime.of(2026, 6, 5, 9, 0), savedYearStatus.getHonorCancelTime());
        assertEquals(120, savedYearStatus.getAnnualPositivePoints());
        assertEquals(20, savedYearStatus.getAnnualNegativePoints());
        assertEquals("年度备注", savedYearStatus.getRemark());
    }

    private static ClubPointTransactionDO buildTransaction() {
        return new ClubPointTransactionDO()
                .setTransactionNo("TXN-M4-001")
                .setUserId(100L)
                .setUserNameSnapshot("员工A")
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("综合部")
                .setDirection(1)
                .setPoints(20)
                .setPointCategory(1)
                .setPointTypeCode("MONTHLY_DUTY")
                .setStatus(1)
                .setSourceType(2)
                .setSourceId(300L)
                .setSourceItemId(301L)
                .setSourceTitleSnapshot("月度履职")
                .setIssuingClubId(400L)
                .setIssuingClubCodeSnapshot("CLUB001")
                .setIssuingClubNameSnapshot("篮球俱乐部")
                .setActivityId(500L)
                .setActivityTitleSnapshot("活动标题")
                .setActivityDateSnapshot(LocalDate.of(2026, 6, 1))
                .setRuleVersionId(600L)
                .setRuleItemId(601L)
                .setRuleItemCodeSnapshot("MONTHLY_DUTY")
                .setRuleSnapshotJson("{\"ruleVersionId\":600}")
                .setEvidenceType(1)
                .setMaterialSummary("材料摘要")
                .setReason("发放原因")
                .setOccurredAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .setBusinessYear(2026)
                .setBusinessMonth(202606)
                .setIdempotencyKey("IDEMP-M4-001")
                .setReverseOfTransactionId(700L)
                .setOperatorUserId(800L)
                .setAuditLogId(900L)
                .setSnapshotJson("{\"source\":\"test\"}");
    }

    private static ClubPointAccountDO buildAccount(Long lastTransactionId) {
        return new ClubPointAccountDO()
                .setUserId(100L)
                .setTotalPositivePoints(120)
                .setTotalNegativePoints(20)
                .setNetPoints(100)
                .setFrozenPoints(30)
                .setAvailablePoints(70)
                .setAnnualEarnedPoints(60)
                .setLastTransactionId(lastTransactionId)
                .setLastTransactionTime(LocalDateTime.of(2026, 6, 1, 10, 0))
                .setLastRebuildTime(LocalDateTime.of(2026, 6, 1, 11, 0))
                .setVersion(3);
    }

    private static ClubPointFreezeDO buildFreeze(Long convertedTransactionId) {
        return new ClubPointFreezeDO()
                .setFreezeNo("FRZ-M4-001")
                .setUserId(100L)
                .setPoints(30)
                .setStatus(1)
                .setSourceType(1)
                .setSourceId(1000L)
                .setFrozenAt(LocalDateTime.of(2026, 6, 2, 9, 0))
                .setConvertedAt(LocalDateTime.of(2026, 6, 3, 9, 0))
                .setReleasedAt(LocalDateTime.of(2026, 6, 4, 9, 0))
                .setReleaseReason("释放原因")
                .setConvertedTransactionId(convertedTransactionId)
                .setIdempotencyKey("FREEZE-IDEMP-M4-001");
    }

    private static ClubPointUserYearStatusDO buildYearStatus(Long transactionId) {
        return new ClubPointUserYearStatusDO()
                .setUserId(100L)
                .setYear(2026)
                .setHonorEligible(true)
                .setHonorCancelReason("取消原因")
                .setHonorCancelTransactionId(transactionId)
                .setHonorCancelTime(LocalDateTime.of(2026, 6, 5, 9, 0))
                .setAnnualPositivePoints(120)
                .setAnnualNegativePoints(20)
                .setRemark("年度备注");
    }

}
