package cn.iocoder.yudao.module.clubpoints.service.attachment;

import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;

/**
 * 俱乐部积分附件绑定服务
 */
public interface ClubAttachmentService {

    Long bindAttachment(ClubAttachmentBindReqBO reqBO);

    int lockBizAttachments(String bizType, Long bizId);

    void validateCanDelete(Long attachmentId);

    void deleteAttachment(Long attachmentId);

}
