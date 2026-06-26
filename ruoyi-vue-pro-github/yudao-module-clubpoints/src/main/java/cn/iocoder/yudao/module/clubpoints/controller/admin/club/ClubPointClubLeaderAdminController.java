package cn.iocoder.yudao.module.clubpoints.controller.admin.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubLeaderPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubLeaderRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubMemberSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryService;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointLeaderService;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubLeaderBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubLeaderPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointLeaderAssignReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointLeaderRemoveReqBO;
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

@Tag(name = "管理后台 - 俱乐部负责人")
@RestController
@RequestMapping("/clubpoints/club-leader")
@Validated
public class ClubPointClubLeaderAdminController {

    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;
    @Resource
    private ClubPointLeaderService clubPointLeaderService;

    @GetMapping("/page")
    @Operation(summary = "俱乐部负责人分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-leader:update')")
    public CommonResult<PageResult<AdminClubLeaderRespVO>> getLeaderPage(@Valid AdminClubLeaderPageReqVO pageReqVO) {
        PageResult<ClubPointClubLeaderBO> pageResult = clubPointClubQueryService.getAdminLeaderPage(
                toPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminClubLeaderRespVO.class),
                pageResult.getTotal()));
    }

    @PostMapping("/assign")
    @Operation(summary = "设置俱乐部负责人")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-leader:update')")
    public CommonResult<Long> assignLeader(@RequestBody @Valid AdminClubMemberSaveReqVO reqVO) {
        return success(clubPointLeaderService.assignLeader(toAssignReqBO(reqVO)));
    }

    @PostMapping("/remove")
    @Operation(summary = "移除俱乐部负责人")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-leader:update')")
    public CommonResult<Boolean> removeLeader(@RequestBody @Valid AdminClubMemberSaveReqVO reqVO) {
        clubPointLeaderService.removeLeader(toRemoveReqBO(reqVO));
        return success(true);
    }

    private static ClubPointClubLeaderPageReqBO toPageReqBO(AdminClubLeaderPageReqVO reqVO) {
        ClubPointClubLeaderPageReqBO reqBO = new ClubPointClubLeaderPageReqBO()
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId())
                .setStatus(reqVO.getStatus());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointLeaderAssignReqBO toAssignReqBO(AdminClubMemberSaveReqVO reqVO) {
        return new ClubPointLeaderAssignReqBO()
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
    }

    private static ClubPointLeaderRemoveReqBO toRemoveReqBO(AdminClubMemberSaveReqVO reqVO) {
        return new ClubPointLeaderRemoveReqBO()
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
    }

}
