package cn.iocoder.yudao.module.clubpoints.controller.app.notify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "员工端 - 我的通知 Response VO")
@Data
@Accessors(chain = true)
public class AppNotifyMessageRespVO {

    private Long id;
    private String templateCode;
    private String templateNickname;
    private String templateContent;
    private Integer templateType;
    private Boolean readStatus;
    private LocalDateTime readTime;
    private LocalDateTime createTime;

}
