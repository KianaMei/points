package cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.attachment.ClubAttachmentRefDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment.ClubAttachmentRefMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointContributionMapperTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 10, 9, 0);

    @Resource
    private ClubPointContributionMaterialMapper materialMapper;
    @Resource
    private ClubPointContributionItemMapper itemMapper;
    @Resource
    private ClubPointContributionReviewRecordMapper reviewRecordMapper;
    @Resource
    private ClubAttachmentRefMapper attachmentRefMapper;

    @Test
    void contributionMappersShouldPersistMaterialItemsAndReviewRecords() {
        ClubPointContributionMaterialDO material = buildMaterial();
        materialMapper.insert(material);

        ClubPointContributionMaterialDO savedMaterial = materialMapper.selectByRequestNo("M8-DIRECT-001");
        assertNotNull(savedMaterial);
        assertEquals(101L, savedMaterial.getClubId());
        assertEquals("Running Club", savedMaterial.getClubNameSnapshot());
        assertEquals(3, savedMaterial.getType());
        assertEquals("宣传材料", savedMaterial.getTitle());
        assertEquals("微信公众号宣传", savedMaterial.getDescription());
        assertEquals(2, savedMaterial.getStatus());
        assertEquals(6001L, savedMaterial.getRuleVersionId());
        assertEquals(8001L, savedMaterial.getSubmitterUserId());
        assertEquals(BASE_TIME, savedMaterial.getSubmitTime());
        assertEquals(9001L, savedMaterial.getReviewerUserId());
        assertEquals(BASE_TIME.plusHours(2), savedMaterial.getReviewTime());
        assertEquals("approved", savedMaterial.getReviewReason());
        assertTrue(savedMaterial.getLocked());
        assertTrue(savedMaterial.getDirectCreated());
        assertEquals("{\"title\":\"宣传材料\"}", savedMaterial.getSnapshotJson());

        ClubAttachmentRefDO attachment = buildAttachment(savedMaterial.getId());
        attachmentRefMapper.insert(attachment);
        List<ClubAttachmentRefDO> attachments = attachmentRefMapper.selectListByBiz(
                ClubAttachmentConstants.BIZ_TYPE_CONTRIBUTION_MATERIAL, savedMaterial.getId(),
                ClubAttachmentConstants.STATUS_EFFECTIVE);
        assertEquals(1, attachments.size());
        assertEquals(3001L, attachments.get(0).getFileId());
        assertEquals(savedMaterial.getId(), attachments.get(0).getBizId());

        ClubPointContributionItemDO firstItem = buildItem(savedMaterial.getId(), 8101L,
                "CONTRIBUTION_ITEM:M8:1", "MONTHLY_DUTY:101:8101:202607");
        ClubPointContributionItemDO secondItem = buildItem(savedMaterial.getId(), 8102L,
                "CONTRIBUTION_ITEM:M8:2", null);
        itemMapper.insert(firstItem);
        itemMapper.insert(secondItem);

        List<ClubPointContributionItemDO> items = itemMapper.selectListByMaterialId(savedMaterial.getId());
        assertEquals(2, items.size());
        ClubPointContributionItemDO savedFirstItem = itemMapper.selectByIdempotencyKey("CONTRIBUTION_ITEM:M8:1");
        assertNotNull(savedFirstItem);
        assertEquals(savedMaterial.getId(), savedFirstItem.getMaterialId());
        assertEquals(101L, savedFirstItem.getClubId());
        assertEquals(8101L, savedFirstItem.getUserId());
        assertEquals("User 8101", savedFirstItem.getUserNameSnapshot());
        assertEquals("Ops", savedFirstItem.getDeptNameSnapshot());
        assertEquals(10, savedFirstItem.getPointCategory());
        assertEquals(6101L, savedFirstItem.getRuleItemId());
        assertEquals("PUBLICITY_ARTICLE", savedFirstItem.getRuleItemCode());
        assertEquals(1, savedFirstItem.getDirection());
        assertEquals(8, savedFirstItem.getPoints());
        assertEquals("公众号推文", savedFirstItem.getReason());
        assertEquals("7 月宣传材料", savedFirstItem.getMaterialSummary());
        assertEquals(202607, savedFirstItem.getDutyMonth());
        assertEquals(8201L, savedFirstItem.getRecommendedUserId());
        assertEquals(2, savedFirstItem.getAwardLevel());
        assertEquals("offline-approved", savedFirstItem.getApprovalResultSnapshot());
        assertEquals(7001L, savedFirstItem.getTransactionId());
        assertEquals("MONTHLY_DUTY:101:8101:202607", savedFirstItem.getEffectiveUniqueKey());
        assertEquals(savedFirstItem.getId(),
                itemMapper.selectByEffectiveUniqueKey("MONTHLY_DUTY:101:8101:202607").getId());

        ClubPointContributionReviewRecordDO reviewRecord = buildReviewRecord(savedMaterial.getId());
        reviewRecordMapper.insert(reviewRecord);

        List<ClubPointContributionReviewRecordDO> reviewRecords =
                reviewRecordMapper.selectListByMaterialId(savedMaterial.getId());
        assertEquals(1, reviewRecords.size());
        ClubPointContributionReviewRecordDO savedReviewRecord = reviewRecords.get(0);
        assertEquals(savedMaterial.getId(), savedReviewRecord.getMaterialId());
        assertEquals(9001L, savedReviewRecord.getReviewerUserId());
        assertEquals(1, savedReviewRecord.getResult());
        assertEquals("审核通过", savedReviewRecord.getReason());
        assertEquals(BASE_TIME.plusHours(2), savedReviewRecord.getReviewTime());
        assertEquals("{\"status\":2,\"items\":2}", savedReviewRecord.getMaterialSnapshotJson());
        assertEquals(2, savedReviewRecord.getCreatedTransactionCount());
        assertEquals(9101L, savedReviewRecord.getAuditLogId());
    }

    private static ClubPointContributionMaterialDO buildMaterial() {
        return new ClubPointContributionMaterialDO()
                .setClubId(101L)
                .setClubNameSnapshot("Running Club")
                .setType(3)
                .setTitle("宣传材料")
                .setDescription("微信公众号宣传")
                .setStatus(2)
                .setRuleVersionId(6001L)
                .setSubmitterUserId(8001L)
                .setSubmitTime(BASE_TIME)
                .setReviewerUserId(9001L)
                .setReviewTime(BASE_TIME.plusHours(2))
                .setReviewReason("approved")
                .setLocked(true)
                .setDirectCreated(true)
                .setRequestNo("M8-DIRECT-001")
                .setSnapshotJson("{\"title\":\"宣传材料\"}");
    }

    private static ClubAttachmentRefDO buildAttachment(Long materialId) {
        return new ClubAttachmentRefDO()
                .setBizType(ClubAttachmentConstants.BIZ_TYPE_CONTRIBUTION_MATERIAL)
                .setBizId(materialId)
                .setAttachmentType(ClubAttachmentConstants.ATTACHMENT_TYPE_FILE)
                .setFileId(3001L)
                .setName("proof.pdf")
                .setStatus(ClubAttachmentConstants.STATUS_EFFECTIVE)
                .setLocked(false)
                .setUploadedBy(8001L)
                .setUploadedTime(BASE_TIME.plusMinutes(10))
                .setAdminAppend(false);
    }

    private static ClubPointContributionItemDO buildItem(Long materialId, Long userId,
                                                         String idempotencyKey, String effectiveUniqueKey) {
        return new ClubPointContributionItemDO()
                .setMaterialId(materialId)
                .setClubId(101L)
                .setUserId(userId)
                .setUserNameSnapshot("User " + userId)
                .setDeptNameSnapshot("Ops")
                .setPointCategory(10)
                .setRuleItemId(6101L)
                .setRuleItemCode("PUBLICITY_ARTICLE")
                .setDirection(1)
                .setPoints(8)
                .setReason("公众号推文")
                .setMaterialSummary("7 月宣传材料")
                .setDutyMonth(202607)
                .setRecommendedUserId(8201L)
                .setAwardLevel(2)
                .setApprovalResultSnapshot("offline-approved")
                .setTransactionId(7001L)
                .setIdempotencyKey(idempotencyKey)
                .setEffectiveUniqueKey(effectiveUniqueKey);
    }

    private static ClubPointContributionReviewRecordDO buildReviewRecord(Long materialId) {
        return new ClubPointContributionReviewRecordDO()
                .setMaterialId(materialId)
                .setReviewerUserId(9001L)
                .setResult(1)
                .setReason("审核通过")
                .setReviewTime(BASE_TIME.plusHours(2))
                .setMaterialSnapshotJson("{\"status\":2,\"items\":2}")
                .setCreatedTransactionCount(2)
                .setAuditLogId(9101L);
    }

}
