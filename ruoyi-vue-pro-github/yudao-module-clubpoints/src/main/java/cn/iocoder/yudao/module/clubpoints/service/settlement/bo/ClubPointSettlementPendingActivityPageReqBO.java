package cn.iocoder.yudao.module.clubpoints.service.settlement.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointSettlementPendingActivityPageReqBO extends PageParam {

    private Long clubId;
    private String keyword;
    private String clubName;
    private String activityTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
