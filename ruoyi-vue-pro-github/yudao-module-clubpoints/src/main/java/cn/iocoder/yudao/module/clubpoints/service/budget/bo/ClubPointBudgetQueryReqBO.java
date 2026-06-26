package cn.iocoder.yudao.module.clubpoints.service.budget.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 预算记录查询请求
 */
@Data
@Accessors(chain = true)
public class ClubPointBudgetQueryReqBO {

    private Integer year;
    private Integer category;
    private Integer sourceType;
    private Long sourceId;
    private Boolean operatorGlobalScope;

}
