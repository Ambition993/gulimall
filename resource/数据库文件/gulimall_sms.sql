/*
 Navicat Premium Data Transfer

 Source Server         : alicloud
 Source Server Type    : MySQL
 Source Server Version : 50650
 Source Host           : 39.106.68.63:3306
 Source Schema         : gulimall_sms

 Target Server Type    : MySQL
 Target Server Version : 50650
 File Encoding         : 65001

 Date: 01/04/2021 20:40:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sms_coupon
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon`;
CREATE TABLE `sms_coupon`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_type` tinyint(1) NULL DEFAULT NULL COMMENT '优惠卷类型[0->全场赠券；1->会员赠券；2->购物赠券；3->注册赠券]',
  `coupon_img` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '优惠券图片',
  `coupon_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '优惠卷名字',
  `num` int(11) NULL DEFAULT NULL COMMENT '数量',
  `amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '金额',
  `per_limit` int(11) NULL DEFAULT NULL COMMENT '每人限领张数',
  `min_point` decimal(18, 4) NULL DEFAULT NULL COMMENT '使用门槛',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `use_type` tinyint(1) NULL DEFAULT NULL COMMENT '使用类型[0->全场通用；1->指定分类；2->指定商品]',
  `note` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `publish_count` int(11) NULL DEFAULT NULL COMMENT '发行数量',
  `use_count` int(11) NULL DEFAULT NULL COMMENT '已使用数量',
  `receive_count` int(11) NULL DEFAULT NULL COMMENT '领取数量',
  `enable_start_time` datetime NULL DEFAULT NULL COMMENT '可以领取的开始日期',
  `enable_end_time` datetime NULL DEFAULT NULL COMMENT '可以领取的结束日期',
  `code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '优惠码',
  `member_level` tinyint(1) NULL DEFAULT NULL COMMENT '可以领取的会员等级[0->不限等级，其他-对应等级]',
  `publish` tinyint(1) NULL DEFAULT NULL COMMENT '发布状态[0-未发布，1-已发布]',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '优惠券信息' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_coupon
-- ----------------------------

-- ----------------------------
-- Table structure for sms_coupon_history
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon_history`;
CREATE TABLE `sms_coupon_history`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_id` bigint(20) NULL DEFAULT NULL COMMENT '优惠券id',
  `member_id` bigint(20) NULL DEFAULT NULL COMMENT '会员id',
  `member_nick_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '会员名字',
  `get_type` tinyint(1) NULL DEFAULT NULL COMMENT '获取方式[0->后台赠送；1->主动领取]',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `use_type` tinyint(1) NULL DEFAULT NULL COMMENT '使用状态[0->未使用；1->已使用；2->已过期]',
  `use_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '订单id',
  `order_sn` bigint(20) NULL DEFAULT NULL COMMENT '订单号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '优惠券领取历史记录' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_coupon_history
-- ----------------------------

-- ----------------------------
-- Table structure for sms_coupon_spu_category_relation
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon_spu_category_relation`;
CREATE TABLE `sms_coupon_spu_category_relation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_id` bigint(20) NULL DEFAULT NULL COMMENT '优惠券id',
  `category_id` bigint(20) NULL DEFAULT NULL COMMENT '产品分类id',
  `category_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品分类名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '优惠券分类关联' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_coupon_spu_category_relation
-- ----------------------------

-- ----------------------------
-- Table structure for sms_coupon_spu_relation
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon_spu_relation`;
CREATE TABLE `sms_coupon_spu_relation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_id` bigint(20) NULL DEFAULT NULL COMMENT '优惠券id',
  `spu_id` bigint(20) NULL DEFAULT NULL COMMENT 'spu_id',
  `spu_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'spu_name',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '优惠券与产品关联' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_coupon_spu_relation
-- ----------------------------

-- ----------------------------
-- Table structure for sms_home_adv
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_adv`;
CREATE TABLE `sms_home_adv`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名字',
  `pic` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图片地址',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态',
  `click_count` int(11) NULL DEFAULT NULL COMMENT '点击数',
  `url` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '广告详情连接地址',
  `note` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  `publisher_id` bigint(20) NULL DEFAULT NULL COMMENT '发布者',
  `auth_id` bigint(20) NULL DEFAULT NULL COMMENT '审核者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '首页轮播广告' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_home_adv
-- ----------------------------

-- ----------------------------
-- Table structure for sms_home_subject
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_subject`;
CREATE TABLE `sms_home_subject`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '专题名字',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '专题标题',
  `sub_title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '专题副标题',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '显示状态',
  `url` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '详情连接',
  `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  `img` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '专题图片地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_home_subject
-- ----------------------------

-- ----------------------------
-- Table structure for sms_home_subject_spu
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_subject_spu`;
CREATE TABLE `sms_home_subject_spu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '专题名字',
  `subject_id` bigint(20) NULL DEFAULT NULL COMMENT '专题id',
  `spu_id` bigint(20) NULL DEFAULT NULL COMMENT 'spu_id',
  `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '专题商品' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_home_subject_spu
-- ----------------------------

-- ----------------------------
-- Table structure for sms_member_price
-- ----------------------------
DROP TABLE IF EXISTS `sms_member_price`;
CREATE TABLE `sms_member_price`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id` bigint(20) NULL DEFAULT NULL COMMENT 'sku_id',
  `member_level_id` bigint(20) NULL DEFAULT NULL COMMENT '会员等级id',
  `member_level_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '会员等级名',
  `member_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '会员对应价格',
  `add_other` tinyint(1) NULL DEFAULT NULL COMMENT '可否叠加其他优惠[0-不可叠加优惠，1-可叠加]',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商品会员价格' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_member_price
-- ----------------------------
INSERT INTO `sms_member_price` VALUES (37, 21, 3, '铜牌会员', 5399.0000, 1);
INSERT INTO `sms_member_price` VALUES (38, 21, 4, '银牌会员', 5299.0000, 1);
INSERT INTO `sms_member_price` VALUES (39, 22, 3, '铜牌会员', 1111.0000, 1);
INSERT INTO `sms_member_price` VALUES (40, 22, 4, '银牌会员', 1111.0000, 1);
INSERT INTO `sms_member_price` VALUES (41, 23, 3, '铜牌会员', 4400.0000, 1);
INSERT INTO `sms_member_price` VALUES (42, 23, 4, '银牌会员', 4300.0000, 1);
INSERT INTO `sms_member_price` VALUES (43, 24, 3, '铜牌会员', 111.0000, 1);
INSERT INTO `sms_member_price` VALUES (44, 24, 4, '银牌会员', 111.0000, 1);
INSERT INTO `sms_member_price` VALUES (45, 25, 3, '铜牌会员', 11.0000, 1);
INSERT INTO `sms_member_price` VALUES (46, 25, 4, '银牌会员', 1111.0000, 1);
INSERT INTO `sms_member_price` VALUES (47, 26, 3, '铜牌会员', 11.0000, 1);
INSERT INTO `sms_member_price` VALUES (48, 26, 4, '银牌会员', 111.0000, 1);
INSERT INTO `sms_member_price` VALUES (49, 27, 3, '铜牌会员', 5299.0000, 1);
INSERT INTO `sms_member_price` VALUES (50, 27, 4, '银牌会员', 5399.0000, 1);
INSERT INTO `sms_member_price` VALUES (51, 28, 3, '铜牌会员', 5299.0000, 1);
INSERT INTO `sms_member_price` VALUES (52, 28, 4, '银牌会员', 5399.0000, 1);
INSERT INTO `sms_member_price` VALUES (53, 29, 3, '铜牌会员', 5799.0000, 1);
INSERT INTO `sms_member_price` VALUES (54, 29, 4, '银牌会员', 5700.0000, 1);
INSERT INTO `sms_member_price` VALUES (55, 30, 3, '铜牌会员', 5399.0000, 1);
INSERT INTO `sms_member_price` VALUES (56, 30, 4, '银牌会员', 5299.0000, 1);
INSERT INTO `sms_member_price` VALUES (57, 31, 3, '铜牌会员', 5399.0000, 1);
INSERT INTO `sms_member_price` VALUES (58, 31, 4, '银牌会员', 5299.0000, 1);
INSERT INTO `sms_member_price` VALUES (59, 32, 3, '铜牌会员', 5799.0000, 1);
INSERT INTO `sms_member_price` VALUES (60, 32, 4, '银牌会员', 5699.0000, 1);
INSERT INTO `sms_member_price` VALUES (61, 33, 3, '铜牌会员', 5777.0000, 1);
INSERT INTO `sms_member_price` VALUES (62, 33, 4, '银牌会员', 5555.0000, 1);
INSERT INTO `sms_member_price` VALUES (63, 34, 3, '铜牌会员', 5555.0000, 1);
INSERT INTO `sms_member_price` VALUES (64, 34, 4, '银牌会员', 5555.0000, 1);
INSERT INTO `sms_member_price` VALUES (65, 35, 3, '铜牌会员', 5555.0000, 1);
INSERT INTO `sms_member_price` VALUES (66, 35, 4, '银牌会员', 5555.0000, 1);
INSERT INTO `sms_member_price` VALUES (67, 36, 3, '铜牌会员', 4899.0000, 1);
INSERT INTO `sms_member_price` VALUES (68, 36, 4, '银牌会员', 4799.0000, 1);
INSERT INTO `sms_member_price` VALUES (69, 37, 3, '铜牌会员', 3899.0000, 1);
INSERT INTO `sms_member_price` VALUES (70, 37, 4, '银牌会员', 3799.0000, 1);
INSERT INTO `sms_member_price` VALUES (71, 38, 3, '铜牌会员', 3999.0000, 1);
INSERT INTO `sms_member_price` VALUES (72, 38, 4, '银牌会员', 3899.0000, 1);
INSERT INTO `sms_member_price` VALUES (73, 39, 3, '铜牌会员', 1111.0000, 1);
INSERT INTO `sms_member_price` VALUES (74, 39, 4, '银牌会员', 1111.0000, 1);

-- ----------------------------
-- Table structure for sms_seckill_promotion
-- ----------------------------
DROP TABLE IF EXISTS `sms_seckill_promotion`;
CREATE TABLE `sms_seckill_promotion`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '活动标题',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始日期',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束日期',
  `status` tinyint(4) NULL DEFAULT NULL COMMENT '上下线状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '秒杀活动' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_seckill_promotion
-- ----------------------------

-- ----------------------------
-- Table structure for sms_seckill_session
-- ----------------------------
DROP TABLE IF EXISTS `sms_seckill_session`;
CREATE TABLE `sms_seckill_session`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '场次名称',
  `start_time` datetime NULL DEFAULT NULL COMMENT '每日开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '每日结束时间',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '启用状态',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '秒杀活动场次' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_seckill_session
-- ----------------------------

-- ----------------------------
-- Table structure for sms_seckill_sku_notice
-- ----------------------------
DROP TABLE IF EXISTS `sms_seckill_sku_notice`;
CREATE TABLE `sms_seckill_sku_notice`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(20) NULL DEFAULT NULL COMMENT 'member_id',
  `sku_id` bigint(20) NULL DEFAULT NULL COMMENT 'sku_id',
  `session_id` bigint(20) NULL DEFAULT NULL COMMENT '活动场次id',
  `subcribe_time` datetime NULL DEFAULT NULL COMMENT '订阅时间',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `notice_type` tinyint(1) NULL DEFAULT NULL COMMENT '通知方式[0-短信，1-邮件]',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '秒杀商品通知订阅' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_seckill_sku_notice
-- ----------------------------

-- ----------------------------
-- Table structure for sms_seckill_sku_relation
-- ----------------------------
DROP TABLE IF EXISTS `sms_seckill_sku_relation`;
CREATE TABLE `sms_seckill_sku_relation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `promotion_id` bigint(20) NULL DEFAULT NULL COMMENT '活动id',
  `promotion_session_id` bigint(20) NULL DEFAULT NULL COMMENT '活动场次id',
  `sku_id` bigint(20) NULL DEFAULT NULL COMMENT '商品id',
  `seckill_price` decimal(10, 0) NULL DEFAULT NULL COMMENT '秒杀价格',
  `seckill_count` decimal(10, 0) NULL DEFAULT NULL COMMENT '秒杀总量',
  `seckill_limit` decimal(10, 0) NULL DEFAULT NULL COMMENT '每人限购数量',
  `seckill_sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '秒杀活动商品关联' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_seckill_sku_relation
-- ----------------------------

-- ----------------------------
-- Table structure for sms_sku_full_reduction
-- ----------------------------
DROP TABLE IF EXISTS `sms_sku_full_reduction`;
CREATE TABLE `sms_sku_full_reduction`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id` bigint(20) NULL DEFAULT NULL COMMENT 'spu_id',
  `full_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '满多少',
  `reduce_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '减多少',
  `add_other` tinyint(1) NULL DEFAULT NULL COMMENT '是否参与其他优惠',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商品满减信息' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_sku_full_reduction
-- ----------------------------
INSERT INTO `sms_sku_full_reduction` VALUES (19, 21, 8888.0000, 88.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (20, 22, 1111.0000, 11.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (21, 23, 11111.0000, 11.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (22, 24, 22222.0000, 11.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (23, 25, 111.0000, 1.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (24, 26, 1111.0000, 1111.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (25, 27, 6666.0000, 66.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (26, 28, 6666.0000, 66.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (27, 29, 6666.0000, 66.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (28, 30, 6666.0000, 66.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (29, 31, 6666.0000, 66.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (30, 32, 2222.0000, 22.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (31, 33, 222.0000, 11.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (32, 34, 5555.0000, 55.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (33, 35, 5555.0000, 55.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (34, 36, 1111.0000, 111.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (35, 37, 1111.0000, 11.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (36, 38, 1111.0000, 11.0000, NULL);
INSERT INTO `sms_sku_full_reduction` VALUES (37, 39, 1111.0000, 11.0000, NULL);

-- ----------------------------
-- Table structure for sms_sku_ladder
-- ----------------------------
DROP TABLE IF EXISTS `sms_sku_ladder`;
CREATE TABLE `sms_sku_ladder`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id` bigint(20) NULL DEFAULT NULL COMMENT 'spu_id',
  `full_count` int(11) NULL DEFAULT NULL COMMENT '满几件',
  `discount` decimal(4, 2) NULL DEFAULT NULL COMMENT '打几折',
  `price` decimal(18, 4) NULL DEFAULT NULL COMMENT '折后价',
  `add_other` tinyint(1) NULL DEFAULT NULL COMMENT '是否叠加其他优惠[0-不可叠加，1-可叠加]',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商品阶梯价格' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_sku_ladder
-- ----------------------------
INSERT INTO `sms_sku_ladder` VALUES (19, 21, 1111, 1.00, NULL, 1);
INSERT INTO `sms_sku_ladder` VALUES (20, 22, 111, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (21, 23, 3, 1.00, NULL, 1);
INSERT INTO `sms_sku_ladder` VALUES (22, 24, 1111, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (23, 25, 1111, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (24, 26, 1111, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (25, 27, 2, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (26, 28, 2, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (27, 29, 2, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (28, 30, 2, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (29, 31, 2, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (30, 32, 222, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (31, 33, 1111, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (32, 34, 555, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (33, 35, 555, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (34, 36, 111, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (35, 37, 1111, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (36, 38, 1111, 1.00, NULL, 0);
INSERT INTO `sms_sku_ladder` VALUES (37, 39, 1111, 1.00, NULL, 0);

-- ----------------------------
-- Table structure for sms_spu_bounds
-- ----------------------------
DROP TABLE IF EXISTS `sms_spu_bounds`;
CREATE TABLE `sms_spu_bounds`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `spu_id` bigint(20) NULL DEFAULT NULL,
  `grow_bounds` decimal(18, 4) NULL DEFAULT NULL COMMENT '成长积分',
  `buy_bounds` decimal(18, 4) NULL DEFAULT NULL COMMENT '购物积分',
  `work` tinyint(1) NULL DEFAULT NULL COMMENT '优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商品spu积分设置' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sms_spu_bounds
-- ----------------------------
INSERT INTO `sms_spu_bounds` VALUES (7, 18, 9000.0000, 9000.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (8, 19, 66.0000, 6000.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (9, 20, 11.0000, 11.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (10, 21, 3000.0000, 4000.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (11, 22, 3000.0000, 4000.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (12, 23, 11.0000, 111.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (13, 24, 11.0000, 111.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (14, 25, 11.0000, 111.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (15, 26, 11.0000, 111.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (16, 27, 222.0000, 222.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (17, 28, 222.0000, 222.0000, NULL);
INSERT INTO `sms_spu_bounds` VALUES (18, 29, 221.0000, 122.0000, NULL);

SET FOREIGN_KEY_CHECKS = 1;
