package cn.iocoder.yudao.module.clubpoints.service.notify;

/**
 * 俱乐部积分通知封装服务
 */
public interface ClubNotifyService {

    void notifyActivityReviewResult(Long userId, String activityTitle, String result, String reason);

    void notifyPointsChanged(Long userId, String reason, String direction, Integer points, Integer availablePoints);

    void notifyRedemptionReviewResult(Long userId, String applicationNo, String result, String reason);

    void notifyDisputeReplied(Long userId, String title, String replyContent);

}
