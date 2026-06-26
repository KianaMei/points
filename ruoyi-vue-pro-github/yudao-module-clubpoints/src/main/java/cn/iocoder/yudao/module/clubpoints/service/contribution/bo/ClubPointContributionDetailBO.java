package cn.iocoder.yudao.module.clubpoints.service.contribution.bo;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 非签到积分材料详情
 */
@Data
@Accessors(chain = true)
public class ClubPointContributionDetailBO {

    private ClubPointContributionMaterialDO material;
    private List<ClubPointContributionItemDO> items;

}
