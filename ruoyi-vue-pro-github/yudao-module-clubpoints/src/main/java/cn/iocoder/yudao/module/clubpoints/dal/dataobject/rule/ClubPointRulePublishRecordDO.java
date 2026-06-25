package cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分规则发布记录 DO
 */
@TableName("club_points_rule_publish_record")
@KeySequence("club_points_rule_publish_record_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRulePublishRecordDO extends BaseDO {

    @TableId
    private Long id;
    private Long ruleVersionId;
    private Integer action;
    private Long operatorUserId;
    private LocalDateTime operationTime;
    private String reason;
    private String beforeJson;
    private String afterJson;
    private Long auditLogId;

}
