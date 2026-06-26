package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionBatchOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionBatchPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionBatchRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionBatchSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionEligibilityPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionEligibilityRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionBatchService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionEligibilityService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionEligibilityPageReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 兑换批次")
@RestController
@RequestMapping("/clubpoints/redemption-batch")
@Validated
public class ClubPointRedemptionBatchAdminController {

    @Resource
    private ClubPointRedemptionBatchService batchService;
    @Resource
    private ClubPointRedemptionEligibilityService eligibilityService;

    @GetMapping("/page")
    @Operation(summary = "兑换批次分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption-batch:manage')")
    public CommonResult<PageResult<AdminRedemptionBatchRespVO>> getBatchPage(
            @Valid AdminRedemptionBatchPageReqVO pageReqVO) {
        PageResult<ClubPointRedemptionBatchDO> pageResult =
                batchService.getAdminBatchPage(true, toPageReqBO(pageReqVO));
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointRedemptionBatchAdminController::toBatchResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @PostMapping("/create")
    @Operation(summary = "创建兑换批次")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption-batch:manage')")
    public CommonResult<Long> createBatch(@RequestBody @Valid AdminRedemptionBatchSaveReqVO reqVO) {
        return success(batchService.createBatch(toSaveReqBO(reqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "修改兑换批次")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption-batch:manage')")
    public CommonResult<Boolean> updateBatch(@RequestBody @Valid AdminRedemptionBatchSaveReqVO reqVO) {
        batchService.updateBatch(toSaveReqBO(reqVO));
        return success(true);
    }

    @PostMapping("/open")
    @Operation(summary = "开启兑换批次")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption-batch:manage')")
    public CommonResult<Boolean> openBatch(@RequestBody @Valid AdminRedemptionBatchOperationReqVO reqVO) {
        batchService.openBatch(reqVO.getId(), toOperationReqBO(reqVO, "开启兑换批次"));
        return success(true);
    }

    @PostMapping("/close")
    @Operation(summary = "关闭兑换批次")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption-batch:manage')")
    public CommonResult<Boolean> closeBatch(@RequestBody @Valid AdminRedemptionBatchOperationReqVO reqVO) {
        batchService.closeBatch(reqVO.getId(), toOperationReqBO(reqVO, "关闭兑换批次"));
        return success(true);
    }

    @GetMapping("/eligibility-page")
    @Operation(summary = "兑换资格快照分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption-batch:manage')")
    public CommonResult<PageResult<AdminRedemptionEligibilityRespVO>> getEligibilityPage(
            @Valid AdminRedemptionEligibilityPageReqVO pageReqVO) {
        PageResult<ClubPointRedemptionEligibilitySnapshotDO> pageResult =
                eligibilityService.getAdminSnapshotPage(true, toEligibilityPageReqBO(pageReqVO));
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointRedemptionBatchAdminController::toEligibilityResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    private static ClubPointRedemptionBatchPageReqBO toPageReqBO(AdminRedemptionBatchPageReqVO reqVO) {
        ClubPointRedemptionBatchPageReqBO reqBO = new ClubPointRedemptionBatchPageReqBO()
                .setYear(reqVO.getYear())
                .setStatus(reqVO.getStatus())
                .setKeyword(reqVO.getKeyword());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointRedemptionEligibilityPageReqBO toEligibilityPageReqBO(
            AdminRedemptionEligibilityPageReqVO reqVO) {
        ClubPointRedemptionEligibilityPageReqBO reqBO = new ClubPointRedemptionEligibilityPageReqBO()
                .setBatchId(reqVO.getBatchId())
                .setQualified(reqVO.getQualified())
                .setUserId(reqVO.getUserId());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointRedemptionBatchSaveReqBO toSaveReqBO(AdminRedemptionBatchSaveReqVO reqVO) {
        return new ClubPointRedemptionBatchSaveReqBO()
                .setId(reqVO.getId())
                .setYear(reqVO.getYear())
                .setName(reqVO.getName())
                .setOpenTime(reqVO.getOpenTime())
                .setCloseTime(reqVO.getCloseTime())
                .setDescription(reqVO.getDescription())
                .setMinAvailablePoints(reqVO.getMinAvailablePoints())
                .setQualifiedCount(reqVO.getQualifiedCount())
                .setIncludeTieAtCutoff(reqVO.getIncludeTieAtCutoff())
                .setQualificationRuleJson(qualificationRuleJson(reqVO.getQualificationRule()))
                .setRuleVersionId(reqVO.getRuleVersionId())
                .setRuleSnapshotJson(ruleSnapshotJson(reqVO))
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
    }

    private static ClubPointRedemptionBatchOperationReqBO toOperationReqBO(
            AdminRedemptionBatchOperationReqVO reqVO, String defaultReason) {
        return new ClubPointRedemptionBatchOperationReqBO()
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(StringUtils.hasText(reqVO.getReason()) ? reqVO.getReason() : defaultReason);
    }

    private static AdminRedemptionBatchRespVO toBatchResp(ClubPointRedemptionBatchDO batch) {
        return new AdminRedemptionBatchRespVO()
                .setId(batch.getId())
                .setYear(batch.getYear())
                .setName(batch.getName())
                .setStatus(batch.getStatus())
                .setOpenTime(batch.getOpenTime())
                .setCloseTime(batch.getCloseTime())
                .setDescription(batch.getDescription())
                .setMinAvailablePoints(batch.getMinAvailablePoints())
                .setQualifiedCount(batch.getQualifiedCount())
                .setIncludeTieAtCutoff(batch.getIncludeTieAtCutoff())
                .setQualificationRule(displayQualificationRule(batch.getQualificationRuleJson()))
                .setSnapshotGenerated(batch.getSnapshotGenerated())
                .setSnapshotGeneratedTime(batch.getSnapshotGeneratedTime())
                .setRuleVersionId(batch.getRuleVersionId())
                .setRuleSnapshotJson(batch.getRuleSnapshotJson());
    }

    private static AdminRedemptionEligibilityRespVO toEligibilityResp(
            ClubPointRedemptionEligibilitySnapshotDO snapshot) {
        return new AdminRedemptionEligibilityRespVO()
                .setId(snapshot.getId())
                .setBatchId(snapshot.getBatchId())
                .setUserId(snapshot.getUserId())
                .setUserNameSnapshot(snapshot.getUserNameSnapshot())
                .setDeptNameSnapshot(snapshot.getDeptNameSnapshot())
                .setNetPointsSnapshot(snapshot.getNetPointsSnapshot())
                .setFrozenPointsSnapshot(snapshot.getFrozenPointsSnapshot())
                .setAvailablePointsSnapshot(snapshot.getAvailablePointsSnapshot())
                .setAnnualEarnedPointsSnapshot(snapshot.getAnnualEarnedPointsSnapshot())
                .setRankNo(snapshot.getRankNo())
                .setQualified(snapshot.getQualified())
                .setQualificationReason(snapshot.getQualificationReason())
                .setTieAtCutoff(snapshot.getTieAtCutoff())
                .setGeneratedTime(snapshot.getGeneratedTime());
    }

    private static String qualificationRuleJson(String qualificationRule) {
        if (JsonUtils.isJsonObject(qualificationRule)) {
            return qualificationRule;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("qualificationRule", qualificationRule);
        return JsonUtils.toJsonString(map);
    }

    private static String ruleSnapshotJson(AdminRedemptionBatchSaveReqVO reqVO) {
        if (StringUtils.hasText(reqVO.getRuleSnapshotJson())) {
            return reqVO.getRuleSnapshotJson();
        }
        return qualificationRuleJson(reqVO.getQualificationRule());
    }

    private static String displayQualificationRule(String qualificationRuleJson) {
        Map<String, Object> map = JsonUtils.parseMap(qualificationRuleJson);
        if (map != null && map.get("qualificationRule") != null) {
            return String.valueOf(map.get("qualificationRule"));
        }
        return qualificationRuleJson;
    }

}
