package cn.iocoder.yudao.module.clubpoints.controller.leader.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo.LeaderClubRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo.LeaderClubSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryService;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubService;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubSaveReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "负责人端 - 俱乐部")
@RestController
@RequestMapping("/clubpoints/leader/club")
@Validated
public class ClubPointClubLeaderController {

    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;
    @Resource
    private ClubPointClubService clubPointClubService;

    @GetMapping("/my-managed-list")
    @Operation(summary = "我负责的俱乐部列表")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-leader')")
    public CommonResult<List<LeaderClubRespVO>> getMyManagedList() {
        return success(BeanUtils.toBean(clubPointClubQueryService.getLeaderManagedClubList(getLoginUserId()),
                LeaderClubRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "负责俱乐部详情")
    @Parameter(name = "id", description = "俱乐部 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:club-leader')")
    public CommonResult<LeaderClubRespVO> getClub(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(clubPointClubQueryService.getLeaderClub(getLoginUserId(), id),
                LeaderClubRespVO.class));
    }

    @PutMapping("/update")
    @Operation(summary = "修改负责俱乐部资料")
    @PreAuthorize("@ss.hasPermission('clubpoints:club:update')")
    public CommonResult<Boolean> updateClub(@RequestBody @Valid LeaderClubSaveReqVO reqVO) {
        ClubPointClubInfoBO current = clubPointClubQueryService.getLeaderClub(getLoginUserId(), reqVO.getId());
        clubPointClubService.updateClub(new ClubPointClubSaveReqBO()
                .setId(reqVO.getId())
                .setCode(current.getCode())
                .setName(reqVO.getName())
                .setDescription(reqVO.getDescription())
                .setContactText(reqVO.getContactText())
                .setCoverFileId(reqVO.getCoverFileId() != null ? reqVO.getCoverFileId() : current.getCoverFileId())
                .setSort(current.getSort())
                .setRemark(current.getRemark())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("leader")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason()));
        return success(true);
    }

}
