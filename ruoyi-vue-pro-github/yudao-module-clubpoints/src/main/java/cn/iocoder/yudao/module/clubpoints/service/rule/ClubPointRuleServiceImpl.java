package cn.iocoder.yudao.module.clubpoints.service.rule;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRulePublishRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRulePublishRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleVersionSaveReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.RULE_DISABLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.RULE_PUBLISH;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.RULE_WITHDRAW;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_ITEM_CODE_DUPLICATED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_VERSION_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_VERSION_STATUS_INVALID;

/**
 * 积分规则版本服务实现
 */
@Service
public class ClubPointRuleServiceImpl implements ClubPointRuleService {

    private static final int ITEM_STATUS_ENABLED = 1;
    private static final int PUBLISH_ACTION_PUBLISH = 1;
    private static final int PUBLISH_ACTION_WITHDRAW = 2;
    private static final int PUBLISH_ACTION_DISABLE = 3;
    private static final int PUBLISH_ACTION_REPLACE = 4;
    private static final String BIZ_TYPE_RULE_VERSION = "RULE_VERSION";

    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;
    @Resource
    private ClubPointRulePublishRecordMapper publishRecordMapper;
    @Resource
    private ClubAuditService clubAuditService;

    @Override
    public Long createDraftVersion(ClubPointRuleVersionSaveReqBO reqBO) {
        ClubPointRuleVersionDO version = buildVersion(reqBO)
                .setStatus(ClubPointRuleVersionStatusEnum.DRAFT.getStatus());
        ruleVersionMapper.insert(version);
        return version.getId();
    }

