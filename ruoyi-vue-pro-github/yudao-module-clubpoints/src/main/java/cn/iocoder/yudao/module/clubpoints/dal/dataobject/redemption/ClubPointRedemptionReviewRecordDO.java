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
 * Redemption review record DO.
 */
@TableName("club_points_redemption_review_record")
@KeySequence("club_points_redemption_review_record_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRedemptionReviewRecordDO extends BaseDO {

    @TableId
    private Long id;
    private Long applicationId;
    private Long reviewerUserId;
    private Integer result;
    private String reason;
    private LocalDateTime reviewTime;
    private String applicationSnapshotJson;
    private String freezeSnapshotJson;
    private String stockSnapshotJson;
    private Long auditLogId;

}
