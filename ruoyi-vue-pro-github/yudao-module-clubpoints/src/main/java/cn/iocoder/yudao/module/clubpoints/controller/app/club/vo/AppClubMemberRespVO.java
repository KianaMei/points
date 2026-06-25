package cn.iocoder.yudao.module.clubpoints.controller.app.club.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AppClubMemberRespVO {

    private Long id;
    private Long clubId;
    private Long userId;
    private String nickname;
    private Long deptId;
    private String deptName;
    private String mobile;
    private Integer status;
    private LocalDateTime joinedTime;
    private Boolean leader;
    private Integer availablePoints;

}
