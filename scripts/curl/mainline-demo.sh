#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"

echo "1) 登录并获取 token"
LOGIN_RESP=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800000001","password":"123456"}')
echo "${LOGIN_RESP}"

TOKEN=$(echo "${LOGIN_RESP}" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
if [ -z "${TOKEN}" ]; then
  echo "未获取到 token，请检查 auth 服务"
  exit 1
fi
AUTH_HEADER="Authorization: Bearer ${TOKEN}"

echo "2) 创建 POI"
POI_RESP=$(curl -s -X POST "${BASE_URL}/api/poi" \
  -H "Content-Type: application/json" \
  -d '{"name":"西湖断桥","longitude":120.1500,"latitude":30.2800,"radiusMeters":800,"rewardPoints":20,"description":"演示打卡点"}')
echo "${POI_RESP}"

POI_ID=$(echo "${POI_RESP}" | sed -n 's/.*"data":\([0-9]*\).*/\1/p')
if [ -z "${POI_ID}" ]; then
  echo "未获取到 poiId，请检查 poi 服务"
  exit 1
fi

echo "3) 发起打卡（用户身份来自 Authorization）"
curl -s -X POST "${BASE_URL}/api/checkin" \
  -H "Content-Type: application/json" \
  -H "${AUTH_HEADER}" \
  -d "{\"poiId\":${POI_ID},\"longitude\":120.1501,\"latitude\":30.2801,\"imageUrl\":\"https://example.com/a.jpg\"}"
echo

echo "4) 重复打卡（应提示今日已打卡）"
curl -s -X POST "${BASE_URL}/api/checkin" \
  -H "Content-Type: application/json" \
  -H "${AUTH_HEADER}" \
  -d "{\"poiId\":${POI_ID},\"longitude\":120.1501,\"latitude\":30.2801,\"imageUrl\":\"https://example.com/a.jpg\"}"
echo
