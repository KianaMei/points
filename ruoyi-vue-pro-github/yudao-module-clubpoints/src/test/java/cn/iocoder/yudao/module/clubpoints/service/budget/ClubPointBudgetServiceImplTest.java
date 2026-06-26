package cn.iocoder.yudao.module.clubpoints.service.budget;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointIncentiveRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.budget.ClubPointBudgetRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointIncentiveRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.budget.ClubPointBudgetRecordMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointBudgetCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointBudgetSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetQueryReqBO;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.BUDGET_CREATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.BUDGET_DISABLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.BUDGET_UPDATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_BUDGET_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_BUDGET_SOURCE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointBudgetServiceImpl.class, ClubAuditServiceImpl.class, ClubScopeServiceImpl.class})
class ClubPointBudgetServiceImplTest extends BaseDbUnitTest {

    private static final LocalDate OCCUR_DATE = LocalDate.of(2026, 3, 8);
    private static final LocalDateTime OPERATION_TIME = LocalDateTime.of(2026, 3, 9, 10, 0);

    @Resource
    private ClubPointBudgetService budgetService;
    @Resource
    private ClubPointBudgetRecordMapper budgetRecordMapper;
    @Resource
    private ClubPointIncentiveRecordMapper incentiveRecordMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;

    @Test
    void createBudgetShouldPersistManualGlobalRecordAndQueryByYear() {
        Long budgetId = budgetService.createBudget(buildManualBudgetReq()
                .setBudgetAmountCent(300_000L)
                .setActualAmountCent(120_000L)
                .setDescription("春季活动经费"));

        ClubPointBudgetRecordDO record = budgetRecordMapper.selectById(budgetId);
        assertNotNull(record);
        assertEquals(ClubPointBudgetCategoryEnum.ACTIVITY.getCategory(), record.getCategory());
        assertEquals(300_000L, record.getBudgetAmountCent());
        assertEquals(120_000L, record.getActualAmountCent());
        assertEquals(OCCUR_DATE, record.getOccurDate());
        assertEquals(9001L, record.getHandlerUserId());
        assertEquals(ClubPointBudgetSourceTypeEnum.MANUAL.getType(), record.getSourceType());
        assertNull(record.getSourceId());
        assertEquals("春季活动经费", record.getDescription());

        List<ClubPointBudgetRecordDO> currentYearRecords = budgetService.listBudgetRecords(new ClubPointBudgetQueryReqBO()
                .setYear(2026)
                .setCategory(ClubPointBudgetCategoryEnum.ACTIVITY.getCategory())
                .setOperatorGlobalScope(true));
        assertEquals(1, currentYearRecords.size());
        assertEquals(budgetId, currentYearRecords.get(0).getId());
        assertTrue(budgetService.listBudgetRecords(new ClubPointBudgetQueryReqBO()
                .setYear(2025)
                .setOperatorGlobalScope(true)).isEmpty());

        ClubAuditLogDO audit = findAudit(BUDGET_CREATE);
        assertEquals("BUDGET_RECORD", audit.getBizType());
        assertEquals(budgetId, audit.getBizId());
    }

    @Test
    void createBudgetFromConfirmedIncentiveShouldLinkSourceAndIncentive() {
        ClubPointIncentiveRecordDO incentive = insertIncentive(ClubPointIncentiveStatusEnum.CONFIRMED.getStatus());

        Long budgetId = budgetService.createBudget(buildManualBudgetReq()
                .setCategory(ClubPointBudgetCategoryEnum.INCENTIVE.getCategory())
                .setSourceType(ClubPointBudgetSourceTypeEnum.RANKING_INCENTIVE.getType())
                .setSourceId(incentive.getId())
                .setBudgetAmountCent(200_000L)
                .setActualAmountCent(200_000L)
                .setDescription("排名激励经费"));

        ClubPointBudgetRecordDO record = budgetRecordMapper.selectBySourceTypeAndSourceId(
                ClubPointBudgetSourceTypeEnum.RANKING_INCENTIVE.getType(), incentive.getId());
        assertNotNull(record);
        assertEquals(budgetId, record.getId());
        ClubPointIncentiveRecordDO linked = incentiveRecordMapper.selectById(incentive.getId());
        assertEquals(budgetId, linked.getBudgetRecordId());

        ClubPointIncentiveRecordDO suggested = insertIncentive(ClubPointIncentiveStatusEnum.SUGGESTED.getStatus());
        assertServiceException(() -> budgetService.createBudget(buildManualBudgetReq()
                .setCategory(ClubPointBudgetCategoryEnum.INCENTIVE.getCategory())
                .setSourceType(ClubPointBudgetSourceTypeEnum.RANKING_INCENTIVE.getType())
                .setSourceId(suggested.getId())
                .setDescription("未确认激励不能登记预算")), CLUB_BUDGET_SOURCE_INVALID);
        assertNull(incentiveRecordMapper.selectById(suggested.getId()).getBudgetRecordId());
    }

