package cn.iocoder.yudao.module.clubpoints.controller.leader.registration.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 负责人端报名响应
 */
@Data
@Accessors(chain = true)
public class LeaderRegistrationRespVO {

    private Long id;
    private Long activityId;
    private Long clubId;
    private Long userId;
    private Integer status;
    private LocalDateTime registerTime;
    private Integer cancelReasonType;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private String mobileSnapshot;
    private String clubNameSnapshot;
    private String activityTitleSnapshot;
    private LocalDateTime activityStartTimeSnapshot;
    private LocalDateTime activityEndTimeSnapshot;
    private Boolean noAbsenceDeduct;
    private Boolean specialAbsenceFlag;

}
