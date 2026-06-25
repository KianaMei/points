package cn.iocoder.yudao.module.clubpoints.dal.mysql.rule;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRulePublishRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClubPointRuleMapperTest extends BaseDbUnitTest {

    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;
    @Resource
    private ClubPointRulePublishRecordMapper rulePublishRecordMapper;

    @Test
    void ruleMappersShouldPersistAndQueryRuleVersionItemAndPublishRecord() {
        ClubPointRuleVersionDO version = buildRuleVersion();
        ruleVersionMapper.insert(version);

        ClubPointRuleVersionDO savedVersion = ruleVersionMapper.selectByVersionNo("RULE-2026");
        assertNotNull(savedVersion);
        assertEquals("积分制度 2026", savedVersion.getName());
        assertEquals(2, savedVersion.getStatus());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), savedVersion.getEffectiveTime());
        assertEquals(LocalDateTime.of(2025, 12, 20, 9, 0), savedVersion.getPublicityTime());
        assertEquals(LocalDateTime.of(2025, 12, 25, 10, 0), savedVersion.getPublishedTime());
        assertEquals(LocalDateTime.of(2026, 12, 31, 23, 59), savedVersion.getDisabledTime());
        assertEquals("默认制度", savedVersion.getSummary());
        assertEquals("制度正文", savedVersion.getContent());
        assertEquals("{\"files\":[1]}", savedVersion.getAttachmentSnapshotJson());
        assertEquals("备注", savedVersion.getRemark());

        ClubPointRuleItemDO item = buildRuleItem(savedVersion.getId());
        ruleItemMapper.insert(item);

        ClubPointRuleItemDO savedItem = ruleItemMapper.selectByRuleVersionIdAndItemCode(savedVersion.getId(), "ACTIVITY_ATTEND");
        assertNotNull(savedItem);
        assertEquals("活动签到积分", savedItem.getItemName());
        assertEquals(1, savedItem.getItemType());
        assertEquals(10, savedItem.getCategory());
        assertEquals(10, savedItem.getMinPoints());
        assertEquals(10, savedItem.getMaxPoints());
        assertEquals(10, savedItem.getDefaultPoints());
        assertEquals(1, savedItem.getIntValue());
        assertEquals(new BigDecimal("1.250000"), savedItem.getDecimalValue());
        assertEquals("文本参数", savedItem.getTextValue());
        assertEquals("{\"fixed\":true}", savedItem.getJsonValue());
        assertEquals(1, savedItem.getStatus());
        assertEquals(100, savedItem.getSort());
        assertEquals("固定分值也以区间表达", savedItem.getRemark());

        ClubPointRulePublishRecordDO publishRecord = buildPublishRecord(savedVersion.getId());
        rulePublishRecordMapper.insert(publishRecord);

        List<ClubPointRulePublishRecordDO> records = rulePublishRecordMapper.selectListByRuleVersionId(savedVersion.getId());
        assertEquals(1, records.size());
        ClubPointRulePublishRecordDO savedRecord = records.get(0);
        assertEquals(1, savedRecord.getAction());
        assertEquals(100L, savedRecord.getOperatorUserId());
        assertEquals(LocalDateTime.of(2025, 12, 25, 10, 30), savedRecord.getOperationTime());
        assertEquals("发布 2026 制度", savedRecord.getReason());
        assertEquals("{\"status\":1}", savedRecord.getBeforeJson());
        assertEquals("{\"status\":2}", savedRecord.getAfterJson());
        assertEquals(900L, savedRecord.getAuditLogId());
    }

    private static ClubPointRuleVersionDO buildRuleVersion() {
        return new ClubPointRuleVersionDO()
                .setVersionNo("RULE-2026")
                .setName("积分制度 2026")
                .setStatus(2)
                .setPublicityTime(LocalDateTime.of(2025, 12, 20, 9, 0))
                .setEffectiveTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setPublishedTime(LocalDateTime.of(2025, 12, 25, 10, 0))
                .setDisabledTime(LocalDateTime.of(2026, 12, 31, 23, 59))
                .setSummary("默认制度")
                .setContent("制度正文")
                .setAttachmentSnapshotJson("{\"files\":[1]}")
                .setRemark("备注");
    }

    private static ClubPointRuleItemDO buildRuleItem(Long ruleVersionId) {
        return new ClubPointRuleItemDO()
                .setRuleVersionId(ruleVersionId)
                .setItemCode("ACTIVITY_ATTEND")
                .setItemName("活动签到积分")
                .setItemType(1)
                .setCategory(10)
                .setMinPoints(10)
                .setMaxPoints(10)
                .setDefaultPoints(10)
                .setIntValue(1)
                .setDecimalValue(new BigDecimal("1.25"))
                .setTextValue("文本参数")
                .setJsonValue("{\"fixed\":true}")
                .setStatus(1)
                .setSort(100)
                .setRemark("固定分值也以区间表达");
    }

    private static ClubPointRulePublishRecordDO buildPublishRecord(Long ruleVersionId) {
        return new ClubPointRulePublishRecordDO()
                .setRuleVersionId(ruleVersionId)
                .setAction(1)
                .setOperatorUserId(100L)
                .setOperationTime(LocalDateTime.of(2025, 12, 25, 10, 30))
                .setReason("发布 2026 制度")
                .setBeforeJson("{\"status\":1}")
                .setAfterJson("{\"status\":2}")
                .setAuditLogId(900L);
    }

}
