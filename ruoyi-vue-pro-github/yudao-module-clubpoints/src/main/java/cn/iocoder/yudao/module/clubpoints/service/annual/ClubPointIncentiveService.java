package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointIncentiveOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointIncentiveSuggestReqBO;

/**
 * 运营激励服务
 */
public interface ClubPointIncentiveService {

    int generateRankingIncentives(ClubPointIncentiveSuggestReqBO reqBO);

    void confirmIncentive(ClubPointIncentiveOperationReqBO reqBO);

    void cancelIncentive(ClubPointIncentiveOperationReqBO reqBO);

}
