package com.zhyf.gulimall.product.exception;


import com.zhyf.common.exception.BizCodeEnum;
import com.zhyf.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.HashMap;
import java.util.Map;

/*
 * 集中处理数据校验异常
 *
 * */
@Slf4j
@RestControllerAdvice(basePackages = "com.zhyf.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ResponseBody
    @ExceptionHandler(value = WebExchangeBindException.class)
    public R handleValidException(WebExchangeBindException e) {
        log.error("数据校验出现问题", e.getMessage(), e.getClass());

        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> map = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError) -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("未知错误",throwable.toString() );
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }
}


