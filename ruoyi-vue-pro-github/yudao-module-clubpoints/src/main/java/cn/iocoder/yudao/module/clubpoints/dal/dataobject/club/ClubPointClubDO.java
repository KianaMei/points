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
 * 俱乐部主数据 DO
 */
@TableName("club_points_club")
@KeySequence("club_points_club_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointClubDO extends BaseDO {

    @TableId
    private Long id;
    private String code;
    private String name;
    private Integer status;
    private String description;
    private String contactText;
    private Long coverFileId;
    private Integer sort;
    private LocalDateTime disabledTime;
    private String disabledReason;
    private String remark;

}
