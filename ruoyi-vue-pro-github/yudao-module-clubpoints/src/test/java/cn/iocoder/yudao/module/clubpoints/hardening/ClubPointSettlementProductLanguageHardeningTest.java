package cn.iocoder.yudao.module.clubpoints.hardening;

import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.ClubPointSettlementAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementPendingActivityPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementRunPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementRunReqVO;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivitySettlementStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointSettlementProductLanguageHardeningTest {

    private static final List<String> REQUIRED_FRONTEND_TERMS = Arrays.asList(
            "活动积分发放",
            "待自动发放",
            "发放记录",
            "异常补发/重跑",
            "异常补发活动积分",
            "正常活动积分应由系统自动发放"
    );
    private static final List<String> FORBIDDEN_FRONTEND_TERMS = Arrays.asList(
            "活动结算",
            "待发放活动",
            "手动生成活动积分",
            "手动生成积分",
            "手动生成原因",
            "确认生成"
    );
    private static final List<String> FORBIDDEN_BACKEND_VISIBLE_TERMS = Arrays.asList(
            "活动结算",
            "待结算",
            "结算中",
            "已结算",
            "结算失败",
            "手动生成活动积分",
            "手动生成积分"
    );

    @Test
    void backendSettlementOpenApiCopyShouldUseProductLanguage() throws Exception {
        assertEquals("管理后台 - 活动积分发放",
                ClubPointSettlementAdminController.class.getAnnotation(Tag.class).name());
        assertOperationSummary("getPendingActivityPage",
                new Class<?>[]{AdminSettlementPendingActivityPageReqVO.class}, "待自动发放活动分页");
        assertOperationSummary("runSettlement",
                new Class<?>[]{AdminSettlementRunReqVO.class}, "异常补发或重跑活动积分");
        assertOperationSummary("getRunPage",
                new Class<?>[]{AdminSettlementRunPageReqVO.class}, "活动积分发放记录分页");
        assertOperationSummary("getDetail",
                new Class<?>[]{Long.class}, "活动积分发放明细");
    }

    @Test
    void backendUserFacingSettlementCopyShouldUseProductLanguage() throws Exception {
        assertEquals("活动积分发放", ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getName());
        assertEquals("已发放", ClubPointActivityStatusEnum.SETTLED.getName());
        assertEquals("活动发放缓冲分钟",
                ClubPointRuleItemCodeEnum.ACTIVITY_SETTLEMENT_GRACE_MINUTES.getName());
        assertEquals("待发放", ClubPointActivitySettlementStatusEnum.PENDING.getName());
        assertEquals("发放中", ClubPointActivitySettlementStatusEnum.PROCESSING.getName());
        assertEquals("已发放", ClubPointActivitySettlementStatusEnum.SETTLED.getName());
        assertEquals("发放失败", ClubPointActivitySettlementStatusEnum.FAILED.getName());

        List<String> visibleTexts = new ArrayList<>();
        visibleTexts.add(ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getName());
        visibleTexts.add(ClubPointActivityStatusEnum.SETTLED.getName());
        visibleTexts.add(ClubPointRuleItemCodeEnum.ACTIVITY_SETTLEMENT_GRACE_MINUTES.getName());
        for (ClubPointActivitySettlementStatusEnum status : ClubPointActivitySettlementStatusEnum.values()) {
            visibleTexts.add(status.getName());
        }
        visibleTexts.addAll(readClubpointsErrorMessages());

        List<String> violations = new ArrayList<>();
        for (String visibleText : visibleTexts) {
            for (String forbiddenTerm : FORBIDDEN_BACKEND_VISIBLE_TERMS) {
                if (visibleText.contains(forbiddenTerm)) {
                    violations.add(visibleText + " contains " + forbiddenTerm);
                }
            }
        }

        assertTrue(violations.isEmpty(), "Backend visible settlement copy still uses old wording: " + violations);
    }

    @Test
    void frontendSettlementPageShouldUseProductLanguage() throws Exception {
        String content = Files.readString(settlementViewPath(), StandardCharsets.UTF_8);
        List<String> violations = new ArrayList<>();
        for (String requiredTerm : REQUIRED_FRONTEND_TERMS) {
            if (!content.contains(requiredTerm)) {
                violations.add("Missing required frontend term: " + requiredTerm);
            }
        }
        for (String forbiddenTerm : FORBIDDEN_FRONTEND_TERMS) {
            if (content.contains(forbiddenTerm)) {
                violations.add("Forbidden old frontend term: " + forbiddenTerm);
            }
        }

        assertTrue(violations.isEmpty(), "Settlement page product language is not aligned: " + violations);
    }

    @Test
    void frontendManualSettlementShouldForcePublishedActivityIntoSettlementFlow() throws Exception {
        String content = Files.readString(settlementViewPath(), StandardCharsets.UTF_8);

        assertTrue(content.contains("force: true"),
                "Manual activity point issuance must pass force=true, otherwise published activities cannot be "
                        + "closed and issued through the real business UI");
    }

    private static void assertOperationSummary(String methodName, Class<?>[] parameterTypes,
                                               String expectedSummary) throws NoSuchMethodException {
        Method method = ClubPointSettlementAdminController.class.getMethod(methodName, parameterTypes);
        Operation operation = method.getAnnotation(Operation.class);
        assertEquals(expectedSummary, operation.summary());
    }

    private static List<String> readClubpointsErrorMessages() throws IllegalAccessException {
        List<String> messages = new ArrayList<>();
        for (Field field : ErrorCodeConstants.class.getFields()) {
            if (ErrorCode.class.isAssignableFrom(field.getType())) {
                messages.add(((ErrorCode) field.get(null)).getMsg());
            }
        }
        return messages;
    }

    private static Path settlementViewPath() throws IOException {
        Path root = findRuoyiRoot();
        Path path = root.resolve("yudao-ui/yudao-ui-admin-vue3/src/views/clubpoints/admin/settlement/index.vue");
        if (!Files.exists(path)) {
            throw new IOException("Cannot find settlement view: " + path);
        }
        return path;
    }

    private static Path findRuoyiRoot() {
        Path current = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 6; i++) {
            if (Files.exists(current.resolve("yudao-server/src/main/resources/application.yaml"))) {
                return current;
            }
            Path nested = current.resolve("ruoyi-vue-pro-github/yudao-server/src/main/resources/application.yaml");
            if (Files.exists(nested)) {
                return current.resolve("ruoyi-vue-pro-github");
            }
            current = current.getParent();
            if (current == null) {
                break;
            }
        }
        throw new IllegalStateException("Cannot find yudao-server/src/main/resources/application.yaml");
    }

}
