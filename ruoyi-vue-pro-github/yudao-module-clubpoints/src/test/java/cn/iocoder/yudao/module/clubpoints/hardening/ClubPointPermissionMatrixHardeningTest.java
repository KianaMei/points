package cn.iocoder.yudao.module.clubpoints.hardening;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.ClubPointLedgerAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerAccountPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerAccountRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.ClubPointRedemptionApplicationAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionApplicationPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionReviewReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.ClubPointReportAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportExportReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.ClubPointLedgerAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.ClubPointLedgerLeaderController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerMemberSummaryPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerMemberSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerQueryServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Import({
        ClubPointLedgerAppController.class,
        ClubPointLedgerLeaderController.class,
        ClubPointLedgerAdminController.class,
        ClubPointLedgerQueryServiceImpl.class,
        ClubScopeServiceImpl.class
})
class ClubPointPermissionMatrixHardeningTest extends BaseDbUnitTest {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_VALID = ClubPointTransactionStatusEnum.VALID.getStatus();
    private static final int DIRECTION_INCREASE = ClubPointTransactionDirectionEnum.INCREASE.getDirection();
    private static final int SOURCE_ACTIVITY = ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType();
    private static final int SOURCE_ADJUSTMENT = ClubPointTransactionSourceTypeEnum.ADJUSTMENT.getType();
    private static final int CATEGORY_BASIC = ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory();
    private static final long EMPLOYEE_ID = 100L;
    private static final long OTHER_EMPLOYEE_ID = 101L;
    private static final long LEADER_ID = 900L;
    private static final long ADMIN_ID = 1L;
    private static final long MANAGED_CLUB_ID = 400L;
    private static final long OTHER_CLUB_ID = 401L;

