package cn.iocoder.yudao.module.clubpoints.controller.leader.registration.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 负责人端特殊缺席请求
 */
@Data
@Accessors(chain = true)
public class LeaderSpecialAbsenceReqVO {

    @NotNull
    private Long id;

    private String reason;

}
