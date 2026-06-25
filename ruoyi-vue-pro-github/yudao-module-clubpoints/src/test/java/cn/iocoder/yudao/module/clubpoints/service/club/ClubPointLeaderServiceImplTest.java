package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointLeaderAssignReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointLeaderRemoveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_LEADER_ASSIGN;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_LEADER_REMOVE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEADER_ALREADY_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEADER_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_IS_DISABLE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointLeaderServiceImpl.class, ClubAuditServiceImpl.class, ClubScopeServiceImpl.class,
        ClubPointLeaderServiceImplTest.TestAdminUserApi.class})
class ClubPointLeaderServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ClubPointLeaderService clubPointLeaderService;
    @Resource
    private ClubScopeService clubScopeService;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private TestAdminUserApi adminUserApi;
    @Resource
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        adminUserApi.clear();
    }

    @Test
    void assignLeaderShouldCreateActiveLeaderWithoutMembershipAndWriteAudit() {
        ClubPointClubDO club = insertClub("CLUB-M5-5001", "Leader Club");
        adminUserApi.putUser(2001L, "Leader-2001", CommonStatusEnum.ENABLE.getStatus());

        Long leaderId = clubPointLeaderService.assignLeader(buildAssignReq(club.getId(), 2001L, true));

        ClubLeaderDO leader = leaderMapper.selectById(leaderId);
        assertEquals(club.getId(), leader.getClubId());
        assertEquals(2001L, leader.getUserId());
        assertEquals(ClubPointLeaderStatusEnum.ACTIVE.getStatus(), leader.getStatus());
        assertEquals(900L, leader.getAssignedBy());
        assertEquals("assign leader", leader.getReason());
        assertEquals("Leader Club", leader.getClubNameSnapshot());
        assertEquals("Leader-2001", leader.getUserNameSnapshot());
        assertEquals(club.getId() + ":2001", leader.getActiveUniqueKey());
        assertNotNull(leader.getAssignedTime());
        assertDoesNotThrow(() -> clubScopeService.validateManagedClub(2001L, club.getId()));
        ClubAuditLogDO auditLog = auditLogMapper.selectOne(null);
        assertEquals(CLUB_LEADER_ASSIGN, auditLog.getActionType());
        assertEquals("CLUB_LEADER", auditLog.getBizType());
        assertEquals(leaderId, auditLog.getBizId());
        assertTrue(auditLog.getAfterJson().contains("\"status\":1"));
    }

    @Test
    void assignLeaderShouldRejectDuplicateActiveLeader() {
        ClubPointClubDO club = insertClub("CLUB-M5-5002", "Duplicate Leader Club");
        adminUserApi.putUser(2002L, "Leader-2002", CommonStatusEnum.ENABLE.getStatus());
        clubPointLeaderService.assignLeader(buildAssignReq(club.getId(), 2002L, true));

        assertServiceException(() -> clubPointLeaderService.assignLeader(buildAssignReq(club.getId(), 2002L, true)),
                CLUB_LEADER_ALREADY_EXISTS);
    }

    @Test
    void assignLeaderShouldRejectDisabledUser() {
        ClubPointClubDO club = insertClub("CLUB-M5-5003", "Disabled User Club");
        adminUserApi.putUser(2003L, "Leader-2003", CommonStatusEnum.DISABLE.getStatus());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> clubPointLeaderService.assignLeader(buildAssignReq(club.getId(), 2003L, true)));
        assertEquals(USER_IS_DISABLE.getCode(), exception.getCode());
        assertEquals("名字为【Leader-2003】的用户已被禁用", exception.getMessage());

        assertEquals(0L, leaderMapper.selectCount());
        assertEquals(0L, auditLogMapper.selectCount());
    }

    @Test
    void removeLeaderShouldDeactivateLeaderWriteAuditAndShrinkScope() {
        ClubPointClubDO club = insertClub("CLUB-M5-5004", "Remove Leader Club");
        adminUserApi.putUser(2004L, "Leader-2004", CommonStatusEnum.ENABLE.getStatus());
        Long leaderId = clubPointLeaderService.assignLeader(buildAssignReq(club.getId(), 2004L, true));

        clubPointLeaderService.removeLeader(buildRemoveReq(club.getId(), 2004L, true));

        ClubLeaderDO leader = leaderMapper.selectById(leaderId);
        assertEquals(ClubPointLeaderStatusEnum.REMOVED.getStatus(), leader.getStatus());
        assertEquals(900L, leader.getRemovedBy());
        assertEquals("remove leader", leader.getReason());
        assertNotNull(leader.getRemovedTime());
        assertNull(leader.getActiveUniqueKey());
        assertServiceException(() -> clubScopeService.validateManagedClub(2004L, club.getId()), CLUB_SCOPE_DENIED);
        ClubAuditLogDO auditLog = selectLatestAuditLog();
        assertEquals(CLUB_LEADER_REMOVE, auditLog.getActionType());
        assertEquals(leaderId, auditLog.getBizId());
        assertTrue(auditLog.getBeforeJson().contains("\"status\":1"));
        assertTrue(auditLog.getAfterJson().contains("\"status\":2"));
    }

    @Test
    void removeLeaderShouldRejectMissingLeader() {
        ClubPointClubDO club = insertClub("CLUB-M5-5005", "Missing Leader Club");

        assertServiceException(() -> clubPointLeaderService.removeLeader(buildRemoveReq(club.getId(), 404L, true)),
                CLUB_LEADER_NOT_EXISTS);
    }

    @Test
    void nonGlobalOperatorShouldNotAssignOrRemoveLeaders() {
        ClubPointClubDO club = insertClub("CLUB-M5-5006", "Scope Leader Club");
        adminUserApi.putUser(2006L, "Leader-2006", CommonStatusEnum.ENABLE.getStatus());

        assertServiceException(() -> clubPointLeaderService.assignLeader(buildAssignReq(club.getId(), 2006L, false)),
                CLUB_SCOPE_DENIED);

        assertEquals(0L, leaderMapper.selectCount());
        Long leaderId = clubPointLeaderService.assignLeader(buildAssignReq(club.getId(), 2006L, true));
        assertServiceException(() -> clubPointLeaderService.removeLeader(buildRemoveReq(club.getId(), 2006L, false)),
                CLUB_SCOPE_DENIED);
        ClubLeaderDO leader = leaderMapper.selectById(leaderId);
        assertEquals(ClubPointLeaderStatusEnum.ACTIVE.getStatus(), leader.getStatus());
        assertEquals(club.getId() + ":2006", leader.getActiveUniqueKey());
    }

    @Test
    void removeLeaderShouldRollbackWhenAuditFails() {
        ClubPointClubDO club = insertClub("CLUB-M5-5007", "Rollback Leader Club");
        adminUserApi.putUser(2007L, "Leader-2007", CommonStatusEnum.ENABLE.getStatus());
        Long leaderId = clubPointLeaderService.assignLeader(buildAssignReq(club.getId(), 2007L, true));
        ClubPointLeaderRemoveReqBO reqBO = buildRemoveReq(club.getId(), 2007L, true)
                .setOperatorNameSnapshot(null);

        assertServiceException(() -> clubPointLeaderService.removeLeader(reqBO), CLUB_AUDIT_WRITE_FAILED);

        ClubLeaderDO leader = leaderMapper.selectById(leaderId);
        assertEquals(ClubPointLeaderStatusEnum.ACTIVE.getStatus(), leader.getStatus());
        assertEquals(club.getId() + ":2007", leader.getActiveUniqueKey());
        assertDoesNotThrow(() -> clubScopeService.validateManagedClub(2007L, club.getId()));
        assertEquals(1L, auditLogMapper.selectCount());
    }

    private ClubPointClubDO insertClub(String code, String name) {
        ClubPointClubDO club = new ClubPointClubDO()
                .setCode(code)
                .setName(name)
                .setStatus(ClubPointClubStatusEnum.ENABLED.getStatus())
                .setDescription("desc")
                .setContactText("contact")
                .setSort(10)
                .setRemark("remark");
        clubMapper.insert(club);
        return club;
    }

    private static ClubPointLeaderAssignReqBO buildAssignReq(Long clubId, Long userId, boolean operatorGlobalScope) {
        return new ClubPointLeaderAssignReqBO()
                .setClubId(clubId)
                .setUserId(userId)
                .setOperatorGlobalScope(operatorGlobalScope)
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("assign leader");
    }

    private static ClubPointLeaderRemoveReqBO buildRemoveReq(Long clubId, Long userId, boolean operatorGlobalScope) {
        return new ClubPointLeaderRemoveReqBO()
                .setClubId(clubId)
                .setUserId(userId)
                .setOperatorGlobalScope(operatorGlobalScope)
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("remove leader");
    }

    private ClubAuditLogDO selectLatestAuditLog() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM club_points_audit_log ORDER BY id DESC LIMIT 1");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return auditLogMapper.selectById(resultSet.getLong(1));
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    static class TestAdminUserApi implements AdminUserApi {

        private final Map<Long, AdminUserRespDTO> users = new ConcurrentHashMap<>();

        void clear() {
            users.clear();
        }

        void putUser(Long id, String nickname, Integer status) {
            AdminUserRespDTO user = new AdminUserRespDTO();
            user.setId(id);
            user.setNickname(nickname);
            user.setStatus(status);
            users.put(id, user);
        }

        @Override
        public AdminUserRespDTO getUser(Long id) {
            return users.get(id);
        }

        @Override
        public List<AdminUserRespDTO> getUserListBySubordinate(Long id) {
            return new ArrayList<>();
        }

        @Override
        public List<AdminUserRespDTO> getUserList(Collection<Long> ids) {
            List<AdminUserRespDTO> result = new ArrayList<>();
            for (Long id : ids) {
                AdminUserRespDTO user = users.get(id);
                if (user != null) {
                    result.add(user);
                }
            }
            return result;
        }

        @Override
        public List<AdminUserRespDTO> getUserListByDeptIds(Collection<Long> deptIds) {
            return new ArrayList<>();
        }

        @Override
        public List<AdminUserRespDTO> getUserListByPostIds(Collection<Long> postIds) {
            return new ArrayList<>();
        }

        @Override
        public List<AdminUserRespDTO> getUserListByNickname(String nickname) {
            return new ArrayList<>();
        }

        @Override
        public void validateUserList(Collection<Long> ids) {
            for (Long id : ids) {
                AdminUserRespDTO user = users.get(id);
                if (user == null) {
                    throw exception(USER_NOT_EXISTS);
                }
                if (CommonStatusEnum.isDisable(user.getStatus())) {
                    throw exception(USER_IS_DISABLE, user.getNickname());
                }
            }
        }

    }

}
