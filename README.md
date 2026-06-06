# GeoTrack 后端项目

GeoTrack 是一个面向 Java 后端面试展示的文旅打卡积分平台。项目采用 Spring Boot 3、Spring Cloud Alibaba、Nacos、Gateway、OpenFeign、Redis、RocketMQ、MyBatis-Plus 和 MySQL，覆盖登录认证、POI 打卡、Redis GEO 距离校验、动态内容、积分商城、秒杀下单和订单查询等链路。

## 模块结构

- `geotrack-common`：统一响应体、业务异常、限流幂等、请求日志、OpenAPI 通用配置
- `geotrack-gateway`：统一网关，基于 Nacos 服务发现和 LoadBalancer 转发请求，并聚合接口文档
- `geotrack-auth-service`：验证码登录、Redis Session、用户信息、积分账户、积分流水、内部积分接口
- `geotrack-poi-service`：POI 管理、Redis GEO 附近查询、打卡距离校验、打卡事件投递
- `geotrack-content-service`：打卡动态、点赞、评论、热门榜、MQ 消费生成动态
- `geotrack-mall-service`：商品目录、普通积分兑换、Redis Lua 秒杀、订单创建和补偿
- `geotrack-order-service`：用户订单查询
- `geotrack-web`：配套前端演示页面

## 核心链路

1. 用户通过验证码登录，登录态写入 Redis Session。
2. 用户查询附近 POI，服务通过 Redis GEO 返回距离最近的启用点位。
3. 用户提交打卡，请求携带 Cookie 和 `X-Idempotency-Key`。
4. POI 服务校验登录态、幂等、频率、距离和当日唯一打卡。
5. 打卡成功后，POI 服务在事务提交后投递 RocketMQ 消息。
6. auth 服务异步发放积分，content 服务异步生成动态。
7. 用户在积分商城兑换或秒杀商品，秒杀使用 Redis Lua 原子预扣库存。
8. mall 服务通过 RocketMQ 异步创建秒杀订单、扣减积分，并在失败时补偿库存和限购标记。

## 技术亮点

- Spring Cloud Gateway + Nacos 服务注册发现
- OpenFeign 服务间同步调用
- Redis Session 分布式登录态
- Redis GEO 附近 POI 查询和打卡距离校验
- RocketMQ 异步解耦积分、动态和秒杀订单链路
- Redis Lua 原子秒杀库存预扣和单用户限购
- 业务幂等：请求幂等键、积分流水唯一约束、MQ 消费日志
- Sentinel + Redis 用户频率控制
- Springdoc OpenAPI 接口文档

## 接口文档

启动网关和各业务服务后访问：

- 网关聚合 Swagger UI：`http://localhost:8080/swagger-ui.html`
- auth 服务：`http://localhost:9001/swagger-ui.html`
- poi 服务：`http://localhost:9002/swagger-ui.html`
- content 服务：`http://localhost:9003/swagger-ui.html`
- mall 服务：`http://localhost:9004/swagger-ui.html`
- order 服务：`http://localhost:9005/swagger-ui.html`

网关聚合的 OpenAPI JSON：

- `/api-docs/auth`
- `/api-docs/poi`
- `/api-docs/content`
- `/api-docs/mall`
- `/api-docs/order`

## 本地启动

1. 在 `deploy` 目录启动 MySQL、Redis、Nacos、RocketMQ 等基础组件。
2. 依次启动 `geotrack-gateway`、`geotrack-auth-service`、`geotrack-poi-service`、`geotrack-content-service`、`geotrack-mall-service`、`geotrack-order-service`。
3. 访问 `http://localhost:8080/api/auth/health` 验证网关转发。
4. 使用 `scripts/curl/mainline-demo.ps1`、`scripts/curl/mainline-demo.sh` 或 Postman 集合验证主链路。
