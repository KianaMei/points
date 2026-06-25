package cn.iocoder.yudao.module.clubpoints.enums;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DictTypeConstantsTest {

    @Test
    void constantsShouldMatchSeedDictTypes() throws Exception {
        Set<String> seedDictTypes = parseSeedDictTypes();
        Set<String> constantDictTypes = readConstantValues();

        assertEquals(seedDictTypes, constantDictTypes);
    }

    @Test
    void constantsShouldExposeM2RequiredDictTypes() throws Exception {
        Set<String> constantDictTypes = readConstantValues();

        assertTrue(constantDictTypes.contains(DictTypeConstants.ACTIVITY_STATUS));
        assertTrue(constantDictTypes.contains(DictTypeConstants.REGISTRATION_STATUS));
        assertTrue(constantDictTypes.contains(DictTypeConstants.ATTENDANCE_TARGET_TYPE));
        assertTrue(constantDictTypes.contains(DictTypeConstants.ATTENDANCE_SOURCE_TYPE));
        assertTrue(constantDictTypes.contains(DictTypeConstants.RULE_VERSION_STATUS));
        assertTrue(constantDictTypes.contains(DictTypeConstants.TRANSACTION_SOURCE_TYPE));
        assertTrue(constantDictTypes.contains(DictTypeConstants.POINT_CATEGORY));
        assertTrue(constantDictTypes.contains(DictTypeConstants.FREEZE_STATUS));
        assertTrue(constantDictTypes.contains(DictTypeConstants.REDEMPTION_APPLICATION_STATUS));
        assertTrue(constantDictTypes.contains(DictTypeConstants.DISPUTE_STATUS));
        assertTrue(constantDictTypes.contains(DictTypeConstants.ANNUAL_CLEARING_STATUS));
    }

    private static Set<String> readConstantValues() throws IllegalAccessException {
        Set<String> values = new TreeSet<>();
        Field[] fields = DictTypeConstants.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(String.class) && Modifier.isStatic(field.getModifiers())) {
                values.add((String) field.get(null));
            }
        }
        return values;
    }

    private static Set<String> parseSeedDictTypes() throws Exception {
        String seed = new String(Files.readAllBytes(findSeedPath()), StandardCharsets.UTF_8);
        int start = seed.indexOf("INSERT INTO `system_dict_type`");
        int end = seed.indexOf("ON DUPLICATE KEY UPDATE", start);
        String dictTypeBlock = seed.substring(start, end);

        Set<String> values = new TreeSet<>();
        Pattern pattern = Pattern.compile("\\(\\d+, '[^']+', '(club_points_[a-z0-9_]+)'");
        Matcher matcher = pattern.matcher(dictTypeBlock);
        while (matcher.find()) {
            values.add(matcher.group(1));
        }
        return values;
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
