package cn.iocoder.yudao.module.clubpoints.controller.admin.rule;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleItemRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleItemSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleVersionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleVersionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleVersionSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleService;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleVersionSaveReqBO;
import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 俱乐部积分规则")
@RestController
@RequestMapping("/clubpoints/rule")
@Validated
public class ClubPointRuleAdminController {

    private static final String OPERATOR_ROLE_SNAPSHOT = "club_points_admin";

    @Resource
    private ClubPointRuleService clubPointRuleService;

    @GetMapping("/page")
    @Operation(summary = "规则版本分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:rule:manage')")
    public CommonResult<PageResult<RuleVersionRespVO>> getRuleVersionPage(@Valid RuleVersionPageReqVO pageReqVO) {
        PageResult<ClubPointRuleVersionDO> pageResult = clubPointRuleService.getRuleVersionPage(pageReqVO,
                pageReqVO.getVersionNo(), pageReqVO.getName(), pageReqVO.getStatus());
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointRuleAdminController::toRuleVersionRespVO)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "规则版本详情")
    @Parameter(name = "id", description = "规则版本 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:rule:manage')")
    public CommonResult<RuleVersionRespVO> getRuleVersion(@RequestParam("id") Long id) {
        return success(toRuleVersionRespVO(clubPointRuleService.getRuleVersion(id)));
    }

    @PostMapping("/create")
    @Operation(summary = "创建规则版本草稿")
    @PreAuthorize("@ss.hasPermission('clubpoints:rule:manage')")
    public CommonResult<Long> createRuleVersion(@Valid @RequestBody RuleVersionSaveReqVO createReqVO) {
        return success(clubPointRuleService.createDraftVersion(toVersionSaveBO(createReqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "修改规则版本草稿")
    @PreAuthorize("@ss.hasPermission('clubpoints:rule:manage')")
    public CommonResult<Boolean> updateRuleVersion(@Valid @RequestBody RuleVersionSaveReqVO updateReqVO) {
        clubPointRuleService.updateDraftVersion(toVersionSaveBO(updateReqVO));
        return success(true);
    }

    @PostMapping("/copy")
    @Operation(summary = "复制规则版本为草稿")
    @Parameter(name = "sourceVersionId", description = "来源规则版本 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:rule:manage')")
    public CommonResult<Long> copyRuleVersion(@RequestParam("sourceVersionId") Long sourceVersionId,
                                              @Valid @RequestBody RuleVersionSaveReqVO createReqVO) {
        return success(clubPointRuleService.copyVersion(sourceVersionId, toVersionSaveBO(createReqVO)));
    }

    @PostMapping("/publish")
    @Operation(summary = "发布规则版本")
    @PreAuthorize("@ss.hasPermission('clubpoints:rule:manage')")
    public CommonResult<Boolean> publishRuleVersion(@Valid @RequestBody RuleOperationReqVO reqVO,
                                                    HttpServletRequest request) {
        clubPointRuleService.publishVersion(reqVO.getId(), toOperationBO(reqVO, request));
        return success(true);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "撤回规则版本")
    @PreAuthorize("@ss.hasPermission('clubpoints:rule:manage')")
    public CommonResult<Boolean> withdrawRuleVersion(@Valid @RequestBody RuleOperationReqVO reqVO,
                                                     HttpServletRequest request) {
        clubPointRuleService.withdrawVersion(reqVO.getId(), toOperationBO(reqVO, request));
        return success(true);
    }

    @PostMapping("/disable")
    @Operation(summary = "停用规则版本")
    @PreAuthorize("@ss.hasPermission('clubpoints:rule:manage')")
    public CommonResult<Boolean> disableRuleVersion(@Valid @RequestBody RuleOperationReqVO reqVO,
                                                    HttpServletRequest request) {
        clubPointRuleService.disableVersion(reqVO.getId(), toOperationBO(reqVO, request));
        return success(true);
    }

    @GetMapping("/item-list")
    @Operation(summary = "规则项列表")
    @Parameter(name = "ruleVersionId", description = "规则版本 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:rule:manage')")
    public CommonResult<List<RuleItemRespVO>> getRuleItemList(@RequestParam("ruleVersionId") Long ruleVersionId) {
        List<ClubPointRuleItemDO> list = clubPointRuleService.getRuleItemList(ruleVersionId);
        return success(BeanUtils.toBean(list, RuleItemRespVO.class));
    }

    @PostMapping("/item/save")
    @Operation(summary = "保存规则项")
    @PreAuthorize("@ss.hasPermission('clubpoints:rule:manage')")
    public CommonResult<Long> saveRuleItem(@Valid @RequestBody RuleItemSaveReqVO saveReqVO) {
        if (saveReqVO.getId() == null) {
            return success(clubPointRuleService.createRuleItem(toRuleItemSaveBO(saveReqVO)));
        }
        clubPointRuleService.updateDraftRuleItem(toRuleItemSaveBO(saveReqVO));
        return success(saveReqVO.getId());
    }

    private static ClubPointRuleVersionSaveReqBO toVersionSaveBO(RuleVersionSaveReqVO reqVO) {
        return new ClubPointRuleVersionSaveReqBO()
                .setId(reqVO.getId())
                .setVersionNo(reqVO.getVersionNo())
                .setName(reqVO.getName())
                .setPublicityTime(reqVO.getPublicityTime())
                .setEffectiveTime(reqVO.getEffectiveTime())
                .setSummary(reqVO.getSummary())
                .setContent(reqVO.getContent())
                .setAttachmentSnapshotJson(reqVO.getAttachments() == null ? null : JsonUtils.toJsonString(reqVO.getAttachments()))
                .setRemark(reqVO.getRemark());
    }

    private static ClubPointRuleItemSaveReqBO toRuleItemSaveBO(RuleItemSaveReqVO reqVO) {
        return new ClubPointRuleItemSaveReqBO()
                .setId(reqVO.getId())
                .setRuleVersionId(reqVO.getRuleVersionId())
                .setItemCode(reqVO.getItemCode())
                .setItemName(reqVO.getItemName())
                .setItemType(reqVO.getItemType())
                .setCategory(reqVO.getCategory())
                .setMinPoints(reqVO.getMinPoints())
                .setMaxPoints(reqVO.getMaxPoints())
                .setDefaultPoints(reqVO.getDefaultPoints())
                .setIntValue(reqVO.getIntValue())
                .setDecimalValue(reqVO.getDecimalValue())
                .setTextValue(reqVO.getTextValue())
                .setJsonValue(reqVO.getJsonValue())
                .setStatus(reqVO.getStatus())
                .setSort(reqVO.getSort())
                .setRemark(reqVO.getRemark());
    }

    private static ClubPointRuleOperationReqBO toOperationBO(RuleOperationReqVO reqVO, HttpServletRequest request) {
        Long operatorUserId = getLoginUserId();
        String operatorName = getLoginUserNickname();
        return new ClubPointRuleOperationReqBO()
                .setOperatorUserId(operatorUserId)
                .setOperatorNameSnapshot(StringUtils.hasText(operatorName) ? operatorName : String.valueOf(operatorUserId))
                .setOperatorRoleSnapshot(OPERATOR_ROLE_SNAPSHOT)
                .setClientIp(request != null ? request.getRemoteAddr() : null)
                .setUserAgent(request != null ? request.getHeader("User-Agent") : null)
                .setReason(reqVO.getReason());
    }

    private static RuleVersionRespVO toRuleVersionRespVO(ClubPointRuleVersionDO version) {
        return BeanUtils.toBean(version, RuleVersionRespVO.class,
                respVO -> respVO.setAttachments(parseAttachments(version.getAttachmentSnapshotJson())));
    }

    private static List<AttachmentInputVO> parseAttachments(String attachmentSnapshotJson) {
        if (!StringUtils.hasText(attachmentSnapshotJson)) {
            return Collections.emptyList();
        }
        List<AttachmentInputVO> attachments = JsonUtils.parseObject(attachmentSnapshotJson,
                new TypeReference<List<AttachmentInputVO>>() {});
        return attachments != null ? attachments : Collections.emptyList();
    }

}
