package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberAddReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberExitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberJoinReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberRemoveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_ADD;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_EXIT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_JOIN;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_REMOVE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ALREADY_JOINED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_DISABLED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_MEMBER;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointMemberServiceImpl.class, ClubAuditServiceImpl.class, ClubScopeServiceImpl.class})
class ClubPointMemberServiceImplTest extends BaseDbUnitTest {

    private static final int REGISTRATION_STATUS_REGISTERED = 1;
    private static final int REGISTRATION_STATUS_CANCELED = 2;
    private static final int CANCEL_REASON_EXIT_CLUB = 2;
    private static final int CANCEL_REASON_ADMIN_REMOVE = 3;
    private static final int LEAVE_REASON_SELF_EXIT = 1;
    private static final int LEAVE_REASON_ADMIN_REMOVE = 2;

    @Resource
    private ClubPointMemberService clubPointMemberService;
    @Resource
    private ClubScopeService clubScopeService;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private DataSource dataSource;

    @Test
    void joinClubShouldCreateActiveMemberAndWriteAudit() {
        ClubPointClubDO club = insertClub("CLUB-M5-4001", "Basketball", ClubPointClubStatusEnum.ENABLED.getStatus());

        Long memberId = clubPointMemberService.joinClub(buildJoinReq(club.getId(), 1001L));

        ClubMemberDO member = memberMapper.selectById(memberId);
        assertEquals(club.getId(), member.getClubId());
        assertEquals(1001L, member.getUserId());
        assertEquals(ClubPointMemberStatusEnum.ACTIVE.getStatus(), member.getStatus());
        assertEquals("User-1001", member.getUserNameSnapshot());
        assertEquals("Dept-1001", member.getDeptNameSnapshot());
        assertEquals("CLUB-M5-4001", member.getClubCodeSnapshot());
        assertEquals("Basketball", member.getClubNameSnapshot());
        assertEquals(club.getId() + ":1001", member.getActiveUniqueKey());
        assertNotNull(member.getJoinTime());
        ClubAuditLogDO auditLog = auditLogMapper.selectOne(null);
        assertEquals(CLUB_MEMBER_JOIN, auditLog.getActionType());
        assertEquals("CLUB_MEMBER", auditLog.getBizType());
        assertEquals(memberId, auditLog.getBizId());
        assertTrue(auditLog.getAfterJson().contains("\"status\":1"));
    }

    @Test
    void addMemberShouldCreateActiveMemberForAdminAndWriteAudit() {
        ClubPointClubDO club = insertClub("CLUB-M5-4002", "Run", ClubPointClubStatusEnum.ENABLED.getStatus());

        Long memberId = clubPointMemberService.addMember(buildAddReq(club.getId(), 1002L));

        ClubMemberDO member = memberMapper.selectById(memberId);
        assertEquals(ClubPointMemberStatusEnum.ACTIVE.getStatus(), member.getStatus());
        assertEquals(900L, member.getOperatorUserId());
        ClubAuditLogDO auditLog = auditLogMapper.selectOne(null);
        assertEquals(CLUB_MEMBER_ADD, auditLog.getActionType());
        assertEquals(memberId, auditLog.getBizId());
    }

    @Test
    void joinClubShouldRejectDuplicateActiveMember() {
        ClubPointClubDO club = insertClub("CLUB-M5-4003", "Photo", ClubPointClubStatusEnum.ENABLED.getStatus());
        clubPointMemberService.joinClub(buildJoinReq(club.getId(), 1003L));

        assertServiceException(() -> clubPointMemberService.joinClub(buildJoinReq(club.getId(), 1003L)),
                CLUB_ALREADY_JOINED);
    }

    @Test
    void joinClubShouldRejectDisabledClub() {
        ClubPointClubDO club = insertClub("CLUB-M5-4004", "Disabled", ClubPointClubStatusEnum.DISABLED.getStatus());

        assertServiceException(() -> clubPointMemberService.joinClub(buildJoinReq(club.getId(), 1004L)),
                CLUB_DISABLED);

        assertEquals(0L, memberMapper.selectCount());
        assertEquals(0L, auditLogMapper.selectCount());
    }

