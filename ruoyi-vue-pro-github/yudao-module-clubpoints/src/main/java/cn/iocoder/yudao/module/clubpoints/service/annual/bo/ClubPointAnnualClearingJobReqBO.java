package cn.iocoder.yudao.module.clubpoints.service.annual.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 年度清零 Job 请求
 */
@Data
@Accessors(chain = true)
public class ClubPointAnnualClearingJobReqBO {

    private Integer year;
    private String runKey;
    private List<Long> userIds;
    private Integer triggerSource;
    private Integer retryCount;
    private Long handlerUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private LocalDateTime plannedTime;
    private LocalDateTime clearTime;
    private String manualHandleReason;
    private String clientIp;
    private String userAgent;

}
