package cn.iocoder.yudao.module.clubpoints.controller.admin.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminActivityPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminActivityReasonReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminActivityReviewReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminActivitySaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointActivityQueryService;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointActivityService;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityReviewReqBO;
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

@Tag(name = "管理后台 - 活动")
@RestController
@RequestMapping("/clubpoints/activity")
@Validated
public class ClubPointActivityAdminController {

    @Resource
    private ClubPointActivityQueryService activityQueryService;
    @Resource
    private ClubPointActivityService activityService;

    @GetMapping("/page")
    @Operation(summary = "活动分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:query')")
    public CommonResult<PageResult<AppActivityRespVO>> getActivityPage(@Valid AdminActivityPageReqVO pageReqVO) {
        PageResult<ClubPointActivityInfoBO> pageResult = activityQueryService.getAdminActivityPage(
                toPageReqBO(pageReqVO));
        return success(BeanUtils.toBean(pageResult, AppActivityRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "活动详情")
    @Parameter(name = "id", description = "活动 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:query')")
    public CommonResult<AppActivityRespVO> getActivity(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(activityQueryService.getAdminActivity(id), AppActivityRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建活动草稿")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:create')")
    public CommonResult<Long> createActivity(@RequestBody @Valid AdminActivitySaveReqVO reqVO) {
        return success(activityService.createDraft(toSaveReqBO(reqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "修改活动")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:update')")
    public CommonResult<Boolean> updateActivity(@RequestBody @Valid AdminActivitySaveReqVO reqVO) {
        activityService.updateActivity(toSaveReqBO(reqVO));
        return success(true);
    }

    @PostMapping("/publish")
    @Operation(summary = "直接发布活动")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:publish')")
    public CommonResult<Boolean> publishActivity(@RequestBody @Valid AdminActivityReasonReqVO reqVO) {
        activityService.submitForReview(toSubmitReqBO(reqVO));
        activityService.approveReview(toReviewReqBO(reqVO.getId(), reqVO.getReason()));
        return success(true);
    }

    @PostMapping("/review")
    @Operation(summary = "审核活动发布")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:review')")
    public CommonResult<Boolean> reviewActivity(@RequestBody @Valid AdminActivityReviewReqVO reqVO) {
        ClubPointActivityReviewReqBO reqBO = toReviewReqBO(reqVO.getId(), reqVO.getReason());
        if (Boolean.TRUE.equals(reqVO.getApproved())) {
            activityService.approveReview(reqBO);
        } else {
            activityService.rejectReview(reqBO);
        }
        return success(true);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消活动")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:cancel')")
    public CommonResult<Boolean> cancelActivity(@RequestBody @Valid AdminActivityReasonReqVO reqVO) {
        activityService.cancelActivity(new ClubPointActivityCancelReqBO()
                .setId(reqVO.getId())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason()));
        return success(true);
    }

    private static ClubPointActivityPageReqBO toPageReqBO(AdminActivityPageReqVO reqVO) {
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

    private static ClubPointActivitySaveReqBO toSaveReqBO(AdminActivitySaveReqVO reqVO) {
        return BeanUtils.toBean(reqVO, ClubPointActivitySaveReqBO.class)
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent());
    }

    private static ClubPointActivitySubmitReqBO toSubmitReqBO(AdminActivityReasonReqVO reqVO) {
        return new ClubPointActivitySubmitReqBO()
                .setId(reqVO.getId())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
    }

    private static ClubPointActivityReviewReqBO toReviewReqBO(Long id, String reason) {
        return new ClubPointActivityReviewReqBO()
                .setId(id)
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reason);
    }

}
