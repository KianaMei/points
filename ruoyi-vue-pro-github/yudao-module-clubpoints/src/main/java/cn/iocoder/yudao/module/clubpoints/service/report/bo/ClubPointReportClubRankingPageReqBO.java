package cn.iocoder.yudao.module.clubpoints.service.report.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointReportClubRankingPageReqBO extends PageParam {

    private Integer year;
    private Long clubId;

}
