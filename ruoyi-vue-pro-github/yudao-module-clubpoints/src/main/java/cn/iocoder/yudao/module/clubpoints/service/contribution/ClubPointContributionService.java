package cn.iocoder.yudao.module.clubpoints.service.contribution;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionDetailBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionDirectCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionFraudHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionMaterialSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionSubmitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionViolationDeductReqBO;
import cn.iocoder.yudao.module.clubpoints.service.contribution.bo.ClubPointContributionWithdrawReqBO;

import java.util.List;

/**
 * 非签到积分材料服务
 */
public interface ClubPointContributionService {

    Long createDraft(ClubPointContributionMaterialSaveReqBO reqBO);

    void updateDraft(ClubPointContributionMaterialSaveReqBO reqBO);

    void submitForReview(ClubPointContributionSubmitReqBO reqBO);

    void withdraw(ClubPointContributionWithdrawReqBO reqBO);

    PageResult<ClubPointContributionMaterialDO> getLeaderMaterialPage(Long operatorUserId,
                                                                      ClubPointContributionPageReqBO reqBO);

    ClubPointContributionDetailBO getLeaderMaterial(Long operatorUserId, Long id);

    List<ClubPointContributionMaterialDO> listPendingReviewMaterials(boolean operatorGlobalScope);

    PageResult<ClubPointContributionMaterialDO> getAdminReviewPage(boolean operatorGlobalScope,
                                                                   ClubPointContributionPageReqBO reqBO);

    ClubPointContributionDetailBO getAdminMaterial(boolean operatorGlobalScope, Long id);

    void reviewMaterial(ClubPointContributionReviewReqBO reqBO);

    Long directCreate(ClubPointContributionDirectCreateReqBO reqBO);

    Long violationDeduct(ClubPointContributionViolationDeductReqBO reqBO);

    Long handleFraud(ClubPointContributionFraudHandleReqBO reqBO);

}
