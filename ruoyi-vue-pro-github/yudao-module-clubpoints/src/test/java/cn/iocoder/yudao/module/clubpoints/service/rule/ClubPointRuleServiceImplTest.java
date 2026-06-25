package cn.iocoder.yudao.module.clubpoints.service.rule;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRulePublishRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRulePublishRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleVersionSaveReqBO;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.RULE_DISABLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.RULE_PUBLISH;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_ITEM_CODE_DUPLICATED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_VERSION_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_VERSION_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({ClubPointRuleServiceImpl.class, ClubAuditServiceImpl.class})
class ClubPointRuleServiceImplTest extends BaseDbUnitTest {

    private static final int STATUS_DRAFT = ClubPointRuleVersionStatusEnum.DRAFT.getStatus();
    private static final int STATUS_PUBLISHED = ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus();
    private static final int STATUS_DISABLED = ClubPointRuleVersionStatusEnum.DISABLED.getStatus();
    private static final int ITEM_STATUS_ENABLED = 1;

    @Resource
    private ClubPointRuleService clubPointRuleService;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;
    @Resource
    private ClubPointRulePublishRecordMapper rulePublishRecordMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;

    @Test
    void createDraftVersionShouldPersistDraftVersion() {
        Long id = clubPointRuleService.createDraftVersion(buildVersionSaveReq("V2026.02"));

        ClubPointRuleVersionDO version = ruleVersionMapper.selectById(id);
        assertEquals("V2026.02", version.getVersionNo());
        assertEquals("规则 V2026.02", version.getName());
        assertEquals(STATUS_DRAFT, version.getStatus());
        assertEquals(LocalDateTime.of(2026, 2, 1, 0, 0), version.getEffectiveTime());
        assertEquals("规则摘要", version.getSummary());
        assertEquals("规则正文", version.getContent());
        assertEquals("{\"files\":[1]}", version.getAttachmentSnapshotJson());
        assertEquals("备注", version.getRemark());
    }

    @Test
    void copyVersionShouldCreateDraftAndCopyItems() {
        ClubPointRuleVersionDO source = insertVersion("V2026.03", STATUS_PUBLISHED, LocalDateTime.now().minusDays(10));
        ruleItemMapper.insert(buildRuleItem(source.getId(), "ACTIVITY_SMALL_BASE", 5, 5, 5));

        Long copiedId = clubPointRuleService.copyVersion(source.getId(), buildVersionSaveReq("V2026.04"));

        ClubPointRuleVersionDO copied = ruleVersionMapper.selectById(copiedId);
        assertEquals(STATUS_DRAFT, copied.getStatus());
        ClubPointRuleItemDO copiedItem = ruleItemMapper.selectByRuleVersionIdAndItemCode(copiedId, "ACTIVITY_SMALL_BASE");
        assertNotNull(copiedItem);
        assertEquals(5, copiedItem.getDefaultPoints());
    }

    @Test
    void createRuleItemShouldRejectDuplicatedCodeInSameVersion() {
        ClubPointRuleVersionDO draft = insertVersion("V2026.05", STATUS_DRAFT, LocalDateTime.now().plusDays(1));
        ruleItemMapper.insert(buildRuleItem(draft.getId(), "ACTIVITY_SMALL_BASE", 5, 5, 5));

        assertServiceException(() -> clubPointRuleService.createRuleItem(
                buildRuleItemSaveReq(null, draft.getId(), "ACTIVITY_SMALL_BASE", 8, 8, 8)),
                CLUB_RULE_ITEM_CODE_DUPLICATED);
    }

    @Test
    void updateDraftRuleItemShouldRejectPublishedVersion() {
        ClubPointRuleVersionDO published = insertVersion("V2026.06", STATUS_PUBLISHED, LocalDateTime.now().minusDays(1));
        ClubPointRuleItemDO item = buildRuleItem(published.getId(), "ACTIVITY_SMALL_BASE", 5, 5, 5);
        ruleItemMapper.insert(item);

        assertServiceException(() -> clubPointRuleService.updateDraftRuleItem(
                buildRuleItemSaveReq(item.getId(), published.getId(), "ACTIVITY_SMALL_BASE", 8, 8, 8)),
                CLUB_RULE_VERSION_STATUS_INVALID);
    }

