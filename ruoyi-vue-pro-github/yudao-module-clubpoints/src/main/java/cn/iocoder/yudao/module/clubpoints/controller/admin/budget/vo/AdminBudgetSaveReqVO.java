package cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class AdminBudgetSaveReqVO {

    private Long id;
    @NotNull
    private Integer category;
    @NotNull
    private Long budgetAmountCent;
    private Long actualAmountCent;
    private LocalDate occurDate;
    private Long handlerUserId;
    private Integer sourceType;
    private Long sourceId;
    private String description;
    private String remark;
    private String reason;

}
