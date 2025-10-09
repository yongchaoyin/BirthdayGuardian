<template>
  <div class="profile-container">
    <el-container>
      <el-header class="header">
        <h2>个人中心</h2>
        <div class="header-actions">
          <el-button @click="goBack" size="small">返回首页</el-button>
          <el-button @click="handleLogout" size="small" type="danger">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <el-row :gutter="20">
          <!-- 用户信息卡片 -->
          <el-col :span="24" :md="8">
            <el-card class="user-info-card">
              <template #header>
                <div class="card-header">
                  <span>用户信息</span>
                </div>
              </template>
              <div class="user-info">
                <div class="info-item">
                  <label>用户名：</label>
                  <span>{{ userInfo.username }}</span>
                </div>
                <div class="info-item">
                  <label>邮箱：</label>
                  <span>{{ userInfo.email }}</span>
                </div>
                <div class="info-item">
                  <label>手机号：</label>
                  <span>{{ userInfo.phone || '未填写' }}</span>
                </div>
                <div class="info-item">
                  <label>会员等级：</label>
                  <span :class="['membership-tag', userInfo.vipActive ? 'vip' : 'free']">{{ membershipLabel }}</span>
                </div>
                <div class="info-item">
                  <label>守护名额：</label>
                  <span>
                    {{ maxRoleDisplay }} 位亲友
                    <template v-if="inviteBonus > 0">（含推广奖励 +{{ inviteBonus }}）</template>
                  </span>
                </div>
                <div class="info-item">
                  <label>VIP到期：</label>
                  <span>{{ vipExpireDisplay }}</span>
                </div>
                <p class="membership-tip">
                  温馨提示：VIP有效期一年，若到期我们会自动切换至温馨体验计划，您可随时联系管理员续订。
                </p>
                <p class="membership-tip contact">
                  开通或续订 VIP？请邮件联系站主：<a href="mailto:yinyc0925@outlook.com" target="_blank">yinyc0925@outlook.com</a>
                </p>
                <el-divider />
                <el-form ref="usernameFormRef" :model="usernameForm" :rules="usernameRules" label-width="80px" class="inline-form">
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="usernameForm.username" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleUpdateUsername" :loading="usernameLoading">保存用户名</el-button>
                  </el-form-item>
                </el-form>
                <el-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-width="80px" class="inline-form">
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="phoneForm.phone" placeholder="用于接收通知及短信" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleUpdatePhone" :loading="phoneLoading">保存手机号</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-card>
            <el-card ref="promoCardRef" :class="['promo-card', { highlight: promoHighlight }]">
              <template #header>
                <div class="card-header">
                  <span>推广任务</span>
                </div>
              </template>
              <div class="promo-content">
                <div class="promo-code-block">
                  <label>我的推广码</label>
                  <div class="code-row">
                    <span class="code-badge">{{ userInfo.inviteCode || '生成中…' }}</span>
                    <el-button size="small" plain type="primary" @click="copyInviteCode" :disabled="!userInfo.inviteCode">
                      复制推广码
                    </el-button>
                  </div>
                </div>
                <div class="promo-link-block" v-if="referralLink">
                  <label>专属注册链接</label>
                  <div class="link-row">
                    <el-input :model-value="referralLink" readonly />
                    <el-button size="small" plain type="primary" @click="copyInviteLink">复制链接</el-button>
                  </div>
                </div>
                <div class="promo-stats">
                  <div class="stat-item">
                    <strong>{{ inviteBonus }}</strong>
                    <span>额外名额</span>
                  </div>
                  <div class="stat-item">
                    <strong>{{ userInfo.inviteSuccessCount || 0 }}</strong>
                    <span>成功邀请</span>
                  </div>
                </div>
                <ul class="promo-rules">
                  <li>分享推广码或注册链接，好友注册时填写即可记录邀请。</li>
                  <li>每成功邀请 1 位好友注册并激活账号，守护名额永久 +1。</li>
                  <li>当前总名额：{{ maxRoleDisplay }} 位（基础 {{ baseQuota }} 位 + 推广奖励 {{ inviteBonus }} 位）。</li>
                </ul>
                <p class="promo-tip">小提示：好友注册后可在首页-守护名单中实时看到增加的守护额度。</p>
              </div>
            </el-card>
          </el-col>

          <!-- 修改密码卡片 -->
          <el-col :span="24" :md="8">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>修改密码</span>
                </div>
              </template>
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <!-- 修改邮箱卡片 -->
          <el-col :span="24" :md="8">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>修改邮箱</span>
                </div>
              </template>
              <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" label-width="100px">
                <el-form-item label="当前邮箱">
                  <el-input :value="userInfo.email" disabled />
                </el-form-item>
                <el-form-item label="新邮箱" prop="email">
                  <el-input v-model="emailForm.email" placeholder="请输入新邮箱" />
                </el-form-item>
                <el-form-item label="确认密码" prop="password">
                  <el-input v-model="emailForm.password" type="password" placeholder="请输入密码确认" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateEmail" :loading="emailLoading">修改邮箱</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo, changePassword, updateEmail, updatePhone, updateUsername } from '../api/user'