    @Test
    void updateDraftRuleItemShouldPersistRuleItemChanges() {
        ClubPointRuleVersionDO draft = insertVersion("V2026.07", STATUS_DRAFT, LocalDateTime.now().plusDays(1));
        ClubPointRuleItemDO item = buildRuleItem(draft.getId(), "ACTIVITY_SMALL_BASE", 5, 5, 5);
        ruleItemMapper.insert(item);

        clubPointRuleService.updateDraftRuleItem(
                buildRuleItemSaveReq(item.getId(), draft.getId(), "ACTIVITY_SMALL_BASE", 8, 10, 9));

        ClubPointRuleItemDO updated = ruleItemMapper.selectById(item.getId());
        assertEquals(8, updated.getMinPoints());
        assertEquals(10, updated.getMaxPoints());
        assertEquals(9, updated.getDefaultPoints());
        assertEquals("规则项-ACTIVITY_SMALL_BASE", updated.getItemName());
    }

    @Test
    void publishVersionShouldPublishDraftDisableOldPublishedAndWriteAudit() {
        ClubPointRuleVersionDO oldPublished = insertVersion("V2026.08", STATUS_PUBLISHED, LocalDateTime.now().minusDays(10));
        ClubPointRuleVersionDO draft = insertVersion("V2026.09", STATUS_DRAFT, LocalDateTime.now().minusDays(1));
        ruleItemMapper.insert(buildRuleItem(draft.getId(), "ACTIVITY_SMALL_BASE", 5, 5, 5));

        clubPointRuleService.publishVersion(draft.getId(), buildOperationReq("发布新规则"));

        ClubPointRuleVersionDO published = ruleVersionMapper.selectById(draft.getId());
        assertEquals(STATUS_PUBLISHED, published.getStatus());
        assertNotNull(published.getPublishedTime());
        ClubPointRuleVersionDO disabledOld = ruleVersionMapper.selectById(oldPublished.getId());
        assertEquals(STATUS_DISABLED, disabledOld.getStatus());
        assertNotNull(disabledOld.getDisabledTime());
        List<ClubPointRulePublishRecordDO> replaceRecords = rulePublishRecordMapper.selectListByRuleVersionId(oldPublished.getId());
        assertEquals(1, replaceRecords.size());
        assertEquals(4, replaceRecords.get(0).getAction());
        assertEquals(100L, replaceRecords.get(0).getOperatorUserId());
        assertEquals("发布新规则", replaceRecords.get(0).getReason());

        List<ClubPointRulePublishRecordDO> records = rulePublishRecordMapper.selectListByRuleVersionId(draft.getId());
        assertEquals(1, records.size());
        assertEquals(1, records.get(0).getAction());
        assertNotNull(records.get(0).getAuditLogId());
        ClubAuditLogDO auditLog = auditLogMapper.selectById(records.get(0).getAuditLogId());
        assertEquals(RULE_PUBLISH, auditLog.getActionType());
        assertEquals("RULE_VERSION", auditLog.getBizType());
        assertEquals(draft.getId(), auditLog.getBizId());
        assertEquals("发布新规则", auditLog.getReason());
    }

    @Test
    void disableVersionShouldDisablePublishedVersionAndWriteAudit() {
        ClubPointRuleVersionDO published = insertVersion("V2026.10", STATUS_PUBLISHED, LocalDateTime.now().minusDays(1));

        clubPointRuleService.disableVersion(published.getId(), buildOperationReq("停用旧规则"));

        ClubPointRuleVersionDO disabled = ruleVersionMapper.selectById(published.getId());
        assertEquals(STATUS_DISABLED, disabled.getStatus());
        assertNotNull(disabled.getDisabledTime());
        List<ClubPointRulePublishRecordDO> records = rulePublishRecordMapper.selectListByRuleVersionId(published.getId());
        assertEquals(1, records.size());
        assertEquals(3, records.get(0).getAction());
        ClubAuditLogDO auditLog = auditLogMapper.selectById(records.get(0).getAuditLogId());
        assertEquals(RULE_DISABLE, auditLog.getActionType());
        assertEquals("停用旧规则", auditLog.getReason());
    }

    @Test
    void getCurrentRuleVersionShouldReadPublishedVersionOnly() {
        insertVersion("V2026.11", STATUS_DRAFT, LocalDateTime.now().minusDays(2));
        ClubPointRuleVersionDO published = insertVersion("V2026.12", STATUS_PUBLISHED, LocalDateTime.now().minusDays(1));

        ClubPointRuleVersionDO current = clubPointRuleService.getCurrentRuleVersion();

        assertEquals(published.getId(), current.getId());
    }

