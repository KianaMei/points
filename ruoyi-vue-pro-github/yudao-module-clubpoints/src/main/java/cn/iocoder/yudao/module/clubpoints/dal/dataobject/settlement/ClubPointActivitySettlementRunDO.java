package cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动积分发放运行记录 DO
 */
@TableName("club_points_activity_settlement_run")
@KeySequence("club_points_activity_settlement_run_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointActivitySettlementRunDO extends BaseDO {

    @TableId
    private Long id;
    private Long activityId;
    private Long jobRunId;
    private String runKey;
    private Integer status;
    private LocalDateTime settlementTime;
    private Long configVersionId;
    private Integer registrationCount;
    private Integer successCount;
    private Integer skipCount;
    private Integer failedCount;
    private String errorMessage;
    private Integer triggerSource;
    private Long operatorUserId;

    @TableField(exist = false)
    private Long clubId;
    @TableField(exist = false)
    private String activityTitle;
    @TableField(exist = false)
    private String clubName;
    @TableField(exist = false)
    private LocalDateTime activityStartTime;
    @TableField(exist = false)
    private LocalDateTime activityEndTime;

}
