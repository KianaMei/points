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
 * 签到签退有效事实 DO
 */
@TableName("club_points_attendance_record")
@KeySequence("club_points_attendance_record_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointAttendanceRecordDO extends BaseDO {

    @TableId
    private Long id;
    private Long registrationId;
    private Long activityId;
    private Long userId;
    private Integer targetType;
    private LocalDateTime recordTime;
    private Integer sourceType;
    private Long operatorUserId;
    private String reason;
    private String clientIp;
    private String remark;

}
