package cn.iocoder.yudao.module.clubpoints.controller.leader.contribution;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo.LeaderContributionItemReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo.LeaderContributionItemRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo.LeaderContributionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo.LeaderContributionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo.LeaderContributionSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.ClubPointContributionService;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionDetailBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionMaterialSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionSubmitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionWithdrawReqBO;
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
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "负责人端 - 非签到积分材料")
@RestController
@RequestMapping("/clubpoints/leader/contribution")
@Validated
public class ClubPointContributionLeaderController {

    @Resource
    private ClubPointContributionService contributionService;

    @GetMapping("/page")
    @Operation(summary = "材料分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:query')")
    public CommonResult<PageResult<LeaderContributionRespVO>> getContributionPage(
            @Valid LeaderContributionPageReqVO pageReqVO) {
        PageResult<ClubPointContributionMaterialDO> pageResult = contributionService.getLeaderMaterialPage(
                getLoginUserId(), toPageReqBO(pageReqVO));
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointContributionLeaderController::toResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "材料详情")
    @Parameter(name = "id", description = "材料 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:query')")
    public CommonResult<LeaderContributionRespVO> getContribution(@RequestParam("id") Long id) {
        return success(toResp(contributionService.getLeaderMaterial(getLoginUserId(), id)));
    }

    @PostMapping("/create")
    @Operation(summary = "保存材料草稿")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:submit')")
    public CommonResult<Long> createContribution(@RequestBody @Valid LeaderContributionSaveReqVO reqVO) {
        return success(contributionService.createDraft(toSaveReqBO(reqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "修改材料")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:submit')")
    public CommonResult<Boolean> updateContribution(@RequestBody @Valid LeaderContributionSaveReqVO reqVO) {
        contributionService.updateDraft(toSaveReqBO(reqVO));
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交材料")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:submit')")
    public CommonResult<Boolean> submitContribution(@RequestParam("id") Long id) {
        contributionService.submitForReview(new ClubPointContributionSubmitReqBO()
                .setId(id)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("leader")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason("提交非签到积分材料"));
        return success(true);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "撤回材料")
    @PreAuthorize("@ss.hasPermission('clubpoints:contribution:withdraw')")
    public CommonResult<Boolean> withdrawContribution(@RequestParam("id") Long id,
                                                      @RequestParam(value = "reason", required = false)
                                                      String reason) {
        contributionService.withdraw(new ClubPointContributionWithdrawReqBO()
                .setId(id)
                .setOperatorUserId(getLoginUserId())
                .setReason(reason));
        return success(true);
    }

    private static ClubPointContributionPageReqBO toPageReqBO(LeaderContributionPageReqVO reqVO) {
        ClubPointContributionPageReqBO reqBO = new ClubPointContributionPageReqBO()
                .setClubId(reqVO.getClubId())
                .setType(reqVO.getType())
                .setStatus(reqVO.getStatus());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointContributionMaterialSaveReqBO toSaveReqBO(LeaderContributionSaveReqVO reqVO) {
        return new ClubPointContributionMaterialSaveReqBO()
                .setId(reqVO.getId())
                .setClubId(reqVO.getClubId())
                .setType(reqVO.getType())
                .setTitle(reqVO.getTitle())
                .setDescription(reqVO.getDescription())
                .setRuleVersionId(reqVO.getRuleVersionId())
                .setItems(toItemSaveReqBOs(reqVO.getItems()))
                .setAttachments(toAttachmentBindReqBOs(reqVO.getAttachments()))
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("leader")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason("保存非签到积分材料");
    }

    private static List<ClubPointContributionItemSaveReqBO> toItemSaveReqBOs(
            List<LeaderContributionItemReqVO> items) {
        return items == null ? null : items.stream()
                .map(item -> new ClubPointContributionItemSaveReqBO()
                        .setUserId(item.getUserId())
                        .setUserNameSnapshot(item.getUserNameSnapshot())
                        .setDeptNameSnapshot(item.getDeptNameSnapshot())
                        .setPoints(item.getPoints())
                        .setReason(item.getReason())
                        .setMaterialSummary(item.getMaterialSummary())
                        .setDutyMonth(item.getDutyMonth())
                        .setRecommendedUserId(item.getRecommendedUserId())
                        .setAwardLevel(item.getAwardLevel())
                        .setApprovalResultSnapshot(item.getApprovalResultSnapshot()))
                .collect(Collectors.toList());
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

    private static LeaderContributionRespVO toResp(ClubPointContributionDetailBO detailBO) {
        return toResp(detailBO.getMaterial())
                .setItems(detailBO.getItems().stream()
                        .map(ClubPointContributionLeaderController::toItemResp)
                        .collect(Collectors.toList()));
    }

    private static LeaderContributionRespVO toResp(ClubPointContributionMaterialDO material) {
        return new LeaderContributionRespVO()
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

    private static LeaderContributionItemRespVO toItemResp(ClubPointContributionItemDO item) {
        return new LeaderContributionItemRespVO()
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