    @Test
    void exitClubShouldMarkMemberInactiveCancelRegistrationsAndShrinkScope() {
        ClubPointClubDO club = insertClub("CLUB-M5-4005", "Exit", ClubPointClubStatusEnum.ENABLED.getStatus());
        Long memberId = clubPointMemberService.joinClub(buildJoinReq(club.getId(), 1005L));
        Long registrationId = insertActiveRegistration(club.getId(), 1005L, "ACT-EXIT-4005");

        clubPointMemberService.exitClub(buildExitReq(club.getId(), 1005L));

        ClubMemberDO member = memberMapper.selectById(memberId);
        assertEquals(ClubPointMemberStatusEnum.SELF_EXITED.getStatus(), member.getStatus());
        assertEquals(LEAVE_REASON_SELF_EXIT, member.getLeaveReasonType());
        assertEquals("self exit", member.getLeaveReason());
        assertEquals(1005L, member.getOperatorUserId());
        assertNotNull(member.getLeaveTime());
        assertNull(member.getActiveUniqueKey());
        RegistrationSnapshot registration = selectRegistration(registrationId);
        assertEquals(REGISTRATION_STATUS_CANCELED, registration.status);
        assertEquals(CANCEL_REASON_EXIT_CLUB, registration.cancelReasonType);
        assertEquals("self exit", registration.cancelReason);
        assertEquals(1005L, registration.cancelOperatorUserId);
        assertTrue(registration.noAbsenceDeduct);
        assertNull(registration.activeUniqueKey);
        assertServiceException(() -> clubScopeService.validateJoinedClub(1005L, club.getId()), CLUB_SCOPE_DENIED);
        ClubAuditLogDO auditLog = selectLatestAuditLog();
        assertEquals(CLUB_MEMBER_EXIT, auditLog.getActionType());
        assertEquals(memberId, auditLog.getBizId());
        assertTrue(auditLog.getBeforeJson().contains("\"status\":1"));
        assertTrue(auditLog.getAfterJson().contains("\"status\":2"));
    }

    @Test
    void removeMemberShouldMarkRemovedCancelRegistrationsAndKeepHistoryRows() {
        ClubPointClubDO club = insertClub("CLUB-M5-4006", "Remove", ClubPointClubStatusEnum.ENABLED.getStatus());
        Long memberId = clubPointMemberService.joinClub(buildJoinReq(club.getId(), 1006L));
        Long registrationId = insertActiveRegistration(club.getId(), 1006L, "ACT-REMOVE-4006");
        insertHistoricalActivity(club.getId(), club.getCode(), club.getName());
        insertHistoricalLedger(club.getId(), club.getCode(), club.getName(), 1006L);

        clubPointMemberService.removeMember(buildRemoveReq(club.getId(), 1006L));

        ClubMemberDO member = memberMapper.selectById(memberId);
        assertEquals(ClubPointMemberStatusEnum.ADMIN_REMOVED.getStatus(), member.getStatus());
        assertEquals(LEAVE_REASON_ADMIN_REMOVE, member.getLeaveReasonType());
        assertEquals("admin remove", member.getLeaveReason());
        assertEquals(900L, member.getOperatorUserId());
        assertNull(member.getActiveUniqueKey());
        RegistrationSnapshot registration = selectRegistration(registrationId);
        assertEquals(REGISTRATION_STATUS_CANCELED, registration.status);
        assertEquals(CANCEL_REASON_ADMIN_REMOVE, registration.cancelReasonType);
        assertEquals(900L, registration.cancelOperatorUserId);
        assertTrue(registration.noAbsenceDeduct);
        assertEquals(1L, countRows("club_points_activity", "club_id", club.getId()));
        assertEquals(1L, countRows("club_points_transaction", "issuing_club_id", club.getId()));
        assertServiceException(() -> clubScopeService.validateJoinedClub(1006L, club.getId()), CLUB_SCOPE_DENIED);
        ClubAuditLogDO auditLog = selectLatestAuditLog();
        assertEquals(CLUB_MEMBER_REMOVE, auditLog.getActionType());
        assertEquals(memberId, auditLog.getBizId());
    }

    @Test
    void exitOrRemoveMissingMemberShouldFail() {
        ClubPointClubDO club = insertClub("CLUB-M5-4007", "Missing", ClubPointClubStatusEnum.ENABLED.getStatus());

        assertServiceException(() -> clubPointMemberService.exitClub(buildExitReq(club.getId(), 404L)),
                CLUB_NOT_MEMBER);
        assertServiceException(() -> clubPointMemberService.removeMember(buildRemoveReq(club.getId(), 404L)),
                CLUB_NOT_MEMBER);
    }

