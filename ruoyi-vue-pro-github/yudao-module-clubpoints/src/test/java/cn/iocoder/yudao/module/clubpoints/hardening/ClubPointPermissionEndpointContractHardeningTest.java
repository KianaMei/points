package cn.iocoder.yudao.module.clubpoints.hardening;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointPermissionEndpointContractHardeningTest {

    private static final Pattern V_HAS_PERMI_PATTERN = Pattern.compile(
            "v-hasPermi\\s*=\\s*\"\\[([^]]+)]\"");
    private static final Pattern SINGLE_QUOTED_PERMISSION_PATTERN = Pattern.compile(
            "'(clubpoints:[^']+)'");
    private static final Pattern PRE_AUTHORIZE_PATTERN = Pattern.compile(
            "@PreAuthorize\\(\\s*\"@ss\\.hasPermission\\('([^']+)'\\)\"\\s*\\)");

    @Test
    void frontendButtonPermissionsShouldExistInSeedAndBackend() throws Exception {
        Set<String> frontendPermissions = readFrontendButtonPermissions();
        Set<String> seedPermissions = readSeedMenus().values().stream()
                .map(menu -> menu.permission)
                .filter(permission -> permission != null && !permission.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> backendPermissions = readBackendPreAuthorizePermissions();

        assertTrue(frontendPermissions.size() > 35, "Parsed too few frontend button permissions: "
                + frontendPermissions.size());

        List<String> missingInSeed = frontendPermissions.stream()
                .filter(permission -> !seedPermissions.contains(permission))
                .sorted()
                .collect(Collectors.toList());
        List<String> missingInBackend = frontendPermissions.stream()
                .filter(permission -> !backendPermissions.contains(permission))
                .sorted()
                .collect(Collectors.toList());

        assertTrue(missingInSeed.isEmpty(), "Frontend v-hasPermi permissions missing from seed: " + missingInSeed);
        assertTrue(missingInBackend.isEmpty(),
                "Frontend v-hasPermi permissions missing backend @PreAuthorize: " + missingInBackend);
    }

    @Test
    void backendPreAuthorizePermissionsShouldExistInSeed() throws Exception {
        Set<String> backendPermissions = readBackendPreAuthorizePermissions();
        Set<String> seedPermissions = readSeedMenus().values().stream()
                .map(menu -> menu.permission)
                .filter(permission -> permission != null && !permission.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        assertTrue(backendPermissions.size() > 50, "Parsed too few backend permissions: "
                + backendPermissions.size());

        List<String> missingInSeed = backendPermissions.stream()
                .filter(permission -> !seedPermissions.contains(permission))
                .sorted()
                .collect(Collectors.toList());

        assertTrue(missingInSeed.isEmpty(), "Backend @PreAuthorize permissions missing from seed: " + missingInSeed);
    }

    @Test
    void seedButtonPermissionsShouldBackRealControllerPermission() throws Exception {
        Set<String> backendPermissions = readBackendPreAuthorizePermissions();
        List<String> deadButtonPermissions = readSeedMenus().values().stream()
                .filter(menu -> menu.type == 3)
                .filter(menu -> menu.permission != null && !menu.permission.isEmpty())
                .filter(menu -> !backendPermissions.contains(menu.permission))
                .map(menu -> menu.id + " " + menu.name + " -> " + menu.permission)
                .sorted()
                .collect(Collectors.toList());

        assertTrue(deadButtonPermissions.isEmpty(),
                "Seed button permissions without backend @PreAuthorize are fake actions: " + deadButtonPermissions);
    }

    private static Set<String> readFrontendButtonPermissions() throws IOException {
        Path viewRoot = findRuoyiRoot().resolve("yudao-ui/yudao-ui-admin-vue3/src/views/clubpoints");
        Set<String> permissions = new LinkedHashSet<>();
        try (Stream<Path> stream = Files.walk(viewRoot)) {
            for (Path file : (Iterable<Path>) stream
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".vue"))
                    .sorted()::iterator) {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                Matcher hasPermiMatcher = V_HAS_PERMI_PATTERN.matcher(content);
                while (hasPermiMatcher.find()) {
                    Matcher permissionMatcher = SINGLE_QUOTED_PERMISSION_PATTERN.matcher(hasPermiMatcher.group(1));
                    while (permissionMatcher.find()) {
                        permissions.add(permissionMatcher.group(1));
                    }
                }
            }
        }
        return permissions;
    }

    private static Set<String> readBackendPreAuthorizePermissions() throws IOException {
        Path controllerRoot = findRuoyiRoot()
                .resolve("yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller");
        Set<String> permissions = new LinkedHashSet<>();
        try (Stream<Path> stream = Files.walk(controllerRoot)) {
            for (Path file : (Iterable<Path>) stream
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .sorted()::iterator) {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                Matcher matcher = PRE_AUTHORIZE_PATTERN.matcher(content);
                while (matcher.find()) {
                    permissions.add(matcher.group(1));
                }
            }
        }
        return permissions;
    }

    private static Map<Long, MenuRow> readSeedMenus() throws IOException {
        Path seed = findRuoyiRoot().resolve("sql/mysql/club-points-seed.sql");
        List<String> lines = Files.readAllLines(seed, StandardCharsets.UTF_8);
        Map<Long, MenuRow> menus = new LinkedHashMap<>();
        boolean inSystemMenuInsert = false;
        for (String line : lines) {
            if (line.startsWith("INSERT INTO `system_menu`")) {
                inSystemMenuInsert = true;
                continue;
            }
            if (inSystemMenuInsert && line.startsWith("ON DUPLICATE KEY UPDATE")) {
                inSystemMenuInsert = false;
                continue;
            }
            if (!inSystemMenuInsert || !line.trim().startsWith("(")) {
                continue;
            }
            MenuRow menu = parseMenuRow(line.trim());
            menus.put(menu.id, menu);
        }
        return menus;
    }

    private static MenuRow parseMenuRow(String tupleLine) {
        String tuple = tupleLine;
        if (tuple.endsWith(",")) {
            tuple = tuple.substring(0, tuple.length() - 1);
        }
        if (tuple.endsWith(";")) {
            tuple = tuple.substring(0, tuple.length() - 1);
        }
        tuple = tuple.substring(1, tuple.length() - 1);
        List<String> values = splitSqlTuple(tuple);
        long id = Long.parseLong(values.get(0));
        String name = unquote(values.get(1));
        String permission = unquote(values.get(2));
        int type = Integer.parseInt(values.get(3));
        return new MenuRow(id, name, permission, type);
    }

    private static List<String> splitSqlTuple(String tuple) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < tuple.length(); i++) {
            char ch = tuple.charAt(i);
            if (ch == '\'') {
                current.append(ch);
                if (inQuote && i + 1 < tuple.length() && tuple.charAt(i + 1) == '\'') {
                    current.append(tuple.charAt(i + 1));
                    i++;
                } else {
                    inQuote = !inQuote;
                }
                continue;
            }
            if (ch == ',' && !inQuote) {
                values.add(current.toString().trim());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        values.add(current.toString().trim());
        return values;
    }

    private static String unquote(String value) {
        if ("NULL".equalsIgnoreCase(value)) {
            return null;
        }
        if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1).replace("''", "'");
        }
        return value;
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

    private static final class MenuRow {

        private final long id;
        private final String name;
        private final String permission;
        private final int type;

        private MenuRow(long id, String name, String permission, int type) {
            this.id = id;
            this.name = name;
            this.permission = permission;
            this.type = type;
        }

    }

}
