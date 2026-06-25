package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动分页查询参数
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointActivityPageReqBO extends PageParam {

    private Long clubId;
    private String keyword;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
