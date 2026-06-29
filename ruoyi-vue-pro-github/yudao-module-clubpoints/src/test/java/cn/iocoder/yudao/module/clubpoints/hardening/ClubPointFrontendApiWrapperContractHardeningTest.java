package cn.iocoder.yudao.module.clubpoints.hardening;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointFrontendApiWrapperContractHardeningTest {

    private static final Pattern TS_CONST_PATTERN = Pattern.compile(
            "(?m)^\\s*const\\s+([A-Z_]+)\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern TS_REQUEST_PATTERN = Pattern.compile(
            "request\\.(get|post|put|delete|download)\\s*\\(\\s*\\{[\\s\\S]*?url\\s*:\\s*(`[^`]+`|'[^']+'|\"[^\"]+\")");
    private static final Pattern JAVA_CLASS_REQUEST_MAPPING_PATTERN = Pattern.compile(
            "@RequestMapping\\(\\s*\"([^\"]+)\"\\s*\\)");
    private static final Pattern JAVA_METHOD_MAPPING_PATTERN = Pattern.compile(
            "@(Get|Post|Put|Delete)Mapping\\(\\s*\"([^\"]+)\"\\s*\\)");

    @Test
    void everyFrontendApiWrapperShouldHaveBackendControllerMapping() throws Exception {
        Set<String> frontendRequests = readFrontendApiRequests();
        Set<String> backendMappings = readBackendControllerMappings();

        assertTrue(frontendRequests.size() > 80, "Frontend API wrapper scan parsed too few requests: "
                + frontendRequests.size());
        assertTrue(backendMappings.size() > 70, "Backend controller scan parsed too few mappings: "
                + backendMappings.size());

        List<String> missing = new ArrayList<>();
        for (String request : frontendRequests) {
            if (!backendMappings.contains(request)) {
                missing.add(request);
            }
        }
        missing.sort(Comparator.naturalOrder());

        assertTrue(missing.isEmpty(), "Frontend clubpoints API wrappers without backend mapping: " + missing);
    }

    private static Set<String> readFrontendApiRequests() throws IOException {
        Path apiRoot = findRuoyiRoot().resolve("yudao-ui/yudao-ui-admin-vue3/src/api/clubpoints");
        Set<String> requests = new LinkedHashSet<>();
        try (Stream<Path> stream = Files.walk(apiRoot)) {
            for (Path file : (Iterable<Path>) stream
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".ts"))
                    .sorted()::iterator) {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                Map<String, String> constants = readTsConstants(content);
                Matcher matcher = TS_REQUEST_PATTERN.matcher(content);
                while (matcher.find()) {
                    String method = "download".equals(matcher.group(1))
                            ? "GET" : matcher.group(1).toUpperCase();
                    String path = resolveTsUrlExpression(matcher.group(2).trim(), constants);
                    if (path != null && path.startsWith("/clubpoints/")) {
                        requests.add(method + " " + path);
                    }
                }
            }
        }
        return requests;
    }

    private static Map<String, String> readTsConstants(String content) {
        Map<String, String> constants = new HashMap<>();
        Matcher matcher = TS_CONST_PATTERN.matcher(content);
        while (matcher.find()) {
            constants.put(matcher.group(1), matcher.group(2));
        }
        return constants;
    }

    private static String resolveTsUrlExpression(String expression, Map<String, String> constants) {
        String path = expression;
        if (path.startsWith("`")) {
            path = path.substring(1);
            int end = path.indexOf('`');
            if (end >= 0) {
                path = path.substring(0, end);
            }
            Matcher variableMatcher = Pattern.compile("\\$\\{([A-Z_]+)}").matcher(path);
            StringBuffer resolved = new StringBuffer();
            while (variableMatcher.find()) {
                String value = constants.get(variableMatcher.group(1));
                if (value == null) {
                    return null;
                }
                variableMatcher.appendReplacement(resolved, Matcher.quoteReplacement(value));
            }
            variableMatcher.appendTail(resolved);
            path = resolved.toString();
        } else if (path.startsWith("'") || path.startsWith("\"")) {
            char quote = path.charAt(0);
            path = path.substring(1);
            int end = path.indexOf(quote);
            if (end >= 0) {
                path = path.substring(0, end);
            }
        } else {
            return null;
        }
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        return path;
    }

    private static Set<String> readBackendControllerMappings() throws IOException {
        Path controllerRoot = findRuoyiRoot()
                .resolve("yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller");
        Set<String> mappings = new LinkedHashSet<>();
        try (Stream<Path> stream = Files.walk(controllerRoot)) {
            for (Path file : (Iterable<Path>) stream
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .sorted()::iterator) {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                Matcher classMatcher = JAVA_CLASS_REQUEST_MAPPING_PATTERN.matcher(content);
                if (!classMatcher.find()) {
                    continue;
                }
                String classPrefix = classMatcher.group(1);
                Matcher methodMatcher = JAVA_METHOD_MAPPING_PATTERN.matcher(content);
                while (methodMatcher.find()) {
                    mappings.add(methodMatcher.group(1).toUpperCase() + " "
                            + normalizePath(classPrefix + methodMatcher.group(2)));
                }
            }
        }
        return mappings;
    }

    private static String normalizePath(String path) {
        return path.replaceAll("/{2,}", "/");
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
