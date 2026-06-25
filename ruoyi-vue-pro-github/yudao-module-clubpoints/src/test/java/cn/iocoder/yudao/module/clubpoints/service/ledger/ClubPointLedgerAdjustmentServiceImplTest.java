package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerAdjustReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.POINT_ADJUST;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.POINT_REVERSE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_ADJUST_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_REVERSE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({ClubPointLedgerServiceImpl.class, ClubPointRuleServiceImpl.class, ClubAuditServiceImpl.class})
class ClubPointLedgerAdjustmentServiceImplTest extends BaseDbUnitTest {

    private static final Integer STATUS_PUBLISHED = 2;
    private static final Integer RULE_ITEM_ENABLED = 1;

    @Resource
    private ClubPointLedgerService clubPointLedgerService;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;

    @Test
    void reversePositiveTransactionShouldCreateOppositeTransactionAndKeepOriginalFacts() {
        insertPublishedRule(1, 100, 10);
        Long sourceTransactionId = clubPointLedgerService.createTransaction(buildPositiveReq("TX-M4-5001", "IDEMP-M4-5001", 20));

        Long reverseTransactionId = clubPointLedgerService.reverseTransaction(buildReverseReq(sourceTransactionId));

        ClubPointTransactionDO source = transactionMapper.selectById(sourceTransactionId);
        assertEquals(ClubPointTransactionStatusEnum.VALID.getStatus(), source.getStatus());
        assertEquals(20, source.getPoints());
        assertEquals(ClubPointTransactionDirectionEnum.INCREASE.getDirection(), source.getDirection());

        ClubPointTransactionDO reverse = transactionMapper.selectById(reverseTransactionId);
        assertNotNull(reverse);
        assertEquals(ClubPointTransactionStatusEnum.REVERSAL.getStatus(), reverse.getStatus());
        assertEquals(ClubPointTransactionDirectionEnum.DECREASE.getDirection(), reverse.getDirection());
        assertEquals(20, reverse.getPoints());
        assertEquals(ClubPointCategoryEnum.REVERSAL.getCategory(), reverse.getPointCategory());
        assertEquals(ClubPointTransactionSourceTypeEnum.REVERSAL.getType(), reverse.getSourceType());
        assertEquals(sourceTransactionId, reverse.getSourceId());
        assertEquals(sourceTransactionId, reverse.getReverseOfTransactionId());
        assertEquals(400L, reverse.getIssuingClubId());
        assertEquals("CLUB001", reverse.getIssuingClubCodeSnapshot());
        assertEquals("篮球俱乐部", reverse.getIssuingClubNameSnapshot());
        assertEquals("LEDGER_REVERSE:" + sourceTransactionId, reverse.getIdempotencyKey());
        assertEquals("{\"attachments\":[{\"name\":\"撤销材料\"}]}", reverse.getSnapshotJson());

        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(20, account.getTotalPositivePoints());
        assertEquals(20, account.getTotalNegativePoints());
        assertEquals(0, account.getNetPoints());
        assertEquals(0, account.getAvailablePoints());

        ClubAuditLogDO auditLog = auditLogMapper.selectById(reverse.getAuditLogId());
        assertEquals(POINT_REVERSE, auditLog.getActionType());
        assertEquals("LEDGER_TRANSACTION", auditLog.getBizType());
        assertEquals(sourceTransactionId, auditLog.getBizId());
        assertEquals("撤销错误发分", auditLog.getReason());
    }

    @Test
    void reverseSameTransactionTwiceShouldReturnExistingReverseWithoutDoubleDeducting() {
        insertPublishedRule(1, 100, 10);
        Long sourceTransactionId = clubPointLedgerService.createTransaction(buildPositiveReq("TX-M4-5002", "IDEMP-M4-5002", 20));
        ClubPointLedgerReverseReqBO reqBO = buildReverseReq(sourceTransactionId);
        Long firstId = clubPointLedgerService.reverseTransaction(reqBO);

        Long secondId = clubPointLedgerService.reverseTransaction(reqBO);

        assertEquals(firstId, secondId);
        assertEquals(2L, transactionMapper.selectCount());
        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(0, account.getNetPoints());
        assertEquals(0, account.getAvailablePoints());
    }

    @Test
    void reverseReversalTransactionShouldReject() {
        insertPublishedRule(1, 100, 10);
        Long sourceTransactionId = clubPointLedgerService.createTransaction(buildPositiveReq("TX-M4-5003", "IDEMP-M4-5003", 20));
        Long reverseTransactionId = clubPointLedgerService.reverseTransaction(buildReverseReq(sourceTransactionId));

        assertServiceException(() -> clubPointLedgerService.reverseTransaction(buildReverseReq(reverseTransactionId)),
                CLUB_LEDGER_REVERSE_INVALID);
    }

    @Test
    void adjustPointsShouldWriteAuditAndCreateAdjustmentTransactionWithAttachmentSnapshot() {
        insertPublishedRule(1, 100, 10);

        Long transactionId = clubPointLedgerService.adjustPoints(buildAdjustReq("REQ-M4-5004", 15));

        ClubPointTransactionDO transaction = transactionMapper.selectById(transactionId);
        assertNotNull(transaction);
        assertEquals(ClubPointTransactionDirectionEnum.INCREASE.getDirection(), transaction.getDirection());
        assertEquals(15, transaction.getPoints());
        assertEquals(ClubPointCategoryEnum.ADMIN_ADJUSTMENT.getCategory(), transaction.getPointCategory());
        assertEquals(ClubPointTransactionSourceTypeEnum.ADJUSTMENT.getType(), transaction.getSourceType());
        assertEquals("LEDGER_ADJUST:REQ-M4-5004", transaction.getIdempotencyKey());
        assertEquals("管理员补发积分", transaction.getReason());
        assertEquals("{\"attachments\":[{\"name\":\"调整材料\"}]}", transaction.getSnapshotJson());

        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(15, account.getTotalPositivePoints());
        assertEquals(15, account.getNetPoints());
        assertEquals(15, account.getAvailablePoints());

        ClubAuditLogDO auditLog = auditLogMapper.selectById(transaction.getAuditLogId());
        assertEquals(POINT_ADJUST, auditLog.getActionType());
        assertEquals("LEDGER_TRANSACTION", auditLog.getBizType());
        assertEquals("管理员补发积分", auditLog.getReason());
        assertEquals("{\"attachments\":[{\"name\":\"调整材料\"}]}", auditLog.getTargetSnapshotJson());
    }

