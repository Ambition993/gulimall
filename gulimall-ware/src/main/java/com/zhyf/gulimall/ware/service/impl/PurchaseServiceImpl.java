package com.zhyf.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.constant.WareConstant;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;
import com.zhyf.gulimall.ware.dao.PurchaseDao;
import com.zhyf.gulimall.ware.entity.PurchaseDetailEntity;
import com.zhyf.gulimall.ware.entity.PurchaseEntity;
import com.zhyf.gulimall.ware.service.PurchaseDetailService;
import com.zhyf.gulimall.ware.service.PurchaseService;
import com.zhyf.gulimall.ware.service.WareSkuService;
import com.zhyf.gulimall.ware.vo.MergeVo;
import com.zhyf.gulimall.ware.vo.PurchaseDoneVo;
import com.zhyf.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    PurchaseDetailService detailService;
    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );
        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        // 如果采购id为null 说明没选采购单
        if(purchaseId == null){
            // 新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        // 合并采购单 [其实就是修改上面创建的采购单]
        List<Long> items = mergeVo.getItems();

        // 从数据库查询所有要合并的采购单并过滤所有大于 [已分配] 状态的订单
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        List<PurchaseDetailEntity> detailEntities = detailService.getBaseMapper().selectBatchIds(items).stream().filter(entity -> {
            // 如果正在合并采购异常的项就把这个采购项之前所在的采购单的状态 wms_purchase 表的状态修改为 已分配
            if(entity.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
                purchaseEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                purchaseEntity.setId(entity.getPurchaseId());
                this.updateById(purchaseEntity);
            }
            return entity.getStatus() < WareConstant.PurchaseDetailStatusEnum.BUYING.getCode() || entity.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode();
        }).collect(Collectors.toList());
        // 将符合条件的id集合重新赋值给 items
        items = detailEntities.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        if(items == null || items.size() == 0){
            return;
        }
        // 设置仓库id
        purchaseEntity.setWareId(detailEntities.get(0).getWareId());
        Long finalPurchaseId = purchaseId;
        // 给采购单设置各种属性
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setId(item);
            entity.setPurchaseId(finalPurchaseId);
            entity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return entity;
        }).collect(Collectors.toList());

        // 每次更新完就更新时间
        detailService.updateBatchById(collect);
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Override
    /**
     *  ids 采购单的ID
     */
    public void received(List<Long> ids) {
        if(ids == null || ids.size() == 0){
            return;
        }
        // 1.确认当前采购单是已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> this.getById(id)
                // 只能采购已分配的
        ).filter(item -> item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode() || item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode())
                .map(item -> {
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    item.setUpdateTime(new Date());
                    return item;
                }).collect(Collectors.toList());
        // 2.被领取之后重新设置采购状态
        this.updateBatchById(collect);

        // 3.改变采购项状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());

            // 收集所有需要更新的采购单id
            List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(entity.getId());
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            // 根据id  批量更新
            detailService.updateBatchById(detailEntities);
        });
    }


    /**
     * {
     * 	"id":"1",
     * 	"items":[
     *                {"itemId":1,"status":3,"reason":""},
     *        {"itemId":3,"status":4,"reason":"无货"}
     * 	]
     * }
     *
     * id：		采购单id
     * items：	采购项
     * itemId：	采购需求id
     * status：	采购状态
     */
    @Override
    public void done(PurchaseDoneVo doneVo) {
        // 1 改变采购项的状态
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        Long id = doneVo.getId();
        Boolean flag = true;
        for (PurchaseItemDoneVo item : items) { // 遍历采购项
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
            }else {
                //增加库存
                PurchaseDetailEntity byId = detailService.getById(item.getItemId());
                wareSkuService.addStock(byId.getSkuId(), byId.getWareId(), byId.getSkuNum());
            }
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item.getItemId());
            detailEntity.setStatus(item.getStatus());
            updates.add(detailEntity);
        }
        detailService.updateBatchById(updates);

        // 1 改变采购单的状态

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }
}