package cn.iocoder.yudao.module.clubpoints.service.annual.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 俱乐部年度排名生成请求
 */
@Data
@Accessors(chain = true)
public class ClubPointAnnualRankingGenerateReqBO {

    private Integer year;
    private LocalDateTime generatedTime;

}
