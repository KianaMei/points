package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动查询结果
 */
@Data
@Accessors(chain = true)
public class ClubPointActivityInfoBO {

    private Long id;
    private Long clubId;
    private String clubCodeSnapshot;
    private String clubNameSnapshot;
    private String title;
    private String location;
    private String description;
    private Long coverFileId;
    private Integer level;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationDeadline;
    private LocalDateTime cancelDeadlineTime;
    private LocalDateTime checkinStartTime;
    private LocalDateTime checkinEndTime;
    private Integer checkoutMode;
    private LocalDateTime checkoutStartTime;
    private LocalDateTime checkoutEndTime;
    private Long currentConfigVersionId;
    private Long creatorUserId;
    private Integer basePoints;
    private Integer fullExtraPoints;
    private Long registrationId;
    private Boolean registered;
    private Integer registrationStatus;
    private Integer checkInStatus;
    private Integer checkInSource;
    private LocalDateTime checkInTime;
    private Integer checkOutStatus;
    private Integer checkOutSource;
    private LocalDateTime checkOutTime;
    private Integer settlementStatus;

}
