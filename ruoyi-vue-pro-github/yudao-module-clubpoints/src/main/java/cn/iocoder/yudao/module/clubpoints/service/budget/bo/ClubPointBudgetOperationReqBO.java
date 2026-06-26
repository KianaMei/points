package cn.iocoder.yudao.module.clubpoints.service.budget.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 预算记录操作请求
 */
@Data
@Accessors(chain = true)
public class ClubPointBudgetOperationReqBO {

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
