package cn.iocoder.yudao.module.clubpoints.controller.admin.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminActivityReviewReqVO;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointActivityService;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityReviewReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private ClubPointActivityService activityService;

    @PostMapping("/review")
    @Operation(summary = "审核活动发布")
    @PreAuthorize("@ss.hasPermission('clubpoints:activity:review')")
    public CommonResult<Boolean> reviewActivity(@RequestBody @Valid AdminActivityReviewReqVO reqVO) {
        ClubPointActivityReviewReqBO reqBO = new ClubPointActivityReviewReqBO()
                .setId(reqVO.getId())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
        if (Boolean.TRUE.equals(reqVO.getApproved())) {
            activityService.approveReview(reqBO);
        } else {
            activityService.rejectReview(reqBO);
        }
        return success(true);
    }

}