    @Test
    void removeMemberShouldRollbackMemberAndRegistrationsWhenAuditFails() {
        ClubPointClubDO club = insertClub("CLUB-M5-4008", "Rollback", ClubPointClubStatusEnum.ENABLED.getStatus());
        Long memberId = clubPointMemberService.joinClub(buildJoinReq(club.getId(), 1008L));
        Long registrationId = insertActiveRegistration(club.getId(), 1008L, "ACT-ROLLBACK-4008");
        ClubPointMemberRemoveReqBO reqBO = buildRemoveReq(club.getId(), 1008L).setOperatorNameSnapshot(null);

        assertServiceException(() -> clubPointMemberService.removeMember(reqBO), CLUB_AUDIT_WRITE_FAILED);

        ClubMemberDO member = memberMapper.selectById(memberId);
        assertEquals(ClubPointMemberStatusEnum.ACTIVE.getStatus(), member.getStatus());
        assertEquals(club.getId() + ":1008", member.getActiveUniqueKey());
        RegistrationSnapshot registration = selectRegistration(registrationId);
        assertEquals(REGISTRATION_STATUS_REGISTERED, registration.status);
        assertFalse(registration.noAbsenceDeduct);
        assertEquals(1L, auditLogMapper.selectCount());
    }

    private ClubPointClubDO insertClub(String code, String name, Integer status) {
        ClubPointClubDO club = new ClubPointClubDO()
                .setCode(code)
                .setName(name)
                .setStatus(status)
                .setDescription("desc")
                .setContactText("contact")
                .setSort(10)
                .setRemark("remark");
        clubMapper.insert(club);
        return club;
    }

    private static ClubPointMemberJoinReqBO buildJoinReq(Long clubId, Long userId) {
        return new ClubPointMemberJoinReqBO()
                .setClubId(clubId)
                .setUserId(userId)
                .setDeptIdSnapshot(10L)
                .setUserNameSnapshot("User-" + userId)
                .setDeptNameSnapshot("Dept-" + userId)
                .setMobileSnapshot("1380000" + userId)
                .setOperatorUserId(userId)
                .setOperatorNameSnapshot("User-" + userId)
                .setOperatorRoleSnapshot("employee")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("join");
    }

    private static ClubPointMemberAddReqBO buildAddReq(Long clubId, Long userId) {
        return new ClubPointMemberAddReqBO()
                .setClubId(clubId)
                .setUserId(userId)
                .setDeptIdSnapshot(20L)
                .setUserNameSnapshot("User-" + userId)
                .setDeptNameSnapshot("Dept-" + userId)
                .setMobileSnapshot("1390000" + userId)
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("admin add");
    }

    private static ClubPointMemberExitReqBO buildExitReq(Long clubId, Long userId) {
        return new ClubPointMemberExitReqBO()
                .setClubId(clubId)
                .setUserId(userId)
                .setOperatorUserId(userId)
                .setOperatorNameSnapshot("User-" + userId)
                .setOperatorRoleSnapshot("employee")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("self exit");
    }

    private static ClubPointMemberRemoveReqBO buildRemoveReq(Long clubId, Long userId) {
        return new ClubPointMemberRemoveReqBO()
                .setClubId(clubId)
                .setUserId(userId)
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("admin remove");
    }

