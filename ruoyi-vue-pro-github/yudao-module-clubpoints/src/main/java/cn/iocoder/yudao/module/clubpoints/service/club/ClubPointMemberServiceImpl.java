package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberAddReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberExitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberJoinReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberRemoveReqBO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_ADD;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_EXIT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_JOIN;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_REMOVE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ALREADY_JOINED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_DISABLED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_MEMBER;

/**
 * 俱乐部成员服务实现
 */
@Service
public class ClubPointMemberServiceImpl implements ClubPointMemberService {

    private static final String BIZ_TYPE_MEMBER = "CLUB_MEMBER";
    private static final int LEAVE_REASON_SELF_EXIT = 1;
    private static final int LEAVE_REASON_ADMIN_REMOVE = 2;
    private static final int REGISTRATION_STATUS_CANCELED = 2;
    private static final int REGISTRATION_CANCEL_REASON_EXIT_CLUB = 2;
    private static final int REGISTRATION_CANCEL_REASON_ADMIN_REMOVE = 3;

    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubAuditService clubAuditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long joinClub(ClubPointMemberJoinReqBO reqBO) {
        ClubPointClubDO club = validateJoinableClub(reqBO.getClubId());
        validateNotActiveMember(reqBO.getUserId(), reqBO.getClubId());
        ClubMemberDO member = buildMember(reqBO, club);
        memberMapper.insert(member);
        createAudit(CLUB_MEMBER_JOIN, member, reqBO, null, snapshot(member));
        return member.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMember(ClubPointMemberAddReqBO reqBO) {
        ClubPointClubDO club = validateJoinableClub(reqBO.getClubId());
        validateNotActiveMember(reqBO.getUserId(), reqBO.getClubId());
        ClubMemberDO member = buildMember(reqBO, club);
        memberMapper.insert(member);
        createAudit(CLUB_MEMBER_ADD, member, reqBO, null, snapshot(member));
        return member.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitClub(ClubPointMemberExitReqBO reqBO) {
        ClubMemberDO member = validateActiveMember(reqBO.getUserId(), reqBO.getClubId());
        changeMemberToInactive(member, ClubPointMemberStatusEnum.SELF_EXITED.getStatus(),
                LEAVE_REASON_SELF_EXIT, reqBO.getReason(), reqBO.getOperatorUserId());
        memberMapper.cancelActiveRegistrationsByMemberChange(reqBO.getClubId(), reqBO.getUserId(),
                REGISTRATION_STATUS_CANCELED, REGISTRATION_CANCEL_REASON_EXIT_CLUB, reqBO.getReason(),
                reqBO.getOperatorUserId(), member.getLeaveTime());
        createAudit(CLUB_MEMBER_EXIT, member, reqBO, snapshotActive(member), snapshot(member));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(ClubPointMemberRemoveReqBO reqBO) {
        ClubMemberDO member = validateActiveMember(reqBO.getUserId(), reqBO.getClubId());
        changeMemberToInactive(member, ClubPointMemberStatusEnum.ADMIN_REMOVED.getStatus(),
                LEAVE_REASON_ADMIN_REMOVE, reqBO.getReason(), reqBO.getOperatorUserId());
        memberMapper.cancelActiveRegistrationsByMemberChange(reqBO.getClubId(), reqBO.getUserId(),
                REGISTRATION_STATUS_CANCELED, REGISTRATION_CANCEL_REASON_ADMIN_REMOVE, reqBO.getReason(),
                reqBO.getOperatorUserId(), member.getLeaveTime());
        createAudit(CLUB_MEMBER_REMOVE, member, reqBO, snapshotActive(member), snapshot(member));
    }

    private ClubPointClubDO validateJoinableClub(Long clubId) {
        ClubPointClubDO club = clubMapper.selectById(clubId);
        if (club == null) {
            throw exception(CLUB_NOT_FOUND);
        }
        if (!ClubPointClubStatusEnum.ENABLED.getStatus().equals(club.getStatus())) {
            throw exception(CLUB_DISABLED);
        }
        return club;
    }

    private void validateNotActiveMember(Long userId, Long clubId) {
        if (memberMapper.selectByUserIdAndClubIdAndStatus(userId, clubId,
                ClubPointMemberStatusEnum.ACTIVE.getStatus()) != null) {
            throw exception(CLUB_ALREADY_JOINED);
        }
    }

    private ClubMemberDO validateActiveMember(Long userId, Long clubId) {
        ClubMemberDO member = memberMapper.selectByUserIdAndClubIdAndStatus(userId, clubId,
                ClubPointMemberStatusEnum.ACTIVE.getStatus());
        if (member == null) {
            throw exception(CLUB_NOT_MEMBER);
        }
        return member;
    }

    private ClubMemberDO buildMember(ClubPointMemberJoinReqBO reqBO, ClubPointClubDO club) {
        return new ClubMemberDO()
                .setClubId(club.getId())
                .setUserId(reqBO.getUserId())
                .setDeptIdSnapshot(reqBO.getDeptIdSnapshot())
                .setUserNameSnapshot(reqBO.getUserNameSnapshot())
                .setDeptNameSnapshot(reqBO.getDeptNameSnapshot())
                .setMobileSnapshot(reqBO.getMobileSnapshot())
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setStatus(ClubPointMemberStatusEnum.ACTIVE.getStatus())
                .setJoinTime(LocalDateTime.now())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setActiveUniqueKey(buildActiveUniqueKey(club.getId(), reqBO.getUserId()));
    }

    private ClubMemberDO buildMember(ClubPointMemberAddReqBO reqBO, ClubPointClubDO club) {
        return new ClubMemberDO()
                .setClubId(club.getId())
                .setUserId(reqBO.getUserId())
                .setDeptIdSnapshot(reqBO.getDeptIdSnapshot())
                .setUserNameSnapshot(reqBO.getUserNameSnapshot())
                .setDeptNameSnapshot(reqBO.getDeptNameSnapshot())
                .setMobileSnapshot(reqBO.getMobileSnapshot())
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setStatus(ClubPointMemberStatusEnum.ACTIVE.getStatus())
                .setJoinTime(LocalDateTime.now())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setActiveUniqueKey(buildActiveUniqueKey(club.getId(), reqBO.getUserId()));
    }

    private void changeMemberToInactive(ClubMemberDO member, Integer status, Integer leaveReasonType,
                                        String reason, Long operatorUserId) {
        LocalDateTime leaveTime = LocalDateTime.now();
        int updated = memberMapper.update(null, new LambdaUpdateWrapper<ClubMemberDO>()
                .eq(ClubMemberDO::getId, member.getId())
                .eq(ClubMemberDO::getStatus, ClubPointMemberStatusEnum.ACTIVE.getStatus())
                .set(ClubMemberDO::getStatus, status)
                .set(ClubMemberDO::getLeaveTime, leaveTime)
                .set(ClubMemberDO::getLeaveReasonType, leaveReasonType)
                .set(ClubMemberDO::getLeaveReason, reason)
                .set(ClubMemberDO::getOperatorUserId, operatorUserId)
                .set(ClubMemberDO::getActiveUniqueKey, null));
        if (updated != 1) {
            throw exception(CLUB_NOT_MEMBER);
        }
        member.setStatus(status)
                .setLeaveTime(leaveTime)
                .setLeaveReasonType(leaveReasonType)
                .setLeaveReason(reason)
                .setOperatorUserId(operatorUserId)
                .setActiveUniqueKey(null);
    }

    private Long createAudit(String actionType, ClubMemberDO member, ClubPointClubOperationReq reqBO,
                             String beforeJson, String afterJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(actionType)
                .setBizType(BIZ_TYPE_MEMBER)
                .setBizId(member.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(LocalDateTime.now())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(snapshot(member))
                .setSuccess(true));
    }

    private static String snapshotActive(ClubMemberDO member) {
        ClubMemberDO active = new ClubMemberDO()
                .setId(member.getId())
                .setClubId(member.getClubId())
                .setUserId(member.getUserId())
                .setDeptIdSnapshot(member.getDeptIdSnapshot())
                .setUserNameSnapshot(member.getUserNameSnapshot())
                .setDeptNameSnapshot(member.getDeptNameSnapshot())
                .setMobileSnapshot(member.getMobileSnapshot())
                .setClubCodeSnapshot(member.getClubCodeSnapshot())
                .setClubNameSnapshot(member.getClubNameSnapshot())
                .setStatus(ClubPointMemberStatusEnum.ACTIVE.getStatus())
                .setJoinTime(member.getJoinTime())
                .setOperatorUserId(member.getOperatorUserId())
                .setActiveUniqueKey(buildActiveUniqueKey(member.getClubId(), member.getUserId()));
        return snapshot(active);
    }

    private static String snapshot(ClubMemberDO member) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", member.getId());
        snapshot.put("clubId", member.getClubId());
        snapshot.put("userId", member.getUserId());
        snapshot.put("userNameSnapshot", member.getUserNameSnapshot());
        snapshot.put("clubCodeSnapshot", member.getClubCodeSnapshot());
        snapshot.put("clubNameSnapshot", member.getClubNameSnapshot());
        snapshot.put("status", member.getStatus());
        snapshot.put("joinTime", member.getJoinTime());
        snapshot.put("leaveTime", member.getLeaveTime());
        snapshot.put("leaveReasonType", member.getLeaveReasonType());
        snapshot.put("leaveReason", member.getLeaveReason());
        snapshot.put("operatorUserId", member.getOperatorUserId());
        snapshot.put("activeUniqueKey", member.getActiveUniqueKey());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String buildActiveUniqueKey(Long clubId, Long userId) {
        return clubId + ":" + userId;
    }

}
