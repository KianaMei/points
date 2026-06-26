package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualClearingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualClearingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAnnualClearingStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearAllReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearResultBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearUserReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointAnnualClearingServiceImpl.class, ClubPointLedgerServiceImpl.class,
        ClubPointRuleServiceImpl.class, ClubAuditServiceImpl.class})
class ClubPointAnnualClearingServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime CLEAR_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Resource
    private ClubPointAnnualClearingService annualClearingService;
    @Resource
    private ClubPointAnnualClearingRecordMapper clearingRecordMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;

    @Test
    void clearUserShouldCreateAnnualClearingTransactionAndRecordOnlyForAvailablePoints() {
        ClubPointRuleVersionDO ruleVersion = insertPublishedRuleVersion();
        insertAccount(100L, 120, 0, 120, 30, 90);

        Long recordId = annualClearingService.clearUser(buildUserReq(100L));

        ClubPointAnnualClearingRecordDO record = clearingRecordMapper.selectById(recordId);
        assertEquals(2026, record.getYear());
        assertEquals(100L, record.getUserId());
        assertEquals(120, record.getNetPointsBefore());
        assertEquals(30, record.getFrozenPointsBefore());
        assertEquals(90, record.getAvailablePointsBefore());
        assertEquals(90, record.getClearablePoints());
        assertEquals(ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus(), record.getStatus());
        assertEquals(900L, record.getRunId());
        assertEquals("ANNUAL_CLEARING:2026:100", record.getIdempotencyKey());
        assertEquals(CLEAR_TIME, record.getClearTime());
        assertNotNull(record.getClearTransactionId());

        ClubPointTransactionDO transaction = transactionMapper.selectById(record.getClearTransactionId());
        assertEquals("AC-2026-100", transaction.getTransactionNo());
        assertEquals(100L, transaction.getUserId());
        assertEquals(ClubPointTransactionDirectionEnum.DECREASE.getDirection(), transaction.getDirection());
        assertEquals(90, transaction.getPoints());
        assertEquals(ClubPointCategoryEnum.ANNUAL_CLEARING.getCategory(), transaction.getPointCategory());
        assertEquals(ClubPointTransactionStatusEnum.VALID.getStatus(), transaction.getStatus());
        assertEquals(ClubPointTransactionSourceTypeEnum.ANNUAL_CLEARING.getType(), transaction.getSourceType());
        assertEquals(recordId, transaction.getSourceId());
        assertEquals("年度清零", transaction.getSourceTitleSnapshot());
        assertEquals("ANNUAL_CLEARING", transaction.getPointTypeCode());
        assertEquals(2026, transaction.getBusinessYear());
        assertEquals(202601, transaction.getBusinessMonth());
        assertEquals("ANNUAL_CLEARING:2026:100", transaction.getIdempotencyKey());
        assertEquals(ruleVersion.getId(), transaction.getRuleVersionId());
        assertTrue(transaction.getRuleSnapshotJson().contains("\"ruleItemCode\":\"ANNUAL_CLEARING\""));

        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(120, account.getTotalPositivePoints());
        assertEquals(90, account.getTotalNegativePoints());
        assertEquals(30, account.getNetPoints());
        assertEquals(30, account.getFrozenPoints());
        assertEquals(0, account.getAvailablePoints());
        assertEquals(120, account.getAnnualEarnedPoints());
        assertEquals(transaction.getId(), account.getLastTransactionId());
        assertEquals(CLEAR_TIME, account.getLastTransactionTime());
    }

    @Test
    void clearUserShouldBeIdempotentForSameUserAndYear() {
        insertPublishedRuleVersion();
        insertAccount(101L, 80, 0, 80, 20, 60);

        Long firstRecordId = annualClearingService.clearUser(buildUserReq(101L));
        Long secondRecordId = annualClearingService.clearUser(buildUserReq(101L));

        assertEquals(firstRecordId, secondRecordId);
        assertEquals(1L, clearingRecordMapper.selectCount());
        assertEquals(1L, transactionMapper.selectCount());
        ClubPointAccountDO account = accountMapper.selectByUserId(101L);
        assertEquals(20, account.getNetPoints());
        assertEquals(20, account.getFrozenPoints());
        assertEquals(0, account.getAvailablePoints());
    }

    @Test
    void clearUserShouldSkipWhenNoAvailablePointsAndNotCreateTransaction() {
        insertPublishedRuleVersion();
        insertAccount(102L, 50, 0, 50, 50, 0);

        Long recordId = annualClearingService.clearUser(buildUserReq(102L));

        ClubPointAnnualClearingRecordDO record = clearingRecordMapper.selectById(recordId);
        assertEquals(ClubPointAnnualClearingStatusEnum.SKIPPED.getStatus(), record.getStatus());
        assertEquals(50, record.getNetPointsBefore());
        assertEquals(50, record.getFrozenPointsBefore());
        assertEquals(0, record.getAvailablePointsBefore());
        assertEquals(0, record.getClearablePoints());
        assertNull(record.getClearTransactionId());
        assertEquals(0L, transactionMapper.selectCount());
        ClubPointAccountDO account = accountMapper.selectByUserId(102L);
        assertEquals(50, account.getNetPoints());
        assertEquals(50, account.getFrozenPoints());
        assertEquals(0, account.getAvailablePoints());
    }

    @Test
    void clearAllShouldScanAccountsAndRerunWithoutDuplicatingRecordsOrTransactions() {
        insertPublishedRuleVersion();
        insertAccount(201L, 30, 0, 30, 0, 30);
        insertAccount(202L, 60, 0, 60, 60, 0);
        insertAccount(203L, 90, 10, 80, 30, 50);

        ClubPointAnnualClearResultBO firstResult = annualClearingService.clearAll(buildAllReq());
        ClubPointAnnualClearResultBO secondResult = annualClearingService.clearAll(buildAllReq());

        assertEquals(3, firstResult.getTotalCount());
        assertEquals(2, firstResult.getSuccessCount());
        assertEquals(1, firstResult.getSkipCount());
        assertEquals(0, firstResult.getFailedCount());
        assertEquals(3, secondResult.getTotalCount());
        assertEquals(2, secondResult.getSuccessCount());
        assertEquals(1, secondResult.getSkipCount());
        assertEquals(0, secondResult.getFailedCount());
        assertEquals(3L, clearingRecordMapper.selectCount());
        assertEquals(2L, transactionMapper.selectCount());
        assertEquals(0, accountMapper.selectByUserId(201L).getAvailablePoints());
        assertEquals(0, accountMapper.selectByUserId(202L).getAvailablePoints());
        assertEquals(30, accountMapper.selectByUserId(203L).getNetPoints());
        assertEquals(30, accountMapper.selectByUserId(203L).getFrozenPoints());
        assertEquals(0, accountMapper.selectByUserId(203L).getAvailablePoints());
    }

    private ClubPointRuleVersionDO insertPublishedRuleVersion() {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("V-M10-3")
                .setName("M10.3 规则")
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

    private static ClubPointAnnualClearUserReqBO buildUserReq(Long userId) {
        return new ClubPointAnnualClearUserReqBO()
                .setYear(2026)
                .setUserId(userId)
                .setRunId(900L)
                .setClearTime(CLEAR_TIME)
                .setOperatorUserId(1L)
                .setReason("年度清零");
    }

    private static ClubPointAnnualClearAllReqBO buildAllReq() {
        return new ClubPointAnnualClearAllReqBO()
                .setYear(2026)
                .setRunId(900L)
                .setClearTime(CLEAR_TIME)
                .setOperatorUserId(1L)
                .setReason("年度清零");
    }

}