    private Long insertActiveRegistration(Long clubId, Long userId, String uniqueSuffix) {
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 10, 0);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO club_points_activity_registration "
                     + "(activity_id, club_id, user_id, status, register_time, no_absence_deduct, "
                     + "special_absence_flag, user_name_snapshot, dept_id_snapshot, dept_name_snapshot, mobile_snapshot, "
                     + "club_name_snapshot, activity_title_snapshot, activity_start_time_snapshot, "
                     + "activity_end_time_snapshot, active_unique_key, create_time, update_time, deleted) "
                     + "VALUES (?, ?, ?, ?, ?, FALSE, FALSE, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                     + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, Math.abs(uniqueSuffix.hashCode()));
            statement.setLong(2, clubId);
            statement.setLong(3, userId);
            statement.setInt(4, REGISTRATION_STATUS_REGISTERED);
            statement.setTimestamp(5, Timestamp.valueOf(now));
            statement.setString(6, "User-" + userId);
            statement.setLong(7, 10L);
            statement.setString(8, "Dept-" + userId);
            statement.setString(9, "1380000" + userId);
            statement.setString(10, "Club-" + clubId);
            statement.setString(11, "Activity-" + uniqueSuffix);
            statement.setTimestamp(12, Timestamp.valueOf(now.plusDays(1)));
            statement.setTimestamp(13, Timestamp.valueOf(now.plusDays(1).plusHours(2)));
            statement.setString(14, uniqueSuffix + ":" + userId);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();
                return generatedKeys.getLong(1);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void insertHistoricalActivity(Long clubId, String clubCode, String clubName) {
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 10, 0);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO club_points_activity "
                     + "(club_id, club_code_snapshot, club_name_snapshot, title, level, status, start_time, end_time, "
                     + "registration_deadline, checkin_start_time, checkin_end_time, checkout_mode, "
                     + "checkout_start_time, checkout_end_time, creator_user_id, create_time, update_time, deleted) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                     + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE)")) {
            statement.setLong(1, clubId);
            statement.setString(2, clubCode);
            statement.setString(3, clubName);
            statement.setString(4, "Historical Activity");
            statement.setInt(5, 1);
            statement.setInt(6, 1);
            statement.setTimestamp(7, Timestamp.valueOf(now));
            statement.setTimestamp(8, Timestamp.valueOf(now.plusHours(2)));
            statement.setTimestamp(9, Timestamp.valueOf(now.minusDays(1)));
            statement.setTimestamp(10, Timestamp.valueOf(now.minusMinutes(30)));
            statement.setTimestamp(11, Timestamp.valueOf(now.plusMinutes(30)));
            statement.setInt(12, 1);
            statement.setTimestamp(13, Timestamp.valueOf(now.plusHours(1)));
            statement.setTimestamp(14, Timestamp.valueOf(now.plusHours(2)));
            statement.setLong(15, 900L);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void insertHistoricalLedger(Long clubId, String clubCode, String clubName, Long userId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO club_points_transaction "
                     + "(transaction_no, user_id, user_name_snapshot, direction, points, point_category, status, "
                     + "source_type, source_title_snapshot, issuing_club_id, issuing_club_code_snapshot, "
                     + "issuing_club_name_snapshot, rule_version_id, occurred_at, business_year, business_month, "
                     + "idempotency_key, create_time, update_time, deleted) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                     + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE)")) {
            statement.setString(1, "TX-M5-4006-" + userId);
            statement.setLong(2, userId);
            statement.setString(3, "User-" + userId);
            statement.setInt(4, 1);
            statement.setInt(5, 10);
            statement.setInt(6, 1);
            statement.setInt(7, 1);
            statement.setInt(8, 1);
            statement.setString(9, "Historical Ledger");
            statement.setLong(10, clubId);
            statement.setString(11, clubCode);
            statement.setString(12, clubName);
            statement.setLong(13, 1L);
            statement.setTimestamp(14, Timestamp.valueOf(LocalDateTime.of(2026, 6, 1, 10, 0)));
            statement.setInt(15, 2026);
            statement.setInt(16, 202606);
            statement.setString(17, "IDEMP-M5-4006-" + userId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private RegistrationSnapshot selectRegistration(Long registrationId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT status, cancel_reason_type, "
                     + "cancel_reason, cancel_operator_user_id, no_absence_deduct, active_unique_key "
                     + "FROM club_points_activity_registration WHERE id = ?")) {
            statement.setLong(1, registrationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return new RegistrationSnapshot(resultSet.getInt("status"),
                        resultSet.getInt("cancel_reason_type"),
                        resultSet.getString("cancel_reason"),
                        resultSet.getLong("cancel_operator_user_id"),
                        resultSet.getBoolean("no_absence_deduct"),
                        resultSet.getString("active_unique_key"));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Long countRows(String tableName, String columnName, Long value) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(1) FROM " + tableName + " WHERE " + columnName + " = ?")) {
            statement.setLong(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
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

    private static final class RegistrationSnapshot {

        private final Integer status;
        private final Integer cancelReasonType;
        private final String cancelReason;
        private final Long cancelOperatorUserId;
        private final Boolean noAbsenceDeduct;
        private final String activeUniqueKey;

        private RegistrationSnapshot(Integer status, Integer cancelReasonType, String cancelReason,
                                     Long cancelOperatorUserId, Boolean noAbsenceDeduct, String activeUniqueKey) {
            this.status = status;
            this.cancelReasonType = cancelReasonType;
            this.cancelReason = cancelReason;
            this.cancelOperatorUserId = cancelOperatorUserId;
            this.noAbsenceDeduct = noAbsenceDeduct;
            this.activeUniqueKey = activeUniqueKey;
        }

    }

}
