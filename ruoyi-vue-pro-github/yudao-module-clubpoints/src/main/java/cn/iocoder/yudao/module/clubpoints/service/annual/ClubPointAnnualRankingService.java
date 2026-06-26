package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualRankingGenerateReqBO;

/**
 * 俱乐部年度排名服务
 */
public interface ClubPointAnnualRankingService {

    void generateRanking(ClubPointAnnualRankingGenerateReqBO reqBO);

    PageResult<ClubPointAnnualRankingRecordDO> getRankingPage(PageParam pageParam, Integer year);

}
