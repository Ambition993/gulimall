package com.zhyf.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhyf.common.constant.ProductConstant;
import com.zhyf.common.utils.PageUtils;
import com.zhyf.common.utils.Query;
import com.zhyf.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.zhyf.gulimall.product.dao.AttrDao;
import com.zhyf.gulimall.product.dao.AttrGroupDao;
import com.zhyf.gulimall.product.dao.CategoryDao;
import com.zhyf.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zhyf.gulimall.product.entity.AttrEntity;
import com.zhyf.gulimall.product.entity.AttrGroupEntity;
import com.zhyf.gulimall.product.entity.CategoryEntity;
import com.zhyf.gulimall.product.service.AttrService;
import com.zhyf.gulimall.product.service.CategoryService;
import com.zhyf.gulimall.product.vo.AttrGroupRelationVo;
import com.zhyf.gulimall.product.vo.AttrRespVo;
import com.zhyf.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationDao relationDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    AttrDao attrDao;
    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // 保存基本属性
        this.save(attrEntity);
        // 保存关联关系
        // 基本属性 保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            relationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type",
                "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        // 不同情况封装方法不同
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> respVos = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            // 设置属性分组的名字
            if ("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity attrgroupRelationEntity = relationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrgroupRelationEntity != null && attrgroupRelationEntity.getAttrGroupId() != null) {
                    Long attrGroupId = attrgroupRelationEntity.getAttrGroupId();
                    AttrGroupEntity groupEntity = attrGroupDao.selectById(attrGroupId);
                    attrRespVo.setGroupName(groupEntity.getAttrGroupName());
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    @Cacheable(value = "attr", key = "'attrInfo:'+#root.args[0]")
    public AttrRespVo getDetailInfo(Long attrId) {
        AttrEntity attrentity = attrDao.selectById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrentity, attrRespVo);
        // 属性分组信息  如果属性是基础属性就有一个分组
        if (attrentity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity != null) {
                attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                if (attrGroupEntity != null) {
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }
        // 分类信息
        Long catelogId = attrentity.getCatelogId();
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            Long[] catelogPath = categoryService.findCatelogPath(categoryEntity.getCatId());
            attrRespVo.setCategoryPath(catelogPath);
            attrRespVo.setCatelogName(categoryEntity.getName());
        }
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attrVo) {
        AttrEntity entity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, entity);
        this.updateById(entity);
        // 修改分组 关联
        if (entity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            relationEntity.setAttrId(attrVo.getAttrId());

            Integer count = relationDao.selectCount(new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            if (count > 0) {
                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            } else {
                relationDao.insert(relationEntity);
            }
        }
    }


    // 根据分类id 查找关联的所有基本属性

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> attrIds = entities.stream().map((entity) -> {
            return entity.getAttrId();
        }).collect(Collectors.toList());
        if (attrIds == null || attrIds.size() == 0) {
            return null;
        }
        List<AttrEntity> attrEntities = this.listByIds(attrIds);
        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> relationEntities = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(relationEntities);
    }

//    //获取当前分组没有关联的所有属性
//    @Override
//    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
//        // 1当前分组只能关联自己所属商品分类的里面的所有属性
//        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
//        // 1.1得到商品分类的ID
//        Long catelogId = attrGroupEntity.getCatelogId();
//        // 2当前分组只能关联到别的分组没有引用的属性
//        // 2.1当前分类下的其他分组   catelog_id 相等 但attr_group_id 不等
//        List<AttrGroupEntity> group = attrGroupDao.selectList(
//                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId).ne("attr_group_id", attrgroupId));
//        // 属于这个商品分类不 但是属性分组不同 的这些组的 id
//        List<Long> collect = group.stream().map((entity) -> {
//            return entity.getAttrGroupId();
//        }).collect(Collectors.toList());
//        // 2.2这些分组关联的属性
//        List<AttrAttrgroupRelationEntity> groupId = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
//        List<Long> attrIds = groupId.stream().map((item) -> {
//            return item.getAttrId();
//        }).collect(Collectors.toList());
//        // 2.3从当前分类的所有属性中剔除这些属性
//        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).
//                eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
//        if (attrIds != null && attrIds.size() > 0) {
//            wrapper.notIn("attr_id", attrIds);
//        }
//        String key = (String) params.get("key");
//        if (!StringUtils.isEmpty(key)) {
//            wrapper.and((w) -> {
//                w.eq("attr_id", key).or().like("attr_name", key);
//            });
//        }
//        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
//
//        return new PageUtils(page);
//    }

    /**
     * 获取当前分组没有关联的所有属性
     *
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1、当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2、当前分组只能关联别的分组没有引用的属性
        //2.1)、当前分类下的其他分组
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = group.stream().map(item -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        //2.2)、这些分组关联的属性
        List<AttrAttrgroupRelationEntity> groupId = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> attrIds = groupId.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());

        //2.3)、从当前分类的所有属性中移除这些属性；
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;
    }

    /**
     * 在指定的属性集合里面挑选出来检索属性
     *
     * @param attrIds
     * @return
     */
    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        // SELECT * FROM `pms_attr` WHERE attr_id IN (?) AND search_type = 1
        return baseMapper.selectSearchAttrsIds(attrIds);
    }
}