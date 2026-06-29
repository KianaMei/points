package cn.iocoder.yudao.module.clubpoints.controller.leader.registration.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 负责人端报名分页请求
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LeaderRegistrationPageReqVO extends PageParam {

    private Long clubId;
    private Long activityId;
    private Integer status;
    private Long userId;

}
