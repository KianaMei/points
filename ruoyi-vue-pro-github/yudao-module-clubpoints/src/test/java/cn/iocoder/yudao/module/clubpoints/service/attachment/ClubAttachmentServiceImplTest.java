package cn.iocoder.yudao.module.clubpoints.service.attachment;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.attachment.ClubAttachmentRefDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment.ClubAttachmentRefMapper;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.ATTACHMENT_TYPE_FILE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.ATTACHMENT_TYPE_URL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.BIZ_TYPE_CONTRIBUTION_MATERIAL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.STATUS_DELETED;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.STATUS_EFFECTIVE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTACHMENT_LOCKED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Import(ClubAttachmentServiceImpl.class)
class ClubAttachmentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ClubAttachmentService clubAttachmentService;
    @Resource
    private ClubAttachmentRefMapper clubAttachmentRefMapper;

    @MockBean
    private FileService fileService;

    @Test
    void bindFileAttachmentShouldReuseInfraFileAndPersistOnlyRef() {
        when(fileService.getFile(9L)).thenReturn(FileDO.builder().id(9L).name("proof.pdf").url("https://file.local/proof.pdf").build());

        Long attachmentId = clubAttachmentService.bindAttachment(buildFileReq());

        ClubAttachmentRefDO attachment = clubAttachmentRefMapper.selectById(attachmentId);
        assertNotNull(attachment);
        assertEquals(BIZ_TYPE_CONTRIBUTION_MATERIAL, attachment.getBizType());
        assertEquals(100L, attachment.getBizId());
        assertEquals(200L, attachment.getBizItemId());
        assertEquals(ATTACHMENT_TYPE_FILE, attachment.getAttachmentType());
        assertEquals(9L, attachment.getFileId());
        assertEquals("proof.pdf", attachment.getName());
        assertEquals(STATUS_EFFECTIVE, attachment.getStatus());
        assertFalse(attachment.getLocked());
        assertFalse(attachment.getAdminAppend());
        verify(fileService).getFile(9L);
        verify(fileService, never()).createFile(any(byte[].class), any(), any(), any());
        verify(fileService, never()).createFile(any(FileCreateReqVO.class));
    }

    @Test
    void bindUrlAttachmentShouldNotCallInfraFileService() {
        Long attachmentId = clubAttachmentService.bindAttachment(buildUrlReq());

        ClubAttachmentRefDO attachment = clubAttachmentRefMapper.selectById(attachmentId);
        assertEquals(ATTACHMENT_TYPE_URL, attachment.getAttachmentType());
        assertEquals("https://example.test/proof", attachment.getUrl());
        assertEquals("外部证明", attachment.getName());
        verifyNoInteractions(fileService);
    }

    @Test
    void lockBizAttachmentsShouldLockEffectiveAttachments() {
        Long firstId = insertRef(buildRef(100L, false, STATUS_EFFECTIVE));
        Long secondId = insertRef(buildRef(100L, false, STATUS_EFFECTIVE).setFileId(10L).setName("second.pdf"));
        Long otherId = insertRef(buildRef(101L, false, STATUS_EFFECTIVE).setFileId(11L).setName("other.pdf"));

        int lockedCount = clubAttachmentService.lockBizAttachments(BIZ_TYPE_CONTRIBUTION_MATERIAL, 100L);

        assertEquals(2, lockedCount);
        assertLocked(firstId);
        assertLocked(secondId);
        assertFalse(clubAttachmentRefMapper.selectById(otherId).getLocked());
    }

    @Test
    void validateCanDeleteShouldRejectLockedAttachment() {
        Long attachmentId = insertRef(buildRef(100L, true, STATUS_EFFECTIVE));

        assertServiceException(() -> clubAttachmentService.validateCanDelete(attachmentId), CLUB_ATTACHMENT_LOCKED);
    }

    @Test
    void deleteAttachmentShouldMarkUnlockedAttachmentDeleted() {
        Long attachmentId = insertRef(buildRef(100L, false, STATUS_EFFECTIVE));

        clubAttachmentService.deleteAttachment(attachmentId);

        assertEquals(STATUS_DELETED, clubAttachmentRefMapper.selectById(attachmentId).getStatus());
    }

    private static ClubAttachmentBindReqBO buildFileReq() {
        return new ClubAttachmentBindReqBO()
                .setBizType(BIZ_TYPE_CONTRIBUTION_MATERIAL)
                .setBizId(100L)
                .setBizItemId(200L)
                .setAttachmentType(ATTACHMENT_TYPE_FILE)
                .setFileId(9L)
                .setName("proof.pdf")
                .setRemark("材料附件")
                .setUploadedBy(1000L);
    }

    private static ClubAttachmentBindReqBO buildUrlReq() {
        return new ClubAttachmentBindReqBO()
                .setBizType(BIZ_TYPE_CONTRIBUTION_MATERIAL)
                .setBizId(100L)
                .setAttachmentType(ATTACHMENT_TYPE_URL)
                .setUrl("https://example.test/proof")
                .setName("外部证明")
                .setUploadedBy(1000L)
                .setAdminAppend(true);
    }

    private static ClubAttachmentRefDO buildRef(Long bizId, boolean locked, Integer status) {
        return new ClubAttachmentRefDO()
                .setBizType(BIZ_TYPE_CONTRIBUTION_MATERIAL)
                .setBizId(bizId)
                .setAttachmentType(ATTACHMENT_TYPE_FILE)
                .setFileId(9L)
                .setName("proof.pdf")
                .setStatus(status)
                .setLocked(locked)
                .setLockTime(locked ? LocalDateTime.now() : null)
                .setUploadedBy(1000L)
                .setUploadedTime(LocalDateTime.now())
                .setAdminAppend(false);
    }

    private void assertLocked(Long attachmentId) {
        ClubAttachmentRefDO attachment = clubAttachmentRefMapper.selectById(attachmentId);
        assertTrue(attachment.getLocked());
        assertNotNull(attachment.getLockTime());
    }

    private Long insertRef(ClubAttachmentRefDO attachment) {
        clubAttachmentRefMapper.insert(attachment);
        return attachment.getId();
    }

}
