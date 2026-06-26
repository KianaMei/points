package cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption;

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
 * Redemption batch DO.
 */
@TableName("club_points_redemption_batch")
@KeySequence("club_points_redemption_batch_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRedemptionBatchDO extends BaseDO {

    @TableId
    private Long id;
    @TableField("`year`")
    private Integer year;
    private String name;
    private Integer status;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private String description;
    private Integer minAvailablePoints;
    private Integer qualifiedCount;
    private Boolean includeTieAtCutoff;
    private String qualificationRuleJson;
    private Boolean snapshotGenerated;
    private LocalDateTime snapshotGeneratedTime;
    private Long ruleVersionId;
    private String ruleSnapshotJson;

}
