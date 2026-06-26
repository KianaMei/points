package cn.iocoder.yudao.module.clubpoints.service.dispute.bo;

import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerAdjustReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 管理员处理异议参数
 */
@Data
@Accessors(chain = true)
public class ClubPointDisputeHandleReqBO {

    private Long id;
    private String replyContent;
    private Integer relatedActionType;
    private ClubPointLedgerAdjustReqBO adjustReqBO;
    private ClubPointLedgerReverseReqBO reverseReqBO;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;
    private LocalDateTime handleTime;

}
