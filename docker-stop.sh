#!/bin/bash

echo "======================================"
echo "  生日守护者系统 - Docker停止脚本"
echo "======================================"
echo ""

echo "🛑 停止所有服务..."
docker-compose down

echo ""
echo "✅ 所有服务已停止"
echo ""
echo "💡 提示："
echo "   - 如需删除数据卷: docker-compose down -v"
echo "   - 如需删除镜像: docker-compose down --rmi all"
echo "======================================"
