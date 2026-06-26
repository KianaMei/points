package cn.iocoder.yudao.module.clubpoints.controller.app.dispute;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.dispute.vo.AppDisputeCreateReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.dispute.vo.AppDisputePageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.dispute.vo.AppDisputeRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.dispute.ClubPointDisputeDO;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.ClubPointDisputeService;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeSubmitReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "员工端 - 积分异议")
@RestController
@RequestMapping("/clubpoints/app/dispute")
@Validated
public class ClubPointDisputeAppController {

    @Resource
    private ClubPointDisputeService disputeService;

    @PostMapping("/create")
    @Operation(summary = "提交积分异议")
    public CommonResult<Long> createDispute(@RequestBody @Valid AppDisputeCreateReqVO reqVO) {
        return success(disputeService.submitDispute(new ClubPointDisputeSubmitReqBO()
                .setUserId(getLoginUserId())
                .setTitle(buildTitle(reqVO))
                .setContent(reqVO.getContent())
                .setTargetType(reqVO.getTargetType())
                .setTargetId(reqVO.getTargetId())
                .setAttachments(toAttachmentBindReqBOs(reqVO.getAttachments()))));
    }

    @GetMapping("/my-page")
    @Operation(summary = "我的积分异议分页")
    public CommonResult<PageResult<AppDisputeRespVO>> getMyPage(@Valid AppDisputePageReqVO pageReqVO) {
        PageResult<ClubPointDisputeDO> pageResult = disputeService.getMyDisputePage(
                getLoginUserId(), pageReqVO, pageReqVO.getStatus());
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointDisputeAppController::toResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "积分异议详情")
    @Parameter(name = "id", description = "异议 ID", required = true, example = "1")
    public CommonResult<AppDisputeRespVO> getDispute(@RequestParam("id") Long id) {
        return success(toResp(disputeService.getMyDispute(getLoginUserId(), id)));
    }

    private static String buildTitle(AppDisputeCreateReqVO reqVO) {
        return "积分异议-" + reqVO.getTargetType() + "-" + reqVO.getTargetId();
    }

    private static AppDisputeRespVO toResp(ClubPointDisputeDO dispute) {
        return new AppDisputeRespVO()
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

    private static List<ClubAttachmentBindReqBO> toAttachmentBindReqBOs(List<AttachmentInputVO> attachments) {
        if (attachments == null) {
            return null;
        }
        return attachments.stream()
                .map(attachment -> new ClubAttachmentBindReqBO()
                        .setAttachmentType(attachment.getType())
                        .setFileId(attachment.getFileId())
                        .setUrl(attachment.getUrl())
                        .setName(attachment.getName())
                        .setRemark(attachment.getRemark()))
                .collect(Collectors.toList());
    }

}
