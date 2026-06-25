package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 员工自助签到签退参数
 */
@Data
@Accessors(chain = true)
public class ClubPointAttendanceSelfReqBO {

    private Long registrationId;
    private Long userId;
    private LocalDateTime recordTime;
    private String clientIp;
    private String remark;

}
