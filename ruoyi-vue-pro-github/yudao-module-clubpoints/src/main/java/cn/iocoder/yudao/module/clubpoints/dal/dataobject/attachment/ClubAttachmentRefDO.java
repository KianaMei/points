package cn.iocoder.yudao.module.clubpoints.dal.dataobject.attachment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 俱乐部积分业务附件绑定 DO
 */
@TableName("club_points_attachment_ref")
@KeySequence("club_points_attachment_ref_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubAttachmentRefDO extends BaseDO {

    @TableId
    private Long id;
    private String bizType;
    private Long bizId;
    private Long bizItemId;
    private Integer attachmentType;
    private Long fileId;
    private String url;
    private String name;
    private String remark;
    private Integer status;
    private Boolean locked;
    private LocalDateTime lockTime;
    private Long uploadedBy;
    private LocalDateTime uploadedTime;
    private Boolean adminAppend;

}
