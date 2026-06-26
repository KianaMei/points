package cn.iocoder.yudao.module.clubpoints.controller.admin.dispute;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dispute.vo.AdminDisputeHandleReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dispute.vo.AdminDisputePageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dispute.vo.AdminDisputeRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.dispute.ClubPointDisputeDO;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeRelatedActionTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.dispute.ClubPointDisputeService;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerAdjustReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;
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

@Tag(name = "管理后台 - 积分异议")
@RestController
@RequestMapping("/clubpoints/dispute")
@Validated
public class ClubPointDisputeAdminController {

    @Resource
    private ClubPointDisputeService disputeService;

    @GetMapping("/page")
    @Operation(summary = "积分异议分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:dispute:handle')")
    public CommonResult<PageResult<AdminDisputeRespVO>> getDisputePage(@Valid AdminDisputePageReqVO pageReqVO) {
        PageResult<ClubPointDisputeDO> pageResult = disputeService.getAdminDisputePage(pageReqVO,
                pageReqVO.getUserId(), pageReqVO.getStatus(), pageReqVO.getTargetType(), pageReqVO.getTargetId(), true);
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointDisputeAdminController::toResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @PostMapping("/handle")
    @Operation(summary = "处理积分异议")
    @PreAuthorize("@ss.hasPermission('clubpoints:dispute:handle')")
    public CommonResult<Boolean> handleDispute(@RequestBody @Valid AdminDisputeHandleReqVO reqVO) {
        disputeService.handleDispute(buildHandleReqBO(reqVO));
        return success(true);
    }

    private static ClubPointDisputeHandleReqBO buildHandleReqBO(AdminDisputeHandleReqVO reqVO) {
        Long operatorUserId = getLoginUserId();
        String operatorName = getLoginUserNickname();
        LocalDateTime operationTime = LocalDateTime.now();
        ClubPointDisputeHandleReqBO reqBO = new ClubPointDisputeHandleReqBO()
                .setId(reqVO.getId())
                .setReplyContent(reqVO.getReplyContent())
                .setRelatedActionType(reqVO.getRelatedActionType())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot(operatorName)
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason())
                .setHandleTime(operationTime);
        if (ClubPointDisputeRelatedActionTypeEnum.REVERSE.getType().equals(reqVO.getRelatedActionType())) {
            reqBO.setReverseReqBO(buildReverseReqBO(reqVO, operatorUserId, operatorName, operationTime));
        } else if (ClubPointDisputeRelatedActionTypeEnum.ADJUSTMENT.getType().equals(reqVO.getRelatedActionType())) {
            reqBO.setAdjustReqBO(buildAdjustReqBO(reqVO, operatorUserId, operatorName, operationTime));
        }
        return reqBO;
    }

    private static ClubPointLedgerReverseReqBO buildReverseReqBO(AdminDisputeHandleReqVO reqVO, Long operatorUserId,
                                                                String operatorName, LocalDateTime operationTime) {
        return new ClubPointLedgerReverseReqBO()
                .setSourceTransactionId(reqVO.getRelatedTransactionId())
                .setTransactionNo(reqVO.getTransactionNo() == null
                        ? "DISPUTE-REV-" + reqVO.getRelatedTransactionId() : reqVO.getTransactionNo())
                .setReason(reqVO.getReason())
                .setOccurredAt(reqVO.getOccurredAt() == null ? operationTime : reqVO.getOccurredAt())
                .setAttachmentSnapshotJson(reqVO.getAttachmentSnapshotJson())
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot(operatorName)
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent());
    }

    private static ClubPointLedgerAdjustReqBO buildAdjustReqBO(AdminDisputeHandleReqVO reqVO, Long operatorUserId,
                                                              String operatorName, LocalDateTime operationTime) {
        return new ClubPointLedgerAdjustReqBO()
                .setRequestNo(reqVO.getRequestNo())
                .setTransactionNo(reqVO.getTransactionNo())
                .setUserId(reqVO.getUserId())
                .setUserNameSnapshot(reqVO.getUserNameSnapshot())
                .setDeptIdSnapshot(reqVO.getDeptIdSnapshot())
                .setDeptNameSnapshot(reqVO.getDeptNameSnapshot())
                .setAdjustType(reqVO.getAdjustType())
                .setDirection(reqVO.getDirection())
                .setPoints(reqVO.getPoints())
                .setIssuingClubId(reqVO.getIssuingClubId())
                .setIssuingClubCodeSnapshot(reqVO.getIssuingClubCodeSnapshot())
                .setIssuingClubNameSnapshot(reqVO.getIssuingClubNameSnapshot())
                .setRuleVersionId(reqVO.getRuleVersionId())
                .setRuleItemCode(reqVO.getRuleItemCode())
                .setReason(reqVO.getReason())
                .setMaterialSummary(reqVO.getMaterialSummary())
                .setAttachmentSnapshotJson(reqVO.getAttachmentSnapshotJson())
                .setOccurredAt(reqVO.getOccurredAt() == null ? operationTime : reqVO.getOccurredAt())
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot(operatorName)
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent());
    }

    private static AdminDisputeRespVO toResp(ClubPointDisputeDO dispute) {
        return new AdminDisputeRespVO()
                .setId(dispute.getId())
                .setUserId(dispute.getUserId())
                .setTargetType(dispute.getTargetType())
                .setTargetId(dispute.getTargetId())
                .setContent(dispute.getContent())
                .setStatus(dispute.getStatus())
                .setReplyContent(dispute.getReplyContent())
                .setRelatedActionType(dispute.getRelatedActionType())
                .setRelatedTransactionId(dispute.getRelatedTransactionId())
                .setCreatedTime(dispute.getSubmitTime())
                .setHandledTime(dispute.getHandleTime());
    }

}
