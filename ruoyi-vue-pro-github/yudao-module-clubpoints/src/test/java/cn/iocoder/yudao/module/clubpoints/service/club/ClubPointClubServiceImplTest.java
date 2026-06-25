package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubDeleteReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubDisableReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubEnableReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubStrongConfirmReqBO;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_DISABLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_UPDATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.PHYSICAL_DELETE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_DELETE_HAS_REFERENCES;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_STRONG_CONFIRM_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointClubServiceImpl.class, ClubAuditServiceImpl.class})
class ClubPointClubServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ClubPointClubService clubPointClubService;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private DataSource dataSource;

    @Test
    void createClubShouldPersistEnabledClub() {
        Long clubId = clubPointClubService.createClub(buildSaveReq(null, "CLUB-M5-3001", "篮球俱乐部"));

        ClubPointClubDO club = clubMapper.selectById(clubId);
        assertEquals("CLUB-M5-3001", club.getCode());
        assertEquals("篮球俱乐部", club.getName());
        assertEquals(ClubPointClubStatusEnum.ENABLED.getStatus(), club.getStatus());
        assertEquals("介绍", club.getDescription());
        assertEquals("联系人", club.getContactText());
        assertEquals(101L, club.getCoverFileId());
        assertEquals(10, club.getSort());
        assertEquals("备注", club.getRemark());
        assertNull(club.getDisabledTime());
        assertNull(club.getDisabledReason());
    }

    @Test
    void updateClubShouldPersistChangesAndWriteAudit() {
        ClubPointClubDO club = insertClub("CLUB-M5-3002", "旧名称", ClubPointClubStatusEnum.ENABLED.getStatus());

        clubPointClubService.updateClub(buildSaveReq(club.getId(), "CLUB-M5-3002", "新名称"));

        ClubPointClubDO updated = clubMapper.selectById(club.getId());
        assertEquals("新名称", updated.getName());
        assertEquals("介绍", updated.getDescription());
        assertEquals(1L, auditLogMapper.selectCount());
        ClubAuditLogDO auditLog = auditLogMapper.selectOne(null);
        assertEquals(CLUB_UPDATE, auditLog.getActionType());
        assertEquals("CLUB", auditLog.getBizType());
        assertEquals(club.getId(), auditLog.getBizId());
        assertTrue(auditLog.getBeforeJson().contains("旧名称"));
        assertTrue(auditLog.getAfterJson().contains("新名称"));
    }

    @Test
    void updateMissingClubShouldFailWithClubNotFound() {
        assertServiceException(() -> clubPointClubService.updateClub(buildSaveReq(404L, "CLUB-M5-3404", "不存在")),
                CLUB_NOT_FOUND);
    }

    @Test
    void disableClubShouldSetDisabledStatusAndWriteAuditWithoutStrongConfirm() {
        ClubPointClubDO club = insertClub("CLUB-M5-3003", "羽毛球俱乐部", ClubPointClubStatusEnum.ENABLED.getStatus());

        clubPointClubService.disableClub(new ClubPointClubDisableReqBO()
                .setId(club.getId())
                .setReason("暂不开放")
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit"));

        ClubPointClubDO disabled = clubMapper.selectById(club.getId());
        assertEquals(ClubPointClubStatusEnum.DISABLED.getStatus(), disabled.getStatus());
        assertEquals("暂不开放", disabled.getDisabledReason());
        assertNotNull(disabled.getDisabledTime());
        ClubAuditLogDO auditLog = auditLogMapper.selectOne(null);
        assertEquals(CLUB_DISABLE, auditLog.getActionType());
        assertEquals("暂不开放", auditLog.getReason());
    }

    @Test
    void enableClubShouldRestoreEnabledStatusAndClearDisabledFields() {
        ClubPointClubDO club = insertClub("CLUB-M5-3004", "跑步俱乐部", ClubPointClubStatusEnum.DISABLED.getStatus());
        club.setDisabledTime(LocalDateTime.of(2026, 6, 1, 10, 0))
                .setDisabledReason("旧原因");
        clubMapper.updateById(club);

        clubPointClubService.enableClub(new ClubPointClubEnableReqBO()
                .setId(club.getId())
                .setReason("恢复开放")
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit"));

        ClubPointClubDO enabled = clubMapper.selectById(club.getId());
        assertEquals(ClubPointClubStatusEnum.ENABLED.getStatus(), enabled.getStatus());
        assertNull(enabled.getDisabledTime());
        assertNull(enabled.getDisabledReason());
    }

    @Test
    void deleteClubPhysicallyShouldRequireStrongConfirmText() {
        ClubPointClubDO club = insertClub("CLUB-M5-3005", "摄影俱乐部", ClubPointClubStatusEnum.DISABLED.getStatus());

        assertServiceException(() -> clubPointClubService.deleteClubPhysically(buildDeleteReq(club).setStrongConfirm(
                new ClubStrongConfirmReqBO()
                        .setConfirmText("错误确认文本")
                        .setConfirmedAt(LocalDateTime.now()))), CLUB_STRONG_CONFIRM_INVALID);

        assertNotNull(clubMapper.selectById(club.getId()));
        assertEquals(0L, auditLogMapper.selectCount());
    }

    @Test
    void deleteClubPhysicallyShouldRejectHistoricalLedgerReference() {
        ClubPointClubDO club = insertClub("CLUB-M5-3006", "历史俱乐部", ClubPointClubStatusEnum.DISABLED.getStatus());
        insertLedgerReference(club);

        assertServiceException(() -> clubPointClubService.deleteClubPhysically(buildDeleteReq(club)),
                CLUB_DELETE_HAS_REFERENCES);

        assertNotNull(clubMapper.selectById(club.getId()));
        assertEquals(1L, countClubRows(club.getId()));
        assertEquals(0L, auditLogMapper.selectCount());
    }

    @Test
    void deleteClubPhysicallyShouldWriteAuditAndRemoveClubMainRecord() {
        ClubPointClubDO club = insertClub("CLUB-M5-3007", "可删俱乐部", ClubPointClubStatusEnum.DISABLED.getStatus());

        clubPointClubService.deleteClubPhysically(buildDeleteReq(club));

        assertNull(clubMapper.selectById(club.getId()));
        assertEquals(0L, countClubRows(club.getId()));
        ClubAuditLogDO auditLog = auditLogMapper.selectOne(null);
        assertEquals(PHYSICAL_DELETE, auditLog.getActionType());
        assertEquals("CLUB", auditLog.getBizType());
        assertEquals(club.getId(), auditLog.getBizId());
        assertTrue(auditLog.getTargetSnapshotJson().contains("可删俱乐部"));
    }

    @Test
    void deleteClubPhysicallyShouldRollbackDeleteWhenAuditFails() {
        ClubPointClubDO club = insertClub("CLUB-M5-3008", "审计失败俱乐部", ClubPointClubStatusEnum.DISABLED.getStatus());
        ClubPointClubDeleteReqBO reqBO = buildDeleteReq(club).setOperatorNameSnapshot(null);

        assertServiceException(() -> clubPointClubService.deleteClubPhysically(reqBO), CLUB_AUDIT_WRITE_FAILED);

        assertNotNull(clubMapper.selectById(club.getId()));
        assertEquals(1L, countClubRows(club.getId()));
        assertEquals(0L, auditLogMapper.selectCount());
    }

    private ClubPointClubDO insertClub(String code, String name, Integer status) {
        ClubPointClubDO club = new ClubPointClubDO()
                .setCode(code)
                .setName(name)
                .setStatus(status)
                .setDescription("介绍")
                .setContactText("联系人")
                .setCoverFileId(101L)
                .setSort(10)
                .setRemark("备注");
        clubMapper.insert(club);
        return club;
    }

    private static ClubPointClubSaveReqBO buildSaveReq(Long id, String code, String name) {
        return new ClubPointClubSaveReqBO()
                .setId(id)
                .setCode(code)
                .setName(name)
                .setDescription("介绍")
                .setContactText("联系人")
                .setCoverFileId(101L)
                .setSort(10)
                .setRemark("备注")
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("维护俱乐部");
    }

    private static ClubPointClubDeleteReqBO buildDeleteReq(ClubPointClubDO club) {
        return new ClubPointClubDeleteReqBO()
                .setId(club.getId())
                .setStrongConfirm(new ClubStrongConfirmReqBO()
                        .setConfirmText("确认删除俱乐部：" + club.getName())
                        .setConfirmedAt(LocalDateTime.now()))
                .setReason("删除空俱乐部")
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

    private void insertLedgerReference(ClubPointClubDO club) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO club_points_transaction "
                     + "(transaction_no, user_id, user_name_snapshot, direction, points, point_category, status, "
                     + "source_type, source_title_snapshot, issuing_club_id, issuing_club_code_snapshot, "
                     + "issuing_club_name_snapshot, rule_version_id, occurred_at, business_year, business_month, "
                     + "idempotency_key, create_time, update_time, deleted) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE)")) {
            statement.setString(1, "TX-M5-3006");
            statement.setLong(2, 100L);
            statement.setString(3, "员工A");
            statement.setInt(4, 1);
            statement.setInt(5, 10);
            statement.setInt(6, 1);
            statement.setInt(7, 1);
            statement.setInt(8, 1);
            statement.setString(9, "历史流水");
            statement.setLong(10, club.getId());
            statement.setString(11, club.getCode());
            statement.setString(12, club.getName());
            statement.setLong(13, 1L);
            statement.setTimestamp(14, Timestamp.valueOf(LocalDateTime.of(2026, 6, 1, 10, 0)));
            statement.setInt(15, 2026);
            statement.setInt(16, 202606);
            statement.setString(17, "IDEMP-M5-3006");
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Long countClubRows(Long clubId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(1) FROM club_points_club WHERE id = ?")) {
            statement.setLong(1, clubId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
