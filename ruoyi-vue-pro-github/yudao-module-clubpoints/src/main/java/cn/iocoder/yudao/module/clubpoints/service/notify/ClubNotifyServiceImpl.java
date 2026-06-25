package cn.iocoder.yudao.module.clubpoints.service.notify;

import cn.iocoder.yudao.module.system.service.notify.NotifySendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.module.clubpoints.enums.ClubNotifyTemplateConstants.TEMPLATE_ACTIVITY_REVIEWED;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubNotifyTemplateConstants.TEMPLATE_DISPUTE_REPLIED;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubNotifyTemplateConstants.TEMPLATE_POINTS_CHANGED;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubNotifyTemplateConstants.TEMPLATE_REDEMPTION_REVIEWED;

/**
 * 俱乐部积分通知封装服务实现
 */
@Service
@Slf4j
public class ClubNotifyServiceImpl implements ClubNotifyService {

    @Resource
    private NotifySendService notifySendService;

    @Override
    public void notifyActivityReviewResult(Long userId, String activityTitle, String result, String reason) {
        Map<String, Object> params = new HashMap<>();
        params.put("activityTitle", activityTitle);
        params.put("result", result);
        params.put("reason", reason);
        sendSafely(userId, TEMPLATE_ACTIVITY_REVIEWED, params);
    }

    @Override
    public void notifyPointsChanged(Long userId, String reason, String direction, Integer points, Integer availablePoints) {
        Map<String, Object> params = new HashMap<>();
        params.put("reason", reason);
        params.put("direction", direction);
        params.put("points", points);
        params.put("availablePoints", availablePoints);
        sendSafely(userId, TEMPLATE_POINTS_CHANGED, params);
    }

    @Override
    public void notifyRedemptionReviewResult(Long userId, String applicationNo, String result, String reason) {
        Map<String, Object> params = new HashMap<>();
        params.put("applicationNo", applicationNo);
        params.put("result", result);
        params.put("reason", reason);
        sendSafely(userId, TEMPLATE_REDEMPTION_REVIEWED, params);
    }

    @Override
    public void notifyDisputeReplied(Long userId, String title, String replyContent) {
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("replyContent", replyContent);
        sendSafely(userId, TEMPLATE_DISPUTE_REPLIED, params);
    }

    private void sendSafely(Long userId, String templateCode, Map<String, Object> templateParams) {
        try {
            notifySendService.sendSingleNotifyToAdmin(userId, templateCode, templateParams);
        } catch (Exception ex) {
            log.warn("[sendSafely][userId({}) templateCode({}) 发送俱乐部积分站内信失败]", userId, templateCode, ex);
        }
    }

}
