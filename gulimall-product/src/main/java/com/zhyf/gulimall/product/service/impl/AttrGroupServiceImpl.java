package com.zhyf.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;
import com.zhyf.gulimall.product.dao.AttrGroupDao;
import com.zhyf.gulimall.product.entity.AttrEntity;
import com.zhyf.gulimall.product.entity.AttrGroupEntity;
import com.zhyf.gulimall.product.service.AttrGroupService;
import com.zhyf.gulimall.product.service.AttrService;
import com.zhyf.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.zhyf.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        Object key = params.get("key");
        // select * from attr_group where catelog_id = ? and ( attr_group_id = key or attr_group_name like %key%)
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        // 没有选中三级分类就设置为0
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper);
            return new PageUtils(page);
        } else {
            queryWrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
        }

    }

    /*
     *  根据分类ID查出所有的属性分组以及这些属性（规格参数）
     * */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 查找分组信息
        // 属性分组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 查血所有的属性
        List<AttrGroupWithAttrsVo> collect = groupEntities.stream().map((item) -> {
            AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(attrsVo.getAttrGroupId());
            attrsVo.setAttrs(attrs);
            return attrsVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        // 1查出当前spu对应的所有属性的分组信息以及当前分组下的所有属性的对应的值
        // 1.1 当前spu有哪些属性分组     select ag.`attr_group_name` ,
        //                           ag.`attr_group_id` ,
        //                           aar.`attr_id`
        //                           from `pms_attr_group`  ag
        //                           left join `pms_attr_attrgroup_relation` aar on aar.`attr_group_id` = ag.`attr_group_id`
        // where catelog_id = 225
        AttrGroupDao baseMapper = this.baseMapper;
        return baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
    }
}