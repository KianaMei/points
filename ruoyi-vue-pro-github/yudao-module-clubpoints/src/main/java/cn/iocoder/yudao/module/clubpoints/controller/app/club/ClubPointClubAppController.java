package cn.iocoder.yudao.module.clubpoints.controller.app.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubMemberPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubMemberRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubRespVO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryService;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointMemberService;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberExitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberJoinReqBO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "员工端 - 俱乐部")
@RestController
@RequestMapping("/clubpoints/app/club")
@Validated
public class ClubPointClubAppController {

    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;
    @Resource
    private ClubPointMemberService clubPointMemberService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/my-list")
    @Operation(summary = "我的俱乐部列表")
    public CommonResult<List<AppClubRespVO>> getMyList() {
        return success(BeanUtils.toBean(clubPointClubQueryService.getAppMyClubList(getLoginUserId()),
                AppClubRespVO.class));
    }

    @GetMapping("/joinable-page")
    @Operation(summary = "可加入俱乐部分页")
    public CommonResult<PageResult<AppClubRespVO>> getJoinablePage(@Valid AppClubPageReqVO pageReqVO) {
        PageResult<ClubPointClubInfoBO> pageResult = clubPointClubQueryService.getAppJoinableClubPage(
                getLoginUserId(), toClubPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AppClubRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/member-page")
    @Operation(summary = "俱乐部成员分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-member:query')")
    public CommonResult<PageResult<AppClubMemberRespVO>> getMemberPage(@Valid AppClubMemberPageReqVO pageReqVO) {
        PageResult<ClubPointClubMemberBO> pageResult = clubPointClubQueryService.getAppMemberPage(
                getLoginUserId(), toMemberPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AppClubMemberRespVO.class),
                pageResult.getTotal()));
    }

    @PostMapping("/join")
    @Operation(summary = "加入俱乐部")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-member:join')")
    public CommonResult<Long> joinClub(@RequestBody @Valid AppClubOperationReqVO reqVO) {
        return success(clubPointMemberService.joinClub(toJoinReqBO(reqVO)));
    }

    @PostMapping("/exit")
    @Operation(summary = "退出俱乐部")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-member:exit')")
    public CommonResult<Boolean> exitClub(@RequestBody @Valid AppClubOperationReqVO reqVO) {
        clubPointMemberService.exitClub(new ClubPointMemberExitReqBO()
                .setClubId(reqVO.getId())
                .setUserId(getLoginUserId())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("employee")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason()));
        return success(true);
    }

    private ClubPointMemberJoinReqBO toJoinReqBO(AppClubOperationReqVO reqVO) {
        Long userId = getLoginUserId();
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        DeptRespDTO dept = user != null && user.getDeptId() != null ? deptApi.getDept(user.getDeptId()) : null;
        return new ClubPointMemberJoinReqBO()
                .setClubId(reqVO.getId())
                .setUserId(userId)
                .setDeptIdSnapshot(user != null ? user.getDeptId() : null)
                .setUserNameSnapshot(user != null ? user.getNickname() : getLoginUserNickname())
                .setDeptNameSnapshot(dept != null ? dept.getName() : null)
                .setMobileSnapshot(user != null ? user.getMobile() : null)
                .setOperatorUserId(userId)
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("employee")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
    }

    private static ClubPointClubPageReqBO toClubPageReqBO(AppClubPageReqVO reqVO) {
        ClubPointClubPageReqBO reqBO = new ClubPointClubPageReqBO()
                .setKeyword(reqVO.getKeyword());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointClubMemberPageReqBO toMemberPageReqBO(AppClubMemberPageReqVO reqVO) {
        ClubPointClubMemberPageReqBO reqBO = new ClubPointClubMemberPageReqBO()
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

}
