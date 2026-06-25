package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.job.ClubJobRunMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointFreezeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountRebuildAllReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountRebuildReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointLedgerServiceImpl.class, ClubPointRuleServiceImpl.class, ClubAuditServiceImpl.class})
class ClubPointAccountRebuildServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ClubPointLedgerService clubPointLedgerService;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointFreezeMapper freezeMapper;
    @Resource
    private ClubJobRunMapper jobRunMapper;

    @Test
    void rebuildUserAccountShouldRestoreCacheFromTransactionsAndFreezeWithoutChangingLedger() {
        insertTransaction("TX-M4-6001-A", 100L,
                ClubPointTransactionDirectionEnum.INCREASE.getDirection(), 50,
                ClubPointTransactionStatusEnum.VALID.getStatus(), LocalDateTime.of(2026, 6, 1, 10, 0));
        insertTransaction("TX-M4-6001-B", 100L,
                ClubPointTransactionDirectionEnum.DECREASE.getDirection(), 15,
                ClubPointTransactionStatusEnum.VALID.getStatus(), LocalDateTime.of(2026, 6, 2, 10, 0));
        insertTransaction("TX-M4-6001-C", 100L,
                ClubPointTransactionDirectionEnum.INCREASE.getDirection(), 20,
                ClubPointTransactionStatusEnum.VALID.getStatus(), LocalDateTime.of(2025, 12, 31, 10, 0));
        insertTransaction("TX-M4-6001-D", 100L,
                ClubPointTransactionDirectionEnum.INCREASE.getDirection(), 999,
                ClubPointTransactionStatusEnum.REVERSED.getStatus(), LocalDateTime.of(2026, 6, 3, 10, 0));
        Long lastTransactionId = insertTransaction("TX-M4-6001-E", 100L,
                ClubPointTransactionDirectionEnum.DECREASE.getDirection(), 5,
                ClubPointTransactionStatusEnum.REVERSAL.getStatus(), LocalDateTime.of(2026, 6, 4, 10, 0));
        insertTransaction("TX-M4-6001-OTHER", 200L,
                ClubPointTransactionDirectionEnum.INCREASE.getDirection(), 300,
                ClubPointTransactionStatusEnum.VALID.getStatus(), LocalDateTime.of(2026, 6, 5, 10, 0));
        insertFreeze("FRZ-M4-6001-A", 100L, 12, ClubPointFreezeStatusEnum.FROZEN.getStatus());
        insertFreeze("FRZ-M4-6001-B", 100L, 7, ClubPointFreezeStatusEnum.CONVERTED.getStatus());
        insertCorruptAccount(100L, 7);
        Long transactionCountBefore = transactionMapper.selectCount();

        Long jobRunId = clubPointLedgerService.rebuildUserAccount(new ClubPointAccountRebuildReqBO()
                .setUserId(100L)
                .setBusinessYear(2026)
                .setRunKey("RUN-M4-6001")
                .setPlannedTime(LocalDateTime.of(2026, 6, 6, 9, 0))
                .setTriggerSource(2)
                .setHandlerUserId(901L));

        assertEquals(transactionCountBefore, transactionMapper.selectCount());
        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(70, account.getTotalPositivePoints());
        assertEquals(20, account.getTotalNegativePoints());
        assertEquals(50, account.getNetPoints());
        assertEquals(12, account.getFrozenPoints());
        assertEquals(38, account.getAvailablePoints());
        assertEquals(50, account.getAnnualEarnedPoints());
        assertEquals(lastTransactionId, account.getLastTransactionId());
        assertEquals(LocalDateTime.of(2026, 6, 4, 10, 0), account.getLastTransactionTime());
        assertNotNull(account.getLastRebuildTime());
        assertEquals(8, account.getVersion());

        ClubJobRunDO jobRun = jobRunMapper.selectById(jobRunId);
        assertEquals("LEDGER_ACCOUNT_REBUILD", jobRun.getTaskType());
        assertEquals("USER_ACCOUNT", jobRun.getBizType());
        assertEquals(100L, jobRun.getBizId());
        assertEquals("RUN-M4-6001", jobRun.getRunKey());
        assertEquals("LEDGER_ACCOUNT_REBUILD:USER:2026:100:RUN-M4-6001", jobRun.getIdempotencyKey());
        assertEquals(3, jobRun.getStatus());
        assertEquals(1, jobRun.getTotalCount());
        assertEquals(1, jobRun.getSuccessCount());
        assertEquals(0, jobRun.getFailedCount());
        assertTrue(jobRun.getResultJson().contains("\"businessYear\":2026"));
        assertTrue(jobRun.getResultJson().contains("\"userId\":100"));
    }

    @Test
    void rebuildAllAccountsShouldRestoreExistingAndMissingAccountsAndWriteJobRun() {
        insertTransaction("TX-M4-6002-A", 101L,
                ClubPointTransactionDirectionEnum.INCREASE.getDirection(), 30,
                ClubPointTransactionStatusEnum.VALID.getStatus(), LocalDateTime.of(2026, 3, 1, 10, 0));
        insertTransaction("TX-M4-6002-B", 101L,
                ClubPointTransactionDirectionEnum.DECREASE.getDirection(), 10,
                ClubPointTransactionStatusEnum.VALID.getStatus(), LocalDateTime.of(2026, 3, 2, 10, 0));
        insertTransaction("TX-M4-6002-C", 102L,
                ClubPointTransactionDirectionEnum.INCREASE.getDirection(), 40,
                ClubPointTransactionStatusEnum.VALID.getStatus(), LocalDateTime.of(2026, 3, 3, 10, 0));
        insertFreeze("FRZ-M4-6002-A", 102L, 5, ClubPointFreezeStatusEnum.FROZEN.getStatus());
        insertCorruptAccount(101L, 2);
        insertCorruptAccount(103L, 4);
        Long transactionCountBefore = transactionMapper.selectCount();

        Long jobRunId = clubPointLedgerService.rebuildAllAccounts(new ClubPointAccountRebuildAllReqBO()
                .setBusinessYear(2026)
                .setRunKey("RUN-M4-6002")
                .setPlannedTime(LocalDateTime.of(2026, 6, 7, 9, 0))
                .setTriggerSource(2)
                .setHandlerUserId(901L));

        assertEquals(transactionCountBefore, transactionMapper.selectCount());
        ClubPointAccountDO account101 = accountMapper.selectByUserId(101L);
        assertEquals(30, account101.getTotalPositivePoints());
        assertEquals(10, account101.getTotalNegativePoints());
        assertEquals(20, account101.getAvailablePoints());
        ClubPointAccountDO account102 = accountMapper.selectByUserId(102L);
        assertEquals(40, account102.getTotalPositivePoints());
        assertEquals(0, account102.getTotalNegativePoints());
        assertEquals(40, account102.getNetPoints());
        assertEquals(5, account102.getFrozenPoints());
        assertEquals(35, account102.getAvailablePoints());
        ClubPointAccountDO account103 = accountMapper.selectByUserId(103L);
        assertEquals(0, account103.getTotalPositivePoints());
        assertEquals(0, account103.getTotalNegativePoints());
        assertEquals(0, account103.getNetPoints());
        assertEquals(0, account103.getAvailablePoints());

        ClubJobRunDO jobRun = jobRunMapper.selectById(jobRunId);
        assertEquals("LEDGER_ACCOUNT_REBUILD", jobRun.getTaskType());
        assertEquals("ALL_ACCOUNT", jobRun.getBizType());
        assertEquals("RUN-M4-6002", jobRun.getRunKey());
        assertEquals("LEDGER_ACCOUNT_REBUILD:ALL:2026:RUN-M4-6002", jobRun.getIdempotencyKey());
        assertEquals(3, jobRun.getStatus());
        assertEquals(3, jobRun.getTotalCount());
        assertEquals(3, jobRun.getSuccessCount());
        assertEquals(0, jobRun.getFailedCount());
        assertTrue(jobRun.getResultJson().contains("\"businessYear\":2026"));
        assertTrue(jobRun.getResultJson().contains("\"rebuiltUsers\":3"));
    }

    private Long insertTransaction(String transactionNo, Long userId, Integer direction, Integer points,
                                   Integer status, LocalDateTime occurredAt) {
        ClubPointTransactionDO transaction = new ClubPointTransactionDO()
                .setTransactionNo(transactionNo)
                .setUserId(userId)
                .setUserNameSnapshot("员工" + userId)
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("综合部")
                .setDirection(direction)
                .setPoints(points)
                .setPointCategory(ClubPointCategoryEnum.SPECIAL_REWARD.getCategory())
                .setPointTypeCode("TEST")
                .setStatus(status)
                .setSourceType(ClubPointTransactionSourceTypeEnum.ADMIN_DIRECT.getType())
                .setSourceTitleSnapshot("测试流水")
                .setRuleVersionId(1L)
                .setRuleItemId(1L)
                .setRuleItemCodeSnapshot("TEST")
                .setOccurredAt(occurredAt)
                .setBusinessYear(occurredAt.getYear())
                .setBusinessMonth(occurredAt.getYear() * 100 + occurredAt.getMonthValue())
                .setIdempotencyKey("IDEMP-" + transactionNo);
        transactionMapper.insert(transaction);
        return transaction.getId();
    }

    private void insertFreeze(String freezeNo, Long userId, Integer points, Integer status) {
        freezeMapper.insert(new ClubPointFreezeDO()
                .setFreezeNo(freezeNo)
                .setUserId(userId)
                .setPoints(points)
                .setStatus(status)
                .setSourceType(1)
                .setSourceId(Long.valueOf(freezeNo.replace("FRZ-M4-", "").replace("-A", "1").replace("-B", "2")))
                .setFrozenAt(LocalDateTime.of(2026, 6, 1, 9, 0))
                .setIdempotencyKey("IDEMP-" + freezeNo));
    }

    private void insertCorruptAccount(Long userId, Integer version) {
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(999)
                .setTotalNegativePoints(999)
                .setNetPoints(999)
                .setFrozenPoints(999)
                .setAvailablePoints(999)
                .setAnnualEarnedPoints(999)
                .setVersion(version));
    }

}
