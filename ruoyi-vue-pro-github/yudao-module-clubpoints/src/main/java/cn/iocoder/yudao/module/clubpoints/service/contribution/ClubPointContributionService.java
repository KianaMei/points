package cn.iocoder.yudao.module.clubpoints.service.contribution;

import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionMaterialSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionSubmitReqBO;

/**
 * 非签到积分材料服务
 */
public interface ClubPointContributionService {

    Long createDraft(ClubPointContributionMaterialSaveReqBO reqBO);

    void updateDraft(ClubPointContributionMaterialSaveReqBO reqBO);

    void submitForReview(ClubPointContributionSubmitReqBO reqBO);

}
