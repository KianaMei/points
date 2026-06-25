package cn.iocoder.yudao.module.clubpoints.controller.admin.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleItemRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleItemSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleVersionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleVersionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleVersionSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRulePublishRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRulePublishRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.RULE_DISABLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.RULE_PUBLISH;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.RULE_WITHDRAW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointRuleAdminController.class, ClubPointRuleServiceImpl.class, ClubAuditServiceImpl.class})
class ClubPointRuleAdminControllerTest extends BaseDbUnitTest {

    private static final String RULE_PERMISSION = "@ss.hasPermission('clubpoints:rule:manage')";
    private static final int STATUS_DRAFT = ClubPointRuleVersionStatusEnum.DRAFT.getStatus();
    private static final int STATUS_PUBLISHED = ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus();
    private static final int STATUS_WITHDRAWN = ClubPointRuleVersionStatusEnum.WITHDRAWN.getStatus();
    private static final int STATUS_DISABLED = ClubPointRuleVersionStatusEnum.DISABLED.getStatus();

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Resource
    private ClubPointRuleAdminController controller;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRulePublishRecordMapper publishRecordMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void endpointsShouldUseDocumentedRulePathsAndManagePermission() throws Exception {
        RequestMapping requestMapping = ClubPointRuleAdminController.class.getAnnotation(RequestMapping.class);
        assertEquals("/clubpoints/rule", requestMapping.value()[0]);

        assertMapping("getRuleVersionPage", new Class<?>[]{RuleVersionPageReqVO.class}, GetMapping.class, "/page");
        assertMapping("getRuleVersion", new Class<?>[]{Long.class}, GetMapping.class, "/get");
        assertMapping("createRuleVersion", new Class<?>[]{RuleVersionSaveReqVO.class}, PostMapping.class, "/create");
        assertMapping("updateRuleVersion", new Class<?>[]{RuleVersionSaveReqVO.class}, PutMapping.class, "/update");
        assertMapping("copyRuleVersion", new Class<?>[]{Long.class, RuleVersionSaveReqVO.class}, PostMapping.class, "/copy");
        assertMapping("publishRuleVersion", new Class<?>[]{RuleOperationReqVO.class, HttpServletRequest.class}, PostMapping.class, "/publish");
        assertMapping("withdrawRuleVersion", new Class<?>[]{RuleOperationReqVO.class, HttpServletRequest.class}, PostMapping.class, "/withdraw");
        assertMapping("disableRuleVersion", new Class<?>[]{RuleOperationReqVO.class, HttpServletRequest.class}, PostMapping.class, "/disable");
        assertMapping("getRuleItemList", new Class<?>[]{Long.class}, GetMapping.class, "/item-list");
        assertMapping("saveRuleItem", new Class<?>[]{RuleItemSaveReqVO.class}, PostMapping.class, "/item/save");
    }

