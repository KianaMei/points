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
 * 运营激励记录 DO
 */
@TableName("club_points_incentive_record")
@KeySequence("club_points_incentive_record_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointIncentiveRecordDO extends BaseDO {

    @TableId
    private Long id;
    @TableField("`year`")
    private Integer year;
    private Integer type;
    private Long clubId;
    private String clubNameSnapshot;
    private Long userId;
    private String userNameSnapshot;
    private String title;
    private Long amountCent;
    private Integer status;
    private Integer sourceType;
    private Long sourceId;
    private Long budgetRecordId;
    private Long confirmedBy;
    private LocalDateTime confirmedTime;
    private String remark;

}
