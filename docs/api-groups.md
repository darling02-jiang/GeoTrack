# GeoTrack API 分组

> Base URL：`http://localhost:8080`（网关）
>
> 当前认证方式：Redis Session + Cookie。需要登录的接口必须携带浏览器 `SESSION` Cookie；需要幂等保护的写接口还需要携带 `X-Idempotency-Key`。

## 用户与认证（auth-service）

- `POST /api/auth/code/send`：发送登录验证码
- `POST /api/auth/login`：手机号验证码登录
- `GET /api/auth/me`：获取当前登录用户
- `GET /api/auth/token/resolve`：解析当前 Session 为 `userId`
- `GET /api/auth/health`：认证服务健康检查

### 内部接口

- `POST /api/auth/internal/checkin-points`：服务间发放打卡积分，需要 `X-GeoTrack-Internal-Token`
- `POST /api/auth/internal/mall-points-deduct`：服务间扣减商城订单积分，需要 `X-GeoTrack-Internal-Token`

## POI 与打卡（poi-service）

- `POST /api/poi`：创建 POI，需要管理员登录态
- `GET /api/poi/list`：POI 列表
- `GET /api/poi/nearby`：Redis GEO 附近 POI 查询
- `PATCH /api/poi/{id}/status`：启用或停用 POI，需要管理员登录态
- `POST /api/checkin`：提交打卡，需要 `SESSION` Cookie 和 `X-Idempotency-Key`
- `GET /api/checkin/my-summary`：我的月度打卡汇总
- `GET /api/checkin/my-recent`：我的最近打卡记录

## 动态与互动（content-service）

- `GET /api/feed/recent`：全站最新动态
- `GET /api/feed/poi/{poiId}`：POI 圈子动态
- `GET /api/feed/hot`：热门动态榜
- `POST /api/feed/internal/create`：内部创建打卡动态
- `POST /api/like/toggle`：点赞或取消点赞，需要 `SESSION` Cookie 和 `X-Idempotency-Key`
- `POST /api/comment`：发表评论，需要 `SESSION` Cookie

## 商城与秒杀（mall-service）

- `GET /api/mall/goods`：上架商品列表
- `GET /api/mall/goods/{id}`：商品详情
- `POST /api/mall/exchange`：普通积分兑换，需要 `SESSION` Cookie 和 `X-Idempotency-Key`
- `POST /api/seckill/order`：秒杀下单，需要 `SESSION` Cookie 和 `X-Idempotency-Key`

## 订单查询（order-service）

- `GET /api/order/health`：订单服务健康检查
- `GET /api/order/my`：我的订单列表，需要 `SESSION` Cookie

## OpenAPI 文档

- 网关聚合 Swagger UI：`http://localhost:8080/swagger-ui.html`
- auth 文档 JSON：`http://localhost:8080/api-docs/auth`
- poi 文档 JSON：`http://localhost:8080/api-docs/poi`
- content 文档 JSON：`http://localhost:8080/api-docs/content`
- mall 文档 JSON：`http://localhost:8080/api-docs/mall`
- order 文档 JSON：`http://localhost:8080/api-docs/order`
