package cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminClubLeaderRespVO {

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
