package cn.iocoder.yudao.module.clubpoints.hardening;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointViewNoDirectRequestHardeningTest {

    private static final Pattern DIRECT_AXIOS_IMPORT = Pattern.compile("@/config/axios");
    private static final Pattern DIRECT_REQUEST_CALL = Pattern.compile(
            "\\brequest\\.(get|post|put|delete|download)\\s*\\(");
    private static final Pattern DIRECT_CLUBPOINTS_URL = Pattern.compile(
            "url\\s*:\\s*([`'\"])/clubpoints/");

    @Test
    void clubpointsViewsShouldNotCallAxiosOrWriteBusinessUrlsDirectly() throws Exception {
        List<String> violations = new ArrayList<>();
        Path viewRoot = findRuoyiRoot()
                .resolve("yudao-ui/yudao-ui-admin-vue3/src/views/clubpoints");
        try (Stream<Path> stream = Files.walk(viewRoot)) {
            for (Path file : (Iterable<Path>) stream
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".vue"))
                    .sorted()::iterator) {
                collectViolations(viewRoot, file, violations);
            }
        }

        violations.sort(Comparator.naturalOrder());
        assertTrue(violations.isEmpty(), "Clubpoints views must call src/api wrappers only: " + violations);
    }

    private static void collectViolations(Path viewRoot, Path file, List<String> violations) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        String relativePath = viewRoot.relativize(file).toString().replace('\\', '/');
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (DIRECT_AXIOS_IMPORT.matcher(line).find()
                    || DIRECT_REQUEST_CALL.matcher(line).find()
                    || DIRECT_CLUBPOINTS_URL.matcher(line).find()) {
                violations.add(relativePath + ":" + (i + 1) + " " + line.trim());
            }
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
