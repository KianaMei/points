package cn.iocoder.yudao.module.clubpoints.controller.leader.activity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

/**
 * 负责人活动保存请求
 */
@Data
@Accessors(chain = true)
public class LeaderActivitySaveReqVO {

    private Long id;
    @NotNull
    private Long clubId;
    @NotNull
    private String title;
    private String location;
    private String description;
    private Long coverFileId;
    @NotNull
    private Integer level;
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    private LocalDateTime endTime;
    @NotNull
    private LocalDateTime registrationDeadline;
    private LocalDateTime cancelDeadlineTime;
    @NotNull
    private LocalDateTime checkinStartTime;
    @NotNull
    private LocalDateTime checkinEndTime;
    @NotNull
    private Integer checkoutMode;
    @NotNull
    private LocalDateTime checkoutStartTime;
    @NotNull
    private LocalDateTime checkoutEndTime;
    private Long ruleVersionId;
    @NotNull
    private Integer basePoints;
    private Integer fullExtraPoints;
    private String remark;
    private String reason;

}
