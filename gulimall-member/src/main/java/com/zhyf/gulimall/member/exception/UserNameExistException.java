package com.zhyf.gulimall.member.exception;

public class UserNameExistException extends  RuntimeException{
    public UserNameExistException() {
        super("用户名已经存在");
    }
}
