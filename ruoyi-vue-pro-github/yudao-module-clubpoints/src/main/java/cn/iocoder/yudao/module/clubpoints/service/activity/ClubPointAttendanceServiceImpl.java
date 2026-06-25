package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceRecordMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSelfReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_REGISTRATION_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_ALREADY_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_CHECKIN_REQUIRED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_WINDOW_CLOSED;

/**
 * 活动签到签退服务实现
 */
@Service
public class ClubPointAttendanceServiceImpl implements ClubPointAttendanceService {

    private static final Integer REGISTRATION_ACTIVE = ClubPointRegistrationStatusEnum.REGISTERED.getStatus();
    private static final Integer SOURCE_SELF = ClubPointAttendanceSourceTypeEnum.SELF.getSourceType();

    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointAttendanceRecordMapper attendanceRecordMapper;
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

}
