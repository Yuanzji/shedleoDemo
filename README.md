# shedleoDemo —— Spring Cloud Alibaba 微服务 Demo（RabbitMQ 版）

## 📋 项目简介

基于 **Spring Cloud Alibaba** 生态构建的电商订单/库存微服务演示项目，核心展示：

- ✅ 订单服务 —— 状态机、合并发货、**RabbitMQ TTL + DLX 延迟消息自动关单**
- ✅ 库存服务 —— Redis + Lua 预扣、DB 乐观锁最终扣减、预售排期、库存预警
- ✅ 订单 ↔ 库存 Feign 远程调用（含 Sentinel 降级）
- ✅ 订单超时自动关闭（RabbitMQ 死信队列实现）
- ✅ 退款、结算、预警等业务消息通过 RabbitMQ 异步解耦

## 🏗️ 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 11 | 语言 |
| Spring Boot | 2.7.18 | 基础框架 |
| Spring Cloud | 2021.0.8 | 微服务框架 |
| Spring Cloud Alibaba | 2021.0.5.0 | Nacos / Sentinel 等 |
| Nacos | 2.2.3 | 注册中心 + 配置中心 |
| OpenFeign | — | 服务间调用 |
| Sentinel | — | 限流降级 |
| **RabbitMQ** | 3.11.x | **延迟消息 + 异步解耦** |
| MySQL | 8.0 | 数据库 |
| MyBatis-Plus | 3.5.3.1 | ORM |
| Redis | 7.0 | 缓存 / 分布式锁 |
| Redisson | 3.20.0 | 分布式锁 |
| Lombok | 1.18.30 | 简化代码 |

## 📂 模块划分

```
shedleoDemo/
├── pom.xml                    # 父 POM（版本管理）
├── sql/
│   └── schema.sql             # 数据库初始化脚本
│
├── shedleo-common/            # 公共模块
│   └── src/main/java/
│       └── com/shedleo/common/
│           ├── result/        # Result<T> 统一返回
│           ├── exception/     # 业务异常 + 全局处理
│           ├── constant/      # 订单/库存/MQ/Redis 常量
│           └── util/          # 雪花 ID / JWT 工具
│
├── shedleo-gateway/           # 网关（壳）
│   └── 路由配置 / Nacos 注册
│
├── shedleo-order/             # 订单服务（★ 核心）
│   └── 状态机 / TTL+DLX 延迟关单 / Feign 调库存
│
├── shedleo-inventory/         # 库存服务（★ 核心）
│   └── Redis+Lua 预扣 / DB 乐观锁 / 预售排期 / 预警
│
├── shedleo-payment/           # 支付服务（壳）
│   └── Feign 接口 + MQ 监听器 + 空实现
│
├── shedleo-user/              # 用户服务（壳）
│   └── Feign 接口 + 模拟数据
│
├── shedleo-product/           # 商品服务（壳）
│   └── Feign 接口 + 模拟数据
│
└── shedleo-message/           # 消息服务（壳）
    └── Feign 接口 + MQ 监听器 + 空实现
```

## 🔌 端口分配

| 服务 | 端口 |
|------|------|
| gateway | 8080 |
| order | 8081 |
| inventory | 8082 |
| payment | 8083 |
| user | 8084 |
| product | 8085 |
| message | 8086 |

## 🚀 快速启动

### 前置环境

1. **JDK 11+**
2. **Maven 3.8+**
3. **MySQL 8.0**
   ```sql
   -- 执行 sql/schema.sql
   source /path/to/sql/schema.sql;
   ```
4. **Redis 7.0**
   ```bash
   redis-server
   ```
5. **RabbitMQ 3.11+**
   ```bash
   # Docker 快速启动
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.11-management
   # 默认账号 guest / guest
   ```
6. **Nacos 2.2.3**
   ```bash
   # Docker 快速启动（单机模式）
   docker run -d --name nacos -p 8848:8848 -p 9848:9848 \
     -e MODE=standalone nacos/nacos-server:v2.2.3
   # 浏览器访问 http://localhost:8848/nacos (nacos/nacos)
   ```

### 修改配置

所有服务的 `application.yml` 默认连接：
- Nacos: `127.0.0.1:8848`
- MySQL: `jdbc:mysql://127.0.0.1:3306/shedleo_demo` (user: root, pwd: root)
- Redis: `127.0.0.1:6379`
- RabbitMQ: `127.0.0.1:5672` (guest/guest)

根据本地环境按需修改。

### Maven 构建

```bash
cd shedleoDemo
mvn clean install -DskipTests
```

### 启动顺序

建议按依赖顺序启动（也可全部并行启动，Nacos 会自动重试）：

```
1️⃣ Nacos Server
2️⃣ Redis
3️⃣ RabbitMQ
4️⃣ MySQL (已建好库)

5️⃣ 壳子服务（无依赖，先启动）：
   - shedleo-user       (8084)
   - shedleo-product    (8085)
   - shedleo-payment    (8083)
   - shedleo-message    (8086)

6️⃣ 核心服务：
   - shedleo-inventory  (8082)   ← 提供库存扣减接口
   - shedleo-order      (8081)   ← 依赖 inventory
   
7️⃣ shedleo-gateway     (8080)   ← 最后启动（聚合其他服务）
```

## 🧪 接口测试

### 1. 创建订单（核心链路）

