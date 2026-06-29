package cn.iocoder.yudao.module.clubpoints.controller.club;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.ClubPointClubAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.ClubPointClubLeaderAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.ClubPointClubMemberAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubDeleteReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubLeaderPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubLeaderRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubMemberSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubMemberPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubMemberRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.ClubPointClubAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubMemberPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubMemberRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.club.ClubPointClubLeaderController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.club.ClubPointClubMemberLeaderController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo.LeaderClubMemberPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo.LeaderClubMemberRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo.LeaderClubRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo.LeaderClubSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointLeaderServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointMemberServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_DISABLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_LEADER_ASSIGN;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_ADD;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_EXIT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_MEMBER_JOIN;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_UPDATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Import({
        ClubPointClubAppController.class,
        ClubPointClubLeaderController.class,
        ClubPointClubMemberLeaderController.class,
        ClubPointClubAdminController.class,
        ClubPointClubMemberAdminController.class,
        ClubPointClubLeaderAdminController.class,
        ClubPointClubQueryServiceImpl.class,
        ClubPointClubServiceImpl.class,
        ClubPointMemberServiceImpl.class,
        ClubPointLeaderServiceImpl.class,
        ClubAuditServiceImpl.class,
        ClubScopeServiceImpl.class
})
class ClubPointClubQueryControllerTest extends BaseDbUnitTest {

