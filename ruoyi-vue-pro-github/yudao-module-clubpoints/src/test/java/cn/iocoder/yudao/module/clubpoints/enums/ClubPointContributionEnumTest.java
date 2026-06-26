package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_MATERIAL_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_REVIEW_DENIED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CONTRIBUTION_SUBMIT_DUPLICATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointContributionEnumTest {

    @Test
    void materialStatusEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointContributionMaterialStatusEnum.values())
                .collect(Collectors.toMap(ClubPointContributionMaterialStatusEnum::getStatus,
                        ClubPointContributionMaterialStatusEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.MATERIAL_STATUS), enumValues);
    }

    @Test
    void materialStatusEnumShouldEnforceAllowedTransitions() {
        assertTrue(ClubPointContributionMaterialStatusEnum.DRAFT
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW));
        assertTrue(ClubPointContributionMaterialStatusEnum.DRAFT
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.DELETED_SNAPSHOT));
        assertFalse(ClubPointContributionMaterialStatusEnum.DRAFT
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.APPROVED));

        assertTrue(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.WITHDRAWN));
        assertTrue(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.REJECTED));
        assertTrue(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.APPROVED));
        assertFalse(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.DRAFT));

        assertTrue(ClubPointContributionMaterialStatusEnum.WITHDRAWN
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW));
        assertTrue(ClubPointContributionMaterialStatusEnum.REJECTED
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW));
        assertTrue(ClubPointContributionMaterialStatusEnum.APPROVED
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.DELETED_SNAPSHOT));
        assertFalse(ClubPointContributionMaterialStatusEnum.DELETED_SNAPSHOT
                .canTransitionTo(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW));
    }

    @Test
    void materialStatusGuardsShouldMatchM8Acceptance() {
        assertTrue(ClubPointContributionMaterialStatusEnum.DRAFT.canEditContent());
        assertTrue(ClubPointContributionMaterialStatusEnum.WITHDRAWN.canEditContent());
        assertTrue(ClubPointContributionMaterialStatusEnum.REJECTED.canEditContent());
        assertFalse(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.canEditContent());
        assertFalse(ClubPointContributionMaterialStatusEnum.APPROVED.canEditContent());
        assertFalse(ClubPointContributionMaterialStatusEnum.DELETED_SNAPSHOT.canEditContent());

        assertTrue(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.canReview());
        assertFalse(ClubPointContributionMaterialStatusEnum.DRAFT.canReview());
        assertFalse(ClubPointContributionMaterialStatusEnum.APPROVED.canReview());

        assertTrue(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.canWithdraw());
        assertFalse(ClubPointContributionMaterialStatusEnum.REJECTED.canWithdraw());
        assertFalse(ClubPointContributionMaterialStatusEnum.APPROVED.canWithdraw());
    }

    @Test
    void errorCodesShouldExposeM8RequiredFailures() {
        assertErrorCode(CLUB_CONTRIBUTION_MATERIAL_NOT_FOUND, 1_300_000_044, "非签到材料不存在");
        assertErrorCode(CLUB_CONTRIBUTION_STATUS_INVALID, 1_300_000_045, "非签到材料状态不允许当前操作");
        assertErrorCode(CLUB_CONTRIBUTION_RULE_VALUE_OUT_OF_RANGE, 1_300_000_046, "非签到材料分值超出规则范围");
        assertErrorCode(CLUB_CONTRIBUTION_ATTACHMENT_REQUIRED, 1_300_000_047, "非签到材料附件缺失");
        assertErrorCode(CLUB_CONTRIBUTION_SUBMIT_DUPLICATED, 1_300_000_048, "非签到材料重复提交");
        assertErrorCode(CLUB_CONTRIBUTION_REVIEW_DENIED, 1_300_000_049, "无权审核非签到材料");
    }

    private static void assertErrorCode(ErrorCode errorCode, int code, String message) {
        assertEquals(code, errorCode.getCode());
        assertEquals(message, errorCode.getMsg());
    }

    private static Map<Integer, String> parseSeedDict(String dictType) throws Exception {
        String seed = new String(Files.readAllBytes(findSeedPath()), StandardCharsets.UTF_8);
        int start = seed.indexOf("INSERT INTO `system_dict_data`");
        int end = seed.indexOf("ON DUPLICATE KEY UPDATE", start);
        String dictDataBlock = seed.substring(start, end);

        Pattern pattern = Pattern.compile("\\(\\d+, \\d+, '([^']+)', '(\\d+)', '" + Pattern.quote(dictType) + "'");
        Matcher matcher = pattern.matcher(dictDataBlock);
        Map<Integer, String> rows = new LinkedHashMap<>();
        while (matcher.find()) {
            rows.put(Integer.valueOf(matcher.group(2)), matcher.group(1));
        }
        return rows;
    }

    private static Path findSeedPath() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve("sql/mysql/club-points-seed.sql");
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot find sql/mysql/club-points-seed.sql from working directory");
    }

}
