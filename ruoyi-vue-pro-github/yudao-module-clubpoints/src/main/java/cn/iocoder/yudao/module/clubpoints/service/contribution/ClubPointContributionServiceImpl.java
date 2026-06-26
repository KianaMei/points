package cn.iocoder.yudao.module.clubpoints.service.contribution;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointUserYearStatusDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment.ClubAttachmentRefMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionMaterialMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionReviewRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointUserYearStatusMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentService;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionDirectCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionFraudHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionDetailBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionMaterialSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionSubmitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionViolationDeductReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionWithdrawReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyService;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleResolveService;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.BIZ_TYPE_CONTRIBUTION_MATERIAL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.STATUS_EFFECTIVE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_DIRECT_CREATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_FRAUD_HANDLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_REVIEW;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_VIOLATION_DEDUCT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum.FRAUD_HANDLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum.VIOLATION_DEDUCT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum.INCREASE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum.ADMIN_DIRECT;
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
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointUserYearStatusMapper userYearStatusMapper;
    @Resource
    private ClubNotifyService clubNotifyService;

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
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(ClubPointContributionWithdrawReqBO reqBO) {
        if (reqBO == null || reqBO.getId() == null || reqBO.getOperatorUserId() == null) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
        ClubPointContributionMaterialDO material = validateMaterialExistsForReview(reqBO.getId());
        clubScopeService.validateManagedClub(reqBO.getOperatorUserId(), material.getClubId());
        validateTransition(material, ClubPointContributionMaterialStatusEnum.WITHDRAWN);
        material.setStatus(ClubPointContributionMaterialStatusEnum.WITHDRAWN.getStatus())
                .setReviewReason(reqBO.getReason());
        material.setSnapshotJson(snapshot(material, itemMapper.selectListByMaterialId(material.getId()).size()));
        materialMapper.updateById(material);
    }

    @Override
    public PageResult<ClubPointContributionMaterialDO> getLeaderMaterialPage(Long operatorUserId,
                                                                            ClubPointContributionPageReqBO reqBO) {
        validateLeaderPageReq(operatorUserId, reqBO);
        clubScopeService.validateManagedClub(operatorUserId, reqBO.getClubId());
        return materialMapper.selectPage(reqBO, reqBO.getClubId(), reqBO.getType(), reqBO.getStatus(), false);
    }

    @Override
    public ClubPointContributionDetailBO getLeaderMaterial(Long operatorUserId, Long id) {
        if (operatorUserId == null || id == null) {
            throw exception(CLUB_CONTRIBUTION_MATERIAL_NOT_FOUND);
        }
        ClubPointContributionMaterialDO material = validateMaterialExists(id);
        clubScopeService.validateManagedClub(operatorUserId, material.getClubId());
        if (Boolean.TRUE.equals(material.getDirectCreated())) {
            throw exception(CLUB_SCOPE_DENIED);
        }
        return buildDetail(material);
    }

    @Override
    public List<ClubPointContributionMaterialDO> listPendingReviewMaterials(boolean operatorGlobalScope) {
        validateReviewScope(operatorGlobalScope);
        return materialMapper.selectListByStatus(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus());
    }

    @Override
    public PageResult<ClubPointContributionMaterialDO> getAdminReviewPage(boolean operatorGlobalScope,
                                                                         ClubPointContributionPageReqBO reqBO) {
        validateReviewScope(operatorGlobalScope);
        if (reqBO == null) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
        return materialMapper.selectPage(reqBO, reqBO.getClubId(), reqBO.getType(),
                ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus(), false);
    }

    @Override
    public ClubPointContributionDetailBO getAdminMaterial(boolean operatorGlobalScope, Long id) {
        validateReviewScope(operatorGlobalScope);
        return buildDetail(validateMaterialExists(id));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long directCreate(ClubPointContributionDirectCreateReqBO reqBO) {
        validateDirectCreateReq(reqBO);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointContributionMaterialDO existing = materialMapper.selectByRequestNo(reqBO.getRequestNo());
        if (existing != null) {
            return existing.getId();
        }

        ClubPointContributionMaterialTypeEnum materialType = validateMaterialType(reqBO.getType());
        ClubPointClubDO club = reqBO.getClubId() == null ? null : validateEnabledClub(reqBO.getClubId());
        ClubPointRuleItemDO ruleItem = validateDirectRuleAndPoints(reqBO, materialType);
        LocalDateTime operationTime = LocalDateTime.now();
        ClubPointContributionMaterialDO material = buildDirectMaterial(reqBO, club, operationTime);
        Long duplicatedMaterialId = insertMaterialWithRequestNo(material, reqBO.getRequestNo());
        if (duplicatedMaterialId != null) {
            return duplicatedMaterialId;
        }
        ClubPointContributionItemDO item = buildDirectItem(material, reqBO, materialType, ruleItem);
        itemMapper.insert(item);
        material.setSnapshotJson(snapshot(material, 1));
        bindAttachments(material.getId(), reqBO.getOperatorUserId(), reqBO.getAttachments());
        clubAttachmentService.lockBizAttachments(BIZ_TYPE_CONTRIBUTION_MATERIAL, material.getId());
        Long auditLogId = createDirectCreateAudit(reqBO, material, item);
        Long transactionId = ledgerService.createTransaction(buildDirectLedgerCreateReq(
                material, item, reqBO, operationTime, auditLogId));
        itemMapper.updateById(new ClubPointContributionItemDO()
                .setId(item.getId())
                .setTransactionId(transactionId));
        materialMapper.updateById(material);
        return material.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long violationDeduct(ClubPointContributionViolationDeductReqBO reqBO) {
        validateViolationDeductReq(reqBO);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointContributionMaterialDO existing = materialMapper.selectByRequestNo(reqBO.getRequestNo());
        if (existing != null) {
            return existing.getId();
        }

        ClubPointClubDO club = validateEnabledClub(reqBO.getClubId());
        ClubPointRuleItemDO ruleItem = validateViolationRuleAndPoints(reqBO);
        LocalDateTime operationTime = LocalDateTime.now();
        ClubPointContributionMaterialDO material = buildViolationMaterial(reqBO, club, operationTime);
        Long duplicatedMaterialId = insertMaterialWithRequestNo(material, reqBO.getRequestNo());
        if (duplicatedMaterialId != null) {
            return duplicatedMaterialId;
        }
        ClubPointContributionItemDO item = buildViolationItem(material, reqBO, ruleItem);
        itemMapper.insert(item);
        material.setSnapshotJson(snapshot(material, 1));
        bindAttachments(material.getId(), reqBO.getOperatorUserId(), reqBO.getAttachments());
        clubAttachmentService.lockBizAttachments(BIZ_TYPE_CONTRIBUTION_MATERIAL, material.getId());
        Long auditLogId = createViolationDeductAudit(reqBO, material, item);
        Long transactionId = ledgerService.createTransaction(buildViolationLedgerCreateReq(
                material, item, reqBO, operationTime, auditLogId));
        itemMapper.updateById(new ClubPointContributionItemDO()
                .setId(item.getId())
                .setTransactionId(transactionId));
        materialMapper.updateById(material);
        return material.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long handleFraud(ClubPointContributionFraudHandleReqBO reqBO) {
        validateFraudHandleReq(reqBO);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointContributionMaterialDO existing = materialMapper.selectByRequestNo(reqBO.getRequestNo());
        if (existing != null) {
            return existing.getId();
        }

        ClubPointContributionMaterialDO originalMaterial = validateMaterialExistsForReview(reqBO.getOriginalMaterialId());
        validateFraudOriginalMaterial(originalMaterial);
        List<ClubPointContributionItemDO> originalItems = itemMapper.selectListByMaterialId(originalMaterial.getId());
        validateFraudOriginalItems(originalItems);
        ClubPointRuleItemDO fraudRuleItem = ruleResolveService.getItem(reqBO.getRuleVersionId(),
                FRAUD_HANDLE.getRuleItemCode());
        LocalDateTime operationTime = LocalDateTime.now();
        ClubPointContributionMaterialDO fraudMaterial = buildFraudMaterial(reqBO, originalMaterial, operationTime);
        Long duplicatedMaterialId = insertMaterialWithRequestNo(fraudMaterial, reqBO.getRequestNo());
        if (duplicatedMaterialId != null) {
            return duplicatedMaterialId;
        }

        fraudMaterial.setSnapshotJson(fraudMaterialSnapshot(fraudMaterial, originalMaterial, originalItems.size()));
        bindAttachments(fraudMaterial.getId(), reqBO.getOperatorUserId(), reqBO.getAttachments());
        clubAttachmentService.lockBizAttachments(BIZ_TYPE_CONTRIBUTION_MATERIAL, fraudMaterial.getId());
        clubAttachmentService.lockBizAttachments(BIZ_TYPE_CONTRIBUTION_MATERIAL, originalMaterial.getId());
        Long auditLogId = createFraudHandleAudit(reqBO, fraudMaterial, originalMaterial, originalItems);
        reverseOriginalTransactions(reqBO, fraudMaterial, originalItems, operationTime);
        Set<Long> affectedUserIds = collectAffectedUserIds(originalItems);
        Map<Long, Integer> deductedPoints = new LinkedHashMap<>();
        Map<Long, Long> deductionTransactionIds = createFraudDeductTransactions(reqBO, fraudMaterial,
                originalItems, fraudRuleItem, operationTime, auditLogId, deductedPoints);
        cancelAnnualHonor(reqBO, operationTime, affectedUserIds, deductionTransactionIds);
        materialMapper.updateById(fraudMaterial);
        notifyFraudAffectedUsers(reqBO, affectedUserIds, deductedPoints);
        return fraudMaterial.getId();
    }

    private ClubPointContributionMaterialDO validateMaterialExists(Long materialId) {
        ClubPointContributionMaterialDO material = materialMapper.selectById(materialId);
        if (material == null) {
            throw exception(CLUB_CONTRIBUTION_MATERIAL_NOT_FOUND);
        }
        return material;
    }

    private ClubPointContributionDetailBO buildDetail(ClubPointContributionMaterialDO material) {
        return new ClubPointContributionDetailBO()
                .setMaterial(material)
                .setItems(itemMapper.selectListByMaterialId(material.getId()));
    }

    private static void validateLeaderPageReq(Long operatorUserId, ClubPointContributionPageReqBO reqBO) {
        if (operatorUserId == null || reqBO == null || reqBO.getClubId() == null) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
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

    private static void validateDirectCreateReq(ClubPointContributionDirectCreateReqBO reqBO) {
        if (reqBO == null || !StringUtils.hasText(reqBO.getRequestNo())
                || reqBO.getType() == null || reqBO.getUserId() == null
                || !StringUtils.hasText(reqBO.getUserNameSnapshot())
                || reqBO.getPoints() == null || reqBO.getPoints() <= 0
                || reqBO.getRuleVersionId() == null || reqBO.getOperatorUserId() == null
                || !StringUtils.hasText(reqBO.getReason())) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
        if (reqBO.getAttachments() == null || reqBO.getAttachments().isEmpty()) {
            throw exception(CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED);
        }
    }

    private ClubPointRuleItemDO validateDirectRuleAndPoints(ClubPointContributionDirectCreateReqBO reqBO,
                                                            ClubPointContributionMaterialTypeEnum materialType) {
        ClubPointRuleItemDO ruleItem = ruleResolveService.getItem(reqBO.getRuleVersionId(),
                materialType.getRuleItemCode());
        try {
            ruleResolveService.validatePointsInRange(reqBO.getRuleVersionId(),
                    materialType.getRuleItemCode(), reqBO.getPoints());
        } catch (ServiceException ex) {
            if (Objects.equals(CLUB_RULE_VALUE_OUT_OF_RANGE.getCode(), ex.getCode())) {
                throw exception(CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE);
            }
            throw ex;
        }
        return ruleItem;
    }

    private static void validateViolationDeductReq(ClubPointContributionViolationDeductReqBO reqBO) {
        if (reqBO == null || !StringUtils.hasText(reqBO.getRequestNo())
                || reqBO.getClubId() == null || reqBO.getUserId() == null
                || !StringUtils.hasText(reqBO.getUserNameSnapshot())
                || reqBO.getPoints() == null || reqBO.getPoints() <= 0
                || reqBO.getRuleVersionId() == null || reqBO.getOperatorUserId() == null
                || !StringUtils.hasText(reqBO.getReason())) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
        if (reqBO.getAttachments() == null || reqBO.getAttachments().isEmpty()) {
            throw exception(CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED);
        }
    }

    private ClubPointRuleItemDO validateViolationRuleAndPoints(ClubPointContributionViolationDeductReqBO reqBO) {
        ClubPointRuleItemDO ruleItem = ruleResolveService.getItem(reqBO.getRuleVersionId(),
                VIOLATION_DEDUCT.getRuleItemCode());
        try {
            ruleResolveService.validatePointsInRange(reqBO.getRuleVersionId(),
                    VIOLATION_DEDUCT.getRuleItemCode(), reqBO.getPoints());
        } catch (ServiceException ex) {
            if (Objects.equals(CLUB_RULE_VALUE_OUT_OF_RANGE.getCode(), ex.getCode())) {
                throw exception(CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE);
            }
            throw ex;
        }
        return ruleItem;
    }

    private static void validateFraudHandleReq(ClubPointContributionFraudHandleReqBO reqBO) {
        if (reqBO == null || !StringUtils.hasText(reqBO.getRequestNo())
                || reqBO.getOriginalMaterialId() == null || reqBO.getRuleVersionId() == null
                || reqBO.getOperatorUserId() == null || !StringUtils.hasText(reqBO.getReason())) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
        if (reqBO.getAttachments() == null || reqBO.getAttachments().isEmpty()) {
            throw exception(CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED);
        }
    }

    private static void validateFraudOriginalMaterial(ClubPointContributionMaterialDO originalMaterial) {
        if (!ClubPointContributionMaterialStatusEnum.APPROVED.getStatus().equals(originalMaterial.getStatus())) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
    }

    private void validateFraudOriginalItems(List<ClubPointContributionItemDO> originalItems) {
        if (originalItems.isEmpty()) {
            throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
        }
        for (ClubPointContributionItemDO item : originalItems) {
            if (item.getTransactionId() == null) {
                throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
            }
            ClubPointTransactionDO transaction = transactionMapper.selectById(item.getTransactionId());
            if (transaction == null || !INCREASE.getDirection().equals(transaction.getDirection())) {
                throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
            }
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

    private static ClubPointContributionMaterialDO buildDirectMaterial(ClubPointContributionDirectCreateReqBO reqBO,
                                                                       ClubPointClubDO club,
                                                                       LocalDateTime operationTime) {
        return new ClubPointContributionMaterialDO()
                .setClubId(club == null ? null : club.getId())
                .setClubNameSnapshot(club == null ? null : club.getName())
                .setType(reqBO.getType())
                .setTitle("管理员代录积分")
                .setDescription(reqBO.getReason())
                .setStatus(ClubPointContributionMaterialStatusEnum.APPROVED.getStatus())
                .setRuleVersionId(reqBO.getRuleVersionId())
                .setSubmitterUserId(reqBO.getOperatorUserId())
                .setSubmitTime(operationTime)
                .setReviewerUserId(reqBO.getOperatorUserId())
                .setReviewTime(operationTime)
                .setReviewReason(reqBO.getReason())
                .setLocked(true)
                .setDirectCreated(true)
                .setRequestNo(reqBO.getRequestNo());
    }

    private Long insertMaterialWithRequestNo(ClubPointContributionMaterialDO material, String requestNo) {
        try {
            materialMapper.insert(material);
            return null;
        } catch (DuplicateKeyException ex) {
            ClubPointContributionMaterialDO duplicated = materialMapper.selectByRequestNo(requestNo);
            if (duplicated != null) {
                return duplicated.getId();
            }
            throw ex;
        }
    }

    private static ClubPointContributionItemDO buildDirectItem(ClubPointContributionMaterialDO material,
                                                               ClubPointContributionDirectCreateReqBO reqBO,
                                                               ClubPointContributionMaterialTypeEnum materialType,
                                                               ClubPointRuleItemDO ruleItem) {
        return new ClubPointContributionItemDO()
                .setMaterialId(material.getId())
                .setClubId(material.getClubId())
                .setUserId(reqBO.getUserId())
                .setUserNameSnapshot(reqBO.getUserNameSnapshot())
                .setDeptNameSnapshot(reqBO.getDeptNameSnapshot())
                .setPointCategory(materialType.getPointCategory())
                .setRuleItemId(ruleItem.getId())
                .setRuleItemCode(materialType.getRuleItemCode())
                .setDirection(materialType.getDirection())
                .setPoints(reqBO.getPoints())
                .setReason(reqBO.getReason())
                .setMaterialSummary(reqBO.getReason())
                .setIdempotencyKey(directIdempotencyKey(reqBO.getRequestNo()));
    }

    private static ClubPointContributionMaterialDO buildViolationMaterial(
            ClubPointContributionViolationDeductReqBO reqBO, ClubPointClubDO club, LocalDateTime operationTime) {
        return new ClubPointContributionMaterialDO()
                .setClubId(club.getId())
                .setClubNameSnapshot(club.getName())
                .setType(VIOLATION_DEDUCT.getType())
                .setTitle("管理员违规扣分")
                .setDescription(reqBO.getReason())
                .setStatus(ClubPointContributionMaterialStatusEnum.APPROVED.getStatus())
                .setRuleVersionId(reqBO.getRuleVersionId())
                .setSubmitterUserId(reqBO.getOperatorUserId())
                .setSubmitTime(operationTime)
                .setReviewerUserId(reqBO.getOperatorUserId())
                .setReviewTime(operationTime)
                .setReviewReason(reqBO.getReason())
                .setLocked(true)
                .setDirectCreated(true)
                .setRequestNo(reqBO.getRequestNo());
    }

    private static ClubPointContributionItemDO buildViolationItem(ClubPointContributionMaterialDO material,
                                                                  ClubPointContributionViolationDeductReqBO reqBO,
                                                                  ClubPointRuleItemDO ruleItem) {
        return new ClubPointContributionItemDO()
                .setMaterialId(material.getId())
                .setClubId(material.getClubId())
                .setUserId(reqBO.getUserId())
                .setUserNameSnapshot(reqBO.getUserNameSnapshot())
                .setDeptNameSnapshot(reqBO.getDeptNameSnapshot())
                .setPointCategory(VIOLATION_DEDUCT.getPointCategory())
                .setRuleItemId(ruleItem.getId())
                .setRuleItemCode(VIOLATION_DEDUCT.getRuleItemCode())
                .setDirection(VIOLATION_DEDUCT.getDirection())
                .setPoints(reqBO.getPoints())
                .setReason(reqBO.getReason())
                .setMaterialSummary(reqBO.getReason())
                .setIdempotencyKey(violationDeductIdempotencyKey(reqBO.getRequestNo()));
    }

    private static ClubPointContributionMaterialDO buildFraudMaterial(ClubPointContributionFraudHandleReqBO reqBO,
                                                                      ClubPointContributionMaterialDO originalMaterial,
                                                                      LocalDateTime operationTime) {
        return new ClubPointContributionMaterialDO()
                .setClubId(originalMaterial.getClubId())
                .setClubNameSnapshot(originalMaterial.getClubNameSnapshot())
                .setType(FRAUD_HANDLE.getType())
                .setTitle("管理员弄虚作假处理")
                .setDescription(reqBO.getReason())
                .setStatus(ClubPointContributionMaterialStatusEnum.APPROVED.getStatus())
                .setRuleVersionId(reqBO.getRuleVersionId())
                .setSubmitterUserId(reqBO.getOperatorUserId())
                .setSubmitTime(operationTime)
                .setReviewerUserId(reqBO.getOperatorUserId())
                .setReviewTime(operationTime)
                .setReviewReason(reqBO.getReason())
                .setLocked(true)
                .setDirectCreated(true)
                .setRequestNo(reqBO.getRequestNo());
    }

    private static ClubPointContributionItemDO buildFraudItem(ClubPointContributionMaterialDO fraudMaterial,
                                                              ClubPointContributionItemDO originalItem,
                                                              ClubPointRuleItemDO fraudRuleItem,
                                                              Integer availablePoints,
                                                              String reason,
                                                              String requestNo) {
        return new ClubPointContributionItemDO()
                .setMaterialId(fraudMaterial.getId())
                .setClubId(fraudMaterial.getClubId())
                .setUserId(originalItem.getUserId())
                .setUserNameSnapshot(originalItem.getUserNameSnapshot())
                .setDeptNameSnapshot(originalItem.getDeptNameSnapshot())
                .setPointCategory(FRAUD_HANDLE.getPointCategory())
                .setRuleItemId(fraudRuleItem.getId())
                .setRuleItemCode(FRAUD_HANDLE.getRuleItemCode())
                .setDirection(FRAUD_HANDLE.getDirection())
                .setPoints(availablePoints)
                .setReason(reason)
                .setMaterialSummary(reason)
                .setIdempotencyKey(fraudDeductIdempotencyKey(requestNo, originalItem.getUserId()));
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

    private void reverseOriginalTransactions(ClubPointContributionFraudHandleReqBO reqBO,
                                             ClubPointContributionMaterialDO fraudMaterial,
                                             List<ClubPointContributionItemDO> originalItems,
                                             LocalDateTime operationTime) {
        for (ClubPointContributionItemDO item : originalItems) {
            ledgerService.reverseTransaction(new ClubPointLedgerReverseReqBO()
                    .setSourceTransactionId(item.getTransactionId())
                    .setTransactionNo(fraudReverseTransactionNo(reqBO.getRequestNo(), item.getTransactionId()))
                    .setReason(reqBO.getReason())
                    .setOccurredAt(operationTime)
                    .setAttachmentSnapshotJson(fraudMaterial.getSnapshotJson())
                    .setOperatorUserId(reqBO.getOperatorUserId())
                    .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                    .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                    .setClientIp(reqBO.getClientIp())
                    .setUserAgent(reqBO.getUserAgent()));
        }
    }

    private Map<Long, Long> createFraudDeductTransactions(ClubPointContributionFraudHandleReqBO reqBO,
                                                          ClubPointContributionMaterialDO fraudMaterial,
                                                          List<ClubPointContributionItemDO> originalItems,
                                                          ClubPointRuleItemDO fraudRuleItem,
                                                          LocalDateTime operationTime,
                                                          Long auditLogId,
                                                          Map<Long, Integer> deductedPoints) {
        Map<Long, Long> transactionIds = new LinkedHashMap<>();
        for (Long userId : collectAffectedUserIds(originalItems)) {
            ClubPointAccountDO account = accountMapper.selectByUserIdForUpdate(userId);
            int availablePoints = account == null ? 0 : account.getAvailablePoints();
            if (availablePoints <= 0) {
                deductedPoints.put(userId, 0);
                continue;
            }
            ClubPointContributionItemDO originalItem = findOriginalItemByUserId(originalItems, userId);
            ClubPointContributionItemDO fraudItem = buildFraudItem(fraudMaterial, originalItem, fraudRuleItem,
                    availablePoints, reqBO.getReason(), reqBO.getRequestNo());
            itemMapper.insert(fraudItem);
            Long transactionId = ledgerService.createTransaction(buildFraudLedgerCreateReq(
                    fraudMaterial, fraudItem, reqBO, operationTime, auditLogId));
            itemMapper.updateById(new ClubPointContributionItemDO()
                    .setId(fraudItem.getId())
                    .setTransactionId(transactionId));
            transactionIds.put(userId, transactionId);
            deductedPoints.put(userId, availablePoints);
        }
        return transactionIds;
    }

    private static Set<Long> collectAffectedUserIds(List<ClubPointContributionItemDO> originalItems) {
        Set<Long> userIds = new LinkedHashSet<>();
        for (ClubPointContributionItemDO item : originalItems) {
            userIds.add(item.getUserId());
        }
        return userIds;
    }

    private static ClubPointContributionItemDO findOriginalItemByUserId(List<ClubPointContributionItemDO> originalItems,
                                                                        Long userId) {
        for (ClubPointContributionItemDO item : originalItems) {
            if (Objects.equals(item.getUserId(), userId)) {
                return item;
            }
        }
        throw exception(CLUB_CONTRIBUTION_STATUS_INVALID);
    }

    private void cancelAnnualHonor(ClubPointContributionFraudHandleReqBO reqBO,
                                   LocalDateTime operationTime,
                                   Set<Long> affectedUserIds,
                                   Map<Long, Long> deductionTransactionIds) {
        for (Long userId : affectedUserIds) {
            ClubPointUserYearStatusDO status = userYearStatusMapper.selectByUserIdAndYearForUpdate(
                    userId, operationTime.getYear());
            if (status == null) {
                status = new ClubPointUserYearStatusDO()
                        .setUserId(userId)
                        .setYear(operationTime.getYear())
                        .setAnnualPositivePoints(0)
                        .setAnnualNegativePoints(0);
            }
            status.setHonorEligible(false)
                    .setHonorCancelReason(reqBO.getReason())
                    .setHonorCancelTransactionId(deductionTransactionIds.get(userId))
                    .setHonorCancelTime(operationTime)
                    .setRemark("FRAUD:" + reqBO.getRequestNo());
            if (status.getId() == null) {
                userYearStatusMapper.insert(status);
            } else {
                userYearStatusMapper.updateById(status);
            }
        }
    }

    private void notifyFraudAffectedUsers(ClubPointContributionFraudHandleReqBO reqBO,
                                          Set<Long> affectedUserIds,
                                          Map<Long, Integer> deductedPoints) {
        for (Long userId : affectedUserIds) {
            ClubPointAccountDO account = accountMapper.selectByUserId(userId);
            try {
                clubNotifyService.notifyPointsChanged(userId, "弄虚作假处理：" + reqBO.getReason(), "-",
                        deductedPoints.get(userId), account == null ? 0 : account.getAvailablePoints());
            } catch (Exception ignored) {
                // 通知是告知链路，失败不能回滚弄虚作假业务处理。
            }
        }
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

    private static ClubPointLedgerCreateReqBO buildDirectLedgerCreateReq(ClubPointContributionMaterialDO material,
                                                                         ClubPointContributionItemDO item,
                                                                         ClubPointContributionDirectCreateReqBO reqBO,
                                                                         LocalDateTime operationTime,
                                                                         Long auditLogId) {
        return new ClubPointLedgerCreateReqBO()
                .setTransactionNo(reqBO.getRequestNo())
                .setUserId(item.getUserId())
                .setUserNameSnapshot(item.getUserNameSnapshot())
                .setDeptNameSnapshot(item.getDeptNameSnapshot())
                .setDirection(item.getDirection())
                .setPoints(item.getPoints())
                .setPointCategory(item.getPointCategory())
                .setPointTypeCode(item.getRuleItemCode())
                .setSourceType(ADMIN_DIRECT.getType())
                .setSourceId(material.getId())
                .setSourceItemId(item.getId())
                .setSourceTitleSnapshot(material.getTitle())
                .setIssuingClubId(material.getClubId())
                .setIssuingClubNameSnapshot(material.getClubNameSnapshot())
                .setMaterialSummary(item.getMaterialSummary())
                .setReason(item.getReason())
                .setOccurredAt(operationTime)
                .setIdempotencyKey(directIdempotencyKey(reqBO.getRequestNo()))
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(auditLogId)
                .setRuleItemCode(item.getRuleItemCode())
                .setRuleVersionId(material.getRuleVersionId())
                .setSourceSnapshotJson(material.getSnapshotJson());
    }

    private static ClubPointLedgerCreateReqBO buildViolationLedgerCreateReq(
            ClubPointContributionMaterialDO material, ClubPointContributionItemDO item,
            ClubPointContributionViolationDeductReqBO reqBO, LocalDateTime operationTime, Long auditLogId) {
        return new ClubPointLedgerCreateReqBO()
                .setTransactionNo(reqBO.getRequestNo())
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
                .setOccurredAt(operationTime)
                .setIdempotencyKey(violationDeductIdempotencyKey(reqBO.getRequestNo()))
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(auditLogId)
                .setRuleItemCode(item.getRuleItemCode())
                .setRuleVersionId(material.getRuleVersionId())
                .setSourceSnapshotJson(material.getSnapshotJson());
    }

    private static ClubPointLedgerCreateReqBO buildFraudLedgerCreateReq(
            ClubPointContributionMaterialDO fraudMaterial, ClubPointContributionItemDO fraudItem,
            ClubPointContributionFraudHandleReqBO reqBO, LocalDateTime operationTime, Long auditLogId) {
        return new ClubPointLedgerCreateReqBO()
                .setTransactionNo(fraudDeductTransactionNo(reqBO.getRequestNo(), fraudItem.getUserId()))
                .setUserId(fraudItem.getUserId())
                .setUserNameSnapshot(fraudItem.getUserNameSnapshot())
                .setDeptNameSnapshot(fraudItem.getDeptNameSnapshot())
                .setDirection(fraudItem.getDirection())
                .setPoints(fraudItem.getPoints())
                .setPointCategory(fraudItem.getPointCategory())
                .setPointTypeCode(fraudItem.getRuleItemCode())
                .setSourceType(CONTRIBUTION_MATERIAL.getType())
                .setSourceId(fraudMaterial.getId())
                .setSourceItemId(fraudItem.getId())
                .setSourceTitleSnapshot(fraudMaterial.getTitle())
                .setIssuingClubId(fraudMaterial.getClubId())
                .setIssuingClubNameSnapshot(fraudMaterial.getClubNameSnapshot())
                .setMaterialSummary(fraudItem.getMaterialSummary())
                .setReason(fraudItem.getReason())
                .setOccurredAt(operationTime)
                .setIdempotencyKey(fraudDeductIdempotencyKey(reqBO.getRequestNo(), fraudItem.getUserId()))
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setAuditLogId(auditLogId)
                .setRuleItemCode(fraudItem.getRuleItemCode())
                .setRuleVersionId(fraudMaterial.getRuleVersionId())
                .setSourceSnapshotJson(fraudMaterial.getSnapshotJson());
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

    private Long createDirectCreateAudit(ClubPointContributionDirectCreateReqBO reqBO,
                                         ClubPointContributionMaterialDO material,
                                         ClubPointContributionItemDO item) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(CONTRIBUTION_DIRECT_CREATE)
                .setBizType(BIZ_TYPE_CONTRIBUTION_MATERIAL)
                .setBizId(material.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(material.getReviewTime())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(null)
                .setAfterJson(directCreateAuditSnapshot(material, item, reqBO.getRequestNo()))
                .setTargetSnapshotJson(material.getSnapshotJson())
                .setSuccess(true));
    }

    private Long createViolationDeductAudit(ClubPointContributionViolationDeductReqBO reqBO,
                                            ClubPointContributionMaterialDO material,
                                            ClubPointContributionItemDO item) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(CONTRIBUTION_VIOLATION_DEDUCT)
                .setBizType(BIZ_TYPE_CONTRIBUTION_MATERIAL)
                .setBizId(material.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(material.getReviewTime())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(null)
                .setAfterJson(violationDeductAuditSnapshot(material, item, reqBO.getRequestNo()))
                .setTargetSnapshotJson(material.getSnapshotJson())
                .setSuccess(true));
    }

    private Long createFraudHandleAudit(ClubPointContributionFraudHandleReqBO reqBO,
                                        ClubPointContributionMaterialDO fraudMaterial,
                                        ClubPointContributionMaterialDO originalMaterial,
                                        List<ClubPointContributionItemDO> originalItems) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(CONTRIBUTION_FRAUD_HANDLE)
                .setBizType(BIZ_TYPE_CONTRIBUTION_MATERIAL)
                .setBizId(fraudMaterial.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(fraudMaterial.getReviewTime())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(originalMaterial.getSnapshotJson())
                .setAfterJson(fraudHandleAuditSnapshot(fraudMaterial, originalMaterial, originalItems))
                .setTargetSnapshotJson(fraudMaterial.getSnapshotJson())
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

    private static String directIdempotencyKey(String requestNo) {
        return "DIRECT_CONTRIBUTION:" + requestNo;
    }

    private static String violationDeductIdempotencyKey(String requestNo) {
        return "VIOLATION_DEDUCT:" + requestNo;
    }

    private static String fraudDeductTransactionNo(String requestNo, Long userId) {
        return "FRAUD_CLEAR_ALL:" + requestNo + ":" + userId;
    }

    private static String fraudDeductIdempotencyKey(String requestNo, Long userId) {
        return "FRAUD_CLEAR_ALL:" + requestNo + ":" + userId;
    }

    private static String fraudReverseTransactionNo(String requestNo, Long sourceTransactionId) {
        return "FRAUD_REVERSE:" + requestNo + ":" + sourceTransactionId;
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
        snapshot.put("requestNo", material.getRequestNo());
        snapshot.put("itemCount", itemCount);
        return JsonUtils.toJsonString(snapshot);
    }

    private static String fraudMaterialSnapshot(ClubPointContributionMaterialDO fraudMaterial,
                                                ClubPointContributionMaterialDO originalMaterial,
                                                int originalItemCount) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", fraudMaterial.getId());
        snapshot.put("requestNo", fraudMaterial.getRequestNo());
        snapshot.put("originalMaterialId", originalMaterial.getId());
        snapshot.put("clubId", fraudMaterial.getClubId());
        snapshot.put("clubNameSnapshot", fraudMaterial.getClubNameSnapshot());
        snapshot.put("type", fraudMaterial.getType());
        snapshot.put("status", fraudMaterial.getStatus());
        snapshot.put("ruleVersionId", fraudMaterial.getRuleVersionId());
        snapshot.put("reviewerUserId", fraudMaterial.getReviewerUserId());
        snapshot.put("reviewTime", fraudMaterial.getReviewTime());
        snapshot.put("reviewReason", fraudMaterial.getReviewReason());
        snapshot.put("locked", fraudMaterial.getLocked());
        snapshot.put("directCreated", fraudMaterial.getDirectCreated());
        snapshot.put("originalItemCount", originalItemCount);
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

    private static String directCreateAuditSnapshot(ClubPointContributionMaterialDO material,
                                                    ClubPointContributionItemDO item,
                                                    String requestNo) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", material.getId());
        snapshot.put("requestNo", requestNo);
        snapshot.put("status", material.getStatus());
        snapshot.put("userId", item.getUserId());
        snapshot.put("points", item.getPoints());
        snapshot.put("ruleItemCode", item.getRuleItemCode());
        snapshot.put("locked", material.getLocked());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String violationDeductAuditSnapshot(ClubPointContributionMaterialDO material,
                                                       ClubPointContributionItemDO item,
                                                       String requestNo) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", material.getId());
        snapshot.put("requestNo", requestNo);
        snapshot.put("status", material.getStatus());
        snapshot.put("userId", item.getUserId());
        snapshot.put("points", item.getPoints());
        snapshot.put("direction", item.getDirection());
        snapshot.put("ruleItemCode", item.getRuleItemCode());
        snapshot.put("locked", material.getLocked());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String fraudHandleAuditSnapshot(ClubPointContributionMaterialDO fraudMaterial,
                                                   ClubPointContributionMaterialDO originalMaterial,
                                                   List<ClubPointContributionItemDO> originalItems) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", fraudMaterial.getId());
        snapshot.put("requestNo", fraudMaterial.getRequestNo());
        snapshot.put("originalMaterialId", originalMaterial.getId());
        snapshot.put("status", fraudMaterial.getStatus());
        snapshot.put("ruleItemCode", FRAUD_HANDLE.getRuleItemCode());
        snapshot.put("affectedUserCount", collectAffectedUserIds(originalItems).size());
        snapshot.put("originalTransactionCount", originalItems.size());
        snapshot.put("locked", fraudMaterial.getLocked());
        return JsonUtils.toJsonString(snapshot);
    }

}
