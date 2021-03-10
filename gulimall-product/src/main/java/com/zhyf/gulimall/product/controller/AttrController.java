package com.zhyf.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.zhyf.gulimall.product.entity.ProductAttrValueEntity;
import com.zhyf.gulimall.product.service.ProductAttrValueService;
import com.zhyf.gulimall.product.vo.AttrGroupRelationVo;
import com.zhyf.gulimall.product.vo.AttrRespVo;
import com.zhyf.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zhyf.gulimall.product.service.AttrService;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.R;


/**
 * 商品属性
 *
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 12:04:22
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrList(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrListforSpu(spuId);
        return R.ok().put("data", entities);
    }

    /**
     * @param params
     * @param catelogId
     * @param type
     * @return
     */
    @RequestMapping("/{attrType}/list/{catelogId}")
    public R baseList(@RequestParam Map<String, Object> params,
                      @PathVariable("catelogId") Long catelogId,
                      @PathVariable("attrType") String type) {
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, type);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
//   @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
//   @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {
//        AttrEntity attr = attrService.getById(attrId);
        AttrRespVo vo = attrService.getDetailInfo(attrId);
        return R.ok().put("attr", vo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//   @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attrVo) {
        attrService.updateAttr(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update/{spuId}")
//    @RequiresPermissions("product:attr:update")
    public R update(@PathVariable("spuId") Long spuId,
                    @RequestBody List<ProductAttrValueEntity> entities) {
        productAttrValueService.update(spuId,entities);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//  @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }


}
