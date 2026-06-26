package cn.iocoder.yudao.module.clubpoints.service.jobrun.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClubJobRunPageReqBO extends PageParam {

    private String taskType;
    private String bizType;
    private Long bizId;
    private String runKey;
    private Integer status;
    private Integer triggerSource;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
