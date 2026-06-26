package cn.iocoder.yudao.module.clubpoints.service.contribution;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment.ClubAttachmentRefMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionMaterialMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentService;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionMaterialSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionSubmitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleResolveService;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.BIZ_TYPE_CONTRIBUTION_MATERIAL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.STATUS_EFFECTIVE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_MATERIAL_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_DISABLED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_VALUE_OUT_OF_RANGE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;

/**
 * 非签到积分材料服务实现
 */
@Service
public class ClubPointContributionServiceImpl implements ClubPointContributionService {

    @Resource
    private ClubPointContributionMaterialMapper materialMapper;
    @Resource
    private ClubPointContributionItemMapper itemMapper;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubScopeService clubScopeService;
    @Resource
    private ClubPointRuleResolveService ruleResolveService;
    @Resource
    private ClubAttachmentService clubAttachmentService;
    @Resource
    private ClubAttachmentRefMapper attachmentRefMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDraft(ClubPointContributionMaterialSaveReqBO reqBO) {
        validateSaveReq(reqBO);
        ClubPointContributionMaterialTypeEnum materialType = validateMaterialType(reqBO.getType());
        ClubPointClubDO club = validateEnabledClub(reqBO.getClubId());
        clubScopeService.validateManagedClub(reqBO.getOperatorUserId(), club.getId());
        validateRuleAndItems(reqBO, materialType);

        ClubPointContributionMaterialDO material = buildMaterial(reqBO, club)
                .setStatus(ClubPointContributionMaterialStatusEnum.DRAFT.getStatus())
                .setSubmitterUserId(reqBO.getOperatorUserId())
                .setLocked(false)
                .setDirectCreated(false);
        materialMapper.insert(material);
        insertItems(material, reqBO, materialType);
        bindAttachments(material.getId(), reqBO.getOperatorUserId(), reqBO.getAttachments());
        material.setSnapshotJson(snapshot(material, itemMapper.selectListByMaterialId(material.getId()).size()));
        materialMapper.updateById(material);
        return material.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDraft(ClubPointContributionMaterialSaveReqBO reqBO) {
        validateSaveReq(reqBO);
        ClubPointContributionMaterialDO material = validateMaterialExists(reqBO.getId());
        validateEditable(material);
        if (!Objects.equals(material.getClubId(), reqBO.getClubId())) {
            throw exception(CLUB_SCOPE_DENIED);
        }
        ClubPointContributionMaterialTypeEnum materialType = validateMaterialType(reqBO.getType());
        ClubPointClubDO club = validateEnabledClub(reqBO.getClubId());
        clubScopeService.validateManagedClub(reqBO.getOperatorUserId(), club.getId());
        validateRuleAndItems(reqBO, materialType);

        material.setClubNameSnapshot(club.getName())
                .setType(reqBO.getType())
                .setTitle(reqBO.getTitle())
                .setDescription(reqBO.getDescription())
                .setRuleVersionId(reqBO.getRuleVersionId());
        itemMapper.deleteByMaterialId(material.getId());
        insertItems(material, reqBO, materialType);
        bindAttachments(material.getId(), reqBO.getOperatorUserId(), reqBO.getAttachments());
        material.setSnapshotJson(snapshot(material, itemMapper.selectListByMaterialId(material.getId()).size()));
        materialMapper.updateById(material);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(ClubPointContributionSubmitReqBO reqBO) {
        ClubPointContributionMaterialDO material = validateMaterialExists(reqBO.getId());
        clubScopeService.validateManagedClub(reqBO.getOperatorUserId(), material.getClubId());
        validateTransition(material, ClubPointContributionMaterialStatusEnum.PENDING_REVIEW);
        validateAttachmentExists(material.getId());
        material.setStatus(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus())
                .setSubmitTime(LocalDateTime.now());
        material.setSnapshotJson(snapshot(material, itemMapper.selectListByMaterialId(material.getId()).size()));
        materialMapper.updateById(material);
    }

    private ClubPointContributionMaterialDO validateMaterialExists(Long materialId) {
        ClubPointContributionMaterialDO material = materialMapper.selectById(materialId);
        if (material == null) {
            throw exception(CLUB_CONTRIBUTION_MATERIAL_NOT_FOUND);
        }
        return material;
    }

    private ClubPointClubDO validateEnabledClub(Long clubId) {
        ClubPointClubDO club = clubMapper.selectById(clubId);
        if (club == null) {
            throw exception(CLUB_NOT_FOUND);
        }
        if (!ClubPointClubStatusEnum.ENABLED.getStatus().equals(club.getStatus())) {
            throw exception(CLUB_DISABLED);
        }
        return club;
    }

    private static ClubPointContributionMaterialTypeEnum validateMaterialType(Integer type) {
        ClubPointContributionMaterialTypeEnum materialType = ClubPointContributionMaterialTypeEnum.of(type);
        if (materialType == null) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
        return materialType;
    }

    private void validateRuleAndItems(ClubPointContributionMaterialSaveReqBO reqBO,
                                      ClubPointContributionMaterialTypeEnum materialType) {
        ruleResolveService.getItem(reqBO.getRuleVersionId(), materialType.getRuleItemCode());
        for (ClubPointContributionItemSaveReqBO item : reqBO.getItems()) {
            validateItemReq(item);
            try {
                ruleResolveService.validatePointsInRange(reqBO.getRuleVersionId(),
                        materialType.getRuleItemCode(), item.getPoints());
            } catch (ServiceException ex) {
                if (Objects.equals(CLUB_RULE_VALUE_OUT_OF_RANGE.getCode(), ex.getCode())) {
                    throw exception(CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE);
                }
                throw ex;
            }
        }
    }

    private static void validateSaveReq(ClubPointContributionMaterialSaveReqBO reqBO) {
        if (reqBO == null || reqBO.getClubId() == null || reqBO.getType() == null
                || !StringUtils.hasText(reqBO.getTitle()) || reqBO.getRuleVersionId() == null
                || reqBO.getOperatorUserId() == null || reqBO.getItems() == null
                || reqBO.getItems().isEmpty()) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
    }

    private static void validateItemReq(ClubPointContributionItemSaveReqBO item) {
        if (item == null || item.getUserId() == null || !StringUtils.hasText(item.getUserNameSnapshot())
                || item.getPoints() == null || !StringUtils.hasText(item.getReason())) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
    }

    private static void validateEditable(ClubPointContributionMaterialDO material) {
        ClubPointContributionMaterialStatusEnum currentStatus =
                ClubPointContributionMaterialStatusEnum.of(material.getStatus());
        if (currentStatus == null || !currentStatus.canEditContent()) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
    }

    private static void validateTransition(ClubPointContributionMaterialDO material,
                                           ClubPointContributionMaterialStatusEnum targetStatus) {
        ClubPointContributionMaterialStatusEnum currentStatus =
                ClubPointContributionMaterialStatusEnum.of(material.getStatus());
        if (currentStatus == null || !currentStatus.canTransitionTo(targetStatus)) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
    }

    private void validateAttachmentExists(Long materialId) {
        if (attachmentRefMapper.selectListByBiz(BIZ_TYPE_CONTRIBUTION_MATERIAL,
                materialId, STATUS_EFFECTIVE).isEmpty()) {
            throw exception(CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED);
        }
    }

    private static ClubPointContributionMaterialDO buildMaterial(ClubPointContributionMaterialSaveReqBO reqBO,
                                                                ClubPointClubDO club) {
        return new ClubPointContributionMaterialDO()
                .setClubId(club.getId())
                .setClubNameSnapshot(club.getName())
                .setType(reqBO.getType())
                .setTitle(reqBO.getTitle())
                .setDescription(reqBO.getDescription())
                .setRuleVersionId(reqBO.getRuleVersionId());
    }

    private void insertItems(ClubPointContributionMaterialDO material,
                             ClubPointContributionMaterialSaveReqBO reqBO,
                             ClubPointContributionMaterialTypeEnum materialType) {
        ClubPointRuleItemDO ruleItem = ruleResolveService.getItem(reqBO.getRuleVersionId(),
                materialType.getRuleItemCode());
        int sequence = 0;
        for (ClubPointContributionItemSaveReqBO itemReq : reqBO.getItems()) {
            sequence++;
            ClubPointContributionItemDO item = new ClubPointContributionItemDO()
                    .setMaterialId(material.getId())
                    .setClubId(material.getClubId())
                    .setUserId(itemReq.getUserId())
                    .setUserNameSnapshot(itemReq.getUserNameSnapshot())
                    .setDeptNameSnapshot(itemReq.getDeptNameSnapshot())
                    .setPointCategory(materialType.getPointCategory())
                    .setRuleItemId(ruleItem.getId())
                    .setRuleItemCode(materialType.getRuleItemCode())
                    .setDirection(materialType.getDirection())
                    .setPoints(itemReq.getPoints())
                    .setReason(itemReq.getReason())
                    .setMaterialSummary(itemReq.getMaterialSummary())
                    .setDutyMonth(itemReq.getDutyMonth())
                    .setRecommendedUserId(itemReq.getRecommendedUserId())
                    .setAwardLevel(itemReq.getAwardLevel())
                    .setApprovalResultSnapshot(itemReq.getApprovalResultSnapshot())
                    .setIdempotencyKey(temporaryIdempotencyKey(material.getId(), sequence, itemReq.getUserId()));
            itemMapper.insert(item);
            itemMapper.updateById(new ClubPointContributionItemDO()
                    .setId(item.getId())
                    .setIdempotencyKey(idempotencyKey(material.getId(), item.getId(), itemReq.getUserId())));
        }
    }

    private void bindAttachments(Long materialId, Long uploadedBy, List<ClubAttachmentBindReqBO> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        for (ClubAttachmentBindReqBO attachment : attachments) {
            clubAttachmentService.bindAttachment(attachment
                    .setBizType(BIZ_TYPE_CONTRIBUTION_MATERIAL)
                    .setBizId(materialId)
                    .setUploadedBy(uploadedBy)
                    .setAdminAppend(false));
        }
    }

    private static String temporaryIdempotencyKey(Long materialId, int sequence, Long userId) {
        return "CONTRIBUTION_TMP:" + materialId + ":" + sequence + ":" + userId;
    }

    private static String idempotencyKey(Long materialId, Long itemId, Long userId) {
        return "CONTRIBUTION:" + materialId + ":" + itemId + ":" + userId;
    }

    private static String snapshot(ClubPointContributionMaterialDO material, int itemCount) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", material.getId());
        snapshot.put("clubId", material.getClubId());
        snapshot.put("clubNameSnapshot", material.getClubNameSnapshot());
        snapshot.put("type", material.getType());
        snapshot.put("title", material.getTitle());
        snapshot.put("description", material.getDescription());
        snapshot.put("status", material.getStatus());
        snapshot.put("ruleVersionId", material.getRuleVersionId());
        snapshot.put("submitterUserId", material.getSubmitterUserId());
        snapshot.put("submitTime", material.getSubmitTime());
        snapshot.put("locked", material.getLocked());
        snapshot.put("directCreated", material.getDirectCreated());
        snapshot.put("itemCount", itemCount);
        return JsonUtils.toJsonString(snapshot);
    }

}
