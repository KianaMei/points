package cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminBudgetPageReqVO extends PageParam {

    private Integer year;
    private Integer category;
    private Integer sourceType;
    private Long sourceId;

}
