package cn.iocoder.yudao.module.clubpoints.service.contribution.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 非签到积分材料撤回请求
 */
@Data
@Accessors(chain = true)
public class ClubPointContributionWithdrawReqBO {

    private Long id;
    private Long operatorUserId;
    private String reason;

}
