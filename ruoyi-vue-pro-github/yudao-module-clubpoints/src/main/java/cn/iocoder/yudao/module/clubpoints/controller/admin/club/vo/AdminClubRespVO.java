package cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class AdminClubRespVO {

    private Long id;
    private String code;
    private String name;
    private Integer status;
    private String description;
    private String contactText;
    private Long coverFileId;
    private Integer sort;
    private LocalDateTime disabledTime;
    private String disabledReason;
    private String remark;
    private Integer memberCount;
    private Integer leaderCount;
    private List<String> leaderNames;

}
