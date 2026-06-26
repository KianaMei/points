package cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdminAnnualClearRespVO {

    private Integer totalCount;
    private Integer successCount;
    private Integer skipCount;
    private Integer failedCount;

}
