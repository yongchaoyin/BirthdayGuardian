# 微信公众号配置和问题排查指南

## 问题：公众号始终看不到正确页面

### 常见原因

1. **公众号菜单链接配置错误**
2. **OAuth授权域名未配置**
3. **服务器URL配置错误**
4. **前端路由问题**
5. **微信公众号参数配置错误**

---

## 正确的配置流程

### 1. 后端配置

#### application.yml 配置
```yaml
wechat:
  mp:
    app-id: ${WECHAT_MP_APP_ID:your-mp-appid}
    secret: ${WECHAT_MP_SECRET:your-mp-secret}
    token: ${WECHAT_MP_TOKEN:your-token}
    aes-key: ${WECHAT_MP_AES_KEY:your-aes-key}
    birthday-template-id: ${WECHAT_MP_BIRTHDAY_TEMPLATE_ID:}
    reminder-url: ${WECHAT_MP_REMINDER_URL:https://your-domain.com/h5}
```

**重要说明：**
- `app-id`: 公众号的 AppID
- `secret`: 公众号的 AppSecret
- `token`: 与公众号后台配置的 Token 一致
- `aes-key`: 消息加解密密钥
- `reminder-url`: **这是关键！** 用户点击公众号消息后跳转的页面

### 2. 微信公众平台配置

#### 步骤 1：配置服务器地址
登录微信公众平台 → 设置与开发 → 基本配置

```
服务器地址(URL): https://your-domain.com/api/wechat/portal
Token: your-token (与后端配置一致)
EncodingAESKey: your-aes-key (与后端配置一致)
消息加解密方式: 安全模式（推荐）
```

**注意：** 目前后端缺少 `/api/wechat/portal` 接口，需要添加！

#### 步骤 2：配置网页授权域名
设置与开发 → 公众号设置 → 功能设置 → 网页授权域名

```
your-domain.com
```

**注意事项：**
- 不要加 `https://`
- 不要加端口号
- 不要加路径
- 域名必须备案

#### 步骤 3：配置 JS 接口安全域名
设置与开发 → 公众号设置 → 功能设置 → JS接口安全域名

```
your-domain.com
```

### 3. 公众号菜单配置

#### 方式 1：在公众号后台配置
管理 → 菜单管理 → 添加菜单

**菜单类型：** 跳转网页
**页面地址：**
```
https://your-domain.com/#/h5
```

或使用 Hash 模式：
```
https://your-domain.com/index.html#/h5
```

#### 方式 2：使用 API 配置（推荐）
```bash
curl -X POST "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN" \
  -d '{
    "button": [
      {
        "type": "view",
        "name": "生日守护",
        "url": "https://your-domain.com/#/h5"
      }
    ]
  }'
```

---

## ✅ 后端代码已完成

### `/api/wechat/portal` 接口已实现

`WeChatController.java` 已包含公众号消息接收接口：

```java
package com.birthday.guardian.controller;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wechat")
public class WeChatController {

    @Autowired
    private WxMpService wxMpService;

    // 验证服务器配置
    @GetMapping("/portal")
    public String checkSignature(
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {

        if (wxMpService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }
        return "error";
    }

    // 接收公众号消息
    @PostMapping(value = "/portal",
                 produces = "application/xml; charset=UTF-8")
    public String handleMessage(
            @RequestBody String requestBody,
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam(value = "encrypt_type", required = false) String encryptType,
            @RequestParam(value = "msg_signature", required = false) String msgSignature) {

        try {
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            WxMpXmlOutMessage outMessage = wxMpService.getMessageRouter()
                .route(inMessage);

            if (outMessage != null) {
                return outMessage.toXml();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // ... 其他已有的方法 ...
}
```

---

## 前端访问路径说明

### 正确的访问路径

1. **PC端访问：**
   ```
   https://your-domain.com
   ```
   自动跳转到桌面版 `/home`

2. **手机浏览器访问：**
   ```
   https://your-domain.com
   ```
   自动跳转到手机版 `/h5`

3. **微信内访问（公众号）：**
   ```
   https://your-domain.com/#/h5
   ```
   显示手机版页面，并自动触发OAuth授权

### 路由自动跳转逻辑

根据 `router/index.js` 的配置：
- 检测到移动设备 → 跳转到 `/h5`
- 检测到PC设备 → 跳转到 `/home`
- 微信内置浏览器 → 触发OAuth授权

---

## 问题排查步骤

### 1. 检查是否能访问H5页面

**测试URL：**
```
https://your-domain.com/#/h5
```

**预期结果：**
- 未登录：跳转到登录页 `/login`
- 已登录：显示手机版首页

**如果无法访问：**
- 检查前端是否正确部署
- 检查 Nginx 配置
- 查看浏览器控制台错误

### 2. 检查OAuth授权流程

在微信内打开：
```
https://your-domain.com/#/h5
```

**预期流程：**
1. 页面加载
2. 检测到未绑定微信
3. 自动跳转到微信OAuth授权页
4. 用户同意授权
5. 跳回 `/h5?code=xxx&state=xxx`
6. 后端绑定微信账号
7. 显示页面内容

