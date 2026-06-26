package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualRankingGenerateReqBO;

/**
 * 俱乐部年度排名服务
 */
public interface ClubPointAnnualRankingService {

    void generateRanking(ClubPointAnnualRankingGenerateReqBO reqBO);

}
