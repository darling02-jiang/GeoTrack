$ErrorActionPreference = "Stop"

$BaseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { "http://127.0.0.1:8080" }

Write-Host "1) 登录并获取 token"
$loginBody = @{
  phone = "13800000001"
  password = "123456"
} | ConvertTo-Json
$loginResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/login" -ContentType "application/json" -Body $loginBody
$loginResp | ConvertTo-Json -Depth 10
$token = $loginResp.data.token
if (-not $token) { throw "未获取到 token，请检查 auth 服务" }
$authHeader = @{ Authorization = "Bearer $token" }

Write-Host "2) 创建 POI"
$poiBody = @{
  name = "西湖断桥"
  longitude = 120.1500
  latitude = 30.2800
  radiusMeters = 800
  rewardPoints = 20
  description = "演示打卡点"
} | ConvertTo-Json
$poiResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/poi" -ContentType "application/json" -Body $poiBody
$poiResp | ConvertTo-Json -Depth 10
$poiId = $poiResp.data
if (-not $poiId) { throw "未获取到 poiId，请检查 poi 服务" }

Write-Host "3) 发起打卡（用户身份来自 Authorization）"
$checkinBody = @{
  poiId = $poiId
  longitude = 120.1501
  latitude = 30.2801
  imageUrl = "https://example.com/a.jpg"
} | ConvertTo-Json
$checkinResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/checkin" -Headers $authHeader -ContentType "application/json" -Body $checkinBody
$checkinResp | ConvertTo-Json -Depth 10

Write-Host "4) 重复打卡（应提示今日已打卡）"
$checkinResp2 = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/checkin" -Headers $authHeader -ContentType "application/json" -Body $checkinBody
$checkinResp2 | ConvertTo-Json -Depth 10
