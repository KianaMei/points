package cn.iocoder.yudao.module.clubpoints.controller.admin.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubLeaderPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubLeaderRespVO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryService;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubLeaderBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubLeaderPageReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 俱乐部负责人")
@RestController
@RequestMapping("/clubpoints/club-leader")
@Validated
public class ClubPointClubLeaderAdminController {

    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;

    @GetMapping("/page")
    @Operation(summary = "俱乐部负责人分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-leader:update')")
    public CommonResult<PageResult<AdminClubLeaderRespVO>> getLeaderPage(@Valid AdminClubLeaderPageReqVO pageReqVO) {
        PageResult<ClubPointClubLeaderBO> pageResult = clubPointClubQueryService.getAdminLeaderPage(
                toPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminClubLeaderRespVO.class),
                pageResult.getTotal()));
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

}
