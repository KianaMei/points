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
 * 俱乐部成员关系 DO
 */
@TableName("club_points_club_member")
@KeySequence("club_points_club_member_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubMemberDO extends BaseDO {

    @TableId
    private Long id;
    private Long clubId;
    private Long userId;
    private Long deptIdSnapshot;
    private String userNameSnapshot;
    private String deptNameSnapshot;
    private String mobileSnapshot;
    private String clubCodeSnapshot;
    private String clubNameSnapshot;
    private Integer status;
    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;
    private Integer leaveReasonType;
    private String leaveReason;
    private Long operatorUserId;
    private String activeUniqueKey;

}
