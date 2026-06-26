package cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 非签到积分材料 DO
 */
@TableName("club_points_contribution_material")
@KeySequence("club_points_contribution_material_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointContributionMaterialDO extends BaseDO {

    @TableId
    private Long id;
    private Long clubId;
    private String clubNameSnapshot;
    private Integer type;
    private String title;
    private String description;
    private Integer status;
    private Long ruleVersionId;
    private Long submitterUserId;
    private LocalDateTime submitTime;
    private Long reviewerUserId;
    private LocalDateTime reviewTime;
    private String reviewReason;
    private Boolean locked;
    private Boolean directCreated;
    private String requestNo;
    private String snapshotJson;

}
