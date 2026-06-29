package cn.iocoder.yudao.module.clubpoints.hardening;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointE2EScriptContractHardeningTest {

    private static final List<String> REQUIRED_CDP_MARKERS = Arrays.asList(
            "Page.enable",
            "Runtime.enable",
            "Network.enable",
            "Runtime.exceptionThrown",
            "Network.responseReceived",
            "/admin-api/clubpoints/"
    );
    private static final List<String> REQUIRED_GATE_MARKERS = Arrays.asList(
            "pageErrors",
            "unexpectedClubpointsResponses",
            "clubpointsApiRequests",
            "visibleResultChanged",
            "assertNoPageErrors",
            "assertNoUnexpectedResponses",
            "assertActionObserved"
    );
    private static final List<String> REQUIRED_ROLE_MARKERS = Arrays.asList(
            "employee",
            "leader",
            "admin",
            "CLUBPOINTS_E2E_EMPLOYEE_USERNAME",
            "CLUBPOINTS_E2E_EMPLOYEE_PASSWORD",
            "CLUBPOINTS_E2E_LEADER_USERNAME",
            "CLUBPOINTS_E2E_LEADER_PASSWORD",
            "CLUBPOINTS_E2E_ADMIN_USERNAME",
            "CLUBPOINTS_E2E_ADMIN_PASSWORD"
    );
    private static final List<String> REQUIRED_FAILURE_MARKERS = Arrays.asList(
            "assertBusinessFailureRetainsInputAndShowsError",
            "non2xx",
            "inputRetained",
            "errorVisible"
    );
    private static final List<String> FORBIDDEN_DEPENDENCY_MARKERS = Arrays.asList(
            "@playwright/test",
            "from 'playwright'",
            "from \"playwright\"",
            "from 'puppeteer'",
            "from \"puppeteer\"",
            "require('playwright')",
            "require(\"playwright\")",
            "require('puppeteer')",
            "require(\"puppeteer\")"
    );
    private static final List<String> REQUIRED_LOGIN_SELECTOR_MARKERS = Arrays.asList(
            "isVisibleInput",
            "placeholder",
            "请输入用户名",
            "type !== 'checkbox'"
    );
    private static final List<String> REQUIRED_LOGIN_FLOW_MARKERS = Arrays.asList(
            "finishLoginAction",
            "loginAction",
            "!location.href.includes('/login')"
    );
    private static final List<String> FORBIDDEN_LOGIN_SELECTOR_MARKERS = Arrays.asList(
            "inputs.find((item) => item.type !== 'password') || inputs[0]"
    );
    private static final List<String> REQUIRED_WINDOWS_CLEANUP_MARKERS = Arrays.asList(
            "waitForProcessExit",
            "removeUserDataDirWithRetry",
            "EBUSY"
    );

    @Test
    void e2eScriptShouldExistUnderFrontendScripts() {
        Path script = scriptPath();
        assertTrue(Files.exists(script), "Missing M13 E2E CDP script: " + script);
    }

    @Test
    void e2eScriptShouldUseCdpAndCollectBrowserFailures() throws Exception {
        String content = scriptContent();

        assertContainsAll(content, REQUIRED_CDP_MARKERS, "CDP/network/page-error markers");
        assertContainsAll(content, REQUIRED_GATE_MARKERS, "machine gate markers");
        assertContainsAll(content, REQUIRED_ROLE_MARKERS, "three-role credential markers");
        assertContainsAll(content, REQUIRED_FAILURE_MARKERS, "business failure markers");
    }

    @Test
    void e2eScriptShouldStayZeroDependency() throws Exception {
        String content = scriptContent();

        List<String> violations = new ArrayList<>();
        for (String marker : FORBIDDEN_DEPENDENCY_MARKERS) {
            if (content.contains(marker)) {
                violations.add(marker);
            }
        }

        assertTrue(content.contains("node:http") || content.contains("node:https")
                        || content.contains("http from 'http'") || content.contains("https from 'https'"),
                "E2E script must use Node built-in HTTP(S) to talk to Chrome DevTools");
        assertTrue(content.contains("spawn(") || content.contains("execFile("),
                "E2E script must launch Chrome/Edge itself for an isolated CDP session");
        assertFalse(content.contains("test.skip(") || content.contains("it.skip("),
                "E2E script must not skip missing role flows");
        assertTrue(violations.isEmpty(), "E2E script must not add browser automation dependencies: " + violations);
    }

    @Test
    void e2eScriptShouldSelectLoginInputsByVisiblePurpose() throws Exception {
        String content = scriptContent();

        assertContainsAll(content, REQUIRED_LOGIN_SELECTOR_MARKERS, "purpose-based login input selector markers");
        assertContainsAll(content, REQUIRED_LOGIN_FLOW_MARKERS, "login flow markers");
        List<String> violations = new ArrayList<>();
        for (String marker : FORBIDDEN_LOGIN_SELECTOR_MARKERS) {
            if (content.contains(marker)) {
                violations.add(marker);
            }
        }
        assertTrue(violations.isEmpty(), "E2E script must not choose the first non-password input: " + violations);
    }

    @Test
    void e2eScriptShouldClearSessionBeforeOpeningLoginPage() throws Exception {
        String content = scriptContent();
        int loginStart = content.indexOf("async function loginAs");
        int loginEnd = content.indexOf("async function executePageAction", loginStart);
        assertTrue(loginStart >= 0 && loginEnd > loginStart, "Cannot locate loginAs function in E2E script");
        String loginAs = content.substring(loginStart, loginEnd);

        int clearIndex = loginAs.indexOf("await clearBrowserSession(cdp");
        int navigateIndex = loginAs.indexOf("await navigate(cdp, `${run.config.frontendUrl}/login`)");
        int waitForInputsIndex = loginAs.indexOf("await waitForExpression(cdp, 'login inputs'");

        assertTrue(clearIndex >= 0, "loginAs must clear browser session for each role");
        assertTrue(navigateIndex >= 0, "loginAs must navigate to the login page");
        assertTrue(waitForInputsIndex >= 0, "loginAs must wait for login inputs after navigation");
        assertTrue(clearIndex < navigateIndex,
                "loginAs must clear session before opening /login; otherwise the next role is redirected by the previous login state");
        assertTrue(navigateIndex < waitForInputsIndex,
                "loginAs must wait for login inputs after opening /login");
    }

    @Test
    void e2eScriptShouldCleanTemporaryChromeProfileReliablyOnWindows() throws Exception {
        String content = scriptContent();

        assertContainsAll(content, REQUIRED_WINDOWS_CLEANUP_MARKERS, "Windows Chrome temp profile cleanup markers");
    }

    private static void assertContainsAll(String content, List<String> markers, String groupName) {
        List<String> missing = new ArrayList<>();
        for (String marker : markers) {
            if (!content.contains(marker)) {
                missing.add(marker);
            }
        }
        assertTrue(missing.isEmpty(), "Missing " + groupName + ": " + missing);
    }

    private static String scriptContent() throws IOException {
        Path script = scriptPath();
        assertTrue(Files.exists(script), "Missing M13 E2E CDP script: " + script);
        return Files.readString(script, StandardCharsets.UTF_8);
    }

    private static Path scriptPath() {
        return findRuoyiRoot().resolve("yudao-ui/yudao-ui-admin-vue3/scripts/clubpoints-e2e-cdp.mjs");
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
