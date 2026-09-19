-- ========================================
-- shedleoDemo 数据库初始化脚本
-- MySQL 8.0+
-- ========================================

CREATE DATABASE IF NOT EXISTS shedleo_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shedleo_demo;

-- ========== 订单模块 ==========

DROP TABLE IF EXISTS t_order_item;
DROP TABLE IF EXISTS t_refund;
DROP TABLE IF EXISTS t_order;

CREATE TABLE t_order (
    id                  BIGINT          NOT NULL COMMENT '主键ID（雪花算法）',
    order_no            VARCHAR(32)     NOT NULL COMMENT '订单号',
    user_id             BIGINT          NOT NULL COMMENT '用户ID',
    merchant_id         BIGINT          NOT NULL COMMENT '商家ID',
    status              TINYINT         NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待支付 1-已支付 2-待发货 3-已发货 4-确认收货 5-已完成 6-已取消 7-退款中 8-已退款',
    total_amount        DECIMAL(12,2)   NOT NULL DEFAULT 0 COMMENT '订单总金额',
    pay_amount          DECIMAL(12,2)   NOT NULL DEFAULT 0 COMMENT '实付金额',
    merge_group_id      VARCHAR(32)     DEFAULT NULL COMMENT '合并发货组ID',
    expected_delivery_time DATETIME      DEFAULT NULL COMMENT '预计发货时间',
    pay_time            DATETIME        DEFAULT NULL COMMENT '支付时间',
    ship_time           DATETIME        DEFAULT NULL COMMENT '发货时间',
    confirm_time        DATETIME        DEFAULT NULL COMMENT '确认收货时间',
    cancel_time         DATETIME        DEFAULT NULL COMMENT '取消时间',
    cancel_reason       VARCHAR(255)    DEFAULT NULL COMMENT '取消原因',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_merchant_id (merchant_id),
    KEY idx_status (status),
    KEY idx_merge_group (merge_group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

CREATE TABLE t_order_item (
    id                  BIGINT          NOT NULL COMMENT '主键ID',
    order_id            BIGINT          NOT NULL COMMENT '订单ID',
    sku_id              BIGINT          NOT NULL COMMENT 'SKU ID',
    quantity            INT             NOT NULL COMMENT '购买数量',
    price               DECIMAL(10,2)   NOT NULL COMMENT '单价',
    product_name        VARCHAR(200)    NOT NULL COMMENT '商品名称（快照）',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_id (order_id),
    KEY idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

CREATE TABLE t_refund (
    id                  BIGINT          NOT NULL COMMENT '主键ID',
    order_id            BIGINT          NOT NULL COMMENT '订单ID',
    refund_no           VARCHAR(32)     NOT NULL COMMENT '退款单号',
    amount              DECIMAL(12,2)   NOT NULL COMMENT '退款金额',
    status              TINYINT         NOT NULL DEFAULT 0 COMMENT '状态: 0-处理中 1-成功 2-失败',
    reason              VARCHAR(255)    DEFAULT NULL COMMENT '退款原因',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refund_no (refund_no),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- ========== 库存模块 ==========

DROP TABLE IF EXISTS t_inventory_log;
DROP TABLE IF EXISTS t_presale_schedule;
DROP TABLE IF EXISTS t_inventory;

CREATE TABLE t_inventory (
    id                  BIGINT          NOT NULL COMMENT '主键ID',
    sku_id              BIGINT          NOT NULL COMMENT 'SKU ID',
    stock               INT             NOT NULL DEFAULT 0 COMMENT '现货库存',
    safe_stock          INT             NOT NULL DEFAULT 10 COMMENT '安全库存阈值',
    version             INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    merchant_id         BIGINT          NOT NULL COMMENT '商家ID',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sku_id (sku_id),
    KEY idx_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

CREATE TABLE t_presale_schedule (
    id                  BIGINT          NOT NULL COMMENT '主键ID',
    sku_id              BIGINT          NOT NULL COMMENT 'SKU ID',
    order_id            BIGINT          NOT NULL COMMENT '订单ID',
    expected_delivery_time DATETIME      NOT NULL COMMENT '预计发货时间',
    status              TINYINT         NOT NULL DEFAULT 0 COMMENT '状态: 0-待发货 1-已发货 2-已取消',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sku_id (sku_id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预售排期表';

CREATE TABLE t_inventory_log (
    id                  BIGINT          NOT NULL COMMENT '主键ID',
    sku_id              BIGINT          NOT NULL COMMENT 'SKU ID',
    order_id            BIGINT          DEFAULT NULL COMMENT '订单ID',
    change_type         TINYINT         NOT NULL COMMENT '变更类型: 1-扣减 2-回滚 3-预售',
    quantity            INT             NOT NULL COMMENT '变更数量（正数）',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sku_id (sku_id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ========== 初始数据 ==========

INSERT INTO t_inventory (id, sku_id, stock, safe_stock, version, merchant_id) VALUES
(1, 1001, 100, 10, 0, 1),
(2, 1002, 50,  10, 0, 1),
(3, 1003, 200, 20, 0, 2),
(4, 1004, 5,   10, 0, 1);
