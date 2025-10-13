# 会员功能说明

## 会员体系

### 免费用户（温馨体验）
- **守护名额**: 3 位亲友
- **提醒方式**:
  - ✅ 微信小程序通知
  - ✅ 邮件提醒
- **费用**: 免费

### VIP会员（守护礼遇）
- **守护名额**: 20 位亲友
- **提醒方式**:
  - ✅ 微信小程序通知
  - ✅ 邮件提醒
  - ✅ 短信提醒（生日当天）
- **费用**: ¥10/年
- **有效期**: 1 年

## 提醒机制说明

### 1. 小程序通知
- **适用人群**: 所有用户
- **通知时机**: 提前 N 天（用户自定义）
- **通知方式**: 微信小程序订阅消息
- **实现说明**:
  - 用户需在小程序内授权订阅消息
  - 支持一次性订阅和长期订阅（需要用户手动授权）

### 2. 邮件提醒
- **适用人群**: 所有用户
- **通知时机**: 提前 N 天（用户自定义）
- **通知方式**: 系统自动发送邮件
- **内容**: 暖心文案，包含生日人姓名、日期等信息

### 3. 短信提醒
- **适用人群**: 仅 VIP 会员
- **通知时机**: 生日当天
- **通知方式**: 发送短信到角色联系电话
- **内容**: 根据用户填写的备注内容发送祝福短信

## 小程序订阅消息实现

### 订阅消息模板

需要在微信小程序后台申请以下订阅消息模板：

**模板 1：生日提醒**
```
角色姓名：{{thing1.DATA}}
生日日期：{{date2.DATA}}
提前天数：{{number3.DATA}}
温馨提示：{{thing4.DATA}}
```

**模板 2：生日当天祝福**
```
角色姓名：{{thing1.DATA}}
祝福语：{{thing2.DATA}}
```

### 订阅流程

1. **用户授权**
   - 用户首次使用或添加生日时，请求订阅消息权限
   - 用户同意后，可接收通知

2. **发送通知**
   - 后端定时任务检查生日
   - 调用微信 API 发送订阅消息
   - 记录发送日志

### 小程序端实现

```javascript
// 请求订阅消息授权
wx.requestSubscribeMessage({
  tmplIds: ['template-id-1', 'template-id-2'],
  success: (res) => {
    // 用户同意授权
    console.log('订阅成功', res)
  },
  fail: (err) => {
    // 用户拒绝授权
    console.log('订阅失败', err)
  }
})
```

### 后端实现

需要在 `WeChatTemplateService` 中添加发送小程序订阅消息的方法：

```java
public boolean sendMiniprogramSubscribeMessage(
    User user,
    BirthdayRole role,
    LocalDate upcomingBirthday,
    long daysUntilBirthday
) {
    // 获取用户的 openid
    UserWechat userWechat = userWechatService.getByUserId(user.getId());

    // 构建订阅消息
    WxMaSubscribeMessage message = WxMaSubscribeMessage.builder()
        .toUser(userWechat.getOpenid())
        .templateId("your-template-id")
        .page("pages/birthdays/list")
        .build();

    // 添加数据
    message.addData(new WxMaSubscribeMessage.Data("thing1", role.getRoleName()));
    message.addData(new WxMaSubscribeMessage.Data("date2", upcomingBirthday.toString()));
    message.addData(new WxMaSubscribeMessage.Data("number3", String.valueOf(daysUntilBirthday)));
    message.addData(new WxMaSubscribeMessage.Data("thing4", "记得准备礼物哦~"));

    // 发送消息
    try {
        wxMaService.getMsgService().sendSubscribeMsg(message);
        return true;
    } catch (WxErrorException e) {
        logger.error("发送小程序订阅消息失败", e);
        return false;
    }
}
```

## 会员订阅功能

### 支付方式

推荐使用微信支付：

1. **微信支付配置**
   ```yaml
   wechat:
     pay:
       app-id: your-appid
       mch-id: your-mch-id
       mch-key: your-mch-key
       notify-url: https://your-domain.com/api/pay/notify
   ```

2. **支付流程**
   - 用户在小程序中选择升级 VIP
   - 调用后端创建订单
   - 后端调用微信支付统一下单 API
   - 返回支付参数给小程序
   - 小程序调用 `wx.requestPayment` 发起支付
   - 支付成功后回调通知后端
   - 后端更新用户会员状态

