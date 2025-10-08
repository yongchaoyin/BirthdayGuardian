#!/bin/bash

echo "======================================"
echo "  生日守护者系统 - Docker启动脚本"
echo "======================================"
echo ""

# 检查.env文件是否存在
if [ ! -f .env ]; then
    echo "⚠️  未找到.env配置文件"
    echo "📝 正在复制.env.example到.env..."
    cp .env.example .env
    echo ""
    echo "⚠️  重要提醒："
    echo "   请编辑.env文件，配置你的邮箱信息："
    echo "   - SPRING_MAIL_USERNAME: 你的邮箱地址"
    echo "   - SPRING_MAIL_PASSWORD: 你的邮箱授权码"
    echo ""
    read -p "按Enter继续或Ctrl+C退出进行配置..."
fi

# 停止旧容器
echo "🛑 停止旧容器..."
docker-compose down

# 构建并启动服务
echo ""
echo "🔨 构建Docker镜像..."
docker-compose build

echo ""
echo "🚀 启动服务..."
docker-compose up -d

# 等待服务启动
echo ""
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo ""
echo "📊 检查服务状态..."
docker-compose ps

echo ""
echo "======================================"
echo "✅ 服务启动完成！"
echo "======================================"
echo ""
echo "📱 访问地址："
echo "   前端: http://localhost"
echo "   后端API: http://localhost:8080"
echo "   MySQL: localhost:3306"
echo ""
echo "📋 查看日志："
echo "   所有服务: docker-compose logs -f"
echo "   后端: docker-compose logs -f backend"
echo "   前端: docker-compose logs -f frontend"
echo "   MySQL: docker-compose logs -f mysql"
echo ""
echo "🛑 停止服务: docker-compose down"
echo "======================================"
