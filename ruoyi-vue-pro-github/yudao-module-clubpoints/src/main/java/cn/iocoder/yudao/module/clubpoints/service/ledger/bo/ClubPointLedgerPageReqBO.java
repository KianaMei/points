package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分流水分页查询 BO
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointLedgerPageReqBO extends PageParam {

    private Long userId;
    private Long clubId;
    private Integer direction;
    private Integer pointCategory;
    private Integer sourceType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
