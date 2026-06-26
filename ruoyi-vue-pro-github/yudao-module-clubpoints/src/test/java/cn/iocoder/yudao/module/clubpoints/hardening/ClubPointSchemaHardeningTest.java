package cn.iocoder.yudao.module.clubpoints.hardening;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClubPointSchemaHardeningTest {

    private static final Pattern MYSQL_TABLE_PATTERN = Pattern.compile(
            "CREATE TABLE IF NOT EXISTS `([^`]+)` \\((.*?)\\) ENGINE", Pattern.DOTALL);
    private static final Pattern H2_TABLE_PATTERN = Pattern.compile(
            "CREATE TABLE IF NOT EXISTS \"([^\"]+)\" \\((.*?)\\);", Pattern.DOTALL);
    private static final Pattern MYSQL_UNIQUE_PATTERN = Pattern.compile(
            "UNIQUE KEY `([^`]+)` \\(([^)]+)\\)");
    private static final Pattern H2_UNIQUE_PATTERN = Pattern.compile(
            "CREATE UNIQUE INDEX IF NOT EXISTS \"([^\"]+)\" ON \"([^\"]+)\" \\(([^)]+)\\);");
    private static final Pattern PRIMARY_KEY_PATTERN = Pattern.compile("PRIMARY KEY \\(([^)]+)\\)");
    private static final Pattern COLUMN_PATTERN = Pattern.compile("^\\s*[`\"]([^`\"]+)[`\"]\\s+");
    private static final String TENANT_COLUMN = "tenant" + "_id";

    @Test
    void mysqlSchemaAndTestDdlShouldKeepTableColumnsPrimaryKeysAndUniqueKeysAligned() throws Exception {
        Path root = findRuoyiRoot();
        SchemaSnapshot mysql = parseMysqlSchema(read(root.resolve("sql/mysql/club-points-schema.sql")));
        SchemaSnapshot h2 = onlyClubPointTables(
                parseH2Schema(read(root.resolve("yudao-module-clubpoints/src/test/resources/sql/create_tables.sql"))));

        assertEquals(34, mysql.tables.size());
        assertEquals(mysql.tables.keySet(), h2.tables.keySet());
        assertEquals(expectedUniqueIndexes(), mysql.uniqueIndexes);
        assertEquals(mysql.uniqueIndexes, h2.uniqueIndexes);

        for (String table : mysql.tables.keySet()) {
            TableSnapshot mysqlTable = mysql.tables.get(table);
            TableSnapshot h2Table = h2.tables.get(table);
            assertEquals(Collections.singletonList("id"), mysqlTable.primaryKeyColumns, table + " mysql primary key");
            assertEquals(Collections.singletonList("id"), h2Table.primaryKeyColumns, table + " h2 primary key");
            assertEquals(mysqlTable.columns, h2Table.columns, table + " columns");
            assertEquals(true, mysqlTable.columns.contains("deleted"), table + " deleted column");
        }
    }

    @Test
    void schemaShouldNotUseTenantOrExportTablesAsFacts() throws Exception {
        Path root = findRuoyiRoot();
        String mysql = read(root.resolve("sql/mysql/club-points-schema.sql"));
        String h2 = read(root.resolve("yudao-module-clubpoints/src/test/resources/sql/create_tables.sql"));

        assertFalse(mysql.contains(TENANT_COLUMN));
        assertFalse(h2.contains(TENANT_COLUMN));
        assertFalse(parseMysqlSchema(mysql).tables.keySet().stream().anyMatch(table -> table.contains("export")));
        assertFalse(parseH2Schema(h2).tables.keySet().stream().anyMatch(table -> table.contains("export")));
    }

    private static SchemaSnapshot parseMysqlSchema(String sql) {
        Matcher tableMatcher = MYSQL_TABLE_PATTERN.matcher(sql);
        Map<String, TableSnapshot> tables = new LinkedHashMap<>();
        Map<String, IndexSnapshot> uniqueIndexes = new LinkedHashMap<>();
        while (tableMatcher.find()) {
            String tableName = tableMatcher.group(1);
            String body = tableMatcher.group(2);
            TableSnapshot table = parseTableBody(body);
            tables.put(tableName, table);
            for (String line : body.split("\\R")) {
                Matcher uniqueMatcher = MYSQL_UNIQUE_PATTERN.matcher(line);
                if (uniqueMatcher.find()) {
                    String indexName = uniqueMatcher.group(1);
                    uniqueIndexes.put(indexName, new IndexSnapshot(tableName, parseColumns(uniqueMatcher.group(2))));
                }
            }
        }
        return new SchemaSnapshot(tables, uniqueIndexes);
    }

    private static SchemaSnapshot parseH2Schema(String sql) {
        Matcher tableMatcher = H2_TABLE_PATTERN.matcher(sql);
        Map<String, TableSnapshot> tables = new LinkedHashMap<>();
        while (tableMatcher.find()) {
            tables.put(tableMatcher.group(1), parseTableBody(tableMatcher.group(2)));
        }

        Matcher uniqueMatcher = H2_UNIQUE_PATTERN.matcher(sql);
        Map<String, IndexSnapshot> uniqueIndexes = new LinkedHashMap<>();
        while (uniqueMatcher.find()) {
            uniqueIndexes.put(uniqueMatcher.group(1),
                    new IndexSnapshot(uniqueMatcher.group(2), parseColumns(uniqueMatcher.group(3))));
        }
        return new SchemaSnapshot(tables, uniqueIndexes);
    }

    private static SchemaSnapshot onlyClubPointTables(SchemaSnapshot snapshot) {
        Map<String, TableSnapshot> tables = new LinkedHashMap<>();
        for (Map.Entry<String, TableSnapshot> entry : snapshot.tables.entrySet()) {
            if (entry.getKey().startsWith("club_points_")) {
                tables.put(entry.getKey(), entry.getValue());
            }
        }

        Map<String, IndexSnapshot> uniqueIndexes = new LinkedHashMap<>();
        for (Map.Entry<String, IndexSnapshot> entry : snapshot.uniqueIndexes.entrySet()) {
            if (entry.getValue().table.startsWith("club_points_")) {
                uniqueIndexes.put(entry.getKey(), entry.getValue());
            }
        }
        return new SchemaSnapshot(tables, uniqueIndexes);
    }

    private static TableSnapshot parseTableBody(String body) {
        Set<String> columns = new LinkedHashSet<>();
        List<String> primaryKeyColumns = Collections.emptyList();
        for (String line : body.split("\\R")) {
            Matcher columnMatcher = COLUMN_PATTERN.matcher(line);
            if (columnMatcher.find()) {
                columns.add(columnMatcher.group(1));
            }
            Matcher primaryKeyMatcher = PRIMARY_KEY_PATTERN.matcher(line);
            if (primaryKeyMatcher.find()) {
                primaryKeyColumns = parseColumns(primaryKeyMatcher.group(1));
            }
        }
        return new TableSnapshot(columns, primaryKeyColumns);
    }

    private static List<String> parseColumns(String columns) {
        List<String> result = new ArrayList<>();
        for (String column : columns.split(",")) {
            result.add(column.replace("`", "")
                    .replace("\"", "")
                    .trim());
        }
        return result;
    }

    private static Map<String, IndexSnapshot> expectedUniqueIndexes() {
        Map<String, IndexSnapshot> indexes = new LinkedHashMap<>();
        put(indexes, "uk_club_points_rule_version_no", "club_points_rule_version", "version_no");
        put(indexes, "uk_club_points_rule_item_version_code", "club_points_rule_item", "rule_version_id", "item_code");
        put(indexes, "uk_club_points_club_code", "club_points_club", "code");
        put(indexes, "uk_club_points_club_name", "club_points_club", "name");
        put(indexes, "uk_club_points_club_member_active", "club_points_club_member", "active_unique_key");
        put(indexes, "uk_club_points_club_leader_active", "club_points_club_leader", "active_unique_key");
        put(indexes, "uk_club_points_activity_config_version", "club_points_activity_point_config_version", "activity_id", "version_no");
        put(indexes, "uk_club_points_activity_registration_active", "club_points_activity_registration", "active_unique_key");
        put(indexes, "uk_club_points_attendance_registration_target", "club_points_attendance_record", "registration_id", "target_type");
        put(indexes, "uk_club_points_activity_settlement_run_key", "club_points_activity_settlement_run", "run_key");
        put(indexes, "uk_club_points_transaction_no", "club_points_transaction", "transaction_no");
        put(indexes, "uk_club_points_transaction_idempotency", "club_points_transaction", "idempotency_key");
        put(indexes, "uk_club_points_transaction_reverse", "club_points_transaction", "reverse_of_transaction_id");
        put(indexes, "uk_club_points_point_account_user", "club_points_point_account", "user_id");
        put(indexes, "uk_club_points_freeze_no", "club_points_freeze", "freeze_no");
        put(indexes, "uk_club_points_freeze_idempotency", "club_points_freeze", "idempotency_key");
        put(indexes, "uk_club_points_freeze_source", "club_points_freeze", "source_type", "source_id");
        put(indexes, "uk_club_points_user_year_status_user_year", "club_points_user_year_status", "user_id", "year");
        put(indexes, "uk_club_points_contribution_material_request", "club_points_contribution_material", "request_no");
        put(indexes, "uk_club_points_contribution_item_idempotency", "club_points_contribution_item", "idempotency_key");
        put(indexes, "uk_club_points_contribution_item_effective", "club_points_contribution_item", "effective_unique_key");
        put(indexes, "uk_club_points_redemption_eligibility_user", "club_points_redemption_eligibility_snapshot", "batch_id", "user_id");
        put(indexes, "uk_club_points_redemption_application_no", "club_points_redemption_application", "application_no");
        put(indexes, "uk_club_points_redemption_application_idempotency", "club_points_redemption_application", "idempotency_key");
        put(indexes, "uk_club_points_stock_lock_application", "club_points_stock_lock", "application_id");
        put(indexes, "uk_club_points_stock_lock_idempotency", "club_points_stock_lock", "idempotency_key");
        put(indexes, "uk_club_points_annual_clearing_user_year", "club_points_annual_clearing_record", "year", "user_id");
        put(indexes, "uk_club_points_annual_clearing_idempotency", "club_points_annual_clearing_record", "idempotency_key");
        put(indexes, "uk_club_points_annual_ranking_year_club", "club_points_annual_ranking_record", "year", "club_code_snapshot");
        put(indexes, "uk_club_points_job_run_idempotency", "club_points_job_run", "idempotency_key");
        return indexes;
    }

    private static void put(Map<String, IndexSnapshot> indexes, String name, String table, String... columns) {
        indexes.put(name, new IndexSnapshot(table, Arrays.asList(columns)));
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static Path findRuoyiRoot() {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("sql/mysql/club-points-schema.sql"))) {
                return current;
            }
            Path nested = current.resolve("ruoyi-vue-pro-github/sql/mysql/club-points-schema.sql");
            if (Files.exists(nested)) {
                return current.resolve("ruoyi-vue-pro-github");
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate ruoyi-vue-pro-github root");
    }

    private static final class SchemaSnapshot {

        private final Map<String, TableSnapshot> tables;
        private final Map<String, IndexSnapshot> uniqueIndexes;

        private SchemaSnapshot(Map<String, TableSnapshot> tables, Map<String, IndexSnapshot> uniqueIndexes) {
            this.tables = tables;
            this.uniqueIndexes = uniqueIndexes;
        }
    }

    private static final class TableSnapshot {

        private final Set<String> columns;
        private final List<String> primaryKeyColumns;

        private TableSnapshot(Set<String> columns, List<String> primaryKeyColumns) {
            this.columns = columns;
            this.primaryKeyColumns = primaryKeyColumns;
        }
    }

    private static final class IndexSnapshot {

        private final String table;
        private final List<String> columns;

        private IndexSnapshot(String table, List<String> columns) {
            this.table = table;
            this.columns = columns;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof IndexSnapshot)) {
                return false;
            }
            IndexSnapshot that = (IndexSnapshot) o;
            return table.equals(that.table) && columns.equals(that.columns);
        }

        @Override
        public int hashCode() {
            int result = table.hashCode();
            result = 31 * result + columns.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return table + columns;
        }
    }
}