    @Test
    void adjustPointsShouldRejectMissingAttachmentSnapshot() {
        insertPublishedRule(1, 100, 10);
        ClubPointLedgerAdjustReqBO reqBO = buildAdjustReq("REQ-M4-5005", 15).setAttachmentSnapshotJson(null);

        assertServiceException(() -> clubPointLedgerService.adjustPoints(reqBO), CLUB_LEDGER_ADJUST_INVALID);

        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(0L, auditLogMapper.selectCount());
        assertEquals(0L, accountMapper.selectCount());
    }

    @Test
    void adjustPointsShouldRollbackLedgerWhenAuditFails() {
        insertPublishedRule(1, 100, 10);
        ClubPointLedgerAdjustReqBO reqBO = buildAdjustReq("REQ-M4-5006", 15).setOperatorNameSnapshot(null);

        assertServiceException(() -> clubPointLedgerService.adjustPoints(reqBO), CLUB_AUDIT_WRITE_FAILED);

        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(0L, auditLogMapper.selectCount());
        assertEquals(0L, accountMapper.selectCount());
    }

    private static ClubPointLedgerCreateReqBO buildPositiveReq(String transactionNo, String idempotencyKey, Integer points) {
        return new ClubPointLedgerCreateReqBO()
                .setTransactionNo(transactionNo)
                .setUserId(100L)
                .setUserNameSnapshot("员工A")
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("综合部")
                .setDirection(ClubPointTransactionDirectionEnum.INCREASE.getDirection())
                .setPoints(points)
                .setPointCategory(ClubPointCategoryEnum.SPECIAL_REWARD.getCategory())
                .setSourceType(ClubPointTransactionSourceTypeEnum.ADMIN_DIRECT.getType())
                .setSourceId(300L)
                .setSourceTitleSnapshot("管理员代录")
                .setIssuingClubId(400L)
                .setIssuingClubCodeSnapshot("CLUB001")
                .setIssuingClubNameSnapshot("篮球俱乐部")
                .setRuleItemCode(ClubPointRuleItemCodeEnum.SPECIAL_CONTRIBUTION.getCode())
                .setEvidenceType(1)
                .setMaterialSummary("材料摘要")
                .setReason("原始发分")
                .setOccurredAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .setIdempotencyKey(idempotencyKey)
                .setOperatorUserId(900L)
                .setSourceSnapshotJson("{\"source\":\"direct\"}");
    }

    private static ClubPointLedgerReverseReqBO buildReverseReq(Long sourceTransactionId) {
        return new ClubPointLedgerReverseReqBO()
                .setSourceTransactionId(sourceTransactionId)
                .setTransactionNo("REV-M4-" + sourceTransactionId)
                .setReason("撤销错误发分")
                .setOccurredAt(LocalDateTime.of(2026, 6, 2, 10, 0))
                .setAttachmentSnapshotJson("{\"attachments\":[{\"name\":\"撤销材料\"}]}")
                .setOperatorUserId(901L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

    private static ClubPointLedgerAdjustReqBO buildAdjustReq(String requestNo, Integer points) {
        return new ClubPointLedgerAdjustReqBO()
                .setRequestNo(requestNo)
                .setTransactionNo("ADJ-M4-" + requestNo)
                .setUserId(100L)
                .setUserNameSnapshot("员工A")
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("综合部")
                .setAdjustType(1)
                .setDirection(ClubPointTransactionDirectionEnum.INCREASE.getDirection())
                .setPoints(points)
                .setIssuingClubId(400L)
                .setIssuingClubCodeSnapshot("CLUB001")
                .setIssuingClubNameSnapshot("篮球俱乐部")
                .setRuleItemCode(ClubPointRuleItemCodeEnum.SPECIAL_CONTRIBUTION.getCode())
                .setReason("管理员补发积分")
                .setMaterialSummary("调整材料摘要")
                .setAttachmentSnapshotJson("{\"attachments\":[{\"name\":\"调整材料\"}]}")
                .setOccurredAt(LocalDateTime.of(2026, 6, 3, 10, 0))
                .setOperatorUserId(901L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

    private void insertPublishedRule(Integer minPoints, Integer maxPoints, Integer defaultPoints) {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("V-M4-5")
                .setName("M4.5 规则")
                .setStatus(STATUS_PUBLISHED)
                .setPublicityTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setEffectiveTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setPublishedTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setSummary("summary")
                .setContent("content");
        ruleVersionMapper.insert(version);

        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(version.getId())
                .setItemCode(ClubPointRuleItemCodeEnum.SPECIAL_CONTRIBUTION.getCode())
                .setItemName("特殊贡献积分")
                .setItemType(1)
                .setCategory(ClubPointCategoryEnum.SPECIAL_REWARD.getCategory())
                .setMinPoints(minPoints)
                .setMaxPoints(maxPoints)
                .setDefaultPoints(defaultPoints)
                .setStatus(RULE_ITEM_ENABLED)
                .setSort(1));
    }

}