**如果授权失败：**
- 检查 `reminder-url` 配置
- 检查网页授权域名配置
- 查看后端日志

### 3. 检查微信公众号配置

#### 测试 OAuth URL生成
```bash
curl -X GET "https://your-domain.com/api/wechat/oauth-url?redirect=https://your-domain.com/%23/h5" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期返回：**
```json
{
  "code": 200,
  "data": {
    "url": "https://open.weixin.qq.com/connect/oauth2/authorize?..."
  }
}
```

#### 测试微信绑定
```bash
curl -X POST "https://your-domain.com/api/wechat/bind" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"code": "微信返回的code"}'
```

### 4. 检查前端配置

**文件：** `frontend/src/utils/request.js`

确保 API 基础路径配置正确：
```javascript
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
```

**文件：** `frontend/.env`

```env
VITE_API_BASE_URL=https://your-domain.com/api
```

---

## 调试技巧

### 1. 浏览器控制台
打开微信开发者工具或 Chrome 调试模式：
- 查看 Network 请求
- 查看 Console 错误
- 查看 Application → Local Storage

### 2. 微信开发者工具
使用微信开发者工具打开公众号页面：
- 工具 → 公众号网页调试
- 输入URL并登录
- 查看请求和响应

### 3. 后端日志
```bash
# Docker部署
docker-compose logs -f backend

# 直接部署
tail -f logs/birthday-guardian.log
```

查找关键词：
- `wechat`
- `oauth`
- `bind`
- `error`

---

## 常见错误和解决方案

### 错误 1：redirect_uri参数错误
**现象：** 微信提示"redirect_uri参数错误"

**原因：** 网页授权域名未配置或配置错误

**解决：**
1. 检查公众号后台的网页授权域名配置
2. 确保域名与后端 `reminder-url` 一致
3. 域名不要加 `https://` 和路径

### 错误 2：页面一直白屏
**现象：** 打开公众号菜单后一直白屏

**原因：**
- 前端未部署或部署错误
- Nginx 配置错误
- 路由配置错误

**解决：**
1. 直接在浏览器访问 URL 测试
2. 检查 Nginx 配置
3. 查看浏览器控制台错误

### 错误 3：OAuth授权后跳转错误
**现象：** 授权后跳转到错误页面

**原因：** `redirect_uri` 参数配置错误

**解决：**
检查 `MobileHome.vue` 中的代码：
```javascript
const redirect = window.location.href.split('#')[0]
```

确保 redirect 参数正确构建。

### 错误 4：绑定失败
**现象：** 提示"微信绑定失败"

**原因：**
- Token 过期或无效
- 后端配置错误
- 微信 code 已使用

**解决：**
1. 检查 Token 是否有效
2. 查看后端日志
3. 确保 code 只使用一次

---

## 完整的配置检查清单

### 后端配置
- [ ] `wechat.mp.app-id` 配置正确
- [ ] `wechat.mp.secret` 配置正确
- [ ] `wechat.mp.token` 配置正确
- [ ] `wechat.mp.aes-key` 配置正确
- [ ] `wechat.mp.reminder-url` 配置正确
- [x] 已添加 `/api/wechat/portal` 接口 (WeChatController.java)
- [ ] 服务正常运行

### 微信公众平台配置
- [ ] 服务器URL配置正确
- [ ] Token与后端一致
- [ ] EncodingAESKey与后端一致
- [ ] 服务器配置已启用
- [ ] 网页授权域名已配置
- [ ] JS接口安全域名已配置
- [ ] 公众号菜单链接正确

### 前端配置
- [ ] 前端已正确部署
- [ ] Nginx 配置正确
- [ ] API 地址配置正确
- [ ] 路由配置正确
- [ ] `/h5` 页面可访问

### 域名和SSL
- [ ] 域名已备案
- [ ] SSL 证书有效
- [ ] HTTPS 可正常访问
- [ ] 域名解析正确

---

## 推荐的公众号菜单配置

### 简单配置（一个菜单）
```json
{
  "button": [
    {
      "type": "view",
      "name": "生日守护",
      "url": "https://your-domain.com/#/h5"
    }
  ]
}
```

### 完整配置（多个菜单）
```json
{
  "button": [
    {
      "type": "view",
      "name": "守护列表",
      "url": "https://your-domain.com/#/h5"
    },
    {
      "name": "更多功能",
      "sub_button": [
        {
          "type": "view",
          "name": "个人中心",
          "url": "https://your-domain.com/#/profile"
        },
        {
          "type": "view",
          "name": "关于我们",
          "url": "https://your-domain.com"
        }
      ]
    },
    {
      "type": "click",
      "name": "联系客服",
      "key": "CONTACT"
    }
  ]
}
```

---

## 技术支持

如果按照以上步骤仍然无法解决问题：

1. **收集信息：**
   - 浏览器控制台截图
   - 后端日志
   - 访问的具体URL
   - 错误提示信息

2. **联系支持：**
   - 邮箱：yinyc0925@outlook.com
   - 提供详细的问题描述

3. **临时方案：**
   - 使用小程序代替公众号
   - 直接分享H5链接给用户
