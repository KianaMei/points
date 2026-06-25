package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityPointConfigVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityPointConfigVersionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityReviewRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivitySaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivitySubmitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentService;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubOperationReq;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleResolveService;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleSnapshotBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.BIZ_TYPE_ACTIVITY;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ACTIVITY_CANCEL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ACTIVITY_KEY_FIELD_UPDATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_LEVEL_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_TIME_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_DISABLED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;

/**
 * 活动管理服务实现
 */
@Service
public class ClubPointActivityServiceImpl implements ClubPointActivityService {

    private static final int REVIEW_RESULT_APPROVED = 1;
    private static final int REVIEW_RESULT_REJECTED = 2;

    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityReviewRecordMapper reviewRecordMapper;
    @Resource
    private ClubPointActivityPointConfigVersionMapper configVersionMapper;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubScopeService clubScopeService;
    @Resource
    private ClubAuditService clubAuditService;
    @Resource
    private ClubAttachmentService clubAttachmentService;
    @Resource
    private ClubPointRuleResolveService ruleResolveService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDraft(ClubPointActivitySaveReqBO reqBO) {
        validateSaveReq(reqBO);
        ClubPointClubDO club = validateEnabledClub(reqBO.getClubId());
        validateOperatorScope(reqBO, reqBO.getOperatorGlobalScope(), club.getId());
        ActivityPointConfig pointConfig = extractPointConfig(reqBO, null);
        ClubPointActivityDO activity = buildActivity(reqBO, club)
                .setStatus(ClubPointActivityStatusEnum.DRAFT.getStatus())
                .setCreatorUserId(reqBO.getOperatorUserId());
        activityMapper.insert(activity);
        activity.setSnapshotJson(snapshot(activity, pointConfig));
        activityMapper.updateById(activity);
        return activity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(ClubPointActivitySubmitReqBO reqBO) {
        ClubPointActivityDO activity = validateActivityExists(reqBO.getId());
        validateOperatorScope(reqBO, reqBO.getOperatorGlobalScope(), activity.getClubId());
        validateTransition(activity, ClubPointActivityStatusEnum.PENDING_REVIEW);
        ActivityPointConfig pointConfig = extractPointConfig(activity);
        activity.setStatus(ClubPointActivityStatusEnum.PENDING_REVIEW.getStatus())
                .setSubmitTime(LocalDateTime.now());
        activity.setSnapshotJson(snapshot(activity, pointConfig));
        activityMapper.updateById(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveReview(ClubPointActivityReviewReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointActivityDO activity = validateActivityExists(reqBO.getId());
        validatePendingReview(activity);
        LocalDateTime operationTime = LocalDateTime.now();
        activity.setStatus(ClubPointActivityStatusEnum.PUBLISHED.getStatus())
                .setPublishTime(operationTime);
        ClubPointActivityPointConfigVersionDO configVersion = createConfigVersion(activity,
                extractPointConfig(activity), reqBO.getReason(), operationTime);
        activity.setCurrentConfigVersionId(configVersion.getId())
                .setSnapshotJson(snapshot(activity, toPointConfig(configVersion)));
        activityMapper.updateById(activity);
        clubAttachmentService.lockBizAttachments(BIZ_TYPE_ACTIVITY, activity.getId());
        insertReviewRecord(activity, reqBO, REVIEW_RESULT_APPROVED, operationTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectReview(ClubPointActivityReviewReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointActivityDO activity = validateActivityExists(reqBO.getId());
        validatePendingReview(activity);
        LocalDateTime operationTime = LocalDateTime.now();
        ActivityPointConfig pointConfig = extractPointConfig(activity);
        activity.setStatus(ClubPointActivityStatusEnum.REJECTED.getStatus())
                .setSnapshotJson(snapshot(activity, pointConfig));
        activityMapper.updateById(activity);
        insertReviewRecord(activity, reqBO, REVIEW_RESULT_REJECTED, operationTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(ClubPointActivitySaveReqBO reqBO) {
        validateSaveReq(reqBO);
        ClubPointActivityDO activity = validateActivityExists(reqBO.getId());
        if (!Objects.equals(activity.getClubId(), reqBO.getClubId())) {
            throw exception(CLUB_SCOPE_DENIED);
        }
        validateOperatorScope(reqBO, reqBO.getOperatorGlobalScope(), activity.getClubId());
        ClubPointActivityStatusEnum currentStatus = validateUpdatableStatus(activity);
        ActivityPointConfig beforeConfig = extractPointConfig(activity);
        ActivityPointConfig afterConfig = extractPointConfig(reqBO, beforeConfig);
        String beforeJson = snapshot(activity, beforeConfig);
        boolean publishedKeyChanged = currentStatus == ClubPointActivityStatusEnum.PUBLISHED
                && hasKeyFieldChanged(activity, beforeConfig, reqBO, afterConfig);

        updateActivityFields(activity, reqBO);
        if (publishedKeyChanged) {
            ClubPointActivityPointConfigVersionDO configVersion = createConfigVersion(activity,
                    afterConfig, reqBO.getReason(), LocalDateTime.now());
            activity.setCurrentConfigVersionId(configVersion.getId());
            afterConfig = toPointConfig(configVersion);
        }
        activity.setSnapshotJson(snapshot(activity, afterConfig));
        activityMapper.updateById(activity);

        if (publishedKeyChanged) {
            createAudit(ACTIVITY_KEY_FIELD_UPDATE, activity, reqBO, beforeJson, snapshot(activity, afterConfig));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelActivity(ClubPointActivityCancelReqBO reqBO) {
        ClubPointActivityDO activity = validateActivityExists(reqBO.getId());
        validateOperatorScope(reqBO, reqBO.getOperatorGlobalScope(), activity.getClubId());
        validateTransition(activity, ClubPointActivityStatusEnum.CANCELED);
        ActivityPointConfig pointConfig = extractPointConfig(activity);
        String beforeJson = snapshot(activity, pointConfig);
        activity.setStatus(ClubPointActivityStatusEnum.CANCELED.getStatus())
                .setCancelTime(LocalDateTime.now())
                .setCancelReason(reqBO.getReason());
        String afterJson = snapshot(activity, pointConfig);
        activity.setSnapshotJson(afterJson);
        activityMapper.updateById(activity);
        createAudit(ACTIVITY_CANCEL, activity, reqBO, beforeJson, afterJson);
    }

    private ClubPointActivityDO validateActivityExists(Long activityId) {
        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw exception(CLUB_ACTIVITY_NOT_FOUND);
        }
        return activity;
    }

    private ClubPointClubDO validateEnabledClub(Long clubId) {
        ClubPointClubDO club = clubMapper.selectById(clubId);
        if (club == null) {
            throw exception(CLUB_NOT_FOUND);
        }
        if (!ClubPointClubStatusEnum.ENABLED.getStatus().equals(club.getStatus())) {
            throw exception(CLUB_DISABLED);
        }
        return club;
    }

    private void validateOperatorScope(ClubPointClubOperationReq reqBO, Boolean operatorGlobalScope, Long clubId) {
        if (Boolean.TRUE.equals(operatorGlobalScope)) {
            clubScopeService.validateGlobal(true);
            return;
        }
        clubScopeService.validateManagedClub(reqBO.getOperatorUserId(), clubId);
    }

    private static void validateTransition(ClubPointActivityDO activity, ClubPointActivityStatusEnum targetStatus) {
        ClubPointActivityStatusEnum currentStatus = ClubPointActivityStatusEnum.of(activity.getStatus());
        if (currentStatus == null || !currentStatus.canTransitionTo(targetStatus)) {
            throw exception(CLUB_ACTIVITY_STATUS_INVALID);
        }
    }

    private static void validatePendingReview(ClubPointActivityDO activity) {
        if (!ClubPointActivityStatusEnum.PENDING_REVIEW.getStatus().equals(activity.getStatus())) {
            throw exception(CLUB_ACTIVITY_STATUS_INVALID);
        }
    }

    private static ClubPointActivityStatusEnum validateUpdatableStatus(ClubPointActivityDO activity) {
        ClubPointActivityStatusEnum currentStatus = ClubPointActivityStatusEnum.of(activity.getStatus());
        if (currentStatus == null || !currentStatus.canUpdateKeyFields()) {
            throw exception(CLUB_ACTIVITY_STATUS_INVALID);
        }
        return currentStatus;
    }

    private static void validateSaveReq(ClubPointActivitySaveReqBO reqBO) {
        if (reqBO == null || reqBO.getClubId() == null || !StringUtils.hasText(reqBO.getTitle())
                || reqBO.getLevel() == null || reqBO.getBasePoints() == null || reqBO.getStartTime() == null
                || reqBO.getEndTime() == null || reqBO.getRegistrationDeadline() == null
                || reqBO.getCheckinStartTime() == null || reqBO.getCheckinEndTime() == null
                || reqBO.getCheckoutMode() == null || reqBO.getCheckoutStartTime() == null
                || reqBO.getCheckoutEndTime() == null) {
            throw exception(CLUB_ACTIVITY_TIME_INVALID);
        }
        if (!reqBO.getStartTime().isBefore(reqBO.getEndTime())
                || reqBO.getRegistrationDeadline().isAfter(reqBO.getStartTime())
                || (reqBO.getCancelDeadlineTime() != null
                && reqBO.getCancelDeadlineTime().isAfter(reqBO.getStartTime()))
                || reqBO.getCheckinStartTime().isAfter(reqBO.getCheckinEndTime())
                || reqBO.getCheckoutStartTime().isAfter(reqBO.getCheckoutEndTime())
                || reqBO.getBasePoints() < 0
                || (reqBO.getFullExtraPoints() != null && reqBO.getFullExtraPoints() < 0)) {
            throw exception(CLUB_ACTIVITY_TIME_INVALID);
        }
        resolveBaseRuleCode(reqBO.getLevel());
    }

    private static ClubPointActivityDO buildActivity(ClubPointActivitySaveReqBO reqBO, ClubPointClubDO club) {
        return updateActivityFields(new ClubPointActivityDO()
                .setClubId(club.getId())
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName()), reqBO);
    }

    private static ClubPointActivityDO updateActivityFields(ClubPointActivityDO activity,
                                                           ClubPointActivitySaveReqBO reqBO) {
        return activity.setTitle(reqBO.getTitle())
                .setLocation(reqBO.getLocation())
                .setDescription(reqBO.getDescription())
                .setCoverFileId(reqBO.getCoverFileId())
                .setLevel(reqBO.getLevel())
                .setStartTime(reqBO.getStartTime())
                .setEndTime(reqBO.getEndTime())
                .setRegistrationDeadline(reqBO.getRegistrationDeadline())
                .setCancelDeadlineTime(reqBO.getCancelDeadlineTime())
                .setCheckinStartTime(reqBO.getCheckinStartTime())
                .setCheckinEndTime(reqBO.getCheckinEndTime())
                .setCheckoutMode(reqBO.getCheckoutMode())
                .setCheckoutStartTime(reqBO.getCheckoutStartTime())
                .setCheckoutEndTime(reqBO.getCheckoutEndTime())
                .setRemark(reqBO.getRemark());
    }

    private ClubPointActivityPointConfigVersionDO createConfigVersion(ClubPointActivityDO activity,
                                                                      ActivityPointConfig pointConfig,
                                                                      String reason,
                                                                      LocalDateTime effectiveTime) {
        Long ruleVersionId = resolveRuleVersionId(pointConfig, activity.getStartTime());
        ClubPointRuleSnapshotBO baseSnapshot = ruleResolveService.snapshotRuleItem(ruleVersionId,
                resolveBaseRuleCode(pointConfig.getLevel()), pointConfig.getBasePoints());
        ClubPointRuleSnapshotBO fullSnapshot = pointConfig.getFullExtraPoints() > 0
                ? ruleResolveService.snapshotRuleItem(ruleVersionId,
                ClubPointRuleItemCodeEnum.ACTIVITY_FULL_EXTRA.getCode(), pointConfig.getFullExtraPoints()) : null;
        deactivateActiveConfigs(activity.getId());
        ClubPointActivityPointConfigVersionDO configVersion = new ClubPointActivityPointConfigVersionDO()
                .setActivityId(activity.getId())
                .setVersionNo(nextConfigVersionNo(activity.getId()))
                .setLevel(pointConfig.getLevel())
                .setBasePoints(pointConfig.getBasePoints())
                .setFullExtraPoints(pointConfig.getFullExtraPoints())
                .setRuleVersionId(ruleVersionId)
                .setBaseRuleItemId(baseSnapshot.getRuleItemId())
                .setFullRuleItemId(fullSnapshot != null ? fullSnapshot.getRuleItemId() : null)
                .setEffectiveTime(effectiveTime)
                .setCreatedReason(reason)
                .setActive(true)
                .setRuleSnapshotJson(configRuleSnapshotJson(baseSnapshot, fullSnapshot));
        configVersionMapper.insert(configVersion);
        return configVersion;
    }

    private Long resolveRuleVersionId(ActivityPointConfig pointConfig, LocalDateTime occurredAt) {
        if (pointConfig.getRuleVersionId() != null) {
            return pointConfig.getRuleVersionId();
        }
        ClubPointRuleVersionDO version = ruleResolveService.getEffectiveVersion(occurredAt);
        return version.getId();
    }

    private void deactivateActiveConfigs(Long activityId) {
        List<ClubPointActivityPointConfigVersionDO> activeConfigs = configVersionMapper.selectList(
                new LambdaQueryWrapperX<ClubPointActivityPointConfigVersionDO>()
                        .eq(ClubPointActivityPointConfigVersionDO::getActivityId, activityId)
                        .eq(ClubPointActivityPointConfigVersionDO::getActive, true));
        for (ClubPointActivityPointConfigVersionDO activeConfig : activeConfigs) {
            configVersionMapper.updateById(new ClubPointActivityPointConfigVersionDO()
                    .setId(activeConfig.getId())
                    .setActive(false));
        }
    }

    private int nextConfigVersionNo(Long activityId) {
        List<ClubPointActivityPointConfigVersionDO> configs = configVersionMapper.selectList(
                new LambdaQueryWrapperX<ClubPointActivityPointConfigVersionDO>()
                        .eq(ClubPointActivityPointConfigVersionDO::getActivityId, activityId)
                        .orderByDesc(ClubPointActivityPointConfigVersionDO::getVersionNo)
                        .last("LIMIT 1"));
        return configs.isEmpty() ? 1 : configs.get(0).getVersionNo() + 1;
    }

    private void insertReviewRecord(ClubPointActivityDO activity, ClubPointActivityReviewReqBO reqBO,
                                    Integer result, LocalDateTime reviewTime) {
        reviewRecordMapper.insert(new ClubPointActivityReviewRecordDO()
                .setActivityId(activity.getId())
                .setReviewerUserId(reqBO.getOperatorUserId())
                .setResult(result)
                .setReason(reqBO.getReason())
                .setReviewTime(reviewTime)
                .setActivitySnapshotJson(activity.getSnapshotJson())
                .setAuditLogId(null));
    }

    private Long createAudit(String actionType, ClubPointActivityDO activity, ClubPointClubOperationReq reqBO,
                             String beforeJson, String afterJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(actionType)
                .setBizType(BIZ_TYPE_ACTIVITY)
                .setBizId(activity.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(LocalDateTime.now())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(afterJson)
                .setSuccess(true));
    }

    private static boolean hasKeyFieldChanged(ClubPointActivityDO activity, ActivityPointConfig beforeConfig,
                                              ClubPointActivitySaveReqBO reqBO, ActivityPointConfig afterConfig) {
        return !Objects.equals(activity.getLevel(), reqBO.getLevel())
                || !Objects.equals(activity.getStartTime(), reqBO.getStartTime())
                || !Objects.equals(activity.getEndTime(), reqBO.getEndTime())
                || !Objects.equals(activity.getRegistrationDeadline(), reqBO.getRegistrationDeadline())
                || !Objects.equals(activity.getCancelDeadlineTime(), reqBO.getCancelDeadlineTime())
                || !Objects.equals(activity.getCheckinStartTime(), reqBO.getCheckinStartTime())
                || !Objects.equals(activity.getCheckinEndTime(), reqBO.getCheckinEndTime())
                || !Objects.equals(activity.getCheckoutMode(), reqBO.getCheckoutMode())
                || !Objects.equals(activity.getCheckoutStartTime(), reqBO.getCheckoutStartTime())
                || !Objects.equals(activity.getCheckoutEndTime(), reqBO.getCheckoutEndTime())
                || !Objects.equals(beforeConfig.getRuleVersionId(), afterConfig.getRuleVersionId())
                || !Objects.equals(beforeConfig.getBasePoints(), afterConfig.getBasePoints())
                || !Objects.equals(beforeConfig.getFullExtraPoints(), afterConfig.getFullExtraPoints());
    }

    private static ActivityPointConfig extractPointConfig(ClubPointActivitySaveReqBO reqBO,
                                                          ActivityPointConfig fallback) {
        Long ruleVersionId = reqBO.getRuleVersionId() != null ? reqBO.getRuleVersionId()
                : fallback != null ? fallback.getRuleVersionId() : null;
        return new ActivityPointConfig()
                .setRuleVersionId(ruleVersionId)
                .setLevel(reqBO.getLevel())
                .setBasePoints(reqBO.getBasePoints())
                .setFullExtraPoints(reqBO.getFullExtraPoints() == null ? 0 : reqBO.getFullExtraPoints());
    }

    private static ActivityPointConfig extractPointConfig(ClubPointActivityDO activity) {
        Map<String, Object> snapshot = parseSnapshot(activity.getSnapshotJson());
        return new ActivityPointConfig()
                .setRuleVersionId(toLong(snapshot.get("ruleVersionId")))
                .setLevel(toInteger(snapshot.get("level"), activity.getLevel()))
                .setBasePoints(toInteger(snapshot.get("basePoints"), 0))
                .setFullExtraPoints(toInteger(snapshot.get("fullExtraPoints"), 0));
    }

    private static ActivityPointConfig toPointConfig(ClubPointActivityPointConfigVersionDO configVersion) {
        return new ActivityPointConfig()
                .setRuleVersionId(configVersion.getRuleVersionId())
                .setLevel(configVersion.getLevel())
                .setBasePoints(configVersion.getBasePoints())
                .setFullExtraPoints(configVersion.getFullExtraPoints());
    }

    private static String snapshot(ClubPointActivityDO activity, ActivityPointConfig pointConfig) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", activity.getId());
        snapshot.put("clubId", activity.getClubId());
        snapshot.put("clubCodeSnapshot", activity.getClubCodeSnapshot());
        snapshot.put("clubNameSnapshot", activity.getClubNameSnapshot());
        snapshot.put("title", activity.getTitle());
        snapshot.put("location", activity.getLocation());
        snapshot.put("description", activity.getDescription());
        snapshot.put("coverFileId", activity.getCoverFileId());
        snapshot.put("level", pointConfig.getLevel());
        snapshot.put("status", activity.getStatus());
        snapshot.put("startTime", activity.getStartTime());
        snapshot.put("endTime", activity.getEndTime());
        snapshot.put("registrationDeadline", activity.getRegistrationDeadline());
        snapshot.put("cancelDeadlineTime", activity.getCancelDeadlineTime());
        snapshot.put("checkinStartTime", activity.getCheckinStartTime());
        snapshot.put("checkinEndTime", activity.getCheckinEndTime());
        snapshot.put("checkoutMode", activity.getCheckoutMode());
        snapshot.put("checkoutStartTime", activity.getCheckoutStartTime());
        snapshot.put("checkoutEndTime", activity.getCheckoutEndTime());
        snapshot.put("currentConfigVersionId", activity.getCurrentConfigVersionId());
        snapshot.put("creatorUserId", activity.getCreatorUserId());
        snapshot.put("submitTime", activity.getSubmitTime());
        snapshot.put("publishTime", activity.getPublishTime());
        snapshot.put("cancelTime", activity.getCancelTime());
        snapshot.put("cancelReason", activity.getCancelReason());
        snapshot.put("ruleVersionId", pointConfig.getRuleVersionId());
        snapshot.put("basePoints", pointConfig.getBasePoints());
        snapshot.put("fullExtraPoints", pointConfig.getFullExtraPoints());
        snapshot.put("remark", activity.getRemark());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String configRuleSnapshotJson(ClubPointRuleSnapshotBO baseSnapshot,
                                                 ClubPointRuleSnapshotBO fullSnapshot) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("base", parseSnapshot(baseSnapshot.getRuleSnapshotJson()));
        snapshot.put("fullExtra", fullSnapshot == null ? null : parseSnapshot(fullSnapshot.getRuleSnapshotJson()));
        return JsonUtils.toJsonString(snapshot);
    }

    private static Map<String, Object> parseSnapshot(String snapshotJson) {
        if (!StringUtils.hasText(snapshotJson)) {
            return Collections.emptyMap();
        }
        Map<String, Object> snapshot = JsonUtils.parseObject(snapshotJson,
                new TypeReference<Map<String, Object>>() {});
        return snapshot == null ? Collections.emptyMap() : snapshot;
    }

    private static String resolveBaseRuleCode(Integer level) {
        if (Objects.equals(level, 1)) {
            return ClubPointRuleItemCodeEnum.ACTIVITY_SMALL_BASE.getCode();
        }
        if (Objects.equals(level, 2)) {
            return ClubPointRuleItemCodeEnum.ACTIVITY_MEDIUM_BASE.getCode();
        }
        if (Objects.equals(level, 3)) {
            return ClubPointRuleItemCodeEnum.ACTIVITY_LARGE_BASE.getCode();
        }
        throw exception(CLUB_ACTIVITY_LEVEL_INVALID);
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private static Integer toInteger(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private static class ActivityPointConfig {

        private Long ruleVersionId;
        private Integer level;
        private Integer basePoints;
        private Integer fullExtraPoints;

        Long getRuleVersionId() {
            return ruleVersionId;
        }

        ActivityPointConfig setRuleVersionId(Long ruleVersionId) {
            this.ruleVersionId = ruleVersionId;
            return this;
        }

        Integer getLevel() {
            return level;
        }

        ActivityPointConfig setLevel(Integer level) {
            this.level = level;
            return this;
        }

        Integer getBasePoints() {
            return basePoints;
        }

        ActivityPointConfig setBasePoints(Integer basePoints) {
            this.basePoints = basePoints;
            return this;
        }

        Integer getFullExtraPoints() {
            return fullExtraPoints;
        }

        ActivityPointConfig setFullExtraPoints(Integer fullExtraPoints) {
            this.fullExtraPoints = fullExtraPoints == null ? 0 : fullExtraPoints;
            return this;
        }

    }

}
