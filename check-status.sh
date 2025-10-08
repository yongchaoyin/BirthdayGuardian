#!/bin/bash

echo "======================================"
echo "  服务状态检查"
echo "======================================"
echo ""

# 检查后端
if lsof -Pi :8089 -sTCP:LISTEN -t >/dev/null 2>&1; then
    BACKEND_PID=$(lsof -ti:8089)
    echo "✅ 后端服务运行中"
    echo "   端口: 8089"
    echo "   PID: $BACKEND_PID"
else
    echo "❌ 后端服务未运行"
fi

echo ""

# 检查前端
if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
    FRONTEND_PID=$(lsof -ti:5173)
    echo "✅ 前端服务运行中"
    echo "   端口: 5173"
    echo "   PID: $FRONTEND_PID"
else
    echo "❌ 前端服务未运行"
fi

echo ""

# 检查后端API
echo "🔍 测试后端API..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8089/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"test","password":"test"}' 2>/dev/null)

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "500" ]; then
    echo "✅ 后端API响应正常 (HTTP $HTTP_CODE)"
else
    echo "❌ 后端API无响应 (HTTP $HTTP_CODE)"
fi

echo ""
echo "======================================"
echo "📱 访问地址："
echo "   前端: http://localhost:5173"
echo "   后端: http://localhost:8089"
echo ""
echo "📋 查看日志："
echo "   后端: tail -f backend.log"
echo "   前端: tail -f frontend.log"
echo ""
echo "🛑 停止服务："
echo "   后端: kill \$(lsof -ti:8089)"
echo "   前端: kill \$(lsof -ti:5173)"
echo "======================================"
