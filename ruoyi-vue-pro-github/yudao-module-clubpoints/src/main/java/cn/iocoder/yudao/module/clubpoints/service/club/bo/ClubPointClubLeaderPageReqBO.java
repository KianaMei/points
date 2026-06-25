package cn.iocoder.yudao.module.clubpoints.service.club.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 俱乐部负责人分页查询参数
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointClubLeaderPageReqBO extends PageParam {

    private Long clubId;
    private Long userId;
    private Integer status;

}
