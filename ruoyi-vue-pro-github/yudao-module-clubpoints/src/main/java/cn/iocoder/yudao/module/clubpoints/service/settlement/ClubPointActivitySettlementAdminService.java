package cn.iocoder.yudao.module.clubpoints.service.settlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement.ClubPointActivitySettlementRunDO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementDetailBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementManualRunReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementPendingActivityPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementRunPageReqBO;

public interface ClubPointActivitySettlementAdminService {

    PageResult<ClubPointActivityDO> getPendingActivityPage(ClubPointSettlementPendingActivityPageReqBO reqBO);

    String runSettlement(ClubPointSettlementManualRunReqBO reqBO) throws Exception;

    PageResult<ClubPointActivitySettlementRunDO> getRunPage(ClubPointSettlementRunPageReqBO reqBO);

    ClubPointSettlementDetailBO getDetail(Long id);

}
