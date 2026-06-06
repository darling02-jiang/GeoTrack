# GeoTrack 架构说明

## 1. 总体分层

- 接入层：`geotrack-gateway`
- 业务层：`auth` / `poi` / `content` / `mall` / `order`
- 支撑层：`MySQL` / `Redis` / `RocketMQ` / `Nacos`
- 展示层：`geotrack-web`

## 2. 服务职责

- `auth-service`：验证码登录、Redis Session、用户信息、积分余额、积分流水、内部积分发放和扣减。
- `poi-service`：POI 管理、Redis GEO 附近查询、打卡距离校验、打卡记录落库、打卡事件投递。
- `content-service`：动态列表、POI 圈子动态、热门榜、点赞、评论、打卡动态 MQ 消费。
- `mall-service`：商品目录、普通积分兑换、秒杀下单、Redis Lua 预扣库存、异步订单结算和补偿。
- `order-service`：用户订单查询。
- `gateway`：统一入口、服务发现路由、接口文档聚合和网关访问日志。

## 3. 服务调用方式

- 外部请求统一进入 `geotrack-gateway`。
- 网关通过 `lb://服务名` 使用 Nacos + LoadBalancer 转发请求。
- 服务间同步调用使用 OpenFeign，例如 poi/content/mall/order 调用 auth 解析登录态。
- 跨服务异步业务使用 RocketMQ，例如打卡后发积分和生成动态、秒杀后异步创建订单和扣积分。

## 4. 打卡主链路

1. 前端携带 `SESSION` Cookie 和 `X-Idempotency-Key` 调用 `/api/checkin`。
2. poi 服务通过 OpenFeign 调用 auth 服务解析用户。
3. `InterfaceGuardService` 处理 Sentinel QPS、用户频率和幂等键。
4. poi 服务使用 Redis GEO 优先计算用户到 POI 的距离，失败时回退 Haversine。
5. MySQL 唯一索引保证同一用户同一 POI 同一天只能成功打卡一次。
6. 打卡记录落库后，事务提交回调投递 RocketMQ 消息。
7. auth 服务消费消息发放积分，content 服务消费消息生成动态。

## 5. 秒杀主链路

1. 前端携带 `SESSION` Cookie 和 `X-Idempotency-Key` 调用 `/api/seckill/order`。
2. mall 服务校验商品状态和秒杀时间窗口。
3. Redis Lua 在单脚本内完成库存判断、库存预扣和用户限购标记。
4. 预扣成功后投递 RocketMQ 秒杀订单创建消息。
5. mall 服务消费消息创建 PENDING 订单，并投递积分扣减消息。
6. auth 服务按 orderNo 幂等扣减积分，并回传扣减结果消息。
7. mall 服务按扣减结果将订单置为 PAID 或 CANCELLED；失败时补偿 Redis 库存和用户限购标记。

## 6. 一致性与可靠性

- 打卡唯一性：数据库唯一索引 `uk_user_poi_day`。
- 积分幂等：积分流水按业务类型和业务编号唯一。
- MQ 幂等：`gt_mq_consume_log` 记录消费组和消息键。
- 秒杀防超卖：Redis Lua 原子预扣 + MySQL 结算扣减。
- 请求防重复：写接口要求 `X-Idempotency-Key`。
- 异常补偿：秒杀积分扣减失败后恢复 Redis 库存和限购标记。

## 7. 接口文档

项目接入 Springdoc OpenAPI。启动网关和业务服务后，访问 `http://localhost:8080/swagger-ui.html` 查看聚合接口文档。
