package cn.iocoder.yudao.module.clubpoints.enums;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClubPointActivitySettlementEnumTest {

    @Test
    void activitySettlementStatusEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointActivitySettlementStatusEnum.values())
                .collect(Collectors.toMap(ClubPointActivitySettlementStatusEnum::getStatus,
                        ClubPointActivitySettlementStatusEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.ACTIVITY_SETTLEMENT_STATUS), enumValues);
    }

    @Test
    void settlementRunStatusEnumShouldReuseJobStatusDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointSettlementRunStatusEnum.values())
                .collect(Collectors.toMap(ClubPointSettlementRunStatusEnum::getStatus,
                        ClubPointSettlementRunStatusEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.JOB_STATUS), enumValues);
    }

    @Test
    void settlementTriggerSourceEnumShouldMatchSettlementRunTable() {
        assertEquals(1, ClubPointActivitySettlementTriggerSourceEnum.SCHEDULED.getSource());
        assertEquals("定时", ClubPointActivitySettlementTriggerSourceEnum.SCHEDULED.getName());
        assertEquals(2, ClubPointActivitySettlementTriggerSourceEnum.ADMIN_MANUAL.getSource());
        assertEquals("管理员手动", ClubPointActivitySettlementTriggerSourceEnum.ADMIN_MANUAL.getName());
    }

    @Test
    void settlementItemTypesShouldExposeLedgerSourceCategoryDirectionAndKeys() {
        assertEquals(ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType(),
                ClubPointActivitySettlementItemTypeEnum.BASE.getSourceType());
        assertEquals(ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(),
                ClubPointActivitySettlementItemTypeEnum.BASE.getPointCategory());
        assertEquals(ClubPointTransactionDirectionEnum.INCREASE.getDirection(),
                ClubPointActivitySettlementItemTypeEnum.BASE.getDirection());
        assertEquals("ACTIVITY_SETTLEMENT:1001:2001:BASE",
                ClubPointActivitySettlementItemTypeEnum.BASE.buildIdempotencyKey(1001L, 2001L, 202606));

        assertEquals(ClubPointCategoryEnum.FULL_PARTICIPATION_EXTRA.getCategory(),
                ClubPointActivitySettlementItemTypeEnum.FULL_EXTRA.getPointCategory());
        assertEquals(ClubPointRuleItemCodeEnum.ACTIVITY_FULL_EXTRA.getCode(),
                ClubPointActivitySettlementItemTypeEnum.FULL_EXTRA.getRuleItemCode());
        assertEquals("ACTIVITY_SETTLEMENT:1001:2001:FULL_EXTRA",
                ClubPointActivitySettlementItemTypeEnum.FULL_EXTRA.buildIdempotencyKey(1001L, 2001L, 202606));

        assertEquals(ClubPointCategoryEnum.DEDUCTION.getCategory(),
                ClubPointActivitySettlementItemTypeEnum.ABSENCE_SINGLE.getPointCategory());
        assertEquals(ClubPointTransactionDirectionEnum.DECREASE.getDirection(),
                ClubPointActivitySettlementItemTypeEnum.ABSENCE_SINGLE.getDirection());
        assertEquals(ClubPointRuleItemCodeEnum.ABSENCE_SINGLE_DEDUCT.getCode(),
                ClubPointActivitySettlementItemTypeEnum.ABSENCE_SINGLE.getRuleItemCode());
        assertEquals("ACTIVITY_SETTLEMENT:1001:2001:ABSENCE_SINGLE",
                ClubPointActivitySettlementItemTypeEnum.ABSENCE_SINGLE.buildIdempotencyKey(1001L, 2001L, 202606));

        assertEquals(ClubPointCategoryEnum.DEDUCTION.getCategory(),
                ClubPointActivitySettlementItemTypeEnum.ABSENCE_MONTHLY.getPointCategory());
        assertEquals(ClubPointRuleItemCodeEnum.ABSENCE_MONTHLY_DEDUCT.getCode(),
                ClubPointActivitySettlementItemTypeEnum.ABSENCE_MONTHLY.getRuleItemCode());
        assertEquals("ABSENCE_MONTHLY:202606:2001",
                ClubPointActivitySettlementItemTypeEnum.ABSENCE_MONTHLY.buildIdempotencyKey(1001L, 2001L, 202606));
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
