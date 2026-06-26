package cn.iocoder.yudao.module.clubpoints.controller.app.redemption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionApplicationPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionApplicationRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionApplyReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionBatchPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionBatchRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionCancelReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionGiftPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionGiftRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionApplicationService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionBatchService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionApplyReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionApplicationPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionCancelReqBO;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "员工端 - 兑换")
@RestController
@RequestMapping("/clubpoints/app/redemption")
@Validated
public class ClubPointRedemptionAppController {

    @Resource
    private ClubPointRedemptionBatchService batchService;
    @Resource
    private ClubPointRedemptionApplicationService applicationService;

    @GetMapping("/batch-page")
    @Operation(summary = "开放兑换批次分页")
    public CommonResult<PageResult<AppRedemptionBatchRespVO>> getBatchPage(
            @Valid AppRedemptionBatchPageReqVO pageReqVO) {
        PageResult<ClubPointRedemptionBatchDO> pageResult =
                batchService.getAppOpenBatchPage(toBatchPageReqBO(pageReqVO));
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointRedemptionAppController::toBatchResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @GetMapping("/gift-page")
    @Operation(summary = "可兑换礼品分页")
    public CommonResult<PageResult<AppRedemptionGiftRespVO>> getGiftPage(
            @Valid AppRedemptionGiftPageReqVO pageReqVO) {
        List<ClubPointRedemptionGiftDO> gifts =
                applicationService.listAvailableGifts(pageReqVO.getBatchId(), getLoginUserId());
        return success(pageList(gifts.stream()
                .map(ClubPointRedemptionAppController::toGiftResp)
                .collect(Collectors.toList()), pageReqVO));
    }

    @PostMapping("/apply")
    @Operation(summary = "提交兑换申请")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption:apply')")
    public CommonResult<Long> apply(@RequestBody @Valid AppRedemptionApplyReqVO reqVO) {
        return success(applicationService.apply(new ClubPointRedemptionApplyReqBO()
                .setBatchId(reqVO.getBatchId())
                .setGiftId(reqVO.getGiftId())
                .setUserId(getLoginUserId())
                .setQuantity(reqVO.getQuantity())
                .setRequestNo(reqVO.getRequestNo())
                .setRemark(reqVO.getRemark())));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消本人兑换申请")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption:cancel-own')")
    public CommonResult<Boolean> cancel(@RequestBody @Valid AppRedemptionCancelReqVO reqVO) {
        applicationService.cancelOwnApplication(new ClubPointRedemptionCancelReqBO()
                .setId(reqVO.getId())
                .setUserId(getLoginUserId())
                .setReason(reqVO.getReason()));
        return success(true);
    }

    @GetMapping("/my-page")
    @Operation(summary = "我的兑换分页")
    public CommonResult<PageResult<AppRedemptionApplicationRespVO>> getMyPage(
            @Valid AppRedemptionApplicationPageReqVO pageReqVO) {
        PageResult<ClubPointRedemptionApplicationDO> pageResult =
                applicationService.getUserApplicationPage(getLoginUserId(), toApplicationPageReqBO(pageReqVO));
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointRedemptionAppController::toApplicationResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    private static ClubPointRedemptionBatchPageReqBO toBatchPageReqBO(AppRedemptionBatchPageReqVO reqVO) {
        ClubPointRedemptionBatchPageReqBO reqBO = new ClubPointRedemptionBatchPageReqBO()
                .setYear(reqVO.getYear())
                .setKeyword(reqVO.getKeyword());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointRedemptionApplicationPageReqBO toApplicationPageReqBO(
            AppRedemptionApplicationPageReqVO reqVO) {
        ClubPointRedemptionApplicationPageReqBO reqBO = new ClubPointRedemptionApplicationPageReqBO()
                .setBatchId(reqVO.getBatchId())
                .setStatus(reqVO.getStatus());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static AppRedemptionBatchRespVO toBatchResp(ClubPointRedemptionBatchDO batch) {
        return new AppRedemptionBatchRespVO()
                .setId(batch.getId())
                .setYear(batch.getYear())
                .setName(batch.getName())
                .setStatus(batch.getStatus())
                .setOpenTime(batch.getOpenTime())
                .setCloseTime(batch.getCloseTime())
                .setDescription(batch.getDescription())
                .setQualificationRule(displayQualificationRule(batch.getQualificationRuleJson()))
                .setSnapshotGenerated(batch.getSnapshotGenerated());
    }

    private static AppRedemptionGiftRespVO toGiftResp(ClubPointRedemptionGiftDO gift) {
        return new AppRedemptionGiftRespVO()
                .setId(gift.getId())
                .setBatchId(gift.getBatchId())
                .setName(gift.getName())
                .setDescription(gift.getDescription())
                .setPointsCost(gift.getPointsCost())
                .setTierMinPoints(gift.getTierMinPoints())
                .setTierMaxPoints(gift.getTierMaxPoints())
                .setReferenceAmountCent(gift.getReferenceAmountCent())
                .setStockTotal(gift.getStockTotal())
                .setStockLocked(gift.getStockLocked())
                .setStockUsed(gift.getStockUsed())
                .setStatus(gift.getStatus())
                .setImageFileId(gift.getImageFileId())
                .setSort(gift.getSort());
    }

    private static AppRedemptionApplicationRespVO toApplicationResp(
            ClubPointRedemptionApplicationDO application) {
        return new AppRedemptionApplicationRespVO()
                .setId(application.getId())
                .setApplicationNo(application.getApplicationNo())
                .setRequestNo(application.getRequestNo())
                .setBatchId(application.getBatchId())
                .setBatchNameSnapshot(snapshotName(application.getBatchSnapshotJson()))
                .setGiftId(application.getGiftId())
                .setGiftNameSnapshot(snapshotName(application.getGiftSnapshotJson()))
                .setPointsCostSnapshot(application.getPointsCost())
                .setQuantity(application.getQuantity())
                .setFrozenPoints(application.getPointsCost())
                .setStatus(application.getStatus())
                .setQualificationRankSnapshot(application.getQualificationRankSnapshot())
                .setApplyTime(application.getApplyTime())
                .setCancelTime(application.getCancelTime())
                .setCancelReason(application.getCancelReason())
                .setReviewTime(application.getReviewTime())
                .setReviewReason(application.getReviewReason())
                .setDirectIssueTime(application.getDirectIssueTime());
    }

    private static <T> PageResult<T> pageList(List<T> list, PageParam pageParam) {
        if (list == null || list.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        int pageNo = pageParam.getPageNo() == null ? 1 : pageParam.getPageNo();
        int pageSize = pageParam.getPageSize() == null ? 10 : pageParam.getPageSize();
        int fromIndex = Math.min((pageNo - 1) * pageSize, list.size());
        int toIndex = Math.min(fromIndex + pageSize, list.size());
        return new PageResult<>(list.subList(fromIndex, toIndex), (long) list.size());
    }

    private static String displayQualificationRule(String qualificationRuleJson) {
        Map<String, Object> map = JsonUtils.parseMap(qualificationRuleJson);
        if (map != null && map.get("qualificationRule") != null) {
            return String.valueOf(map.get("qualificationRule"));
        }
        return qualificationRuleJson;
    }

    private static String snapshotName(String snapshotJson) {
        Map<String, Object> map = JsonUtils.parseMap(snapshotJson);
        if (map == null || map.get("name") == null) {
            return null;
        }
        return String.valueOf(map.get("name"));
    }

}
