package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 预算统计报表分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminReportBudgetPageReqVO extends PageParam {

    private Integer year;
    private Integer category;
    private Integer sourceType;
    private Long sourceId;

}
