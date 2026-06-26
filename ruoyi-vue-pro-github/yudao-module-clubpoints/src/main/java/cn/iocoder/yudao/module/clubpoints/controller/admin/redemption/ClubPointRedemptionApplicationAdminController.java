package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionApplicationPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionApplicationRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionReviewReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionApplicationService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionApplicationPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionReviewReqBO;
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
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 兑换申请")
@RestController
@RequestMapping("/clubpoints/redemption-application")
@Validated
public class ClubPointRedemptionApplicationAdminController {

    @Resource
    private ClubPointRedemptionApplicationService applicationService;

    @GetMapping("/page")
    @Operation(summary = "兑换申请分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption:review')")
    public CommonResult<PageResult<AdminRedemptionApplicationRespVO>> getApplicationPage(
            @Valid AdminRedemptionApplicationPageReqVO pageReqVO) {
        PageResult<ClubPointRedemptionApplicationDO> pageResult =
                applicationService.getAdminApplicationPage(true, toPageReqBO(pageReqVO));
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointRedemptionApplicationAdminController::toResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @PostMapping("/review")
    @Operation(summary = "审核兑换申请")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption:review')")
    public CommonResult<Boolean> reviewApplication(@RequestBody @Valid AdminRedemptionReviewReqVO reqVO) {
        applicationService.review(new ClubPointRedemptionReviewReqBO()
                .setId(reqVO.getId())
                .setResult(reqVO.getResult())
                .setReason(reqVO.getReason())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent()));
        return success(true);
    }

    private static ClubPointRedemptionApplicationPageReqBO toPageReqBO(
            AdminRedemptionApplicationPageReqVO reqVO) {
        ClubPointRedemptionApplicationPageReqBO reqBO = new ClubPointRedemptionApplicationPageReqBO()
                .setBatchId(reqVO.getBatchId())
                .setUserId(reqVO.getUserId())
                .setStatus(reqVO.getStatus());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    public static AdminRedemptionApplicationRespVO toResp(ClubPointRedemptionApplicationDO application) {
        return new AdminRedemptionApplicationRespVO()
                .setId(application.getId())
                .setApplicationNo(application.getApplicationNo())
                .setRequestNo(application.getRequestNo())
                .setBatchId(application.getBatchId())
                .setBatchNameSnapshot(snapshotName(application.getBatchSnapshotJson()))
                .setGiftId(application.getGiftId())
                .setGiftNameSnapshot(snapshotName(application.getGiftSnapshotJson()))
                .setUserId(application.getUserId())
                .setPointsCostSnapshot(application.getPointsCost())
                .setQuantity(application.getQuantity())
                .setFrozenPoints(application.getPointsCost())
                .setStatus(application.getStatus())
                .setQualificationRankSnapshot(application.getQualificationRankSnapshot())
                .setApplyTime(application.getApplyTime())
                .setCancelTime(application.getCancelTime())
                .setCancelReason(application.getCancelReason())
                .setReviewerUserId(application.getReviewerUserId())
                .setReviewTime(application.getReviewTime())
                .setReviewReason(application.getReviewReason())
                .setDirectIssueTime(application.getDirectIssueTime());
    }

    static String snapshotName(String snapshotJson) {
        Map<String, Object> map = JsonUtils.parseMap(snapshotJson);
        if (map == null || map.get("name") == null) {
            return null;
        }
        return String.valueOf(map.get("name"));
    }

}
