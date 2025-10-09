<template>
  <div class="mobile-home">
    <div class="mobile-ambient ambient-sunrise"></div>
    <div class="mobile-ambient ambient-dawn"></div>
    <div class="mobile-ambient ambient-blossom"></div>
    <header class="mobile-header">
      <div class="header-info">
        <h1>生日守护者</h1>
        <p class="welcome">Hi {{ userInfo.username || '守护者' }}，欢迎回来</p>
      </div>
      <el-button type="danger" text @click="handleLogout">退出</el-button>
    </header>

    <section class="membership-card" :class="{ vip: membershipInfo.vipActive }">
      <div class="membership-title">
        <span>{{ membershipLabel }}</span>
        <el-tag :type="membershipInfo.vipActive ? 'success' : 'warning'" size="small">{{ membershipInfo.vipActive ? 'VIP' : 'FREE' }}</el-tag>
      </div>
      <p class="membership-message">{{ membershipMessage }}</p>
      <p class="membership-slots">还能添加 {{ availableSlots }} 位亲友</p>
    </section>

    <section v-if="announcements.length" class="announcement-section">
      <h2>最新公告</h2>
      <div class="announcement-list">
        <el-card v-for="announcement in announcements" :key="announcement.id" class="announcement-item" shadow="never">
          <div class="announcement-header">
            <span>{{ announcement.title }}</span>
            <span class="time">{{ formatDateTime(announcement.createTime) }}</span>
          </div>
          <p class="announcement-content">{{ announcement.content }}</p>
        </el-card>
      </div>
    </section>

    <section class="roles-section">
      <div class="section-header">
        <h2>守护列表</h2>
        <span>{{ membershipInfo.currentCount }} / {{ membershipInfo.maxRoleCount }}</span>
      </div>
      <el-empty v-if="!roles.length" description="还没有守护对象，快去添加吧" />
      <div v-else class="role-cards">
        <el-card v-for="role in roles" :key="role.id" class="role-card" shadow="never">
          <div class="role-card-header">
            <div>
              <p class="role-name">{{ role.roleName }}</p>
              <p class="role-type">{{ role.roleType }}</p>
            </div>
            <el-tag size="small" type="info">{{ role.calendarType === 1 ? '阳历' : '阴历' }}</el-tag>
          </div>
          <div class="role-info">
            <p><strong>出生日期：</strong>{{ formatChineseDate(role.birthDate) }}</p>
            <p v-if="role.lunarBirthDate"><strong>阴历生日：</strong>{{ role.lunarBirthDate }}</p>
            <p><strong>提前提醒：</strong>{{ role.remindDays }} 天</p>
            <p v-if="role.rolePhone"><strong>联系电话：</strong>{{ role.rolePhone }}</p>
            <p v-if="role.remark"><strong>备注：</strong>{{ role.remark }}</p>
          </div>
          <div class="role-actions">
            <el-button size="small" type="primary" @click="handleEdit(role)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(role)">删除</el-button>
          </div>
        </el-card>
      </div>
    </section>

    <div class="add-role-bar">
      <el-button type="primary" round @click="showAddDialog" :disabled="membershipInfo.currentCount >= membershipInfo.maxRoleCount">
        添加守护对象
      </el-button>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑守护对象' : '添加守护对象'"
      width="100%"
      :fullscreen="true"
      class="mobile-dialog"
    >
      <div class="mobile-dialog-banner" :class="{ vip: membershipInfo.vipActive }">
        <div class="banner-icon">{{ isEdit ? '💌' : '🎀' }}</div>
        <div>
          <h3>{{ isEdit ? '更新守护对象' : '新增守护对象' }}</h3>
          <p>填写生日与提醒信息，守护时刻不缺席</p>
        </div>
      </div>
      <el-form
        :model="roleForm"
        :rules="roleRules"
        ref="roleFormRef"
        label-position="top"
        class="mobile-form"
      >
        <el-form-item label="角色类型" prop="roleType">
          <el-select v-model="roleForm.roleType" placeholder="请选择角色类型">
            <el-option label="父亲" value="父亲" />
            <el-option label="母亲" value="母亲" />
            <el-option label="爷爷" value="爷爷" />
            <el-option label="奶奶" value="奶奶" />
            <el-option label="外公" value="外公" />
            <el-option label="外婆" value="外婆" />
            <el-option label="兄弟" value="兄弟" />
            <el-option label="姐妹" value="姐妹" />
            <el-option label="朋友" value="朋友" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="日历类型" prop="calendarType">
          <el-radio-group v-model="roleForm.calendarType">
            <el-radio :label="1">阳历</el-radio>
            <el-radio :label="2">阴历</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker
            v-model="roleForm.birthDate"
            type="date"
            placeholder="请选择出生日期"
            format="YYYY年M月D号"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="提醒天数" prop="remindDays">
          <el-input-number v-model="roleForm.remindDays" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="联系电话" prop="rolePhone">
          <el-input v-model="roleForm.rolePhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="备注" prop="remark" class="full-row">
          <el-input v-model="roleForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="mobile-dialog-footer">
          <el-button round @click="dialogVisible = false">稍后填写</el-button>
          <el-button round type="primary" @click="handleSubmit">保存守护</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, addRole, updateRole, deleteRole } from '../api/role'
