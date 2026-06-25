package cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

/**
 * 管理员修正签到签退请求
 */
@Data
@Accessors(chain = true)
public class AdminAttendanceCorrectReqVO {

    @NotNull
    private Long attendanceRecordId;
    @NotNull
    private LocalDateTime newRecordTime;
    private String reason;

}
