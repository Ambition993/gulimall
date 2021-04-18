package com.zhyf.common.exception;

/*
 *   错误码为5位
 *   前两位是业务场景
 *   后三位是错误码
 *   10 通用
 *       001 数据校验错误
 *       002 短信验证码频率太高
 *   11 商品
 *   12 订单
 *   13 购物车
 *   14 物流
 *   15 用户
 *   21 库存
 * */
public enum BizCodeEnum {

    UNKNOWN_EXCEPTION(10000, "未知错误"),
    VALID_EXCEPTION(10001, "参数校验错误"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高"),
    TOO_MANY_REQUEST(10003, "请求过多"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USER_EXIST_EXCEPTION(15001, "用户已存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已存在"),
    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003, "用户账号或密码错误"),
    NO_STOCK_EXCEPTION(21000, "商品无库存");


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
