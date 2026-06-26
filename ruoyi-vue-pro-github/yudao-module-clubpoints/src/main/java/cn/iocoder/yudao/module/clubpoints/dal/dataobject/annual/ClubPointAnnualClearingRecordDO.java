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
 * 年度清零记录 DO
 */
@TableName("club_points_annual_clearing_record")
@KeySequence("club_points_annual_clearing_record_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointAnnualClearingRecordDO extends BaseDO {

    @TableId
    private Long id;
    @TableField("`year`")
    private Integer year;
    private Long userId;
    private Integer netPointsBefore;
    private Integer frozenPointsBefore;
    private Integer availablePointsBefore;
    private Integer clearablePoints;
    private Long clearTransactionId;
    private Integer status;
    private Long runId;
    private String idempotencyKey;
    private LocalDateTime clearTime;
    private String errorMessage;

}
