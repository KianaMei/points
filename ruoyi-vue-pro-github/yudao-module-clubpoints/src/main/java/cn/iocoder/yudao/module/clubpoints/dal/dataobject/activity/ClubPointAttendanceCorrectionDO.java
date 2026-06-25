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
 * 签到签退补录修正记录 DO
 */
@TableName("club_points_attendance_correction")
@KeySequence("club_points_attendance_correction_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointAttendanceCorrectionDO extends BaseDO {

    @TableId
    private Long id;
    private Long attendanceRecordId;
    private Long registrationId;
    private Long activityId;
    private Long userId;
    private Integer targetType;
    private Integer correctionType;
    private LocalDateTime beforeRecordTime;
    private LocalDateTime afterRecordTime;
    private String reason;
    private Long operatorUserId;
    private Long auditLogId;

}
