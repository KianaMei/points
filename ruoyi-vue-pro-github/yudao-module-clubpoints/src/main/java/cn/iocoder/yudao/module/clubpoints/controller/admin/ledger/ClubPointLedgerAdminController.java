package cn.iocoder.yudao.module.clubpoints.controller.admin.ledger;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerAccountPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerAccountRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerAdjustReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerReverseReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryService;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerQueryService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerAdjustReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerTransactionBO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 俱乐部积分账本")
@RestController
@RequestMapping("/clubpoints/ledger")
@Validated
public class ClubPointLedgerAdminController {

    @Resource
    private ClubPointLedgerQueryService clubPointLedgerQueryService;
    @Resource
    private ClubPointLedgerService clubPointLedgerService;
    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/account-page")
    @Operation(summary = "积分账户分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:ledger:query')")
    public CommonResult<PageResult<AdminLedgerAccountRespVO>> getAccountPage(
            @Valid AdminLedgerAccountPageReqVO pageReqVO) {
        PageResult<ClubPointAccountDO> pageResult = clubPointLedgerQueryService.getAdminAccountPage(
                toAccountPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminLedgerAccountRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/transaction-page")
    @Operation(summary = "积分流水分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:ledger:query')")
    public CommonResult<PageResult<AdminLedgerTransactionRespVO>> getTransactionPage(
            @Valid AdminLedgerTransactionPageReqVO pageReqVO) {
        PageResult<ClubPointLedgerTransactionBO> pageResult = clubPointLedgerQueryService.getAdminTransactionPage(
                toLedgerPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminLedgerTransactionRespVO.class),
                pageResult.getTotal()));
    }

    @PostMapping("/adjust")
    @Operation(summary = "调整积分")
    @PreAuthorize("@ss.hasPermission('clubpoints:ledger:adjust')")
    public CommonResult<Long> adjustLedger(@RequestBody @Valid AdminLedgerAdjustReqVO reqVO) {
        return success(clubPointLedgerService.adjustPoints(toAdjustReqBO(reqVO)));
    }

    @PostMapping("/reverse")
    @Operation(summary = "撤销积分流水")
    @PreAuthorize("@ss.hasPermission('clubpoints:ledger:reverse')")
    public CommonResult<Long> reverseLedger(@RequestBody @Valid AdminLedgerReverseReqVO reqVO) {
        LocalDateTime now = LocalDateTime.now();
        return success(clubPointLedgerService.reverseTransaction(new ClubPointLedgerReverseReqBO()
                .setSourceTransactionId(reqVO.getTransactionId())
                .setTransactionNo("REV-" + reqVO.getTransactionId())
                .setReason(reqVO.getReason())
                .setOccurredAt(now)
                .setAttachmentSnapshotJson(emptyAttachmentSnapshot())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())));
    }

    private static ClubPointAccountPageReqBO toAccountPageReqBO(AdminLedgerAccountPageReqVO reqVO) {
        ClubPointAccountPageReqBO reqBO = new ClubPointAccountPageReqBO()
                .setUserId(reqVO.getUserId());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointLedgerPageReqBO toLedgerPageReqBO(AdminLedgerTransactionPageReqVO reqVO) {
        ClubPointLedgerPageReqBO reqBO = new ClubPointLedgerPageReqBO()
                .setUserId(reqVO.getUserId())
                .setClubId(reqVO.getClubId())
                .setDirection(reqVO.getDirection())
                .setPointCategory(reqVO.getPointCategory())
                .setSourceType(reqVO.getSourceType())
                .setStartTime(reqVO.getStartTime())
                .setEndTime(reqVO.getEndTime());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private ClubPointLedgerAdjustReqBO toAdjustReqBO(AdminLedgerAdjustReqVO reqVO) {
        AdminUserRespDTO user = adminUserApi.getUser(reqVO.getUserId());
        DeptRespDTO dept = user != null && user.getDeptId() != null ? deptApi.getDept(user.getDeptId()) : null;
        ClubPointClubInfoBO club = reqVO.getIssuingClubId() != null
                ? clubPointClubQueryService.getAdminClub(reqVO.getIssuingClubId()) : null;
        LocalDateTime now = LocalDateTime.now();
        return new ClubPointLedgerAdjustReqBO()
                .setRequestNo(reqVO.getRequestNo())
                .setTransactionNo("ADJ-" + reqVO.getRequestNo())
                .setUserId(reqVO.getUserId())
                .setUserNameSnapshot(user != null ? user.getNickname() : null)
                .setDeptIdSnapshot(user != null ? user.getDeptId() : null)
                .setDeptNameSnapshot(dept != null ? dept.getName() : null)
                .setAdjustType(reqVO.getAdjustType() != null ? reqVO.getAdjustType() : reqVO.getDirection())
                .setDirection(reqVO.getDirection())
                .setPoints(reqVO.getPoints())
                .setIssuingClubId(reqVO.getIssuingClubId())
                .setIssuingClubCodeSnapshot(club != null ? club.getCode() : null)
                .setIssuingClubNameSnapshot(club != null ? club.getName() : null)
                .setRuleVersionId(reqVO.getRuleVersionId())
                .setRuleItemCode(reqVO.getRuleItemCode())
                .setReason(reqVO.getReason())
                .setMaterialSummary(reqVO.getReason())
                .setAttachmentSnapshotJson(attachmentSnapshot(reqVO))
                .setOccurredAt(now)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent());
    }

    private static String attachmentSnapshot(AdminLedgerAdjustReqVO reqVO) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("attachments", reqVO.getAttachments() != null ? reqVO.getAttachments() : Collections.emptyList());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String emptyAttachmentSnapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("attachments", Collections.emptyList());
        return JsonUtils.toJsonString(snapshot);
    }

}
