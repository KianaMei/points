package cn.iocoder.yudao.module.clubpoints.controller.leader.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo.LeaderClubMemberPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo.LeaderClubMemberRespVO;
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
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "负责人端 - 俱乐部成员")
@RestController
@RequestMapping("/clubpoints/leader/member")
@Validated
public class ClubPointClubMemberLeaderController {

    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;

    @GetMapping("/page")
    @Operation(summary = "负责俱乐部成员分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-member:query')")
    public CommonResult<PageResult<LeaderClubMemberRespVO>> getMemberPage(
            @Valid LeaderClubMemberPageReqVO pageReqVO) {
        PageResult<ClubPointClubMemberBO> pageResult = clubPointClubQueryService.getLeaderMemberPage(
                getLoginUserId(), toPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), LeaderClubMemberRespVO.class),
                pageResult.getTotal()));
    }

    private static ClubPointClubMemberPageReqBO toPageReqBO(LeaderClubMemberPageReqVO reqVO) {
        ClubPointClubMemberPageReqBO reqBO = new ClubPointClubMemberPageReqBO()
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

}