    @Test
    void getCurrentRuleVersionShouldFailWithoutPublishedVersion() {
        insertVersion("V2026.13", STATUS_DRAFT, LocalDateTime.now().minusDays(2));

        assertServiceException(() -> clubPointRuleService.getCurrentRuleVersion(), CLUB_RULE_VERSION_NOT_EXISTS);
    }

    @Test
    void getRuleItemByCodeShouldReadFromCurrentPublishedVersion() {
        ClubPointRuleVersionDO draft = insertVersion("V2026.14", STATUS_DRAFT, LocalDateTime.now().minusDays(2));
        ruleItemMapper.insert(buildRuleItem(draft.getId(), "ACTIVITY_SMALL_BASE", 99, 99, 99));
        ClubPointRuleVersionDO published = insertVersion("V2026.15", STATUS_PUBLISHED, LocalDateTime.now().minusDays(1));
        ruleItemMapper.insert(buildRuleItem(published.getId(), "ACTIVITY_SMALL_BASE", 5, 5, 5));

        ClubPointRuleItemDO item = clubPointRuleService.getRuleItemByCode("ACTIVITY_SMALL_BASE");

        assertEquals(published.getId(), item.getRuleVersionId());
        assertEquals(5, item.getDefaultPoints());
    }

    @Test
    void getRuleItemByCodeShouldFailWhenCurrentVersionDoesNotContainItem() {
        insertVersion("V2026.16", STATUS_PUBLISHED, LocalDateTime.now().minusDays(1));

        assertServiceException(() -> clubPointRuleService.getRuleItemByCode("ACTIVITY_SMALL_BASE"), CLUB_RULE_ITEM_NOT_EXISTS);
    }

    private static ClubPointRuleVersionSaveReqBO buildVersionSaveReq(String versionNo) {
        return new ClubPointRuleVersionSaveReqBO()
                .setVersionNo(versionNo)
                .setName("规则 " + versionNo)
                .setPublicityTime(LocalDateTime.of(2026, 1, 20, 0, 0))
                .setEffectiveTime(LocalDateTime.of(2026, 2, 1, 0, 0))
                .setSummary("规则摘要")
                .setContent("规则正文")
                .setAttachmentSnapshotJson("{\"files\":[1]}")
                .setRemark("备注");
    }

    private static ClubPointRuleItemSaveReqBO buildRuleItemSaveReq(Long id, Long ruleVersionId, String itemCode,
                                                                   Integer minPoints, Integer maxPoints, Integer defaultPoints) {
        return new ClubPointRuleItemSaveReqBO()
                .setId(id)
                .setRuleVersionId(ruleVersionId)
                .setItemCode(itemCode)
                .setItemName("规则项-" + itemCode)
                .setItemType(ClubPointRuleItemTypeEnum.POINTS.getType())
                .setCategory(10)
                .setMinPoints(minPoints)
                .setMaxPoints(maxPoints)
                .setDefaultPoints(defaultPoints)
                .setStatus(ITEM_STATUS_ENABLED)
                .setSort(1)
                .setRemark("规则项备注");
    }

    private static ClubPointRuleOperationReqBO buildOperationReq(String reason) {
        return new ClubPointRuleOperationReqBO()
                .setOperatorUserId(100L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason(reason);
    }

    private ClubPointRuleVersionDO insertVersion(String versionNo, Integer status, LocalDateTime effectiveTime) {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo(versionNo)
                .setName("规则 " + versionNo)
                .setStatus(status)
                .setPublicityTime(effectiveTime.minusDays(1))
                .setEffectiveTime(effectiveTime)
                .setPublishedTime(STATUS_PUBLISHED == status ? effectiveTime : null)
                .setSummary("规则摘要")
                .setContent("规则正文")
                .setAttachmentSnapshotJson("{\"files\":[1]}")
                .setRemark("备注");
        ruleVersionMapper.insert(version);
        return version;
    }

    private static ClubPointRuleItemDO buildRuleItem(Long ruleVersionId, String itemCode,
                                                     Integer minPoints, Integer maxPoints, Integer defaultPoints) {
        return new ClubPointRuleItemDO()
                .setRuleVersionId(ruleVersionId)
                .setItemCode(itemCode)
                .setItemName("规则项-" + itemCode)
                .setItemType(ClubPointRuleItemTypeEnum.POINTS.getType())
                .setCategory(10)
                .setMinPoints(minPoints)
                .setMaxPoints(maxPoints)
                .setDefaultPoints(defaultPoints)
                .setStatus(ITEM_STATUS_ENABLED)
                .setSort(1)
                .setRemark("规则项备注");
    }

}
