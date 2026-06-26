package cn.iocoder.yudao.module.clubpoints.service.settlement;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityPointConfigVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement.ClubPointActivitySettlementRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityPointConfigVersionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.settlement.ClubPointActivitySettlementRunMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivitySettlementItemTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivitySettlementTriggerSourceEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointSettlementRunStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleResolveService;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementRunReqBO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_STATUS_INVALID;

/**
 * 活动积分结算服务实现
 */
@Service
public class ClubPointActivitySettlementServiceImpl implements ClubPointActivitySettlementService {

    private static final Integer REGISTRATION_ACTIVE = ClubPointRegistrationStatusEnum.REGISTERED.getStatus();
    private static final String SOURCE_TITLE_BASE = "活动基础参与积分";
    private static final String SOURCE_TITLE_FULL_EXTRA = "活动全程参与额外积分";
    private static final String SOURCE_TITLE_ABSENCE = "活动无故缺席扣分";

    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointAttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private ClubPointActivityPointConfigVersionMapper configVersionMapper;
    @Resource
    private ClubPointActivitySettlementRunMapper settlementRunMapper;
    @Resource
    private ClubPointLedgerService ledgerService;
    @Resource
    private ClubPointRuleResolveService ruleResolveService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long settleActivity(ClubPointActivitySettlementRunReqBO reqBO) {
        ClubPointActivitySettlementRunDO existingRun = settlementRunMapper.selectByRunKey(reqBO.getRunKey());
        if (existingRun != null) {
            return existingRun.getId();
        }
        ClubPointActivityDO activity = activityMapper.selectByIdForUpdate(reqBO.getActivityId());
        if (activity == null) {
            throw exception(CLUB_ACTIVITY_NOT_FOUND);
        }
        boolean alreadySettled = ClubPointActivityStatusEnum.SETTLED.getStatus().equals(activity.getStatus());
        if (!ClubPointActivityStatusEnum.ENDED.getStatus().equals(activity.getStatus()) && !alreadySettled) {
            throw exception(CLUB_ACTIVITY_STATUS_INVALID);
        }
        ClubPointActivityPointConfigVersionDO config = validateConfigVersion(activity);

        int successCount = 0;
        int skipCount = 0;
        List<ClubPointActivityRegistrationDO> registrations = registrationMapper.selectListByActivityId(activity.getId());
        for (ClubPointActivityRegistrationDO registration : registrations) {
            if (shouldSkip(registration)) {
                skipCount++;
                continue;
            }
            if (settleRegistration(activity, config, registration, reqBO.getOperatorUserId())) {
                successCount++;
            } else {
                skipCount++;
            }
        }

        ClubPointActivitySettlementRunDO run = buildRun(reqBO, activity, config, registrations.size(),
                successCount, skipCount);
        Long runId = insertRun(run);
        if (!alreadySettled) {
            activity.setStatus(ClubPointActivityStatusEnum.SETTLED.getStatus());
            activityMapper.updateById(activity);
        }
        return runId;
    }

    private ClubPointActivityPointConfigVersionDO validateConfigVersion(ClubPointActivityDO activity) {
        if (activity.getCurrentConfigVersionId() == null) {
            throw exception(CLUB_ACTIVITY_STATUS_INVALID);
        }
        ClubPointActivityPointConfigVersionDO config = configVersionMapper.selectById(activity.getCurrentConfigVersionId());
        if (config == null || !activity.getId().equals(config.getActivityId())) {
            throw exception(CLUB_ACTIVITY_STATUS_INVALID);
        }
        return config;
    }

    private boolean settleRegistration(ClubPointActivityDO activity, ClubPointActivityPointConfigVersionDO config,
                                       ClubPointActivityRegistrationDO registration, Long operatorUserId) {
        ClubPointAttendanceRecordDO checkin = attendanceRecordMapper.selectByRegistrationIdAndTargetType(
                registration.getId(), ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType());
        ClubPointAttendanceRecordDO checkout = attendanceRecordMapper.selectByRegistrationIdAndTargetType(
                registration.getId(), ClubPointAttendanceTargetTypeEnum.CHECK_OUT.getTargetType());
        if (checkin == null) {
            createTransaction(activity, config, registration, ClubPointActivitySettlementItemTypeEnum.ABSENCE_SINGLE,
                    ruleResolveService.getFixedPoints(config.getRuleVersionId(),
                            ClubPointActivitySettlementItemTypeEnum.ABSENCE_SINGLE.getRuleItemCode()),
                    SOURCE_TITLE_ABSENCE, operatorUserId);
            return true;
        }

        createTransaction(activity, config, registration, ClubPointActivitySettlementItemTypeEnum.BASE,
                config.getBasePoints(), SOURCE_TITLE_BASE, operatorUserId);
        if (checkout != null && config.getFullExtraPoints() != null && config.getFullExtraPoints() > 0) {
            createTransaction(activity, config, registration, ClubPointActivitySettlementItemTypeEnum.FULL_EXTRA,
                    config.getFullExtraPoints(), SOURCE_TITLE_FULL_EXTRA, operatorUserId);
        }
        return true;
    }

