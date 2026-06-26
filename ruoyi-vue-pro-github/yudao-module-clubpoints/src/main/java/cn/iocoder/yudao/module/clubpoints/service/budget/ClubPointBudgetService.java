package cn.iocoder.yudao.module.clubpoints.service.budget;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.budget.ClubPointBudgetRecordDO;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetQueryReqBO;
import cn.iocoder.yudao.module.clubpoints.service.budget.bo.ClubPointBudgetSaveReqBO;

import java.util.List;

/**
 * 预算和经费记录服务
 */
public interface ClubPointBudgetService {

    Long createBudget(ClubPointBudgetSaveReqBO reqBO);

    void updateBudget(ClubPointBudgetSaveReqBO reqBO);

    void disableBudget(ClubPointBudgetOperationReqBO reqBO);

    List<ClubPointBudgetRecordDO> listBudgetRecords(ClubPointBudgetQueryReqBO reqBO);

}
