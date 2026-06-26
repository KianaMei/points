package cn.iocoder.yudao.module.clubpoints.service.report.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 报表导出结果。
 */
@Data
@Accessors(chain = true)
public class ClubPointReportExportResultBO {

    private Integer reportType;
    private String reportName;
    private String sheetName;
    private List<?> rows;

}
