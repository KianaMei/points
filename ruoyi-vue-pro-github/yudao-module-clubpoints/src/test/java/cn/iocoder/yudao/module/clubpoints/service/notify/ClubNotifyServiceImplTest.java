package cn.iocoder.yudao.module.clubpoints.service.notify;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.system.service.notify.NotifySendService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

import static cn.iocoder.yudao.module.clubpoints.enums.ClubNotifyTemplateConstants.TEMPLATE_ACTIVITY_REVIEWED;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubNotifyTemplateConstants.TEMPLATE_DISPUTE_REPLIED;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubNotifyTemplateConstants.TEMPLATE_POINTS_CHANGED;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubNotifyTemplateConstants.TEMPLATE_REDEMPTION_REVIEWED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@Import({ClubNotifyServiceImpl.class, ClubNotifyServiceImplTest.TransactionalProbeService.class})
class ClubNotifyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ClubNotifyService clubNotifyService;
    @Resource
    private ClubMemberMapper clubMemberMapper;
    @Resource
    private TransactionalProbeService transactionalProbeService;

    @MockBean
    private NotifySendService notifySendService;

    @Test
    void notifyActivityReviewResultShouldUseSystemNotifyTemplate() {
        clubNotifyService.notifyActivityReviewResult(100L, "羽毛球活动", "通过", "材料完整");

        Map<String, Object> params = captureParams(TEMPLATE_ACTIVITY_REVIEWED);
        assertEquals("羽毛球活动", params.get("activityTitle"));
        assertEquals("通过", params.get("result"));
        assertEquals("材料完整", params.get("reason"));
    }

    @Test
    void notifyPointsChangedShouldUseSystemNotifyTemplate() {
        clubNotifyService.notifyPointsChanged(100L, "活动结算", "+", 20, 120);

        Map<String, Object> params = captureParams(TEMPLATE_POINTS_CHANGED);
        assertEquals("活动结算", params.get("reason"));
        assertEquals("+", params.get("direction"));
        assertEquals(20, params.get("points"));
        assertEquals(120, params.get("availablePoints"));
    }

    @Test
    void notifyRedemptionReviewResultShouldUseSystemNotifyTemplate() {
        clubNotifyService.notifyRedemptionReviewResult(100L, "EX-20260625-001", "驳回", "库存不足");

        Map<String, Object> params = captureParams(TEMPLATE_REDEMPTION_REVIEWED);
        assertEquals("EX-20260625-001", params.get("applicationNo"));
        assertEquals("驳回", params.get("result"));
        assertEquals("库存不足", params.get("reason"));
    }

    @Test
    void notifyDisputeRepliedShouldUseSystemNotifyTemplate() {
        clubNotifyService.notifyDisputeReplied(100L, "积分少算", "已补发");

        Map<String, Object> params = captureParams(TEMPLATE_DISPUTE_REPLIED);
        assertEquals("积分少算", params.get("title"));
        assertEquals("已补发", params.get("replyContent"));
    }

    @Test
    void notifyFailureShouldNotRollbackBusinessTransaction() {
        doThrow(new IllegalStateException("notify down")).when(notifySendService)
                .sendSingleNotifyToAdmin(eq(100L), eq(TEMPLATE_POINTS_CHANGED), org.mockito.ArgumentMatchers.anyMap());

        transactionalProbeService.writeBusinessThenNotify(100L);

        assertEquals(1L, clubMemberMapper.selectCount(ClubMemberDO::getUserId, 100L));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> captureParams(String templateCode) {
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(notifySendService).sendSingleNotifyToAdmin(eq(100L), eq(templateCode), paramsCaptor.capture());
        assertTrue(paramsCaptor.getValue().size() > 0);
        return paramsCaptor.getValue();
    }

    @Service
    static class TransactionalProbeService {

        @Resource
        private ClubMemberMapper clubMemberMapper;
        @Resource
        private ClubNotifyService clubNotifyService;

        @Transactional(rollbackFor = Exception.class)
        public void writeBusinessThenNotify(Long userId) {
            clubMemberMapper.insert(new ClubMemberDO()
                    .setClubId(1L)
                    .setUserId(userId)
                    .setDeptIdSnapshot(10L)
                    .setUserNameSnapshot("员工" + userId)
                    .setDeptNameSnapshot("研发部")
                    .setMobileSnapshot("13800000000")
                    .setClubCodeSnapshot("CLUB-1")
                    .setClubNameSnapshot("俱乐部1")
                    .setStatus(1)
                    .setJoinTime(LocalDateTime.now())
                    .setActiveUniqueKey("1:" + userId));
            clubNotifyService.notifyPointsChanged(userId, "活动结算", "+", 20, 120);
        }

    }

}
