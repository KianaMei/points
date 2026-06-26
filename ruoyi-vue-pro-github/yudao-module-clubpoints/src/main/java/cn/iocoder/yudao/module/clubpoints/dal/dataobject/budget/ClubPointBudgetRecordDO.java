package cn.iocoder.yudao.module.clubpoints.dal.dataobject.budget;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 预算和经费记录 DO
 */
@TableName("club_points_budget_record")
@KeySequence("club_points_budget_record_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointBudgetRecordDO extends BaseDO {

    @TableId
    private Long id;
    private Integer category;
    private Long budgetAmountCent;
    private Long actualAmountCent;
    private LocalDate occurDate;
    private Long handlerUserId;
    private Integer sourceType;
    private Long sourceId;
    private String description;
    private String remark;

}
