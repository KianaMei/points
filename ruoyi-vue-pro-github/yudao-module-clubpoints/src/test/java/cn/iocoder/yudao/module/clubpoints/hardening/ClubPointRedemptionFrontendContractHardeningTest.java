package cn.iocoder.yudao.module.clubpoints.hardening;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointRedemptionFrontendContractHardeningTest {

    @Test
    void appRedemptionGiftPointsFieldShouldMatchBackendResponse() throws Exception {
        Path frontendRoot = findRuoyiRoot().resolve("yudao-ui/yudao-ui-admin-vue3");
        String appRedemptionApi = read(frontendRoot.resolve("src/api/clubpoints/app/redemption.ts"));
        String appRedemptionView = read(frontendRoot.resolve("src/views/clubpoints/app/redemption/index.vue"));

        assertTrue(appRedemptionApi.contains("pointsCost: number"),
                "App redemption gift API type must expose backend field pointsCost");
        assertFalse(appRedemptionApi.contains("pointPrice"),
                "App redemption gift API type must not invent pointPrice");
        assertTrue(appRedemptionView.contains("prop=\"pointsCost\""),
                "App redemption gift table must bind backend field pointsCost");
        assertTrue(appRedemptionView.contains(":value=\"row.pointsCost\""),
                "App redemption gift points display must read backend field pointsCost");
        assertFalse(appRedemptionView.contains("pointPrice"),
                "App redemption gift view must not read missing field pointPrice");
    }

    private static String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
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
