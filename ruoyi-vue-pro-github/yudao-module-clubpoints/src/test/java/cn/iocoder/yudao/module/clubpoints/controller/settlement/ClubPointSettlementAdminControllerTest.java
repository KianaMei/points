package cn.iocoder.yudao.module.clubpoints.controller.settlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.ClubPointSettlementAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementDetailRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementPendingActivityPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementPendingActivityRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementRunPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementRunReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementRunRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityPointConfigVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityPointConfigVersionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.job.ClubJobRunMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivitySettlementItemTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointSettlementRunStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementAdminServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementJobService;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointSettlementAdminController.class,
        ClubPointActivitySettlementAdminServiceImpl.class,
        ClubPointActivitySettlementJobService.class,
        ClubPointActivitySettlementServiceImpl.class,
        ClubPointLedgerServiceImpl.class,
        ClubPointRuleServiceImpl.class,
        ClubAuditServiceImpl.class
})
class ClubPointSettlementAdminControllerTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 1, 9, 0);

    @Resource
    private ClubPointSettlementAdminController adminController;
    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityPointConfigVersionMapper configVersionMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointAttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;
    @Resource
    private ClubJobRunMapper jobRunMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminSettlementEndpointsShouldRunAndQuerySettlement() throws Exception {
        login(1L, "管理员");
        ClubPointRuleVersionDO ruleVersion = seedSettlementRules();
        ClubPointClubDO club = insertClub("CLUB-M7-API-1", "M7 API Club");
        ClubPointActivityDO activity = insertEndedActivity(club, "M7 API Activity");
        insertConfigVersion(activity, ruleVersion);
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 7401L, "Api User");
        insertAttendance(registration, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(),
                activity.getStartTime());

        PageResult<AdminSettlementPendingActivityRespVO> pendingPage = adminController.getPendingActivityPage(
                buildPendingActivityPageReq()).getCheckedData();
        assertEquals(1L, pendingPage.getTotal());
        assertEquals(activity.getId(), pendingPage.getList().get(0).getId());

        String runResult = adminController.runSettlement(new AdminSettlementRunReqVO()
                .setActivityId(activity.getId())
                .setReason("管理员手动结算")).getCheckedData();

        ClubJobRunDO jobRun = jobRunMapper.selectList().get(0);
        assertTrue(runResult.contains(String.valueOf(jobRun.getId())));
        assertEquals(ClubPointSettlementRunStatusEnum.SUCCESS.getStatus(), jobRun.getStatus());
        ClubPointTransactionDO baseTransaction = transactionMapper.selectByIdempotencyKey(
                ClubPointActivitySettlementItemTypeEnum.BASE.buildIdempotencyKey(activity.getId(), 7401L, 202607));
        assertNotNull(baseTransaction);
        assertEquals(8, baseTransaction.getPoints());
        assertEquals(ClubPointActivityStatusEnum.SETTLED.getStatus(),
                activityMapper.selectById(activity.getId()).getStatus());

        ClubAuditLogDO auditLog = auditLogMapper.selectList().get(0);
        assertEquals(ClubAuditActionTypeConstants.ACTIVITY_SETTLEMENT_MANUAL, auditLog.getActionType());
        assertEquals("ACTIVITY_SETTLEMENT", auditLog.getBizType());
        assertEquals(activity.getId(), auditLog.getBizId());
        assertEquals("管理员手动结算", auditLog.getReason());

        PageResult<AdminSettlementRunRespVO> runPage = adminController.getRunPage(buildRunPageReq(activity.getId()))
                .getCheckedData();
        assertEquals(1L, runPage.getTotal());
        assertEquals(jobRun.getId(), runPage.getList().get(0).getJobRunId());

        AdminSettlementDetailRespVO detail = adminController.getDetail(runPage.getList().get(0).getId())
                .getCheckedData();
        assertEquals(activity.getId(), detail.getRun().getActivityId());
        assertEquals(1, detail.getTransactions().size());
        assertEquals(baseTransaction.getId(), detail.getTransactions().get(0).getId());
    }

    @Test
    void adminManualRerunShouldNotDuplicateLedgerTransactions() throws Exception {
        login(1L, "管理员");
        ClubPointRuleVersionDO ruleVersion = seedSettlementRules();
        ClubPointClubDO club = insertClub("CLUB-M7-API-2", "M7 API Rerun Club");
        ClubPointActivityDO activity = insertEndedActivity(club, "M7 API Rerun Activity");
        insertConfigVersion(activity, ruleVersion);
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 7402L, "Rerun User");
        insertAttendance(registration, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(),
                activity.getStartTime());

        adminController.runSettlement(new AdminSettlementRunReqVO()
                .setActivityId(activity.getId())
                .setReason("第一次手动结算"));
        adminController.runSettlement(new AdminSettlementRunReqVO()
                .setActivityId(activity.getId())
                .setForce(true)
                .setReason("强制重跑"));

        assertEquals(1L, transactionMapper.selectList().stream()
                .filter(transaction -> activity.getId().equals(transaction.getActivityId()))
                .filter(transaction -> ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType()
                        .equals(transaction.getSourceType()))
                .count());
        assertEquals(2L, jobRunMapper.selectCount());
    }

    @Test
    void endpointsShouldUseDocumentedSettlementPathsAndPermissions() throws Exception {
        assertEquals("/clubpoints/settlement",
                ClubPointSettlementAdminController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertGetMapping("getPendingActivityPage",
                new Class<?>[]{AdminSettlementPendingActivityPageReqVO.class}, "/pending-activity-page",
                "@ss.hasPermission('clubpoints:settlement:query')");
        assertPostMapping("runSettlement", new Class<?>[]{AdminSettlementRunReqVO.class}, "/run",
                "@ss.hasPermission('clubpoints:settlement:run')");
        assertGetMapping("getRunPage", new Class<?>[]{AdminSettlementRunPageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:settlement:query')");
        assertGetMapping("getDetail", new Class<?>[]{Long.class}, "/detail",
                "@ss.hasPermission('clubpoints:settlement:query')");
    }

    private void assertGetMapping(String methodName, Class<?>[] parameterTypes, String expectedPath,
                                  String expectedPermission) throws NoSuchMethodException {
        Method method = ClubPointSettlementAdminController.class.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(GetMapping.class).value()[0]);
        assertPermission(method, expectedPermission);
    }

    private void assertPostMapping(String methodName, Class<?>[] parameterTypes, String expectedPath,
                                   String expectedPermission) throws NoSuchMethodException {
        Method method = ClubPointSettlementAdminController.class.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(PostMapping.class).value()[0]);
        assertPermission(method, expectedPermission);
    }

    private static void assertPermission(Method method, String expectedPermission) {
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize);
        assertEquals(expectedPermission, preAuthorize.value());
    }

    private static AdminSettlementPendingActivityPageReqVO buildPendingActivityPageReq() {
        AdminSettlementPendingActivityPageReqVO reqVO = new AdminSettlementPendingActivityPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AdminSettlementRunPageReqVO buildRunPageReq(Long activityId) {
        AdminSettlementRunPageReqVO reqVO = new AdminSettlementRunPageReqVO()
                .setActivityId(activityId);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private ClubPointRuleVersionDO seedSettlementRules() {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("M7-API-RULE-001")
                .setName("M7 API rules")
                .setStatus(ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus())
                .setEffectiveTime(BASE_TIME.minusDays(1))
                .setPublishedTime(BASE_TIME.minusDays(1));
        ruleVersionMapper.insert(version);
        insertRuleItem(version.getId(), ClubPointRuleItemCodeEnum.ACTIVITY_MEDIUM_BASE.getCode(),
                "Medium base", ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(), 8, 1);
        insertRuleItem(version.getId(), ClubPointRuleItemCodeEnum.ACTIVITY_FULL_EXTRA.getCode(),
                "Full extra", ClubPointCategoryEnum.FULL_PARTICIPATION_EXTRA.getCategory(), 2, 2);
        insertRuleItem(version.getId(), ClubPointRuleItemCodeEnum.ABSENCE_SINGLE_DEDUCT.getCode(),
                "Absence single", ClubPointCategoryEnum.DEDUCTION.getCategory(), 3, 3);
        insertIntRuleItem(version.getId(), ClubPointRuleItemCodeEnum.ABSENCE_MONTHLY_THRESHOLD.getCode(),
                "Absence monthly threshold", ClubPointCategoryEnum.DEDUCTION.getCategory(), 3, 4);
        insertRuleItem(version.getId(), ClubPointRuleItemCodeEnum.ABSENCE_MONTHLY_DEDUCT.getCode(),
                "Absence monthly", ClubPointCategoryEnum.DEDUCTION.getCategory(), 20, 5);
        return version;
    }

    private void insertRuleItem(Long versionId, String code, String name, Integer category,
                                Integer defaultPoints, Integer sort) {
        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(versionId)
                .setItemCode(code)
                .setItemName(name)
                .setItemType(1)
                .setCategory(category)
                .setMinPoints(0)
                .setMaxPoints(30)
                .setDefaultPoints(defaultPoints)
                .setStatus(1)
                .setSort(sort));
    }

    private void insertIntRuleItem(Long versionId, String code, String name, Integer category,
                                   Integer intValue, Integer sort) {
        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(versionId)
                .setItemCode(code)
                .setItemName(name)
                .setItemType(2)
                .setCategory(category)
                .setIntValue(intValue)
                .setStatus(1)
                .setSort(sort));
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

    private ClubPointActivityDO insertEndedActivity(ClubPointClubDO club, String title) {
        ClubPointActivityDO activity = new ClubPointActivityDO()
                .setClubId(club.getId())
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setTitle(title)
                .setLocation("Gym")
                .setDescription("Settlement admin activity")
                .setLevel(2)
                .setStatus(ClubPointActivityStatusEnum.ENDED.getStatus())
                .setStartTime(BASE_TIME.plusDays(3))
                .setEndTime(BASE_TIME.plusDays(3).plusHours(2))
                .setRegistrationDeadline(BASE_TIME.plusDays(2))
                .setCancelDeadlineTime(BASE_TIME.plusDays(2).plusHours(12))
                .setCheckinStartTime(BASE_TIME.plusDays(3).minusMinutes(30))
                .setCheckinEndTime(BASE_TIME.plusDays(3).plusMinutes(30))
                .setCheckoutMode(1)
                .setCheckoutStartTime(BASE_TIME.plusDays(3).plusHours(1))
                .setCheckoutEndTime(BASE_TIME.plusDays(3).plusHours(3))
                .setCreatorUserId(2000L)
                .setSnapshotJson("{}")
                .setRemark("activity remark");
        activityMapper.insert(activity);
        return activity;
    }

    private void insertConfigVersion(ClubPointActivityDO activity, ClubPointRuleVersionDO ruleVersion) {
        ClubPointActivityPointConfigVersionDO config = new ClubPointActivityPointConfigVersionDO()
                .setActivityId(activity.getId())
                .setVersionNo(1)
                .setLevel(2)
                .setBasePoints(8)
                .setFullExtraPoints(2)
                .setRuleVersionId(ruleVersion.getId())
                .setEffectiveTime(activity.getStartTime())
                .setCreatedReason("publish")
                .setActive(true)
                .setRuleSnapshotJson("{}");
        configVersionMapper.insert(config);
        activity.setCurrentConfigVersionId(config.getId());
        activityMapper.updateById(activity);
    }

    private ClubPointActivityRegistrationDO insertRegistration(ClubPointActivityDO activity, Long userId,
                                                               String userName) {
        ClubPointActivityRegistrationDO registration = new ClubPointActivityRegistrationDO()
                .setActivityId(activity.getId())
                .setClubId(activity.getClubId())
                .setUserId(userId)
                .setStatus(ClubPointRegistrationStatusEnum.REGISTERED.getStatus())
                .setRegisterTime(BASE_TIME.plusDays(1))
                .setNoAbsenceDeduct(false)
                .setSpecialAbsenceFlag(false)
                .setUserNameSnapshot(userName)
                .setDeptIdSnapshot(61L)
                .setDeptNameSnapshot("Operations")
                .setMobileSnapshot("1390000" + userId)
                .setClubNameSnapshot(activity.getClubNameSnapshot())
                .setActivityTitleSnapshot(activity.getTitle())
                .setActivityStartTimeSnapshot(activity.getStartTime())
                .setActivityEndTimeSnapshot(activity.getEndTime())
                .setActiveUniqueKey(activity.getId() + ":" + userId);
        registrationMapper.insert(registration);
        return registration;
    }

    private void insertAttendance(ClubPointActivityRegistrationDO registration, Integer targetType,
                                  LocalDateTime recordTime) {
        attendanceRecordMapper.insert(new ClubPointAttendanceRecordDO()
                .setRegistrationId(registration.getId())
                .setActivityId(registration.getActivityId())
                .setUserId(registration.getUserId())
                .setTargetType(targetType)
                .setRecordTime(recordTime)
                .setSourceType(1)
                .setOperatorUserId(registration.getUserId())
                .setClientIp("127.0.0.1")
                .setRemark("attendance"));
    }

    private static void login(Long userId, String nickname) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        java.util.Map<String, String> info = new java.util.HashMap<>();
        info.put(INFO_KEY_NICKNAME, nickname);
        SecurityFrameworkUtils.setLoginUser(new LoginUser()
                .setId(userId)
                .setUserType(ADMIN.getValue())
                .setInfo(info), request);
    }

}
