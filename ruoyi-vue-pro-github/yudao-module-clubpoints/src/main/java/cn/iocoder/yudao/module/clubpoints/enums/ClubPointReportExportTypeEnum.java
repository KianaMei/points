package cn.iocoder.yudao.module.clubpoints.enums;

import java.util.Arrays;

/**
 * 俱乐部积分报表导出类型。
 */
public enum ClubPointReportExportTypeEnum {

    POINT_DETAIL(1, "积分明细报表", "积分明细"),
    REDEMPTION(2, "兑换记录报表", "兑换记录"),
    LEDGER_SUMMARY(3, "积分总台账报表", "总台账"),
    CLUB_RANKING(4, "俱乐部发放积分排名报表", "俱乐部排名"),
    BUDGET(5, "预算和经费统计报表", "预算统计");

    private final Integer type;
    private final String reportName;
    private final String sheetName;

    ClubPointReportExportTypeEnum(Integer type, String reportName, String sheetName) {
        this.type = type;
        this.reportName = reportName;
        this.sheetName = sheetName;
    }

    public Integer getType() {
        return type;
    }

    public String getReportName() {
        return reportName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public static ClubPointReportExportTypeEnum typeOf(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

}
