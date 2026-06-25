package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivitySaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivitySubmitReqBO;

/**
 * 活动管理服务
 */
public interface ClubPointActivityService {

    Long createDraft(ClubPointActivitySaveReqBO reqBO);

    void submitForReview(ClubPointActivitySubmitReqBO reqBO);

    void approveReview(ClubPointActivityReviewReqBO reqBO);

    void rejectReview(ClubPointActivityReviewReqBO reqBO);

    void updateActivity(ClubPointActivitySaveReqBO reqBO);

    void cancelActivity(ClubPointActivityCancelReqBO reqBO);

}
