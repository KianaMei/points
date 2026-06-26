package cn.iocoder.yudao.module.clubpoints.service.annual.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 生成排名激励建议请求
 */
@Data
@Accessors(chain = true)
public class ClubPointIncentiveSuggestReqBO {

    private Integer year;
    private Boolean operatorGlobalScope;

}
