package cn.iocoder.yudao.module.clubpoints.service.contribution;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment.ClubAttachmentRefMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionMaterialMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionReviewRecordMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentService;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionMaterialSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionSubmitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
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
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_REVIEW;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum.CONTRIBUTION_MATERIAL;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_MATERIAL_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_REVIEW_DENIED;
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

    private static final int REVIEW_RESULT_APPROVED = 1;
    private static final int REVIEW_RESULT_REJECTED = 2;

    @Resource
    private ClubPointContributionMaterialMapper materialMapper;
    @Resource
    private ClubPointContributionItemMapper itemMapper;
    @Resource
    private ClubPointContributionReviewRecordMapper reviewRecordMapper;
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
    @Resource
    private ClubAuditService clubAuditService;
    @Resource
    private ClubPointLedgerService ledgerService;

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

    @Override
    public List<ClubPointContributionMaterialDO> listPendingReviewMaterials(boolean operatorGlobalScope) {
        validateReviewScope(operatorGlobalScope);
        return materialMapper.selectListByStatus(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewMaterial(ClubPointContributionReviewReqBO reqBO) {
        validateReviewReq(reqBO);
        validateReviewScope(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointContributionMaterialDO material = validateMaterialExistsForReview(reqBO.getId());
        validateReviewable(material);
        List<ClubPointContributionItemDO> items = itemMapper.selectListByMaterialId(material.getId());
        LocalDateTime reviewTime = LocalDateTime.now();
        String beforeJson = snapshot(material, items.size());
        applyReviewResult(material, reqBO, reviewTime);
        String materialSnapshotJson = snapshot(material, items.size());
        material.setSnapshotJson(materialSnapshotJson);

        Long auditLogId = createReviewAudit(reqBO, material, beforeJson,
                reviewAuditSnapshot(material, reqBO.getResult(), items.size()));
        int createdTransactionCount = REVIEW_RESULT_APPROVED == reqBO.getResult()
                ? createContributionTransactions(material, items, reqBO, reviewTime, auditLogId) : 0;
        materialMapper.updateById(material);
        if (REVIEW_RESULT_APPROVED == reqBO.getResult()) {
            clubAttachmentService.lockBizAttachments(BIZ_TYPE_CONTRIBUTION_MATERIAL, material.getId());
        }
        insertReviewRecord(material, reqBO, reviewTime, createdTransactionCount, auditLogId);
    }

    private ClubPointContributionMaterialDO validateMaterialExists(Long materialId) {
        ClubPointContributionMaterialDO material = materialMapper.selectById(materialId);
        if (material == null) {
            throw exception(CLUB_CONTRIBUTION_MATERIAL_NOT_FOUND);
        }
        return material;
    }

    private ClubPointContributionMaterialDO validateMaterialExistsForReview(Long materialId) {
        ClubPointContributionMaterialDO material = materialMapper.selectByIdForUpdate(materialId);
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

    private static void validateReviewable(ClubPointContributionMaterialDO material) {
        ClubPointContributionMaterialStatusEnum currentStatus =
                ClubPointContributionMaterialStatusEnum.of(material.getStatus());
        if (currentStatus == null || !currentStatus.canReview()) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
    }

    private static void validateReviewReq(ClubPointContributionReviewReqBO reqBO) {
        if (reqBO == null || reqBO.getId() == null || reqBO.getResult() == null
                || reqBO.getOperatorUserId() == null) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
        if (REVIEW_RESULT_APPROVED != reqBO.getResult() && REVIEW_RESULT_REJECTED != reqBO.getResult()) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
        if (REVIEW_RESULT_REJECTED == reqBO.getResult() && !StringUtils.hasText(reqBO.getReason())) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
    }

    private void validateReviewScope(boolean operatorGlobalScope) {
        if (!clubScopeService.hasGlobalScope(operatorGlobalScope)) {
            throw exception(CLUB_CONTRIBUTION_REVIEW_DENIED);
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

    private static void applyReviewResult(ClubPointContributionMaterialDO material,
                                          ClubPointContributionReviewReqBO reqBO,
                                          LocalDateTime reviewTime) {
        ClubPointContributionMaterialStatusEnum targetStatus = REVIEW_RESULT_APPROVED == reqBO.getResult()
                ? ClubPointContributionMaterialStatusEnum.APPROVED : ClubPointContributionMaterialStatusEnum.REJECTED;
        material.setStatus(targetStatus.getStatus())
                .setReviewerUserId(reqBO.getOperatorUserId())
                .setReviewTime(reviewTime)
                .setReviewReason(reqBO.getReason());
        if (targetStatus == ClubPointContributionMaterialStatusEnum.APPROVED) {
            material.setLocked(true);
        }
    }

    private int createContributionTransactions(ClubPointContributionMaterialDO material,
                                               List<ClubPointContributionItemDO> items,
                                               ClubPointContributionReviewReqBO reqBO,
                                               LocalDateTime reviewTime,
                                               Long auditLogId) {
        int createdTransactionCount = 0;
        for (ClubPointContributionItemDO item : items) {
            Long transactionId = ledgerService.createTransaction(buildLedgerCreateReq(
                    material, item, reqBO, reviewTime, auditLogId));
            itemMapper.updateById(new ClubPointContributionItemDO()
                    .setId(item.getId())
                    .setTransactionId(transactionId));
            createdTransactionCount++;
        }
        return createdTransactionCount;
    }

    private static ClubPointLedgerCreateReqBO buildLedgerCreateReq(ClubPointContributionMaterialDO material,
                                                                   ClubPointContributionItemDO item,
                                                                   ClubPointContributionReviewReqBO reqBO,
                                                                   LocalDateTime reviewTime,
                                                                   Long auditLogId) {
        return new ClubPointLedgerCreateReqBO()
                .setTransactionNo(buildContributionTransactionNo(material.getId(), item.getId()))
                .setUserId(item.getUserId())
                .setUserNameSnapshot(item.getUserNameSnapshot())
                .setDeptNameSnapshot(item.getDeptNameSnapshot())
                .setDirection(item.getDirection())
                .setPoints(item.getPoints())
                .setPointCategory(item.getPointCategory())
                .setPointTypeCode(item.getRuleItemCode())
                .setSourceType(CONTRIBUTION_MATERIAL.getType())
                .setSourceId(material.getId())
                .setSourceItemId(item.getId())
                .setSourceTitleSnapshot(material.getTitle())
                .setIssuingClubId(material.getClubId())
                .setIssuingClubNameSnapshot(material.getClubNameSnapshot())
                .setMaterialSummary(item.getMaterialSummary())
                .setReason(item.getReason())
                .setOccurredAt(reviewTime)
                .setIdempotencyKey(item.getIdempotencyKey())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(auditLogId)
                .setRuleItemCode(item.getRuleItemCode())
                .setRuleVersionId(material.getRuleVersionId())
                .setSourceSnapshotJson(material.getSnapshotJson());
    }

    private static String buildContributionTransactionNo(Long materialId, Long itemId) {
        return "CONTRIBUTION:" + materialId + ":" + itemId;
    }

    private Long createReviewAudit(ClubPointContributionReviewReqBO reqBO,
                                   ClubPointContributionMaterialDO material,
                                   String beforeJson,
                                   String afterJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(CONTRIBUTION_REVIEW)
                .setBizType(BIZ_TYPE_CONTRIBUTION_MATERIAL)
                .setBizId(material.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(material.getReviewTime())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(material.getSnapshotJson())
                .setSuccess(true));
    }

    private void insertReviewRecord(ClubPointContributionMaterialDO material,
                                    ClubPointContributionReviewReqBO reqBO,
                                    LocalDateTime reviewTime,
                                    int createdTransactionCount,
                                    Long auditLogId) {
        reviewRecordMapper.insert(new ClubPointContributionReviewRecordDO()
                .setMaterialId(material.getId())
                .setReviewerUserId(reqBO.getOperatorUserId())
                .setResult(reqBO.getResult())
                .setReason(reqBO.getReason())
                .setReviewTime(reviewTime)
                .setMaterialSnapshotJson(material.getSnapshotJson())
                .setCreatedTransactionCount(createdTransactionCount)
                .setAuditLogId(auditLogId));
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
        snapshot.put("reviewerUserId", material.getReviewerUserId());
        snapshot.put("reviewTime", material.getReviewTime());
        snapshot.put("reviewReason", material.getReviewReason());
        snapshot.put("locked", material.getLocked());
        snapshot.put("directCreated", material.getDirectCreated());
        snapshot.put("itemCount", itemCount);
        return JsonUtils.toJsonString(snapshot);
    }

    private static String reviewAuditSnapshot(ClubPointContributionMaterialDO material, Integer result, int itemCount) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", material.getId());
        snapshot.put("status", material.getStatus());
        snapshot.put("result", result);
        snapshot.put("reviewerUserId", material.getReviewerUserId());
        snapshot.put("reviewTime", material.getReviewTime());
        snapshot.put("reviewReason", material.getReviewReason());
        snapshot.put("locked", material.getLocked());
        snapshot.put("itemCount", itemCount);
        return JsonUtils.toJsonString(snapshot);
    }

}
