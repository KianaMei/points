package cn.iocoder.yudao.module.clubpoints.service.contribution;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.attachment.ClubAttachmentRefDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment.ClubAttachmentRefMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionMaterialMapper;
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
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionMaterialSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionSubmitReqBO;
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
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum.ACTIVE_CONTRIBUTION;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum.PUBLICITY_SUGGESTION;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum.SPECIAL_CONTRIBUTION;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum.INCREASE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED;
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
        ClubPointRuleServiceImpl.class, ClubAuditServiceImpl.class})
class ClubPointContributionServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 15, 9, 0);

    @Resource
    private ClubPointContributionService contributionService;
    @Resource
    private ClubPointContributionMaterialMapper materialMapper;
    @Resource
    private ClubPointContributionItemMapper itemMapper;
    @Resource
    private ClubAttachmentRefMapper attachmentRefMapper;
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

}
