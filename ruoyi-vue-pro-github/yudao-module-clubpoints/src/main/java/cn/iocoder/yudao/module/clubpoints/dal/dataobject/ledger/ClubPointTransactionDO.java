package cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 积分流水事实源 DO
 */
@TableName("club_points_transaction")
@KeySequence("club_points_transaction_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointTransactionDO extends BaseDO {

    @TableId
    private Long id;
    private String transactionNo;
    private Long userId;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private Integer direction;
    private Integer points;
    private Integer pointCategory;
    private String pointTypeCode;
    private Integer status;
    private Integer sourceType;
    private Long sourceId;
    private Long sourceItemId;
    private String sourceTitleSnapshot;
    private Long issuingClubId;
    private String issuingClubCodeSnapshot;
    private String issuingClubNameSnapshot;
    private Long activityId;
    private String activityTitleSnapshot;
    private LocalDate activityDateSnapshot;
    private Long ruleVersionId;
    private Long ruleItemId;
    private String ruleItemCodeSnapshot;
    private String ruleSnapshotJson;
    private Integer evidenceType;
    private String materialSummary;
    private String reason;
    private LocalDateTime occurredAt;
    private Integer businessYear;
    private Integer businessMonth;
    private String idempotencyKey;
    private Long reverseOfTransactionId;
    private Long operatorUserId;
    private Long auditLogId;
    private String snapshotJson;

}