    @Resource
    private ClubPointLedgerAppController appLedgerController;
    @Resource
    private ClubPointLedgerLeaderController leaderLedgerController;
    @Resource
    private ClubPointLedgerAdminController adminLedgerController;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dataScopeShouldAllowSelfManagedClubAndAdminGlobalOnly() {
        accountMapper.insert(buildAccount(EMPLOYEE_ID, 100, 20, 80));
        accountMapper.insert(buildAccount(OTHER_EMPLOYEE_ID, 60, 0, 60));
        memberMapper.insert(buildMember(EMPLOYEE_ID, MANAGED_CLUB_ID));
        memberMapper.insert(buildMember(OTHER_EMPLOYEE_ID, MANAGED_CLUB_ID));
        leaderMapper.insert(buildLeader(LEADER_ID, MANAGED_CLUB_ID));

        Long employeeOwnTransactionId = insertTransaction("M12-PERM-TX-001", EMPLOYEE_ID, DIRECTION_INCREASE,
                10, SOURCE_ACTIVITY, MANAGED_CLUB_ID, "employee own transaction");
        Long otherEmployeeTransactionId = insertTransaction("M12-PERM-TX-002", OTHER_EMPLOYEE_ID, DIRECTION_INCREASE,
                12, SOURCE_ACTIVITY, MANAGED_CLUB_ID, "other employee managed club transaction");
        Long otherClubTransactionId = insertTransaction("M12-PERM-TX-003", EMPLOYEE_ID, DIRECTION_INCREASE,
                50, SOURCE_ACTIVITY, OTHER_CLUB_ID, "other club transaction");
        Long globalAdjustmentTransactionId = insertTransaction("M12-PERM-TX-004", EMPLOYEE_ID, DIRECTION_INCREASE,
                30, SOURCE_ADJUSTMENT, null, "global adjustment transaction");

        login(EMPLOYEE_ID, "Employee");
        AppLedgerSummaryRespVO summary = appLedgerController.getSummary().getCheckedData();
        assertEquals(80, summary.getAvailablePoints());
        PageResult<AppLedgerTransactionRespVO> appPage = appLedgerController.getTransactionPage(appPage()).getCheckedData();
        Set<Long> appTransactionIds = appPage.getList().stream()
                .map(AppLedgerTransactionRespVO::getId)
                .collect(Collectors.toSet());
        assertTrue(appTransactionIds.contains(employeeOwnTransactionId));
        assertTrue(appTransactionIds.contains(otherClubTransactionId));
        assertTrue(appTransactionIds.contains(globalAdjustmentTransactionId));
        assertFalse(appPage.getList().stream().anyMatch(item -> Long.valueOf(OTHER_EMPLOYEE_ID).equals(item.getUserId())));
        assertFalse(hasField(AppLedgerTransactionPageReqVO.class, "userId"));

        login(LEADER_ID, "Leader");
        PageResult<LeaderLedgerTransactionRespVO> leaderPage = leaderLedgerController
                .getTransactionPage(leaderTransactionPage(MANAGED_CLUB_ID)).getCheckedData();
        Set<Long> leaderTransactionIds = leaderPage.getList().stream()
                .map(LeaderLedgerTransactionRespVO::getId)
                .collect(Collectors.toSet());
        assertTrue(leaderTransactionIds.contains(employeeOwnTransactionId));
        assertTrue(leaderTransactionIds.contains(otherEmployeeTransactionId));
        assertFalse(leaderTransactionIds.contains(otherClubTransactionId));
        assertFalse(leaderTransactionIds.contains(globalAdjustmentTransactionId));
        assertTrue(leaderPage.getList().stream().allMatch(item -> Long.valueOf(MANAGED_CLUB_ID).equals(item.getIssuingClubId())));

        PageResult<LeaderLedgerMemberSummaryRespVO> memberSummaryPage = leaderLedgerController
                .getMemberSummaryPage(leaderMemberSummaryPage(MANAGED_CLUB_ID)).getCheckedData();
        LeaderLedgerMemberSummaryRespVO employeeSummary = memberSummaryPage.getList().stream()
                .filter(item -> Long.valueOf(EMPLOYEE_ID).equals(item.getUserId()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertEquals(10, employeeSummary.getClubPositivePoints());
        assertEquals(10, employeeSummary.getClubNetPoints());
        assertServiceException(() -> leaderLedgerController.getTransactionPage(leaderTransactionPage(OTHER_CLUB_ID)),
                CLUB_SCOPE_DENIED);

        login(ADMIN_ID, "Admin");
        PageResult<AdminLedgerAccountRespVO> accountPage = adminLedgerController.getAccountPage(adminAccountPage())
                .getCheckedData();
        assertEquals(2L, accountPage.getTotal());
        PageResult<AdminLedgerTransactionRespVO> adminTransactionPage = adminLedgerController
                .getTransactionPage(adminTransactionPage()).getCheckedData();
        Set<Long> adminTransactionIds = adminTransactionPage.getList().stream()
                .map(AdminLedgerTransactionRespVO::getId)
                .collect(Collectors.toSet());
        assertTrue(adminTransactionIds.contains(employeeOwnTransactionId));
        assertTrue(adminTransactionIds.contains(otherEmployeeTransactionId));
        assertTrue(adminTransactionIds.contains(otherClubTransactionId));
        assertTrue(adminTransactionIds.contains(globalAdjustmentTransactionId));
    }

    @Test
    void leaderShouldNotReceiveAdminOnlyReviewOrExportPermissions() throws Exception {
        assertPermission(ClubPointRedemptionApplicationAdminController.class, "getApplicationPage",
                new Class<?>[]{AdminRedemptionApplicationPageReqVO.class},
                "@ss.hasPermission('clubpoints:redemption:review')");
        assertPermission(ClubPointRedemptionApplicationAdminController.class, "reviewApplication",
                new Class<?>[]{AdminRedemptionReviewReqVO.class},
                "@ss.hasPermission('clubpoints:redemption:review')");
        assertPermission(ClubPointReportAdminController.class, "exportExcel",
                new Class<?>[]{AdminReportExportReqVO.class, HttpServletResponse.class},
                "@ss.hasPermission('clubpoints:report:export')");

        String seedSql = read(findRuoyiRoot().resolve("sql/mysql/club-points-seed.sql"));
        Set<Long> adminOnlyMenuIds = menuIdsByPermissions(seedSql,
                new String[]{"clubpoints:redemption:review", "clubpoints:report:export"});
        Set<Long> leaderMenuIds = roleMenuIds(seedSql, 1300000001L);

        assertFalse(adminOnlyMenuIds.isEmpty());
        for (Long adminOnlyMenuId : adminOnlyMenuIds) {
            assertFalse(leaderMenuIds.contains(adminOnlyMenuId), "leader role must not include menu " + adminOnlyMenuId);
        }
    }

    private static void assertPermission(Class<?> controllerClass, String methodName, Class<?>[] parameterTypes,
                                         String expectedPermission) throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize);
        assertEquals(expectedPermission, preAuthorize.value());
    }

    private static boolean hasField(Class<?> type, String fieldName) {
        return Arrays.stream(type.getDeclaredFields()).map(Field::getName).anyMatch(fieldName::equals);
    }

    private static AppLedgerTransactionPageReqVO appPage() {
        AppLedgerTransactionPageReqVO reqVO = new AppLedgerTransactionPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        return reqVO;
    }

    private static LeaderLedgerTransactionPageReqVO leaderTransactionPage(Long clubId) {
        LeaderLedgerTransactionPageReqVO reqVO = new LeaderLedgerTransactionPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        reqVO.setClubId(clubId);
        return reqVO;
    }

    private static LeaderLedgerMemberSummaryPageReqVO leaderMemberSummaryPage(Long clubId) {
        LeaderLedgerMemberSummaryPageReqVO reqVO = new LeaderLedgerMemberSummaryPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        reqVO.setClubId(clubId);
        return reqVO;
    }

    private static AdminLedgerAccountPageReqVO adminAccountPage() {
        AdminLedgerAccountPageReqVO reqVO = new AdminLedgerAccountPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        return reqVO;
    }

    private static AdminLedgerTransactionPageReqVO adminTransactionPage() {
        AdminLedgerTransactionPageReqVO reqVO = new AdminLedgerTransactionPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        return reqVO;
    }

