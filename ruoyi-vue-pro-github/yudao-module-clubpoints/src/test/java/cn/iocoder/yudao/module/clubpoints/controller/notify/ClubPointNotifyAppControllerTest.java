package cn.iocoder.yudao.module.clubpoints.controller.notify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.app.notify.ClubPointNotifyAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.notify.vo.AppNotifyMessagePageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.notify.vo.AppNotifyMessageRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import cn.iocoder.yudao.module.system.service.notify.NotifyMessageServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.MEMBER;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointNotifyAppController.class,
        NotifyMessageServiceImpl.class
})
class ClubPointNotifyAppControllerTest extends BaseDbUnitTest {

    @Resource
    private ClubPointNotifyAppController notifyAppController;
    @Resource
    private NotifyMessageMapper notifyMessageMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void myNotifyPageAndUpdateReadShouldUseLoginUserOnly() {
        login(100L, "员工A");
        Long ownUnreadId = insertNotifyMessage(100L, ADMIN.getValue(), "club_points_changed", false, "积分到账");
        Long ownReadId = insertNotifyMessage(100L, ADMIN.getValue(), "club_redemption_reviewed", true, "兑换通过");
        Long otherUserUnreadId = insertNotifyMessage(101L, ADMIN.getValue(), "club_points_changed", false, "别人通知");
        Long otherTypeUnreadId = insertNotifyMessage(100L, MEMBER.getValue(), "club_points_changed", false, "会员通知");

        AppNotifyMessagePageReqVO pageReqVO = new AppNotifyMessagePageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(10);
        pageReqVO.setReadStatus(false);
        PageResult<AppNotifyMessageRespVO> page = notifyAppController.getMyNotifyPage(pageReqVO).getCheckedData();

        assertEquals(1L, page.getTotal());
        assertEquals(ownUnreadId, page.getList().get(0).getId());
        assertEquals("积分到账", page.getList().get(0).getTemplateContent());
        assertFalse(page.getList().get(0).getReadStatus());

        Boolean updated = notifyAppController.updateNotifyRead(Arrays.asList(
                ownUnreadId, ownReadId, otherUserUnreadId, otherTypeUnreadId)).getCheckedData();

        assertTrue(updated);
        assertTrue(notifyMessageMapper.selectById(ownUnreadId).getReadStatus());
        assertNotNull(notifyMessageMapper.selectById(ownUnreadId).getReadTime());
        assertTrue(notifyMessageMapper.selectById(ownReadId).getReadStatus());
        assertFalse(notifyMessageMapper.selectById(otherUserUnreadId).getReadStatus());
        assertFalse(notifyMessageMapper.selectById(otherTypeUnreadId).getReadStatus());
    }

    @Test
    void endpointsShouldUseDocumentedNotifyPathsWithoutExtraPermission() throws Exception {
        RequestMapping requestMapping = ClubPointNotifyAppController.class.getAnnotation(RequestMapping.class);
        assertNotNull(requestMapping);
        assertEquals("/clubpoints/app/notify", requestMapping.value()[0]);

        assertGetMapping("getMyNotifyPage", new Class<?>[]{AppNotifyMessagePageReqVO.class}, "/my-page");
        assertPutMapping("updateNotifyRead", new Class<?>[]{java.util.List.class}, "/update-read");
    }

    private static void assertGetMapping(String methodName, Class<?>[] parameterTypes, String expectedPath)
            throws NoSuchMethodException {
        Method method = ClubPointNotifyAppController.class.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(GetMapping.class).value()[0]);
        assertFalse(method.isAnnotationPresent(PreAuthorize.class));
    }

    private static void assertPutMapping(String methodName, Class<?>[] parameterTypes, String expectedPath)
            throws NoSuchMethodException {
        Method method = ClubPointNotifyAppController.class.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(PutMapping.class).value()[0]);
        assertFalse(method.isAnnotationPresent(PreAuthorize.class));
    }

    private Long insertNotifyMessage(Long userId, Integer userType, String templateCode, Boolean readStatus,
                                     String content) {
        NotifyMessageDO message = new NotifyMessageDO();
        message.setUserId(userId);
        message.setUserType(userType);
        message.setTemplateId(1L);
        message.setTemplateCode(templateCode);
        message.setTemplateNickname("system");
        message.setTemplateContent(content);
        message.setTemplateType(2);
        message.setTemplateParams(Collections.singletonMap("content", content));
        message.setReadStatus(readStatus);
        message.setReadTime(Boolean.TRUE.equals(readStatus) ? LocalDateTime.now() : null);
        notifyMessageMapper.insert(message);
        return message.getId();
    }

    private static void login(Long userId, String nickname) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        java.util.Map<String, String> info = new java.util.HashMap<>();
        info.put(INFO_KEY_NICKNAME, nickname);
        SecurityFrameworkUtils.setLoginUser(new LoginUser()
                .setId(userId)
                .setUserType(ADMIN.getValue())
                .setInfo(info), request);
    }

}
