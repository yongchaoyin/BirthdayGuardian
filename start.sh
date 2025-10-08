#!/bin/bash

echo "正在启动生日守护者系统..."

# 检查MySQL是否运行
if ! docker ps | grep -q mysql; then
    echo "MySQL未运行，正在启动..."
    docker start qingni-mysql
    sleep 5
fi

# 启动后端
echo "正在启动后端服务..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!

# 等待后端启动
sleep 10

# 启动前端
echo "正在启动前端服务..."
cd ../frontend
npm run dev &
FRONTEND_PID=$!

echo "系统启动完成！"
echo "前端地址：http://localhost:5173"
echo "后端地址：http://localhost:8080"
echo ""
echo "按 Ctrl+C 停止所有服务"

# 等待中断信号
trap "kill $BACKEND_PID $FRONTEND_PID; exit" INT

wait
