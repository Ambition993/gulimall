package com.zhyf.gulimall.ware.service.impl;

import com.sun.corba.se.spi.orbutil.threadpool.WorkQueue;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;

import com.zhyf.gulimall.ware.dao.PurchaseDetailDao;
import com.zhyf.gulimall.ware.entity.PurchaseDetailEntity;
import com.zhyf.gulimall.ware.service.PurchaseDetailService;
import org.springframework.util.StringUtils;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    /**
     * @param params
     * @return PageUtils
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        /**
         * 采购需求模糊查询
         * status: 0
         * wareId: 1
         */
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            // 连续拼接
            wrapper.and(w -> w.eq("purchase_id", key).or().eq("sku_id", key));
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        List<PurchaseDetailEntity> purchaseId = this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
        return purchaseId;
    }

}