package cn.iocoder.yudao.module.clubpoints.dal.dataobject.dispute;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 员工积分异议 DO
 */
@TableName("club_points_dispute")
@KeySequence("club_points_dispute_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointDisputeDO extends BaseDO {

    @TableId
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer targetType;
    private Long targetId;
    private Integer status;
    private LocalDateTime submitTime;
    private Long handlerUserId;
    private LocalDateTime handleTime;
    private String replyContent;
    private Integer relatedActionType;
    private Long relatedTransactionId;
    private LocalDateTime closeTime;
    private Long auditLogId;

}