### 订单管理

建议添加订单表：

```sql
CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_type` VARCHAR(20) NOT NULL COMMENT '产品类型：VIP',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PAID/CANCELLED',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付交易号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

### VIP 续费提醒

系统应在 VIP 到期前提醒用户续费：

- **提前 7 天**: 发送邮件和小程序通知提醒即将到期
- **到期当天**: 自动降级为免费用户
- **到期后**: 保留所有数据，但功能降级

## 短信功能说明

### 短信服务配置

```yaml
sms:
  config:
    config-type: YAML
    is-print: false
  blends:
    default:
      supplier: alibaba  # 或使用其他短信服务商
      access-key-id: ${SMS_ACCESS_KEY}
      access-key-secret: ${SMS_ACCESS_SECRET}
      template-id: ${SMS_TEMPLATE_ID}
      template-name: content
      signature: 生日守护者
      region-id: cn-hangzhou
```

### 短信模板示例

```
【生日守护者】{备注内容}。今天是{角色姓名}的生日，祝TA生日快乐！
```

### 短信发送逻辑

1. 系统定时任务检查今天是否有生日
2. 检查��户是否为 VIP
3. 检查角色是否填写了联系电话
4. 发送短信到角色的联系电话
5. 记录发送日志

## 数据库变更

无需额外变更，现有表结构已支持：

- `user` 表的 `membership_level` 和 `vip_expire_time` 字段
- `birthday_role` 表的 `role_phone` 字段
- `notification_log` 表记录所有通知日志

## 小程序页面调整

### 1. 会员中心页面

添加会员升级入口：

```xml
<view class="vip-upgrade" wx:if="{{membershipLevel === 'FREE'}}">
  <button class="btn-upgrade" bindtap="handleUpgrade">
    <text class="upgrade-icon">👑</text>
    <view class="upgrade-info">
      <text class="upgrade-title">升级VIP会员</text>
      <text class="upgrade-price">仅需 ¥10/年</text>
    </view>
  </button>

  <view class="vip-benefits">
    <text class="benefits-title">VIP特权</text>
    <view class="benefit-item">
      <text class="benefit-icon">✅</text>
      <text class="benefit-text">守护20位亲友</text>
    </view>
    <view class="benefit-item">
      <text class="benefit-icon">✅</text>
      <text class="benefit-text">生日当天短信提醒</text>
    </view>
    <view class="benefit-item">
      <text class="benefit-icon">✅</text>
      <text class="benefit-text">所有免费功能</text>
    </view>
  </view>
</view>
```

### 2. 支付页面

创建支付页面 `pages/pay/pay`:

```javascript
// pages/pay/pay.js
Page({
  data: {
    productType: 'VIP',
    price: 10,
    duration: '1年'
  },

  handlePay() {
    // 调用后端创建订单
    wx.request({
      url: `${app.globalData.apiBase}/api/order/create`,
      method: 'POST',
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`
      },
      data: {
        productType: 'VIP'
      },
      success: (res) => {
        if (res.data.code === 200) {
          // 发起微信支付
          wx.requestPayment({
            ...res.data.data,
            success: () => {
              wx.showToast({ title: '支付成功', icon: 'success' })
              setTimeout(() => {
                wx.navigateBack()
              }, 1500)
            },
            fail: (err) => {
              wx.showToast({ title: '支付失败', icon: 'none' })
            }
          })
        }
      }
    })
  }
})
```

## 实现优先级

### 第一阶段（已完成）
- ✅ 基础会员体系
- ✅ 名额限制
- ✅ 邮件提醒
- ✅ 短信提醒（VIP）

### 第二阶段（建议实现）
- 📋 小程序订阅消息
- 📋 微信支付集成
- 📋 订单管理
- 📋 VIP 续费提醒

### 第三阶段（可选）
- 📋 支付统计
- 📋 收入报表
- 📋 优惠券系统
- 📋 会员等级扩展

## 技术支持

如需实现支付功能或订阅消息，请参考：

- [微信支付开发文档](https://pay.weixin.qq.com/wiki/doc/apiv3/index.shtml)
- [小程序订阅消息](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/subscribe-message.html)
- [微信支付小程序接入](https://pay.weixin.qq.com/wiki/doc/apiv3/open/pay/chapter2_8_1.shtml)

联系方式：
- 邮箱：yinyc0925@outlook.com
