package cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 负责人端俱乐部成员响应
 */
@Data
@Accessors(chain = true)
public class LeaderClubMemberRespVO {

    private Long id;
    private Long clubId;
    private String clubCodeSnapshot;
    private String clubNameSnapshot;
    private Long userId;
    private String nickname;
    private Long deptId;
    private String deptName;
    private String mobile;
    private Integer status;
    private LocalDateTime joinedTime;
    private LocalDateTime leaveTime;
    private Integer leaveReasonType;
    private String leaveReason;
    private Boolean leader;
    private Integer availablePoints;

}
