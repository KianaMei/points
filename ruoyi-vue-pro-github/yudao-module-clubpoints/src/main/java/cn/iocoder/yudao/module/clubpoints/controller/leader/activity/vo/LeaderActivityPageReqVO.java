package cn.iocoder.yudao.module.clubpoints.controller.leader.activity.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

/**
 * 负责人活动分页请求
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LeaderActivityPageReqVO extends PageParam {

    @NotNull
    private Long clubId;
    private String keyword;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
