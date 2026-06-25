package cn.iocoder.yudao.module.clubpoints.controller.leader.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.activity.vo.LeaderActivityPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.activity.vo.LeaderActivitySaveReqVO;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointActivityQueryService;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointActivityService;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivitySaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivitySubmitReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "负责人端 - 活动")
@RestController
@RequestMapping("/clubpoints/leader/activity")
@Validated
public class ClubPointActivityLeaderController {

    @Resource
    private ClubPointActivityQueryService activityQueryService;
    @Resource
    private ClubPointActivityService activityService;

    @GetMapping("/page")
    @Operation(summary = "活动分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:query')")
    public CommonResult<PageResult<AppActivityRespVO>> getActivityPage(@Valid LeaderActivityPageReqVO pageReqVO) {
        PageResult<ClubPointActivityInfoBO> pageResult = activityQueryService.getLeaderActivityPage(
                getLoginUserId(), toPageReqBO(pageReqVO));
        return success(BeanUtils.toBean(pageResult, AppActivityRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "活动详情")
    @Parameter(name = "id", description = "活动 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:query')")
    public CommonResult<AppActivityRespVO> getActivity(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(activityQueryService.getLeaderActivity(getLoginUserId(), id),
                AppActivityRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建活动草稿")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:create')")
    public CommonResult<Long> createActivity(@RequestBody @Valid LeaderActivitySaveReqVO reqVO) {
        return success(activityService.createDraft(toSaveReqBO(reqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "修改活动")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:update')")
    public CommonResult<Boolean> updateActivity(@RequestBody @Valid LeaderActivitySaveReqVO reqVO) {
        activityService.updateActivity(toSaveReqBO(reqVO));
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交发布审核")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:submit')")
    public CommonResult<Boolean> submitActivity(@RequestParam("id") Long id) {
        activityService.submitForReview(new ClubPointActivitySubmitReqBO()
                .setId(id)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("leader")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason("提交活动审核"));
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消活动")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:cancel')")
    public CommonResult<Boolean> cancelActivity(@RequestParam("id") Long id,
                                                @RequestParam(value = "reason", required = false) String reason) {
        activityService.cancelActivity(new ClubPointActivityCancelReqBO()
                .setId(id)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("leader")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reason));
        return success(true);
    }

    private static ClubPointActivityPageReqBO toPageReqBO(LeaderActivityPageReqVO reqVO) {
        ClubPointActivityPageReqBO reqBO = new ClubPointActivityPageReqBO()
                .setClubId(reqVO.getClubId())
                .setKeyword(reqVO.getKeyword())
                .setStatus(reqVO.getStatus())
                .setStartTime(reqVO.getStartTime())
                .setEndTime(reqVO.getEndTime());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointActivitySaveReqBO toSaveReqBO(LeaderActivitySaveReqVO reqVO) {
        return BeanUtils.toBean(reqVO, ClubPointActivitySaveReqBO.class)
                .setOperatorGlobalScope(false)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("leader")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent());
    }

}
