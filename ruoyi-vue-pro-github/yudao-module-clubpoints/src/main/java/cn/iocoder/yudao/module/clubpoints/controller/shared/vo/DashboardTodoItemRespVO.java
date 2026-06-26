package cn.iocoder.yudao.module.clubpoints.controller.shared.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "俱乐部积分 - 工作台待办项 Response VO")
@Data
@Accessors(chain = true)
public class DashboardTodoItemRespVO {

    private String code;
    private String name;
    private Integer count;
    private String path;
    private String queryJson;

}
