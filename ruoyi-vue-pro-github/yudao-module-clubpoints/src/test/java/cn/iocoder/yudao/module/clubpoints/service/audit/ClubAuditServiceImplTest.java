package cn.iocoder.yudao.module.clubpoints.service.audit;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.POINT_ADJUST;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.REPORT_EXPORT;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubAuditServiceImpl.class, ClubAuditServiceImplTest.TransactionalProbeService.class})
class ClubAuditServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ClubAuditService clubAuditService;
    @Resource
    private ClubAuditLogMapper clubAuditLogMapper;
    @Resource
    private ClubMemberMapper clubMemberMapper;
    @Resource
    private TransactionalProbeService transactionalProbeService;

    @Test
    void createAuditLogShouldPersistAllFields() {
        ClubAuditCreateReqBO reqBO = buildAuditReq()
                .setActionType(REPORT_EXPORT)
                .setBizType("REPORT")
                .setBizId(200L)
                .setSuccess(true)
                .setTargetSnapshotJson("{\"request\":{\"year\":2026}}");

        Long auditLogId = clubAuditService.createAuditLog(reqBO);

        ClubAuditLogDO auditLog = clubAuditLogMapper.selectById(auditLogId);
        assertNotNull(auditLog);
        assertEquals(REPORT_EXPORT, auditLog.getActionType());
        assertEquals("REPORT", auditLog.getBizType());
        assertEquals(200L, auditLog.getBizId());
        assertEquals(100L, auditLog.getOperatorUserId());
        assertEquals("管理员", auditLog.getOperatorNameSnapshot());
        assertEquals("club_points_admin", auditLog.getOperatorRoleSnapshot());
        assertEquals(LocalDateTime.of(2026, 6, 25, 10, 30), auditLog.getOperationTime());
        assertEquals("127.0.0.1", auditLog.getClientIp());
        assertEquals("JUnit", auditLog.getUserAgent());
        assertEquals("导出报表", auditLog.getReason());
        assertEquals("{\"before\":1}", auditLog.getBeforeJson());
        assertEquals("{\"after\":2}", auditLog.getAfterJson());
        assertEquals("{\"request\":{\"year\":2026}}", auditLog.getTargetSnapshotJson());
        assertTrue(auditLog.getSuccess());
    }

    @Test
    void createAuditLogShouldPersistFailureResult() {
        ClubAuditCreateReqBO reqBO = buildAuditReq()
                .setActionType(POINT_ADJUST)
                .setBizType("LEDGER")
                .setBizId(300L)
                .setSuccess(false)
                .setErrorMessage("积分调整失败");

        Long auditLogId = clubAuditService.createAuditLog(reqBO);

        ClubAuditLogDO auditLog = clubAuditLogMapper.selectById(auditLogId);
        assertEquals(false, auditLog.getSuccess());
        assertEquals("积分调整失败", auditLog.getErrorMessage());
    }

    @Test
    void auditActionTypesShouldCoverRequiredHighRiskOperations() {
        assertEquals("REPORT_EXPORT", REPORT_EXPORT);
        assertEquals("PHYSICAL_DELETE", cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.PHYSICAL_DELETE);
        assertEquals("REVIEW_APPROVE", cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.REVIEW_APPROVE);
        assertEquals("POINT_ADJUST", POINT_ADJUST);
        assertEquals("ANNUAL_CLEARING_MANUAL", cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ANNUAL_CLEARING_MANUAL);
    }

    @Test
    void createAuditLogShouldRejectMissingRequiredField() {
        ClubAuditCreateReqBO reqBO = buildAuditReq().setActionType(null);

        assertServiceException(() -> clubAuditService.createAuditLog(reqBO), CLUB_AUDIT_WRITE_FAILED);
    }

    @Test
    void auditFailureShouldRollbackBusinessChangeInSameTransaction() {
        assertServiceException(() -> transactionalProbeService.writeBusinessThenFailAudit(), CLUB_AUDIT_WRITE_FAILED);

        assertEquals(0L, clubMemberMapper.selectCount(null));
        assertEquals(0L, clubAuditLogMapper.selectCount(null));
    }

    private static ClubAuditCreateReqBO buildAuditReq() {
        return new ClubAuditCreateReqBO()
                .setActionType(REPORT_EXPORT)
                .setBizType("REPORT")
                .setBizId(200L)
                .setOperatorUserId(100L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setOperationTime(LocalDateTime.of(2026, 6, 25, 10, 30))
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("导出报表")
                .setBeforeJson("{\"before\":1}")
                .setAfterJson("{\"after\":2}")
                .setTargetSnapshotJson("{\"request\":{\"year\":2026}}")
                .setSuccess(true);
    }

    private static ClubMemberDO buildMember() {
        return new ClubMemberDO()
                .setClubId(1L)
                .setUserId(100L)
                .setDeptIdSnapshot(10L)
                .setUserNameSnapshot("员工100")
                .setDeptNameSnapshot("研发部")
                .setMobileSnapshot("13800000000")
                .setClubCodeSnapshot("CLUB-1")
                .setClubNameSnapshot("俱乐部1")
                .setStatus(1)
                .setJoinTime(LocalDateTime.now())
                .setActiveUniqueKey("1:100");
    }

    @Service
    static class TransactionalProbeService {

        @Resource
        private ClubMemberMapper clubMemberMapper;
        @Resource
        private ClubAuditService clubAuditService;

        @Transactional
        public void writeBusinessThenFailAudit() {
            clubMemberMapper.insert(buildMember());
            clubAuditService.createAuditLog(buildAuditReq().setActionType(null));
        }

    }

}
