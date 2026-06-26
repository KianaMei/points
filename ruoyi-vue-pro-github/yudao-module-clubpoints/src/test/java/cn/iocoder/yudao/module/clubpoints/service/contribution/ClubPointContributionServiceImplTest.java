package cn.iocoder.yudao.module.clubpoints.service.contribution;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.attachment.ClubAttachmentRefDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment.ClubAttachmentRefMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionMaterialMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionReviewRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionDirectCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionMaterialSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionSubmitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.ATTACHMENT_TYPE_URL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.BIZ_TYPE_CONTRIBUTION_MATERIAL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.STATUS_EFFECTIVE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_DIRECT_CREATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_REVIEW;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum.ACTIVE_CONTRIBUTION;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum.PUBLICITY_SUGGESTION;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum.SPECIAL_CONTRIBUTION;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum.INCREASE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum.ADMIN_DIRECT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum.CONTRIBUTION_MATERIAL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum.VALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_REVIEW_DENIED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointContributionServiceImpl.class, ClubScopeServiceImpl.class, ClubAttachmentServiceImpl.class,
        ClubPointRuleServiceImpl.class, ClubAuditServiceImpl.class, ClubPointLedgerServiceImpl.class})
class ClubPointContributionServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 15, 9, 0);

    @Resource
    private ClubPointContributionService contributionService;
    @Resource
    private ClubPointContributionMaterialMapper materialMapper;
    @Resource
    private ClubPointContributionItemMapper itemMapper;
    @Resource
    private ClubPointContributionReviewRecordMapper reviewRecordMapper;
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
    private ClubPointAccountMapper accountMapper;

    @MockBean
    private FileService fileService;

    @Test
    void createDraftShouldPersistMaterialItemsAndBindAttachments() {
        ClubPointClubDO club = insertClub("CLUB-M8-3001", "Contribution Club");
        insertLeader(club.getId(), 7100L);
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-001");
        ClubPointRuleItemDO ruleItem = insertRuleItem(version.getId(), PUBLICITY_SUGGESTION, 2, 10, 5);

        Long materialId = contributionService.createDraft(buildSaveReq(null, club.getId(), version.getId(), 7100L,
                PUBLICITY_SUGGESTION)
                .setAttachments(Arrays.asList(buildUrlAttachment())));

        ClubPointContributionMaterialDO material = materialMapper.selectById(materialId);
        assertEquals(club.getId(), material.getClubId());
        assertEquals("Contribution Club", material.getClubNameSnapshot());
        assertEquals(PUBLICITY_SUGGESTION.getType(), material.getType());
        assertEquals("宣传材料", material.getTitle());
        assertEquals("微信公众号宣传", material.getDescription());
        assertEquals(ClubPointContributionMaterialStatusEnum.DRAFT.getStatus(), material.getStatus());
        assertEquals(version.getId(), material.getRuleVersionId());
        assertEquals(7100L, material.getSubmitterUserId());
        assertNull(material.getSubmitTime());
        assertFalse(material.getLocked());
        assertFalse(material.getDirectCreated());
        assertTrue(material.getSnapshotJson().contains("\"title\":\"宣传材料\""));

        List<ClubPointContributionItemDO> items = itemMapper.selectListByMaterialId(materialId);
        assertEquals(2, items.size());
        ClubPointContributionItemDO firstItem = items.get(0);
        assertEquals(club.getId(), firstItem.getClubId());
        assertEquals(7101L, firstItem.getUserId());
        assertEquals("User 7101", firstItem.getUserNameSnapshot());
        assertEquals("Ops", firstItem.getDeptNameSnapshot());
        assertEquals(ACTIVE_CONTRIBUTION.getCategory(), firstItem.getPointCategory());
        assertEquals(ruleItem.getId(), firstItem.getRuleItemId());
        assertEquals(PUBLICITY_SUGGESTION.getRuleItemCode(), firstItem.getRuleItemCode());
        assertEquals(INCREASE.getDirection(), firstItem.getDirection());
        assertEquals(6, firstItem.getPoints());
        assertEquals("公众号推文", firstItem.getReason());
        assertEquals("7 月宣传材料", firstItem.getMaterialSummary());
        assertEquals("CONTRIBUTION:" + materialId + ":" + firstItem.getId() + ":7101",
                firstItem.getIdempotencyKey());
        assertNull(firstItem.getEffectiveUniqueKey());
        assertNull(firstItem.getTransactionId());

        List<ClubAttachmentRefDO> attachments = attachmentRefMapper.selectListByBiz(
                BIZ_TYPE_CONTRIBUTION_MATERIAL, materialId, STATUS_EFFECTIVE);
        assertEquals(1, attachments.size());
        assertEquals(ATTACHMENT_TYPE_URL, attachments.get(0).getAttachmentType());
        assertEquals("https://example.test/contribution-proof", attachments.get(0).getUrl());
        assertEquals(7100L, attachments.get(0).getUploadedBy());
        assertFalse(attachments.get(0).getLocked());
        assertEquals(0L, transactionMapper.selectCount());
    }

    @Test
    void submitForReviewShouldRequireAttachmentAndMoveDraftToPendingReview() {
        ClubPointClubDO club = insertClub("CLUB-M8-3002", "Submit Contribution Club");
        insertLeader(club.getId(), 7200L);
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-002");
        insertRuleItem(version.getId(), PUBLICITY_SUGGESTION, 2, 10, 5);
        Long noAttachmentMaterialId = contributionService.createDraft(buildSaveReq(null, club.getId(),
                version.getId(), 7200L, PUBLICITY_SUGGESTION));

        assertServiceException(() -> contributionService.submitForReview(
                buildSubmitReq(noAttachmentMaterialId, 7200L)), CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED);
        ClubPointContributionMaterialDO draft = materialMapper.selectById(noAttachmentMaterialId);
        assertEquals(ClubPointContributionMaterialStatusEnum.DRAFT.getStatus(), draft.getStatus());
        assertNull(draft.getSubmitTime());

        Long materialId = contributionService.createDraft(buildSaveReq(null, club.getId(), version.getId(), 7200L,
                PUBLICITY_SUGGESTION)
                .setAttachments(Arrays.asList(buildUrlAttachment())));

        contributionService.submitForReview(buildSubmitReq(materialId, 7200L));

        ClubPointContributionMaterialDO submitted = materialMapper.selectById(materialId);
        assertEquals(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus(), submitted.getStatus());
        assertNotNull(submitted.getSubmitTime());
        assertTrue(submitted.getSnapshotJson().contains("\"status\":2"));
        assertEquals(2, itemMapper.selectListByMaterialId(materialId).size());
        assertEquals(0L, transactionMapper.selectCount());

        assertServiceException(() -> contributionService.updateDraft(
                buildSaveReq(materialId, club.getId(), version.getId(), 7200L, PUBLICITY_SUGGESTION)
                        .setTitle("提交后修改")),
                CLUB_CONTRIBUTION_STATUS_INVALID);
    }

    @Test
    void createDraftShouldRejectUnmanagedClub() {
        ClubPointClubDO managedClub = insertClub("CLUB-M8-3003", "Managed Contribution Club");
        ClubPointClubDO unmanagedClub = insertClub("CLUB-M8-3004", "Unmanaged Contribution Club");
        insertLeader(managedClub.getId(), 7300L);
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-003");
        insertRuleItem(version.getId(), PUBLICITY_SUGGESTION, 2, 10, 5);

        assertServiceException(() -> contributionService.createDraft(buildSaveReq(null, unmanagedClub.getId(),
                version.getId(), 7300L, PUBLICITY_SUGGESTION)), CLUB_SCOPE_DENIED);

        assertEquals(0L, materialMapper.selectCount());
        assertEquals(0L, itemMapper.selectCount());
    }

    @Test
    void createDraftShouldRejectOutOfRangePointsWithContributionError() {
        ClubPointClubDO club = insertClub("CLUB-M8-3005", "Range Contribution Club");
        insertLeader(club.getId(), 7400L);
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-004");
        insertRuleItem(version.getId(), SPECIAL_CONTRIBUTION, 10, 50, 30);

        assertServiceException(() -> contributionService.createDraft(buildSaveReq(null, club.getId(),
                version.getId(), 7400L, SPECIAL_CONTRIBUTION)
                .setItems(Arrays.asList(buildItem(7401L).setPoints(51)))),
                CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE);

        assertEquals(0L, materialMapper.selectCount());
        assertEquals(0L, itemMapper.selectCount());
    }

    @Test
    void createDraftShouldRejectMissingRuleItem() {
        ClubPointClubDO club = insertClub("CLUB-M8-3006", "Missing Rule Contribution Club");
        insertLeader(club.getId(), 7500L);
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-005");

        assertServiceException(() -> contributionService.createDraft(buildSaveReq(null, club.getId(),
                version.getId(), 7500L, PUBLICITY_SUGGESTION)), CLUB_RULE_ITEM_NOT_EXISTS);

        assertEquals(0L, materialMapper.selectCount());
        assertEquals(0L, itemMapper.selectCount());
    }

    @Test
    void listPendingReviewMaterialsShouldRequireGlobalScopeAndReturnPendingMaterialsOnly() {
        ClubPointClubDO club = insertClub("CLUB-M8-4001", "Pending Review Club");
        insertLeader(club.getId(), 8100L);
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-401");
        insertRuleItem(version.getId(), PUBLICITY_SUGGESTION, 2, 10, 5);
        Long pendingMaterialId = createSubmittedMaterial(club.getId(), version.getId(), 8100L);
        contributionService.createDraft(buildSaveReq(null, club.getId(), version.getId(), 8100L,
                PUBLICITY_SUGGESTION));

        assertServiceException(() -> contributionService.listPendingReviewMaterials(false),
                CLUB_CONTRIBUTION_REVIEW_DENIED);

        List<ClubPointContributionMaterialDO> pendingMaterials =
                contributionService.listPendingReviewMaterials(true);

        assertEquals(1, pendingMaterials.size());
        assertEquals(pendingMaterialId, pendingMaterials.get(0).getId());
        assertEquals(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus(),
                pendingMaterials.get(0).getStatus());
    }

    @Test
    void approveReviewShouldCreateTransactionsLockAttachmentsWriteAuditAndReviewRecord() {
        ClubPointClubDO club = insertClub("CLUB-M8-4002", "Approve Review Club");
        insertLeader(club.getId(), 8200L);
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-402");
        insertRuleItem(version.getId(), PUBLICITY_SUGGESTION, 2, 10, 5);
        Long materialId = createSubmittedMaterial(club.getId(), version.getId(), 8200L);

        contributionService.reviewMaterial(buildReviewReq(materialId, true));

        ClubPointContributionMaterialDO material = materialMapper.selectById(materialId);
        assertEquals(ClubPointContributionMaterialStatusEnum.APPROVED.getStatus(), material.getStatus());
        assertEquals(900L, material.getReviewerUserId());
        assertNotNull(material.getReviewTime());
        assertEquals("approve contribution", material.getReviewReason());
        assertTrue(material.getLocked());

        List<ClubPointContributionItemDO> items = itemMapper.selectListByMaterialId(materialId);
        assertEquals(2, items.size());
        for (ClubPointContributionItemDO item : items) {
            assertNotNull(item.getTransactionId());
            ClubPointTransactionDO transaction = transactionMapper.selectById(item.getTransactionId());
            assertEquals(item.getUserId(), transaction.getUserId());
            assertEquals(item.getUserNameSnapshot(), transaction.getUserNameSnapshot());
            assertEquals(INCREASE.getDirection(), transaction.getDirection());
            assertEquals(item.getPoints(), transaction.getPoints());
            assertEquals(ACTIVE_CONTRIBUTION.getCategory(), transaction.getPointCategory());
            assertEquals(VALID.getStatus(), transaction.getStatus());
            assertEquals(CONTRIBUTION_MATERIAL.getType(), transaction.getSourceType());
            assertEquals(materialId, transaction.getSourceId());
            assertEquals(item.getId(), transaction.getSourceItemId());
            assertEquals(material.getTitle(), transaction.getSourceTitleSnapshot());
            assertEquals(club.getId(), transaction.getIssuingClubId());
            assertEquals(club.getName(), transaction.getIssuingClubNameSnapshot());
            assertEquals(item.getRuleItemCode(), transaction.getRuleItemCodeSnapshot());
            assertEquals(item.getIdempotencyKey(), transaction.getIdempotencyKey());
            assertEquals(900L, transaction.getOperatorUserId());
            assertNotNull(transaction.getAuditLogId());
        }

        ClubPointAccountDO firstAccount = accountMapper.selectByUserId(7101L);
        assertEquals(6, firstAccount.getAvailablePoints());
        ClubPointAccountDO secondAccount = accountMapper.selectByUserId(7102L);
        assertEquals(8, secondAccount.getAvailablePoints());

        List<ClubAttachmentRefDO> attachments = attachmentRefMapper.selectListByBiz(
                BIZ_TYPE_CONTRIBUTION_MATERIAL, materialId, STATUS_EFFECTIVE);
        assertEquals(1, attachments.size());
        assertTrue(attachments.get(0).getLocked());
        assertNotNull(attachments.get(0).getLockTime());

        ClubPointContributionReviewRecordDO reviewRecord =
                reviewRecordMapper.selectListByMaterialId(materialId).get(0);
        assertEquals(1, reviewRecord.getResult());
        assertEquals(2, reviewRecord.getCreatedTransactionCount());
        assertEquals(900L, reviewRecord.getReviewerUserId());
        assertNotNull(reviewRecord.getAuditLogId());
        assertTrue(reviewRecord.getMaterialSnapshotJson().contains("\"status\":5"));
        ClubAuditLogDO auditLog = auditLogMapper.selectById(reviewRecord.getAuditLogId());
        assertEquals(CONTRIBUTION_REVIEW, auditLog.getActionType());
        assertEquals("CONTRIBUTION_MATERIAL", auditLog.getBizType());
        assertEquals(materialId, auditLog.getBizId());
        assertTrue(auditLog.getAfterJson().contains("\"result\":1"));

        assertServiceException(() -> contributionService.reviewMaterial(buildReviewReq(materialId, true)),
                CLUB_CONTRIBUTION_STATUS_INVALID);
        assertEquals(2L, transactionMapper.selectCount());
    }

    @Test
    void rejectReviewShouldWriteAuditAndReviewRecordWithoutCreatingTransactions() {
        ClubPointClubDO club = insertClub("CLUB-M8-4003", "Reject Review Club");
        insertLeader(club.getId(), 8300L);
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-403");
        insertRuleItem(version.getId(), PUBLICITY_SUGGESTION, 2, 10, 5);
        Long materialId = createSubmittedMaterial(club.getId(), version.getId(), 8300L);

        contributionService.reviewMaterial(buildReviewReq(materialId, false).setReason("proof insufficient"));

        ClubPointContributionMaterialDO material = materialMapper.selectById(materialId);
        assertEquals(ClubPointContributionMaterialStatusEnum.REJECTED.getStatus(), material.getStatus());
        assertEquals("proof insufficient", material.getReviewReason());
        assertFalse(material.getLocked());
        assertEquals(0L, transactionMapper.selectCount());

        ClubPointContributionReviewRecordDO reviewRecord =
                reviewRecordMapper.selectListByMaterialId(materialId).get(0);
        assertEquals(2, reviewRecord.getResult());
        assertEquals(0, reviewRecord.getCreatedTransactionCount());
        assertEquals("proof insufficient", reviewRecord.getReason());
        assertNotNull(reviewRecord.getAuditLogId());
        ClubAuditLogDO auditLog = auditLogMapper.selectById(reviewRecord.getAuditLogId());
        assertEquals(CONTRIBUTION_REVIEW, auditLog.getActionType());
        assertTrue(auditLog.getAfterJson().contains("\"result\":2"));
    }

    @Test
    void approveReviewShouldRollbackWhenAuditFails() {
        ClubPointClubDO club = insertClub("CLUB-M8-4004", "Rollback Review Club");
        insertLeader(club.getId(), 8400L);
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-404");
        insertRuleItem(version.getId(), PUBLICITY_SUGGESTION, 2, 10, 5);
        Long materialId = createSubmittedMaterial(club.getId(), version.getId(), 8400L);

        assertServiceException(() -> contributionService.reviewMaterial(
                buildReviewReq(materialId, true).setOperatorNameSnapshot(null)), CLUB_AUDIT_WRITE_FAILED);

        ClubPointContributionMaterialDO material = materialMapper.selectById(materialId);
        assertEquals(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus(), material.getStatus());
        assertNull(material.getReviewerUserId());
        assertNull(material.getReviewTime());
        assertEquals(0L, reviewRecordMapper.selectCount());
        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(0L, auditLogMapper.selectCount());
        assertFalse(attachmentRefMapper.selectListByBiz(BIZ_TYPE_CONTRIBUTION_MATERIAL,
                materialId, STATUS_EFFECTIVE).get(0).getLocked());
    }

    @Test
    void directCreateShouldCreateApprovedMaterialTransactionAttachmentAuditAndBeIdempotent() {
        ClubPointClubDO club = insertClub("CLUB-M8-5001", "Direct Create Club");
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-501");
        insertRuleItem(version.getId(), SPECIAL_CONTRIBUTION, 10, 50, 30);

        Long materialId = contributionService.directCreate(buildDirectCreateReq("REQ-M8-5001",
                club.getId(), version.getId(), SPECIAL_CONTRIBUTION));
        Long repeatedMaterialId = contributionService.directCreate(buildDirectCreateReq("REQ-M8-5001",
                club.getId(), version.getId(), SPECIAL_CONTRIBUTION));

        assertEquals(materialId, repeatedMaterialId);
        assertEquals(1L, materialMapper.selectCount());
        ClubPointContributionMaterialDO material = materialMapper.selectById(materialId);
        assertEquals(club.getId(), material.getClubId());
        assertEquals(ClubPointContributionMaterialStatusEnum.APPROVED.getStatus(), material.getStatus());
        assertEquals(version.getId(), material.getRuleVersionId());
        assertEquals(900L, material.getSubmitterUserId());
        assertEquals(900L, material.getReviewerUserId());
        assertEquals("REQ-M8-5001", material.getRequestNo());
        assertTrue(material.getDirectCreated());
        assertTrue(material.getLocked());
        assertNotNull(material.getSubmitTime());
        assertNotNull(material.getReviewTime());

        List<ClubPointContributionItemDO> items = itemMapper.selectListByMaterialId(materialId);
        assertEquals(1, items.size());
        ClubPointContributionItemDO item = items.get(0);
        assertEquals(9101L, item.getUserId());
        assertEquals("Direct User", item.getUserNameSnapshot());
        assertEquals(SPECIAL_CONTRIBUTION.getRuleItemCode(), item.getRuleItemCode());
        assertEquals(30, item.getPoints());
        assertEquals("DIRECT_CONTRIBUTION:REQ-M8-5001", item.getIdempotencyKey());
        assertNotNull(item.getTransactionId());

        ClubPointTransactionDO transaction = transactionMapper.selectById(item.getTransactionId());
        assertEquals(ADMIN_DIRECT.getType(), transaction.getSourceType());
        assertEquals(materialId, transaction.getSourceId());
        assertEquals(item.getId(), transaction.getSourceItemId());
        assertEquals("DIRECT_CONTRIBUTION:REQ-M8-5001", transaction.getIdempotencyKey());
        assertEquals(9101L, transaction.getUserId());
        assertEquals(30, transaction.getPoints());
        assertEquals(SPECIAL_CONTRIBUTION.getPointCategory(), transaction.getPointCategory());
        assertEquals(club.getId(), transaction.getIssuingClubId());
        assertEquals(club.getName(), transaction.getIssuingClubNameSnapshot());
        assertEquals(900L, transaction.getOperatorUserId());
        assertNotNull(transaction.getAuditLogId());

        ClubPointAccountDO account = accountMapper.selectByUserId(9101L);
        assertEquals(30, account.getAvailablePoints());

        List<ClubAttachmentRefDO> attachments = attachmentRefMapper.selectListByBiz(
                BIZ_TYPE_CONTRIBUTION_MATERIAL, materialId, STATUS_EFFECTIVE);
        assertEquals(1, attachments.size());
        assertEquals(900L, attachments.get(0).getUploadedBy());
        assertTrue(attachments.get(0).getLocked());

        ClubAuditLogDO auditLog = auditLogMapper.selectById(transaction.getAuditLogId());
        assertEquals(CONTRIBUTION_DIRECT_CREATE, auditLog.getActionType());
        assertEquals(BIZ_TYPE_CONTRIBUTION_MATERIAL, auditLog.getBizType());
        assertEquals(materialId, auditLog.getBizId());
        assertTrue(auditLog.getAfterJson().contains("\"requestNo\":\"REQ-M8-5001\""));
        assertEquals(1L, auditLogMapper.selectCount());
        assertEquals(0L, reviewRecordMapper.selectCount());
        assertEquals(1L, transactionMapper.selectCount());
    }

    @Test
    void directCreateShouldRequireGlobalScopeReasonAndAttachment() {
        ClubPointClubDO club = insertClub("CLUB-M8-5002", "Direct Validate Club");
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-502");
        insertRuleItem(version.getId(), PUBLICITY_SUGGESTION, 2, 10, 5);

        assertServiceException(() -> contributionService.directCreate(buildDirectCreateReq("REQ-M8-5002-A",
                club.getId(), version.getId(), PUBLICITY_SUGGESTION).setOperatorGlobalScope(false)), CLUB_SCOPE_DENIED);
        assertServiceException(() -> contributionService.directCreate(buildDirectCreateReq("REQ-M8-5002-B",
                club.getId(), version.getId(), PUBLICITY_SUGGESTION).setReason("")), CLUB_CONTRIBUTION_STATUS_INVALID);
        assertServiceException(() -> contributionService.directCreate(buildDirectCreateReq("REQ-M8-5002-C",
                club.getId(), version.getId(), PUBLICITY_SUGGESTION).setAttachments(null)),
                CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED);

        assertEquals(0L, materialMapper.selectCount());
        assertEquals(0L, itemMapper.selectCount());
        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(0L, auditLogMapper.selectCount());
    }

    @Test
    void directCreateShouldRejectOutOfRangePointsAndRollback() {
        ClubPointClubDO club = insertClub("CLUB-M8-5003", "Direct Range Club");
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-503");
        insertRuleItem(version.getId(), SPECIAL_CONTRIBUTION, 10, 50, 30);

        assertServiceException(() -> contributionService.directCreate(buildDirectCreateReq("REQ-M8-5003",
                club.getId(), version.getId(), SPECIAL_CONTRIBUTION).setPoints(51)),
                CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE);

        assertEquals(0L, materialMapper.selectCount());
        assertEquals(0L, itemMapper.selectCount());
        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(0L, auditLogMapper.selectCount());
    }

    @Test
    void directCreateShouldRollbackWhenAuditFails() {
        ClubPointClubDO club = insertClub("CLUB-M8-5004", "Direct Audit Rollback Club");
        ClubPointRuleVersionDO version = insertPublishedRuleVersion("M8-RULE-504");
        insertRuleItem(version.getId(), SPECIAL_CONTRIBUTION, 10, 50, 30);

        assertServiceException(() -> contributionService.directCreate(buildDirectCreateReq("REQ-M8-5004",
                club.getId(), version.getId(), SPECIAL_CONTRIBUTION).setOperatorNameSnapshot(null)),
                CLUB_AUDIT_WRITE_FAILED);

        assertEquals(0L, materialMapper.selectCount());
        assertEquals(0L, itemMapper.selectCount());
        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(0L, auditLogMapper.selectCount());
        assertEquals(0L, attachmentRefMapper.selectCount());
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

    private ClubPointRuleVersionDO insertPublishedRuleVersion(String versionNo) {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo(versionNo)
                .setName("规则 " + versionNo)
                .setStatus(ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus())
                .setEffectiveTime(BASE_TIME.minusDays(1))
                .setPublishedTime(BASE_TIME.minusDays(1));
        ruleVersionMapper.insert(version);
        return version;
    }

    private ClubPointRuleItemDO insertRuleItem(Long versionId, ClubPointContributionMaterialTypeEnum materialType,
                                               Integer minPoints, Integer maxPoints, Integer defaultPoints) {
        ClubPointRuleItemDO item = new ClubPointRuleItemDO()
                .setRuleVersionId(versionId)
                .setItemCode(materialType.getRuleItemCode())
                .setItemName("规则项-" + materialType.getRuleItemCode())
                .setItemType(ClubPointRuleItemTypeEnum.POINTS.getType())
                .setCategory(materialType.getPointCategory())
                .setMinPoints(minPoints)
                .setMaxPoints(maxPoints)
                .setDefaultPoints(defaultPoints)
                .setStatus(1)
                .setSort(1);
        ruleItemMapper.insert(item);
        return item;
    }

    private Long createSubmittedMaterial(Long clubId, Long ruleVersionId, Long operatorUserId) {
        Long materialId = contributionService.createDraft(buildSaveReq(null, clubId, ruleVersionId,
                operatorUserId, PUBLICITY_SUGGESTION)
                .setAttachments(Arrays.asList(buildUrlAttachment())));
        contributionService.submitForReview(buildSubmitReq(materialId, operatorUserId));
        return materialId;
    }

    private static ClubPointContributionMaterialSaveReqBO buildSaveReq(Long id, Long clubId, Long ruleVersionId,
                                                                       Long operatorUserId,
                                                                       ClubPointContributionMaterialTypeEnum type) {
        return new ClubPointContributionMaterialSaveReqBO()
                .setId(id)
                .setClubId(clubId)
                .setType(type.getType())
                .setTitle("宣传材料")
                .setDescription("微信公众号宣传")
                .setRuleVersionId(ruleVersionId)
                .setItems(Arrays.asList(buildItem(7101L), buildItem(7102L).setPoints(8)))
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot("Leader")
                .setOperatorRoleSnapshot("club_points_leader")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("submit contribution");
    }

    private static ClubPointContributionItemSaveReqBO buildItem(Long userId) {
        return new ClubPointContributionItemSaveReqBO()
                .setUserId(userId)
                .setUserNameSnapshot("User " + userId)
                .setDeptNameSnapshot("Ops")
                .setPoints(6)
                .setReason("公众号推文")
                .setMaterialSummary("7 月宣传材料");
    }

    private static ClubAttachmentBindReqBO buildUrlAttachment() {
        return new ClubAttachmentBindReqBO()
                .setAttachmentType(ATTACHMENT_TYPE_URL)
                .setUrl("https://example.test/contribution-proof")
                .setName("contribution-proof")
                .setRemark("材料附件");
    }

    private static ClubPointContributionSubmitReqBO buildSubmitReq(Long materialId, Long operatorUserId) {
        return new ClubPointContributionSubmitReqBO()
                .setId(materialId)
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot("Leader")
                .setOperatorRoleSnapshot("club_points_leader")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("submit contribution");
    }

    private static ClubPointContributionReviewReqBO buildReviewReq(Long materialId, boolean approved) {
        return new ClubPointContributionReviewReqBO()
                .setId(materialId)
                .setResult(approved ? 1 : 2)
                .setReason(approved ? "approve contribution" : "reject contribution")
                .setOperatorGlobalScope(true)
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

    private static ClubPointContributionDirectCreateReqBO buildDirectCreateReq(String requestNo, Long clubId,
                                                                               Long ruleVersionId,
                                                                               ClubPointContributionMaterialTypeEnum type) {
        return new ClubPointContributionDirectCreateReqBO()
                .setRequestNo(requestNo)
                .setClubId(clubId)
                .setType(type.getType())
                .setUserId(9101L)
                .setUserNameSnapshot("Direct User")
                .setDeptNameSnapshot("Ops")
                .setPoints(30)
                .setRuleVersionId(ruleVersionId)
                .setReason("direct create contribution")
                .setAttachments(Arrays.asList(buildUrlAttachment()))
                .setOperatorGlobalScope(true)
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

}
