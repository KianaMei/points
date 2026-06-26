package cn.iocoder.yudao.module.clubpoints.service.annual.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 全量年度清零请求
 */
@Data
@Accessors(chain = true)
public class ClubPointAnnualClearAllReqBO {

    private Integer year;
    private Long runId;
    private LocalDateTime clearTime;
    private Long operatorUserId;
    private String reason;

}
