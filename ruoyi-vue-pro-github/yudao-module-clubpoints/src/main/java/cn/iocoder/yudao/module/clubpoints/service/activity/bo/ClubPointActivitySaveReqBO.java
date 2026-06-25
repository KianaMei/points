package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubOperationReq;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动保存参数
 */
@Data
@Accessors(chain = true)
public class ClubPointActivitySaveReqBO implements ClubPointClubOperationReq {

    private Long id;
    private Long clubId;
    private String title;
    private String location;
    private String description;
    private Long coverFileId;
    private Integer level;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationDeadline;
    private LocalDateTime cancelDeadlineTime;
    private LocalDateTime checkinStartTime;
    private LocalDateTime checkinEndTime;
    private Integer checkoutMode;
    private LocalDateTime checkoutStartTime;
    private LocalDateTime checkoutEndTime;
    private Long ruleVersionId;
    private Integer basePoints;
    private Integer fullExtraPoints;
    private String remark;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;

}
