package cn.iocoder.yudao.module.clubpoints.controller.jobrun;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.jobrun.ClubJobRunAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.jobrun.vo.AdminJobRunHandleReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.jobrun.vo.AdminJobRunPageReqVO;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.ClubJobRunAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(ClubJobRunAdminController.class)
class ClubJobRunAdminControllerTest extends BaseDbUnitTest {

    private static final String JOB_QUERY_PERMISSION = "@ss.hasPermission('clubpoints:job:query')";
    private static final String JOB_HANDLE_PERMISSION = "@ss.hasPermission('clubpoints:job:handle')";

    @MockBean
    private ClubJobRunAdminService jobRunAdminService;

    @Test
    void endpointsShouldUseJobRunPathsAndPermissions() throws Exception {
        RequestMapping requestMapping = ClubJobRunAdminController.class.getAnnotation(RequestMapping.class);
        assertNotNull(requestMapping);
        assertEquals("/clubpoints/job-run", requestMapping.value()[0]);

        assertGetMapping("getJobRunPage", new Class<?>[]{AdminJobRunPageReqVO.class},
                "/page", JOB_QUERY_PERMISSION);
        assertGetMapping("getJobRunDetail", new Class<?>[]{Long.class},
                "/detail", JOB_QUERY_PERMISSION);
        assertPostMapping("handleJobRun", new Class<?>[]{AdminJobRunHandleReqVO.class},
                "/handle", JOB_HANDLE_PERMISSION);
    }

    private static void assertGetMapping(String methodName, Class<?>[] parameterTypes, String expectedPath,
                                         String expectedPermission) throws Exception {
        Method method = ClubJobRunAdminController.class.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(GetMapping.class).value()[0]);
        assertEquals(expectedPermission, method.getAnnotation(PreAuthorize.class).value());
    }

    private static void assertPostMapping(String methodName, Class<?>[] parameterTypes, String expectedPath,
                                          String expectedPermission) throws Exception {
        Method method = ClubJobRunAdminController.class.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(PostMapping.class).value()[0]);
        assertEquals(expectedPermission, method.getAnnotation(PreAuthorize.class).value());
    }

}
