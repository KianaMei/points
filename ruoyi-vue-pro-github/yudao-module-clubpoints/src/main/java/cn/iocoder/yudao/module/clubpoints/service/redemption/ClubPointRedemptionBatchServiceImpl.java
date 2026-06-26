package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionEligibilitySnapshotMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionBatchStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.REDEMPTION_BATCH_RULE_UPDATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_CLOSED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_RULE_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_TIME_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_ELIGIBILITY_SNAPSHOT_DUPLICATED;

/**
 * 兑换批次服务实现
 */
@Service
public class ClubPointRedemptionBatchServiceImpl implements ClubPointRedemptionBatchService {

    private static final String BIZ_TYPE_REDEMPTION_BATCH = "REDEMPTION_BATCH";
    private static final String QUALIFIED_REASON = "满足资格规则";
    private static final String TIE_QUALIFIED_REASON = "并列 cutoff 进入";
    private static final String BELOW_MIN_REASON = "低于最低可用积分";
    private static final String OUT_OF_RANK_REASON = "超出资格排名";

    @Resource
    private ClubPointRedemptionBatchMapper batchMapper;
    @Resource
    private ClubPointRedemptionEligibilitySnapshotMapper eligibilitySnapshotMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubAuditService clubAuditService;
    @Resource
    private ClubScopeService clubScopeService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @Override
    public PageResult<ClubPointRedemptionBatchDO> getAdminBatchPage(boolean operatorGlobalScope,
                                                                    ClubPointRedemptionBatchPageReqBO reqBO) {
        clubScopeService.validateGlobal(operatorGlobalScope);
        return batchMapper.selectPage(reqBO, reqBO.getYear(), reqBO.getStatus(), reqBO.getKeyword(), null);
    }

    @Override
    public PageResult<ClubPointRedemptionBatchDO> getAppOpenBatchPage(ClubPointRedemptionBatchPageReqBO reqBO) {
        return batchMapper.selectPage(reqBO, reqBO.getYear(),
                ClubPointRedemptionBatchStatusEnum.OPENED.getStatus(), reqBO.getKeyword(), null);
    }

