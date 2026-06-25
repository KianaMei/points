package cn.iocoder.yudao.module.clubpoints.service.attachment.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 俱乐部积分附件绑定请求 BO
 */
@Data
@Accessors(chain = true)
public class ClubAttachmentBindReqBO {

    private String bizType;
    private Long bizId;
    private Long bizItemId;
    private Integer attachmentType;
    private Long fileId;
    private String url;
    private String name;
    private String remark;
    private Long uploadedBy;
    private Boolean adminAppend;

}
