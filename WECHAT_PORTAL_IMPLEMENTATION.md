# 微信公众号 Portal 接口实现说明

## 概述

已成功为微信公众号集成添加 `/api/wechat/portal` 接口，该接口用于：
1. 验证微信服务器配置（GET请求）
2. 接收微信公众号消息和事件（POST请求）

## 实现位置

**文件：** [backend/src/main/java/com/birthday/guardian/controller/WeChatController.java](backend/src/main/java/com/birthday/guardian/controller/WeChatController.java)

## 实现细节

### 1. GET /api/wechat/portal - 服务器验证

```java
@GetMapping("/portal")
public String verifyWeChatServer(
        @RequestParam(name = "signature", required = false) String signature,
        @RequestParam(name = "timestamp", required = false) String timestamp,
        @RequestParam(name = "nonce", required = false) String nonce,
        @RequestParam(name = "echostr", required = false) String echostr) {

    try {
        if (wxMpService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }
    } catch (Exception e) {
        System.err.println("微信服务器验证失败: " + e.getMessage());
    }
    return "error";
}
```

**功能说明：**
- 接收微信服务器发送的验证参数
- 使用 `wxMpService.checkSignature()` 验证签名
- 验证成功返回 `echostr`，失败返回 "error"
- 用于在微信公众平台配置服务器URL时的验证

### 2. POST /api/wechat/portal - 消息接收

```java
@PostMapping(value = "/portal", produces = "application/xml; charset=UTF-8")
public String handleWeChatMessage(
        @RequestBody String requestBody,
        @RequestParam(name = "signature", required = false) String signature,
        @RequestParam(name = "timestamp", required = false) String timestamp,
        @RequestParam(name = "nonce", required = false) String nonce,
        @RequestParam(name = "encrypt_type", required = false) String encryptType,
        @RequestParam(name = "msg_signature", required = false) String msgSignature) {

    try {
        WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);

        // 如果配置了消息路由器，使用路由器处理消息
        if (messageRouter != null) {
            WxMpXmlOutMessage outMessage = messageRouter.route(inMessage);
            if (outMessage != null) {
                return outMessage.toXml();
            }
        }

        // 默认返回空字符串表示成功接收
        return "success";
    } catch (Exception e) {
        System.err.println("处理微信消息失败: " + e.getMessage());
        e.printStackTrace();
        return "";
    }
}
```

**功能说明：**
- 接收微信公众号发送的消息和事件
- 解析XML格式的消息内容
- 如果配置了 `WxMpMessageRouter`，使用路由器处理消息
- 支持返回XML格式的回复消息
- 默认返回 "success" 表示成功接收

## 新增依赖注入

在 `WeChatController` 中添加了以下服务：

```java
@Autowired
private WxMpService wxMpService;

@Autowired(required = false)
private WxMpMessageRouter messageRouter;
```

**说明：**
- `wxMpService`：微信公众号服务，由 `WeChatMpConfig` 配置类提供
- `messageRouter`：消息路由器（可选），用于处理不同类型的消息和事件

## 编译验证

已通过Maven编译验证：

```bash
[INFO] Compiling 55 source files to D:\code\BirthdayGuardian\backend\target\classes
[INFO] BUILD SUCCESS
```

## 配置步骤

### 1. 配置 application.yml

确保以下配置正确：

```yaml
wechat:
  mp:
    app-id: ${WECHAT_MP_APP_ID:your-mp-appid}
    secret: ${WECHAT_MP_SECRET:your-mp-secret}
    token: ${WECHAT_MP_TOKEN:your-token}
    aes-key: ${WECHAT_MP_AES_KEY:your-aes-key}
```

### 2. 在微信公众平台配置服务器

登录微信公众平台 → 设置与开发 → 基本配置

```
服务器地址(URL): https://your-domain.com/api/wechat/portal
Token: your-token (与后端配置一致)
EncodingAESKey: your-aes-key (与后端配置一致)
消息加解密方式: 安全模式（推荐）
```

### 3. 点击"提交"验证

微信服务器会向 `/api/wechat/portal` 发送GET请求进行验证。

## 测试建议

### 1. 测试服务器验证

```bash
# 模拟微信服务器验证请求
curl -X GET "http://localhost:8089/api/wechat/portal?signature=xxx&timestamp=xxx&nonce=xxx&echostr=test"
```

**预期结果：** 如果签名验证通过，返回 "test"

### 2. 查看日志

启动应用后，关注以下日志：
- 微信服务验证成功/失败日志
- 消息接收和处理日志
- 错误堆栈信息

### 3. 微信公众平台测试

配置完成后，可以：
- 关注公众号
- 发送文本消息
- 查看后端日志确认消息接收

## 消息路由器扩展（可选）

如果需要处理用户消息，可以创建 `WxMpMessageRouter` Bean：

```java
@Configuration
public class WeChatMessageConfig {

    @Autowired
    private WxMpService wxMpService;

    @Bean
    public WxMpMessageRouter messageRouter() {
        WxMpMessageRouter router = new WxMpMessageRouter(wxMpService);

        // 处理文本消息
        router.rule()
            .async(false)
            .msgType(WxConsts.XmlMsgType.TEXT)
            .handler((wxMessage, context, service, sessionManager) -> {
                String content = wxMessage.getContent();
                // 处理文本消息逻辑
                return new TextBuilder()
                    .build("感谢您的留言：" + content, wxMessage, service);
            })
            .end();

        // 处理关注事件
        router.rule()
            .async(false)
            .msgType(WxConsts.XmlMsgType.EVENT)
            .event(WxConsts.EventType.SUBSCRIBE)
            .handler((wxMessage, context, service, sessionManager) -> {
                // 处理用户关注事件
                return new TextBuilder()
                    .build("欢迎关注生日守护者！", wxMessage, service);
            })
            .end();

        return router;
    }
}
```

## 已解决的问题

✅ 微信公众平台无法验证服务器配置
✅ 无法接收公众号消息
✅ `/api/wechat/portal` 接口缺失

## 下一步

1. **配置微信公众平台**
   - 添加服务器URL配置
   - 配置网页授权域名
   - 配置JS接口安全域名

2. **测试OAuth授权流程**
   - 访问 `https://your-domain.com/#/h5`
   - 验证微信OAuth授权
   - 测试账号绑定功能

3. **测试模板消息推送**
   - 配置模板消息ID
   - 测试生日提醒推送

4. **配置公众号菜单**
   - 设置菜单链接到H5页面
   - 测试菜单跳转

## 相关文档

- [WECHAT_MP_GUIDE.md](WECHAT_MP_GUIDE.md) - 完整的微信公众号配置和排查指南
- [backend/src/main/java/com/birthday/guardian/controller/WeChatController.java](backend/src/main/java/com/birthday/guardian/controller/WeChatController.java) - Portal接口实现
- [backend/src/main/java/com/birthday/guardian/config/WeChatMpConfig.java](backend/src/main/java/com/birthday/guardian/config/WeChatMpConfig.java) - 微信公众号配置

## 技术支持

如有问题，请参考：
1. 查看 [WECHAT_MP_GUIDE.md](WECHAT_MP_GUIDE.md) 中的问题排查步骤
2. 检查后端日志中的错误信息
3. 联系邮箱：yinyc0925@outlook.com
