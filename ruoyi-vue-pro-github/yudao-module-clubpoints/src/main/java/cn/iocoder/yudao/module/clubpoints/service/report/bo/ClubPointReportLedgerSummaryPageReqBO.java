package cn.iocoder.yudao.module.clubpoints.service.report.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointReportLedgerSummaryPageReqBO extends PageParam {

    private Long userId;
    private Long clubId;
    private Integer year;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
