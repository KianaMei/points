package cn.iocoder.yudao.module.clubpoints.service.club.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 俱乐部负责人查询结果
 */
@Data
@Accessors(chain = true)
public class ClubPointClubLeaderBO {

    private Long id;
    private Long clubId;
    private Long userId;
    private String userNameSnapshot;
    private String clubNameSnapshot;
    private Integer status;
    private LocalDateTime assignedTime;
    private LocalDateTime removedTime;
    private Long assignedBy;
    private Long removedBy;
    private String reason;

}
