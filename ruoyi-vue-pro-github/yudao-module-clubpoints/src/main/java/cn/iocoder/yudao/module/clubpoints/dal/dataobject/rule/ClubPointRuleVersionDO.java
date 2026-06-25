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
 * 积分制度版本 DO
 */
@TableName("club_points_rule_version")
@KeySequence("club_points_rule_version_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRuleVersionDO extends BaseDO {

    @TableId
    private Long id;
    private String versionNo;
    private String name;
    private Integer status;
    private LocalDateTime publicityTime;
    private LocalDateTime effectiveTime;
    private LocalDateTime publishedTime;
    private LocalDateTime disabledTime;
    private String summary;
    private String content;
    private String attachmentSnapshotJson;
    private String remark;

}
