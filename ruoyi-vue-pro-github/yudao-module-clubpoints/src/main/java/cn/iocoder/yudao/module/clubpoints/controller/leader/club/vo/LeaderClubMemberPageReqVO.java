package cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 负责人端俱乐部成员分页请求
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LeaderClubMemberPageReqVO extends PageParam {

    @NotNull
    private Long clubId;

    private Long userId;

}
