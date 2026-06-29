package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceCorrectionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceCorrectionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceRecordMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceCorrectionTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceCorrectReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendancePageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSelfReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSupplementReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointSpecialAbsenceReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.BIZ_TYPE_ACTIVITY_REGISTRATION;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.BIZ_TYPE_ATTENDANCE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ATTENDANCE_CORRECT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ATTENDANCE_SUPPLEMENT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.SPECIAL_ABSENCE_MARK;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_REGISTRATION_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_ALREADY_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_CHECKIN_REQUIRED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_TARGET_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_WINDOW_CLOSED;

/**
 * 活动签到签退服务实现
 */
@Service
public class ClubPointAttendanceServiceImpl implements ClubPointAttendanceService {

    private static final Integer REGISTRATION_ACTIVE = ClubPointRegistrationStatusEnum.REGISTERED.getStatus();
    private static final Integer LEADER_ACTIVE = ClubPointLeaderStatusEnum.ACTIVE.getStatus();
    private static final Integer SOURCE_SELF = ClubPointAttendanceSourceTypeEnum.SELF.getSourceType();
    private static final Integer SOURCE_SUPPLEMENT = ClubPointAttendanceSourceTypeEnum.SUPPLEMENT.getSourceType();
    private static final Integer SOURCE_CORRECTION = ClubPointAttendanceSourceTypeEnum.CORRECTION.getSourceType();

    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointAttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private ClubPointAttendanceCorrectionMapper correctionMapper;
    @Resource
    private ClubAuditService clubAuditService;
    @Resource
    private ClubScopeService clubScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long checkIn(ClubPointAttendanceSelfReqBO reqBO) {
        return createSelfAttendance(reqBO, ClubPointAttendanceTargetTypeEnum.CHECK_IN);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long checkOut(ClubPointAttendanceSelfReqBO reqBO) {
        return createSelfAttendance(reqBO, ClubPointAttendanceTargetTypeEnum.CHECK_OUT);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointAttendanceRecordDO> getLeaderAttendancePage(Long loginUserId,
                                                                           ClubPointAttendancePageReqBO reqBO) {
        List<Long> managedClubIds = reqBO.getClubId() != null
                ? validateAndBuildSingleManagedClub(loginUserId, reqBO.getClubId())
                : getManagedClubIds(loginUserId);
        if (managedClubIds.isEmpty()) {
            return PageResult.empty();
        }
        List<Long> activityIds = activityMapper.selectListByClubIds(managedClubIds).stream()
                .map(ClubPointActivityDO::getId)
                .collect(Collectors.toList());
        if (activityIds.isEmpty()) {
            return PageResult.empty();
        }
        return attendanceRecordMapper.selectPageByActivityIds(reqBO, activityIds, reqBO.getActivityId(),
                reqBO.getRegistrationId(), reqBO.getUserId(), reqBO.getTargetType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long supplementAttendance(ClubPointAttendanceSupplementReqBO reqBO) {
        ClubPointAttendanceTargetTypeEnum targetType = targetType(reqBO.getTargetType());
        ClubPointActivityRegistrationDO registration = validateActiveRegistration(reqBO.getRegistrationId());
        validateOperatorScope(reqBO.getOperatorGlobalScope(), reqBO.getOperatorUserId(), registration.getClubId());
        validateActivityCanCorrectAttendance(registration.getActivityId());
        if (targetType == ClubPointAttendanceTargetTypeEnum.CHECK_OUT) {
            validateCheckInExists(registration.getId());
        }
        validateNotExists(registration.getId(), targetType);

        ClubPointAttendanceRecordDO record = new ClubPointAttendanceRecordDO()
                .setRegistrationId(registration.getId())
                .setActivityId(registration.getActivityId())
                .setUserId(registration.getUserId())
                .setTargetType(targetType.getTargetType())
                .setRecordTime(recordTime(reqBO.getRecordTime()))
                .setSourceType(SOURCE_SUPPLEMENT)
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setReason(reqBO.getReason())
                .setClientIp(reqBO.getClientIp());
        try {
            attendanceRecordMapper.insert(record);
        } catch (DuplicateKeyException ex) {
            throw exception(CLUB_ATTENDANCE_ALREADY_EXISTS);
        }
        String afterJson = snapshot(record);
        Long auditLogId = createAudit(ATTENDANCE_SUPPLEMENT, BIZ_TYPE_ATTENDANCE, record.getId(), reqBO,
                null, afterJson);
        ClubPointAttendanceCorrectionDO correction = new ClubPointAttendanceCorrectionDO()
                .setAttendanceRecordId(record.getId())
                .setRegistrationId(registration.getId())
                .setActivityId(registration.getActivityId())
                .setUserId(registration.getUserId())
                .setTargetType(targetType.getTargetType())
                .setCorrectionType(ClubPointAttendanceCorrectionTypeEnum.SUPPLEMENT.getCorrectionType())
                .setBeforeRecordTime(null)
                .setAfterRecordTime(record.getRecordTime())
                .setReason(reqBO.getReason())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(auditLogId);
        correctionMapper.insert(correction);
        return correction.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long correctAttendance(ClubPointAttendanceCorrectReqBO reqBO) {
        ClubPointAttendanceRecordDO record = attendanceRecordMapper.selectById(reqBO.getAttendanceRecordId());
        if (record == null) {
            throw exception(CLUB_ATTENDANCE_NOT_FOUND);
        }
        ClubPointActivityRegistrationDO registration = registrationMapper.selectById(record.getRegistrationId());
        if (registration == null) {
            throw exception(CLUB_ACTIVITY_REGISTRATION_NOT_FOUND);
        }
        validateOperatorScope(reqBO.getOperatorGlobalScope(), reqBO.getOperatorUserId(), registration.getClubId());
        validateActivityCanCorrectAttendance(record.getActivityId());
        String beforeJson = snapshot(record);
        LocalDateTime beforeRecordTime = record.getRecordTime();
        record.setRecordTime(recordTime(reqBO.getNewRecordTime()))
                .setSourceType(SOURCE_CORRECTION)
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setReason(reqBO.getReason())
                .setClientIp(reqBO.getClientIp());
        attendanceRecordMapper.updateById(record);
        String afterJson = snapshot(record);
        Long auditLogId = createAudit(ATTENDANCE_CORRECT, BIZ_TYPE_ATTENDANCE, record.getId(), reqBO,
                beforeJson, afterJson);
        ClubPointAttendanceCorrectionDO correction = new ClubPointAttendanceCorrectionDO()
                .setAttendanceRecordId(record.getId())
                .setRegistrationId(record.getRegistrationId())
                .setActivityId(record.getActivityId())
                .setUserId(record.getUserId())
                .setTargetType(record.getTargetType())
                .setCorrectionType(ClubPointAttendanceCorrectionTypeEnum.CORRECTION.getCorrectionType())
                .setBeforeRecordTime(beforeRecordTime)
                .setAfterRecordTime(record.getRecordTime())
                .setReason(reqBO.getReason())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(auditLogId);
        correctionMapper.insert(correction);
        return correction.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSpecialAbsence(ClubPointSpecialAbsenceReqBO reqBO) {
        ClubPointActivityRegistrationDO registration = validateActiveRegistration(reqBO.getRegistrationId());
        validateOperatorScope(reqBO.getOperatorGlobalScope(), reqBO.getOperatorUserId(), registration.getClubId());
        String beforeJson = snapshot(registration);
        registration.setSpecialAbsenceFlag(true)
                .setNoAbsenceDeduct(true)
                .setSpecialAbsenceReason(reqBO.getReason())
                .setSpecialAbsenceTime(recordTime(reqBO.getOperationTime()))
                .setSpecialAbsenceOperatorId(reqBO.getOperatorUserId());
        registrationMapper.updateById(registration);
        String afterJson = snapshot(registration);
        createAudit(SPECIAL_ABSENCE_MARK, BIZ_TYPE_ACTIVITY_REGISTRATION, registration.getId(), reqBO,
                beforeJson, afterJson);
    }

    private Long createSelfAttendance(ClubPointAttendanceSelfReqBO reqBO,
                                      ClubPointAttendanceTargetTypeEnum targetType) {
        ClubPointActivityRegistrationDO registration = validateActiveRegistration(reqBO.getRegistrationId());
        clubScopeService.validateSelf(reqBO.getUserId(), registration.getUserId());
        ClubPointActivityDO activity = validateActivityCanCheckAttendance(registration.getActivityId());
        LocalDateTime recordTime = recordTime(reqBO.getRecordTime());
        validateTarget(targetType, registration, activity, recordTime);

        ClubPointAttendanceRecordDO record = new ClubPointAttendanceRecordDO()
                .setRegistrationId(registration.getId())
                .setActivityId(registration.getActivityId())
                .setUserId(registration.getUserId())
                .setTargetType(targetType.getTargetType())
                .setRecordTime(recordTime)
                .setSourceType(SOURCE_SELF)
                .setOperatorUserId(reqBO.getUserId())
                .setClientIp(reqBO.getClientIp())
                .setRemark(reqBO.getRemark());
        try {
            attendanceRecordMapper.insert(record);
        } catch (DuplicateKeyException ex) {
            throw exception(CLUB_ATTENDANCE_ALREADY_EXISTS);
        }
        return record.getId();
    }

    private ClubPointActivityRegistrationDO validateActiveRegistration(Long registrationId) {
        ClubPointActivityRegistrationDO registration = registrationMapper.selectById(registrationId);
        if (registration == null || !REGISTRATION_ACTIVE.equals(registration.getStatus())) {
            throw exception(CLUB_ACTIVITY_REGISTRATION_NOT_FOUND);
        }
        return registration;
    }

    private ClubPointActivityDO validateActivityCanCheckAttendance(Long activityId) {
        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw exception(CLUB_ACTIVITY_NOT_FOUND);
        }
        ClubPointActivityStatusEnum status = ClubPointActivityStatusEnum.of(activity.getStatus());
        if (status == null || !status.canCheckAttendance()) {
            throw exception(CLUB_ACTIVITY_STATUS_INVALID);
        }
        return activity;
    }

    private ClubPointActivityDO validateActivityCanCorrectAttendance(Long activityId) {
        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw exception(CLUB_ACTIVITY_NOT_FOUND);
        }
        ClubPointActivityStatusEnum status = ClubPointActivityStatusEnum.of(activity.getStatus());
        if (status != ClubPointActivityStatusEnum.PUBLISHED && status != ClubPointActivityStatusEnum.ENDED) {
            throw exception(CLUB_ACTIVITY_STATUS_INVALID);
        }
        return activity;
    }

    private void validateTarget(ClubPointAttendanceTargetTypeEnum targetType,
                                ClubPointActivityRegistrationDO registration,
                                ClubPointActivityDO activity,
                                LocalDateTime recordTime) {
        if (targetType == ClubPointAttendanceTargetTypeEnum.CHECK_IN) {
            validateWindow(recordTime, activity.getCheckinStartTime(), activity.getCheckinEndTime());
            validateNotExists(registration.getId(), targetType);
            return;
        }
        validateCheckInExists(registration.getId());
        validateWindow(recordTime, activity.getCheckoutStartTime(), activity.getCheckoutEndTime());
        validateNotExists(registration.getId(), targetType);
    }

    private void validateCheckInExists(Long registrationId) {
        if (attendanceRecordMapper.selectByRegistrationIdAndTargetType(registrationId,
                ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType()) == null) {
            throw exception(CLUB_ATTENDANCE_CHECKIN_REQUIRED);
        }
    }

    private void validateNotExists(Long registrationId, ClubPointAttendanceTargetTypeEnum targetType) {
        if (attendanceRecordMapper.selectByRegistrationIdAndTargetType(registrationId,
                targetType.getTargetType()) != null) {
            throw exception(CLUB_ATTENDANCE_ALREADY_EXISTS);
        }
    }

    private static void validateWindow(LocalDateTime recordTime, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || recordTime.isBefore(startTime) || recordTime.isAfter(endTime)) {
            throw exception(CLUB_ATTENDANCE_WINDOW_CLOSED);
        }
    }

    private static LocalDateTime recordTime(LocalDateTime recordTime) {
        return recordTime != null ? recordTime : LocalDateTime.now();
    }

    private void validateOperatorScope(Boolean operatorGlobalScope, Long operatorUserId, Long clubId) {
        if (Boolean.TRUE.equals(operatorGlobalScope)) {
            clubScopeService.validateGlobal(true);
            return;
        }
        clubScopeService.validateManagedClub(operatorUserId, clubId);
    }

    private List<Long> validateAndBuildSingleManagedClub(Long loginUserId, Long clubId) {
        clubScopeService.validateManagedClub(loginUserId, clubId);
        return java.util.Collections.singletonList(clubId);
    }

    private List<Long> getManagedClubIds(Long loginUserId) {
        return leaderMapper.selectActiveListByUserId(loginUserId, LEADER_ACTIVE).stream()
                .map(ClubLeaderDO::getClubId)
                .collect(Collectors.toList());
    }

    private static ClubPointAttendanceTargetTypeEnum targetType(Integer targetType) {
        if (ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType().equals(targetType)) {
            return ClubPointAttendanceTargetTypeEnum.CHECK_IN;
        }
        if (ClubPointAttendanceTargetTypeEnum.CHECK_OUT.getTargetType().equals(targetType)) {
            return ClubPointAttendanceTargetTypeEnum.CHECK_OUT;
        }
        throw exception(CLUB_ATTENDANCE_TARGET_INVALID);
    }

    private Long createAudit(String actionType, String bizType, Long bizId, ClubPointAttendanceSupplementReqBO reqBO,
                             String beforeJson, String afterJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(actionType)
                .setBizType(bizType)
                .setBizId(bizId)
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(reqBO.getOperationTime())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(afterJson)
                .setSuccess(true));
    }

    private Long createAudit(String actionType, String bizType, Long bizId, ClubPointAttendanceCorrectReqBO reqBO,
                             String beforeJson, String afterJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(actionType)
                .setBizType(bizType)
                .setBizId(bizId)
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(reqBO.getOperationTime())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(afterJson)
                .setSuccess(true));
    }

    private Long createAudit(String actionType, String bizType, Long bizId, ClubPointSpecialAbsenceReqBO reqBO,
                             String beforeJson, String afterJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(actionType)
                .setBizType(bizType)
                .setBizId(bizId)
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(reqBO.getOperationTime())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(afterJson)
                .setSuccess(true));
    }

    private static String snapshot(ClubPointAttendanceRecordDO record) {
        return "{\"recordId\":" + record.getId()
                + ",\"registrationId\":" + record.getRegistrationId()
                + ",\"activityId\":" + record.getActivityId()
                + ",\"userId\":" + record.getUserId()
                + ",\"targetType\":" + record.getTargetType()
                + ",\"recordTime\":\"" + record.getRecordTime()
                + "\",\"sourceType\":" + record.getSourceType() + "}";
    }

    private static String snapshot(ClubPointActivityRegistrationDO registration) {
        return "{\"registrationId\":" + registration.getId()
                + ",\"activityId\":" + registration.getActivityId()
                + ",\"userId\":" + registration.getUserId()
                + ",\"specialAbsenceFlag\":" + registration.getSpecialAbsenceFlag()
                + ",\"noAbsenceDeduct\":" + registration.getNoAbsenceDeduct() + "}";
    }

}
