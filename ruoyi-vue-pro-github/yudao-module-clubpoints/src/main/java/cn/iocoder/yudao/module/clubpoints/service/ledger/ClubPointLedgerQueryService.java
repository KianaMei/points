package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerMemberSummaryBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerSummaryBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerTransactionBO;

/**
 * 积分账本查询服务
 */
public interface ClubPointLedgerQueryService {

    ClubPointLedgerSummaryBO getAppSummary(Long loginUserId);

    PageResult<ClubPointLedgerTransactionBO> getAppTransactionPage(Long loginUserId, ClubPointLedgerPageReqBO reqBO);

    PageResult<ClubPointLedgerMemberSummaryBO> getLeaderMemberSummaryPage(Long loginUserId,
                                                                          ClubPointAccountPageReqBO reqBO);

    PageResult<ClubPointLedgerTransactionBO> getLeaderTransactionPage(Long loginUserId,
                                                                      ClubPointLedgerPageReqBO reqBO);

    PageResult<ClubPointAccountDO> getAdminAccountPage(ClubPointAccountPageReqBO reqBO);

    PageResult<ClubPointLedgerTransactionBO> getAdminTransactionPage(ClubPointLedgerPageReqBO reqBO);

}