    @Resource
    private ClubPointClubAppController appController;
    @Resource
    private ClubPointClubLeaderController leaderController;
    @Resource
    private ClubPointClubMemberLeaderController leaderMemberController;
    @Resource
    private ClubPointClubAdminController adminController;
    @Resource
    private ClubPointClubMemberAdminController adminMemberController;
    @Resource
    private ClubPointClubLeaderAdminController adminLeaderController;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;

    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private DeptApi deptApi;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void appClubEndpointsShouldUseLoginUserMembershipScope() {
        login(100L, "员工A");
        ClubPointClubDO joinedClub = insertClub("CLUB-M5-6001", "已加入俱乐部",
                ClubPointClubStatusEnum.ENABLED.getStatus(), 10);
        ClubPointClubDO joinableClub = insertClub("CLUB-M5-6002", "可加入俱乐部",
                ClubPointClubStatusEnum.ENABLED.getStatus(), 20);
        ClubPointClubDO disabledClub = insertClub("CLUB-M5-6003", "停用俱乐部",
                ClubPointClubStatusEnum.DISABLED.getStatus(), 30);
        memberMapper.insert(buildMember(joinedClub, 100L, "员工A", ClubPointMemberStatusEnum.ACTIVE.getStatus()));
        memberMapper.insert(buildMember(joinedClub, 101L, "员工B", ClubPointMemberStatusEnum.ACTIVE.getStatus()));
        memberMapper.insert(buildMember(disabledClub, 100L, "员工A", ClubPointMemberStatusEnum.ACTIVE.getStatus()));
        accountMapper.insert(buildAccount(100L, 18));
        accountMapper.insert(buildAccount(101L, 26));

        List<AppClubRespVO> myList = appController.getMyList().getCheckedData();
        Set<Long> myClubIds = myList.stream().map(AppClubRespVO::getId).collect(Collectors.toSet());
        assertTrue(myClubIds.contains(joinedClub.getId()));
        assertTrue(myClubIds.contains(disabledClub.getId()));
        assertFalse(myClubIds.contains(joinableClub.getId()));
        assertTrue(myList.stream().filter(item -> joinedClub.getId().equals(item.getId()))
                .findFirst().orElseThrow(AssertionError::new).getJoined());

        AppClubPageReqVO joinableReqVO = new AppClubPageReqVO();
        joinableReqVO.setPageNo(1);
        joinableReqVO.setPageSize(10);
        PageResult<AppClubRespVO> joinablePage = appController.getJoinablePage(joinableReqVO).getCheckedData();
        assertEquals(1L, joinablePage.getTotal());
        assertEquals(joinableClub.getId(), joinablePage.getList().get(0).getId());
        assertFalse(joinablePage.getList().get(0).getJoined());

        AppClubMemberPageReqVO memberReqVO = new AppClubMemberPageReqVO();
        memberReqVO.setPageNo(1);
        memberReqVO.setPageSize(10);
        memberReqVO.setClubId(joinedClub.getId());
        PageResult<AppClubMemberRespVO> memberPage = appController.getMemberPage(memberReqVO).getCheckedData();
        assertEquals(2L, memberPage.getTotal());
        AppClubMemberRespVO member = memberPage.getList().stream()
                .filter(item -> Long.valueOf(101L).equals(item.getUserId()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertEquals("员工B", member.getNickname());
        assertEquals(26, member.getAvailablePoints());

        AppClubMemberPageReqVO deniedReqVO = new AppClubMemberPageReqVO();
        deniedReqVO.setPageNo(1);
        deniedReqVO.setPageSize(10);
        deniedReqVO.setClubId(joinableClub.getId());
        assertServiceException(() -> appController.getMemberPage(deniedReqVO), CLUB_SCOPE_DENIED);
    }

    @Test
    void appClubJoinAndExitEndpointsShouldWriteMembershipAndAuditAsCurrentUser() {
        login(1002L, "员工1002");
        mockUser(1002L, "员工1002", 301L, "员工部", "13900001002");
        ClubPointClubDO club = insertClub("CLUB-M13-6101", "员工可加入俱乐部",
                ClubPointClubStatusEnum.ENABLED.getStatus(), 10);

        Long memberId = appController.joinClub(new AppClubOperationReqVO()
                .setId(club.getId())
                .setReason("员工主动加入")).getCheckedData();
        appController.exitClub(new AppClubOperationReqVO()
                .setId(club.getId())
                .setReason("员工主动退出")).checkError();

        ClubMemberDO member = memberMapper.selectById(memberId);
        assertEquals(ClubPointMemberStatusEnum.SELF_EXITED.getStatus(), member.getStatus());
        assertEquals("员工1002", member.getUserNameSnapshot());
        assertEquals("员工部", member.getDeptNameSnapshot());
        assertEquals("员工主动退出", member.getLeaveReason());
        Set<String> auditActions = auditLogMapper.selectList().stream()
                .map(ClubAuditLogDO::getActionType)
                .collect(Collectors.toSet());
        assertTrue(auditActions.contains(CLUB_MEMBER_JOIN));
        assertTrue(auditActions.contains(CLUB_MEMBER_EXIT));
    }

    @Test
    void leaderClubEndpointsShouldUseManagedClubScope() {
        login(900L, "负责人");
        ClubPointClubDO managedClub = insertClub("CLUB-M5-6011", "负责俱乐部",
                ClubPointClubStatusEnum.ENABLED.getStatus(), 10);
        ClubPointClubDO otherClub = insertClub("CLUB-M5-6012", "其他俱乐部",
                ClubPointClubStatusEnum.ENABLED.getStatus(), 20);
        leaderMapper.insert(buildLeader(managedClub, 900L, "负责人", ClubPointLeaderStatusEnum.ACTIVE.getStatus()));
        leaderMapper.insert(buildLeader(otherClub, 901L, "其他负责人", ClubPointLeaderStatusEnum.ACTIVE.getStatus()));

        List<LeaderClubRespVO> managedList = leaderController.getMyManagedList().getCheckedData();
        assertEquals(1, managedList.size());
        assertEquals(managedClub.getId(), managedList.get(0).getId());

        LeaderClubRespVO detail = leaderController.getClub(managedClub.getId()).getCheckedData();
        assertEquals("负责俱乐部", detail.getName());
        assertServiceException(() -> leaderController.getClub(otherClub.getId()), CLUB_SCOPE_DENIED);
    }

    @Test
    void leaderClubUpdateAndMemberPageShouldUseManagedClubScope() {
        login(9002L, "负责人9002");
        ClubPointClubDO managedClub = insertClub("CLUB-M13-6111", "负责人可改俱乐部",
                ClubPointClubStatusEnum.ENABLED.getStatus(), 10);
        ClubPointClubDO otherClub = insertClub("CLUB-M13-6112", "负责人不可改俱乐部",
                ClubPointClubStatusEnum.ENABLED.getStatus(), 20);
        leaderMapper.insert(buildLeader(managedClub, 9002L, "负责人9002", ClubPointLeaderStatusEnum.ACTIVE.getStatus()));
        memberMapper.insert(buildMember(managedClub, 1003L, "员工1003", ClubPointMemberStatusEnum.ACTIVE.getStatus()));
        memberMapper.insert(buildMember(otherClub, 1004L, "员工1004", ClubPointMemberStatusEnum.ACTIVE.getStatus()));

        leaderController.updateClub(new LeaderClubSaveReqVO()
                .setId(managedClub.getId())
                .setName("负责人修改后的俱乐部")
                .setDescription("负责人维护介绍")
                .setContactText("leader-contact")
                .setReason("负责人维护资料")).checkError();

        ClubPointClubDO updated = clubMapper.selectById(managedClub.getId());
        assertEquals("CLUB-M13-6111", updated.getCode());
        assertEquals("负责人修改后的俱乐部", updated.getName());
        assertEquals("leader-contact", updated.getContactText());

        LeaderClubMemberPageReqVO memberReqVO = new LeaderClubMemberPageReqVO();
        memberReqVO.setPageNo(1);
        memberReqVO.setPageSize(10);
        memberReqVO.setClubId(managedClub.getId());
        PageResult<LeaderClubMemberRespVO> memberPage = leaderMemberController.getMemberPage(memberReqVO)
                .getCheckedData();
        assertEquals(1L, memberPage.getTotal());
        assertEquals(1003L, memberPage.getList().get(0).getUserId());

        assertServiceException(() -> leaderController.updateClub(new LeaderClubSaveReqVO()
                .setId(otherClub.getId())
                .setName("越权修改")
                .setReason("越权")), CLUB_SCOPE_DENIED);
        LeaderClubMemberPageReqVO deniedReqVO = new LeaderClubMemberPageReqVO();
        deniedReqVO.setPageNo(1);
        deniedReqVO.setPageSize(10);
        deniedReqVO.setClubId(otherClub.getId());
        assertServiceException(() -> leaderMemberController.getMemberPage(deniedReqVO), CLUB_SCOPE_DENIED);
    }

    @Test
    void adminClubEndpointsShouldExposeGlobalClubMemberAndLeaderPages() {
        login(1L, "管理员");
        ClubPointClubDO firstClub = insertClub("CLUB-M5-6021", "管理员俱乐部一",
                ClubPointClubStatusEnum.ENABLED.getStatus(), 10);
        ClubPointClubDO secondClub = insertClub("CLUB-M5-6022", "管理员俱乐部二",
                ClubPointClubStatusEnum.DISABLED.getStatus(), 20);
        memberMapper.insert(buildMember(firstClub, 100L, "员工A", ClubPointMemberStatusEnum.ACTIVE.getStatus()));
        memberMapper.insert(buildMember(firstClub, 101L, "员工B", ClubPointMemberStatusEnum.ADMIN_REMOVED.getStatus()));
        leaderMapper.insert(buildLeader(firstClub, 900L, "负责人A", ClubPointLeaderStatusEnum.ACTIVE.getStatus()));
        leaderMapper.insert(buildLeader(secondClub, 901L, "负责人B", ClubPointLeaderStatusEnum.REMOVED.getStatus()));

        AdminClubPageReqVO clubPageReqVO = new AdminClubPageReqVO();
        clubPageReqVO.setPageNo(1);
        clubPageReqVO.setPageSize(10);
        PageResult<AdminClubRespVO> clubPage = adminController.getClubPage(clubPageReqVO).getCheckedData();
        assertEquals(2L, clubPage.getTotal());
        Set<Long> clubIds = clubPage.getList().stream().map(AdminClubRespVO::getId).collect(Collectors.toSet());
        assertTrue(clubIds.contains(firstClub.getId()));
        assertTrue(clubIds.contains(secondClub.getId()));

        AdminClubRespVO detail = adminController.getClub(firstClub.getId()).getCheckedData();
        assertEquals(firstClub.getCode(), detail.getCode());
        assertEquals(1, detail.getMemberCount());
        assertTrue(detail.getLeaderNames().contains("负责人A"));

        AdminClubMemberPageReqVO memberReqVO = new AdminClubMemberPageReqVO();
        memberReqVO.setPageNo(1);
        memberReqVO.setPageSize(10);
        memberReqVO.setClubId(firstClub.getId());
        PageResult<AdminClubMemberRespVO> memberPage = adminMemberController.getMemberPage(memberReqVO)
                .getCheckedData();
        assertEquals(2L, memberPage.getTotal());

        AdminClubLeaderPageReqVO leaderReqVO = new AdminClubLeaderPageReqVO();
        leaderReqVO.setPageNo(1);
        leaderReqVO.setPageSize(10);
        leaderReqVO.setClubId(firstClub.getId());
        PageResult<AdminClubLeaderRespVO> leaderPage = adminLeaderController.getLeaderPage(leaderReqVO)
                .getCheckedData();
        assertEquals(1L, leaderPage.getTotal());
        assertEquals(900L, leaderPage.getList().get(0).getUserId());
    }

    @Test
    void adminClubMutationEndpointsShouldCreateUpdateDisableMembersAndLeadersThroughServices() {
        login(1L, "管理员");
        mockUser(1001L, "员工1001", 201L, "综合部", "13900001001");
        mockUser(9001L, "负责人9001", 202L, "运营部", "13900009001");

        Long clubId = adminController.createClub(new AdminClubSaveReqVO()
                .setCode("CLUB-M12-DEMO")
                .setName("M12 演示俱乐部")
                .setDescription("M12 demo club")
                .setContactText("contact")
                .setSort(1)
                .setReason("创建演示俱乐部")).getCheckedData();
        adminController.updateClub(new AdminClubSaveReqVO()
                .setId(clubId)
                .setCode("CLUB-M12-DEMO")
                .setName("M12 演示俱乐部改名")
                .setDescription("M12 demo club updated")
                .setContactText("contact updated")
                .setSort(2)
                .setReason("更新演示俱乐部")).checkError();
        Long memberId = adminMemberController.addMember(new AdminClubMemberSaveReqVO()
                .setClubId(clubId)
                .setUserId(1001L)
                .setReason("添加演示成员")).getCheckedData();
        Long leaderId = adminLeaderController.assignLeader(new AdminClubMemberSaveReqVO()
                .setClubId(clubId)
                .setUserId(9001L)
                .setReason("设置演示负责人")).getCheckedData();
        adminController.disableClub(new AdminClubOperationReqVO()
                .setId(clubId)
                .setReason("演示停用")).checkError();

        ClubPointClubDO club = clubMapper.selectById(clubId);
        ClubMemberDO member = memberMapper.selectById(memberId);
        ClubLeaderDO leader = leaderMapper.selectById(leaderId);

        assertEquals("M12 演示俱乐部改名", club.getName());
        assertEquals(ClubPointClubStatusEnum.DISABLED.getStatus(), club.getStatus());
        assertEquals("员工1001", member.getUserNameSnapshot());
        assertEquals("综合部", member.getDeptNameSnapshot());
        assertEquals("负责人9001", leader.getUserNameSnapshot());

        Set<String> auditActions = auditLogMapper.selectList().stream()
                .map(ClubAuditLogDO::getActionType)
                .collect(Collectors.toSet());
        assertTrue(auditActions.contains(CLUB_UPDATE));
        assertTrue(auditActions.contains(CLUB_DISABLE));
        assertTrue(auditActions.contains(CLUB_MEMBER_ADD));
        assertTrue(auditActions.contains(CLUB_LEADER_ASSIGN));
    }

    @Test
    void endpointsShouldUseDocumentedClubPathsAndPermissions() throws Exception {
        assertEquals("/clubpoints/app/club",
                ClubPointClubAppController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/leader/club",
                ClubPointClubLeaderController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/leader/member",
                ClubPointClubMemberLeaderController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/club",
                ClubPointClubAdminController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/club-member",
                ClubPointClubMemberAdminController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/club-leader",
                ClubPointClubLeaderAdminController.class.getAnnotation(RequestMapping.class).value()[0]);

        assertGetMapping(ClubPointClubAppController.class, "getMyList", new Class<?>[]{}, "/my-list", null);
        assertGetMapping(ClubPointClubAppController.class, "getJoinablePage",
                new Class<?>[]{AppClubPageReqVO.class}, "/joinable-page", null);
        assertGetMapping(ClubPointClubAppController.class, "getMemberPage",
                new Class<?>[]{AppClubMemberPageReqVO.class}, "/member-page",
                "@ss.hasPermission('clubpoints:club-member:query')");
        assertPostMapping(ClubPointClubAppController.class, "joinClub",
                new Class<?>[]{AppClubOperationReqVO.class}, "/join",
                "@ss.hasPermission('clubpoints:club-member:join')");
        assertPostMapping(ClubPointClubAppController.class, "exitClub",
                new Class<?>[]{AppClubOperationReqVO.class}, "/exit",
                "@ss.hasPermission('clubpoints:club-member:exit')");
        assertGetMapping(ClubPointClubLeaderController.class, "getMyManagedList", new Class<?>[]{},
                "/my-managed-list", "@ss.hasPermission('clubpoints:club-leader')");
        assertGetMapping(ClubPointClubLeaderController.class, "getClub", new Class<?>[]{Long.class},
                "/get", "@ss.hasPermission('clubpoints:club-leader')");
        assertPutMapping(ClubPointClubLeaderController.class, "updateClub",
                new Class<?>[]{LeaderClubSaveReqVO.class}, "/update",
                "@ss.hasPermission('clubpoints:club:update')");
        assertGetMapping(ClubPointClubMemberLeaderController.class, "getMemberPage",
                new Class<?>[]{LeaderClubMemberPageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:club-member:query')");
        assertGetMapping(ClubPointClubAdminController.class, "getClubPage",
                new Class<?>[]{AdminClubPageReqVO.class}, "/page", "@ss.hasPermission('clubpoints:club:query')");
        assertGetMapping(ClubPointClubAdminController.class, "getClub", new Class<?>[]{Long.class},
                "/get", "@ss.hasPermission('clubpoints:club:query')");
        assertPostMapping(ClubPointClubAdminController.class, "createClub",
                new Class<?>[]{AdminClubSaveReqVO.class}, "/create", "@ss.hasPermission('clubpoints:club:create')");
        assertPutMapping(ClubPointClubAdminController.class, "updateClub",
                new Class<?>[]{AdminClubSaveReqVO.class}, "/update", "@ss.hasPermission('clubpoints:club:update')");
        assertPostMapping(ClubPointClubAdminController.class, "disableClub",
                new Class<?>[]{AdminClubOperationReqVO.class}, "/disable",
                "@ss.hasPermission('clubpoints:club:disable')");
        assertDeleteMapping(ClubPointClubAdminController.class, "deleteClub",
                new Class<?>[]{AdminClubDeleteReqVO.class}, "/delete", "@ss.hasPermission('clubpoints:club:delete')");
        assertGetMapping(ClubPointClubMemberAdminController.class, "getMemberPage",
                new Class<?>[]{AdminClubMemberPageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:club-member:query')");
        assertPostMapping(ClubPointClubMemberAdminController.class, "addMember",
                new Class<?>[]{AdminClubMemberSaveReqVO.class}, "/add",
                "@ss.hasPermission('clubpoints:club-member:add')");
        assertPostMapping(ClubPointClubMemberAdminController.class, "removeMember",
                new Class<?>[]{AdminClubMemberSaveReqVO.class}, "/remove",
                "@ss.hasPermission('clubpoints:club-member:remove')");
        assertGetMapping(ClubPointClubLeaderAdminController.class, "getLeaderPage",
                new Class<?>[]{AdminClubLeaderPageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:club-leader:update')");
        assertPostMapping(ClubPointClubLeaderAdminController.class, "assignLeader",
                new Class<?>[]{AdminClubMemberSaveReqVO.class}, "/assign",
                "@ss.hasPermission('clubpoints:club-leader:update')");
        assertPostMapping(ClubPointClubLeaderAdminController.class, "removeLeader",
                new Class<?>[]{AdminClubMemberSaveReqVO.class}, "/remove",
                "@ss.hasPermission('clubpoints:club-leader:update')");
    }

    private static void assertGetMapping(Class<?> controllerClass, String methodName, Class<?>[] parameterTypes,
                                         String expectedPath, String expectedPermission) throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(GetMapping.class).value()[0]);
        assertPermission(method, expectedPermission);
    }

    private static void assertPostMapping(Class<?> controllerClass, String methodName, Class<?>[] parameterTypes,
                                          String expectedPath, String expectedPermission) throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(PostMapping.class).value()[0]);
        assertPermission(method, expectedPermission);
    }

    private static void assertPutMapping(Class<?> controllerClass, String methodName, Class<?>[] parameterTypes,
                                         String expectedPath, String expectedPermission) throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(PutMapping.class).value()[0]);
        assertPermission(method, expectedPermission);
    }

    private static void assertDeleteMapping(Class<?> controllerClass, String methodName, Class<?>[] parameterTypes,
                                            String expectedPath, String expectedPermission) throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(DeleteMapping.class).value()[0]);
        assertPermission(method, expectedPermission);
    }

