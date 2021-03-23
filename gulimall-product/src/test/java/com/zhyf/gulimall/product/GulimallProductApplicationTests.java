package com.zhyf.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhyf.gulimall.product.dao.AttrGroupDao;
import com.zhyf.gulimall.product.entity.BrandEntity;
import com.zhyf.gulimall.product.service.BrandService;
import com.zhyf.gulimall.product.service.CategoryService;
import com.zhyf.gulimall.product.vo.SpuItemAttrGroupVo;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    AttrGroupDao attrGroupDao;

    @Test
    public void getParentPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        for (Long catelog : catelogPath) {
            System.out.println(catelog);
        }
    }

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("ss");
        brandEntity.setLogo("hw");
        brandService.save(brandEntity);
        List<BrandEntity> brandEntities = brandService.list(new QueryWrapper<BrandEntity>().eq("logo", "hw"));
        brandEntities.forEach(System.out::println);
    }

    @Test
    void testRedis() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello", "world_" + UUID.randomUUID().toString());
        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Test
    public void testSql() {
        List<SpuItemAttrGroupVo> spuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(28L, 225L);
        System.out.println(spuId);
    }
}
