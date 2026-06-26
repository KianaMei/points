package cn.iocoder.yudao.module.clubpoints.controller.admin.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubMemberPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubMemberRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubMemberSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryService;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointMemberService;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberAddReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberRemoveReqBO;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 俱乐部成员")
@RestController
@RequestMapping("/clubpoints/club-member")
@Validated
public class ClubPointClubMemberAdminController {

    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;
    @Resource
    private ClubPointMemberService clubPointMemberService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/page")
    @Operation(summary = "俱乐部成员分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-member:query')")
    public CommonResult<PageResult<AdminClubMemberRespVO>> getMemberPage(@Valid AdminClubMemberPageReqVO pageReqVO) {
        PageResult<ClubPointClubMemberBO> pageResult = clubPointClubQueryService.getAdminMemberPage(
                toPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminClubMemberRespVO.class),
                pageResult.getTotal()));
    }

    @PostMapping("/add")
    @Operation(summary = "添加俱乐部成员")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-member:add')")
    public CommonResult<Long> addMember(@RequestBody @Valid AdminClubMemberSaveReqVO reqVO) {
        return success(clubPointMemberService.addMember(toAddReqBO(reqVO)));
    }

    @PostMapping("/remove")
    @Operation(summary = "移除俱乐部成员")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-member:remove')")
    public CommonResult<Boolean> removeMember(@RequestBody @Valid AdminClubMemberSaveReqVO reqVO) {
        clubPointMemberService.removeMember(toRemoveReqBO(reqVO));
        return success(true);
    }

    private static ClubPointClubMemberPageReqBO toPageReqBO(AdminClubMemberPageReqVO reqVO) {
        ClubPointClubMemberPageReqBO reqBO = new ClubPointClubMemberPageReqBO()
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId())
                .setStatus(reqVO.getStatus());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private ClubPointMemberAddReqBO toAddReqBO(AdminClubMemberSaveReqVO reqVO) {
        adminUserApi.validateUser(reqVO.getUserId());
        AdminUserRespDTO user = adminUserApi.getUser(reqVO.getUserId());
        DeptRespDTO dept = user != null && user.getDeptId() != null ? deptApi.getDept(user.getDeptId()) : null;
        return new ClubPointMemberAddReqBO()
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId())
                .setDeptIdSnapshot(user != null ? user.getDeptId() : null)
                .setUserNameSnapshot(user != null ? user.getNickname() : null)
                .setDeptNameSnapshot(dept != null ? dept.getName() : null)
                .setMobileSnapshot(user != null ? user.getMobile() : null)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
    }

    private static ClubPointMemberRemoveReqBO toRemoveReqBO(AdminClubMemberSaveReqVO reqVO) {
        return new ClubPointMemberRemoveReqBO()
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
    }

}
