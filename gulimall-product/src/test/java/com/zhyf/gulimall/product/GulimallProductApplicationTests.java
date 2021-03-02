package com.zhyf.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhyf.gulimall.product.entity.BrandEntity;
import com.zhyf.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;


    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("ss");
        brandEntity.setLogo("hw");
        brandService.save(brandEntity);
        List<BrandEntity> brandEntities = brandService.list(new QueryWrapper<BrandEntity>().eq("logo", "hw"));
        brandEntities.forEach(System.out::println);
    }
}
