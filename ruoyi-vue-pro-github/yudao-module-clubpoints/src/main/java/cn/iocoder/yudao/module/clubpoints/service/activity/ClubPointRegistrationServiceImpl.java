package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationCancelReasonEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_CANCEL_WINDOW_CLOSED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_REGISTRATION_CLOSED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_REGISTRATION_DUPLICATED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_REGISTRATION_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;

/**
 * 活动报名服务实现
 */
@Service
public class ClubPointRegistrationServiceImpl implements ClubPointRegistrationService {

    private static final Integer MEMBER_ACTIVE = ClubPointMemberStatusEnum.ACTIVE.getStatus();
    private static final Integer LEADER_ACTIVE = ClubPointLeaderStatusEnum.ACTIVE.getStatus();
    private static final Integer REGISTRATION_ACTIVE = ClubPointRegistrationStatusEnum.REGISTERED.getStatus();
    private static final Integer REGISTRATION_CANCELED = ClubPointRegistrationStatusEnum.CANCELED.getStatus();

    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;
    @Resource
    private ClubScopeService clubScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRegistration(ClubPointRegistrationCreateReqBO reqBO) {
        ClubPointActivityDO activity = validateActivityExists(reqBO.getActivityId());
        validateActivityCanRegister(activity);
        LocalDateTime operationTime = operationTime(reqBO.getOperationTime());
        ClubMemberDO member = validateActiveMember(reqBO.getUserId(), activity.getClubId());
        if (operationTime.isAfter(activity.getRegistrationDeadline())) {
            throw exception(CLUB_ACTIVITY_REGISTRATION_CLOSED);
        }
        if (registrationMapper.selectByActivityIdAndUserIdAndStatus(activity.getId(), reqBO.getUserId(),
                REGISTRATION_ACTIVE) != null) {
            throw exception(CLUB_ACTIVITY_REGISTRATION_DUPLICATED);
        }
        ClubPointActivityRegistrationDO registration = buildRegistration(activity, member, operationTime);
        try {
            registrationMapper.insert(registration);
        } catch (DuplicateKeyException ex) {
            throw exception(CLUB_ACTIVITY_REGISTRATION_DUPLICATED);
        }
        return registration.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRegistration(ClubPointRegistrationCancelReqBO reqBO) {
        ClubPointActivityRegistrationDO registration = validateRegistrationExists(reqBO.getRegistrationId());
        clubScopeService.validateSelf(reqBO.getUserId(), registration.getUserId());
        if (!REGISTRATION_ACTIVE.equals(registration.getStatus())) {
            throw exception(CLUB_ACTIVITY_REGISTRATION_NOT_FOUND);
        }
        ClubPointActivityDO activity = validateActivityExists(registration.getActivityId());
        LocalDateTime operationTime = operationTime(reqBO.getOperationTime());
        LocalDateTime cancelDeadline = activity.getCancelDeadlineTime() != null
                ? activity.getCancelDeadlineTime() : activity.getStartTime();
        if (operationTime.isAfter(cancelDeadline)) {
            throw exception(CLUB_ACTIVITY_CANCEL_WINDOW_CLOSED);
        }
        int updated = registrationMapper.update(null, new LambdaUpdateWrapper<ClubPointActivityRegistrationDO>()
                .eq(ClubPointActivityRegistrationDO::getId, registration.getId())
                .eq(ClubPointActivityRegistrationDO::getStatus, REGISTRATION_ACTIVE)
                .set(ClubPointActivityRegistrationDO::getStatus, REGISTRATION_CANCELED)
                .set(ClubPointActivityRegistrationDO::getCancelTime, operationTime)
                .set(ClubPointActivityRegistrationDO::getCancelReasonType,
                        ClubPointRegistrationCancelReasonEnum.SELF_CANCEL.getReasonType())
                .set(ClubPointActivityRegistrationDO::getCancelReason, reqBO.getReason())
                .set(ClubPointActivityRegistrationDO::getCancelOperatorUserId, reqBO.getUserId())
                .set(ClubPointActivityRegistrationDO::getNoAbsenceDeduct, true)
                .set(ClubPointActivityRegistrationDO::getActiveUniqueKey, null));
        if (updated != 1) {
            throw exception(CLUB_ACTIVITY_REGISTRATION_NOT_FOUND);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointActivityRegistrationDO> getLeaderRegistrationPage(Long loginUserId,
                                                                                 ClubPointRegistrationPageReqBO reqBO) {
        if (reqBO.getClubId() != null) {
            clubScopeService.validateManagedClub(loginUserId, reqBO.getClubId());
            return registrationMapper.selectPage(reqBO, reqBO.getClubId(), reqBO.getActivityId(),
                    reqBO.getStatus(), reqBO.getUserId());
        }
        List<Long> managedClubIds = getManagedClubIds(loginUserId);
        if (managedClubIds.isEmpty()) {
            return PageResult.empty();
        }
        return registrationMapper.selectPageByClubIds(reqBO, managedClubIds, reqBO.getActivityId(),
                reqBO.getStatus(), reqBO.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointActivityRegistrationDO> getAdminRegistrationPage(ClubPointRegistrationPageReqBO reqBO) {
        return registrationMapper.selectPage(reqBO, reqBO.getClubId(), reqBO.getActivityId(),
                reqBO.getStatus(), reqBO.getUserId());
    }

    private ClubPointActivityDO validateActivityExists(Long activityId) {
        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw exception(CLUB_ACTIVITY_NOT_FOUND);
        }
        return activity;
    }

    private static void validateActivityCanRegister(ClubPointActivityDO activity) {
        ClubPointActivityStatusEnum status = ClubPointActivityStatusEnum.of(activity.getStatus());
        if (status == null || !status.canRegister()) {
            throw exception(CLUB_ACTIVITY_STATUS_INVALID);
        }
    }

    private ClubMemberDO validateActiveMember(Long userId, Long clubId) {
        ClubMemberDO member = memberMapper.selectByUserIdAndClubIdAndStatus(userId, clubId, MEMBER_ACTIVE);
        if (member == null) {
            throw exception(CLUB_SCOPE_DENIED);
        }
        return member;
    }

    private ClubPointActivityRegistrationDO validateRegistrationExists(Long registrationId) {
        ClubPointActivityRegistrationDO registration = registrationMapper.selectById(registrationId);
        if (registration == null) {
            throw exception(CLUB_ACTIVITY_REGISTRATION_NOT_FOUND);
        }
        return registration;
    }

    private static ClubPointActivityRegistrationDO buildRegistration(ClubPointActivityDO activity,
                                                                     ClubMemberDO member,
                                                                     LocalDateTime operationTime) {
        return new ClubPointActivityRegistrationDO()
                .setActivityId(activity.getId())
                .setClubId(activity.getClubId())
                .setUserId(member.getUserId())
                .setStatus(REGISTRATION_ACTIVE)
                .setRegisterTime(operationTime)
                .setNoAbsenceDeduct(false)
                .setSpecialAbsenceFlag(false)
                .setUserNameSnapshot(member.getUserNameSnapshot())
                .setDeptIdSnapshot(member.getDeptIdSnapshot())
                .setDeptNameSnapshot(member.getDeptNameSnapshot())
                .setMobileSnapshot(member.getMobileSnapshot())
                .setClubNameSnapshot(activity.getClubNameSnapshot())
                .setActivityTitleSnapshot(activity.getTitle())
                .setActivityStartTimeSnapshot(activity.getStartTime())
                .setActivityEndTimeSnapshot(activity.getEndTime())
                .setActiveUniqueKey(buildActiveUniqueKey(activity.getId(), member.getUserId()));
    }

    private static LocalDateTime operationTime(LocalDateTime operationTime) {
        return operationTime != null ? operationTime : LocalDateTime.now();
    }

    private List<Long> getManagedClubIds(Long loginUserId) {
        return leaderMapper.selectActiveListByUserId(loginUserId, LEADER_ACTIVE).stream()
                .map(ClubLeaderDO::getClubId)
                .collect(Collectors.toList());
    }

    private static String buildActiveUniqueKey(Long activityId, Long userId) {
        return activityId + ":" + userId;
    }

}
