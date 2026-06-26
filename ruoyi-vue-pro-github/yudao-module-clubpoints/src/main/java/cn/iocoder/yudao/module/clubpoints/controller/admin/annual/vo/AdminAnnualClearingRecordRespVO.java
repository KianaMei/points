package cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminAnnualClearingRecordRespVO {

    private Long id;
    private Integer year;
    private Long userId;
    private Integer netPointsBefore;
    private Integer frozenPointsBefore;
    private Integer availablePointsBefore;
    private Integer clearablePoints;
    private Long clearTransactionId;
    private Integer status;
    private Long runId;
    private LocalDateTime clearTime;
    private String errorMessage;

}
