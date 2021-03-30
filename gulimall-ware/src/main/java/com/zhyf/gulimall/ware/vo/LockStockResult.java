package com.zhyf.gulimall.ware.vo;

import lombok.Data;

@Data
public class LockStockResult {
    private Long skuId;  // skuid
    private Integer num; // 锁定数量
    private Boolean locked; // 是否成功
}
