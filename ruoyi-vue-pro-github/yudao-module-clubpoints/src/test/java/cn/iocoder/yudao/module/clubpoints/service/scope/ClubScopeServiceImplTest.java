package cn.iocoder.yudao.module.clubpoints.service.scope;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(ClubScopeServiceImpl.class)
class ClubScopeServiceImplTest extends BaseDbUnitTest {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_LEFT = 2;
    private static final int STATUS_REMOVED = 2;

    @Resource
    private ClubScopeService clubScopeService;
    @Resource
    private ClubMemberMapper clubMemberMapper;
    @Resource
    private ClubLeaderMapper clubLeaderMapper;

    @Test
    void validateSelfShouldRejectOtherUser() {
        assertServiceException(() -> clubScopeService.validateSelf(100L, 101L), CLUB_SCOPE_DENIED);
    }

    @Test
    void validateSelfShouldAllowSameUser() {
        assertDoesNotThrow(() -> clubScopeService.validateSelf(100L, 100L));
    }

    @Test
    void validateJoinedClubShouldRejectClubNotJoined() {
        clubMemberMapper.insert(buildMember(100L, 1L, STATUS_ACTIVE));

        assertServiceException(() -> clubScopeService.validateJoinedClub(100L, 2L), CLUB_SCOPE_DENIED);
    }

    @Test
    void validateJoinedClubShouldRejectInactiveMembership() {
        clubMemberMapper.insert(buildMember(100L, 1L, STATUS_LEFT));

        assertServiceException(() -> clubScopeService.validateJoinedClub(100L, 1L), CLUB_SCOPE_DENIED);
    }

    @Test
    void validateJoinedClubShouldAllowActiveMembership() {
        clubMemberMapper.insert(buildMember(100L, 1L, STATUS_ACTIVE));

        assertDoesNotThrow(() -> clubScopeService.validateJoinedClub(100L, 1L));
    }

    @Test
    void validateManagedClubShouldRejectOtherClub() {
        clubLeaderMapper.insert(buildLeader(100L, 1L, STATUS_ACTIVE));

        assertServiceException(() -> clubScopeService.validateManagedClub(100L, 2L), CLUB_SCOPE_DENIED);
    }

    @Test
    void validateManagedClubShouldRejectInactiveLeader() {
        clubLeaderMapper.insert(buildLeader(100L, 1L, STATUS_REMOVED));

        assertServiceException(() -> clubScopeService.validateManagedClub(100L, 1L), CLUB_SCOPE_DENIED);
    }

    @Test
    void validateManagedClubShouldAllowActiveLeader() {
        clubLeaderMapper.insert(buildLeader(100L, 1L, STATUS_ACTIVE));

        assertDoesNotThrow(() -> clubScopeService.validateManagedClub(100L, 1L));
    }

    @Test
    void validateGlobalShouldRejectWithoutGlobalScope() {
        assertFalse(clubScopeService.hasGlobalScope(false));
        assertServiceException(() -> clubScopeService.validateGlobal(false), CLUB_SCOPE_DENIED);
    }

    @Test
    void validateGlobalShouldAllowGlobalScope() {
        assertTrue(clubScopeService.hasGlobalScope(true));
        assertDoesNotThrow(() -> clubScopeService.validateGlobal(true));
    }

    private static ClubMemberDO buildMember(Long userId, Long clubId, Integer status) {
        return new ClubMemberDO()
                .setClubId(clubId)
                .setUserId(userId)
                .setDeptIdSnapshot(10L)
                .setUserNameSnapshot("员工" + userId)
                .setDeptNameSnapshot("研发部")
                .setMobileSnapshot("13800000000")
                .setClubCodeSnapshot("CLUB-" + clubId)
                .setClubNameSnapshot("俱乐部" + clubId)
                .setStatus(status)
                .setJoinTime(LocalDateTime.now())
                .setActiveUniqueKey(status == STATUS_ACTIVE ? clubId + ":" + userId : null);
    }

    private static ClubLeaderDO buildLeader(Long userId, Long clubId, Integer status) {
        return new ClubLeaderDO()
                .setClubId(clubId)
                .setUserId(userId)
                .setStatus(status)
                .setAssignedTime(LocalDateTime.now())
                .setAssignedBy(1L)
                .setReason("任命负责人")
                .setClubNameSnapshot("俱乐部" + clubId)
                .setUserNameSnapshot("负责人" + userId)
                .setActiveUniqueKey(status == STATUS_ACTIVE ? clubId + ":" + userId : null);
    }

}
