package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;

/**
 * 活动查询服务实现
 */
@Service
public class ClubPointActivityQueryServiceImpl implements ClubPointActivityQueryService {

    private static final Integer MEMBER_ACTIVE = ClubPointMemberStatusEnum.ACTIVE.getStatus();
    private static final Integer REGISTRATION_ACTIVE = ClubPointRegistrationStatusEnum.REGISTERED.getStatus();
    private static final List<Integer> APP_VISIBLE_STATUSES = Arrays.asList(
            ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
            ClubPointActivityStatusEnum.CANCELED.getStatus(),
            ClubPointActivityStatusEnum.ENDED.getStatus(),
            ClubPointActivityStatusEnum.SETTLED.getStatus());

    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointAttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubScopeService clubScopeService;

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointActivityInfoBO> getAppActivityPage(Long loginUserId,
                                                                  ClubPointActivityPageReqBO reqBO) {
        List<Integer> statuses = appVisibleStatuses(reqBO.getStatus());
        if (statuses.isEmpty()) {
            return PageResult.empty();
        }
        List<Long> clubIds = null;
        if (reqBO.getClubId() != null) {
            clubScopeService.validateJoinedClub(loginUserId, reqBO.getClubId());
        } else {
            clubIds = memberMapper.selectClubIdsByUserIdAndStatus(loginUserId, MEMBER_ACTIVE);
            if (clubIds.isEmpty()) {
                return PageResult.empty();
            }
        }
        PageResult<ClubPointActivityDO> pageResult = activityMapper.selectPage(reqBO, clubIds, reqBO.getClubId(),
                reqBO.getKeyword(), null, statuses, reqBO.getStartTime(), reqBO.getEndTime());
        return toPageResult(pageResult, loginUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubPointActivityInfoBO getAppActivity(Long loginUserId, Long activityId) {
        ClubPointActivityDO activity = validateActivityExists(activityId);
        boolean joined = memberMapper.selectByUserIdAndClubIdAndStatus(loginUserId, activity.getClubId(),
                MEMBER_ACTIVE) != null;
        ClubPointActivityRegistrationDO registration = registrationMapper.selectByActivityIdAndUserIdAndStatus(
                activity.getId(), loginUserId, REGISTRATION_ACTIVE);
        if (!joined && registration == null) {
            throw exception(CLUB_SCOPE_DENIED);
        }
        if (!APP_VISIBLE_STATUSES.contains(activity.getStatus()) && registration == null) {
            throw exception(CLUB_SCOPE_DENIED);
        }
        return toInfo(activity, loginUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointActivityInfoBO> getLeaderActivityPage(Long loginUserId,
                                                                     ClubPointActivityPageReqBO reqBO) {
        clubScopeService.validateManagedClub(loginUserId, reqBO.getClubId());
        PageResult<ClubPointActivityDO> pageResult = activityMapper.selectPage(reqBO, null, reqBO.getClubId(),
                reqBO.getKeyword(), reqBO.getStatus(), null, reqBO.getStartTime(), reqBO.getEndTime());
        return toPageResult(pageResult, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubPointActivityInfoBO getLeaderActivity(Long loginUserId, Long activityId) {
        ClubPointActivityDO activity = validateActivityExists(activityId);
        clubScopeService.validateManagedClub(loginUserId, activity.getClubId());
        return toInfo(activity, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointActivityInfoBO> getAdminActivityPage(ClubPointActivityPageReqBO reqBO) {
        PageResult<ClubPointActivityDO> pageResult = activityMapper.selectPage(reqBO, null, reqBO.getClubId(),
                reqBO.getKeyword(), reqBO.getStatus(), null, reqBO.getStartTime(), reqBO.getEndTime());
        return toPageResult(pageResult, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubPointActivityInfoBO getAdminActivity(Long activityId) {
        return toInfo(validateActivityExists(activityId), null);
    }

    private ClubPointActivityDO validateActivityExists(Long activityId) {
        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw exception(CLUB_ACTIVITY_NOT_FOUND);
        }
        return activity;
    }

    private PageResult<ClubPointActivityInfoBO> toPageResult(PageResult<ClubPointActivityDO> pageResult,
                                                            Long loginUserId) {
        return new PageResult<>(pageResult.getList().stream()
                .map(item -> toInfo(item, loginUserId))
                .collect(Collectors.toList()), pageResult.getTotal());
    }

    private ClubPointActivityInfoBO toInfo(ClubPointActivityDO activity, Long loginUserId) {
        Map<String, Object> snapshot = parseSnapshot(activity.getSnapshotJson());
        ClubPointActivityInfoBO info = new ClubPointActivityInfoBO()
                .setId(activity.getId())
                .setClubId(activity.getClubId())
                .setClubCodeSnapshot(activity.getClubCodeSnapshot())
                .setClubNameSnapshot(activity.getClubNameSnapshot())
                .setTitle(activity.getTitle())
                .setLocation(activity.getLocation())
                .setDescription(activity.getDescription())
                .setCoverFileId(activity.getCoverFileId())
                .setLevel(activity.getLevel())
                .setStatus(activity.getStatus())
                .setStartTime(activity.getStartTime())
                .setEndTime(activity.getEndTime())
                .setRegistrationDeadline(activity.getRegistrationDeadline())
                .setCancelDeadlineTime(activity.getCancelDeadlineTime())
                .setCheckinStartTime(activity.getCheckinStartTime())
                .setCheckinEndTime(activity.getCheckinEndTime())
                .setCheckoutMode(activity.getCheckoutMode())
                .setCheckoutStartTime(activity.getCheckoutStartTime())
                .setCheckoutEndTime(activity.getCheckoutEndTime())
                .setCurrentConfigVersionId(activity.getCurrentConfigVersionId())
                .setCreatorUserId(activity.getCreatorUserId())
                .setBasePoints(toInteger(snapshot.get("basePoints")))
                .setFullExtraPoints(toInteger(snapshot.get("fullExtraPoints")))
                .setRegistered(false)
                .setCheckInStatus(0)
                .setCheckOutStatus(0);
        if (loginUserId != null) {
            fillAppRegistrationInfo(info, activity.getId(), loginUserId);
        }
        return info;
    }

    private void fillAppRegistrationInfo(ClubPointActivityInfoBO info, Long activityId, Long loginUserId) {
        ClubPointActivityRegistrationDO registration = registrationMapper.selectByActivityIdAndUserIdAndStatus(
                activityId, loginUserId, REGISTRATION_ACTIVE);
        if (registration == null) {
            return;
        }
        info.setRegistrationId(registration.getId())
                .setRegistered(true)
                .setRegistrationStatus(registration.getStatus());
        ClubPointAttendanceRecordDO checkIn = attendanceRecordMapper.selectByRegistrationIdAndTargetType(
                registration.getId(), ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType());
        if (checkIn != null) {
            info.setCheckInStatus(1)
                    .setCheckInSource(checkIn.getSourceType())
                    .setCheckInTime(checkIn.getRecordTime());
        }
        ClubPointAttendanceRecordDO checkOut = attendanceRecordMapper.selectByRegistrationIdAndTargetType(
                registration.getId(), ClubPointAttendanceTargetTypeEnum.CHECK_OUT.getTargetType());
        if (checkOut != null) {
            info.setCheckOutStatus(1)
                    .setCheckOutSource(checkOut.getSourceType())
                    .setCheckOutTime(checkOut.getRecordTime());
        }
    }

    private static List<Integer> appVisibleStatuses(Integer requestedStatus) {
        if (requestedStatus == null) {
            return APP_VISIBLE_STATUSES;
        }
        return APP_VISIBLE_STATUSES.contains(requestedStatus)
                ? Collections.singletonList(requestedStatus) : Collections.emptyList();
    }

    private static Map<String, Object> parseSnapshot(String snapshotJson) {
        if (!StringUtils.hasText(snapshotJson)) {
            return Collections.emptyMap();
        }
        Map<String, Object> snapshot = JsonUtils.parseObject(snapshotJson,
                new TypeReference<Map<String, Object>>() {});
        return snapshot == null ? Collections.emptyMap() : snapshot;
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

}