const router = useRouter()
const route = useRoute()
const userInfo = ref({})
const passwordFormRef = ref(null)
const emailFormRef = ref(null)
const usernameFormRef = ref(null)
const phoneFormRef = ref(null)
const passwordLoading = ref(false)
const emailLoading = ref(false)
const usernameLoading = ref(false)
const phoneLoading = ref(false)
const promoCardRef = ref(null)
const promoHighlight = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const emailForm = reactive({
  email: '',
  password: ''
})

const usernameForm = reactive({
  username: ''
})

const phoneForm = reactive({
  phone: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const usernameRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20之间', trigger: 'blur' }
  ]
}

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' }
  ]
}

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data || {}
    userInfo.value.inviteSuccessCount = userInfo.value.inviteSuccessCount ?? 0
    userInfo.value.inviteBonus = userInfo.value.inviteBonus ?? userInfo.value.inviteSuccessCount ?? 0
    // 同时更新localStorage中的用户信息
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    usernameForm.username = res.data.username || ''
    phoneForm.phone = res.data.phone || ''
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

const membershipLabel = computed(() => (userInfo.value.vipActive ? 'VIP守护礼遇' : '温馨体验计划'))

const inviteBonus = computed(() => {
  if (!userInfo.value) {
    return 0
  }
  if (typeof userInfo.value.inviteBonus === 'number') {
    return userInfo.value.inviteBonus
  }
  if (typeof userInfo.value.inviteSuccessCount === 'number') {
    return userInfo.value.inviteSuccessCount
  }
  return 0
})

const baseQuota = computed(() => (userInfo.value && userInfo.value.vipActive ? 20 : 3))

const maxRoleDisplay = computed(() => {
  if (!userInfo.value) {
    return 3
  }
  if (typeof userInfo.value.maxRoleCount === 'number') {
    return userInfo.value.maxRoleCount
  }
  return baseQuota.value + inviteBonus.value
})

const referralLink = computed(() => {
  if (!userInfo.value || !userInfo.value.inviteCode) {
    return ''
  }
  return `${window.location.origin}/register?code=${userInfo.value.inviteCode}`
})

const vipExpireDisplay = computed(() => {
  if (!userInfo.value.vipExpireTime) {
    return '—'
  }
  return new Date(userInfo.value.vipExpireTime).toLocaleString('zh-CN')
})

const handleUpdateUsername = async () => {
  try {
    await usernameFormRef.value.validate()
    usernameLoading.value = true
    await updateUsername({ username: usernameForm.username })
    ElMessage.success('用户名更新成功')
    await loadUserInfo()
  } catch (error) {
    if (error?.response) {
      ElMessage.error(error.response.data.message || '更新用户名失败')
    } else if (error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    usernameLoading.value = false
  }
}

const handleUpdatePhone = async () => {
  try {
    await phoneFormRef.value.validate()
    phoneLoading.value = true
    await updatePhone({ phone: phoneForm.phone })
    ElMessage.success('手机号更新成功')
    await loadUserInfo()
  } catch (error) {
    if (error?.response) {
      ElMessage.error(error.response.data.message || '更新手机号失败')
    } else if (error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    phoneLoading.value = false
  }
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    setTimeout(() => {
      handleLogout()
    }, 1500)
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '修改密码失败')
    } else if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    passwordLoading.value = false
  }
}

const handleUpdateEmail = async () => {
  try {
    await emailFormRef.value.validate()
    emailLoading.value = true
    await updateEmail({
      email: emailForm.email,
      password: emailForm.password
    })
    ElMessage.success('邮箱修改成功')
    emailForm.email = ''
    emailForm.password = ''
    await loadUserInfo()
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '修改邮箱失败')
    } else if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    emailLoading.value = false
  }
}

