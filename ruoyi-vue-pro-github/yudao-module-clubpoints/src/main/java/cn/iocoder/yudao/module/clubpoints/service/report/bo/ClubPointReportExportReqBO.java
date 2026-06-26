package cn.iocoder.yudao.module.clubpoints.service.report.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 报表导出请求。
 */
@Data
@Accessors(chain = true)
public class ClubPointReportExportReqBO {

    private Integer reportType;
    private Long userId;
    private Long clubId;
    private Integer year;
    private Integer direction;
    private Integer pointCategory;
    private Integer sourceType;
    private Integer status;
    private Integer category;
    private Long sourceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private LocalDateTime operationTime;
    private String clientIp;
    private String userAgent;
    private String reason;

}
