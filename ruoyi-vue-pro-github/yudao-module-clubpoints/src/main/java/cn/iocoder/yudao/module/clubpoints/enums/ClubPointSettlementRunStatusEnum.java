package cn.iocoder.yudao.module.clubpoints.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 结算运行状态枚举，复用业务任务状态字典。
 */
public enum ClubPointSettlementRunStatusEnum implements ArrayValuable<Integer> {

    PENDING(1, "待运行"),
    RUNNING(2, "运行中"),
    SUCCESS(3, "成功"),
    RETRYABLE_FAILED(4, "可重试失败"),
    FINAL_FAILED(5, "最终失败"),
    MANUAL_HANDLING(6, "人工处理中"),
    CLOSED(7, "已关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ClubPointSettlementRunStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    ClubPointSettlementRunStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
