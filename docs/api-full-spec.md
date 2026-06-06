# GeoTrack API 文档（当前实现版）

本文档按当前 Controller 与前端实际调用整理，只记录已经在代码中实现的接口。

## 1. 通用约定

- Base URL: `http://localhost:8080`
- 网关：`geotrack-gateway`
- 返回结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

- 成功：`code = 0`
- 失败：`code = -1`，`message` 为业务错误信息，`data = null`
- 当前认证方式：Redis Session + Cookie，不是 JWT。
- 浏览器端请求需要携带 Cookie；`fetch` 使用 `credentials: "include"`。
- 需要幂等保护的写接口需要请求头 `X-Idempotency-Key`。
- 时间字段由 Spring Boot 默认 JSON 序列化输出，通常为 ISO-8601 格式。

## 2. 认证与用户

### 2.1 发送登录验证码

- Method: `POST`
- Path: `/api/auth/code/send`
- Auth: 不需要登录
- Body:

```json
{
  "phone": "13800000001"
}
```

- Response `data`:

```json
"验证码发送成功"
```

说明：当前配置支持 mock 验证码，默认验证码为 `1234`。

### 2.2 手机号验证码登录

- Method: `POST`
- Path: `/api/auth/login`
- Auth: 不需要登录，但必须先调用 `/api/auth/code/send` 创建 Session
- Body:

```json
{
  "phone": "13800000001",
  "code": "1234"
}
```

- Response `data`:

```json
{
  "userId": 1,
  "nickname": "用户0001",
  "phone": "13800000001"
}
```

说明：登录成功后服务端写入 Redis Session，浏览器通过 `SESSION` Cookie 保持会话。

### 2.3 当前登录用户

- Method: `GET`
- Path: `/api/auth/me`
- Auth: Cookie Session

- Response `data`:

```json
{
  "id": 1,
  "phone": "13800000001",
  "passwordHash": null,
  "nickname": "用户0001",
  "avatarUrl": null,
  "profile": null,
  "pointsBalance": 100,
  "status": 1
}
```

### 2.4 解析当前会话用户 ID

- Method: `GET`
- Path: `/api/auth/token/resolve`
- Auth: Cookie Session

- Response `data`:

```json
1
```

说明：业务服务通过该接口把 Cookie Session 解析为 `userId`。

### 2.5 认证服务健康检查

- Method: `GET`
- Path: `/api/auth/health`
- Auth: 不需要登录

- Response `data`:

```json
"auth-service-ok"
```

## 3. 认证内部接口

内部接口需要请求头：

```http
X-GeoTrack-Internal-Token: dev-internal-token
```

### 3.1 发放打卡积分

- Method: `POST`
- Path: `/api/auth/internal/checkin-points`
- Auth: 内部服务令牌
- Body:

```json
{
  "userId": 1,
  "checkInRecordId": 1001,
  "points": 10
}
```

- Response `data`:

```json
"积分发放成功"
```

### 3.2 扣减商城订单积分

- Method: `POST`
- Path: `/api/auth/internal/mall-points-deduct`
- Auth: 内部服务令牌
- Body:

```json
{
  "userId": 1,
  "orderNo": "GT202605270001",
  "points": 30
}
```

- Response `data`:

```json
"积分扣减成功"
```

## 4. POI

### 4.1 创建 POI

- Method: `POST`
- Path: `/api/poi`
- Auth: Cookie Session + 管理员权限
- Body:

```json
{
  "name": "西湖断桥",
  "longitude": 120.1501,
  "latitude": 30.2801,
  "radiusMeters": 500,
  "rewardPoints": 10,
  "description": "经典打卡点",
  "status": 1
}
```

- Response `data`:

```json
1
```

说明：`status = 1` 的 POI 会同步写入 Redis GEO。

### 4.2 POI 列表

- Method: `GET`
- Path: `/api/poi/list`
- Auth: 不需要登录

- Response `data`:

```json
[
  {
    "id": 1,
    "name": "西湖断桥",
    "longitude": 120.1501,
    "latitude": 30.2801,
    "radiusMeters": 500,
    "rewardPoints": 10,
    "description": "经典打卡点",
    "status": 1
  }
]
```

### 4.3 附近 POI 查询

- Method: `GET`
- Path: `/api/poi/nearby`
- Auth: 不需要登录
- Query:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `longitude` | number | 是 | - | 用户经度 |
| `latitude` | number | 是 | - | 用户纬度 |
| `radiusMeters` | number | 否 | `3000` | 搜索半径，单位米 |
| `limit` | number | 否 | `20` | 返回数量 |

- Example:

```http
GET /api/poi/nearby?longitude=120.15&latitude=30.28&radiusMeters=3000&limit=20
```

- Response `data`:

```json
[
  {
    "id": 1,
    "name": "西湖断桥",
    "distanceMeters": 128.6,
    "longitude": 120.1501,
    "latitude": 30.2801,
    "radiusMeters": 500,
    "rewardPoints": 10,
    "status": 1,
    "description": "经典打卡点"
  }
]
```