    @Test
    void updateBudgetShouldWriteAuditAndAuditFailureShouldRollback() {
        Long budgetId = budgetService.createBudget(buildManualBudgetReq()
                .setBudgetAmountCent(180_000L)
                .setActualAmountCent(90_000L)
                .setDescription("更新前"));

        budgetService.updateBudget(buildManualBudgetReq()
                .setId(budgetId)
                .setBudgetAmountCent(220_000L)
                .setActualAmountCent(150_000L)
                .setDescription("更新后")
                .setReason("调整预算金额"));

        ClubPointBudgetRecordDO updated = budgetRecordMapper.selectById(budgetId);
        assertEquals(220_000L, updated.getBudgetAmountCent());
        assertEquals(150_000L, updated.getActualAmountCent());
        assertEquals("更新后", updated.getDescription());
        ClubAuditLogDO audit = findAudit(BUDGET_UPDATE);
        assertEquals("调整预算金额", audit.getReason());
        assertEquals(budgetId, audit.getBizId());

        assertServiceException(() -> budgetService.updateBudget(buildManualBudgetReq()
                .setId(budgetId)
                .setBudgetAmountCent(999_000L)
                .setDescription("审计失败不落库")
                .setOperatorNameSnapshot(null)), CLUB_AUDIT_WRITE_FAILED);
        ClubPointBudgetRecordDO unchanged = budgetRecordMapper.selectById(budgetId);
        assertEquals(220_000L, unchanged.getBudgetAmountCent());
        assertEquals("更新后", unchanged.getDescription());

        assertServiceException(() -> budgetService.updateBudget(buildManualBudgetReq()
                .setId(budgetId)
                .setSourceType(999)), CLUB_BUDGET_INVALID);
        ClubPointBudgetRecordDO stillValid = budgetRecordMapper.selectById(budgetId);
        assertEquals(ClubPointBudgetSourceTypeEnum.MANUAL.getType(), stillValid.getSourceType());
    }

    @Test
    void disableBudgetShouldSoftDeleteAndRemoveFromQuery() {
        Long budgetId = budgetService.createBudget(buildManualBudgetReq()
                .setBudgetAmountCent(180_000L)
                .setDescription("待停用预算"));

        budgetService.disableBudget(buildOperationReq(budgetId, "停用错误预算记录"));

        assertNull(budgetRecordMapper.selectById(budgetId));
        assertTrue(budgetService.listBudgetRecords(new ClubPointBudgetQueryReqBO()
                .setYear(2026)
                .setOperatorGlobalScope(true)).isEmpty());
        ClubAuditLogDO audit = findAudit(BUDGET_DISABLE);
        assertEquals(budgetId, audit.getBizId());
        assertEquals("停用错误预算记录", audit.getReason());
    }

    private static ClubPointBudgetSaveReqBO buildManualBudgetReq() {
        return new ClubPointBudgetSaveReqBO()
                .setCategory(ClubPointBudgetCategoryEnum.ACTIVITY.getCategory())
                .setBudgetAmountCent(100_000L)
                .setActualAmountCent(50_000L)
                .setOccurDate(OCCUR_DATE)
                .setHandlerUserId(9001L)
                .setSourceType(ClubPointBudgetSourceTypeEnum.MANUAL.getType())
                .setSourceId(null)
                .setDescription("预算记录")
                .setRemark("unit-test")
                .setOperatorGlobalScope(true)
                .setOperatorUserId(9001L)
                .setOperatorNameSnapshot("Budget Admin")
                .setOperatorRoleSnapshot("admin")
                .setOperationTime(OPERATION_TIME)
                .setClientIp("127.0.0.1")
                .setUserAgent("unit-test")
                .setReason("维护预算记录");
    }

    private static ClubPointBudgetOperationReqBO buildOperationReq(Long id, String reason) {
        return new ClubPointBudgetOperationReqBO()
                .setId(id)
                .setOperatorGlobalScope(true)
                .setOperatorUserId(9001L)
                .setOperatorNameSnapshot("Budget Admin")
                .setOperatorRoleSnapshot("admin")
                .setOperationTime(OPERATION_TIME)
                .setClientIp("127.0.0.1")
                .setUserAgent("unit-test")
                .setReason(reason);
    }

    private ClubPointIncentiveRecordDO insertIncentive(Integer status) {
        ClubPointIncentiveRecordDO incentive = new ClubPointIncentiveRecordDO()
                .setYear(2026)
                .setType(ClubPointIncentiveTypeEnum.RANKING.getType())
                .setClubId(100L + incentiveRecordMapper.selectCount())
                .setClubNameSnapshot("Ranking Club")
                .setTitle("2026年度俱乐部排名激励")
                .setAmountCent(200_000L)
                .setStatus(status)
                .setSourceType(ClubPointIncentiveSourceTypeEnum.ANNUAL_RANKING.getType())
                .setSourceId(700L + incentiveRecordMapper.selectCount())
                .setConfirmedBy(status.equals(ClubPointIncentiveStatusEnum.CONFIRMED.getStatus()) ? 9001L : null)
                .setConfirmedTime(status.equals(ClubPointIncentiveStatusEnum.CONFIRMED.getStatus()) ? OPERATION_TIME : null);
        incentiveRecordMapper.insert(incentive);
        return incentive;
    }

    private ClubAuditLogDO findAudit(String actionType) {
        return auditLogMapper.selectList().stream()
                .filter(audit -> actionType.equals(audit.getActionType()))
                .findFirst()
                .orElseThrow();
    }

}
