package com.zhyf.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.zhyf.gulimall.product.dao")
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhyf.gulimall.product.feign")
public class GulimallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }
}
//使用了mybatis-plus 的逻辑删除功能
// 1配置全局的逻辑删除规则
// 2实体字段添加逻辑删除注解



/*
 *  1 要对数据进行校验 使用javax.validation 包的注解并且自定义错误消息提示
 *  2 在controller层的参数前面加上 @Valid注解使得controller层的数据校验生效
 *
            @RequestMapping("/save")
            @RequiresPermissions("product:category:save")
            public R save(@Valid @RequestBody CategoryEntity category) {
                categoryService.save(category);

                return R.ok();
            }
 *   校验出错后会有响应
 * 3 给controller 加一个bindingResult
 *
 *  @RequestMapping("/save")
//   @RequiresPermissions("product:category:save")
    public R save(@Valid @RequestBody CategoryEntity category, BindingResult result) {
        Map<String, String> map = new HashMap<>();
        if (result.hasErrors()) {
            // 获取校验的错误结果
            result.getFieldErrors().forEach((item) -> {
                // 获取到错误message
                String message = item.getDefaultMessage();
                // 发生错误的字段
                String fieldName = item.getField();
                map.put(message, fieldName);
            });
            R.error(400, "提交的数据不合法").put("data", map);
        } else {
            categoryService.save(category);
        }
        return R.ok();
    }
 *  4  直接新建一个进行全局处理
 *  @ControllerAdvice
 *  @Slf4j
    @RestControllerAdvice(basePackages = "com.zhyf.gulimall.controller")
    public class GulimallExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> map = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError) -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(400, "数据校验出现问题").put("data", map);
    }
}
*
* 
*
*
*
 * */
