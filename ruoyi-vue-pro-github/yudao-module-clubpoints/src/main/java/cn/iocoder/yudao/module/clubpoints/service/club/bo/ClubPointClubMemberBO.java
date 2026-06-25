package cn.iocoder.yudao.module.clubpoints.service.club.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 俱乐部成员查询结果
 */
@Data
@Accessors(chain = true)
public class ClubPointClubMemberBO {

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
