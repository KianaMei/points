package cn.iocoder.yudao.module.clubpoints.controller.leader.attendance.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 负责人端修正签到签退请求
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LeaderAttendanceCorrectReqVO extends LeaderAttendanceSupplementReqVO {

    @NotNull
    private Long id;

}
