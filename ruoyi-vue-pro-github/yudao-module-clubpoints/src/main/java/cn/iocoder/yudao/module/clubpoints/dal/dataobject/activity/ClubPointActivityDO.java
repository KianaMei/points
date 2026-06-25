package cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动 DO
 */
@TableName("club_points_activity")
@KeySequence("club_points_activity_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointActivityDO extends BaseDO {

    @TableId
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
    private LocalDateTime submitTime;
    private LocalDateTime publishTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String snapshotJson;
    private String remark;

}
