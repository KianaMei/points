package cn.iocoder.yudao.module.clubpoints.service.budget;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointIncentiveRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.budget.ClubPointBudgetRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointIncentiveRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.budget.ClubPointBudgetRecordMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointBudgetCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointBudgetSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetQueryReqBO;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.BUDGET_CREATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.BUDGET_DISABLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.BUDGET_UPDATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_BUDGET_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_BUDGET_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_BUDGET_SOURCE_INVALID;

/**
 * 预算和经费记录服务实现
 */
@Service
public class ClubPointBudgetServiceImpl implements ClubPointBudgetService {

    private static final String BIZ_TYPE_BUDGET_RECORD = "BUDGET_RECORD";

    @Resource
    private ClubPointBudgetRecordMapper budgetRecordMapper;
    @Resource
    private ClubPointIncentiveRecordMapper incentiveRecordMapper;
    @Resource
    private ClubAuditService clubAuditService;
    @Resource
    private ClubScopeService clubScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBudget(ClubPointBudgetSaveReqBO reqBO) {
        validateSaveReq(reqBO, false);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointIncentiveRecordDO linkedIncentive = validateAndLockSource(reqBO, null);
        ClubPointBudgetRecordDO budget = buildBudget(new ClubPointBudgetRecordDO(), reqBO);
        budgetRecordMapper.insert(budget);
        linkIncentive(linkedIncentive, budget.getId());
        createAudit(BUDGET_CREATE, budget.getId(), reqBO, null, snapshot(budget), snapshot(budget));
        return budget.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBudget(ClubPointBudgetSaveReqBO reqBO) {
        validateSaveReq(reqBO, true);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointBudgetRecordDO budget = validateBudgetExistsForUpdate(reqBO.getId());
        ClubPointIncentiveRecordDO linkedIncentive = validateAndLockSource(reqBO, budget.getId());
        String beforeJson = snapshot(budget);
        buildBudget(budget, reqBO);
        String afterJson = snapshot(budget);
        createAudit(BUDGET_UPDATE, budget.getId(), reqBO, beforeJson, afterJson, afterJson);
        budgetRecordMapper.updateById(budget);
        linkIncentive(linkedIncentive, budget.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableBudget(ClubPointBudgetOperationReqBO reqBO) {
        validateOperationReq(reqBO);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointBudgetRecordDO budget = validateBudgetExistsForUpdate(reqBO.getId());
        String beforeJson = snapshot(budget);
        createAudit(BUDGET_DISABLE, budget.getId(), reqBO, beforeJson, null, beforeJson);
        budgetRecordMapper.deleteById(budget.getId());
    }

    @Override
    public List<ClubPointBudgetRecordDO> listBudgetRecords(ClubPointBudgetQueryReqBO reqBO) {
        if (reqBO == null) {
            throw exception(CLUB_BUDGET_INVALID);
        }
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        LocalDate startDate = reqBO.getYear() == null ? null : LocalDate.of(reqBO.getYear(), 1, 1);
        LocalDate endDateExclusive = startDate == null ? null : startDate.plusYears(1);
        return budgetRecordMapper.selectListByQuery(reqBO.getCategory(), startDate, endDateExclusive,
                reqBO.getSourceType(), reqBO.getSourceId());
    }

    private ClubPointBudgetRecordDO validateBudgetExistsForUpdate(Long id) {
        ClubPointBudgetRecordDO budget = budgetRecordMapper.selectByIdForUpdate(id);
        if (budget == null) {
            throw exception(CLUB_BUDGET_NOT_EXISTS);
        }
        return budget;
    }

    private ClubPointIncentiveRecordDO validateAndLockSource(ClubPointBudgetSaveReqBO reqBO, Long currentBudgetId) {
        Integer sourceType = normalizeSourceType(reqBO.getSourceType());
        reqBO.setSourceType(sourceType);
        if (!ClubPointBudgetSourceTypeEnum.isValid(sourceType)) {
            throw exception(CLUB_BUDGET_INVALID);
        }
        if (!ClubPointBudgetSourceTypeEnum.RANKING_INCENTIVE.getType().equals(sourceType)) {
            return null;
        }
        if (reqBO.getSourceId() == null) {
            throw exception(CLUB_BUDGET_SOURCE_INVALID);
        }
        ClubPointBudgetRecordDO existing = budgetRecordMapper.selectBySourceTypeAndSourceId(sourceType, reqBO.getSourceId());
        if (existing != null && (currentBudgetId == null || !existing.getId().equals(currentBudgetId))) {
            throw exception(CLUB_BUDGET_SOURCE_INVALID);
        }
        ClubPointIncentiveRecordDO incentive = incentiveRecordMapper.selectByIdForUpdate(reqBO.getSourceId());
        if (incentive == null || !ClubPointIncentiveStatusEnum.CONFIRMED.getStatus().equals(incentive.getStatus())) {
            throw exception(CLUB_BUDGET_SOURCE_INVALID);
        }
        if (incentive.getBudgetRecordId() != null
                && (currentBudgetId == null || !incentive.getBudgetRecordId().equals(currentBudgetId))) {
            throw exception(CLUB_BUDGET_SOURCE_INVALID);
        }
        return incentive;
    }

    private void linkIncentive(ClubPointIncentiveRecordDO incentive, Long budgetId) {
        if (incentive == null) {
            return;
        }
        incentive.setBudgetRecordId(budgetId);
        incentiveRecordMapper.updateById(incentive);
    }

    private static Integer normalizeSourceType(Integer sourceType) {
        return sourceType == null ? ClubPointBudgetSourceTypeEnum.MANUAL.getType() : sourceType;
    }

    private static void validateSaveReq(ClubPointBudgetSaveReqBO reqBO, boolean requireId) {
        if (reqBO == null || (requireId && reqBO.getId() == null)
                || !ClubPointBudgetCategoryEnum.isValid(reqBO.getCategory())
                || reqBO.getBudgetAmountCent() == null || reqBO.getBudgetAmountCent() <= 0
                || reqBO.getOperatorUserId() == null) {
            throw exception(CLUB_BUDGET_INVALID);
        }
        if (reqBO.getActualAmountCent() != null && reqBO.getActualAmountCent() < 0) {
            throw exception(CLUB_BUDGET_INVALID);
        }
        if (reqBO.getSourceType() != null && !ClubPointBudgetSourceTypeEnum.isValid(reqBO.getSourceType())) {
            throw exception(CLUB_BUDGET_INVALID);
        }
    }

    private static void validateOperationReq(ClubPointBudgetOperationReqBO reqBO) {
        if (reqBO == null || reqBO.getId() == null || reqBO.getOperatorUserId() == null) {
            throw exception(CLUB_BUDGET_INVALID);
        }
    }

    private static ClubPointBudgetRecordDO buildBudget(ClubPointBudgetRecordDO budget, ClubPointBudgetSaveReqBO reqBO) {
        return budget.setCategory(reqBO.getCategory())
                .setBudgetAmountCent(reqBO.getBudgetAmountCent())
                .setActualAmountCent(reqBO.getActualAmountCent())
                .setOccurDate(reqBO.getOccurDate())
                .setHandlerUserId(reqBO.getHandlerUserId())
                .setSourceType(normalizeSourceType(reqBO.getSourceType()))
                .setSourceId(reqBO.getSourceId())
                .setDescription(reqBO.getDescription())
                .setRemark(reqBO.getRemark());
    }

    private Long createAudit(String actionType, Long budgetId, ClubPointBudgetSaveReqBO reqBO,
                             String beforeJson, String afterJson, String targetSnapshotJson) {
        return createAudit(actionType, budgetId, reqBO.getOperatorUserId(), reqBO.getOperatorNameSnapshot(),
                reqBO.getOperatorRoleSnapshot(), reqBO.getOperationTime(), reqBO.getClientIp(), reqBO.getUserAgent(),
                reqBO.getReason(), beforeJson, afterJson, targetSnapshotJson);
    }

    private Long createAudit(String actionType, Long budgetId, ClubPointBudgetOperationReqBO reqBO,
                             String beforeJson, String afterJson, String targetSnapshotJson) {
        return createAudit(actionType, budgetId, reqBO.getOperatorUserId(), reqBO.getOperatorNameSnapshot(),
                reqBO.getOperatorRoleSnapshot(), reqBO.getOperationTime(), reqBO.getClientIp(), reqBO.getUserAgent(),
                reqBO.getReason(), beforeJson, afterJson, targetSnapshotJson);
    }

    private Long createAudit(String actionType, Long budgetId, Long operatorUserId, String operatorNameSnapshot,
                             String operatorRoleSnapshot, LocalDateTime operationTime, String clientIp,
                             String userAgent, String reason, String beforeJson, String afterJson,
                             String targetSnapshotJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(actionType)
                .setBizType(BIZ_TYPE_BUDGET_RECORD)
                .setBizId(budgetId)
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot(operatorNameSnapshot)
                .setOperatorRoleSnapshot(operatorRoleSnapshot)
                .setOperationTime(operationTime == null ? LocalDateTime.now() : operationTime)
                .setClientIp(clientIp)
                .setUserAgent(userAgent)
                .setReason(reason)
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(targetSnapshotJson)
                .setSuccess(true));
    }

    private static String snapshot(ClubPointBudgetRecordDO budget) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", budget.getId());
        snapshot.put("category", budget.getCategory());
        snapshot.put("budgetAmountCent", budget.getBudgetAmountCent());
        snapshot.put("actualAmountCent", budget.getActualAmountCent());
        snapshot.put("occurDate", budget.getOccurDate());
        snapshot.put("handlerUserId", budget.getHandlerUserId());
        snapshot.put("sourceType", budget.getSourceType());
        snapshot.put("sourceId", budget.getSourceId());
        snapshot.put("description", budget.getDescription());
        snapshot.put("remark", budget.getRemark());
        return JsonUtils.toJsonString(snapshot);
    }

}