    private static void assertPermission(Method method, String expectedPermission) {
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        if (expectedPermission == null) {
            assertFalse(method.isAnnotationPresent(PreAuthorize.class));
        } else {
            assertNotNull(preAuthorize);
            assertEquals(expectedPermission, preAuthorize.value());
        }
    }

    private ClubPointClubDO insertClub(String code, String name, Integer status, Integer sort) {
        ClubPointClubDO club = new ClubPointClubDO()
                .setCode(code)
                .setName(name)
                .setStatus(status)
                .setDescription("desc-" + name)
                .setContactText("contact-" + name)
                .setSort(sort)
                .setRemark("remark");
        clubMapper.insert(club);
        return club;
    }

    private static ClubMemberDO buildMember(ClubPointClubDO club, Long userId, String userName, Integer status) {
        return new ClubMemberDO()
                .setClubId(club.getId())
                .setUserId(userId)
                .setDeptIdSnapshot(10L + userId)
                .setUserNameSnapshot(userName)
                .setDeptNameSnapshot("综合部")
                .setMobileSnapshot("1380000" + userId)
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setStatus(status)
                .setJoinTime(LocalDateTime.of(2026, 6, 1, 10, 0).plusMinutes(userId))
                .setActiveUniqueKey(ClubPointMemberStatusEnum.ACTIVE.getStatus().equals(status)
                        ? club.getId() + ":" + userId : null);
    }

