package com.zhyf.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.to.SkuReductionTo;
import com.zhyf.common.to.SpuBoundsTo;
import com.zhyf.common.to.es.SkuEsModel;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.product.dao.SpuInfoDao;
import com.zhyf.gulimall.product.entity.*;
import com.zhyf.gulimall.product.feign.CouponFeignService;
import com.zhyf.gulimall.product.feign.WareFeignService;
import com.zhyf.gulimall.product.service.*;
import com.zhyf.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1 spu基本信息的保存  pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);
        // 2 保存spu描述的图片  pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);
        // 3 图片集   pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(images, spuInfoEntity.getId());
        // 4 保存spu的规格参数  pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            // attrName 字段
            AttrEntity byId = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(byId.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(spuInfoEntity.getId());
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);
        // 保存spu的积分信息  gulimall_sms -> sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundsTo boundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, boundsTo);
        boundsTo.setSpuId(spuInfoEntity.getId());
        // 远程调用了coupon服务的方法来保存spubounds
        R r = couponFeignService.saveSpuBounds(boundsTo);
        if (r.getCode() != 0) {
            log.error("remote save spuBounds info error!!!");
        }


        // 5 保存当前spu 对应的所有sku信息
        List<Skus> skus = vo.getSkus();
        // 5.1 sku 基本信息 pms_sku_info
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                // copy 一些属性 skuName  price skuTitle skuSubtitle
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                // 设置默认图片
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> skuImagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    // 返回true就是需要他 false 就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                // 5.2 sku 图片信息  pms_sku_images
                //TODO 没有图片路径的就不保存了
                skuImagesService.saveBatch(skuImagesEntities);
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> attrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());
                // 5.3 sku 销售属性信息 pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(attrValueEntities);
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    // 5.4 sku 优惠满减等等信息  gulimall_sms-> 一些表  sms_sku_ladder  sms_sku_full_reduction sms_member_price
                    R res = couponFeignService.saveSpuReduction(skuReductionTo);
                    if (res.getCode() != 0) {
                        log.error("remote save sku coupon  info error!!!");
                    }
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        // 根据 spu管理带来的条件进行叠加模糊查询
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> w.eq("id", key).or().like("spu_name", key));
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        List<SkuEsModel> upProducts = new ArrayList<>();


        // 1 查找当前spuid 对应的所有sku信息 品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //todo  当前sku所有的可以的被检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListforSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        List<Long> searchAttrs = attrService.selectSearchAttrs(attrIds);

        List<SkuEsModel.Attr> attrs = new ArrayList<>();
        Set<Long> idSet = new HashSet<>(searchAttrs);
        List<SkuEsModel.Attr> attrList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attr attrInES = new SkuEsModel.Attr();
            BeanUtils.copyProperties(item, attrInES);
            return attrInES;
        }).collect(Collectors.toList());
        R<List<SkuHasStockVo>> skusHasStock = wareFeignService.getSkusHasStock(skuIdList);
        Map<Long, Boolean> booleanMap = skusHasStock.getData().stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));

        // 2 封装每个sku 的信息
        List<SkuEsModel> collect = skus.stream().map(item -> {
            // 组装数据
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(item, skuEsModel);
            // skuPrice skuImg hasStock hotScore  List<Attr> 要自己来设置
            skuEsModel.setSkuPrice(item.getPrice());
            skuEsModel.setSkuImg(item.getSkuDefaultImg());
            //todo 发送远程调用查询是否有库存 上面已经提前一次查好  避免网络io延迟
            //todo 热度评分 0 1 2
            skuEsModel.setHotScore(0L);
            //todo 品牌和分类的名字和信息
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            CategoryEntity category = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(category.getName());
            skuEsModel.setAttrs(attrList);
            return skuEsModel;
        }).collect(Collectors.toList());
        //todo 将数据发送给es来保存 gulimall-search

    }

}