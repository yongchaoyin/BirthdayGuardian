#!/bin/bash

echo "======================================"
echo "  登录问题诊断脚本"
echo "======================================"
echo ""

# 检查后端是否运行
echo "1. 检查后端服务..."
if curl -s http://localhost:8089/api/auth/login > /dev/null 2>&1; then
    echo "   ✅ 后端服务运行正常 (端口8089)"
else
    echo "   ❌ 后端服务无响应"
    echo "   请确认后端已启动: mvn spring-boot:run"
fi

echo ""
echo "2. 检查数据库连接..."
if docker ps | grep -q birthday-guardian-mysql; then
    echo "   ✅ MySQL容器运行中"
else
    echo "   ⚠️  MySQL容器未运行"
fi

echo ""
echo "3. 测试注册接口..."
REGISTER_RESULT=$(curl -s -X POST http://localhost:8089/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser123","email":"test123@example.com","password":"123456"}')
echo "   响应: $REGISTER_RESULT"

echo ""
echo "4. 测试登录接口..."
LOGIN_RESULT=$(curl -s -X POST http://localhost:8089/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"testuser123","password":"123456"}')
echo "   响应: $LOGIN_RESULT"

echo ""
echo "======================================"
echo "常见问题解决："
echo "1. 用户不存在 → 先注册账号"
echo "2. 密码错误 → 检查密码是否正确"
echo "3. 网络错误 → 确认后端端口8089"
echo "4. 数据库错误 → 检查MySQL是否运行"
echo "======================================"
echo ""
echo "查看详细日志:"
echo "  后端: docker-compose logs -f backend"
echo "  前端: 打开浏览器F12控制台"
echo "======================================"
