package cn.iocoder.yudao.module.clubpoints.controller.admin.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubMemberPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubMemberRespVO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryService;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberPageReqBO;
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

@Tag(name = "管理后台 - 俱乐部成员")
@RestController
@RequestMapping("/clubpoints/club-member")
@Validated
public class ClubPointClubMemberAdminController {

    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;

    @GetMapping("/page")
    @Operation(summary = "俱乐部成员分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-member:query')")
    public CommonResult<PageResult<AdminClubMemberRespVO>> getMemberPage(@Valid AdminClubMemberPageReqVO pageReqVO) {
        PageResult<ClubPointClubMemberBO> pageResult = clubPointClubQueryService.getAdminMemberPage(
                toPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminClubMemberRespVO.class),
                pageResult.getTotal()));
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

}
