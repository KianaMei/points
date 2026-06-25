package cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 员工活动分页请求
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AppActivityPageReqVO extends PageParam {

    private Long clubId;
    private String keyword;
    private Integer activityStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
