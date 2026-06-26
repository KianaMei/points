package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 兑换记录报表分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminReportRedemptionPageReqVO extends PageParam {

    private Long batchId;
    private Long userId;
    private Integer status;
    private Integer year;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
