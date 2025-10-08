#!/bin/bash

echo "======================================"
echo "  端口更新后的快速重启脚本"
echo "======================================"
echo ""
echo "✅ 已更新的配置："
echo "   - 后端端口: 8080 → 8089"
echo "   - 前端API地址: http://localhost:8089/api"
echo "   - Nginx代理: backend:8089"
echo "   - Docker端口映射: 8089:8089"
echo ""

read -p "是否要重启服务？[y/N] " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    echo ""
    echo "🔄 重启服务中..."

    # 如果是Docker部署
    if [ -f "docker-compose.yml" ]; then
        echo "📦 检测到Docker部署，重启容器..."
        docker-compose down
        docker-compose up -d --build

        echo ""
        echo "✅ Docker服务已重启"
        echo "   - 前端: http://localhost"
        echo "   - 后端: http://localhost:8089"

    else
        echo "💻 本地部署模式"
        echo "请手动执行："
        echo "   1. 后端: cd backend && mvn spring-boot:run"
        echo "   2. 前端: cd frontend && npm run dev"
    fi

    echo ""
    echo "📊 查看日志: docker-compose logs -f"
else
    echo ""
    echo "⚠️  请注意手动重启服务以应用更改"
fi

echo ""
echo "======================================"
