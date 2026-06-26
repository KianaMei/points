package cn.iocoder.yudao.module.clubpoints.service.annual.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 年度清零结果
 */
@Data
@Accessors(chain = true)
public class ClubPointAnnualClearResultBO {

    private Integer totalCount;
    private Integer successCount;
    private Integer skipCount;
    private Integer failedCount;

}
