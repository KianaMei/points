package cn.iocoder.yudao.module.clubpoints.controller.admin.contribution;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionDirectCreateReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionFraudHandleReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionItemRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionReviewPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionReviewReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionViolationDeductReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.ClubPointContributionService;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionDetailBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionDirectCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionFraudHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionViolationDeductReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 非签到积分材料")
@RestController
@RequestMapping("/clubpoints/contribution")
@Validated
public class ClubPointContributionAdminController {

    @Resource
    private ClubPointContributionService contributionService;

    @GetMapping("/review-page")
    @Operation(summary = "待审核材料分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:review')")
    public CommonResult<PageResult<AdminContributionRespVO>> getReviewPage(
            @Valid AdminContributionReviewPageReqVO pageReqVO) {
        PageResult<ClubPointContributionMaterialDO> pageResult = contributionService.getAdminReviewPage(
                true, toPageReqBO(pageReqVO));
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointContributionAdminController::toResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "材料详情")
    @Parameter(name = "id", description = "材料 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:review')")
    public CommonResult<AdminContributionRespVO> getContribution(@RequestParam("id") Long id) {
        return success(toResp(contributionService.getAdminMaterial(true, id)));
    }

    @PostMapping("/review")
    @Operation(summary = "审核材料")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:review')")
    public CommonResult<Boolean> reviewContribution(@RequestBody @Valid AdminContributionReviewReqVO reqVO) {
        contributionService.reviewMaterial(new ClubPointContributionReviewReqBO()
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

    @PostMapping("/direct-create")
    @Operation(summary = "管理员代录积分")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:direct-create')")
    public CommonResult<Long> directCreate(@RequestBody @Valid AdminContributionDirectCreateReqVO reqVO) {
        return success(contributionService.directCreate(new ClubPointContributionDirectCreateReqBO()
                .setRequestNo(reqVO.getRequestNo())
                .setClubId(reqVO.getClubId())
                .setType(reqVO.getType())
                .setUserId(reqVO.getUserId())
                .setUserNameSnapshot(reqVO.getUserNameSnapshot())
                .setDeptNameSnapshot(reqVO.getDeptNameSnapshot())
                .setPoints(reqVO.getPoints())
                .setRuleVersionId(reqVO.getRuleVersionId())
                .setReason(reqVO.getReason())
                .setAttachments(toAttachmentBindReqBOs(reqVO.getAttachments()))
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())));
    }

    @PostMapping("/violation-deduct")
    @Operation(summary = "管理员违规扣分")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:violation-deduct')")
    public CommonResult<Long> violationDeduct(@RequestBody @Valid AdminContributionViolationDeductReqVO reqVO) {
        return success(contributionService.violationDeduct(new ClubPointContributionViolationDeductReqBO()
                .setRequestNo(reqVO.getRequestNo())
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId())
                .setUserNameSnapshot(reqVO.getUserNameSnapshot())
                .setDeptNameSnapshot(reqVO.getDeptNameSnapshot())
                .setPoints(reqVO.getPoints())
                .setRuleVersionId(reqVO.getRuleVersionId())
                .setReason(reqVO.getReason())
                .setAttachments(toAttachmentBindReqBOs(reqVO.getAttachments()))
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())));
    }

    @PostMapping("/fraud-handle")
    @Operation(summary = "管理员弄虚作假处理")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:fraud-handle')")
    public CommonResult<Long> handleFraud(@RequestBody @Valid AdminContributionFraudHandleReqVO reqVO) {
        return success(contributionService.handleFraud(new ClubPointContributionFraudHandleReqBO()
                .setRequestNo(reqVO.getRequestNo())
                .setOriginalMaterialId(reqVO.getOriginalMaterialId())
                .setRuleVersionId(reqVO.getRuleVersionId())
                .setReason(reqVO.getReason())
                .setAttachments(toAttachmentBindReqBOs(reqVO.getAttachments()))
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())));
    }

    private static ClubPointContributionPageReqBO toPageReqBO(AdminContributionReviewPageReqVO reqVO) {
        ClubPointContributionPageReqBO reqBO = new ClubPointContributionPageReqBO()
                .setClubId(reqVO.getClubId())
                .setType(reqVO.getType());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static List<ClubAttachmentBindReqBO> toAttachmentBindReqBOs(List<AttachmentInputVO> attachments) {
        return attachments == null ? null : attachments.stream()
                .map(attachment -> new ClubAttachmentBindReqBO()
                        .setAttachmentType(attachment.getType())
                        .setFileId(attachment.getFileId())
                        .setUrl(attachment.getUrl())
                        .setName(attachment.getName())
                        .setRemark(attachment.getRemark()))
                .collect(Collectors.toList());
    }

    private static AdminContributionRespVO toResp(ClubPointContributionDetailBO detailBO) {
        return toResp(detailBO.getMaterial())
                .setItems(detailBO.getItems().stream()
                        .map(ClubPointContributionAdminController::toItemResp)
                        .collect(Collectors.toList()));
    }

    private static AdminContributionRespVO toResp(ClubPointContributionMaterialDO material) {
        return new AdminContributionRespVO()
                .setId(material.getId())
                .setClubId(material.getClubId())
                .setClubNameSnapshot(material.getClubNameSnapshot())
                .setType(material.getType())
                .setTitle(material.getTitle())
                .setDescription(material.getDescription())
                .setStatus(material.getStatus())
                .setRuleVersionId(material.getRuleVersionId())
                .setSubmitterUserId(material.getSubmitterUserId())
                .setSubmitTime(material.getSubmitTime())
                .setReviewerUserId(material.getReviewerUserId())
                .setReviewTime(material.getReviewTime())
                .setReviewReason(material.getReviewReason())
                .setLocked(material.getLocked())
                .setDirectCreated(material.getDirectCreated())
                .setRequestNo(material.getRequestNo());
    }

    private static AdminContributionItemRespVO toItemResp(ClubPointContributionItemDO item) {
        return new AdminContributionItemRespVO()
                .setId(item.getId())
                .setMaterialId(item.getMaterialId())
                .setClubId(item.getClubId())
                .setUserId(item.getUserId())
                .setUserNameSnapshot(item.getUserNameSnapshot())
                .setDeptNameSnapshot(item.getDeptNameSnapshot())
                .setPointCategory(item.getPointCategory())
                .setRuleItemId(item.getRuleItemId())
                .setRuleItemCode(item.getRuleItemCode())
                .setDirection(item.getDirection())
                .setPoints(item.getPoints())
                .setReason(item.getReason())
                .setMaterialSummary(item.getMaterialSummary())
                .setDutyMonth(item.getDutyMonth())
                .setRecommendedUserId(item.getRecommendedUserId())
                .setAwardLevel(item.getAwardLevel())
                .setApprovalResultSnapshot(item.getApprovalResultSnapshot())
                .setTransactionId(item.getTransactionId());
    }

}