```bash
curl -X POST http://localhost:8081/order/create \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": 1001,
    "merchantId": 1,
    "items": [
      { "skuId": 1001, "quantity": 2, "price": 99.00, "productName": "商品A" },
      { "skuId": 1002, "quantity": 1, "price": 199.00, "productName": "商品B" }
    ]
  }'
```

**预期结果**：
- ✅ DB `t_order` 插入一条状态为 0（待支付）的订单
- ✅ DB `t_order_item` 插入 2 条明细
- ✅ DB `t_inventory` 乐观锁扣减成功（version+1）
- ✅ Redis 库存缓存同步扣减
- ✅ **RabbitMQ 发送延迟消息** → 24 小时后自动关闭

### 2. 查询订单

```bash
curl http://localhost:8081/order/ORDxxxxxxxxx
```

### 3. 分页查询

```bash
curl "http://localhost:8081/order/list?current=1&size=10&userId=1001"
```

### 4. 确认收货（触发结算消息）

```bash
curl -X POST "http://localhost:8081/order/confirm?orderNo=ORDxxxxxxxxx"
```

**预期结果**：
- ✅ 订单状态变更为"已确认"
- ✅ 发送 `payment.settle` 消息到 RabbitMQ
- ✅ payment 壳服务打印 `[PAYMENT-SHELL] 收到结算消息` 日志

### 5. 申请退款

```bash
curl -X POST http://localhost:8081/order/refund/apply \
  -H 'Content-Type: application/json' \
  -d '{
    "orderNo": "ORDxxxxxxxxx",
    "amount": 99.00,
    "reason": "不想要了"
  }'
```

**预期结果**：
- ✅ 订单状态 → 退款中
- ✅ 发送 `payment.refund` 消息
- ✅ 发送 `inventory.rollback` 消息
- ✅ inventory 壳消费消息后回滚库存（Redis+DB）

### 6. 查询库存

```bash
curl http://localhost:8082/inventory/1001
curl "http://localhost:8082/inventory/list?merchantId=1"
```

### 7. 通过网关访问

```bash
curl http://localhost:8080/order/list?current=1&size=10
curl http://localhost:8080/inventory/1001
```

### 8. 验证订单超时自动关闭（核心功能）

默认 TTL 为 **24 小时**。本地测试可以在 `OrderService.sendDelayCloseMessage` 中临时修改：

```java
// 原: setExpiration(String.valueOf(MqConstant.ORDER_TIMEOUT_SECONDS * 1000));
// 改为 30 秒快速验证:
message.getMessageProperties().setExpiration("30000");
```

然后：
1. 创建一笔订单
2. 等待约 30 秒
3. 查看 `t_order` 表，状态应为 6（已取消）
4. 查看 RabbitMQ 控制台 → DeadLetterConsumer 消费日志
5. 查看库存表，已自动回滚

## 🔄 RabbitMQ 消息流程

```
 ┌───────────────┐          ┌──────────────────────┐          ┌───────────────┐
 │  Order Service │          │  RabbitMQ Broker     │          │  Consumer     │
 │               │          │                      │          │               │
 │ 创建订单 ─────┼──TTL───▶ │ order.delay.queue    │          │               │
 │               │  24h     │   (暂存，不消费)      │          │               │
 │               │          │         │            │          │               │
 │               │          │   TTL 到期 → 死信    │          │               │
 │               │          │         ▼            │          │               │
 │               │          │ order.dlx.exchange   │          │               │
 │               │          │   (DLX 交换机)      │          │               │
 │               │          │         │            │          │               │
 │               │          │ order.dlx.queue ─────┼─────────▶│ DeadLetter    │
 │               │          │   (死信队列)          │          │  Consumer     │
 │               │          │                      │          │ (关单+回滚)  │
 └───────────────┘          └──────────────────────┘          └───────────────┘
```

## 🔒 库存扣减流程（防超卖）

```
请求 → Redisson 分布式锁（skuId 级别）
     → Redis Lua 预扣（原子性）
     → DB 乐观锁最终扣减（version 字段）
     → 不一致时回滚 Redis 并重试
     → 保存库存流水 log
     → 释放锁
```

## 📝 注意事项

1. **壳子服务**（payment/user/product/message）只提供 Feign 接口 + 空实现 + 注释，**不具备真实业务能力**，仅作为远程调用目标和 MQ 消费者存在。
2. 所有服务默认连接本地中间件，生产环境请统一使用 Nacos 配置中心管理。
3. Sentinel Dashboard 可从 https://github.com/alibaba/Sentinel/releases 下载运行。
4. 项目使用 Lombok，IDEA 需安装 Lombok 插件并开启 Annotation Processing。

## 🎯 核心亮点

| 功能 | 实现方式 | 代码位置 |
|------|---------|---------|
| 订单超时关闭 | RabbitMQ TTL + DLX | `RabbitMqConfig`, `DeadLetterConsumer`, `OrderService.sendDelayCloseMessage()` |
| 防超卖 | Redis Lua + Redisson + DB 乐观锁 | `InventoryService.deductSpot()` |
| 状态机 | 枚举 + 静态状态流转表 | `OrderStateMachine` |
| 异步解耦 | RabbitMQ Direct Exchange | 各 `RabbitMqConfig` + Consumer |
| 服务降级 | OpenFeign Sentinel Fallback | `InventoryClientFallback` |
| 库存预警 | 定时扫描 + MQ 广播 | `StockWarnJob` |