    @Override
    public Long createBatch(ClubPointRedemptionBatchSaveReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        validateSaveReq(reqBO);
        ClubPointRedemptionBatchDO batch = buildBatch(reqBO)
                .setStatus(ClubPointRedemptionBatchStatusEnum.DRAFT.getStatus())
                .setSnapshotGenerated(false)
                .setSnapshotGeneratedTime(null);
        batchMapper.insert(batch);
        return batch.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatch(ClubPointRedemptionBatchSaveReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        validateSaveReq(reqBO);
        ClubPointRedemptionBatchDO batch = validateBatchExistsForUpdate(reqBO.getId());
        if (!ClubPointRedemptionBatchStatusEnum.DRAFT.getStatus().equals(batch.getStatus())) {
            throw exception(CLUB_REDEMPTION_BATCH_STATUS_INVALID);
        }
        String beforeJson = snapshot(batch);
        boolean qualificationRuleChanged = qualificationRuleChanged(batch, reqBO);
        updateBatchFields(batch, reqBO);
        batchMapper.updateById(batch);
        if (qualificationRuleChanged) {
            createRuleUpdateAudit(reqBO, batch.getId(), beforeJson, snapshot(batch));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void openBatch(Long batchId, ClubPointRedemptionBatchOperationReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointRedemptionBatchDO batch = validateBatchExistsForUpdate(batchId);
        if (!ClubPointRedemptionBatchStatusEnum.DRAFT.getStatus().equals(batch.getStatus())) {
            throw exception(CLUB_REDEMPTION_BATCH_STATUS_INVALID);
        }
        LocalDateTime operationTime = LocalDateTime.now();
        generateEligibilitySnapshots(batch, operationTime);
        batch.setStatus(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus())
                .setSnapshotGenerated(true)
                .setSnapshotGeneratedTime(operationTime);
        batchMapper.updateById(batch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeBatch(Long batchId, ClubPointRedemptionBatchOperationReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointRedemptionBatchDO batch = validateBatchExistsForUpdate(batchId);
        if (!ClubPointRedemptionBatchStatusEnum.OPENED.getStatus().equals(batch.getStatus())) {
            throw exception(CLUB_REDEMPTION_BATCH_STATUS_INVALID);
        }
        batch.setStatus(ClubPointRedemptionBatchStatusEnum.CLOSED.getStatus());
        batchMapper.updateById(batch);
    }

    @Override
    public void validateBatchOpenForApply(Long batchId) {
        ClubPointRedemptionBatchDO batch = validateBatchExists(batchId);
        if (!ClubPointRedemptionBatchStatusEnum.OPENED.getStatus().equals(batch.getStatus())) {
            throw exception(CLUB_REDEMPTION_BATCH_CLOSED);
        }
    }

    private void generateEligibilitySnapshots(ClubPointRedemptionBatchDO batch, LocalDateTime generatedTime) {
        if (Boolean.TRUE.equals(batch.getSnapshotGenerated())
                || eligibilitySnapshotMapper.selectCountByBatchId(batch.getId()) > 0) {
            throw exception(CLUB_REDEMPTION_ELIGIBILITY_SNAPSHOT_DUPLICATED);
        }
        List<ClubPointAccountDO> accounts = accountMapper.selectListForEligibilitySnapshot();
        Map<Long, AdminUserRespDTO> userMap = getUserMap(accounts);
        Map<Long, DeptRespDTO> deptMap = getDeptMap(userMap);
        Integer cutoffPoints = calculateCutoffPoints(batch, accounts);

        for (int i = 0; i < accounts.size(); i++) {
            ClubPointAccountDO account = accounts.get(i);
            int rankNo = i + 1;
            boolean meetsMin = safeInt(account.getAvailablePoints()) >= batch.getMinAvailablePoints();
            boolean tieAtCutoff = meetsMin && Boolean.TRUE.equals(batch.getIncludeTieAtCutoff())
                    && cutoffPoints != null && rankNo > batch.getQualifiedCount()
                    && Objects.equals(account.getAvailablePoints(), cutoffPoints);
            boolean qualified = meetsMin && (rankNo <= batch.getQualifiedCount() || tieAtCutoff);
            eligibilitySnapshotMapper.insert(buildEligibilitySnapshot(batch, account, userMap, deptMap,
                    rankNo, qualified, tieAtCutoff, generatedTime));
        }
    }

    private static Integer calculateCutoffPoints(ClubPointRedemptionBatchDO batch, List<ClubPointAccountDO> accounts) {
        int eligibleRank = 0;
        for (ClubPointAccountDO account : accounts) {
            if (safeInt(account.getAvailablePoints()) < batch.getMinAvailablePoints()) {
                continue;
            }
            eligibleRank++;
            if (eligibleRank == batch.getQualifiedCount()) {
                return account.getAvailablePoints();
            }
        }
        return null;
    }

    private static ClubPointRedemptionEligibilitySnapshotDO buildEligibilitySnapshot(
            ClubPointRedemptionBatchDO batch, ClubPointAccountDO account,
            Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap,
            Integer rankNo, boolean qualified, boolean tieAtCutoff, LocalDateTime generatedTime) {
        AdminUserRespDTO user = userMap.get(account.getUserId());
        DeptRespDTO dept = user == null || user.getDeptId() == null ? null : deptMap.get(user.getDeptId());
        return new ClubPointRedemptionEligibilitySnapshotDO()
                .setBatchId(batch.getId())
                .setUserId(account.getUserId())
                .setUserNameSnapshot(userName(account.getUserId(), user))
                .setDeptNameSnapshot(dept == null ? null : dept.getName())
                .setNetPointsSnapshot(safeInt(account.getNetPoints()))
                .setFrozenPointsSnapshot(safeInt(account.getFrozenPoints()))
                .setAvailablePointsSnapshot(safeInt(account.getAvailablePoints()))
                .setAnnualEarnedPointsSnapshot(safeInt(account.getAnnualEarnedPoints()))
                .setRankNo(rankNo)
                .setQualified(qualified)
                .setQualificationReason(qualificationReason(account, batch, qualified, tieAtCutoff))
                .setTieAtCutoff(tieAtCutoff)
                .setGeneratedTime(generatedTime);
    }

    private static String qualificationReason(ClubPointAccountDO account, ClubPointRedemptionBatchDO batch,
                                              boolean qualified, boolean tieAtCutoff) {
        if (tieAtCutoff) {
            return TIE_QUALIFIED_REASON;
        }
        if (qualified) {
            return QUALIFIED_REASON;
        }
        if (safeInt(account.getAvailablePoints()) < batch.getMinAvailablePoints()) {
            return BELOW_MIN_REASON;
        }
        return OUT_OF_RANK_REASON;
    }

    private Map<Long, AdminUserRespDTO> getUserMap(List<ClubPointAccountDO> accounts) {
        Set<Long> userIds = new LinkedHashSet<>();
        for (ClubPointAccountDO account : accounts) {
            userIds.add(account.getUserId());
        }
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return adminUserApi.getUserMap(userIds);
    }

    private Map<Long, DeptRespDTO> getDeptMap(Map<Long, AdminUserRespDTO> userMap) {
        Set<Long> deptIds = new LinkedHashSet<>();
        for (AdminUserRespDTO user : userMap.values()) {
            if (user.getDeptId() != null) {
                deptIds.add(user.getDeptId());
            }
        }
        if (deptIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return deptApi.getDeptMap(deptIds);
    }

    private ClubPointRedemptionBatchDO validateBatchExists(Long batchId) {
        ClubPointRedemptionBatchDO batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw exception(CLUB_REDEMPTION_BATCH_NOT_EXISTS);
        }
        return batch;
    }

    private ClubPointRedemptionBatchDO validateBatchExistsForUpdate(Long batchId) {
        ClubPointRedemptionBatchDO batch = batchMapper.selectByIdForUpdate(batchId);
        if (batch == null) {
            throw exception(CLUB_REDEMPTION_BATCH_NOT_EXISTS);
        }
        return batch;
    }

    private static void validateSaveReq(ClubPointRedemptionBatchSaveReqBO reqBO) {
        if (reqBO == null || reqBO.getOpenTime() == null || reqBO.getCloseTime() == null
                || !reqBO.getOpenTime().isBefore(reqBO.getCloseTime())) {
            throw exception(CLUB_REDEMPTION_BATCH_TIME_INVALID);
        }
        if (reqBO.getMinAvailablePoints() == null || reqBO.getMinAvailablePoints() < 0
                || reqBO.getQualifiedCount() == null || reqBO.getQualifiedCount() <= 0
                || reqBO.getIncludeTieAtCutoff() == null || reqBO.getRuleVersionId() == null
                || !StringUtils.hasText(reqBO.getQualificationRuleJson())
                || !StringUtils.hasText(reqBO.getRuleSnapshotJson())) {
            throw exception(CLUB_REDEMPTION_BATCH_RULE_INVALID);
        }
    }

    private static ClubPointRedemptionBatchDO buildBatch(ClubPointRedemptionBatchSaveReqBO reqBO) {
        return updateBatchFields(new ClubPointRedemptionBatchDO(), reqBO);
    }

    private static ClubPointRedemptionBatchDO updateBatchFields(ClubPointRedemptionBatchDO batch,
                                                               ClubPointRedemptionBatchSaveReqBO reqBO) {
        return batch.setYear(reqBO.getYear())
                .setName(reqBO.getName())
                .setOpenTime(reqBO.getOpenTime())
                .setCloseTime(reqBO.getCloseTime())
                .setDescription(reqBO.getDescription())
                .setMinAvailablePoints(reqBO.getMinAvailablePoints())
                .setQualifiedCount(reqBO.getQualifiedCount())
                .setIncludeTieAtCutoff(reqBO.getIncludeTieAtCutoff())
                .setQualificationRuleJson(reqBO.getQualificationRuleJson())
                .setRuleVersionId(reqBO.getRuleVersionId())
                .setRuleSnapshotJson(reqBO.getRuleSnapshotJson());
    }

    private static boolean qualificationRuleChanged(ClubPointRedemptionBatchDO batch,
                                                    ClubPointRedemptionBatchSaveReqBO reqBO) {
        return !Objects.equals(batch.getMinAvailablePoints(), reqBO.getMinAvailablePoints())
                || !Objects.equals(batch.getQualifiedCount(), reqBO.getQualifiedCount())
                || !Objects.equals(batch.getIncludeTieAtCutoff(), reqBO.getIncludeTieAtCutoff())
                || !Objects.equals(batch.getQualificationRuleJson(), reqBO.getQualificationRuleJson())
                || !Objects.equals(batch.getRuleVersionId(), reqBO.getRuleVersionId())
                || !Objects.equals(batch.getRuleSnapshotJson(), reqBO.getRuleSnapshotJson());
    }

    private Long createRuleUpdateAudit(ClubPointRedemptionBatchSaveReqBO reqBO, Long batchId,
                                       String beforeJson, String afterJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(REDEMPTION_BATCH_RULE_UPDATE)
                .setBizType(BIZ_TYPE_REDEMPTION_BATCH)
                .setBizId(batchId)
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(LocalDateTime.now())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setSuccess(true));
    }

    private static String snapshot(ClubPointRedemptionBatchDO batch) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", batch.getId());
        snapshot.put("year", batch.getYear());
        snapshot.put("name", batch.getName());
        snapshot.put("status", batch.getStatus());
        snapshot.put("openTime", batch.getOpenTime());
        snapshot.put("closeTime", batch.getCloseTime());
        snapshot.put("description", batch.getDescription());
        snapshot.put("minAvailablePoints", batch.getMinAvailablePoints());
        snapshot.put("qualifiedCount", batch.getQualifiedCount());
        snapshot.put("includeTieAtCutoff", batch.getIncludeTieAtCutoff());
        snapshot.put("qualificationRuleJson", batch.getQualificationRuleJson());
        snapshot.put("ruleVersionId", batch.getRuleVersionId());
        snapshot.put("ruleSnapshotJson", batch.getRuleSnapshotJson());
        snapshot.put("snapshotGenerated", batch.getSnapshotGenerated());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String userName(Long userId, AdminUserRespDTO user) {
        if (user != null && StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        return "用户" + userId;
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

}
