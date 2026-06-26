package cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Redemption eligibility snapshot DO.
 */
@TableName("club_points_redemption_eligibility_snapshot")
@KeySequence("club_points_redemption_eligibility_snapshot_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRedemptionEligibilitySnapshotDO extends BaseDO {

    @TableId
    private Long id;
    private Long batchId;
    private Long userId;
    private String userNameSnapshot;
    private String deptNameSnapshot;
    private Integer netPointsSnapshot;
    private Integer frozenPointsSnapshot;
    private Integer availablePointsSnapshot;
    private Integer annualEarnedPointsSnapshot;
    private Integer rankNo;
    private Boolean qualified;
    private String qualificationReason;
    private Boolean tieAtCutoff;
    private LocalDateTime generatedTime;

}
