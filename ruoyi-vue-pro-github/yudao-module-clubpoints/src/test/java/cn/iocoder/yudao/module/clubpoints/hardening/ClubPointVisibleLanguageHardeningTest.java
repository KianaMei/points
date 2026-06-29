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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointVisibleLanguageHardeningTest {

    private static final Pattern VISIBLE_ATTRIBUTE_PATTERN = Pattern.compile(
            "\\b(?:label|title|placeholder)\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern TEXT_NODE_PATTERN = Pattern.compile(">([^<]+)<");
    private static final List<String> FORBIDDEN_VISIBLE_TERMS = Arrays.asList(
            "runKey",
            "resultJson",
            "requestNo",
            "bizId",
            "idempotencyKey",
            "settlementRunId",
            "targetSnapshotJson",
            "beforeJson",
            "afterJson",
            "活动ID",
            "俱乐部ID",
            "员工ID",
            "报名ID",
            "流水ID",
            "运行ID",
            "业务ID",
            "操作人ID",
            "请求号",
            "运行键",
            "幂等键"
    );

    private static final List<AllowedVisibleTerm> ALLOWED_VISIBLE_TERMS = Arrays.asList(
            allowed("admin/audit/index.vue", "bizId", "审计详情技术诊断信息-业务对象字段",
                    "审计追溯需要定位原始业务对象"),
            allowed("admin/audit/index.vue", "targetSnapshotJson", "审计详情技术诊断信息-目标快照字段",
                    "审计详情需要查看原始目标快照"),
            allowed("admin/audit/index.vue", "beforeJson", "审计详情技术诊断信息-变更前快照字段",
                    "审计详情需要查看变更前原始快照"),
            allowed("admin/audit/index.vue", "afterJson", "审计详情技术诊断信息-变更后快照字段",
                    "审计详情需要查看变更后原始快照"),
            allowed("admin/job-run/index.vue", "runKey", "任务详情技术诊断信息-任务批次字段",
                    "任务异常处理需要定位后台执行批次"),
            allowed("admin/job-run/index.vue", "bizId", "任务详情技术诊断信息-业务对象字段",
                    "任务异常处理需要定位后台业务对象"),
            allowed("admin/job-run/index.vue", "idempotencyKey", "任务详情技术诊断信息-幂等字段",
                    "任务异常处理需要排查重复执行"),
            allowed("admin/job-run/index.vue", "resultJson", "任务详情技术诊断信息-运行结果字段",
                    "任务异常处理需要查看原始运行结果")
    );

    @Test
    void clubpointsViewsShouldNotExposeDatabaseOrDeveloperLanguage() throws Exception {
        List<String> violations = new ArrayList<>();
        Path viewRoot = findRuoyiRoot()
                .resolve("yudao-ui/yudao-ui-admin-vue3/src/views/clubpoints");
        try (Stream<Path> stream = Files.walk(viewRoot)) {
            for (Path file : (Iterable<Path>) stream
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".vue"))
                    .sorted()::iterator) {
                collectVisibleLanguageViolations(viewRoot, file, violations);
            }
        }

        violations.sort(Comparator.naturalOrder());
        assertTrue(violations.isEmpty(), "Clubpoints visible copy exposes technical language: " + violations);
    }

    @Test
    void visibleLanguageWhitelistShouldBeExactAndAuditable() {
        Set<String> keys = new HashSet<>();
        List<String> violations = new ArrayList<>();
        for (AllowedVisibleTerm allowedTerm : ALLOWED_VISIBLE_TERMS) {
            if (allowedTerm.file == null || allowedTerm.file.trim().isEmpty()) {
                violations.add("Missing whitelist file for term " + allowedTerm.term);
            }
            if (allowedTerm.term == null || allowedTerm.term.trim().isEmpty()
                    || allowedTerm.term.contains("等") || allowedTerm.term.contains("技术字段")) {
                violations.add("Whitelist term must be exact: " + allowedTerm.term);
            }
            if (!FORBIDDEN_VISIBLE_TERMS.contains(allowedTerm.term)) {
                violations.add("Whitelist term is not a forbidden visible term: "
                        + allowedTerm.file + " -> " + allowedTerm.term);
            }
            if (!"技术诊断信息".equals(allowedTerm.requiredRegionTitle)) {
                violations.add("Whitelist term must be limited to 技术诊断信息 region: "
                        + allowedTerm.file + " -> " + allowedTerm.term);
            }
            if (allowedTerm.visiblePosition == null || allowedTerm.visiblePosition.trim().length() < 6
                    || allowedTerm.visiblePosition.contains("默认")) {
                violations.add("Whitelist visible position must be exact: "
                        + allowedTerm.file + " -> " + allowedTerm.term);
            }
            if (allowedTerm.businessReason == null || allowedTerm.businessReason.trim().length() < 8
                    || allowedTerm.businessReason.contains("技术字段") || allowedTerm.businessReason.contains("需要排查等")) {
                violations.add("Whitelist business reason must be explicit: "
                        + allowedTerm.file + " -> " + allowedTerm.term);
            }
            String key = allowedTerm.file + "|" + allowedTerm.term + "|" + allowedTerm.requiredRegionTitle;
            if (!keys.add(key)) {
                violations.add("Duplicate whitelist term: " + key);
            }
        }

        assertTrue(violations.isEmpty(), "Visible language whitelist is not exact: " + violations);
    }

    private static void collectVisibleLanguageViolations(Path viewRoot, Path file,
                                                         List<String> violations) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        String relativePath = viewRoot.relativize(file).toString().replace('\\', '/');
        int templateDepth = 0;
        String visibleRegionTitle = null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("<template")) {
                templateDepth++;
            }
            if (templateDepth > 0) {
                if (line.contains("技术诊断信息")) {
                    visibleRegionTitle = "技术诊断信息";
                }
                for (String visibleFragment : extractVisibleFragments(line)) {
                    for (String term : FORBIDDEN_VISIBLE_TERMS) {
                        if (visibleFragment.contains(term) && !isAllowed(relativePath, term, visibleRegionTitle)) {
                            violations.add(relativePath + ":" + (i + 1) + " contains [" + term + "] "
                                    + visibleFragment.trim());
                        }
                    }
                }
                if (visibleRegionTitle != null && line.contains("</el-collapse-item>")) {
                    visibleRegionTitle = null;
                }
            }
            if (line.contains("</template>")) {
                templateDepth--;
                if (templateDepth <= 0) {
                    templateDepth = 0;
                    visibleRegionTitle = null;
                }
            }
        }
    }

    private static List<String> extractVisibleFragments(String line) {
        List<String> fragments = new ArrayList<>();
        Matcher attributeMatcher = VISIBLE_ATTRIBUTE_PATTERN.matcher(line);
        while (attributeMatcher.find()) {
            fragments.add(attributeMatcher.group(1));
        }
        Matcher textMatcher = TEXT_NODE_PATTERN.matcher(line);
        while (textMatcher.find()) {
            String text = textMatcher.group(1).replaceAll("\\{\\{[^}]*}}", "").trim();
            if (!text.isEmpty()) {
                fragments.add(text);
            }
        }
        return fragments;
    }

    private static boolean isAllowed(String relativePath, String term, String visibleRegionTitle) {
        for (AllowedVisibleTerm allowedTerm : ALLOWED_VISIBLE_TERMS) {
            if (allowedTerm.file.equals(relativePath)
                    && allowedTerm.term.equals(term)
                    && allowedTerm.requiredRegionTitle.equals(visibleRegionTitle)) {
                return true;
            }
        }
        return false;
    }

    private static AllowedVisibleTerm allowed(String file, String term, String visiblePosition,
                                              String businessReason) {
        return new AllowedVisibleTerm(file, term, "技术诊断信息", visiblePosition, businessReason);
    }

    private static final class AllowedVisibleTerm {

        private final String file;
        private final String term;
        private final String requiredRegionTitle;
        private final String visiblePosition;
        private final String businessReason;

        private AllowedVisibleTerm(String file, String term, String requiredRegionTitle,
                                   String visiblePosition, String businessReason) {
            this.file = file;
            this.term = term;
            this.requiredRegionTitle = requiredRegionTitle;
            this.visiblePosition = visiblePosition;
            this.businessReason = businessReason;
        }

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

}