    @Override
    public void updateDraftVersion(ClubPointRuleVersionSaveReqBO reqBO) {
        ClubPointRuleVersionDO version = validateDraftVersion(reqBO.getId());
        updateVersion(version, reqBO);
        ruleVersionMapper.updateById(version);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyVersion(Long sourceVersionId, ClubPointRuleVersionSaveReqBO reqBO) {
        ClubPointRuleVersionDO source = validateRuleVersionExists(sourceVersionId);
        ClubPointRuleVersionDO copied = buildVersion(reqBO)
                .setStatus(ClubPointRuleVersionStatusEnum.DRAFT.getStatus());
        ruleVersionMapper.insert(copied);
        for (ClubPointRuleItemDO sourceItem : ruleItemMapper.selectListByRuleVersionId(source.getId())) {
            ruleItemMapper.insert(copyRuleItem(sourceItem, copied.getId()));
        }
        return copied.getId();
    }

    @Override
    public Long createRuleItem(ClubPointRuleItemSaveReqBO reqBO) {
        ClubPointRuleVersionDO version = validateDraftVersion(reqBO.getRuleVersionId());
        validateRuleItemCodeNotDuplicated(version.getId(), reqBO.getItemCode(), null);
        ClubPointRuleItemDO ruleItem = buildRuleItem(reqBO).setRuleVersionId(version.getId());
        ruleItemMapper.insert(ruleItem);
        return ruleItem.getId();
    }

    @Override
    public void updateDraftRuleItem(ClubPointRuleItemSaveReqBO reqBO) {
        ClubPointRuleItemDO ruleItem = validateRuleItemExists(reqBO.getId());
        validateDraftVersion(ruleItem.getRuleVersionId());
        validateRuleItemCodeNotDuplicated(ruleItem.getRuleVersionId(), reqBO.getItemCode(), ruleItem.getId());
        updateRuleItem(ruleItem, reqBO);
        ruleItemMapper.updateById(ruleItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishVersion(Long ruleVersionId, ClubPointRuleOperationReqBO reqBO) {
        ClubPointRuleVersionDO version = validateRuleVersionExists(ruleVersionId);
        if (!ClubPointRuleVersionStatusEnum.DRAFT.getStatus().equals(version.getStatus())) {
            throw exception(CLUB_RULE_VERSION_STATUS_INVALID);
        }
        LocalDateTime operationTime = LocalDateTime.now();
        Long auditLogId = createRuleAudit(RULE_PUBLISH, version, reqBO, operationTime,
                snapshot(version), snapshot(version, ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus()));

        disableOtherPublishedVersions(version.getId(), reqBO, operationTime, auditLogId);

        version.setStatus(ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus())
                .setPublishedTime(operationTime);
        ruleVersionMapper.updateById(version);
        insertPublishRecord(version.getId(), PUBLISH_ACTION_PUBLISH, reqBO, operationTime, auditLogId,
                snapshot(version, ClubPointRuleVersionStatusEnum.DRAFT.getStatus()), snapshot(version));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawVersion(Long ruleVersionId, ClubPointRuleOperationReqBO reqBO) {
        ClubPointRuleVersionDO version = validateRuleVersionExists(ruleVersionId);
        if (!ClubPointRuleVersionStatusEnum.DRAFT.getStatus().equals(version.getStatus())) {
            throw exception(CLUB_RULE_VERSION_STATUS_INVALID);
        }
        LocalDateTime operationTime = LocalDateTime.now();
        Long auditLogId = createRuleAudit(RULE_WITHDRAW, version, reqBO, operationTime,
                snapshot(version), snapshot(version, ClubPointRuleVersionStatusEnum.WITHDRAWN.getStatus()));
        version.setStatus(ClubPointRuleVersionStatusEnum.WITHDRAWN.getStatus());
        ruleVersionMapper.updateById(version);
        insertPublishRecord(version.getId(), PUBLISH_ACTION_WITHDRAW, reqBO, operationTime, auditLogId,
                snapshot(version, ClubPointRuleVersionStatusEnum.DRAFT.getStatus()), snapshot(version));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableVersion(Long ruleVersionId, ClubPointRuleOperationReqBO reqBO) {
        ClubPointRuleVersionDO version = validateRuleVersionExists(ruleVersionId);
        if (!ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus().equals(version.getStatus())) {
            throw exception(CLUB_RULE_VERSION_STATUS_INVALID);
        }
        LocalDateTime operationTime = LocalDateTime.now();
        Long auditLogId = createRuleAudit(RULE_DISABLE, version, reqBO, operationTime,
                snapshot(version), snapshot(version, ClubPointRuleVersionStatusEnum.DISABLED.getStatus()));
        version.setStatus(ClubPointRuleVersionStatusEnum.DISABLED.getStatus())
                .setDisabledTime(operationTime);
        ruleVersionMapper.updateById(version);
        insertPublishRecord(version.getId(), PUBLISH_ACTION_DISABLE, reqBO, operationTime, auditLogId,
                snapshot(version, ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus()), snapshot(version));
    }

    @Override
    public PageResult<ClubPointRuleVersionDO> getRuleVersionPage(PageParam pageParam, String versionNo,
                                                                 String name, Integer status) {
        return ruleVersionMapper.selectPage(pageParam, versionNo, name, status);
    }

    @Override
    public ClubPointRuleVersionDO getRuleVersion(Long ruleVersionId) {
        return validateRuleVersionExists(ruleVersionId);
    }

    @Override
    public ClubPointRuleVersionDO getCurrentRuleVersion() {
        ClubPointRuleVersionDO version = ruleVersionMapper.selectCurrentPublished(
                ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus(), LocalDateTime.now());
        if (version == null) {
            throw exception(CLUB_RULE_VERSION_NOT_EXISTS);
        }
        return version;
    }

    @Override
    public List<ClubPointRuleItemDO> getRuleItemList(Long ruleVersionId) {
        validateRuleVersionExists(ruleVersionId);
        return ruleItemMapper.selectListByRuleVersionId(ruleVersionId);
    }

    @Override
    public ClubPointRuleItemDO getRuleItemByCode(String code) {
        ClubPointRuleVersionDO version = getCurrentRuleVersion();
        ClubPointRuleItemDO ruleItem = ruleItemMapper.selectByRuleVersionIdAndItemCodeAndStatus(
                version.getId(), code, ITEM_STATUS_ENABLED);
        if (ruleItem == null) {
            throw exception(CLUB_RULE_ITEM_NOT_EXISTS);
        }
        return ruleItem;
    }

    private void disableOtherPublishedVersions(Long currentVersionId, ClubPointRuleOperationReqBO reqBO,
                                               LocalDateTime operationTime, Long auditLogId) {
        List<ClubPointRuleVersionDO> publishedVersions = ruleVersionMapper.selectListByStatus(
                ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus());
        for (ClubPointRuleVersionDO publishedVersion : publishedVersions) {
            if (publishedVersion.getId().equals(currentVersionId)) {
                continue;
            }
            String beforeJson = snapshot(publishedVersion);
            publishedVersion.setStatus(ClubPointRuleVersionStatusEnum.DISABLED.getStatus())
                    .setDisabledTime(operationTime);
            ruleVersionMapper.updateById(publishedVersion);
            insertPublishRecord(publishedVersion.getId(), PUBLISH_ACTION_REPLACE, reqBO, operationTime, auditLogId,
                    beforeJson, snapshot(publishedVersion));
        }
    }

    private ClubPointRuleVersionDO validateRuleVersionExists(Long ruleVersionId) {
        ClubPointRuleVersionDO version = ruleVersionMapper.selectById(ruleVersionId);
        if (version == null) {
            throw exception(CLUB_RULE_VERSION_NOT_EXISTS);
        }
        return version;
    }

    private ClubPointRuleVersionDO validateDraftVersion(Long ruleVersionId) {
        ClubPointRuleVersionDO version = validateRuleVersionExists(ruleVersionId);
        if (!ClubPointRuleVersionStatusEnum.DRAFT.getStatus().equals(version.getStatus())) {
            throw exception(CLUB_RULE_VERSION_STATUS_INVALID);
        }
        return version;
    }

    private ClubPointRuleItemDO validateRuleItemExists(Long ruleItemId) {
        ClubPointRuleItemDO ruleItem = ruleItemMapper.selectById(ruleItemId);
        if (ruleItem == null) {
            throw exception(CLUB_RULE_ITEM_NOT_EXISTS);
        }
        return ruleItem;
    }

    private void validateRuleItemCodeNotDuplicated(Long ruleVersionId, String itemCode, Long excludeId) {
        ClubPointRuleItemDO existed = ruleItemMapper.selectByRuleVersionIdAndItemCode(ruleVersionId, itemCode);
        if (existed != null && (excludeId == null || !excludeId.equals(existed.getId()))) {
            throw exception(CLUB_RULE_ITEM_CODE_DUPLICATED);
        }
    }

    private Long createRuleAudit(String actionType, ClubPointRuleVersionDO version, ClubPointRuleOperationReqBO reqBO,
                                 LocalDateTime operationTime, String beforeJson, String afterJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(actionType)
                .setBizType(BIZ_TYPE_RULE_VERSION)
                .setBizId(version.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(operationTime)
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setSuccess(true));
    }

    private void insertPublishRecord(Long ruleVersionId, Integer action, ClubPointRuleOperationReqBO reqBO,
                                     LocalDateTime operationTime, Long auditLogId,
                                     String beforeJson, String afterJson) {
        publishRecordMapper.insert(new ClubPointRulePublishRecordDO()
                .setRuleVersionId(ruleVersionId)
                .setAction(action)
                .setOperatorUserId(reqBO != null ? reqBO.getOperatorUserId() : 0L)
                .setOperationTime(operationTime)
                .setReason(reqBO != null ? reqBO.getReason() : "发布新版本替代")
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setAuditLogId(auditLogId));
    }

    private static ClubPointRuleVersionDO buildVersion(ClubPointRuleVersionSaveReqBO reqBO) {
        return updateVersion(new ClubPointRuleVersionDO(), reqBO);
    }

    private static ClubPointRuleVersionDO updateVersion(ClubPointRuleVersionDO version,
                                                        ClubPointRuleVersionSaveReqBO reqBO) {
        return version.setVersionNo(reqBO.getVersionNo())
                .setName(reqBO.getName())
                .setPublicityTime(reqBO.getPublicityTime())
                .setEffectiveTime(reqBO.getEffectiveTime())
                .setSummary(reqBO.getSummary())
                .setContent(reqBO.getContent())
                .setAttachmentSnapshotJson(reqBO.getAttachmentSnapshotJson())
                .setRemark(reqBO.getRemark());
    }

    private static ClubPointRuleItemDO copyRuleItem(ClubPointRuleItemDO sourceItem, Long targetVersionId) {
        return new ClubPointRuleItemDO()
                .setRuleVersionId(targetVersionId)
                .setItemCode(sourceItem.getItemCode())
                .setItemName(sourceItem.getItemName())
                .setItemType(sourceItem.getItemType())
                .setCategory(sourceItem.getCategory())
                .setMinPoints(sourceItem.getMinPoints())
                .setMaxPoints(sourceItem.getMaxPoints())
                .setDefaultPoints(sourceItem.getDefaultPoints())
                .setIntValue(sourceItem.getIntValue())
                .setDecimalValue(sourceItem.getDecimalValue())
                .setTextValue(sourceItem.getTextValue())
                .setJsonValue(sourceItem.getJsonValue())
                .setStatus(sourceItem.getStatus())
                .setSort(sourceItem.getSort())
                .setRemark(sourceItem.getRemark());
    }

    private static ClubPointRuleItemDO buildRuleItem(ClubPointRuleItemSaveReqBO reqBO) {
        return updateRuleItem(new ClubPointRuleItemDO(), reqBO);
    }

    private static ClubPointRuleItemDO updateRuleItem(ClubPointRuleItemDO ruleItem, ClubPointRuleItemSaveReqBO reqBO) {
        return ruleItem.setItemCode(reqBO.getItemCode())
                .setItemName(reqBO.getItemName())
                .setItemType(reqBO.getItemType())
                .setCategory(reqBO.getCategory())
                .setMinPoints(reqBO.getMinPoints())
                .setMaxPoints(reqBO.getMaxPoints())
                .setDefaultPoints(reqBO.getDefaultPoints())
                .setIntValue(reqBO.getIntValue())
                .setDecimalValue(reqBO.getDecimalValue())
                .setTextValue(reqBO.getTextValue())
                .setJsonValue(reqBO.getJsonValue())
                .setStatus(reqBO.getStatus())
                .setSort(reqBO.getSort())
                .setRemark(reqBO.getRemark());
    }

    private static String snapshot(ClubPointRuleVersionDO version) {
        return snapshot(version, version.getStatus());
    }

    private static String snapshot(ClubPointRuleVersionDO version, Integer status) {
        return "{\"id\":" + version.getId() + ",\"versionNo\":\"" + version.getVersionNo()
                + "\",\"status\":" + status + "}";
    }

}