### 4.4 更新 POI 状态

- Method: `PATCH`
- Path: `/api/poi/{id}/status`
- Auth: Cookie Session + 管理员权限
- Body:

```json
{
  "status": 0
}
```

- Response `data`:

```json
null
```

说明：`status` 仅支持 `0`（停用）或 `1`（启用）。启用会写入 Redis GEO，停用会从 Redis GEO 移除。

## 5. 打卡

### 5.1 提交打卡

- Method: `POST`
- Path: `/api/checkin`
- Auth: Cookie Session
- Headers:

```http
X-Idempotency-Key: checkin-1-2026-05-27
```

- Body:

```json
{
  "poiId": 1,
  "longitude": 120.1501,
  "latitude": 30.2801,
  "content": "今天完成打卡",
  "imageUrl": "https://example.com/checkin.jpg"
}
```

- Response `data`:

```json
"打卡成功，获得 10 积分"
```

说明：

- 打卡距离优先使用 Redis GEO `GEODIST` 计算，失败时回退本地 Haversine。
- 同一用户同一 POI 同一天只能成功打卡一次。
- 打卡成功后通过 RocketMQ 异步发放积分并生成动态。

### 5.2 我的月度打卡汇总

- Method: `GET`
- Path: `/api/checkin/my-summary`
- Auth: Cookie Session
- Query:

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `year` | number | 是 | 年份 |
| `month` | number | 是 | 月份，1-12 |

- Example:

```http
GET /api/checkin/my-summary?year=2026&month=5
```

- Response `data`:

```json
{
  "checkedDates": ["2026-05-01", "2026-05-02"],
  "distinctPoiCount": 2,
  "totalSuccessCount": 8
}
```

### 5.3 我的最近打卡记录

- Method: `GET`
- Path: `/api/checkin/my-recent`
- Auth: Cookie Session
- Query:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `limit` | number | 否 | `50` | 返回数量，服务端会限制在 1-100 |

- Response `data`:

```json
[
  {
    "id": 1001,
    "userId": 1,
    "poiId": 1,
    "result": "success",
    "createdAt": "2026-05-27T10:00"
  }
]
```

## 6. 动态与互动

### 6.1 全站最新动态

- Method: `GET`
- Path: `/api/feed/recent`
- Auth: 不需要登录
- Query:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `limit` | number | 否 | `30` | 返回数量 |

- Response `data`:

```json
[
  {
    "id": 1,
    "checkInRecordId": 1001,
    "userId": 1,
    "poiId": 1,
    "content": "今天完成打卡",
    "imageUrl": "https://example.com/checkin.jpg",
    "likeCount": 3,
    "commentCount": 1,
    "hotScore": 120,
    "createdAt": "2026-05-27T10:00:00"
  }
]
```

### 6.2 POI 圈子动态

- Method: `GET`
- Path: `/api/feed/poi/{poiId}`
- Auth: 不需要登录
- Query:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `sort` | string | 否 | `latest` | `latest` 按时间；`likes` 按点赞 |
| `limit` | number | 否 | `30` | 返回数量 |

- Example:

```http
GET /api/feed/poi/1?sort=latest&limit=40
```

- Response `data`:

```json
[
  {
    "id": 1,
    "userId": 1,
    "poiId": 1,
    "content": "今天完成打卡",
    "imageUrl": "https://example.com/checkin.jpg",
    "likeCount": 3,
    "commentCount": 1,
    "hotScore": 120,
    "createdAt": "2026-05-27T10:00:00",
    "commentPreview": [
      {
        "id": 11,
        "userId": 2,
        "content": "拍得真好",
        "createdAt": "2026-05-27T10:05:00"
      }
    ]
  }
]
```

### 6.3 热门动态榜

- Method: `GET`
- Path: `/api/feed/hot`
- Auth: 不需要登录
- Query:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `poiId` | number | 否 | - | 指定 POI 时返回该圈子热门；不传则全站热门 |
| `limit` | number | 否 | `10` | 返回数量 |

- Response `data`:

```json
[
  {
    "id": 1,
    "userId": 1,
    "poiId": 1,
    "content": "今天完成打卡",
    "imageUrl": "https://example.com/checkin.jpg",
    "likeCount": 3,
    "commentCount": 1,
    "hotScore": 120,
    "createdAt": "2026-05-27T10:00:00"
  }
]
```

### 6.4 内部创建打卡动态

- Method: `POST`
- Path: `/api/feed/internal/create`
- Auth: 当前 Controller 未校验内部令牌，建议仅服务间调用
- Body:

```json
{
  "checkInRecordId": 1001,
  "userId": 1,
  "poiId": 1,
  "content": "今天完成打卡",
  "imageUrl": "https://example.com/checkin.jpg"
}
```

- Response `data`:

```json
1
```

