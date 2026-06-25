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
 * 活动审核记录 DO
 */
@TableName("club_points_activity_review_record")
@KeySequence("club_points_activity_review_record_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointActivityReviewRecordDO extends BaseDO {

    @TableId
    private Long id;
    private Long activityId;
    private Long reviewerUserId;
    private Integer result;
    private String reason;
    private LocalDateTime reviewTime;
    private String activitySnapshotJson;
    private Long auditLogId;

}