import { getActiveAnnouncements } from '../api/announcement'
import { getUserInfo } from '../api/user'
import { getWeChatOauthUrl, bindWeChatAccount } from '../api/wechat'
import { isWeChatBrowser } from '../utils/ua'
import { initWeChatJssdk } from '../utils/wechat'

const router = useRouter()
const route = useRoute()

const WECHAT_BOUND_KEY = 'bg_wechat_bound'
const WECHAT_PENDING_KEY = 'bg_wechat_oauth_pending'

const roles = ref([])
const announcements = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const roleFormRef = ref(null)

const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

const membershipInfo = reactive({
  membershipLevel: 'FREE',
  vipActive: false,
  maxRoleCount: 3,
  currentCount: 0
})

const roleForm = reactive({
  id: null,
  roleType: '',
  roleName: '',
  birthDate: '',
  calendarType: 1,
  remindDays: 3,
  rolePhone: '',
  remark: ''
})

const roleRules = {
  roleType: [{ required: true, message: '请选择角色类型', trigger: 'change' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  birthDate: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  remindDays: [{ required: true, message: '请输入提前提醒天数', trigger: 'change' }]
}

const ensureWeChatOAuth = async () => {
  if (!isWeChatBrowser()) {
    return
  }

  const token = localStorage.getItem('token')
  if (!token) {
    return
  }

  const query = { ...route.query }
  const code = query.code

  if (code) {
    try {
      sessionStorage.removeItem(WECHAT_PENDING_KEY)
      await bindWeChatAccount({ code })
      localStorage.setItem(WECHAT_BOUND_KEY, '1')
      ElMessage.success('微信账号绑定成功')
    } catch (error) {
      localStorage.removeItem(WECHAT_BOUND_KEY)
      const message = error?.response?.data?.message || error.message || '微信绑定失败'
      ElMessage.error(message)
    } finally {
      delete query.code
      delete query.state
      await router.replace({ path: route.path, query })
    }
    return
  }

  if (localStorage.getItem(WECHAT_BOUND_KEY) === '1') {
    return
  }

  if (sessionStorage.getItem(WECHAT_PENDING_KEY) === '1') {
    return
  }

  try {
    sessionStorage.setItem(WECHAT_PENDING_KEY, '1')
    const redirect = window.location.href.split('#')[0]
    const state = Math.random().toString(36).slice(2, 10)
    const res = await getWeChatOauthUrl({ redirect, state })
    const targetUrl = res.data?.url
    if (targetUrl) {
      window.location.href = targetUrl
    } else {
      sessionStorage.removeItem(WECHAT_PENDING_KEY)
    }
  } catch (error) {
    sessionStorage.removeItem(WECHAT_PENDING_KEY)
    const message = error?.response?.data?.message || error.message || '获取微信授权链接失败'
    ElMessage.error(message)
  }
}

const setupWeChatShare = async () => {
  if (!isWeChatBrowser()) {
    return
  }

  try {
    const wx = await initWeChatJssdk({
      jsApiList: [
        'updateAppMessageShareData',
        'updateTimelineShareData',
        'onMenuShareTimeline',
        'onMenuShareAppMessage'
      ]
    })

    const link = window.location.href.split('#')[0]
    const shareData = {
      title: '生日守护者',
      desc: '守护亲友生日的专属助手，支持提醒、短信祝福与名额管理。',
      link,
      imgUrl: `${window.location.origin}/vite.svg`
    }

    if (wx.updateAppMessageShareData) {
      wx.updateAppMessageShareData(shareData)
    }
    if (wx.onMenuShareAppMessage) {
      wx.onMenuShareAppMessage(shareData)
    }

    const timelineData = {
      title: shareData.title,
      link: shareData.link,
      imgUrl: shareData.imgUrl
    }

    if (wx.updateTimelineShareData) {
      wx.updateTimelineShareData(timelineData)
    }
    if (wx.onMenuShareTimeline) {
      wx.onMenuShareTimeline(timelineData)
    }
  } catch (error) {
    console.warn('WeChat JSSDK 初始化失败', error)
  }
}

const loadRoles = async () => {
  try {
    const res = await getRoles()
    const payload = res.data || {}
    roles.value = payload.roles || []
    membershipInfo.membershipLevel = payload.membershipLevel || membershipInfo.membershipLevel
    membershipInfo.vipActive = Boolean(payload.vipActive)
    membershipInfo.maxRoleCount = payload.maxRoleCount ?? (membershipInfo.vipActive ? 20 : 3)
    membershipInfo.currentCount = payload.currentCount ?? roles.value.length
  } catch (error) {
    ElMessage.error('加载守护列表失败，请稍后再试')
  }
}

const loadUserProfile = async () => {
  try {
    const res = await getUserInfo()
    if (res.data) {
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      membershipInfo.membershipLevel = res.data.membershipLevel || membershipInfo.membershipLevel
      membershipInfo.vipActive = Boolean(res.data.vipActive)
      membershipInfo.maxRoleCount = res.data.maxRoleCount || (membershipInfo.vipActive ? 20 : 3)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const loadAnnouncements = async () => {
  try {
    const res = await getActiveAnnouncements()
    if (res.code === 200) {
      announcements.value = res.data
    }
  } catch (error) {
    console.error('加载公告失败:', error)
  }
}

const formatChineseDate = (dateStr) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('YYYY年M月D日')
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('YYYY年M月D日 HH:mm')
}

const showAddDialog = () => {
  if (membershipInfo.currentCount >= membershipInfo.maxRoleCount) {
    if (membershipInfo.vipActive) {
      ElMessage.info('您的守护名额已满，如需更多名额请联系我们~')
    } else {
      ElMessage.warning(`最多可守护 ${membershipInfo.maxRoleCount} 位亲友，升级 VIP 感受更多贴心提醒吧~`)
    }
    return
  }
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(roleForm, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该守护对象吗？', '提示', { type: 'warning' })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请稍后再试')
    }
  }
}

const handleSubmit = async () => {
  try {
    await roleFormRef.value.validate()
    if (isEdit.value) {
      await updateRole(roleForm.id, roleForm)
      ElMessage.success('更新成功')
    } else {
      await addRole(roleForm)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadRoles()
  } catch (error) {
    if (error?.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

const resetForm = () => {
  Object.assign(roleForm, {
    id: null,
    roleType: '',
    roleName: '',
    birthDate: '',
    calendarType: 1,
    remindDays: 3,
    rolePhone: '',
    remark: ''
  })
  if (roleFormRef.value) {
    roleFormRef.value.clearValidate()
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

const availableSlots = computed(() => Math.max(0, membershipInfo.maxRoleCount - membershipInfo.currentCount))

const membershipLabel = computed(() => (membershipInfo.vipActive ? 'VIP 守护礼遇' : '温馨体验会员'))

const membershipMessage = computed(() => {
  if (membershipInfo.vipActive) {
    return 'VIP 会员可守护 20 位亲友，重要日子我们都为你记得。'
  }
  return `当前可守护 ${membershipInfo.maxRoleCount} 位亲友，升级 VIP 解锁短信祝福等更多服务。`
})

onMounted(async () => {
  await ensureWeChatOAuth()
  await Promise.all([loadUserProfile(), loadRoles(), loadAnnouncements()])
  setupWeChatShare()
})
</script>

<style scoped>
.mobile-home {
  position: relative;
  min-height: 100vh;
  padding: 24px 22px 96px;
  background: radial-gradient(circle at 20% 0%, rgba(255, 231, 220, 0.75), transparent 60%),
    radial-gradient(circle at 80% 15%, rgba(219, 232, 255, 0.8), transparent 55%),
    linear-gradient(180deg, rgba(254, 248, 242, 0.92), rgba(246, 238, 255, 0.9));
  overflow: hidden;
}

.mobile-ambient {
  position: absolute;
  border-radius: 50%;
  filter: blur(0px);
  opacity: 0.75;
  z-index: 0;
}

.ambient-sunrise {
  width: 180px;
  height: 180px;
  top: -60px;
  left: -40px;
  background: radial-gradient(circle, rgba(255, 195, 158, 0.6), transparent 70%);
}

.ambient-dawn {
  width: 240px;
  height: 240px;
  top: 220px;
  right: -80px;
  background: radial-gradient(circle, rgba(213, 219, 255, 0.65), transparent 70%);
}

.ambient-blossom {
  width: 260px;
  height: 260px;
  bottom: -120px;
  left: 20%;
  background: radial-gradient(circle, rgba(246, 213, 255, 0.4), transparent 70%);
}

.mobile-header {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 12px 32px rgba(189, 168, 255, 0.22);
  margin-bottom: 18px;
}

.mobile-header h1 {
  font-size: 22px;
  margin: 0 0 6px;
  color: #3d2a27;
}

.welcome {
  margin: 0;
  font-size: 13px;
  letter-spacing: 0.5px;
  color: rgba(96, 74, 69, 0.8);
}

.membership-card {
  position: relative;
  z-index: 1;
  padding: 22px;
  border-radius: 26px;
  background: linear-gradient(135deg, rgba(255, 208, 183, 0.92), rgba(255, 233, 216, 0.85));
  box-shadow: 0 18px 42px rgba(245, 181, 143, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.75);
  margin-bottom: 20px;
  overflow: hidden;
}

.membership-card.vip {
  background: linear-gradient(135deg, rgba(255, 218, 180, 0.96), rgba(255, 238, 208, 0.85));
}

.membership-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #3c221f;
  margin-bottom: 12px;
}

.membership-message {
  font-size: 15px;
  color: rgba(86, 60, 53, 0.9);
  line-height: 1.6;
  margin: 0 0 12px;
}

.membership-slots {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.55);
  color: #643e36;
  font-size: 13px;
}

.announcement-section {
  position: relative;
  z-index: 1;
  margin: 24px 0;
  padding: 24px 20px;
  border-radius: 40px 40px 62px 62px / 54px 54px 86px 86px;
  background: var(--surface-strong);
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow: var(--shadow-elevated);
  overflow: hidden;
}

.announcement-section::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.6), rgba(240, 139, 103, 0.14) 45%, rgba(166, 140, 255, 0.2));
  opacity: 0.75;
  z-index: 0;
}

.announcement-section > * {
  position: relative;
  z-index: 1;
}

.announcement-section h2,
.roles-section h2 {
  font-size: 18px;
  margin: 0 0 16px;
  color: #3d2d29;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 28px;
}

.announcement-item {
  border-radius: 26px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: rgba(255, 255, 255, 0.82);
  padding: 18px;
  box-shadow: 0 16px 32px rgba(168, 146, 255, 0.18);
  backdrop-filter: blur(10px);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  font-size: 15px;
  margin-bottom: 10px;
  color: #4a3733;
}

.announcement-header .time {
  font-size: 12px;
  color: rgba(119, 99, 91, 0.8);
}

.announcement-content {
  font-size: 14px;
  color: #66514d;
  line-height: 1.66;
}

.roles-section {
  margin-top: 16px;
  position: relative;
  z-index: 1;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: rgba(87, 69, 64, 0.9);
}

.section-header span {
  font-weight: 600;
}

.role-cards {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.role-card {
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.92);
  padding: 18px;
  box-shadow: 0 16px 36px rgba(210, 192, 255, 0.22);
}

.role-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.role-name {
  font-size: 18px;
  font-weight: 700;
  color: #3b2926;
}

.role-type {
  font-size: 13px;
  color: rgba(131, 107, 100, 0.9);
}

.role-info {
  font-size: 14px;
  color: #5d4642;
  line-height: 1.7;
}

.role-info strong {
  font-weight: 600;
  color: #3b2724;
}

.role-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.add-role-bar {
  position: fixed;
  bottom: 22px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  pointer-events: none;
  z-index: 2;
}

.add-role-bar .el-button {
  width: calc(100% - 48px);
  max-width: 420px;
  height: 48px;
  pointer-events: all;
  font-size: 16px;
  letter-spacing: 1px;
  box-shadow: 0 14px 32px rgba(240, 139, 103, 0.35);
}

.mobile-dialog :deep(.el-dialog) {
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(16px);
}

.mobile-dialog .el-dialog__body {
  padding: 16px 20px 12px;
}

.mobile-dialog-banner {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
  padding: 16px 18px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(255, 207, 181, 0.85), rgba(255, 232, 212, 0.75));
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.mobile-dialog-banner.vip {
  background: linear-gradient(135deg, rgba(255, 219, 187, 0.9), rgba(255, 240, 210, 0.78));
}

.mobile-dialog-banner h3 {
  margin: 0;
  font-size: 18px;
  color: #3d271f;
}

.mobile-dialog-banner p {
  margin: 4px 0 0;
  color: rgba(92, 69, 63, 0.8);
  font-size: 13px;
}

.mobile-dialog-banner .banner-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.mobile-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px 14px;
  padding-bottom: 16px;
}

.mobile-form :deep(.el-form-item) {
  margin-bottom: 0;
  background: rgba(255, 255, 255, 0.85);
  padding: 12px 14px;
  border-radius: 16px;
  border: 1px solid rgba(232, 224, 255, 0.8);
}

.mobile-form :deep(.full-row) {
  grid-column: 1 / -1;
}

.mobile-form :deep(.el-form-item__label) {
  font-size: 13px;
  color: #5a4340;
  margin-bottom: 6px;
}

.mobile-form :deep(.el-input__inner),
.mobile-form :deep(.el-textarea__inner) {
  font-size: 14px;
  border-radius: 12px;
}

.mobile-form :deep(.el-radio__label) {
  font-size: 14px;
}

.mobile-form :deep(.el-select .el-input__inner) {
  font-size: 14px;
}

.mobile-dialog-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.mobile-dialog-footer :deep(.el-button) {
  flex: 1;
  border-radius: 999px;
  padding: 12px 0;
}

.mobile-dialog-footer :deep(.el-button--primary) {
  background: linear-gradient(135deg, #f08b67, #f5a178);
  border: none;
  box-shadow: 0 14px 28px rgba(240, 139, 103, 0.3);
}

@media (min-width: 768px) {
  .mobile-home {
    max-width: 520px;
    margin: 32px auto;
    border-radius: 32px;
  }

  .add-role-bar {
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