说明：当前主链路主要通过 RocketMQ 消费打卡动态消息生成动态，该 HTTP 内部接口用于兼容或服务间调用。

### 6.5 点赞/取消点赞

- Method: `POST`
- Path: `/api/like/toggle`
- Auth: Cookie Session
- Headers:

```http
X-Idempotency-Key: like-1-1-2026-05-27T10:00:00
```

- Body:

```json
{
  "feedId": 1
}
```

- Response `data`:

```json
{
  "liked": true,
  "likeCount": 4
}
```

### 6.6 发表评论

- Method: `POST`
- Path: `/api/comment`
- Auth: Cookie Session
- Body:

```json
{
  "feedId": 1,
  "content": "拍得真好"
}
```

- Response `data`:

```json
{
  "id": 11,
  "userId": 2,
  "content": "拍得真好",
  "createdAt": "2026-05-27T10:05:00"
}
```

## 7. 商城与秒杀

### 7.1 商品列表

- Method: `GET`
- Path: `/api/mall/goods`
- Auth: 不需要登录

- Response `data`:

```json
[
  {
    "id": 1,
    "name": "咖啡券",
    "pointsPrice": 30,
    "stock": 100,
    "seckill": false,
    "beginTime": null,
    "endTime": null
  }
]
```

### 7.2 商品详情

- Method: `GET`
- Path: `/api/mall/goods/{id}`
- Auth: 不需要登录

- Response `data`:

```json
{
  "id": 1,
  "name": "咖啡券",
  "pointsPrice": 30,
  "stock": 100,
  "seckill": false,
  "beginTime": null,
  "endTime": null
}
```

### 7.3 普通积分兑换

- Method: `POST`
- Path: `/api/mall/exchange`
- Auth: Cookie Session
- Headers:

```http
X-Idempotency-Key: exchange-1-2026-05-27T10:00:00
```

- Body:

```json
{
  "goodsId": 1
}
```

- Response `data`:

```json
{
  "orderNo": "GT202605270001",
  "status": "PAID"
}
```

说明：普通兑换为同步扣积分与落库订单。重复提交同一幂等键会返回缓存的订单号。

### 7.4 秒杀下单

- Method: `POST`
- Path: `/api/seckill/order`
- Auth: Cookie Session
- Headers:

```http
X-Idempotency-Key: seckill-1-2026-05-27T10:00:00
```

- Body:

```json
{
  "goodsId": 2
}
```

- Response `data`:

```json
{
  "orderNo": "GT202605270002",
  "status": "PENDING"
}
```

说明：

- 秒杀入口使用 Redis Lua 完成库存预扣与单用户限购。
- 下单请求成功后投递 RocketMQ，异步创建订单并扣积分。
- 积分扣减失败时会取消订单，并补偿 Redis 秒杀库存与用户限购标记。

## 8. 订单查询

### 8.1 订单服务健康检查

- Method: `GET`
- Path: `/api/order/health`
- Auth: 不需要登录

- Response `data`:

```json
"order-service-ok"
```

### 8.2 我的订单列表

- Method: `GET`
- Path: `/api/order/my`
- Auth: Cookie Session
- Query:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `limit` | number | 否 | `50` | 返回数量 |

- Response `data`:

```json
[
  {
    "orderNo": "GT202605270001",
    "goodsId": 1,
    "pointsCost": 30,
    "status": "PAID",
    "createdAt": "2026-05-27T10:00:00"
  }
]
```

## 9. 状态与错误说明

### 9.1 通用错误

```json
{
  "code": -1,
  "message": "未登录或会话已过期",
  "data": null
}
```

### 9.2 常见业务错误

- `手机号格式不正确`
- `请求过于频繁，请N秒后重试`
- `验证码已过期，请重新获取`
- `验证码不正确`
- `未登录或会话已过期`
- `缺少认证信息`
- `接口限流，请稍后再试`
- `请勿重复提交`
- `今日已完成打卡`
- `未在打卡范围内`
- `积分不足`
- `商品不存在或已下架`
- `库存不足`
- `该商品不支持秒杀`
- `秒杀尚未开始`
- `秒杀已结束`
- `您已参与过该秒杀活动`

## 10. 当前未实现接口说明

旧版文档中出现过但当前 Controller 未实现的接口如下，不应作为当前联调 API 使用：

- `POST /api/auth/logout`
- `POST /api/auth/refresh-token`
- `PUT /api/auth/me`
- `GET /api/poi/{id}`
- `PUT /api/poi/{id}`
- `GET /api/checkin/records`
- `/api/file/**`
- `POST /api/feed/publish`
- `GET /api/feed/list`
- `GET /api/feed/{id}`
- `POST /api/like`
- `POST /api/seckill`
- `GET /api/seckill/activities`
- `GET /api/seckill/result/{requestId}`
- `GET /api/order/{orderNo}`
- `GET /api/order/user/list`
- `/api/ops/**`
