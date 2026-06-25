package cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger;

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
 * 员工年度状态和评优资格 DO
 */
@TableName("club_points_user_year_status")
@KeySequence("club_points_user_year_status_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointUserYearStatusDO extends BaseDO {

    @TableId
    private Long id;
    private Long userId;
    @TableField("`year`")
    private Integer year;
    private Boolean honorEligible;
    private String honorCancelReason;
    private Long honorCancelTransactionId;
    private LocalDateTime honorCancelTime;
    private Integer annualPositivePoints;
    private Integer annualNegativePoints;
    private String remark;

}
