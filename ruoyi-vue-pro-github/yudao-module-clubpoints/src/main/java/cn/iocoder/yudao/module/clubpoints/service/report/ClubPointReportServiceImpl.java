package cn.iocoder.yudao.module.clubpoints.service.report;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.budget.ClubPointBudgetRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualRankingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.budget.ClubPointBudgetRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointReportExportTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REPORT_EXPORT_TYPE_INVALID;

@Service
public class ClubPointReportServiceImpl implements ClubPointReportService {

    private static final String BIZ_TYPE_REPORT = "REPORT";

    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointRedemptionApplicationMapper redemptionApplicationMapper;
    @Resource
    private ClubPointAnnualRankingRecordMapper annualRankingRecordMapper;
    @Resource
    private ClubPointBudgetRecordMapper budgetRecordMapper;
    @Resource
    private ClubAuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointReportPointDetailBO> getPointDetailPage(
            ClubPointReportPointDetailPageReqBO reqBO) {
        PageResult<ClubPointTransactionDO> pageResult = transactionMapper.selectPageForReportPointDetail(reqBO,
                reqBO.getUserId(), reqBO.getClubId(), reqBO.getYear(), reqBO.getDirection(),
                reqBO.getPointCategory(), reqBO.getSourceType(), reqBO.getStartTime(), reqBO.getEndTime());
        return new PageResult<>(pageResult.getList().stream()
                .map(ClubPointReportServiceImpl::toPointDetailBO)
                .collect(Collectors.toList()), pageResult.getTotal());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointReportLedgerSummaryBO> getLedgerSummaryPage(
            ClubPointReportLedgerSummaryPageReqBO reqBO) {
        PageResult<ClubPointAccountDO> accountPage = accountMapper.selectPage(reqBO, reqBO.getUserId());
        Map<Long, ClubPointReportLedgerSummaryBO> summaryMap = accountPage.getList().stream()
                .map(ClubPointReportServiceImpl::toLedgerSummaryBO)
                .collect(Collectors.toMap(ClubPointReportLedgerSummaryBO::getUserId, item -> item,
                        (left, right) -> left, LinkedHashMap::new));
        List<Long> userIds = accountPage.getList().stream()
                .map(ClubPointAccountDO::getUserId)
                .collect(Collectors.toList());
        for (ClubPointTransactionDO transaction : transactionMapper.selectListForReportSummary(userIds,
                reqBO.getClubId(), reqBO.getYear(), reqBO.getStartTime(), reqBO.getEndTime())) {
            ClubPointReportLedgerSummaryBO summary = summaryMap.get(transaction.getUserId());
            if (summary == null) {
                continue;
            }
            if (ClubPointTransactionDirectionEnum.INCREASE.getDirection().equals(transaction.getDirection())) {
                summary.setReportPositivePoints(summary.getReportPositivePoints() + transaction.getPoints());
            } else {
                summary.setReportNegativePoints(summary.getReportNegativePoints() + transaction.getPoints());
            }
            summary.setReportNetPoints(summary.getReportPositivePoints() - summary.getReportNegativePoints())
                    .setTransactionCount(summary.getTransactionCount() + 1)
                    .setUserNameSnapshot(transaction.getUserNameSnapshot())
                    .setDeptIdSnapshot(transaction.getDeptIdSnapshot())
                    .setDeptNameSnapshot(transaction.getDeptNameSnapshot())
                    .setLastTransactionId(transaction.getId())
                    .setLastTransactionTime(transaction.getOccurredAt());
        }
        return new PageResult<>(summaryMap.values().stream().collect(Collectors.toList()), accountPage.getTotal());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointReportRedemptionBO> getRedemptionPage(ClubPointReportRedemptionPageReqBO reqBO) {
        PageResult<ClubPointRedemptionApplicationDO> pageResult = redemptionApplicationMapper.selectPageForReport(reqBO,
                reqBO.getBatchId(), reqBO.getUserId(), reqBO.getStatus(),
                resolveStartTime(reqBO.getYear(), reqBO.getStartTime()),
                resolveEndTime(reqBO.getYear(), reqBO.getEndTime()));
        return new PageResult<>(pageResult.getList().stream()
                .map(ClubPointReportServiceImpl::toRedemptionBO)
                .collect(Collectors.toList()), pageResult.getTotal());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointReportClubRankingBO> getClubRankingPage(ClubPointReportClubRankingPageReqBO reqBO) {
        PageResult<ClubPointAnnualRankingRecordDO> pageResult = annualRankingRecordMapper.selectPageForReport(reqBO,
                reqBO.getYear(), reqBO.getClubId());
        return new PageResult<>(pageResult.getList().stream()
                .map(ClubPointReportServiceImpl::toClubRankingBO)
                .collect(Collectors.toList()), pageResult.getTotal());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointReportBudgetBO> getBudgetPage(ClubPointReportBudgetPageReqBO reqBO) {
        PageResult<ClubPointBudgetRecordDO> pageResult = budgetRecordMapper.selectPageForReport(reqBO,
                reqBO.getCategory(), resolveStartDate(reqBO.getYear()), resolveEndDateExclusive(reqBO.getYear()),
                reqBO.getSourceType(), reqBO.getSourceId());
        return new PageResult<>(pageResult.getList().stream()
                .map(ClubPointReportServiceImpl::toBudgetBO)
                .collect(Collectors.toList()), pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClubPointReportExportResultBO exportReport(ClubPointReportExportReqBO reqBO) {
        ClubPointReportExportTypeEnum exportType = ClubPointReportExportTypeEnum.typeOf(reqBO.getReportType());
        if (exportType == null) {
            throw exception(CLUB_REPORT_EXPORT_TYPE_INVALID);
        }
        List<?> rows = getExportRows(exportType, reqBO);
        createExportAudit(reqBO, exportType, rows.size());
        return new ClubPointReportExportResultBO()
                .setReportType(exportType.getType())
                .setReportName(exportType.getReportName())
                .setSheetName(exportType.getSheetName())
                .setRows(rows);
    }

    private List<?> getExportRows(ClubPointReportExportTypeEnum exportType, ClubPointReportExportReqBO reqBO) {
        switch (exportType) {
            case POINT_DETAIL:
                return getPointDetailPage(toPointDetailPageReqBO(reqBO)).getList();
            case REDEMPTION:
                return getRedemptionPage(toRedemptionPageReqBO(reqBO)).getList();
            case LEDGER_SUMMARY:
                return getLedgerSummaryPage(toLedgerSummaryPageReqBO(reqBO)).getList();
            case CLUB_RANKING:
                return getClubRankingPage(toClubRankingPageReqBO(reqBO)).getList();
            case BUDGET:
                return getBudgetPage(toBudgetPageReqBO(reqBO)).getList();
            default:
                throw exception(CLUB_REPORT_EXPORT_TYPE_INVALID);
        }
    }

    private static ClubPointReportPointDetailBO toPointDetailBO(ClubPointTransactionDO transaction) {
        return new ClubPointReportPointDetailBO()
                .setId(transaction.getId())
                .setTransactionNo(transaction.getTransactionNo())
                .setUserId(transaction.getUserId())
                .setUserNameSnapshot(transaction.getUserNameSnapshot())
                .setDeptIdSnapshot(transaction.getDeptIdSnapshot())
                .setDeptNameSnapshot(transaction.getDeptNameSnapshot())
                .setDirection(transaction.getDirection())
                .setPoints(transaction.getPoints())
                .setPointCategory(transaction.getPointCategory())
                .setSourceType(transaction.getSourceType())
                .setSourceId(transaction.getSourceId())
                .setSourceItemId(transaction.getSourceItemId())
                .setSourceTitleSnapshot(transaction.getSourceTitleSnapshot())
                .setIssuingClubId(transaction.getIssuingClubId())
                .setIssuingClubNameSnapshot(transaction.getIssuingClubNameSnapshot())
                .setActivityId(transaction.getActivityId())
                .setActivityTitleSnapshot(transaction.getActivityTitleSnapshot())
                .setRuleVersionId(transaction.getRuleVersionId())
                .setRuleItemId(transaction.getRuleItemId())
                .setRuleItemCodeSnapshot(transaction.getRuleItemCodeSnapshot())
                .setEvidenceType(transaction.getEvidenceType())
                .setMaterialSummary(transaction.getMaterialSummary())
                .setReason(transaction.getReason())
                .setOccurredTime(transaction.getOccurredAt())
                .setCreatedTime(transaction.getCreateTime());
    }

    private static ClubPointReportLedgerSummaryBO toLedgerSummaryBO(ClubPointAccountDO account) {
        return new ClubPointReportLedgerSummaryBO()
                .setUserId(account.getUserId())
                .setReportPositivePoints(0)
                .setReportNegativePoints(0)
                .setReportNetPoints(0)
                .setTransactionCount(0)
                .setTotalPositivePoints(account.getTotalPositivePoints())
                .setTotalNegativePoints(account.getTotalNegativePoints())
                .setNetPoints(account.getNetPoints())
                .setFrozenPoints(account.getFrozenPoints())
                .setAvailablePoints(account.getAvailablePoints())
                .setAnnualEarnedPoints(account.getAnnualEarnedPoints())
                .setLastTransactionId(account.getLastTransactionId())
                .setLastTransactionTime(account.getLastTransactionTime());
    }

    private static ClubPointReportRedemptionBO toRedemptionBO(ClubPointRedemptionApplicationDO application) {
        return new ClubPointReportRedemptionBO()
                .setId(application.getId())
                .setApplicationNo(application.getApplicationNo())
                .setRequestNo(application.getRequestNo())
                .setBatchId(application.getBatchId())
                .setGiftId(application.getGiftId())
                .setUserId(application.getUserId())
                .setStatus(application.getStatus())
                .setPointsCost(application.getPointsCost())
                .setQuantity(application.getQuantity())
                .setFreezeId(application.getFreezeId())
                .setStockLockId(application.getStockLockId())
                .setDeductTransactionId(application.getDeductTransactionId())
                .setBatchSnapshotJson(application.getBatchSnapshotJson())
                .setGiftSnapshotJson(application.getGiftSnapshotJson())
                .setApplyTime(application.getApplyTime())
                .setCancelTime(application.getCancelTime())
                .setReviewTime(application.getReviewTime())
                .setReviewReason(application.getReviewReason())
                .setDirectIssueTime(application.getDirectIssueTime());
    }

    private static ClubPointReportClubRankingBO toClubRankingBO(ClubPointAnnualRankingRecordDO ranking) {
        return new ClubPointReportClubRankingBO()
                .setId(ranking.getId())
                .setYear(ranking.getYear())
                .setClubId(ranking.getClubId())
                .setClubCodeSnapshot(ranking.getClubCodeSnapshot())
                .setClubNameSnapshot(ranking.getClubNameSnapshot())
                .setActivityPoints(ranking.getActivityPoints())
                .setContributionPoints(ranking.getContributionPoints())
                .setRewardPoints(ranking.getRewardPoints())
                .setReversedPoints(ranking.getReversedPoints())
                .setTotalIssuedPoints(ranking.getTotalIssuedPoints())
                .setRankNo(ranking.getRankNo())
                .setIncentiveAmountCent(ranking.getIncentiveAmountCent())
                .setConfirmStatus(ranking.getConfirmStatus())
                .setBudgetRecordId(ranking.getBudgetRecordId())
                .setGeneratedTime(ranking.getGeneratedTime());
    }

    private static ClubPointReportBudgetBO toBudgetBO(ClubPointBudgetRecordDO budget) {
        return new ClubPointReportBudgetBO()
                .setId(budget.getId())
                .setCategory(budget.getCategory())
                .setBudgetAmountCent(budget.getBudgetAmountCent())
                .setActualAmountCent(budget.getActualAmountCent())
                .setOccurDate(budget.getOccurDate())
                .setHandlerUserId(budget.getHandlerUserId())
                .setSourceType(budget.getSourceType())
                .setSourceId(budget.getSourceId())
                .setDescription(budget.getDescription())
                .setRemark(budget.getRemark());
    }

    private void createExportAudit(ClubPointReportExportReqBO reqBO, ClubPointReportExportTypeEnum exportType,
                                   int rowCount) {
        auditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(ClubAuditActionTypeConstants.REPORT_EXPORT)
                .setBizType(BIZ_TYPE_REPORT)
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(reqBO.getOperationTime() == null ? LocalDateTime.now() : reqBO.getOperationTime())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setTargetSnapshotJson(buildExportSnapshot(reqBO, exportType, rowCount))
                .setSuccess(true));
    }

    private static String buildExportSnapshot(ClubPointReportExportReqBO reqBO,
                                              ClubPointReportExportTypeEnum exportType, int rowCount) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("reportType", exportType.getType());
        snapshot.put("reportName", exportType.getReportName());
        snapshot.put("rowCount", rowCount);
        snapshot.put("userId", reqBO.getUserId());
        snapshot.put("clubId", reqBO.getClubId());
        snapshot.put("year", reqBO.getYear());
        snapshot.put("direction", reqBO.getDirection());
        snapshot.put("pointCategory", reqBO.getPointCategory());
        snapshot.put("sourceType", reqBO.getSourceType());
        snapshot.put("status", reqBO.getStatus());
        snapshot.put("category", reqBO.getCategory());
        snapshot.put("sourceId", reqBO.getSourceId());
        snapshot.put("startTime", reqBO.getStartTime());
        snapshot.put("endTime", reqBO.getEndTime());
        return JsonUtils.toJsonString(snapshot);
    }

    private static ClubPointReportPointDetailPageReqBO toPointDetailPageReqBO(ClubPointReportExportReqBO reqBO) {
        ClubPointReportPointDetailPageReqBO pageReqBO = new ClubPointReportPointDetailPageReqBO()
                .setUserId(reqBO.getUserId())
                .setClubId(reqBO.getClubId())
                .setYear(reqBO.getYear())
                .setDirection(reqBO.getDirection())
                .setPointCategory(reqBO.getPointCategory())
                .setSourceType(reqBO.getSourceType())
                .setStartTime(reqBO.getStartTime())
                .setEndTime(reqBO.getEndTime());
        setExportPage(pageReqBO);
        return pageReqBO;
    }

    private static ClubPointReportLedgerSummaryPageReqBO toLedgerSummaryPageReqBO(ClubPointReportExportReqBO reqBO) {
        ClubPointReportLedgerSummaryPageReqBO pageReqBO = new ClubPointReportLedgerSummaryPageReqBO()
                .setUserId(reqBO.getUserId())
                .setClubId(reqBO.getClubId())
                .setYear(reqBO.getYear())
                .setStartTime(reqBO.getStartTime())
                .setEndTime(reqBO.getEndTime());
        setExportPage(pageReqBO);
        return pageReqBO;
    }

    private static ClubPointReportRedemptionPageReqBO toRedemptionPageReqBO(ClubPointReportExportReqBO reqBO) {
        ClubPointReportRedemptionPageReqBO pageReqBO = new ClubPointReportRedemptionPageReqBO()
                .setBatchId(reqBO.getSourceId())
                .setUserId(reqBO.getUserId())
                .setStatus(reqBO.getStatus())
                .setYear(reqBO.getYear())
                .setStartTime(reqBO.getStartTime())
                .setEndTime(reqBO.getEndTime());
        setExportPage(pageReqBO);
        return pageReqBO;
    }

    private static ClubPointReportClubRankingPageReqBO toClubRankingPageReqBO(ClubPointReportExportReqBO reqBO) {
        ClubPointReportClubRankingPageReqBO pageReqBO = new ClubPointReportClubRankingPageReqBO()
                .setYear(reqBO.getYear())
                .setClubId(reqBO.getClubId());
        setExportPage(pageReqBO);
        return pageReqBO;
    }

    private static ClubPointReportBudgetPageReqBO toBudgetPageReqBO(ClubPointReportExportReqBO reqBO) {
        ClubPointReportBudgetPageReqBO pageReqBO = new ClubPointReportBudgetPageReqBO()
                .setYear(reqBO.getYear())
                .setCategory(reqBO.getCategory())
                .setSourceType(reqBO.getSourceType())
                .setSourceId(reqBO.getSourceId());
        setExportPage(pageReqBO);
        return pageReqBO;
    }

    private static void setExportPage(PageParam pageParam) {
        pageParam.setPageNo(1);
        pageParam.setPageSize(PageParam.PAGE_SIZE_NONE);
    }

    private static LocalDateTime resolveStartTime(Integer year, LocalDateTime startTime) {
        if (year == null) {
            return startTime;
        }
        LocalDateTime yearStart = LocalDateTime.of(year, 1, 1, 0, 0);
        return startTime == null || startTime.isBefore(yearStart) ? yearStart : startTime;
    }

    private static LocalDateTime resolveEndTime(Integer year, LocalDateTime endTime) {
        if (year == null) {
            return endTime;
        }
        LocalDateTime yearEnd = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        return endTime == null || endTime.isAfter(yearEnd) ? yearEnd : endTime;
    }

    private static LocalDate resolveStartDate(Integer year) {
        return year == null ? null : LocalDate.of(year, 1, 1);
    }

    private static LocalDate resolveEndDateExclusive(Integer year) {
        return year == null ? null : LocalDate.of(year + 1, 1, 1);
    }

}
