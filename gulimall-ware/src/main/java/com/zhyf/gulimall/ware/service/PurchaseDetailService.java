package com.zhyf.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.gulimall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 13:25:56
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

