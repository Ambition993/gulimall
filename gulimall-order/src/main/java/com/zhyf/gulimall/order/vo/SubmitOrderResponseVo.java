package com.zhyf.gulimall.order.vo;

import com.zhyf.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code; //状态吗 0成功
}
