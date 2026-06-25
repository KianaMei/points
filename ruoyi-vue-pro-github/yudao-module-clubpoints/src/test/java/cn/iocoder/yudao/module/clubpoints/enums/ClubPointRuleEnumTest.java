package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_ITEM_CODE_DUPLICATED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_VALUE_OUT_OF_RANGE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_VERSION_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_RULE_VERSION_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClubPointRuleEnumTest {

    @Test
    void versionStatusEnumShouldExposeDatabaseValues() {
        Map<Integer, String> values = Arrays.stream(ClubPointRuleVersionStatusEnum.values())
                .collect(Collectors.toMap(ClubPointRuleVersionStatusEnum::getStatus, ClubPointRuleVersionStatusEnum::getName));

        assertEquals(4, values.size());
        assertEquals("草稿", values.get(1));
        assertEquals("已发布", values.get(2));
        assertEquals("已撤回", values.get(3));
        assertEquals("已停用", values.get(4));
    }

    @Test
    void itemTypeEnumShouldExposeDatabaseValues() {
        Map<Integer, String> values = Arrays.stream(ClubPointRuleItemTypeEnum.values())
                .collect(Collectors.toMap(ClubPointRuleItemTypeEnum::getType, ClubPointRuleItemTypeEnum::getName));

        assertEquals(6, values.size());
        assertEquals("分值", values.get(1));
        assertEquals("阈值", values.get(2));
        assertEquals("开关", values.get(3));
        assertEquals("金额", values.get(4));
        assertEquals("文本", values.get(5));
        assertEquals("JSON", values.get(6));
    }

    @Test
    void itemCodeEnumShouldMatchSeedRuleItemCodes() throws Exception {
        Set<String> seedCodes = parseSeedRuleItems().keySet();
        Set<String> enumCodes = Arrays.stream(ClubPointRuleItemCodeEnum.values())
                .map(ClubPointRuleItemCodeEnum::getCode)
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(seedCodes, enumCodes);
    }

    @Test
    void fixedPointSeedRowsShouldUseMinMaxRange() throws Exception {
        Map<String, RuleItemSeedRow> rows = parseSeedRuleItems();

        assertFixedPointRange(rows.get(ClubPointRuleItemCodeEnum.ACTIVITY_SMALL_BASE.getCode()), 5);
        assertFixedPointRange(rows.get(ClubPointRuleItemCodeEnum.ACTIVITY_MEDIUM_BASE.getCode()), 8);
        assertFixedPointRange(rows.get(ClubPointRuleItemCodeEnum.ACTIVITY_LARGE_BASE.getCode()), 10);
        assertFixedPointRange(rows.get(ClubPointRuleItemCodeEnum.ACTIVITY_FULL_EXTRA.getCode()), 2);
        assertFixedPointRange(rows.get(ClubPointRuleItemCodeEnum.ABSENCE_SINGLE_DEDUCT.getCode()), 2);
        assertFixedPointRange(rows.get(ClubPointRuleItemCodeEnum.ABSENCE_MONTHLY_DEDUCT.getCode()), 5);
        assertFixedPointRange(rows.get(ClubPointRuleItemCodeEnum.MONTHLY_DUTY.getCode()), 5);
    }

    @Test
    void errorCodesShouldExposeM3RequiredFailures() {
        assertErrorCode(CLUB_RULE_VERSION_NOT_EXISTS, 1_300_000_006, "规则版本不存在");
        assertErrorCode(CLUB_RULE_VERSION_STATUS_INVALID, 1_300_000_007, "规则版本状态不允许当前操作");
        assertErrorCode(CLUB_RULE_ITEM_NOT_EXISTS, 1_300_000_008, "规则项不存在");
        assertErrorCode(CLUB_RULE_ITEM_CODE_DUPLICATED, 1_300_000_009, "规则项编码重复");
        assertErrorCode(CLUB_RULE_VALUE_OUT_OF_RANGE, 1_300_000_010, "规则值超出允许范围");
    }

    private static void assertFixedPointRange(RuleItemSeedRow row, Integer expectedPoints) {
        assertEquals(1, row.itemType);
        assertEquals(expectedPoints, row.minPoints);
        assertEquals(expectedPoints, row.maxPoints);
        assertEquals(expectedPoints, row.defaultPoints);
    }

    private static void assertErrorCode(ErrorCode errorCode, int code, String message) {
        assertEquals(code, errorCode.getCode());
        assertEquals(message, errorCode.getMsg());
    }

    private static Map<String, RuleItemSeedRow> parseSeedRuleItems() throws Exception {
        String seed = new String(Files.readAllBytes(findSeedPath()), StandardCharsets.UTF_8);
        int start = seed.indexOf("INSERT INTO `club_points_rule_item`");
        int end = seed.indexOf("ON DUPLICATE KEY UPDATE", start);
        String ruleItemBlock = seed.substring(start, end);

        Map<String, RuleItemSeedRow> rows = new HashMap<>();
        Pattern pattern = Pattern.compile("\\(\\d+, \\d+, '([^']+)', '[^']+', (\\d+), \\d+, (NULL|\\d+), (NULL|\\d+), (NULL|\\d+),");
        Matcher matcher = pattern.matcher(ruleItemBlock);
        while (matcher.find()) {
            rows.put(matcher.group(1), new RuleItemSeedRow(
                    Integer.valueOf(matcher.group(2)),
                    parseNullableInt(matcher.group(3)),
                    parseNullableInt(matcher.group(4)),
                    parseNullableInt(matcher.group(5))));
        }
        return rows;
    }

    private static Integer parseNullableInt(String value) {
        return "NULL".equals(value) ? null : Integer.valueOf(value);
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

    private static class RuleItemSeedRow {

        private final Integer itemType;
        private final Integer minPoints;
        private final Integer maxPoints;
        private final Integer defaultPoints;

        private RuleItemSeedRow(Integer itemType, Integer minPoints, Integer maxPoints, Integer defaultPoints) {
            this.itemType = itemType;
            this.minPoints = minPoints;
            this.maxPoints = maxPoints;
            this.defaultPoints = defaultPoints;
        }

    }
}
