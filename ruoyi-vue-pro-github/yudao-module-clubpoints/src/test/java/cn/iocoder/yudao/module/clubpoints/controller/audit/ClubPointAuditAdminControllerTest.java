package cn.iocoder.yudao.module.clubpoints.controller.audit;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.audit.ClubPointAuditAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.audit.vo.AdminAuditPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.audit.vo.AdminAuditRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointAuditAdminController.class, ClubAuditServiceImpl.class})
class ClubPointAuditAdminControllerTest extends BaseDbUnitTest {

    private static final String AUDIT_QUERY_PERMISSION = "@ss.hasPermission('clubpoints:audit:query')";
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 6, 1, 9, 0);

    @Resource
    private ClubPointAuditAdminController adminController;
    @Resource
    private ClubAuditLogMapper auditLogMapper;

    @Test
    void endpointShouldUseAuditPagePathAndPermission() throws Exception {
        RequestMapping requestMapping = ClubPointAuditAdminController.class.getAnnotation(RequestMapping.class);
        assertNotNull(requestMapping);
        assertEquals("/clubpoints/audit", requestMapping.value()[0]);

        Method method = ClubPointAuditAdminController.class.getMethod("getAuditPage", AdminAuditPageReqVO.class);
        assertEquals("/page", method.getAnnotation(GetMapping.class).value()[0]);
        assertEquals(AUDIT_QUERY_PERMISSION, method.getAnnotation(PreAuthorize.class).value());
    }

    @Test
    void getAuditPageShouldFilterStrongAuditLogs() {
        auditLogMapper.insert(buildAuditLog(ClubAuditActionTypeConstants.REPORT_EXPORT,
                "REPORT", 1001L, 9001L, true, "导出报表", BASE_TIME));
        auditLogMapper.insert(buildAuditLog(ClubAuditActionTypeConstants.POINT_ADJUST,
                "LEDGER", 2001L, 9002L, false, "积分调整", BASE_TIME.plusDays(1)));

        AdminAuditPageReqVO reqVO = new AdminAuditPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setActionType(ClubAuditActionTypeConstants.REPORT_EXPORT);
        reqVO.setBizType("REPORT");
        reqVO.setBizId(1001L);
        reqVO.setOperatorUserId(9001L);
        reqVO.setSuccess(true);
        reqVO.setOperationTimeStart(BASE_TIME.minusMinutes(1));
        reqVO.setOperationTimeEnd(BASE_TIME.plusMinutes(1));
        CommonResult<PageResult<AdminAuditRespVO>> result = adminController.getAuditPage(reqVO);

        assertEquals(1L, result.getData().getTotal());
        AdminAuditRespVO auditLog = result.getData().getList().get(0);
        assertEquals(ClubAuditActionTypeConstants.REPORT_EXPORT, auditLog.getActionType());
        assertEquals("REPORT", auditLog.getBizType());
        assertEquals(1001L, auditLog.getBizId());
        assertEquals(9001L, auditLog.getOperatorUserId());
        assertEquals("管理员9001", auditLog.getOperatorNameSnapshot());
        assertEquals("admin", auditLog.getOperatorRoleSnapshot());
        assertEquals("导出报表", auditLog.getReason());
        assertTrue(auditLog.getSuccess());
        assertEquals("{\"request\":{\"reportType\":1}}", auditLog.getTargetSnapshotJson());
    }

    private static ClubAuditLogDO buildAuditLog(String actionType, String bizType, Long bizId,
                                                Long operatorUserId, Boolean success, String reason,
                                                LocalDateTime operationTime) {
        return new ClubAuditLogDO()
                .setActionType(actionType)
                .setBizType(bizType)
                .setBizId(bizId)
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot("管理员" + operatorUserId)
                .setOperatorRoleSnapshot("admin")
                .setOperationTime(operationTime)
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason(reason)
                .setBeforeJson("{\"before\":1}")
                .setAfterJson("{\"after\":2}")
                .setTargetSnapshotJson("{\"request\":{\"reportType\":1}}")
                .setSuccess(success)
                .setErrorMessage(success ? null : "失败原因");
    }

}
