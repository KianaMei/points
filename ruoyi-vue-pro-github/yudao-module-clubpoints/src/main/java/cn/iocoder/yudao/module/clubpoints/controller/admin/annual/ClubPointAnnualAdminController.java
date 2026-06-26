package cn.iocoder.yudao.module.clubpoints.controller.admin.annual;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearingRecordPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearingRecordRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualIncentiveOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualIncentiveSuggestReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualRankingGenerateReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualRankingPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualRankingRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualClearingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingService;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualRankingService;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointIncentiveService;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearAllReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearResultBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualRankingGenerateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointIncentiveOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointIncentiveSuggestReqBO;
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
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 年度运营")
@RestController
@RequestMapping("/clubpoints/annual")
@Validated
public class ClubPointAnnualAdminController {

    @Resource
    private ClubPointAnnualClearingService clearingService;
    @Resource
    private ClubPointAnnualRankingService rankingService;
    @Resource
    private ClubPointIncentiveService incentiveService;

    @PostMapping("/clear")
    @Operation(summary = "年度清零")
    @PreAuthorize("@ss.hasPermission('clubpoints:annual:clear')")
    public CommonResult<AdminAnnualClearRespVO> clearAnnualPoints(@RequestBody @Valid AdminAnnualClearReqVO reqVO) {
        ClubPointAnnualClearResultBO result = clearingService.clearAll(new ClubPointAnnualClearAllReqBO()
                .setYear(reqVO.getYear())
                .setOperatorUserId(getLoginUserId())
                .setReason(reqVO.getReason()));
        return success(new AdminAnnualClearRespVO()
                .setTotalCount(result.getTotalCount())
                .setSuccessCount(result.getSuccessCount())
                .setSkipCount(result.getSkipCount())
                .setFailedCount(result.getFailedCount()));
    }

    @GetMapping("/clearing-record-page")
    @Operation(summary = "年度清零记录分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:annual:query')")
    public CommonResult<PageResult<AdminAnnualClearingRecordRespVO>> getClearingRecordPage(
            @Valid AdminAnnualClearingRecordPageReqVO pageReqVO) {
        PageResult<ClubPointAnnualClearingRecordDO> pageResult = clearingService.getClearingRecordPage(
                pageReqVO, pageReqVO.getYear(), pageReqVO.getUserId(), pageReqVO.getStatus());
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointAnnualAdminController::toClearingRecordResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @PostMapping("/ranking-generate")
    @Operation(summary = "生成年度排名")
    @PreAuthorize("@ss.hasPermission('clubpoints:annual:manage')")
    public CommonResult<Boolean> generateRanking(@RequestBody @Valid AdminAnnualRankingGenerateReqVO reqVO) {
        rankingService.generateRanking(new ClubPointAnnualRankingGenerateReqBO()
                .setYear(reqVO.getYear())
                .setGeneratedTime(LocalDateTime.now()));
        return success(true);
    }

    @GetMapping("/ranking-page")
    @Operation(summary = "年度排名分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:annual:query')")
    public CommonResult<PageResult<AdminAnnualRankingRespVO>> getRankingPage(
            @Valid AdminAnnualRankingPageReqVO pageReqVO) {
        PageResult<ClubPointAnnualRankingRecordDO> pageResult =
                rankingService.getRankingPage(pageReqVO, pageReqVO.getYear());
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointAnnualAdminController::toRankingResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @PostMapping("/incentive-suggest")
    @Operation(summary = "生成激励建议")
    @PreAuthorize("@ss.hasPermission('clubpoints:annual:manage')")
    public CommonResult<Integer> suggestIncentives(@RequestBody @Valid AdminAnnualIncentiveSuggestReqVO reqVO) {
        return success(incentiveService.generateRankingIncentives(new ClubPointIncentiveSuggestReqBO()
                .setYear(reqVO.getYear())
                .setOperatorGlobalScope(true)));
    }

    @PostMapping("/incentive-confirm")
    @Operation(summary = "确认激励")
    @PreAuthorize("@ss.hasPermission('clubpoints:annual:manage')")
    public CommonResult<Boolean> confirmIncentive(@RequestBody @Valid AdminAnnualIncentiveOperationReqVO reqVO) {
        incentiveService.confirmIncentive(buildIncentiveOperationReqBO(reqVO));
        return success(true);
    }

    @PostMapping("/incentive-cancel")
    @Operation(summary = "取消激励")
    @PreAuthorize("@ss.hasPermission('clubpoints:annual:manage')")
    public CommonResult<Boolean> cancelIncentive(@RequestBody @Valid AdminAnnualIncentiveOperationReqVO reqVO) {
        incentiveService.cancelIncentive(buildIncentiveOperationReqBO(reqVO));
        return success(true);
    }

    private static ClubPointIncentiveOperationReqBO buildIncentiveOperationReqBO(
            AdminAnnualIncentiveOperationReqVO reqVO) {
        return new ClubPointIncentiveOperationReqBO()
                .setId(reqVO.getId())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setOperationTime(LocalDateTime.now())
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
    }

    private static AdminAnnualClearingRecordRespVO toClearingRecordResp(ClubPointAnnualClearingRecordDO record) {
        return new AdminAnnualClearingRecordRespVO()
                .setId(record.getId())
                .setYear(record.getYear())
                .setUserId(record.getUserId())
                .setNetPointsBefore(record.getNetPointsBefore())
                .setFrozenPointsBefore(record.getFrozenPointsBefore())
                .setAvailablePointsBefore(record.getAvailablePointsBefore())
                .setClearablePoints(record.getClearablePoints())
                .setClearTransactionId(record.getClearTransactionId())
                .setStatus(record.getStatus())
                .setRunId(record.getRunId())
                .setClearTime(record.getClearTime())
                .setErrorMessage(record.getErrorMessage());
    }

    private static AdminAnnualRankingRespVO toRankingResp(ClubPointAnnualRankingRecordDO record) {
        return new AdminAnnualRankingRespVO()
                .setId(record.getId())
                .setYear(record.getYear())
                .setClubId(record.getClubId())
                .setClubCodeSnapshot(record.getClubCodeSnapshot())
                .setClubNameSnapshot(record.getClubNameSnapshot())
                .setActivityPoints(record.getActivityPoints())
                .setContributionPoints(record.getContributionPoints())
                .setRewardPoints(record.getRewardPoints())
                .setReversedPoints(record.getReversedPoints())
                .setTotalIssuedPoints(record.getTotalIssuedPoints())
                .setRankNo(record.getRankNo())
                .setIncentiveAmountCent(record.getIncentiveAmountCent())
                .setConfirmStatus(record.getConfirmStatus())
                .setGeneratedTime(record.getGeneratedTime());
    }

}
