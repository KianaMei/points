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

import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointActivityEnumTest {

    @Test
    void activityStatusEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointActivityStatusEnum.values())
                .collect(Collectors.toMap(ClubPointActivityStatusEnum::getStatus,
                        ClubPointActivityStatusEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.ACTIVITY_STATUS), enumValues);
    }

    @Test
    void activityStatusEnumShouldEnforceAllowedTransitions() {
        assertTrue(ClubPointActivityStatusEnum.DRAFT.canTransitionTo(ClubPointActivityStatusEnum.PENDING_REVIEW));
        assertTrue(ClubPointActivityStatusEnum.DRAFT.canTransitionTo(ClubPointActivityStatusEnum.PUBLISHED));
        assertTrue(ClubPointActivityStatusEnum.DRAFT.canTransitionTo(ClubPointActivityStatusEnum.DELETED_SNAPSHOT));
        assertFalse(ClubPointActivityStatusEnum.DRAFT.canTransitionTo(ClubPointActivityStatusEnum.SETTLED));

        assertTrue(ClubPointActivityStatusEnum.PENDING_REVIEW.canTransitionTo(ClubPointActivityStatusEnum.PUBLISHED));
        assertTrue(ClubPointActivityStatusEnum.PENDING_REVIEW.canTransitionTo(ClubPointActivityStatusEnum.REJECTED));
        assertFalse(ClubPointActivityStatusEnum.PENDING_REVIEW.canTransitionTo(ClubPointActivityStatusEnum.ENDED));

        assertTrue(ClubPointActivityStatusEnum.REJECTED.canTransitionTo(ClubPointActivityStatusEnum.PENDING_REVIEW));
        assertFalse(ClubPointActivityStatusEnum.REJECTED.canTransitionTo(ClubPointActivityStatusEnum.PUBLISHED));

        assertTrue(ClubPointActivityStatusEnum.PUBLISHED.canTransitionTo(ClubPointActivityStatusEnum.CANCELED));
        assertTrue(ClubPointActivityStatusEnum.PUBLISHED.canTransitionTo(ClubPointActivityStatusEnum.ENDED));
        assertFalse(ClubPointActivityStatusEnum.PUBLISHED.canTransitionTo(ClubPointActivityStatusEnum.SETTLED));

        assertTrue(ClubPointActivityStatusEnum.ENDED.canTransitionTo(ClubPointActivityStatusEnum.SETTLED));
        assertFalse(ClubPointActivityStatusEnum.CANCELED.canTransitionTo(ClubPointActivityStatusEnum.PUBLISHED));
        assertFalse(ClubPointActivityStatusEnum.SETTLED.canTransitionTo(ClubPointActivityStatusEnum.PUBLISHED));
    }

    @Test
    void activityStatusGuardsShouldMatchM6Acceptance() {
        assertFalse(ClubPointActivityStatusEnum.DRAFT.canRegister());
        assertFalse(ClubPointActivityStatusEnum.PENDING_REVIEW.canRegister());
        assertFalse(ClubPointActivityStatusEnum.REJECTED.canRegister());
        assertTrue(ClubPointActivityStatusEnum.PUBLISHED.canRegister());

        assertTrue(ClubPointActivityStatusEnum.PUBLISHED.canCheckAttendance());
        assertFalse(ClubPointActivityStatusEnum.CANCELED.canCheckAttendance());
        assertFalse(ClubPointActivityStatusEnum.SETTLED.canCheckAttendance());

        assertTrue(ClubPointActivityStatusEnum.PUBLISHED.canUpdateKeyFields());
        assertFalse(ClubPointActivityStatusEnum.CANCELED.canUpdateKeyFields());
        assertFalse(ClubPointActivityStatusEnum.SETTLED.canUpdateKeyFields());
    }

    @Test
    void errorCodesShouldExposeM6RequiredFailures() {
        assertErrorCode(CLUB_ACTIVITY_STATUS_INVALID, 1_300_000_031, "活动状态不允许当前操作");
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
