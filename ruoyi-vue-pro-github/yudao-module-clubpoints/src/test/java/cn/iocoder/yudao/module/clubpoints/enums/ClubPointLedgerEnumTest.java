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

import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_FROZEN_POINTS_NOT_ENOUGH;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_REVERSE_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_TRANSACTION_DUPLICATED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_TRANSACTION_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_YEAR_ALREADY_CLEARED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClubPointLedgerEnumTest {

    @Test
    void transactionDirectionEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointTransactionDirectionEnum.values())
                .collect(Collectors.toMap(ClubPointTransactionDirectionEnum::getDirection,
                        ClubPointTransactionDirectionEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.TRANSACTION_DIRECTION), enumValues);
    }

    @Test
    void transactionStatusEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointTransactionStatusEnum.values())
                .collect(Collectors.toMap(ClubPointTransactionStatusEnum::getStatus,
                        ClubPointTransactionStatusEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.TRANSACTION_STATUS), enumValues);
    }

    @Test
    void transactionSourceTypeEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointTransactionSourceTypeEnum.values())
                .collect(Collectors.toMap(ClubPointTransactionSourceTypeEnum::getType,
                        ClubPointTransactionSourceTypeEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.TRANSACTION_SOURCE_TYPE), enumValues);
    }

    @Test
    void freezeStatusEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointFreezeStatusEnum.values())
                .collect(Collectors.toMap(ClubPointFreezeStatusEnum::getStatus,
                        ClubPointFreezeStatusEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.FREEZE_STATUS), enumValues);
    }

    @Test
    void pointCategoryEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointCategoryEnum.values())
                .collect(Collectors.toMap(ClubPointCategoryEnum::getCategory,
                        ClubPointCategoryEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.POINT_CATEGORY), enumValues);
    }

    @Test
    void annualClearingStatusEnumShouldMatchSeedDict() throws Exception {
        Map<Integer, String> enumValues = Arrays.stream(ClubPointAnnualClearingStatusEnum.values())
                .collect(Collectors.toMap(ClubPointAnnualClearingStatusEnum::getStatus,
                        ClubPointAnnualClearingStatusEnum::getName,
                        (left, right) -> left,
                        LinkedHashMap::new));

        assertEquals(parseSeedDict(DictTypeConstants.ANNUAL_CLEARING_STATUS), enumValues);
    }

    @Test
    void errorCodesShouldExposeM4RequiredFailures() {
        assertErrorCode(CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH, 1_300_000_011, "可用积分不足");
        assertErrorCode(CLUB_LEDGER_FROZEN_POINTS_NOT_ENOUGH, 1_300_000_012, "冻结积分不足");
        assertErrorCode(CLUB_LEDGER_TRANSACTION_DUPLICATED, 1_300_000_013, "积分流水重复");
        assertErrorCode(CLUB_LEDGER_TRANSACTION_NOT_EXISTS, 1_300_000_014, "积分流水不存在");
        assertErrorCode(CLUB_LEDGER_YEAR_ALREADY_CLEARED, 1_300_000_015, "年度积分已清零");
        assertErrorCode(CLUB_LEDGER_REVERSE_INVALID, 1_300_000_016, "积分流水撤销不允许当前操作");
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
