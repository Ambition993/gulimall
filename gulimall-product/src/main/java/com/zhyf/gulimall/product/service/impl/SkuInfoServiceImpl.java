package com.zhyf.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.product.dao.SkuInfoDao;
import com.zhyf.gulimall.product.entity.SkuImagesEntity;
import com.zhyf.gulimall.product.entity.SkuInfoEntity;
import com.zhyf.gulimall.product.entity.SpuInfoDescEntity;
import com.zhyf.gulimall.product.feign.SecKillFeignService;
import com.zhyf.gulimall.product.service.*;
import com.zhyf.gulimall.product.vo.SeckillInfoVo;
import com.zhyf.gulimall.product.vo.SkuItemSaleAttrVo;
import com.zhyf.gulimall.product.vo.SkuItemVo;
import com.zhyf.gulimall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    SecKillFeignService secKillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByConditon(Map<String, Object> params) {
        /*
         * key
         * catelogId
         * brandId
         * min
         * max
         * */

        // SELECT COUNT(1) FROM pms_sku_info WHERE (catelog_id = ? AND brand_id = ? AND price >= ? AND price <= ?)

        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> w.eq("sku_id", key).or().like("sku_name", key));
        }
        // 三级id没选择不应该拼这个条件  没选应该查询所有
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            // gt : 大于;  ge: 大于等于
            wrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    // le: 小于等于
                    wrapper.le("price", max);
                }
            } catch (Exception e) {
                System.out.println("com.firenay.mall.product.service.impl.SkuInfoServiceImpl：前端传来非数字字符");
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo itemVo = new SkuItemVo();
        // sku基本信息获取 pms_sku_info
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = getById(skuId);
            itemVo.setInfo(info);
            return info;
        }, threadPoolExecutor);
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 销售属性
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            itemVo.setSaleAttr(saleAttrVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            // spu的介绍
            SpuInfoDescEntity spuDesc = spuInfoDescService.getById(res.getSpuId());
            itemVo.setDesp(spuDesc);
        }, threadPoolExecutor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 获取spu规格参数信息
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            itemVo.setGroupAttrs(attrGroupVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            // 图片信息         pms_sku_images
            List<SkuImagesEntity> images = imagesService.getSkuImgsBySkuId(skuId);
            itemVo.setImages(images);

        }, threadPoolExecutor);

        //  查询商品的秒杀信息
        CompletableFuture<Void> secKillFuture = CompletableFuture.runAsync(() -> {
            R r = secKillFeignService.getSkuSeckillInfo(skuId);
            log.info("secKillFeignService.getSkuSeckillInfo(skuId);");
            if (r.getCode() == 0) {
                SeckillInfoVo data = r.getData("data", new TypeReference<SeckillInfoVo>() {
                });
                itemVo.setSeckillInfo(data);
            }
        }, threadPoolExecutor);

        // 等待所有的任务完成
        CompletableFuture.allOf(saleAttrFuture, descFuture, imagesFuture, baseAttrFuture, secKillFuture).get();
        return itemVo;
    }


}