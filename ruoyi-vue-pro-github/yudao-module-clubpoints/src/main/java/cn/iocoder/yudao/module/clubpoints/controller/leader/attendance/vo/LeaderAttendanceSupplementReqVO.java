package cn.iocoder.yudao.module.clubpoints.controller.leader.attendance.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 负责人端补录签到签退请求
 */
@Data
@Accessors(chain = true)
public class LeaderAttendanceSupplementReqVO {

    @NotNull
    private Long registrationId;

    @NotNull
    private Integer targetType;

    @NotNull
    private LocalDateTime occurTime;

    private String reason;

}
