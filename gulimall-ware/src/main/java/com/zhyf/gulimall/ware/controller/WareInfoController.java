package com.zhyf.gulimall.ware.controller;

import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.R;
import com.zhyf.gulimall.ware.entity.WareInfoEntity;
import com.zhyf.gulimall.ware.service.WareInfoService;
import com.zhyf.gulimall.ware.vo.FareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 * 仓库信息
 *
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 13:25:56
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {
    @Autowired
    private WareInfoService wareInfoService;

    @GetMapping("/fare")
    @ResponseBody
    public FareVo getFare(@RequestParam("addrId") Long addrId) {
        return wareInfoService.getFare(addrId);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
//   @RequiresPermissions("product:wareinfo:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//   @RequiresPermissions("product:wareinfo:info")
    public R info(@PathVariable("id") Long id) {
        WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//   @RequiresPermissions("product:wareinfo:save")
    public R save(@RequestBody WareInfoEntity wareInfo) {
        wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:wareinfo:update")
    public R update(@RequestBody WareInfoEntity wareInfo) {
        wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//  @RequiresPermissions("product:wareinfo:delete")
    public R delete(@RequestBody Long[] ids) {
        wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