    private Long insertTransaction(String transactionNo, Long userId, Integer direction, Integer points,
                                   Integer sourceType, Long issuingClubId, String reason) {
        ClubPointTransactionDO transaction = new ClubPointTransactionDO()
                .setTransactionNo(transactionNo)
                .setUserId(userId)
                .setUserNameSnapshot("User " + userId)
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("Ops")
                .setDirection(direction)
                .setPoints(points)
                .setPointCategory(CATEGORY_BASIC)
                .setPointTypeCode("M12_PERMISSION")
                .setStatus(STATUS_VALID)
                .setSourceType(sourceType)
                .setSourceId(1L)
                .setSourceTitleSnapshot("M12 permission source")
                .setIssuingClubId(issuingClubId)
                .setIssuingClubCodeSnapshot(issuingClubId == null ? null : "CLUB-" + issuingClubId)
                .setIssuingClubNameSnapshot(issuingClubId == null ? null : "Club " + issuingClubId)
                .setRuleVersionId(1L)
                .setRuleItemCodeSnapshot("M12_PERMISSION")
                .setEvidenceType(1)
                .setMaterialSummary("M12 permission material")
                .setReason(reason)
                .setOccurredAt(LocalDateTime.of(2026, 6, 2, 10, 0).plusMinutes(points))
                .setBusinessYear(2026)
                .setBusinessMonth(202606)
                .setIdempotencyKey("IDEMP-" + transactionNo)
                .setOperatorUserId(ADMIN_ID);
        transactionMapper.insert(transaction);
        return transaction.getId();
    }

    private static ClubPointAccountDO buildAccount(Long userId, Integer totalPositivePoints,
                                                   Integer totalNegativePoints, Integer availablePoints) {
        return new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(totalPositivePoints)
                .setTotalNegativePoints(totalNegativePoints)
                .setNetPoints(totalPositivePoints - totalNegativePoints)
                .setFrozenPoints(0)
                .setAvailablePoints(availablePoints)
                .setAnnualEarnedPoints(totalPositivePoints)
                .setVersion(1);
    }

    private static ClubMemberDO buildMember(Long userId, Long clubId) {
        return new ClubMemberDO()
                .setClubId(clubId)
                .setUserId(userId)
                .setDeptIdSnapshot(10L)
                .setUserNameSnapshot("User " + userId)
                .setDeptNameSnapshot("Ops")
                .setMobileSnapshot("13800000000")
                .setClubCodeSnapshot("CLUB-" + clubId)
                .setClubNameSnapshot("Club " + clubId)
                .setStatus(STATUS_ACTIVE)
                .setJoinTime(LocalDateTime.of(2026, 6, 1, 9, 0))
                .setActiveUniqueKey(clubId + ":" + userId);
    }

    private static ClubLeaderDO buildLeader(Long userId, Long clubId) {
        return new ClubLeaderDO()
                .setClubId(clubId)
                .setUserId(userId)
                .setStatus(STATUS_ACTIVE)
                .setAssignedTime(LocalDateTime.of(2026, 6, 1, 9, 0))
                .setAssignedBy(ADMIN_ID)
                .setReason("assign leader")
                .setClubNameSnapshot("Club " + clubId)
                .setUserNameSnapshot("Leader " + userId)
                .setActiveUniqueKey(clubId + ":" + userId);
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

    private static void assertServiceException(Runnable runnable, ErrorCode errorCode) {
        try {
            runnable.run();
            fail("Expected ServiceException");
        } catch (ServiceException ex) {
            assertEquals(errorCode.getCode(), ex.getCode());
            assertEquals(errorCode.getMsg(), ex.getMessage());
        }
    }

    private static Set<Long> menuIdsByPermissions(String sql, String[] permissions) {
        Set<String> permissionSet = new LinkedHashSet<>(Arrays.asList(permissions));
        Pattern pattern = Pattern.compile("\\((\\d+),\\s*'[^']*',\\s*'([^']+)'", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(sql);
        Set<Long> menuIds = new LinkedHashSet<>();
        while (matcher.find()) {
            if (permissionSet.contains(matcher.group(2))) {
                menuIds.add(Long.valueOf(matcher.group(1)));
            }
        }
        return menuIds;
    }

    private static Set<Long> roleMenuIds(String sql, Long roleId) {
        Pattern pattern = Pattern.compile("SELECT\\s+" + roleId + ",\\s*`id`.*?WHERE\\s+`id`\\s+IN\\s*\\((.*?)\\);",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(sql);
        assertTrue(matcher.find(), "role menu assignment not found for role " + roleId);
        Set<Long> menuIds = new LinkedHashSet<>();
        for (String value : matcher.group(1).split(",")) {
            menuIds.add(Long.valueOf(value.trim()));
        }
        return menuIds;
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static Path findRuoyiRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("sql/mysql/club-points-seed.sql"))) {
                return current;
            }
            Path nested = current.resolve("ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql");
            if (Files.exists(nested)) {
                return current.resolve("ruoyi-vue-pro-github");
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate ruoyi-vue-pro-github root");
    }

}
