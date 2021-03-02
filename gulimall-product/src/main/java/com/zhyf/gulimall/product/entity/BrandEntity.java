package com.zhyf.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.zhyf.common.valid.AddGroup;
import com.zhyf.common.valid.ListValue;
import com.zhyf.common.valid.UpdateGroup;
import com.zhyf.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.annotation.RegEx;
import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author zhyf
 * @email nibainle@gmail.com
 * @date 2020-12-16 12:04:21
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(message = "品牌ID不能为空", groups = {UpdateGroup.class})
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    // 必须包含一个非空格字符 无论添加还是修改都不能为空
    @NotBlank(message = "品牌名必须提交", groups = {UpdateGroup.class, AddGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    // 修改的时候可以不携带 但是带了一定要合法
    @NotBlank(message = "logo不能为空", groups = {AddGroup.class}) //新增的时候触发这个
    @URL(message = "logo必须是一个合法的URL地址", groups = {UpdateGroup.class, AddGroup.class})
    private String logo;
    /**
     * 介绍
     */
    // 修改的时候可以不携带 但是带了一定要合法
    @NotBlank(message = "logo不能为空", groups = {AddGroup.class, UpdateGroup.class}) //新增的时候触发这个

    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotNull(message = "显示状态不能为空", groups = {AddGroup.class, UpdateStatusGroup.class})
    @ListValue(vals = {0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    // 修改的时候可以不携带（数据库原来的）  但是带了一定要合法
    @NotBlank(message = "检索首字母不能为空", groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是个一个字母", groups = {UpdateGroup.class, AddGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(message = "排序字段不能为空", groups = {AddGroup.class})
    @Min(value = 0, message = "排序必须大于等于0", groups = {UpdateGroup.class, AddGroup.class})
    private Integer sort;

}
