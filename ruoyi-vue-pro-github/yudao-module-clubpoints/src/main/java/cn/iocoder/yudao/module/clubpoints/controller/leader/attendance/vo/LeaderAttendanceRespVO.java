package cn.iocoder.yudao.module.clubpoints.controller.leader.attendance.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 负责人端签到签退响应
 */
@Data
@Accessors(chain = true)
public class LeaderAttendanceRespVO {

    private Long id;
    private Long registrationId;
    private Long activityId;
    private Long clubId;
    private Long userId;
    private String userNameSnapshot;
    private String deptNameSnapshot;
    private String clubNameSnapshot;
    private String activityTitleSnapshot;
    private Integer targetType;
    private LocalDateTime occurTime;
    private Integer sourceType;
    private String reason;

}
