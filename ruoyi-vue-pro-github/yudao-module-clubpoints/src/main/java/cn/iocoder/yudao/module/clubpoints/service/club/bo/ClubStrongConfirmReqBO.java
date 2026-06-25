package cn.iocoder.yudao.module.clubpoints.service.club.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 强确认参数
 */
@Data
@Accessors(chain = true)
public class ClubStrongConfirmReqBO {

    private String confirmText;
    private LocalDateTime confirmedAt;

}
