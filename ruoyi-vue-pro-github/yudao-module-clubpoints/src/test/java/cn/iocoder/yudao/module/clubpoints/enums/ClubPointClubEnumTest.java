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

import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ALREADY_JOINED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_DISABLED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEADER_ALREADY_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEADER_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_MEMBER;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClubPointClubEnumTest {

    @Test
    void clubStatusEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointClubStatusEnum.values())
                .collect(Collectors.toMap(ClubPointClubStatusEnum::getStatus,
                        ClubPointClubStatusEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.CLUB_STATUS), enumValues);
    }

    @Test
    void memberStatusEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointMemberStatusEnum.values())
                .collect(Collectors.toMap(ClubPointMemberStatusEnum::getStatus,
                        ClubPointMemberStatusEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.MEMBER_STATUS), enumValues);
    }

    @Test
    void leaderStatusEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointLeaderStatusEnum.values())
                .collect(Collectors.toMap(ClubPointLeaderStatusEnum::getStatus,
                        ClubPointLeaderStatusEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.LEADER_STATUS), enumValues);
    }

    @Test
    void errorCodesShouldExposeM5RequiredFailures() {
        assertErrorCode(CLUB_SCOPE_DENIED, 1_300_000_001, "无权访问该俱乐部数据");
        assertErrorCode(CLUB_NOT_FOUND, 1_300_000_021, "俱乐部不存在");
        assertErrorCode(CLUB_DISABLED, 1_300_000_022, "俱乐部已停用");
        assertErrorCode(CLUB_ALREADY_JOINED, 1_300_000_023, "成员已存在");
        assertErrorCode(CLUB_NOT_MEMBER, 1_300_000_024, "成员不存在");
        assertErrorCode(CLUB_LEADER_ALREADY_EXISTS, 1_300_000_025, "负责人已存在");
        assertErrorCode(CLUB_LEADER_NOT_EXISTS, 1_300_000_026, "负责人不存在");
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
