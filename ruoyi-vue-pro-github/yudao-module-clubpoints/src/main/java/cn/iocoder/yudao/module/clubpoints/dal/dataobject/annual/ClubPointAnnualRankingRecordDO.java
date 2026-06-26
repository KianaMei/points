package cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual;

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
 * 俱乐部年度排名记录 DO
 */
@TableName("club_points_annual_ranking_record")
@KeySequence("club_points_annual_ranking_record_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointAnnualRankingRecordDO extends BaseDO {

    @TableId
    private Long id;
    @TableField("`year`")
    private Integer year;
    private Long clubId;
    private String clubCodeSnapshot;
    private String clubNameSnapshot;
    private Integer activityPoints;
    private Integer contributionPoints;
    private Integer rewardPoints;
    private Integer reversedPoints;
    private Integer totalIssuedPoints;
    private Integer rankNo;
    private Long incentiveAmountCent;
    private Integer confirmStatus;
    private Long confirmedBy;
    private LocalDateTime confirmedTime;
    private Long budgetRecordId;
    private LocalDateTime generatedTime;
    private String snapshotJson;

}
