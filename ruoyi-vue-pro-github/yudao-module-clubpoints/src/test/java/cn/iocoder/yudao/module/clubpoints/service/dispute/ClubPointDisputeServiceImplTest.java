package cn.iocoder.yudao.module.clubpoints.service.dispute;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.attachment.ClubAttachmentRefDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.dispute.ClubPointDisputeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment.ClubAttachmentRefMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.dispute.ClubPointDisputeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeRelatedActionTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeAcceptReqBO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeSubmitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerAdjustReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.system.service.notify.NotifySendService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.ATTACHMENT_TYPE_URL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.BIZ_TYPE_DISPUTE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.STATUS_EFFECTIVE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.DISPUTE_HANDLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubNotifyTemplateConstants.TEMPLATE_DISPUTE_REPLIED;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum.SPECIAL_REWARD;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum.INCREASE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@Import({ClubPointDisputeServiceImpl.class, ClubAttachmentServiceImpl.class, ClubAuditServiceImpl.class,
        ClubPointLedgerServiceImpl.class, ClubPointRuleServiceImpl.class, ClubNotifyServiceImpl.class,
        ClubScopeServiceImpl.class})
class ClubPointDisputeServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 8, 1, 10, 0);

    @Resource
    private ClubPointDisputeService disputeService;
    @Resource
    private ClubPointLedgerService ledgerService;
    @Resource
    private ClubPointDisputeMapper disputeMapper;
    @Resource
    private ClubAttachmentRefMapper attachmentRefMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;

    @MockBean
    private FileService fileService;
    @MockBean
    private NotifySendService notifySendService;

    @Test
    void submitDisputeShouldPersistPendingDisputeAndBindAttachmentsWithoutLedgerChange() {
        Long disputeId = disputeService.submitDispute(buildSubmitReq(8101L, 3001L)
                .setAttachments(Arrays.asList(buildUrlAttachment())));

        ClubPointDisputeDO dispute = disputeMapper.selectById(disputeId);
        assertEquals(8101L, dispute.getUserId());
        assertEquals("积分流水异议", dispute.getTitle());
        assertEquals("这笔积分登记有问题", dispute.getContent());
        assertEquals(ClubPointDisputeTargetTypeEnum.TRANSACTION.getType(), dispute.getTargetType());
        assertEquals(3001L, dispute.getTargetId());
        assertEquals(ClubPointDisputeStatusEnum.PENDING.getStatus(), dispute.getStatus());
        assertNotNull(dispute.getSubmitTime());
        assertNull(dispute.getHandlerUserId());
        assertNull(dispute.getReplyContent());
        assertNull(dispute.getRelatedActionType());
        assertNull(dispute.getRelatedTransactionId());

        List<ClubAttachmentRefDO> attachments = attachmentRefMapper.selectListByBiz(
                BIZ_TYPE_DISPUTE, disputeId, STATUS_EFFECTIVE);
        assertEquals(1, attachments.size());
        assertEquals(ATTACHMENT_TYPE_URL, attachments.get(0).getAttachmentType());
        assertEquals("https://example.test/dispute-proof", attachments.get(0).getUrl());
        assertEquals(8101L, attachments.get(0).getUploadedBy());
        assertEquals(0L, transactionMapper.selectCount());
    }

    @Test
    void acceptDisputeShouldSetHandlerWithoutChangingLedger() {
        Long disputeId = disputeService.submitDispute(buildSubmitReq(8201L, 3002L));

        disputeService.acceptDispute(buildAcceptReq(disputeId));

        ClubPointDisputeDO dispute = disputeMapper.selectById(disputeId);
        assertEquals(ClubPointDisputeStatusEnum.PENDING.getStatus(), dispute.getStatus());
        assertEquals(900L, dispute.getHandlerUserId());
        assertNotNull(dispute.getHandleTime());
        assertNull(dispute.getReplyContent());
        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(0L, auditLogMapper.selectCount());
    }

    @Test
    void rejectDisputeShouldCloseWithoutLedgerAndWriteAudit() {
        Long disputeId = disputeService.submitDispute(buildSubmitReq(8301L, 3003L));

        disputeService.rejectDispute(buildHandleReq(disputeId, "异议证据不足")
                .setRelatedActionType(ClubPointDisputeRelatedActionTypeEnum.NO_ACTION.getType()));

        ClubPointDisputeDO dispute = disputeMapper.selectById(disputeId);
        assertEquals(ClubPointDisputeStatusEnum.CLOSED.getStatus(), dispute.getStatus());
        assertEquals("异议证据不足", dispute.getReplyContent());
        assertEquals(ClubPointDisputeRelatedActionTypeEnum.NO_ACTION.getType(), dispute.getRelatedActionType());
        assertNull(dispute.getRelatedTransactionId());
        assertNotNull(dispute.getCloseTime());
        assertEquals(0L, transactionMapper.selectCount());

        ClubAuditLogDO auditLog = auditLogMapper.selectById(dispute.getAuditLogId());
        assertEquals(DISPUTE_HANDLE, auditLog.getActionType());
        assertEquals(BIZ_TYPE_DISPUTE, auditLog.getBizType());
        assertEquals(disputeId, auditLog.getBizId());
        verify(notifySendService).sendSingleNotifyToAdmin(eq(8301L), eq(TEMPLATE_DISPUTE_REPLIED),
                org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void handleDisputeWithoutPointChangeShouldWriteAuditReplyNotifyAndKeepReplied() {
        Long disputeId = disputeService.submitDispute(buildSubmitReq(8401L, 3004L));

        Long relatedTransactionId = disputeService.handleDispute(buildHandleReq(disputeId, "登记无误，不调整")
                .setRelatedActionType(ClubPointDisputeRelatedActionTypeEnum.NO_ACTION.getType()));

        assertNull(relatedTransactionId);
        ClubPointDisputeDO dispute = disputeMapper.selectById(disputeId);
        assertEquals(ClubPointDisputeStatusEnum.REPLIED.getStatus(), dispute.getStatus());
        assertEquals("登记无误，不调整", dispute.getReplyContent());
        assertEquals(ClubPointDisputeRelatedActionTypeEnum.NO_ACTION.getType(), dispute.getRelatedActionType());
        assertNull(dispute.getRelatedTransactionId());
        assertNull(dispute.getCloseTime());
        assertNotNull(dispute.getAuditLogId());
        assertEquals(0L, transactionMapper.selectCount());
        verify(notifySendService).sendSingleNotifyToAdmin(eq(8401L), eq(TEMPLATE_DISPUTE_REPLIED),
                org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void handleDisputeWithAdjustmentShouldCallLedgerAdjustAndPersistRelatedTransaction() {
        insertPublishedRule("M10-DISPUTE-ADJUST");
        Long disputeId = disputeService.submitDispute(buildSubmitReq(8501L, 3005L));

        Long transactionId = disputeService.handleDispute(buildHandleReq(disputeId, "异议成立，补发积分")
                .setRelatedActionType(ClubPointDisputeRelatedActionTypeEnum.ADJUSTMENT.getType())
                .setAdjustReqBO(buildAdjustReq("REQ-M10-ADJUST", 8501L, 12)));

        ClubPointDisputeDO dispute = disputeMapper.selectById(disputeId);
        assertEquals(ClubPointDisputeStatusEnum.REPLIED.getStatus(), dispute.getStatus());
        assertEquals(ClubPointDisputeRelatedActionTypeEnum.ADJUSTMENT.getType(), dispute.getRelatedActionType());
        assertEquals(transactionId, dispute.getRelatedTransactionId());
        assertNotNull(dispute.getAuditLogId());

        ClubPointTransactionDO transaction = transactionMapper.selectById(transactionId);
        assertEquals(8501L, transaction.getUserId());
        assertEquals(12, transaction.getPoints());
        assertEquals(ClubPointTransactionSourceTypeEnum.ADJUSTMENT.getType(), transaction.getSourceType());
        assertEquals("LEDGER_ADJUST:REQ-M10-ADJUST", transaction.getIdempotencyKey());
        assertEquals(12, accountMapper.selectByUserId(8501L).getAvailablePoints());
        assertEquals(2L, auditLogMapper.selectCount());
    }

    @Test
    void handleDisputeWithReverseShouldCallLedgerReverseAndPersistRelatedTransaction() {
        insertPublishedRule("M10-DISPUTE-REVERSE");
        Long sourceTransactionId = ledgerService.createTransaction(buildPositiveReq("TX-M10-SOURCE",
                "IDEMP-M10-SOURCE", 8601L, 20));
        Long disputeId = disputeService.submitDispute(buildSubmitReq(8601L, sourceTransactionId));

        Long reverseTransactionId = disputeService.handleDispute(buildHandleReq(disputeId, "异议成立，撤销原流水")
                .setRelatedActionType(ClubPointDisputeRelatedActionTypeEnum.REVERSE.getType())
                .setReverseReqBO(buildReverseReq(sourceTransactionId)));

        ClubPointDisputeDO dispute = disputeMapper.selectById(disputeId);
        assertEquals(ClubPointDisputeStatusEnum.REPLIED.getStatus(), dispute.getStatus());
        assertEquals(ClubPointDisputeRelatedActionTypeEnum.REVERSE.getType(), dispute.getRelatedActionType());
        assertEquals(reverseTransactionId, dispute.getRelatedTransactionId());

        ClubPointTransactionDO reverse = transactionMapper.selectById(reverseTransactionId);
        assertEquals(ClubPointTransactionStatusEnum.REVERSAL.getStatus(), reverse.getStatus());
        assertEquals(ClubPointTransactionSourceTypeEnum.REVERSAL.getType(), reverse.getSourceType());
        assertEquals(sourceTransactionId, reverse.getReverseOfTransactionId());
        assertEquals(0, accountMapper.selectByUserId(8601L).getAvailablePoints());
        assertEquals(2L, auditLogMapper.selectCount());
    }

    @Test
    void handleDisputeShouldRollbackWhenAuditFails() {
        Long disputeId = disputeService.submitDispute(buildSubmitReq(8701L, 3007L));

        assertServiceException(() -> disputeService.handleDispute(buildHandleReq(disputeId, "审计失败回滚")
                .setOperatorNameSnapshot(null)
                .setRelatedActionType(ClubPointDisputeRelatedActionTypeEnum.NO_ACTION.getType())),
                CLUB_AUDIT_WRITE_FAILED);

        ClubPointDisputeDO dispute = disputeMapper.selectById(disputeId);
        assertEquals(ClubPointDisputeStatusEnum.PENDING.getStatus(), dispute.getStatus());
        assertNull(dispute.getReplyContent());
        assertNull(dispute.getAuditLogId());
        assertEquals(0L, auditLogMapper.selectCount());
        assertEquals(0L, transactionMapper.selectCount());
    }

    @Test
    void handleDisputeShouldNotRollbackWhenNotifyFails() {
        Long disputeId = disputeService.submitDispute(buildSubmitReq(8801L, 3008L));
        doThrow(new IllegalStateException("notify down")).when(notifySendService)
                .sendSingleNotifyToAdmin(eq(8801L), eq(TEMPLATE_DISPUTE_REPLIED),
                        org.mockito.ArgumentMatchers.anyMap());

        disputeService.handleDispute(buildHandleReq(disputeId, "通知失败但处理保留")
                .setRelatedActionType(ClubPointDisputeRelatedActionTypeEnum.NO_ACTION.getType()));

        ClubPointDisputeDO dispute = disputeMapper.selectById(disputeId);
        assertEquals(ClubPointDisputeStatusEnum.REPLIED.getStatus(), dispute.getStatus());
        assertEquals("通知失败但处理保留", dispute.getReplyContent());
        assertNotNull(dispute.getAuditLogId());
        assertEquals(1L, auditLogMapper.selectCount());
    }

    private static ClubPointDisputeSubmitReqBO buildSubmitReq(Long userId, Long targetId) {
        return new ClubPointDisputeSubmitReqBO()
                .setUserId(userId)
                .setTitle("积分流水异议")
                .setContent("这笔积分登记有问题")
                .setTargetType(ClubPointDisputeTargetTypeEnum.TRANSACTION.getType())
                .setTargetId(targetId)
                .setSubmitTime(BASE_TIME);
    }

    private static ClubPointDisputeAcceptReqBO buildAcceptReq(Long disputeId) {
        return new ClubPointDisputeAcceptReqBO()
                .setId(disputeId)
                .setOperatorGlobalScope(true)
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setHandleTime(BASE_TIME.plusHours(1));
    }

    private static ClubPointDisputeHandleReqBO buildHandleReq(Long disputeId, String replyContent) {
        return new ClubPointDisputeHandleReqBO()
                .setId(disputeId)
                .setReplyContent(replyContent)
                .setOperatorGlobalScope(true)
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason(replyContent)
                .setHandleTime(BASE_TIME.plusHours(2));
    }

    private static ClubAttachmentBindReqBO buildUrlAttachment() {
        return new ClubAttachmentBindReqBO()
                .setAttachmentType(ATTACHMENT_TYPE_URL)
                .setUrl("https://example.test/dispute-proof")
                .setName("dispute-proof")
                .setRemark("异议材料");
    }

    private static ClubPointLedgerAdjustReqBO buildAdjustReq(String requestNo, Long userId, Integer points) {
        return new ClubPointLedgerAdjustReqBO()
                .setRequestNo(requestNo)
                .setTransactionNo("ADJ-" + requestNo)
                .setUserId(userId)
                .setUserNameSnapshot("员工" + userId)
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("综合部")
                .setAdjustType(1)
                .setDirection(INCREASE.getDirection())
                .setPoints(points)
                .setIssuingClubId(400L)
                .setIssuingClubCodeSnapshot("CLUB-M10")
                .setIssuingClubNameSnapshot("M10 俱乐部")
                .setRuleItemCode(ClubPointRuleItemCodeEnum.SPECIAL_CONTRIBUTION.getCode())
                .setReason("异议补发积分")
                .setMaterialSummary("异议处理材料")
                .setAttachmentSnapshotJson("{\"attachments\":[{\"name\":\"异议材料\"}]}")
                .setOccurredAt(BASE_TIME.plusHours(3))
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

    private static ClubPointLedgerReverseReqBO buildReverseReq(Long sourceTransactionId) {
        return new ClubPointLedgerReverseReqBO()
                .setSourceTransactionId(sourceTransactionId)
                .setTransactionNo("DISPUTE-REV-" + sourceTransactionId)
                .setReason("异议撤销原流水")
                .setOccurredAt(BASE_TIME.plusHours(3))
                .setAttachmentSnapshotJson("{\"attachments\":[{\"name\":\"异议撤销材料\"}]}")
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

    private static ClubPointLedgerCreateReqBO buildPositiveReq(String transactionNo, String idempotencyKey,
                                                               Long userId, Integer points) {
        return new ClubPointLedgerCreateReqBO()
                .setTransactionNo(transactionNo)
                .setUserId(userId)
                .setUserNameSnapshot("员工" + userId)
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("综合部")
                .setDirection(INCREASE.getDirection())
                .setPoints(points)
                .setPointCategory(SPECIAL_REWARD.getCategory())
                .setSourceType(ClubPointTransactionSourceTypeEnum.ADMIN_DIRECT.getType())
                .setSourceId(300L)
                .setSourceTitleSnapshot("管理员代录")
                .setIssuingClubId(400L)
                .setIssuingClubCodeSnapshot("CLUB-M10")
                .setIssuingClubNameSnapshot("M10 俱乐部")
                .setRuleItemCode(ClubPointRuleItemCodeEnum.SPECIAL_CONTRIBUTION.getCode())
                .setMaterialSummary("原始材料摘要")
                .setReason("原始发分")
                .setOccurredAt(BASE_TIME.minusDays(1))
                .setIdempotencyKey(idempotencyKey)
                .setOperatorUserId(900L)
                .setSourceSnapshotJson("{\"source\":\"direct\"}");
    }

    private void insertPublishedRule(String versionNo) {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo(versionNo)
                .setName("M10 异议规则")
                .setStatus(ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus())
                .setPublicityTime(BASE_TIME.minusDays(10))
                .setEffectiveTime(BASE_TIME.minusDays(10))
                .setPublishedTime(BASE_TIME.minusDays(10))
                .setSummary("summary")
                .setContent("content");
        ruleVersionMapper.insert(version);

        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(version.getId())
                .setItemCode(ClubPointRuleItemCodeEnum.SPECIAL_CONTRIBUTION.getCode())
                .setItemName("特殊贡献积分")
                .setItemType(ClubPointRuleItemTypeEnum.POINTS.getType())
                .setCategory(SPECIAL_REWARD.getCategory())
                .setMinPoints(1)
                .setMaxPoints(100)
                .setDefaultPoints(10)
                .setStatus(1)
                .setSort(1));
    }

}
