package cn.iocoder.yudao.module.clubpoints.controller.leader.attendance.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 负责人端签到签退分页请求
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LeaderAttendancePageReqVO extends PageParam {

    private Long clubId;
    private Long activityId;
    private Long registrationId;
    private Long userId;
    private Integer targetType;

}
