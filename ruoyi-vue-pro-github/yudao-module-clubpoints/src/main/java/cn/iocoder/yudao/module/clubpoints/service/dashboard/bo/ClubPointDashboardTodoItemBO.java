package cn.iocoder.yudao.module.clubpoints.service.dashboard.bo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClubPointDashboardTodoItemBO {

    private String code;
    private String name;
    private Integer count;
    private String path;
    private String queryJson;

}
