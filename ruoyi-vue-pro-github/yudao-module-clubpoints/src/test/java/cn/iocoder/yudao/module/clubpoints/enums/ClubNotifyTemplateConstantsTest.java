package cn.iocoder.yudao.module.clubpoints.enums;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubNotifyTemplateConstantsTest {

    @Test
    void constantsShouldMatchSeedNotifyTemplates() throws Exception {
        Set<String> seedTemplateCodes = parseSeedNotifyTemplateCodes();
        Set<String> constantTemplateCodes = readConstantValues();

        assertEquals(seedTemplateCodes, constantTemplateCodes);
    }

    @Test
    void constantsShouldExposeM2RequiredTemplates() throws Exception {
        Set<String> constantTemplateCodes = readConstantValues();

        assertTrue(constantTemplateCodes.contains(ClubNotifyTemplateConstants.TEMPLATE_ACTIVITY_REVIEWED));
        assertTrue(constantTemplateCodes.contains(ClubNotifyTemplateConstants.TEMPLATE_POINTS_CHANGED));
        assertTrue(constantTemplateCodes.contains(ClubNotifyTemplateConstants.TEMPLATE_REDEMPTION_REVIEWED));
        assertTrue(constantTemplateCodes.contains(ClubNotifyTemplateConstants.TEMPLATE_DISPUTE_REPLIED));
    }

    private static Set<String> readConstantValues() throws IllegalAccessException {
        Set<String> values = new TreeSet<>();
        Field[] fields = ClubNotifyTemplateConstants.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(String.class) && Modifier.isStatic(field.getModifiers())) {
                values.add((String) field.get(null));
            }
        }
        return values;
    }

    private static Set<String> parseSeedNotifyTemplateCodes() throws Exception {
        String seed = new String(Files.readAllBytes(findSeedPath()), StandardCharsets.UTF_8);
        int start = seed.indexOf("INSERT INTO `system_notify_template`");
        int end = seed.indexOf("ON DUPLICATE KEY UPDATE", start);
        String notifyTemplateBlock = seed.substring(start, end);

        Set<String> values = new TreeSet<>();
        Pattern pattern = Pattern.compile("\\(\\d+, '[^']+', '(club_points_[a-z0-9_]+)'");
        Matcher matcher = pattern.matcher(notifyTemplateBlock);
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
