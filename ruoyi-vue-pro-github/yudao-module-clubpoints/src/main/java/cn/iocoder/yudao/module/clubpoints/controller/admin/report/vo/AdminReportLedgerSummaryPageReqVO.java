package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分总台账报表分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminReportLedgerSummaryPageReqVO extends PageParam {

    private Long userId;
    private Long clubId;
    private Integer year;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
