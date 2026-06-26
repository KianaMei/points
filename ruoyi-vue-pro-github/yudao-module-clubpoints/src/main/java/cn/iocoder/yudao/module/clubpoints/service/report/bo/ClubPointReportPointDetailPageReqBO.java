package cn.iocoder.yudao.module.clubpoints.service.report.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointReportPointDetailPageReqBO extends PageParam {

    private Long userId;
    private Long clubId;
    private Integer year;
    private Integer direction;
    private Integer pointCategory;
    private Integer sourceType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
