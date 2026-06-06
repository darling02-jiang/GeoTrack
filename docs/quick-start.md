# 快速启动

## 1. 启动基础组件

在 `deploy` 目录执行：

```bash
docker compose up -d
```

基础组件包括 MySQL、Redis、Nacos 和 RocketMQ。

## 2. 启动服务

建议按以下顺序启动：

1. `geotrack-gateway`
2. `geotrack-auth-service`
3. `geotrack-poi-service`
4. `geotrack-content-service`
5. `geotrack-mall-service`
6. `geotrack-order-service`

## 3. 验证服务

- 网关健康验证：`GET http://localhost:8080/api/auth/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- 登录态验证：`GET http://localhost:8080/api/auth/me`，需要浏览器携带 `SESSION` Cookie
- POI 打卡：`POST http://localhost:8080/api/checkin`，需要 `SESSION` Cookie 和 `X-Idempotency-Key`
- 秒杀下单：`POST http://localhost:8080/api/seckill/order`，需要 `SESSION` Cookie 和 `X-Idempotency-Key`
- 订单查询：`GET http://localhost:8080/api/order/my`

## 4. 主链路测试脚本

- Bash：`scripts/curl/mainline-demo.sh`
- PowerShell：`scripts/curl/mainline-demo.ps1`
- Postman：`scripts/postman/geotrack-mainline.postman_collection.json`

## 5. 接口文档入口

- 网关聚合：`http://localhost:8080/swagger-ui.html`
- auth：`http://localhost:9001/swagger-ui.html`
- poi：`http://localhost:9002/swagger-ui.html`
- content：`http://localhost:9003/swagger-ui.html`
- mall：`http://localhost:9004/swagger-ui.html`
- order：`http://localhost:9005/swagger-ui.html`
