package cn.iocoder.yudao.module.clubpoints.service.settlement.bo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClubPointSettlementManualRunReqBO {

    private Long activityId;
    private Boolean force;
    private String reason;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;

}
