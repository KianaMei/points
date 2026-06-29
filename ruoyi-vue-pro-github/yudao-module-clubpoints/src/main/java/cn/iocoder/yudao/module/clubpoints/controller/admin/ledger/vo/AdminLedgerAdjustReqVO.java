package cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo;

import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理后台积分调整请求
 */
@Data
@Accessors(chain = true)
public class AdminLedgerAdjustReqVO {

    @NotBlank
    private String requestNo;

    @NotNull
    private Long userId;

    private Integer adjustType;

    @NotNull
    private Integer direction;

    @NotNull
    private Integer points;

    @NotNull
    private Long ruleVersionId;

    @NotBlank
    private String ruleItemCode;

    private Long issuingClubId;

    @NotBlank
    private String reason;

    @Valid
    private List<AttachmentInputVO> attachments;

}