const goBack = () => {
  router.push('/home')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

const focusPromoCard = () => {
  if (route.query.focus !== 'promo') {
    return
  }
  nextTick(() => {
    const target = promoCardRef.value?.$el || promoCardRef.value
    if (target && typeof target.scrollIntoView === 'function') {
      target.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
    promoHighlight.value = true
    window.setTimeout(() => {
      promoHighlight.value = false
    }, 2000)
  })
}

const copyText = async (text, successMessage) => {
  if (!text) {
    ElMessage.warning('暂无可复制的内容')
    return
  }
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(successMessage)
  } catch (error) {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      ElMessage.success(successMessage)
    } catch (copyError) {
      ElMessage.error('复制失败，请手动操作')
    } finally {
      document.body.removeChild(textarea)
    }
  }
}

const copyInviteCode = () => {
  copyText(userInfo.value.inviteCode, '推广码已复制，感谢分享！')
}

const copyInviteLink = () => {
  copyText(referralLink.value, '推广链接已复制，快去邀请好友吧！')
}

onMounted(async () => {
  await loadUserInfo()
  focusPromoCard()
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  padding: 48px 0;
  background: var(--bg-gradient);
}

.header {
  background: var(--surface-glass);
  border-bottom: 1px solid rgba(255, 255, 255, 0.62);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 28px;
  margin: 0 36px;
  border-radius: 26px 26px 10px 10px;
  box-shadow: var(--shadow-elevated);
  backdrop-filter: blur(16px);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.card-header {
  font-weight: bold;
}


.user-info-card,
.promo-card,
.el-card {
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 22px 46px rgba(184, 162, 255, 0.2);
  backdrop-filter: blur(14px);
}

.user-info-card .user-info {
  padding: 20px 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.info-item label {
  font-weight: bold;
  color: #606266;
  min-width: 80px;
}

.info-item span {
  color: #303133;
}

.membership-tag {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  display: inline-block;
  background: #f0f4ff;
  color: #5468ff;
}

.membership-tag.vip {
  background: #fff3e0;
  color: #ff8c42;
}

.membership-tag.free {
  background: #eef7ff;
  color: #409eff;
}

.membership-tip {
  margin: 10px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.membership-tip.contact a {
  color: #409eff;
}


.promo-card {
  margin-top: 20px;
  border-radius: 32px;
  background: var(--surface-strong);
  border: 1px solid rgba(255, 255, 255, 0.68);
  box-shadow: var(--shadow-elevated);
  backdrop-filter: blur(14px);
}

.promo-card.highlight {
  box-shadow: 0 0 0 3px rgba(240, 139, 103, 0.35), 0 18px 38px rgba(202, 186, 255, 0.36);
}

.promo-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.promo-content label {
  font-weight: 600;
  color: #5b4742;
  font-size: 14px;
}

.code-row,
.link-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.code-badge {
  padding: 10px 18px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(255, 225, 206, 0.9), rgba(255, 241, 227, 0.9));
  border: 1px solid rgba(255, 255, 255, 0.8);
  font-family: var(--font-heading);
  font-size: 16px;
  letter-spacing: 1px;
  color: #4a332f;
}

.promo-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  flex: 1;
  padding: 16px 14px;
  border-radius: 22px;
  background: rgba(255, 247, 242, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.78);
  text-align: center;
  box-shadow: 0 14px 30px rgba(240, 139, 103, 0.18);
}

.stat-item strong {
  display: block;
  font-size: 20px;
  color: #f08b67;
  font-family: var(--font-heading);
}

.stat-item span {
  font-size: 12px;
  color: #7a6660;
}

.promo-rules {
  margin: 4px 0 0;
  padding-left: 18px;
  color: #6a5550;
  line-height: 1.8;
  font-size: 13px;
}

.promo-rules li::marker {
  color: var(--primary-color);
}

.promo-tip {
  margin: -6px 0 0;
  font-size: 12px;
  color: rgba(99, 78, 72, 0.7);
}

.promo-link-block .link-row {
  flex-wrap: nowrap;
}

:deep(.promo-link-block .el-input__wrapper) {
  border-radius: 14px;
}

:deep(.promo-link-block .el-input) {
  flex: 1;
}

:deep(.promo-link-block .el-input__inner) {
  font-size: 13px;
  color: #5a4742;
}

@media (max-width: 1024px) {
  .header {
    margin: 0 24px;
  }
}

@media (max-width: 768px) {
  .profile-container {
    padding: 32px 0;
  }

  .header {
    margin: 0 16px;
    border-radius: 20px;
    padding: 14px 20px;
  }

  .promo-card {
    margin-top: 14px;
  }
}

.inline-form {
  margin-top: 10px;
}

.inline-form .el-form-item {
  margin-bottom: 10px;
}
</style>
