#!/bin/bash

echo "======================================"
echo "  快速登录测试"
echo "======================================"
echo ""

# 等待后端启动
echo "等待后端服务..."
sleep 2

# 测试登录
echo "测试登录账号: yinyongchao"
RESULT=$(curl -s -X POST http://localhost:8089/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"yinyongchao","password":"1qaz2wsx!"}')

echo "响应结果:"
echo "$RESULT" | python3 -m json.tool 2>/dev/null || echo "$RESULT"

echo ""
if echo "$RESULT" | grep -q "\"code\":200"; then
    echo "✅ 登录成功！"
else
    echo "❌ 登录失败"
    echo ""
    echo "可能原因："
    echo "1. 用户不存在 → 先注册该用户"
    echo "2. 密码错误 → 检查密码"
    echo "3. 后端未启动 → mvn spring-boot:run"
fi

echo ""
echo "======================================"
