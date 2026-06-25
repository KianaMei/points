package cn.iocoder.yudao.module.clubpoints.service.rule.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分规则版本保存参数
 */
@Data
@Accessors(chain = true)
public class ClubPointRuleVersionSaveReqBO {

    private String versionNo;
    private String name;
    private LocalDateTime publicityTime;
    private LocalDateTime effectiveTime;
    private String summary;
    private String content;
    private String attachmentSnapshotJson;
    private String remark;

}
