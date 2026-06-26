package cn.iocoder.yudao.module.clubpoints.service.budget.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预算记录保存请求
 */
@Data
@Accessors(chain = true)
public class ClubPointBudgetSaveReqBO {

    private Long id;
    private Integer category;
    private Long budgetAmountCent;
    private Long actualAmountCent;
    private LocalDate occurDate;
    private Long handlerUserId;
    private Integer sourceType;
    private Long sourceId;
    private String description;
    private String remark;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private LocalDateTime operationTime;
    private String clientIp;
    private String userAgent;
    private String reason;

}
