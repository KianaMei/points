package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubLeaderBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubLeaderPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_FOUND;

/**
 * 俱乐部查询服务实现
 */
@Service
public class ClubPointClubQueryServiceImpl implements ClubPointClubQueryService {

    private static final Integer MEMBER_ACTIVE = ClubPointMemberStatusEnum.ACTIVE.getStatus();
    private static final Integer LEADER_ACTIVE = ClubPointLeaderStatusEnum.ACTIVE.getStatus();

    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubScopeService clubScopeService;

    @Override
    @Transactional(readOnly = true)
    public List<ClubPointClubInfoBO> getAppMyClubList(Long loginUserId) {
        clubScopeService.validateSelf(loginUserId, loginUserId);
        List<ClubMemberDO> members = memberMapper.selectActiveListByUserId(loginUserId, MEMBER_ACTIVE);
        if (members.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, ClubMemberDO> memberMap = members.stream()
                .collect(Collectors.toMap(ClubMemberDO::getClubId, Function.identity(), (left, right) -> left));
        return clubMapper.selectListByIds(memberMap.keySet()).stream()
                .map(club -> toClubInfo(club, true, memberMap.get(club.getId()).getJoinTime()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointClubInfoBO> getAppJoinableClubPage(Long loginUserId, ClubPointClubPageReqBO reqBO) {
        clubScopeService.validateSelf(loginUserId, loginUserId);
        List<Long> joinedClubIds = memberMapper.selectClubIdsByUserIdAndStatus(loginUserId, MEMBER_ACTIVE);
        PageResult<ClubPointClubDO> pageResult = clubMapper.selectJoinablePage(reqBO, reqBO.getKeyword(), joinedClubIds);
        return toClubInfoPage(pageResult, false, Collections.emptyMap());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointClubMemberBO> getAppMemberPage(Long loginUserId, ClubPointClubMemberPageReqBO reqBO) {
        clubScopeService.validateJoinedClub(loginUserId, reqBO.getClubId());
        return toMemberPage(memberMapper.selectPageByClubIdAndStatus(reqBO, reqBO.getClubId(), MEMBER_ACTIVE,
                reqBO.getUserId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubPointClubInfoBO> getLeaderManagedClubList(Long loginUserId) {
        List<ClubLeaderDO> leaders = leaderMapper.selectActiveListByUserId(loginUserId, LEADER_ACTIVE);
        if (leaders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, LocalDateTime> assignedTimeMap = leaders.stream()
                .collect(Collectors.toMap(ClubLeaderDO::getClubId, ClubLeaderDO::getAssignedTime, (left, right) -> left));
        return clubMapper.selectListByIds(assignedTimeMap.keySet()).stream()
                .map(club -> toClubInfo(club, false, assignedTimeMap.get(club.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClubPointClubInfoBO getLeaderClub(Long loginUserId, Long clubId) {
        clubScopeService.validateManagedClub(loginUserId, clubId);
        return toClubInfo(validateClubExists(clubId), false, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointClubInfoBO> getAdminClubPage(ClubPointClubPageReqBO reqBO) {
        return toClubInfoPage(clubMapper.selectPage(reqBO, reqBO.getKeyword(), reqBO.getStatus()),
                false, Collections.emptyMap());
    }

    @Override
    @Transactional(readOnly = true)
    public ClubPointClubInfoBO getAdminClub(Long clubId) {
        return toClubInfo(validateClubExists(clubId), false, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointClubMemberBO> getAdminMemberPage(ClubPointClubMemberPageReqBO reqBO) {
        return toMemberPage(memberMapper.selectPageByClubId(reqBO, reqBO.getClubId(), reqBO.getStatus(),
                reqBO.getUserId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointClubLeaderBO> getAdminLeaderPage(ClubPointClubLeaderPageReqBO reqBO) {
        PageResult<ClubLeaderDO> pageResult = leaderMapper.selectPageByClubId(reqBO, reqBO.getClubId(),
                reqBO.getStatus(), reqBO.getUserId());
        List<ClubPointClubLeaderBO> list = pageResult.getList().stream()
                .map(this::toLeaderBO)
                .collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    private PageResult<ClubPointClubInfoBO> toClubInfoPage(PageResult<ClubPointClubDO> pageResult, Boolean joined,
                                                           Map<Long, LocalDateTime> joinedTimeMap) {
        List<ClubPointClubInfoBO> list = pageResult.getList().stream()
                .map(club -> toClubInfo(club, joined, joinedTimeMap.get(club.getId())))
                .collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    private ClubPointClubInfoBO toClubInfo(ClubPointClubDO club, Boolean joined, LocalDateTime joinedTime) {
        List<ClubLeaderDO> leaders = leaderMapper.selectListByClubIdAndStatus(club.getId(), LEADER_ACTIVE);
        List<String> leaderNames = leaders.stream()
                .map(ClubLeaderDO::getUserNameSnapshot)
                .collect(Collectors.toList());
        return new ClubPointClubInfoBO()
                .setId(club.getId())
                .setCode(club.getCode())
                .setName(club.getName())
                .setStatus(club.getStatus())
                .setDescription(club.getDescription())
                .setContactText(club.getContactText())
                .setCoverFileId(club.getCoverFileId())
                .setSort(club.getSort())
                .setDisabledTime(club.getDisabledTime())
                .setDisabledReason(club.getDisabledReason())
                .setRemark(club.getRemark())
                .setMemberCount(memberMapper.selectCountByClubIdAndStatus(club.getId(), MEMBER_ACTIVE).intValue())
                .setLeaderCount(leaderMapper.selectCountByClubIdAndStatus(club.getId(), LEADER_ACTIVE).intValue())
                .setLeaderNames(leaderNames)
                .setJoined(joined)
                .setJoinedTime(joinedTime);
    }

    private PageResult<ClubPointClubMemberBO> toMemberPage(PageResult<ClubMemberDO> pageResult) {
        List<ClubPointClubMemberBO> list = pageResult.getList().stream()
                .map(this::toMemberBO)
                .collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    private ClubPointClubMemberBO toMemberBO(ClubMemberDO member) {
        ClubPointAccountDO account = accountMapper.selectByUserId(member.getUserId());
        return new ClubPointClubMemberBO()
                .setId(member.getId())
                .setClubId(member.getClubId())
                .setClubCodeSnapshot(member.getClubCodeSnapshot())
                .setClubNameSnapshot(member.getClubNameSnapshot())
                .setUserId(member.getUserId())
                .setNickname(member.getUserNameSnapshot())
                .setDeptId(member.getDeptIdSnapshot())
                .setDeptName(member.getDeptNameSnapshot())
                .setMobile(member.getMobileSnapshot())
                .setStatus(member.getStatus())
                .setJoinedTime(member.getJoinTime())
                .setLeaveTime(member.getLeaveTime())
                .setLeaveReasonType(member.getLeaveReasonType())
                .setLeaveReason(member.getLeaveReason())
                .setLeader(leaderMapper.selectByUserIdAndClubIdAndStatus(member.getUserId(), member.getClubId(),
                        LEADER_ACTIVE) != null)
                .setAvailablePoints(account == null ? 0 : account.getAvailablePoints());
    }

    private ClubPointClubLeaderBO toLeaderBO(ClubLeaderDO leader) {
        return new ClubPointClubLeaderBO()
                .setId(leader.getId())
                .setClubId(leader.getClubId())
                .setUserId(leader.getUserId())
                .setUserNameSnapshot(leader.getUserNameSnapshot())
                .setClubNameSnapshot(leader.getClubNameSnapshot())
                .setStatus(leader.getStatus())
                .setAssignedTime(leader.getAssignedTime())
                .setRemovedTime(leader.getRemovedTime())
                .setAssignedBy(leader.getAssignedBy())
                .setRemovedBy(leader.getRemovedBy())
                .setReason(leader.getReason());
    }

    private ClubPointClubDO validateClubExists(Long clubId) {
        ClubPointClubDO club = clubMapper.selectById(clubId);
        if (club == null) {
            throw exception(CLUB_NOT_FOUND);
        }
        return club;
    }

}
