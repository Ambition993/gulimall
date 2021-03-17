package com.zhyf.common.exception;

/*
 *   错误码为5位
 *   前两位是业务场景
 *   后三位是错误码
 *   10 通用
 *       001 数据校验错误
 *   11 商品
 *   12 订单
 *   13 购物车
 *   14 物流
 *
 * */
public enum BizCodeEnum {

    UNKNOWN_EXCEPTION(10000, "未知错误"),
    VALID_EXCEPTION(10001, "参数校验错误"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常");


    private int code;
    private String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
