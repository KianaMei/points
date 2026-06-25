package cn.iocoder.yudao.module.clubpoints.controller.admin.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubRespVO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryService;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubPageReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 俱乐部")
@RestController
@RequestMapping("/clubpoints/club")
@Validated
public class ClubPointClubAdminController {

    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;

    @GetMapping("/page")
    @Operation(summary = "俱乐部分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:club:query')")
    public CommonResult<PageResult<AdminClubRespVO>> getClubPage(@Valid AdminClubPageReqVO pageReqVO) {
        PageResult<ClubPointClubInfoBO> pageResult = clubPointClubQueryService.getAdminClubPage(toPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminClubRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "俱乐部详情")
    @Parameter(name = "id", description = "俱乐部 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:club:query')")
    public CommonResult<AdminClubRespVO> getClub(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(clubPointClubQueryService.getAdminClub(id), AdminClubRespVO.class));
    }

    private static ClubPointClubPageReqBO toPageReqBO(AdminClubPageReqVO reqVO) {
        ClubPointClubPageReqBO reqBO = new ClubPointClubPageReqBO()
                .setKeyword(reqVO.getKeyword())
                .setStatus(reqVO.getStatus());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

}