    private static ClubLeaderDO buildLeader(ClubPointClubDO club, Long userId, String userName, Integer status) {
        return new ClubLeaderDO()
                .setClubId(club.getId())
                .setUserId(userId)
                .setStatus(status)
                .setAssignedTime(LocalDateTime.of(2026, 6, 1, 9, 0).plusMinutes(userId))
                .setAssignedBy(1L)
                .setRemovedTime(ClubPointLeaderStatusEnum.REMOVED.getStatus().equals(status)
                        ? LocalDateTime.of(2026, 6, 2, 9, 0) : null)
                .setRemovedBy(ClubPointLeaderStatusEnum.REMOVED.getStatus().equals(status) ? 1L : null)
                .setReason("任免负责人")
                .setClubNameSnapshot(club.getName())
                .setUserNameSnapshot(userName)
                .setActiveUniqueKey(ClubPointLeaderStatusEnum.ACTIVE.getStatus().equals(status)
                        ? club.getId() + ":" + userId : null);
    }

    private static ClubPointAccountDO buildAccount(Long userId, Integer availablePoints) {
        return new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(availablePoints)
                .setTotalNegativePoints(0)
                .setNetPoints(availablePoints)
                .setFrozenPoints(0)
                .setAvailablePoints(availablePoints)
                .setAnnualEarnedPoints(availablePoints)
                .setVersion(1);
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

    private void mockUser(Long userId, String nickname, Long deptId, String deptName, String mobile) {
        AdminUserRespDTO user = new AdminUserRespDTO()
                .setId(userId)
                .setNickname(nickname)
                .setDeptId(deptId)
                .setMobile(mobile);
        DeptRespDTO dept = new DeptRespDTO()
                .setId(deptId)
                .setName(deptName);
        doNothing().when(adminUserApi).validateUser(userId);
        when(adminUserApi.getUser(userId)).thenReturn(user);
        when(deptApi.getDept(deptId)).thenReturn(dept);
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

}
