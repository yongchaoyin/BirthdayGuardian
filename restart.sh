#!/bin/bash

echo "======================================"
echo "  生日守护者系统 - 重启脚本"
echo "======================================"
echo ""

# 检查部署方式
if [ -f "docker-compose.yml" ] && command -v docker-compose &> /dev/null; then
    echo "检测到Docker环境"
    echo ""
    read -p "使用Docker重启？[Y/n] " -n 1 -r
    echo

    if [[ $REPLY =~ ^[Yy]$ ]] || [[ -z $REPLY ]]; then
        echo ""
        echo "🔄 停止Docker服务..."
        docker-compose down

        echo ""
        echo "🔨 重新构建镜像..."
        docker-compose build --no-cache backend

        echo ""
        echo "🚀 启动服务..."
        docker-compose up -d

        echo ""
        echo "⏳ 等待服务启动..."
        sleep 10

        echo ""
        echo "📊 检查服务状态..."
        docker-compose ps

        echo ""
        echo "✅ Docker服务已重启"
        echo ""
        echo "📱 访问地址："
        echo "   前端: http://localhost"
        echo "   后端: http://localhost:8089"
        echo ""
        echo "📋 查看日志: docker-compose logs -f"

        exit 0
    fi
fi

echo ""
echo "💻 本地开发模式重启"
echo ""
echo "请按以下步骤操作："
echo ""
echo "1️⃣  停止后端服务"
echo "   - 切换到运行后端的终端"
echo "   - 按 Ctrl+C 停止"
echo ""
echo "2️⃣  重新启动后端"
echo "   cd /Users/yongchao/Code/BirthdayGuardian-1/backend"
echo "   mvn spring-boot:run"
echo ""
echo "3️⃣  停止前端服务（如果在运行）"
echo "   - 切换到运行前端的终端"
echo "   - 按 Ctrl+C 停止"
echo ""
echo "4️⃣  重新启动前端"
echo "   cd /Users/yongchao/Code/BirthdayGuardian-1/frontend"
echo "   npm run dev"
echo ""
echo "======================================"
echo ""
read -p "是否自动启动本地开发服务？[y/N] " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "🚀 启动后端服务..."
    cd backend

    # 检查并停止占用8089端口的进程
    if lsof -Pi :8089 -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "端口8089被占用，尝试停止..."
        lsof -ti:8089 | xargs kill -9 2>/dev/null
        sleep 2
    fi

    echo "启动后端..."
    echo "后端日志将在新终端中显示"

    # 在新终端启动后端
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        osascript -e "tell app \"Terminal\" to do script \"cd '$PWD' && mvn spring-boot:run\""
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux
        if command -v gnome-terminal &> /dev/null; then
            gnome-terminal -- bash -c "cd '$PWD' && mvn spring-boot:run; exec bash"
        elif command -v xterm &> /dev/null; then
            xterm -e "cd '$PWD' && mvn spring-boot:run" &
        else
            echo "请手动在新终端运行: cd backend && mvn spring-boot:run"
        fi
    fi

    echo ""
    echo "⏳ 等待后端启动（10秒）..."
    sleep 10

    cd ../frontend

    # 检查并停止占用5173端口的进程
    if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "端口5173被占用，尝试停止..."
        lsof -ti:5173 | xargs kill -9 2>/dev/null
        sleep 2
    fi

    echo ""
    echo "🎨 启动前端服务..."

    # 在新终端启动前端
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        osascript -e "tell app \"Terminal\" to do script \"cd '$PWD' && npm run dev\""
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux
        if command -v gnome-terminal &> /dev/null; then
            gnome-terminal -- bash -c "cd '$PWD' && npm run dev; exec bash"
        elif command -v xterm &> /dev/null; then
            xterm -e "cd '$PWD' && npm run dev" &
        else
            echo "请手动在新终端运行: cd frontend && npm run dev"
        fi
    fi

    echo ""
    echo "✅ 服务启动中..."
    echo ""
    echo "📱 访问地址："
    echo "   前端: http://localhost:5173"
    echo "   后端: http://localhost:8089"
    echo ""
    echo "💡 提示: 服务在新终端窗口中运行"
    echo "   关闭终端窗口即可停止服务"
else
    echo ""
    echo "请手动重启服务"
fi

echo ""
echo "======================================"
