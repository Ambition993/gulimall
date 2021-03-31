package com.zhyf.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.exception.NoStockException;
import com.zhyf.common.to.mq.StockDetailTo;
import com.zhyf.common.to.mq.StockLockedTo;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.ware.dao.WareSkuDao;
import com.zhyf.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.zhyf.gulimall.ware.entity.WareOrderTaskEntity;
import com.zhyf.gulimall.ware.entity.WareSkuEntity;
import com.zhyf.gulimall.ware.feign.OrderFeignService;
import com.zhyf.gulimall.ware.feign.ProductFeignService;
import com.zhyf.gulimall.ware.service.WareOrderTaskDetailService;
import com.zhyf.gulimall.ware.service.WareOrderTaskService;
import com.zhyf.gulimall.ware.service.WareSkuService;
import com.zhyf.gulimall.ware.vo.OrderItemVo;
import com.zhyf.gulimall.ware.vo.OrderVo;
import com.zhyf.gulimall.ware.vo.SkuHasStockVo;
import com.zhyf.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService orderTaskService;

    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        /**
         * skuId
         * wareId
         */
        //  SELECT id,sku_id,ware_id,stock,sku_name,stock_locked FROM wms_ware_sku WHERE (sku_id = ? AND ware_id = ?)
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 如果没这个库存记录就新增
        List<WareSkuEntity> entityList = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entityList == null || entityList.size() == 0) {
            // 添加
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                // 不做处理
            }
            wareSkuDao.insert(wareSkuEntity);

        } else {
            // 更新
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            // 查询当前sku的总库存量
            //SELECT SUM (stock - stock_locked) FROM `wms_ware_sku` WHERE sku_id = 1
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为某个订单锁定库存
     *
     * @param wareSkuLockVo
     * @return 库存解锁的场景）
     * 1下订单成功 订单过期没支付被系统自动取消   用户手动取消了
     * 2下单成功 库存锁成功了 由于网络IO问题 导致订单回滚 之前锁定的库存必须解锁  希望一段时间后自动解锁
     */
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public Boolean orderLockStockWareSkuLockVo(WareSkuLockVo wareSkuLockVo) {
        /**
         * 保存库存工作单的详情
         * 追溯
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
        orderTaskService.save(taskEntity);
        // 1 按照下单的收货地址 找到一个就近的仓库 锁库存
        // 2 找到每个商品在那个仓库有库存
        List<OrderItemVo> locks = wareSkuLockVo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());
            List<Long> wareId = wareSkuDao.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareId);
            return skuWareHasStock;
        }).collect(Collectors.toList());
        // 2 锁定库存
        Boolean allLocked = true;
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                // 没有任何仓库是有库存的
                throw new NoStockException(skuId);
            }
            // 每个商品都锁成功了 将当前商品锁定了几件的工作单记录发给mq
            // 锁定失败了 前面保存的工作单信息就回滚了 发送出去的消息即使要解锁记录 由于去数据库查不到ID所以就不用解锁
            //
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    // 先保存工作单
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(taskDetailEntity);
                    // TODO 告诉mq 锁定成功
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo detail = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity, detail);
                    // 只发ID不行 防止回滚后找不到数据
                    stockLockedTo.setDetail(detail);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;   // 锁成功了 跳出
                } else {
                    // 当前仓库锁失败了
                }
            }
            if (skuStocked == false) {
                // 当前商品所有仓库都没锁住
                throw new NoStockException(skuId);
            }
        }
        // 走到这里肯定都是锁成功了
        return true;
    }

    @Override
    public void unLockStock(StockLockedTo to) {
        /**
         *             // 解锁
         *             // 查询数据库 关于这个订单的锁定库存信息
         *             // 有 需要解锁 证明了库存是锁定成功了
         *             //          解锁 看订单情况
         *             // 1没有这个订单必须解锁
         *             // 2有这个订单
         *             // 订单状态 ： 已取消 解锁
         *             //           ，没取消 不解锁
         *             // 没有 库存锁定失败了 库存回滚了  这种情况无需解锁
         *             // 一旦解锁失败 一定要告诉服务器
         */


        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailId);
        if (byId != null) {
            // 解锁
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn(); // 根据订单号来查询订单号的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                OrderVo data = r.getData("data", new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    // 这个订单已经取消了 可以解锁库存 或者订单不存在
                    unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                }
            } else {
                // 远程服务失败
                throw new RuntimeException("远程服务失败");
            }
        } else {
            // 无需解锁
        }

    }


    private void unLockStock(Long skuId, Long wareId, Integer skuNum, Long detailId) {
        wareSkuDao.unLockStock(skuId, wareId, skuNum);
    }


    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private List<Long> wareId;
        private Integer num;
    }
}