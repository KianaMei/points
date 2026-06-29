package cn.iocoder.yudao.module.clubpoints.hardening;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointMenuRouteContractHardeningTest {

    @Test
    void seedShouldExposeFinalThreeRoleMenuNames() throws Exception {
        Map<Long, MenuRow> menus = readSeedMenus();

        assertEquals("俱乐部积分", menus.get(1300010000L).name);
        assertEquals("员工积分中心", menus.get(1300010100L).name);
        assertEquals("负责人工作台", menus.get(1300010200L).name);
        assertEquals("积分管理后台", menus.get(1300010300L).name);

        assertChildNames(menus, 1300010100L, Arrays.asList(
                "我的积分",
                "我的俱乐部",
                "活动报名签到",
                "积分兑换",
                "我的异议",
                "我的通知"
        ));
        assertChildNames(menus, 1300010200L, Arrays.asList(
                "负责人首页",
                "负责俱乐部",
                "活动管理",
                "报名与签到",
                "非签到积分材料"
        ));
        assertChildNames(menus, 1300010300L, Arrays.asList(
                "管理员首页",
                "规则配置",
                "俱乐部管理",
                "活动审核与管理",
                "活动积分发放",
                "积分账户",
                "积分流水",
                "非签到材料审核",
                "管理员代录",
                "兑换批次",
                "礼品维护",
                "兑换审核",
                "异议处理",
                "年度清零",
                "年度排名与激励",
                "预算记录",
                "报表中心",
                "审计日志",
                "任务异常处理"
        ));
    }

    @Test
    void seedMenuComponentsShouldPointToExistingVueFiles() throws Exception {
        Map<Long, MenuRow> menus = readSeedMenus();
        Path viewsRoot = findRuoyiRoot().resolve("yudao-ui/yudao-ui-admin-vue3/src/views");

        List<String> missingComponents = menus.values().stream()
                .filter(menu -> menu.type == 2)
                .filter(menu -> menu.component != null && menu.component.startsWith("clubpoints/"))
                .filter(menu -> !Files.exists(viewsRoot.resolve(menu.component + ".vue")))
                .map(menu -> menu.id + " " + menu.name + " -> " + menu.component + ".vue")
                .sorted()
                .collect(Collectors.toList());

        assertTrue(missingComponents.isEmpty(), "Seed menu component files are missing: " + missingComponents);
    }

    @Test
    void seedShouldNotExposeOldSettlementActionNames() throws Exception {
        List<String> violations = readSeedMenus().values().stream()
                .filter(menu -> menu.name.contains("活动结算")
                        || menu.name.contains("手动生成积分")
                        || menu.name.contains("手动生成活动积分")
                        || menu.name.contains("待发放活动"))
                .map(menu -> menu.id + " " + menu.name)
                .sorted()
                .collect(Collectors.toList());

        assertTrue(violations.isEmpty(), "Seed menu/button names still expose old settlement wording: " + violations);
    }

    private static void assertChildNames(Map<Long, MenuRow> menus, long parentId, List<String> expectedNames) {
        List<String> actualNames = menus.values().stream()
                .filter(menu -> menu.parentId == parentId && menu.type == 2)
                .sorted(Comparator.comparingInt(menu -> menu.sort))
                .map(menu -> menu.name)
                .collect(Collectors.toList());

        assertEquals(expectedNames, actualNames, "Unexpected child menu names under " + menus.get(parentId).name);
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
        int sort = Integer.parseInt(values.get(4));
        long parentId = Long.parseLong(values.get(5));
        String component = unquote(values.get(8));
        return new MenuRow(id, name, permission, type, sort, parentId, component);
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
        private final int sort;
        private final long parentId;
        private final String component;

        private MenuRow(long id, String name, String permission, int type, int sort,
                        long parentId, String component) {
            this.id = id;
            this.name = name;
            this.permission = permission;
            this.type = type;
            this.sort = sort;
            this.parentId = parentId;
            this.component = component;
        }

    }

}
