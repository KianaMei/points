package cn.iocoder.yudao.module.clubpoints.dal.dataobject.club;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 俱乐部负责人关系 DO
 */
@TableName("club_points_club_leader")
@KeySequence("club_points_club_leader_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubLeaderDO extends BaseDO {

    @TableId
    private Long id;
    private Long clubId;
    private Long userId;
    private Integer status;
    private LocalDateTime assignedTime;
    private LocalDateTime removedTime;
    private Long assignedBy;
    private Long removedBy;
    private String reason;
    private String clubNameSnapshot;
    private String userNameSnapshot;
    private String activeUniqueKey;

}
