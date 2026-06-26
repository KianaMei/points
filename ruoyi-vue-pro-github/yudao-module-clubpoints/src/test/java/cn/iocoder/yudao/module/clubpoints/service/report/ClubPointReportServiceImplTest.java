package cn.iocoder.yudao.module.clubpoints.service.report;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.budget.ClubPointBudgetRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualRankingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.budget.ClubPointBudgetRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionGiftMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointBudgetCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointBudgetSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointReportExportTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportBudgetBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportBudgetPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportClubRankingBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportClubRankingPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportExportReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportExportResultBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportLedgerSummaryBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportLedgerSummaryPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportPointDetailBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportPointDetailPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportRedemptionBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportRedemptionPageReqBO;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointReportServiceImpl.class,
        ClubAuditServiceImpl.class
})
class ClubPointReportServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 6, 1, 10, 0);
    private static final int STATUS_VALID = ClubPointTransactionStatusEnum.VALID.getStatus();
    private static final int STATUS_REVERSED = ClubPointTransactionStatusEnum.REVERSED.getStatus();
    private static final int DIRECTION_INCREASE = ClubPointTransactionDirectionEnum.INCREASE.getDirection();
    private static final int DIRECTION_DECREASE = ClubPointTransactionDirectionEnum.DECREASE.getDirection();
    private static final int SOURCE_ACTIVITY = ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType();
    private static final int SOURCE_REDEMPTION = ClubPointTransactionSourceTypeEnum.REDEMPTION.getType();
    private static final int CATEGORY_BASIC = ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory();
    private static final int CATEGORY_DEDUCTION = ClubPointCategoryEnum.DEDUCTION.getCategory();

    @Resource
    private ClubPointReportService reportService;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointRedemptionBatchMapper redemptionBatchMapper;
    @Resource
    private ClubPointRedemptionGiftMapper redemptionGiftMapper;
    @Resource
    private ClubPointRedemptionApplicationMapper redemptionApplicationMapper;
    @Resource
    private ClubPointAnnualRankingRecordMapper rankingRecordMapper;
    @Resource
    private ClubPointBudgetRecordMapper budgetRecordMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;

    @Test
    void pointDetailReportShouldReadEffectiveTransactionsWithFilters() {
        Long expectedId = insertTransaction("TX-RPT-1001", 100L, "User 100", DIRECTION_INCREASE, 12,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 400L, "Club 400", STATUS_VALID, BASE_TIME);
        insertTransaction("TX-RPT-1002", 100L, "User 100", DIRECTION_INCREASE, 99,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 401L, "Club 401", STATUS_VALID, BASE_TIME.plusHours(1));
        insertTransaction("TX-RPT-1003", 100L, "User 100", DIRECTION_DECREASE, 3,
                CATEGORY_DEDUCTION, SOURCE_REDEMPTION, 400L, "Club 400", STATUS_REVERSED, BASE_TIME.plusHours(2));

        ClubPointReportPointDetailPageReqBO reqBO = new ClubPointReportPointDetailPageReqBO()
                .setUserId(100L)
                .setClubId(400L)
                .setYear(2026)
                .setStartTime(BASE_TIME.minusDays(1))
                .setEndTime(BASE_TIME.plusDays(1));
        reqBO.setPageNo(1);
        reqBO.setPageSize(10);

        PageResult<ClubPointReportPointDetailBO> page = reportService.getPointDetailPage(reqBO);

        assertEquals(1L, page.getTotal());
        assertEquals(expectedId, page.getList().get(0).getId());
        assertEquals("TX-RPT-1001", page.getList().get(0).getTransactionNo());
        assertEquals("Club 400", page.getList().get(0).getIssuingClubNameSnapshot());
    }

    @Test
    void ledgerSummaryReportShouldAggregateTransactionsAndReadCurrentAccountCache() {
        accountMapper.insert(buildAccount(100L, 120, 40, 80, 5, 75, 120));
        accountMapper.insert(buildAccount(101L, 200, 0, 200, 0, 200, 200));
        insertTransaction("TX-RPT-2001", 100L, "User 100", DIRECTION_INCREASE, 70,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 400L, "Club 400", STATUS_VALID, BASE_TIME);
        insertTransaction("TX-RPT-2002", 100L, "User 100", DIRECTION_DECREASE, 20,
                CATEGORY_DEDUCTION, SOURCE_REDEMPTION, 400L, "Club 400", STATUS_VALID, BASE_TIME.plusHours(1));
        insertTransaction("TX-RPT-2003", 101L, "User 101", DIRECTION_INCREASE, 999,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 400L, "Club 400", STATUS_VALID, BASE_TIME.plusHours(2));

        ClubPointReportLedgerSummaryPageReqBO reqBO = new ClubPointReportLedgerSummaryPageReqBO()
                .setUserId(100L)
                .setClubId(400L)
                .setYear(2026);
        reqBO.setPageNo(1);
        reqBO.setPageSize(10);

        PageResult<ClubPointReportLedgerSummaryBO> page = reportService.getLedgerSummaryPage(reqBO);

        assertEquals(1L, page.getTotal());
        ClubPointReportLedgerSummaryBO row = page.getList().get(0);
        assertEquals(100L, row.getUserId());
        assertEquals(70, row.getReportPositivePoints());
        assertEquals(20, row.getReportNegativePoints());
        assertEquals(50, row.getReportNetPoints());
        assertEquals(2, row.getTransactionCount());
        assertEquals(75, row.getAvailablePoints());
        assertEquals(5, row.getFrozenPoints());
    }

    @Test
    void redemptionReportShouldUseApplicationSnapshotsNotMutableGift() {
        ClubPointRedemptionBatchDO batch = insertBatch();
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(), "Current Gift", 99);
        ClubPointRedemptionApplicationDO application = new ClubPointRedemptionApplicationDO()
                .setApplicationNo("APP-RPT-3001")
                .setRequestNo("REQ-RPT-3001")
                .setBatchId(batch.getId())
                .setGiftId(gift.getId())
                .setEligibilitySnapshotId(3001L)
                .setUserId(100L)
                .setStatus(ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus())
                .setPointsCost(30)
                .setQuantity(2)
                .setDeductTransactionId(9001L)
                .setBeforeNetPoints(100)
                .setBeforeFrozenPoints(0)
                .setBeforeAvailablePoints(100)
                .setAfterNetPoints(40)
                .setAfterFrozenPoints(0)
                .setAfterAvailablePoints(40)
                .setBatchSnapshotJson("{\"name\":\"Snapshot Batch\"}")
                .setGiftSnapshotJson("{\"name\":\"Snapshot Gift\",\"pointsCost\":30}")
                .setApplyTime(BASE_TIME)
                .setReviewTime(BASE_TIME.plusHours(1))
                .setDirectIssueTime(BASE_TIME.plusHours(2))
                .setIdempotencyKey("REDEMPTION:REQ-RPT-3001");
        redemptionApplicationMapper.insert(application);
        redemptionGiftMapper.updateById(gift.setName("Mutated Gift").setPointsCost(999));

        ClubPointReportRedemptionPageReqBO reqBO = new ClubPointReportRedemptionPageReqBO()
                .setBatchId(batch.getId())
                .setUserId(100L)
                .setStatus(ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus())
                .setYear(2026);
        reqBO.setPageNo(1);
        reqBO.setPageSize(10);

        PageResult<ClubPointReportRedemptionBO> page = reportService.getRedemptionPage(reqBO);

        assertEquals(1L, page.getTotal());
        ClubPointReportRedemptionBO row = page.getList().get(0);
        assertEquals(application.getId(), row.getId());
        assertEquals(30, row.getPointsCost());
        assertEquals(2, row.getQuantity());
        assertTrue(row.getGiftSnapshotJson().contains("Snapshot Gift"));
        assertTrue(!row.getGiftSnapshotJson().contains("Mutated Gift"));
    }

    @Test
    void clubRankingReportShouldReadAnnualRankingRecords() {
        rankingRecordMapper.insert(new ClubPointAnnualRankingRecordDO()
                .setYear(2026)
                .setClubId(400L)
                .setClubCodeSnapshot("CLUB-400")
                .setClubNameSnapshot("Club 400")
                .setActivityPoints(80)
                .setContributionPoints(15)
                .setRewardPoints(5)
                .setReversedPoints(10)
                .setTotalIssuedPoints(90)
                .setRankNo(1)
                .setIncentiveAmountCent(30000L)
                .setConfirmStatus(1)
                .setGeneratedTime(BASE_TIME)
                .setSnapshotJson("{}"));
        rankingRecordMapper.insert(new ClubPointAnnualRankingRecordDO()
                .setYear(2025)
                .setClubId(401L)
                .setClubCodeSnapshot("CLUB-401")
                .setClubNameSnapshot("Club 401")
                .setActivityPoints(999)
                .setContributionPoints(0)
                .setRewardPoints(0)
                .setReversedPoints(0)
                .setTotalIssuedPoints(999)
                .setRankNo(1)
                .setIncentiveAmountCent(0L)
                .setConfirmStatus(0)
                .setGeneratedTime(BASE_TIME)
                .setSnapshotJson("{}"));

        ClubPointReportClubRankingPageReqBO reqBO = new ClubPointReportClubRankingPageReqBO().setYear(2026);
        reqBO.setPageNo(1);
        reqBO.setPageSize(10);

        PageResult<ClubPointReportClubRankingBO> page = reportService.getClubRankingPage(reqBO);

        assertEquals(1L, page.getTotal());
        assertEquals(400L, page.getList().get(0).getClubId());
        assertEquals(90, page.getList().get(0).getTotalIssuedPoints());
        assertEquals(1, page.getList().get(0).getRankNo());
    }

    @Test
    void budgetReportShouldReadBudgetRecordsByOccurDateYear() {
        budgetRecordMapper.insert(new ClubPointBudgetRecordDO()
                .setCategory(ClubPointBudgetCategoryEnum.ACTIVITY.getCategory())
                .setBudgetAmountCent(100000L)
                .setActualAmountCent(60000L)
                .setOccurDate(LocalDate.of(2026, 5, 1))
                .setHandlerUserId(9001L)
                .setSourceType(ClubPointBudgetSourceTypeEnum.MANUAL.getType())
                .setDescription("2026 budget"));
        budgetRecordMapper.insert(new ClubPointBudgetRecordDO()
                .setCategory(ClubPointBudgetCategoryEnum.ACTIVITY.getCategory())
                .setBudgetAmountCent(999000L)
                .setActualAmountCent(999000L)
                .setOccurDate(LocalDate.of(2025, 5, 1))
                .setHandlerUserId(9001L)
                .setSourceType(ClubPointBudgetSourceTypeEnum.MANUAL.getType())
                .setDescription("2025 budget"));

        ClubPointReportBudgetPageReqBO reqBO = new ClubPointReportBudgetPageReqBO()
                .setYear(2026)
                .setCategory(ClubPointBudgetCategoryEnum.ACTIVITY.getCategory());
        reqBO.setPageNo(1);
        reqBO.setPageSize(10);

        PageResult<ClubPointReportBudgetBO> page = reportService.getBudgetPage(reqBO);

        assertEquals(1L, page.getTotal());
        assertEquals(100000L, page.getList().get(0).getBudgetAmountCent());
        assertEquals(60000L, page.getList().get(0).getActualAmountCent());
        assertEquals("2026 budget", page.getList().get(0).getDescription());
    }

    @Test
    void exportReportShouldWriteStrongAuditAndReturnRows() {
        Long expectedId = insertTransaction("TX-RPT-EXPORT-SVC-1001", 100L, "User 100", DIRECTION_INCREASE, 12,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 400L, "Club 400", STATUS_VALID, BASE_TIME);

        ClubPointReportExportResultBO exportResult = reportService.exportReport(new ClubPointReportExportReqBO()
                .setReportType(ClubPointReportExportTypeEnum.POINT_DETAIL.getType())
                .setUserId(100L)
                .setClubId(400L)
                .setYear(2026)
                .setOperatorUserId(9001L)
                .setOperatorNameSnapshot("Report Admin")
                .setOperatorRoleSnapshot("admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("导出积分明细"));

        assertEquals(ClubPointReportExportTypeEnum.POINT_DETAIL.getType(), exportResult.getReportType());
        assertEquals("积分明细报表", exportResult.getReportName());
        assertEquals(1, exportResult.getRows().size());
        assertEquals(expectedId, ((ClubPointReportPointDetailBO) exportResult.getRows().get(0)).getId());

        ClubAuditLogDO auditLog = auditLogMapper.selectList().get(0);
        assertEquals(ClubAuditActionTypeConstants.REPORT_EXPORT, auditLog.getActionType());
        assertEquals("REPORT", auditLog.getBizType());
        assertEquals(9001L, auditLog.getOperatorUserId());
        assertEquals("Report Admin", auditLog.getOperatorNameSnapshot());
        assertTrue(auditLog.getTargetSnapshotJson().contains("\"reportType\":1"));
        assertTrue(auditLog.getTargetSnapshotJson().contains("\"rowCount\":1"));
        assertTrue(auditLog.getTargetSnapshotJson().contains("\"clubId\":400"));
    }

    @Test
    void exportReportShouldFailWhenStrongAuditCannotBeWritten() {
        insertTransaction("TX-RPT-EXPORT-SVC-2001", 100L, "User 100", DIRECTION_INCREASE, 12,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 400L, "Club 400", STATUS_VALID, BASE_TIME);

        assertThrows(ServiceException.class, () -> reportService.exportReport(new ClubPointReportExportReqBO()
                .setReportType(ClubPointReportExportTypeEnum.POINT_DETAIL.getType())
                .setUserId(100L)
                .setClubId(400L)
                .setYear(2026)
                .setOperatorUserId(9001L)
                .setOperatorRoleSnapshot("admin")
                .setReason("缺少操作人快照，强审计必须失败")));

        assertEquals(0L, auditLogMapper.selectCount());
    }

    private Long insertTransaction(String transactionNo, Long userId, String userName, Integer direction,
                                   Integer points, Integer pointCategory, Integer sourceType, Long issuingClubId,
                                   String issuingClubName, Integer status, LocalDateTime occurredAt) {
        ClubPointTransactionDO transaction = new ClubPointTransactionDO()
                .setTransactionNo(transactionNo)
                .setUserId(userId)
                .setUserNameSnapshot(userName)
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("Ops")
                .setDirection(direction)
                .setPoints(points)
                .setPointCategory(pointCategory)
                .setPointTypeCode("REPORT_TEST")
                .setStatus(status)
                .setSourceType(sourceType)
                .setSourceId(1L)
                .setSourceTitleSnapshot("Report Source")
                .setIssuingClubId(issuingClubId)
                .setIssuingClubCodeSnapshot(issuingClubId == null ? null : "CLUB-" + issuingClubId)
                .setIssuingClubNameSnapshot(issuingClubName)
                .setRuleVersionId(1L)
                .setRuleItemCodeSnapshot("REPORT_TEST")
                .setEvidenceType(1)
                .setMaterialSummary("Report material")
                .setReason("Report reason")
                .setOccurredAt(occurredAt)
                .setBusinessYear(occurredAt.getYear())
                .setBusinessMonth(occurredAt.getYear() * 100 + occurredAt.getMonthValue())
                .setIdempotencyKey("IDEMP-" + transactionNo)
                .setOperatorUserId(9001L);
        transactionMapper.insert(transaction);
        return transaction.getId();
    }

    private static ClubPointAccountDO buildAccount(Long userId, Integer totalPositivePoints,
                                                   Integer totalNegativePoints, Integer netPoints,
                                                   Integer frozenPoints, Integer availablePoints,
                                                   Integer annualEarnedPoints) {
        return new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(totalPositivePoints)
                .setTotalNegativePoints(totalNegativePoints)
                .setNetPoints(netPoints)
                .setFrozenPoints(frozenPoints)
                .setAvailablePoints(availablePoints)
                .setAnnualEarnedPoints(annualEarnedPoints)
                .setVersion(1);
    }

    private ClubPointRedemptionBatchDO insertBatch() {
        ClubPointRedemptionBatchDO batch = new ClubPointRedemptionBatchDO()
                .setYear(2026)
                .setName("Report Batch")
                .setStatus(1)
                .setOpenTime(BASE_TIME.minusDays(1))
                .setCloseTime(BASE_TIME.plusDays(30))
                .setMinAvailablePoints(0)
                .setQualifiedCount(100)
                .setIncludeTieAtCutoff(true)
                .setQualificationRuleJson("{}")
                .setSnapshotGenerated(true)
                .setSnapshotGeneratedTime(BASE_TIME.minusHours(1))
                .setRuleVersionId(1L)
                .setRuleSnapshotJson("{}");
        redemptionBatchMapper.insert(batch);
        return batch;
    }

    private ClubPointRedemptionGiftDO insertGift(Long batchId, String name, Integer pointsCost) {
        ClubPointRedemptionGiftDO gift = new ClubPointRedemptionGiftDO()
                .setBatchId(batchId)
                .setName(name)
                .setDescription("Report gift")
                .setPointsCost(pointsCost)
                .setStockTotal(100)
                .setStockLocked(0)
                .setStockUsed(0)
                .setStatus(1)
                .setSort(1)
                .setGiftSnapshotJson("{\"name\":\"" + name + "\"}");
        redemptionGiftMapper.insert(gift);
        return gift;
    }

}
