package cn.iocoder.yudao.module.clubpoints.controller.admin.dispute.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminDisputeHandleReqVO {

    @NotNull
    private Long id;
    @NotBlank
    private String replyContent;
    private Integer relatedActionType;
    private Long relatedTransactionId;
    private String reason;

    private String requestNo;
    private String transactionNo;
    private Long userId;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private Integer adjustType;
    private Integer direction;
    private Integer points;
    private Long issuingClubId;
    private String issuingClubCodeSnapshot;
    private String issuingClubNameSnapshot;
    private Long ruleVersionId;
    private String ruleItemCode;
    private String materialSummary;
    private String attachmentSnapshotJson;
    private LocalDateTime occurredAt;

}
