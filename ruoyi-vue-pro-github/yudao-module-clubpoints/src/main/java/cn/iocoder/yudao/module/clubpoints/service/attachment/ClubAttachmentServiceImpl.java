package cn.iocoder.yudao.module.clubpoints.service.attachment;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.attachment.ClubAttachmentRefDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment.ClubAttachmentRefMapper;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.ATTACHMENT_TYPE_FILE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.ATTACHMENT_TYPE_URL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.STATUS_DELETED;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.STATUS_EFFECTIVE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTACHMENT_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTACHMENT_LOCKED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTACHMENT_NOT_EXISTS;

/**
 * 俱乐部积分附件绑定服务实现
 */
@Service
public class ClubAttachmentServiceImpl implements ClubAttachmentService {

    @Resource
    private ClubAttachmentRefMapper clubAttachmentRefMapper;
    @Resource
    private FileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bindAttachment(ClubAttachmentBindReqBO reqBO) {
        validateBindReq(reqBO);
        ClubAttachmentRefDO attachment = buildAttachment(reqBO);
        clubAttachmentRefMapper.insert(attachment);
        return attachment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int lockBizAttachments(String bizType, Long bizId) {
        if (!StringUtils.hasText(bizType) || bizId == null) {
            throw exception(CLUB_ATTACHMENT_INVALID);
        }
        List<ClubAttachmentRefDO> attachments = clubAttachmentRefMapper.selectListByBiz(bizType, bizId, STATUS_EFFECTIVE);
        LocalDateTime now = LocalDateTime.now();
        int lockedCount = 0;
        for (ClubAttachmentRefDO attachment : attachments) {
            if (Boolean.TRUE.equals(attachment.getLocked())) {
                continue;
            }
            clubAttachmentRefMapper.updateById(new ClubAttachmentRefDO()
                    .setId(attachment.getId())
                    .setLocked(true)
                    .setLockTime(now));
            lockedCount++;
        }
        return lockedCount;
    }

    @Override
    public void validateCanDelete(Long attachmentId) {
        ClubAttachmentRefDO attachment = clubAttachmentRefMapper.selectById(attachmentId);
        if (attachment == null || Objects.equals(attachment.getStatus(), STATUS_DELETED)) {
            throw exception(CLUB_ATTACHMENT_NOT_EXISTS);
        }
        if (Boolean.TRUE.equals(attachment.getLocked())) {
            throw exception(CLUB_ATTACHMENT_LOCKED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long attachmentId) {
        validateCanDelete(attachmentId);
        clubAttachmentRefMapper.updateById(new ClubAttachmentRefDO()
                .setId(attachmentId)
                .setStatus(STATUS_DELETED));
    }

    private void validateBindReq(ClubAttachmentBindReqBO reqBO) {
        if (reqBO == null || !StringUtils.hasText(reqBO.getBizType()) || reqBO.getBizId() == null
                || reqBO.getAttachmentType() == null || !StringUtils.hasText(reqBO.getName())
                || reqBO.getUploadedBy() == null) {
            throw exception(CLUB_ATTACHMENT_INVALID);
        }
        if (Objects.equals(reqBO.getAttachmentType(), ATTACHMENT_TYPE_FILE)) {
            validateFileAttachment(reqBO);
            return;
        }
        if (Objects.equals(reqBO.getAttachmentType(), ATTACHMENT_TYPE_URL)) {
            validateUrlAttachment(reqBO);
            return;
        }
        throw exception(CLUB_ATTACHMENT_INVALID);
    }

    private void validateFileAttachment(ClubAttachmentBindReqBO reqBO) {
        if (reqBO.getFileId() == null) {
            throw exception(CLUB_ATTACHMENT_INVALID);
        }
        FileDO file = fileService.getFile(reqBO.getFileId());
        if (file == null) {
            throw exception(CLUB_ATTACHMENT_INVALID);
        }
    }

    private static void validateUrlAttachment(ClubAttachmentBindReqBO reqBO) {
        if (!StringUtils.hasText(reqBO.getUrl())) {
            throw exception(CLUB_ATTACHMENT_INVALID);
        }
    }

    private static ClubAttachmentRefDO buildAttachment(ClubAttachmentBindReqBO reqBO) {
        return new ClubAttachmentRefDO()
                .setBizType(reqBO.getBizType())
                .setBizId(reqBO.getBizId())
                .setBizItemId(reqBO.getBizItemId())
                .setAttachmentType(reqBO.getAttachmentType())
                .setFileId(reqBO.getFileId())
                .setUrl(reqBO.getUrl())
                .setName(reqBO.getName())
                .setRemark(reqBO.getRemark())
                .setStatus(STATUS_EFFECTIVE)
                .setLocked(false)
                .setUploadedBy(reqBO.getUploadedBy())
                .setUploadedTime(LocalDateTime.now())
                .setAdminAppend(Boolean.TRUE.equals(reqBO.getAdminAppend()));
    }

}
