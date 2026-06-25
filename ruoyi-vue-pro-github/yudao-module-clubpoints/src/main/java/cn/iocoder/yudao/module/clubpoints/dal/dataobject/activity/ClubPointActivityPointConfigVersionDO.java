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
 * 活动积分配置版本 DO
 */
@TableName("club_points_activity_point_config_version")
@KeySequence("club_points_activity_point_config_version_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointActivityPointConfigVersionDO extends BaseDO {

    @TableId
    private Long id;
    private Long activityId;
    private Integer versionNo;
    private Integer level;
    private Integer basePoints;
    private Integer fullExtraPoints;
    private Long ruleVersionId;
    private Long baseRuleItemId;
    private Long fullRuleItemId;
    private LocalDateTime effectiveTime;
    private String createdReason;
    private Boolean active;
    private String ruleSnapshotJson;

}