    private void createTransaction(ClubPointActivityDO activity, ClubPointActivityPointConfigVersionDO config,
                                   ClubPointActivityRegistrationDO registration,
                                   ClubPointActivitySettlementItemTypeEnum itemType, Integer points,
                                   String sourceTitle, Long operatorUserId) {
        if (points == null || points <= 0) {
            return;
        }
        ledgerService.createTransaction(new ClubPointLedgerCreateReqBO()
                .setTransactionNo(buildTransactionNo(activity.getId(), registration.getUserId(), itemType))
                .setUserId(registration.getUserId())
                .setUserNameSnapshot(registration.getUserNameSnapshot())
                .setDeptIdSnapshot(registration.getDeptIdSnapshot())
                .setDeptNameSnapshot(registration.getDeptNameSnapshot())
                .setDirection(itemType.getDirection())
                .setPoints(points)
                .setPointCategory(itemType.getPointCategory())
                .setSourceType(itemType.getSourceType())
                .setSourceId(activity.getId())
                .setSourceItemId(registration.getId())
                .setSourceTitleSnapshot(sourceTitle)
                .setIssuingClubId(activity.getClubId())
                .setIssuingClubCodeSnapshot(activity.getClubCodeSnapshot())
                .setIssuingClubNameSnapshot(activity.getClubNameSnapshot())
                .setActivityId(activity.getId())
                .setActivityTitleSnapshot(activity.getTitle())
                .setEvidenceType(1)
                .setMaterialSummary(sourceTitle)
                .setReason(sourceTitle)
                .setOccurredAt(activity.getEndTime())
                .setIdempotencyKey(itemType.buildIdempotencyKey(activity.getId(), registration.getUserId(),
                        businessMonth(activity.getEndTime())))
                .setOperatorUserId(operatorUserId)
                .setRuleItemCode(resolveRuleItemCode(config, itemType))
                .setRuleVersionId(config.getRuleVersionId())
                .setSourceSnapshotJson(buildSourceSnapshot(activity, registration, itemType)));
    }

    private static boolean shouldSkip(ClubPointActivityRegistrationDO registration) {
        return !REGISTRATION_ACTIVE.equals(registration.getStatus())
                || Boolean.TRUE.equals(registration.getNoAbsenceDeduct())
                || Boolean.TRUE.equals(registration.getSpecialAbsenceFlag());
    }

    private static ClubPointActivitySettlementRunDO buildRun(ClubPointActivitySettlementRunReqBO reqBO,
                                                             ClubPointActivityDO activity,
                                                             ClubPointActivityPointConfigVersionDO config,
                                                             int registrationCount, int successCount,
                                                             int skipCount) {
        return new ClubPointActivitySettlementRunDO()
                .setActivityId(activity.getId())
                .setRunKey(reqBO.getRunKey())
                .setStatus(ClubPointSettlementRunStatusEnum.SUCCESS.getStatus())
                .setSettlementTime(reqBO.getSettlementTime() != null ? reqBO.getSettlementTime() : LocalDateTime.now())
                .setConfigVersionId(config.getId())
                .setRegistrationCount(registrationCount)
                .setSuccessCount(successCount)
                .setSkipCount(skipCount)
                .setFailedCount(0)
                .setTriggerSource(reqBO.getTriggerSource() != null ? reqBO.getTriggerSource()
                        : ClubPointActivitySettlementTriggerSourceEnum.SCHEDULED.getSource())
                .setOperatorUserId(reqBO.getOperatorUserId());
    }

    private Long insertRun(ClubPointActivitySettlementRunDO run) {
        try {
            settlementRunMapper.insert(run);
            return run.getId();
        } catch (DuplicateKeyException ex) {
            ClubPointActivitySettlementRunDO duplicated = settlementRunMapper.selectByRunKey(run.getRunKey());
            if (duplicated != null) {
                return duplicated.getId();
            }
            throw ex;
        }
    }

    private static String resolveRuleItemCode(ClubPointActivityPointConfigVersionDO config,
                                              ClubPointActivitySettlementItemTypeEnum itemType) {
        if (itemType == ClubPointActivitySettlementItemTypeEnum.BASE) {
            return resolveBaseRuleCode(config.getLevel());
        }
        return itemType.getRuleItemCode();
    }

    private static String resolveBaseRuleCode(Integer level) {
        if (Integer.valueOf(1).equals(level)) {
            return ClubPointRuleItemCodeEnum.ACTIVITY_SMALL_BASE.getCode();
        }
        if (Integer.valueOf(2).equals(level)) {
            return ClubPointRuleItemCodeEnum.ACTIVITY_MEDIUM_BASE.getCode();
        }
        if (Integer.valueOf(3).equals(level)) {
            return ClubPointRuleItemCodeEnum.ACTIVITY_LARGE_BASE.getCode();
        }
        throw exception(CLUB_ACTIVITY_STATUS_INVALID);
    }

    private static String buildTransactionNo(Long activityId, Long userId,
                                             ClubPointActivitySettlementItemTypeEnum itemType) {
        return "AS-" + activityId + "-" + userId + "-" + itemType.getItemType().charAt(0);
    }

    private static Integer businessMonth(LocalDateTime occurredAt) {
        return occurredAt.getYear() * 100 + occurredAt.getMonthValue();
    }

    private static String buildSourceSnapshot(ClubPointActivityDO activity,
                                              ClubPointActivityRegistrationDO registration,
                                              ClubPointActivitySettlementItemTypeEnum itemType) {
        return "{\"activityId\":" + activity.getId()
                + ",\"registrationId\":" + registration.getId()
                + ",\"userId\":" + registration.getUserId()
                + ",\"itemType\":\"" + itemType.getItemType() + "\"}";
    }

}
