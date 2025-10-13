# 快速启动指南

## 问题：小程序启动失败，提示图片找不到

### 原因
小程序配置中引用了 TabBar 图标和默认头像图片，但这些图片文件不存在。

### 解决方案
已修改配置，移除了对图片文件的依赖：

1. **TabBar 图标** - 使用纯文字 TabBar（无图标）
2. **用户头像** - 使用文字头像（显示用户名首字）

### 修改内容

#### 1. app.json
- 移除了 `iconPath` 和 `selectedIconPath` 配置
- TabBar 现在使用纯文字显示
- 选中颜色改为紫色 (#667eea) 以匹配主题

#### 2. pages/profile/profile.wxml
- 将 `<image>` 标签改为 `<view>` 标签
- 显示用户名的首字母作为头像

#### 3. pages/profile/profile.wxss
- 头像背景改为渐变色
- 添加文字样式

## 现在可以正常启动

小程序现在可以正常启动了，不再依赖任何图片文件。

### 启动步骤

1. **使用微信开发者工具打开项目**
   ```
   打开目录：D:\code\BirthdayGuardian\miniprogram
   ```

2. **配置 AppID**
   - 如果没有 AppID，选择"测试号"或"不校验合法域名"
   - 在 project.config.json 中将 appid 改为你的小程序 AppID

3. **配置后端地址**
   - 打开 `app.js`
   - 修改 `apiBase` 为你的后端地址
   - 开发环境：`http://localhost:8089`
   - 生产环境：`https://your-domain.com`

4. **点击编译**
   - 小程序应该可以正常启动
   - 首次进入会跳转到登录页

### 界面效果

#### TabBar
- 首页 | 生日管理 | 我的
- 选中时文字变为紫色
- 简洁美观的纯文字风格

#### 个人中心头像
- 圆形渐变背景（紫色）
- 显示用户名首字
- 无需图片文件

## 可选：添加图标

如果将来想要添加 TabBar 图标，可以：

### 方法1：使用 iconfont 字体图标
```javascript
// 在 tabBar 的 text 中使用 iconfont
"text": "🏠 首页"
```

### 方法2：准备图片文件
1. 在 `miniprogram/images/` 目录创建以下文件：
   - tab-home.png (81×81)
   - tab-home-active.png (81×81)
   - tab-birthday.png (81×81)
   - tab-birthday-active.png (81×81)
   - tab-profile.png (81×81)
   - tab-profile-active.png (81×81)

2. 恢复 app.json 中的 iconPath 配置：
```json
{
  "pagePath": "pages/index/index",
  "iconPath": "images/tab-home.png",
  "selectedIconPath": "images/tab-home-active.png",
  "text": "首页"
}
```

### 图标设计建议
- 使用简单的线条风格
- 未选中：灰色 (#7A7E83)
- 选中：紫色 (#667eea)
- 尺寸：81×81 像素
- 格式：PNG（支持透明背景）

## 测试清单

启动后请测试以下功能：

- [ ] 小程序正常启动，无错误提示
- [ ] 登录页面显示正常
- [ ] TabBar 三个标签可以切换
- [ ] 微信登录功能正常（需要配置后端）
- [ ] 个人中心头像显示正常

## 常见问题

### 1. 小程序还是无法启动
- 检查 app.json 语法是否正确
- 检查所有页面路径是否存在
- 查看控制台错误信息

### 2. 接口请求失败
- 确认后端服务已启动
- 检查 app.js 中的 apiBase 配置
- 开发阶段勾选"不校验合法域名"

### 3. 登录失败
- 确认后端已配置小程序 AppID 和 Secret
- 查看后端日志获取详细错误信息

## 下一步

1. **配置后端**
   - 在后端 application.yml 配置小程序参数
   - 启动后端服务

2. **测试功能**
   - 测试登录
   - 测试生日管理
   - 测试用户信息修改

3. **部署上线**
   - 参考 [DEPLOYMENT.md](DEPLOYMENT.md)
   - 配置服务器域名
   - 提交审核

## 技术支持

如遇问题：
- 查看 [README.md](README.md) 完整文档
- 查看 [DEPLOYMENT.md](DEPLOYMENT.md) 部署指南
- 联系：yinyc0925@outlook.com
