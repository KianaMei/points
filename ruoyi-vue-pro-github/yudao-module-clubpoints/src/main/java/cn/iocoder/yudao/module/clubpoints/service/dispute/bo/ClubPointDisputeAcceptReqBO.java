package cn.iocoder.yudao.module.clubpoints.service.dispute.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 管理员受理异议参数
 */
@Data
@Accessors(chain = true)
public class ClubPointDisputeAcceptReqBO {

    private Long id;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;
    private LocalDateTime handleTime;

}
