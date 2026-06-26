package cn.iocoder.yudao.module.clubpoints.service.contribution;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionMaterialSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionSubmitReqBO;

import java.util.List;

/**
 * 非签到积分材料服务
 */
public interface ClubPointContributionService {

    Long createDraft(ClubPointContributionMaterialSaveReqBO reqBO);

    void updateDraft(ClubPointContributionMaterialSaveReqBO reqBO);

    void submitForReview(ClubPointContributionSubmitReqBO reqBO);

    List<ClubPointContributionMaterialDO> listPendingReviewMaterials(boolean operatorGlobalScope);

    void reviewMaterial(ClubPointContributionReviewReqBO reqBO);

}
