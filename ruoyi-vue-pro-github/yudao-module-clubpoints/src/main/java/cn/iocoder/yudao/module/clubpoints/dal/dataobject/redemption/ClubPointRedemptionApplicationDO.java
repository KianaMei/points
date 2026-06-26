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
 * Redemption application DO.
 */
@TableName("club_points_redemption_application")
@KeySequence("club_points_redemption_application_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRedemptionApplicationDO extends BaseDO {

    @TableId
    private Long id;
    private String applicationNo;
    private String requestNo;
    private Long batchId;
    private Long giftId;
    private Long eligibilitySnapshotId;
    private Long userId;
    private Integer status;
    private Integer pointsCost;
    private Integer quantity;
    private Long freezeId;
    private Long stockLockId;
    private Long deductTransactionId;
    private Integer qualificationRankSnapshot;
    private Integer beforeNetPoints;
    private Integer beforeFrozenPoints;
    private Integer beforeAvailablePoints;
    private Integer afterNetPoints;
    private Integer afterFrozenPoints;
    private Integer afterAvailablePoints;
    private String batchSnapshotJson;
    private String giftSnapshotJson;
    private LocalDateTime applyTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private Long reviewerUserId;
    private LocalDateTime reviewTime;
    private String reviewReason;
    private LocalDateTime directIssueTime;
    private String idempotencyKey;

}
