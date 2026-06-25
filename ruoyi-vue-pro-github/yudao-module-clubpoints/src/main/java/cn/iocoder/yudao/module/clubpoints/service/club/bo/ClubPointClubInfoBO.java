package cn.iocoder.yudao.module.clubpoints.service.club.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 俱乐部查询结果
 */
@Data
@Accessors(chain = true)
public class ClubPointClubInfoBO {

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
    private Boolean joined;
    private LocalDateTime joinedTime;

}
