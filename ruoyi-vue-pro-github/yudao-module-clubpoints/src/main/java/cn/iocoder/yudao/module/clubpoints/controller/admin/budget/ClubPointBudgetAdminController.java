package cn.iocoder.yudao.module.clubpoints.controller.admin.budget;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo.AdminBudgetOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo.AdminBudgetPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo.AdminBudgetRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo.AdminBudgetSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.budget.ClubPointBudgetRecordDO;
import cn.iocoder.yudao.module.clubpoints.service.budget.ClubPointBudgetService;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetQueryReqBO;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetSaveReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 预算记录")
@RestController
@RequestMapping("/clubpoints/budget")
@Validated
public class ClubPointBudgetAdminController {

    @Resource
    private ClubPointBudgetService budgetService;

    @GetMapping("/page")
    @Operation(summary = "预算记录分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:budget:manage')")
    public CommonResult<PageResult<AdminBudgetRespVO>> getBudgetPage(@Valid AdminBudgetPageReqVO pageReqVO) {
        List<ClubPointBudgetRecordDO> budgets = budgetService.listBudgetRecords(new ClubPointBudgetQueryReqBO()
                .setYear(pageReqVO.getYear())
                .setCategory(pageReqVO.getCategory())
                .setSourceType(pageReqVO.getSourceType())
                .setSourceId(pageReqVO.getSourceId())
                .setOperatorGlobalScope(true));
        return success(pageList(budgets.stream()
                .map(ClubPointBudgetAdminController::toResp)
                .collect(Collectors.toList()), pageReqVO));
    }

    @PostMapping("/create")
    @Operation(summary = "创建预算记录")
    @PreAuthorize("@ss.hasPermission('clubpoints:budget:manage')")
    public CommonResult<Long> createBudget(@RequestBody @Valid AdminBudgetSaveReqVO reqVO) {
        return success(budgetService.createBudget(buildSaveReqBO(reqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "修改预算记录")
    @PreAuthorize("@ss.hasPermission('clubpoints:budget:manage')")
    public CommonResult<Boolean> updateBudget(@RequestBody @Valid AdminBudgetSaveReqVO reqVO) {
        budgetService.updateBudget(buildSaveReqBO(reqVO));
        return success(true);
    }

    @PostMapping("/disable")
    @Operation(summary = "停用预算记录")
    @PreAuthorize("@ss.hasPermission('clubpoints:budget:manage')")
    public CommonResult<Boolean> disableBudget(@RequestBody @Valid AdminBudgetOperationReqVO reqVO) {
        budgetService.disableBudget(new ClubPointBudgetOperationReqBO()
                .setId(reqVO.getId())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setOperationTime(LocalDateTime.now())
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason()));
        return success(true);
    }

    private static ClubPointBudgetSaveReqBO buildSaveReqBO(AdminBudgetSaveReqVO reqVO) {
        return new ClubPointBudgetSaveReqBO()
                .setId(reqVO.getId())
                .setCategory(reqVO.getCategory())
                .setBudgetAmountCent(reqVO.getBudgetAmountCent())
                .setActualAmountCent(reqVO.getActualAmountCent())
                .setOccurDate(reqVO.getOccurDate())
                .setHandlerUserId(reqVO.getHandlerUserId())
                .setSourceType(reqVO.getSourceType())
                .setSourceId(reqVO.getSourceId())
                .setDescription(reqVO.getDescription())
                .setRemark(reqVO.getRemark())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setOperationTime(LocalDateTime.now())
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
    }

    private static AdminBudgetRespVO toResp(ClubPointBudgetRecordDO budget) {
        return new AdminBudgetRespVO()
                .setId(budget.getId())
                .setCategory(budget.getCategory())
                .setBudgetAmountCent(budget.getBudgetAmountCent())
                .setActualAmountCent(budget.getActualAmountCent())
                .setOccurDate(budget.getOccurDate())
                .setHandlerUserId(budget.getHandlerUserId())
                .setSourceType(budget.getSourceType())
                .setSourceId(budget.getSourceId())
                .setDescription(budget.getDescription())
                .setRemark(budget.getRemark());
    }

    private static <T> PageResult<T> pageList(List<T> list, PageParam pageParam) {
        if (list == null || list.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        int pageNo = pageParam.getPageNo() == null ? 1 : pageParam.getPageNo();
        int pageSize = pageParam.getPageSize() == null ? 10 : pageParam.getPageSize();
        int fromIndex = Math.min((pageNo - 1) * pageSize, list.size());
        int toIndex = Math.min(fromIndex + pageSize, list.size());
        return new PageResult<>(list.subList(fromIndex, toIndex), (long) list.size());
    }

}