    @Test
    void operationReqVOShouldNotExposeFrontendOperatorFields() {
        Set<String> fieldNames = java.util.Arrays.stream(RuleOperationReqVO.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        assertFalse(fieldNames.contains("operatorUserId"));
        assertFalse(fieldNames.contains("operatorNameSnapshot"));
        assertFalse(fieldNames.contains("operatorRoleSnapshot"));
    }

    @Test
    void ruleItemSaveReqVOShouldRejectInvalidPointRange() {
        RuleItemSaveReqVO reqVO = buildRuleItemSaveReq(null, 1L)
                .setMinPoints(10)
                .setMaxPoints(20)
                .setDefaultPoints(21);

        Set<ConstraintViolation<RuleItemSaveReqVO>> violations = VALIDATOR.validate(reqVO);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getMessage().contains("默认分值必须在最小值和最大值之间")));
    }

    @Test
    void ruleItemSaveReqVOShouldRejectInvalidItemType() {
        RuleItemSaveReqVO reqVO = buildRuleItemSaveReq(null, 1L)
                .setItemType(999);

        Set<ConstraintViolation<RuleItemSaveReqVO>> violations = VALIDATOR.validate(reqVO);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getMessage().contains("必须在指定范围")));
    }

    @Test
    void draftAndRuleItemEndpointsShouldPersistAndReadAdminData() {
        Long versionId = controller.createRuleVersion(buildVersionSaveReq(null, "V2026.30")).getCheckedData();

        controller.updateRuleVersion(buildVersionSaveReq(versionId, "V2026.30")
                .setName("更新后的规则")).checkError();
        RuleVersionRespVO detail = controller.getRuleVersion(versionId).getCheckedData();
        assertEquals("更新后的规则", detail.getName());
        assertEquals(STATUS_DRAFT, detail.getStatus());
        assertEquals(1, detail.getAttachments().size());
        assertEquals(10L, detail.getAttachments().get(0).getFileId());

        RuleVersionPageReqVO pageReqVO = new RuleVersionPageReqVO().setVersionNo("V2026.30");
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(10);
        PageResult<RuleVersionRespVO> page = controller.getRuleVersionPage(pageReqVO).getCheckedData();
        assertEquals(1L, page.getTotal());

        Long itemId = controller.saveRuleItem(buildRuleItemSaveReq(null, versionId)).getCheckedData();
        List<RuleItemRespVO> items = controller.getRuleItemList(versionId).getCheckedData();
        assertEquals(1, items.size());
        assertEquals(itemId, items.get(0).getId());

        controller.saveRuleItem(buildRuleItemSaveReq(itemId, versionId).setDefaultPoints(8)).checkError();
        List<RuleItemRespVO> updatedItems = controller.getRuleItemList(versionId).getCheckedData();
        assertEquals(8, updatedItems.get(0).getDefaultPoints());

        Long copiedId = controller.copyRuleVersion(versionId, buildVersionSaveReq(null, "V2026.31")).getCheckedData();
        assertEquals(1, controller.getRuleItemList(copiedId).getCheckedData().size());
    }

    @Test
    void publishWithdrawAndDisableShouldUseLoginUserForAudit() {
        MockHttpServletRequest request = loginRequest(100L, "管理员");
        Long publishId = controller.createRuleVersion(buildVersionSaveReq(null, "V2026.32")).getCheckedData();

        controller.publishRuleVersion(new RuleOperationReqVO()
                .setId(publishId)
                .setReason("发布规则"), request).checkError();

        ClubPointRuleVersionDO published = ruleVersionMapper.selectById(publishId);
        assertEquals(STATUS_PUBLISHED, published.getStatus());
        ClubAuditLogDO publishAudit = findAudit(RULE_PUBLISH);
        assertEquals(100L, publishAudit.getOperatorUserId());
        assertEquals("管理员", publishAudit.getOperatorNameSnapshot());
        assertEquals("发布规则", publishAudit.getReason());

        Long withdrawId = controller.createRuleVersion(buildVersionSaveReq(null, "V2026.33")).getCheckedData();
        controller.withdrawRuleVersion(new RuleOperationReqVO()
                .setId(withdrawId)
                .setReason("暂不发布"), request).checkError();
        ClubPointRuleVersionDO withdrawn = ruleVersionMapper.selectById(withdrawId);
        assertEquals(STATUS_WITHDRAWN, withdrawn.getStatus());
        assertEquals(2, findPublishRecord(withdrawId, 2).getAction());
        assertEquals(100L, findAudit(RULE_WITHDRAW).getOperatorUserId());

        controller.disableRuleVersion(new RuleOperationReqVO()
                .setId(publishId)
                .setReason("停用规则"), request).checkError();
        ClubPointRuleVersionDO disabled = ruleVersionMapper.selectById(publishId);
        assertEquals(STATUS_DISABLED, disabled.getStatus());
        assertEquals(3, findPublishRecord(publishId, 3).getAction());
        assertEquals("停用规则", findAudit(RULE_DISABLE).getReason());
    }

    private static void assertMapping(String methodName, Class<?>[] parameterTypes,
                                      Class<? extends java.lang.annotation.Annotation> mappingType,
                                      String expectedPath) throws NoSuchMethodException {
        Method method = ClubPointRuleAdminController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize);
        assertEquals(RULE_PERMISSION, preAuthorize.value());

        if (mappingType == GetMapping.class) {
            assertEquals(expectedPath, method.getAnnotation(GetMapping.class).value()[0]);
        } else if (mappingType == PostMapping.class) {
            assertEquals(expectedPath, method.getAnnotation(PostMapping.class).value()[0]);
        } else if (mappingType == PutMapping.class) {
            assertEquals(expectedPath, method.getAnnotation(PutMapping.class).value()[0]);
        }
    }

    private static RuleVersionSaveReqVO buildVersionSaveReq(Long id, String versionNo) {
        return new RuleVersionSaveReqVO()
                .setId(id)
                .setVersionNo(versionNo)
                .setName("规则 " + versionNo)
                .setPublicityTime(LocalDateTime.of(2026, 1, 20, 0, 0))
                .setEffectiveTime(LocalDateTime.now().minusDays(1))
                .setSummary("规则摘要")
                .setContent("规则正文")
                .setAttachments(Collections.singletonList(new AttachmentInputVO()
                        .setType(1)
                        .setFileId(10L)
                        .setName("制度.pdf")));
    }

    private static RuleItemSaveReqVO buildRuleItemSaveReq(Long id, Long ruleVersionId) {
        return new RuleItemSaveReqVO()
                .setId(id)
                .setRuleVersionId(ruleVersionId)
                .setItemCode("ACTIVITY_SMALL_BASE")
                .setItemName("小型活动基础分")
                .setItemType(ClubPointRuleItemTypeEnum.POINTS.getType())
                .setCategory(10)
                .setMinPoints(5)
                .setMaxPoints(10)
                .setDefaultPoints(5)
                .setStatus(1)
                .setSort(1)
                .setRemark("规则项备注");
    }

    private static MockHttpServletRequest loginRequest(Long userId, String nickname) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        Map<String, String> info = new HashMap<>();
        info.put(INFO_KEY_NICKNAME, nickname);
        SecurityFrameworkUtils.setLoginUser(new LoginUser()
                .setId(userId)
                .setUserType(ADMIN.getValue())
                .setInfo(info), request);
        return request;
    }

    private ClubAuditLogDO findAudit(String actionType) {
        return auditLogMapper.selectList().stream()
                .filter(auditLog -> actionType.equals(auditLog.getActionType()))
                .findFirst()
                .orElseThrow(AssertionError::new);
    }

    private ClubPointRulePublishRecordDO findPublishRecord(Long ruleVersionId, Integer action) {
        return publishRecordMapper.selectListByRuleVersionId(ruleVersionId).stream()
                .filter(record -> action.equals(record.getAction()))
                .findFirst()
                .orElseThrow(AssertionError::new);
    }

}
