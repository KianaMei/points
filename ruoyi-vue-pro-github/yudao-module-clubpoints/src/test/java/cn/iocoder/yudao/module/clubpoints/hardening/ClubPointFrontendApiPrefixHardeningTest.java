package cn.iocoder.yudao.module.clubpoints.hardening;

import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.framework.web.config.YudaoWebAutoConfiguration;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dashboard.ClubPointDashboardAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.ClubPointReportAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.ClubPointActivityAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.dashboard.ClubPointDashboardAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.ClubPointRedemptionAppController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.activity.ClubPointActivityLeaderController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.ClubPointContributionLeaderController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.dashboard.ClubPointDashboardLeaderController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointFrontendApiPrefixHardeningTest {

    private static final String ADMIN_API_PREFIX = "/admin-api";
    private static final String APP_API_PREFIX = "/app-api";
    private static final String ADMIN_CONTROLLER_PATTERN = "**.controller.admin.**";
    private static final String CLUBPOINTS_APP_CONTROLLER_PATTERN =
            "**.module.clubpoints.controller.app.**";
    private static final String CLUBPOINTS_LEADER_CONTROLLER_PATTERN =
            "**.module.clubpoints.controller.leader.**";

    @Test
    void frontendRegressionControllersShouldResolveToAdminApiPrefix() throws Exception {
        WebProperties webProperties = new WebProperties();
        webProperties.getAdminApi().setController(readConfiguredAdminApiController());

        RequestMappingHandlerMapping mapping = buildMapping(webProperties);
        Map<String, Predicate<Class<?>>> pathPrefixes = mapping.getPathPrefixes();

        assertEquals(ADMIN_API_PREFIX, firstMatchingPrefix(pathPrefixes, ClubPointDashboardAdminController.class));
        assertEquals(ADMIN_API_PREFIX, firstMatchingPrefix(pathPrefixes, ClubPointReportAdminController.class));
        assertEquals(ADMIN_API_PREFIX, firstMatchingPrefix(pathPrefixes, ClubPointDashboardAppController.class));
        assertEquals(ADMIN_API_PREFIX, firstMatchingPrefix(pathPrefixes, ClubPointActivityAppController.class));
        assertEquals(ADMIN_API_PREFIX, firstMatchingPrefix(pathPrefixes, ClubPointRedemptionAppController.class));
        assertEquals(ADMIN_API_PREFIX, firstMatchingPrefix(pathPrefixes, ClubPointDashboardLeaderController.class));
        assertEquals(ADMIN_API_PREFIX, firstMatchingPrefix(pathPrefixes, ClubPointActivityLeaderController.class));
        assertEquals(ADMIN_API_PREFIX, firstMatchingPrefix(pathPrefixes, ClubPointContributionLeaderController.class));
    }

    @Test
    void adminApiControllerPatternShouldKeepAdminAndAddOnlyClubPointsFrontendScopes() throws Exception {
        String adminController = readConfiguredAdminApiController();

        assertTrue(adminController.contains(ADMIN_CONTROLLER_PATTERN));
        assertTrue(adminController.contains(CLUBPOINTS_APP_CONTROLLER_PATTERN));
        assertTrue(adminController.contains(CLUBPOINTS_LEADER_CONTROLLER_PATTERN));
    }

    @Test
    void frontendRegressionPagesShouldUseImplementedActionShapesOnly() throws Exception {
        String appActivityApi = readFrontendFile("src/api/clubpoints/app/activity.ts");
        assertTrue(appActivityApi.contains("registrationId: data.id"));

        String leaderActivityApi = readFrontendFile("src/api/clubpoints/leader/activity.ts");
        assertTrue(leaderActivityApi.contains("withActivityDefaults"));
        assertTrue(leaderActivityApi.contains("registrationDeadline: data.registrationDeadline ?? data.startTime"));
        assertTrue(leaderActivityApi.contains("checkinStartTime: data.checkinStartTime ?? data.startTime"));
        assertTrue(leaderActivityApi.contains("checkoutEndTime: data.checkoutEndTime ?? data.endTime"));
        assertTrue(leaderActivityApi.contains("params: { id: data.id"));
        assertTrue(leaderActivityApi.contains("params: { id: data.id, reason: data.reason }"));
        assertTrue(leaderActivityApi.contains("export const submitLeaderActivity"));
        assertTrue(leaderActivityApi.contains("export const cancelLeaderActivity"));
        assertTrue(!leaderActivityApi.contains("withdrawLeaderActivity"));
        assertTrue(!leaderActivityApi.contains("deleteLeaderActivity"));

        String leaderContributionApi = readFrontendFile("src/api/clubpoints/leader/contribution.ts");
        assertTrue(leaderContributionApi.contains("params: { id: data.id, reason: data.reason }"));
        assertTrue(!leaderContributionApi.contains("deleteLeaderContribution"));

        String leaderActivityPage = readFrontendFile("src/views/clubpoints/leader/activity/index.vue");
        assertTrue(leaderActivityPage.contains("ensureDefaultClub"));
        assertTrue(!leaderActivityPage.contains("@click=\"withdrawActivity(row)\""));
        assertTrue(!leaderActivityPage.contains("@click=\"deleteActivity(row)\""));

        String leaderContributionPage = readFrontendFile("src/views/clubpoints/leader/contribution/index.vue");
        assertTrue(leaderContributionPage.contains("ensureDefaultClub"));
        assertTrue(!leaderContributionPage.contains("@click=\"deleteMaterial(row)\""));

        String adminActivityPage = readFrontendFile("src/views/clubpoints/admin/activity/index.vue");
        assertTrue(!adminActivityPage.contains("@click=\"deleteActivity(row)\""));

        String adminActivityApi = readFrontendFile("src/api/clubpoints/admin/activity.ts");
        assertTrue(adminActivityApi.contains("withActivityDefaults"));
        assertTrue(adminActivityApi.contains("registrationDeadline: data.registrationDeadline ?? data.startTime"));
        assertTrue(adminActivityApi.contains("checkinStartTime: data.checkinStartTime ?? data.startTime"));
        assertTrue(adminActivityApi.contains("checkoutEndTime: data.checkoutEndTime ?? data.endTime"));

        String adminContributionPage = readFrontendFile("src/views/clubpoints/admin/contribution-review/index.vue");
        assertTrue(!adminContributionPage.contains("@click=\"deleteMaterial(row)\""));
    }

    private static RequestMappingHandlerMapping buildMapping(WebProperties webProperties) {
        WebMvcRegistrations registrations = new YudaoWebAutoConfiguration().webMvcRegistrations(webProperties);
        return registrations.getRequestMappingHandlerMapping();
    }

    private static String firstMatchingPrefix(Map<String, Predicate<Class<?>>> pathPrefixes,
                                              Class<?> controllerClass) {
        for (Map.Entry<String, Predicate<Class<?>>> entry : pathPrefixes.entrySet()) {
            if (entry.getValue().test(controllerClass)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static String readConfiguredAdminApiController() throws IOException {
        String yaml = Files.readString(findRuoyiRoot()
                .resolve("yudao-server/src/main/resources/application.yaml"), StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile("(?m)^    admin-api:\\R      controller: ['\"]?([^'\"\\r\\n]+)['\"]?")
                .matcher(yaml);
        if (!matcher.find()) {
            throw new AssertionError("Cannot find yudao.web.admin-api.controller in application.yaml");
        }
        return matcher.group(1).trim();
    }

    private static String readFrontendFile(String relativePath) throws IOException {
        return Files.readString(findRuoyiRoot()
                .resolve("yudao-ui/yudao-ui-admin-vue3")
                .resolve(relativePath), StandardCharsets.UTF_8);
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
