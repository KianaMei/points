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
 * 活动报名记录 DO
 */
@TableName("club_points_activity_registration")
@KeySequence("club_points_activity_registration_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointActivityRegistrationDO extends BaseDO {

    @TableId
    private Long id;
    private Long activityId;
    private Long clubId;
    private Long userId;
    private Integer status;
    private LocalDateTime registerTime;
    private LocalDateTime cancelTime;
    private Integer cancelReasonType;
    private String cancelReason;
    private Long cancelOperatorUserId;
    private Boolean noAbsenceDeduct;
    private Boolean specialAbsenceFlag;
    private String specialAbsenceReason;
    private LocalDateTime specialAbsenceTime;
    private Long specialAbsenceOperatorId;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private String mobileSnapshot;
    private String clubNameSnapshot;
    private String activityTitleSnapshot;
    private LocalDateTime activityStartTimeSnapshot;
    private LocalDateTime activityEndTimeSnapshot;
    private String activeUniqueKey;

}
