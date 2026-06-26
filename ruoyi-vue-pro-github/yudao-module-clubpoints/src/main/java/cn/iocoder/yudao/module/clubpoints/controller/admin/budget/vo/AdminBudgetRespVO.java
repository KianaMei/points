package cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class AdminBudgetRespVO {

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

}
