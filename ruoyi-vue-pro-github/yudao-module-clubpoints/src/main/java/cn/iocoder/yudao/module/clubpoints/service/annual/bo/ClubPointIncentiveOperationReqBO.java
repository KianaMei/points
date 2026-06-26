package cn.iocoder.yudao.module.clubpoints.service.annual.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 激励确认或取消请求
 */
@Data
@Accessors(chain = true)
public class ClubPointIncentiveOperationReqBO {

    private Long id;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private LocalDateTime operationTime;
    private String clientIp;
    private String userAgent;
    private String reason;

}
