package cn.iocoder.yudao.module.clubpoints.controller.app.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubMemberPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubMemberRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.club.vo.AppClubRespVO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryService;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubPageReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "员工端 - 俱乐部")
@RestController
@RequestMapping("/clubpoints/app/club")
@Validated
public class ClubPointClubAppController {

    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;

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
