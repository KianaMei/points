package cn.iocoder.yudao.module.clubpoints.service.annual.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 单用户年度清零请求
 */
@Data
@Accessors(chain = true)
public class ClubPointAnnualClearUserReqBO {

    private Integer year;
    private Long userId;
    private Long runId;
    private LocalDateTime clearTime;
    private Long operatorUserId;
    private String reason;

}
