package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityPointConfigVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.attachment.ClubAttachmentRefDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityPointConfigVersionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityReviewRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment.ClubAttachmentRefMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivitySaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivitySubmitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentService;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.ATTACHMENT_TYPE_URL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.BIZ_TYPE_ACTIVITY;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ACTIVITY_CANCEL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ACTIVITY_KEY_FIELD_UPDATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum.ACTIVITY_FULL_EXTRA;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum.ACTIVITY_MEDIUM_BASE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointActivityServiceImpl.class, ClubScopeServiceImpl.class, ClubAuditServiceImpl.class,
        ClubAttachmentServiceImpl.class, ClubPointRuleServiceImpl.class})
class ClubPointActivityServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 1, 9, 0);

    @Resource
    private ClubPointActivityService clubPointActivityService;
    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityReviewRecordMapper reviewRecordMapper;
    @Resource
    private ClubPointActivityPointConfigVersionMapper configVersionMapper;
    @Resource
    private ClubAttachmentService clubAttachmentService;
    @Resource
    private ClubAttachmentRefMapper attachmentRefMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private DataSource dataSource;

    @MockBean
    private FileService fileService;

    @Test
    void leaderCreateDraftShouldPersistActivitySnapshotsAndRejectUnmanagedClub() {
        ClubPointClubDO managedClub = insertClub("CLUB-M6-3001", "Managed Activity Club");
        ClubPointClubDO unmanagedClub = insertClub("CLUB-M6-3002", "Unmanaged Activity Club");
        insertLeader(managedClub.getId(), 2001L);

        Long activityId = clubPointActivityService.createDraft(buildSaveReq(null, managedClub.getId(), 2001L));

        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        assertEquals(managedClub.getId(), activity.getClubId());
        assertEquals(managedClub.getCode(), activity.getClubCodeSnapshot());
        assertEquals(managedClub.getName(), activity.getClubNameSnapshot());
        assertEquals("Weekly Run", activity.getTitle());
        assertEquals("Central Park", activity.getLocation());
        assertEquals(ClubPointActivityStatusEnum.DRAFT.getStatus(), activity.getStatus());
        assertEquals(2, activity.getLevel());
        assertEquals(BASE_TIME.plusDays(4), activity.getStartTime());
        assertEquals(BASE_TIME.plusDays(4).plusHours(2), activity.getEndTime());
        assertEquals(2001L, activity.getCreatorUserId());
        assertNull(activity.getCurrentConfigVersionId());
        assertTrue(activity.getSnapshotJson().contains("\"basePoints\":8"));
        assertTrue(activity.getSnapshotJson().contains("\"fullExtraPoints\":2"));

        assertServiceException(() -> clubPointActivityService.createDraft(
                buildSaveReq(null, unmanagedClub.getId(), 2001L)), CLUB_SCOPE_DENIED);
    }

    @Test
    void submitForReviewShouldMoveDraftToPendingReview() {
        ClubPointClubDO club = insertClub("CLUB-M6-3003", "Submit Activity Club");
        insertLeader(club.getId(), 2003L);
        Long activityId = clubPointActivityService.createDraft(buildSaveReq(null, club.getId(), 2003L));

        clubPointActivityService.submitForReview(buildSubmitReq(activityId, 2003L));

        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        assertEquals(ClubPointActivityStatusEnum.PENDING_REVIEW.getStatus(), activity.getStatus());
        assertNotNull(activity.getSubmitTime());
    }

    @Test
    void approveReviewShouldPublishCreateConfigVersionLockAttachmentsAndReviewRecord() {
        ClubPointClubDO club = insertClub("CLUB-M6-3004", "Approve Activity Club");
        insertLeader(club.getId(), 2004L);
        seedActivityRules();
        Long activityId = clubPointActivityService.createDraft(buildSaveReq(null, club.getId(), 2004L));
        clubPointActivityService.submitForReview(buildSubmitReq(activityId, 2004L));
        Long attachmentId = insertActivityAttachment(activityId);

        clubPointActivityService.approveReview(buildReviewReq(activityId, true));

        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        assertEquals(ClubPointActivityStatusEnum.PUBLISHED.getStatus(), activity.getStatus());
        assertNotNull(activity.getPublishTime());
        assertNotNull(activity.getCurrentConfigVersionId());

        ClubPointActivityPointConfigVersionDO config = configVersionMapper.selectById(activity.getCurrentConfigVersionId());
        assertEquals(activityId, config.getActivityId());
        assertEquals(1, config.getVersionNo());
        assertEquals(2, config.getLevel());
        assertEquals(8, config.getBasePoints());
        assertEquals(2, config.getFullExtraPoints());
        assertTrue(config.getActive());
        assertTrue(config.getRuleSnapshotJson().contains(ACTIVITY_MEDIUM_BASE.getCode()));
        assertTrue(config.getRuleSnapshotJson().contains(ACTIVITY_FULL_EXTRA.getCode()));

        ClubPointActivityReviewRecordDO reviewRecord = reviewRecordMapper.selectOne(null);
        assertEquals(activityId, reviewRecord.getActivityId());
        assertEquals(900L, reviewRecord.getReviewerUserId());
        assertEquals(1, reviewRecord.getResult());
        assertTrue(reviewRecord.getActivitySnapshotJson().contains("\"status\":4"));

        ClubAttachmentRefDO attachment = attachmentRefMapper.selectById(attachmentId);
        assertTrue(attachment.getLocked());
        assertNotNull(attachment.getLockTime());
        assertEquals(0L, transactionMapper.selectCount());
    }

    @Test
    void rejectReviewShouldMarkRejectedAndKeepReviewRecord() {
        ClubPointClubDO club = insertClub("CLUB-M6-3005", "Reject Activity Club");
        insertLeader(club.getId(), 2005L);
        Long activityId = clubPointActivityService.createDraft(buildSaveReq(null, club.getId(), 2005L));
        clubPointActivityService.submitForReview(buildSubmitReq(activityId, 2005L));

        clubPointActivityService.rejectReview(buildReviewReq(activityId, true).setReason("time conflict"));

        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        assertEquals(ClubPointActivityStatusEnum.REJECTED.getStatus(), activity.getStatus());
        ClubPointActivityReviewRecordDO reviewRecord = reviewRecordMapper.selectOne(null);
        assertEquals(activityId, reviewRecord.getActivityId());
        assertEquals(2, reviewRecord.getResult());
        assertEquals("time conflict", reviewRecord.getReason());
        assertNull(activity.getCurrentConfigVersionId());
    }

    @Test
    void leaderUpdateUnpublishedActivityShouldPersistChangesWithinManagedClub() {
        ClubPointClubDO club = insertClub("CLUB-M6-3006", "Update Draft Activity Club");
        insertLeader(club.getId(), 2006L);
        Long activityId = clubPointActivityService.createDraft(buildSaveReq(null, club.getId(), 2006L));

        clubPointActivityService.updateActivity(buildSaveReq(activityId, club.getId(), 2006L)
                .setTitle("Updated Weekly Run")
                .setStartTime(BASE_TIME.plusDays(5))
                .setEndTime(BASE_TIME.plusDays(5).plusHours(2))
                .setRegistrationDeadline(BASE_TIME.plusDays(4))
                .setCancelDeadlineTime(BASE_TIME.plusDays(4).plusHours(12))
                .setCheckinStartTime(BASE_TIME.plusDays(5).minusMinutes(30))
                .setCheckinEndTime(BASE_TIME.plusDays(5).plusMinutes(30))
                .setCheckoutStartTime(BASE_TIME.plusDays(5).plusHours(1))
                .setCheckoutEndTime(BASE_TIME.plusDays(5).plusHours(3)));

        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        assertEquals("Updated Weekly Run", activity.getTitle());
        assertEquals(BASE_TIME.plusDays(5), activity.getStartTime());
        assertEquals(0L, auditLogMapper.selectCount());
    }

    @Test
    void updatePublishedKeyFieldsShouldWriteAuditCreateNewConfigVersionAndRollbackWhenAuditFails() {
        ClubPointClubDO club = insertClub("CLUB-M6-3007", "Published Update Activity Club");
        insertLeader(club.getId(), 2007L);
        seedActivityRules();
        Long activityId = publishActivity(club.getId(), 2007L);
        Long originalConfigId = activityMapper.selectById(activityId).getCurrentConfigVersionId();

        clubPointActivityService.updateActivity(buildSaveReq(activityId, club.getId(), 900L)
                .setOperatorGlobalScope(true)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("club_points_admin")
                .setReason("adjust published key fields")
                .setStartTime(BASE_TIME.plusDays(6))
                .setEndTime(BASE_TIME.plusDays(6).plusHours(2))
                .setRegistrationDeadline(BASE_TIME.plusDays(5))
                .setCancelDeadlineTime(BASE_TIME.plusDays(5).plusHours(12))
                .setCheckinStartTime(BASE_TIME.plusDays(6).minusMinutes(20))
                .setCheckinEndTime(BASE_TIME.plusDays(6).plusMinutes(40))
                .setCheckoutStartTime(BASE_TIME.plusDays(6).plusHours(1))
                .setCheckoutEndTime(BASE_TIME.plusDays(6).plusHours(3))
                .setBasePoints(9)
                .setFullExtraPoints(3));

        ClubPointActivityDO updated = activityMapper.selectById(activityId);
        assertEquals(BASE_TIME.plusDays(6), updated.getStartTime());
        assertNotEquals(originalConfigId, updated.getCurrentConfigVersionId());
        assertFalse(configVersionMapper.selectById(originalConfigId).getActive());
        ClubPointActivityPointConfigVersionDO latestConfig = configVersionMapper.selectById(updated.getCurrentConfigVersionId());
        assertEquals(2, latestConfig.getVersionNo());
        assertEquals(9, latestConfig.getBasePoints());
        assertEquals(3, latestConfig.getFullExtraPoints());
        assertTrue(latestConfig.getActive());
        ClubAuditLogDO auditLog = selectLatestAuditLog();
        assertEquals(ACTIVITY_KEY_FIELD_UPDATE, auditLog.getActionType());
        assertEquals("ACTIVITY", auditLog.getBizType());
        assertEquals(activityId, auditLog.getBizId());
        assertTrue(auditLog.getBeforeJson().contains("\"basePoints\":8"));
        assertTrue(auditLog.getAfterJson().contains("\"basePoints\":9"));

        ClubPointActivitySaveReqBO badAuditReq = buildSaveReq(activityId, club.getId(), 900L)
                .setOperatorGlobalScope(true)
                .setOperatorNameSnapshot(null)
                .setReason("bad audit")
                .setStartTime(BASE_TIME.plusDays(7))
                .setEndTime(BASE_TIME.plusDays(7).plusHours(2))
                .setRegistrationDeadline(BASE_TIME.plusDays(6))
                .setCancelDeadlineTime(BASE_TIME.plusDays(6).plusHours(12))
                .setCheckinStartTime(BASE_TIME.plusDays(7).minusMinutes(20))
                .setCheckinEndTime(BASE_TIME.plusDays(7).plusMinutes(40))
                .setCheckoutStartTime(BASE_TIME.plusDays(7).plusHours(1))
                .setCheckoutEndTime(BASE_TIME.plusDays(7).plusHours(3));
        assertServiceException(() -> clubPointActivityService.updateActivity(badAuditReq), CLUB_AUDIT_WRITE_FAILED);

        ClubPointActivityDO afterRollback = activityMapper.selectById(activityId);
        assertEquals(BASE_TIME.plusDays(6), afterRollback.getStartTime());
        assertEquals(updated.getCurrentConfigVersionId(), afterRollback.getCurrentConfigVersionId());
        assertEquals(2L, configVersionMapper.selectCount());
    }

    @Test
    void cancelPublishedActivityShouldWriteAuditAndNotCreateTransactions() {
        ClubPointClubDO club = insertClub("CLUB-M6-3008", "Cancel Activity Club");
        insertLeader(club.getId(), 2008L);
        seedActivityRules();
        Long activityId = publishActivity(club.getId(), 2008L);

        clubPointActivityService.cancelActivity(buildCancelReq(activityId, 2008L));

        ClubPointActivityDO activity = activityMapper.selectById(activityId);
        assertEquals(ClubPointActivityStatusEnum.CANCELED.getStatus(), activity.getStatus());
        assertEquals("bad weather", activity.getCancelReason());
        assertNotNull(activity.getCancelTime());
        ClubAuditLogDO auditLog = selectLatestAuditLog();
        assertEquals(ACTIVITY_CANCEL, auditLog.getActionType());
        assertEquals(activityId, auditLog.getBizId());
        assertTrue(auditLog.getAfterJson().contains("\"status\":5"));
        assertEquals(0L, transactionMapper.selectCount());

        assertServiceException(() -> clubPointActivityService.submitForReview(buildSubmitReq(activityId, 2008L)),
                CLUB_ACTIVITY_STATUS_INVALID);
    }

    private Long publishActivity(Long clubId, Long leaderUserId) {
        Long activityId = clubPointActivityService.createDraft(buildSaveReq(null, clubId, leaderUserId));
        clubPointActivityService.submitForReview(buildSubmitReq(activityId, leaderUserId));
        clubPointActivityService.approveReview(buildReviewReq(activityId, true));
        return activityId;
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

    private void insertLeader(Long clubId, Long userId) {
        leaderMapper.insert(new ClubLeaderDO()
                .setClubId(clubId)
                .setUserId(userId)
                .setStatus(ClubPointLeaderStatusEnum.ACTIVE.getStatus())
                .setAssignedTime(BASE_TIME.minusDays(1))
                .setAssignedBy(900L)
                .setReason("assign")
                .setClubNameSnapshot("club")
                .setUserNameSnapshot("leader")
                .setActiveUniqueKey(clubId + ":" + userId));
    }

    private void seedActivityRules() {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("M6-RULE-001")
                .setName("M6 rules")
                .setStatus(ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus())
                .setEffectiveTime(BASE_TIME.minusDays(1))
                .setPublishedTime(BASE_TIME.minusDays(1));
        ruleVersionMapper.insert(version);
        insertRuleItem(version.getId(), ACTIVITY_MEDIUM_BASE.getCode(), "Medium base", 1, 10, 8, 2);
        insertRuleItem(version.getId(), ACTIVITY_FULL_EXTRA.getCode(), "Full extra", 1, 11, 2, 3);
    }

    private void insertRuleItem(Long versionId, String code, String name, Integer itemType, Integer category,
                                Integer defaultPoints, Integer sort) {
        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(versionId)
                .setItemCode(code)
                .setItemName(name)
                .setItemType(itemType)
                .setCategory(category)
                .setMinPoints(0)
                .setMaxPoints(20)
                .setDefaultPoints(defaultPoints)
                .setStatus(1)
                .setSort(sort));
    }

    private Long insertActivityAttachment(Long activityId) {
        return clubAttachmentService.bindAttachment(new ClubAttachmentBindReqBO()
                .setBizType(BIZ_TYPE_ACTIVITY)
                .setBizId(activityId)
                .setAttachmentType(ATTACHMENT_TYPE_URL)
                .setUrl("https://example.test/activity-proof")
                .setName("activity-proof")
                .setUploadedBy(2004L));
    }

    private static ClubPointActivitySaveReqBO buildSaveReq(Long id, Long clubId, Long operatorUserId) {
        return new ClubPointActivitySaveReqBO()
                .setId(id)
                .setClubId(clubId)
                .setTitle("Weekly Run")
                .setLocation("Central Park")
                .setDescription("Weekly running activity")
                .setCoverFileId(101L)
                .setLevel(2)
                .setStartTime(BASE_TIME.plusDays(4))
                .setEndTime(BASE_TIME.plusDays(4).plusHours(2))
                .setRegistrationDeadline(BASE_TIME.plusDays(3))
                .setCancelDeadlineTime(BASE_TIME.plusDays(3).plusHours(12))
                .setCheckinStartTime(BASE_TIME.plusDays(4).minusMinutes(30))
                .setCheckinEndTime(BASE_TIME.plusDays(4).plusMinutes(30))
                .setCheckoutMode(1)
                .setCheckoutStartTime(BASE_TIME.plusDays(4).plusHours(1))
                .setCheckoutEndTime(BASE_TIME.plusDays(4).plusHours(3))
                .setRuleVersionId(null)
                .setBasePoints(8)
                .setFullExtraPoints(2)
                .setRemark("activity remark")
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot("Leader")
                .setOperatorRoleSnapshot("club_points_leader")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("activity operation");
    }

    private static ClubPointActivitySubmitReqBO buildSubmitReq(Long activityId, Long operatorUserId) {
        return new ClubPointActivitySubmitReqBO()
                .setId(activityId)
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot("Leader")
                .setOperatorRoleSnapshot("club_points_leader")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("submit activity");
    }

    private static ClubPointActivityReviewReqBO buildReviewReq(Long activityId, boolean operatorGlobalScope) {
        return new ClubPointActivityReviewReqBO()
                .setId(activityId)
                .setOperatorGlobalScope(operatorGlobalScope)
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("review activity");
    }

    private static ClubPointActivityCancelReqBO buildCancelReq(Long activityId, Long operatorUserId) {
        return new ClubPointActivityCancelReqBO()
                .setId(activityId)
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot("Leader")
                .setOperatorRoleSnapshot("club_points_leader")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("bad weather");
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

}
