<template>
  <div class="home-container">
    <div class="ambient ambient-one"></div>
    <div class="ambient ambient-two"></div>
    <div class="ambient ambient-three"></div>
    <el-container class="home-shell">
      <el-header class="header">
        <div class="brand-block">
          <div class="brand-icon">🎂</div>
          <div>
            <h1>生日守护者</h1>
            <p class="brand-subtitle">以温柔的提醒，守护每一个重要日子</p>
          </div>
        </div>
        <div class="user-info">
          <div class="user-name">{{ userInfo.username }}</div>
          <el-button @click="goToProfile" size="small" round type="primary">个人中心</el-button>
          <el-button @click="handleLogout" size="small" round type="danger">退出</el-button>
        </div>
      </el-header>
      <el-main class="main-area">
        <div class="content-columns">
          <div class="left-pane">
            <div class="welcome-banner" :class="{ vip: membershipInfo.vipActive }">
              <div class="banner-body">
                <div class="banner-greeting">
                  <p class="hello">Hi {{ userInfo.username || '守护者' }}</p>
                  <h2>让爱的人，在生日这天感受到你的惦念</h2>
              <p class="message">{{ membershipMessage }}</p>
              <div class="slot-info">
                <span>还能温暖守护</span>
                <strong>{{ availableSlots }}</strong>
                <span>位亲友</span>
                <span v-if="inviteBonusCount > 0" class="bonus-tag">+推广奖励 {{ inviteBonusCount }}</span>
              </div>
            </div>
            <div class="banner-status">
              <el-tag
                :type="membershipInfo.vipActive ? 'success' : 'warning'"
                    effect="dark"
                    size="large"
                    class="status-tag"
                  >{{ membershipLabel }}</el-tag>
                  <div class="meta-circle">
                    <span class="count">{{ membershipInfo.currentCount }}</span>
                    <span class="caption">已守护</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="toolbar">
              <div>
                <h3>守护计划</h3>
                <p class="toolbar-hint">{{ membershipInfo.currentCount }} / {{ membershipInfo.maxRoleCount }} 位亲友已加入守护</p>
              </div>
              <el-button
                type="primary"
                round
                size="large"
                @click="showAddDialog"
                :disabled="membershipInfo.currentCount >= membershipInfo.maxRoleCount"
              >
                新增守护对象
              </el-button>
            </div>

            <div class="membership-rules">
              <el-card class="rule-card" shadow="hover">
                <template #header>
                  <span>🎈 守护规则速览</span>
                </template>
                <ul>
                  <li><strong>温馨体验</strong>：可守护 3 位亲友，享受基础邮件提醒。</li>
                  <li><strong>VIP 守护礼遇</strong>：可守护 20 位亲友，生日当天自动发送短信暖语。</li>
                  <li>填写角色电话后，VIP 用户将在生日当天把备注祝福通过短信送给对方。</li>
                  <li>如需开通 / 续订 VIP，请发送邮件至 <a href="mailto:yinyc0925@outlook.com" target="_blank">yinyc0925@outlook.com</a> 联系站主。</li>
                </ul>
              </el-card>
            </div>

            <div class="invite-cta">
              <div class="cta-text">
                <h4>推广任务 · 守护名额加倍</h4>
                <p>
                  每邀请 1 位好友成功注册，即可永久增加 1 个守护名额。
                  <span v-if="inviteBonusCount > 0">目前已获赠 <strong>{{ inviteBonusCount }}</strong> 个名额。</span>
                  <span v-else>现在就分享推广码开始累计额外名额吧。</span>
                </p>
              </div>
              <el-button type="info" plain round size="small" @click="goToPromo">
                查看推广码
              </el-button>
            </div>

            <el-card class="roles-card" shadow="never">
              <template #header>
                <div class="roles-header">
                  <div>
                    <h3>守护名单</h3>
                    <p class="sub">记录那些你想要好好珍惜的生日</p>
                  </div>
                  <el-tag effect="dark" round type="info">{{ roles.length }} 位亲友</el-tag>
                </div>
              </template>
              <el-table :data="paginatedRoles" border stripe class="roles-table">
                <el-table-column prop="roleName" label="姓名" width="140" />
                <el-table-column prop="roleType" label="角色" width="120" />
                <el-table-column prop="birthDate" label="出生日期" width="140" />
                <el-table-column label="日历类型" width="110">
                  <template #default="scope">
                    <el-tag size="small" :type="scope.row.calendarType === 1 ? 'success' : 'warning'">
                      {{ scope.row.calendarType === 1 ? '阳历' : '阴历' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="lunarBirthDate" label="阴历生日" width="160" />
                <el-table-column prop="remindDays" label="提前提醒" width="120">
                  <template #default="scope">
                    <span>{{ scope.row.remindDays }} 天</span>
                  </template>
                </el-table-column>
                <el-table-column prop="rolePhone" label="联系电话" width="160" />
                <el-table-column prop="remark" label="备注" min-width="180" />
                <el-table-column label="操作" width="160" fixed="right">
                  <template #default="scope">
                    <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
                    <el-divider direction="vertical"></el-divider>
                    <el-button size="small" type="danger" link @click="handleDelete(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            <div class="table-footer" v-if="roles.length > pageSize">
              <el-pagination
                background
                layout="prev, pager, next"
                :total="roles.length"
                :page-size="pageSize"
                :current-page="currentPage"
                @current-change="handlePageChange"
              />
            </div>
            </el-card>
          </div>
          <aside class="right-pane">
            <div class="announcement-panel">
              <h3 class="announcements-title">📢 公告栏</h3>
              <div v-if="announcements.length" class="announcements-section">
                <el-card
                  v-for="announcement in announcements"
                  :key="announcement.id"
                  class="announcement-card"
                  shadow="hover"
                >
                  <template #header>
                    <div class="announcement-header">
                      <span class="announcement-title">{{ announcement.title }}</span>
                      <span class="announcement-time">{{ formatDate(announcement.createTime) }}</span>
                    </div>
                  </template>
                  <div class="announcement-content">{{ announcement.content }}</div>
                </el-card>
              </div>
              <div v-else class="announcement-empty">暂无公告，祝你和亲友每天都快乐💫</div>
            </div>
          </aside>
        </div>
      </el-main>
    </el-container>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '添加角色'"
      width="520px"
      class="role-dialog"
      :destroy-on-close="true"
    >
      <div class="dialog-banner" :class="{ vip: membershipInfo.vipActive }">
        <div class="banner-icon">{{ isEdit ? '💌' : '🎁' }}</div>
        <div>
          <h3>{{ isEdit ? '更新守护对象' : '新增守护对象' }}</h3>
          <p>填写关键信息，让提醒更贴心准确</p>
        </div>
      </div>
      <el-form
        :model="roleForm"
        :rules="roleRules"
        ref="roleFormRef"
        label-position="top"
        class="role-form"
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
        <el-form-item label="提前提醒天数" prop="remindDays">
          <el-input-number v-model="roleForm.remindDays" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="角色电话" prop="rolePhone">
          <el-input
            v-model="roleForm.rolePhone"
            placeholder="用于生日当天发送短信"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark" class="full-row">
          <el-input
            v-model="roleForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button round @click="dialogVisible = false">稍后再说</el-button>
          <el-button round type="primary" @click="handleSubmit">保存守护</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, addRole, updateRole, deleteRole } from '../api/role'
import { getActiveAnnouncements } from '../api/announcement'
import { getUserInfo } from '../api/user'

const router = useRouter()
const roles = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const announcements = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const roleFormRef = ref(null)
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
const membershipInfo = reactive({
  membershipLevel: 'FREE',
  vipActive: false,
  maxRoleCount: 3,
  currentCount: 0,
  inviteBonus: 0
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
  remindDays: [{ required: true, message: '请输入提前提醒天数', trigger: 'blur' }]
}

const paginatedRoles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return roles.value.slice(start, start + pageSize.value)
})

const handlePageChange = (page) => {
  currentPage.value = page
}

watch(
  () => roles.value.length,
  (len) => {
    const maxPage = Math.max(1, Math.ceil(len / pageSize.value))
    if (currentPage.value > maxPage) {
      currentPage.value = maxPage
    }
  }
)

const loadRoles = async () => {
  try {
    const res = await getRoles()
    const payload = res.data || {}
    roles.value = payload.roles || []
    membershipInfo.membershipLevel = payload.membershipLevel || membershipInfo.membershipLevel
    membershipInfo.vipActive = Boolean(payload.vipActive)
    membershipInfo.maxRoleCount = payload.maxRoleCount ?? (membershipInfo.vipActive ? 20 : 3)
    membershipInfo.currentCount = payload.currentCount ?? roles.value.length
    membershipInfo.inviteBonus = payload.inviteBonus ?? payload.inviteSuccessCount ?? 0
  } catch (error) {
    ElMessage.error('加载角色列表失败')
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
      membershipInfo.inviteBonus = res.data.inviteBonus ?? res.data.inviteSuccessCount ?? 0
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

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const showAddDialog = () => {
  if (membershipInfo.currentCount >= membershipInfo.maxRoleCount) {
    if (membershipInfo.vipActive) {
      ElMessage.info('您已温暖守护满额亲友，如需更多名额请与我们联系~')
    } else {
      ElMessage.warning('温馨体验会员最多可守护 ' + membershipInfo.maxRoleCount + ' 位亲友，升级VIP即可拥抱更多挚爱哦~')
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
    await ElMessageBox.confirm('确定要删除这个角色吗？', '提示', {
      type: 'warning'
    })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
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
    if (error.response) {
      ElMessage.error(error.response.data.message || '操作失败')
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

const goToProfile = () => {
  router.push('/profile')
}

const goToPromo = () => {
  router.push({ path: '/profile', query: { focus: 'promo' } })
}

const availableSlots = computed(() => Math.max(0, membershipInfo.maxRoleCount - membershipInfo.currentCount))

const membershipLabel = computed(() => (membershipInfo.vipActive ? 'VIP守护礼遇' : '温馨体验计划'))

const inviteBonusCount = computed(() => membershipInfo.inviteBonus || 0)

const baseQuota = computed(() => (membershipInfo.vipActive ? 20 : 3))

const membershipMessage = computed(() => {
  const bonus = inviteBonusCount.value
  const total = membershipInfo.maxRoleCount
  if (membershipInfo.vipActive) {
    return bonus > 0
      ? `VIP会员可同时守护 ${total} 位亲友（含推广奖励 +${bonus}），我们会在每一个重要时刻为你点亮提醒。`
      : 'VIP会员可同时守护 20 位亲友，我们会在每一个重要时刻为你点亮提醒。'
  }
  if (bonus > 0) {
    return `当前可守护 ${total} 位亲友（基础 ${baseQuota.value} 位 + 推广奖励 ${bonus} 位），继续分享可获得更多名额哦。`
  }
  return `当前可守护 ${total} 位亲友，升级VIP可拥有更多贴心提醒与未来短信服务。`
})

onMounted(() => {
  loadUserProfile()
  loadRoles()
  loadAnnouncements()
})
</script>

<style scoped>
.home-container {
  position: relative;
  min-height: 100vh;
  padding: 48px 36px 96px;
  overflow: hidden;
}

.ambient {
  position: absolute;
  border-radius: 50%;
  filter: blur(0px);
  opacity: 0.8;
  z-index: 0;
}

.ambient-one {
  width: 240px;
  height: 240px;
  top: -60px;
  right: 12%;
  background: radial-gradient(circle, rgba(255, 199, 170, 0.5), transparent 70%);
}

.ambient-two {
  width: 280px;
  height: 280px;
  bottom: -100px;
  left: 8%;
  background: radial-gradient(circle, rgba(198, 215, 255, 0.55), transparent 70%);
}

.ambient-three {
  width: 320px;
  height: 320px;
  bottom: -120px;
  right: -60px;
  background: radial-gradient(circle, rgba(250, 224, 255, 0.45), transparent 70%);
}

.home-shell {
  position: relative;
  z-index: 1;
  max-width: 1140px;
  margin: 0 auto;
  background: transparent;
}

.header {
  position: sticky;
  top: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  backdrop-filter: blur(14px);
  background: rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-card);
  margin-bottom: 28px;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 18px;
}

.brand-icon {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  background: linear-gradient(135deg, rgba(255, 184, 145, 0.9), rgba(255, 225, 200, 0.8));
  box-shadow: 0 12px 24px rgba(255, 184, 145, 0.25);
}

.brand-block h1 {
  margin: 0;
  font-size: 26px;
  letter-spacing: 1px;
  color: #352626;
  font-family: var(--font-heading);
}

.brand-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
  font-family: var(--font-body);
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-name {
  font-weight: 600;
  color: #4f3a36;
}

.main-area {
  display: flex;
  flex-direction: column;
}

.content-columns {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.left-pane {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.right-pane {
  flex: 1;
  display: flex;
}

.announcement-panel {
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 28px 24px;
  border-radius: 48px 48px 68px 68px / 60px 60px 90px 90px;
  background: var(--surface-strong);
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow: var(--shadow-elevated);
  overflow: hidden;
}

.announcement-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.65), rgba(240, 139, 103, 0.12) 45%, rgba(152, 123, 255, 0.18));
  z-index: 0;
  opacity: 0.7;
}

.announcement-panel > * {
  position: relative;
  z-index: 1;
}

.announcements-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #3c2c29;
  letter-spacing: 1px;
  font-family: var(--font-heading);
}

.announcements-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-card {
  width: 100%;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 18px 38px rgba(134, 120, 255, 0.18);
  backdrop-filter: blur(12px);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-title {
  font-weight: 600;
  color: #3c2c29;
  font-family: var(--font-heading);
}

.announcement-time {
  font-size: 12px;
  color: #9f8f89;
}

.announcement-content {
  color: #5d4c47;
  line-height: 1.7;
  white-space: pre-wrap;
  font-family: var(--font-body);
}

.announcement-empty {
  padding: 32px 12px;
  text-align: center;
  color: var(--text-secondary);
  border-radius: 18px;
  border: 1px dashed rgba(219, 211, 255, 0.7);
  background: rgba(255, 255, 255, 0.6);
}

.welcome-banner {
  position: relative;
  padding: 36px;
  border-radius: 28px;
  overflow: hidden;
  background: linear-gradient(130deg, rgba(255, 229, 214, 0.85), rgba(245, 240, 255, 0.78));
  box-shadow: var(--shadow-elevated);
  border: 1px solid rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(16px);
}

.welcome-banner.vip {
  background: linear-gradient(125deg, rgba(255, 224, 193, 0.9), rgba(255, 247, 220, 0.78));
}

.banner-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
}

.banner-greeting {
  max-width: 520px;
}

.hello {
  margin: 0;
  font-size: 16px;
  color: rgba(82, 61, 56, 0.8);
  letter-spacing: 1px;
  font-family: var(--font-accent);
}

.banner-greeting h2 {
  margin: 12px 0;
  font-size: 28px;
  color: #3a2523;
  font-family: var(--font-heading);
  line-height: 1.36;
}

.message {
  margin: 12px 0;
  color: #604843;
  line-height: 1.7;
  font-family: var(--font-body);
}

.slot-info {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.5);
  color: #5a403b;
  font-size: 14px;
}

.slot-info strong {
  font-size: 22px;
  font-weight: 700;
}

.bonus-tag {
  margin-left: 6px;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(240, 139, 103, 0.16);
  color: #f08b67;
  font-size: 12px;
}

.banner-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.status-tag {
  font-size: 14px;
  letter-spacing: 2px;
  padding: 10px 18px;
}

.meta-circle {
  width: 116px;
  height: 116px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-shadow: 0 12px 25px rgba(172, 127, 115, 0.26);
}

.meta-circle .count {
  font-size: 36px;
  font-weight: 700;
  color: #3f2e2a;
}

.meta-circle .caption {
  font-size: 14px;
  color: #7d615b;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 28px;
  border-radius: 28px;
  background: var(--surface-strong);
  border: 1px solid rgba(255, 255, 255, 0.68);
  box-shadow: 0 18px 34px rgba(173, 164, 255, 0.18);
}

.toolbar h3 {
  margin: 0 0 4px;
  font-size: 20px;
  color: #3d2d29;
  font-family: var(--font-heading);
}

.toolbar-hint {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  font-family: var(--font-body);
}

.membership-rules .rule-card {
  border-radius: 32px;
  border: 1px solid rgba(255, 255, 255, 0.68);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 38px rgba(200, 188, 255, 0.2);
  backdrop-filter: blur(12px);
}

.invite-cta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
  margin-top: 12px;
  padding: 18px 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px dashed rgba(240, 139, 103, 0.4);
  box-shadow: 0 12px 28px rgba(240, 139, 103, 0.18);
}

.invite-cta .cta-text {
  flex: 1;
  min-width: 220px;
}

.invite-cta .cta-text h4 {
  margin: 0 0 6px;
  font-size: 16px;
  color: #4a3330;
  font-family: var(--font-heading);
}

.invite-cta .cta-text p {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.invite-cta .cta-text strong {
  color: #f08b67;
  font-family: var(--font-heading);
}

.rule-card ul {
  margin: 0;
  padding-left: 20px;
  color: #5b4d49;
  line-height: 1.9;
}

.rule-card li::marker {
  color: var(--primary-color);
}

.rule-card a {
  color: var(--primary-hover);
}

.roles-card {
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 22px 46px rgba(184, 162, 255, 0.2);
  backdrop-filter: blur(14px);
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  padding: 18px 12px 4px;
}

.table-footer :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 12px;
  margin: 0 4px;
}

.roles-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.roles-header h3 {
  margin: 0;
  font-size: 20px;
  color: #3f2c27;
  font-family: var(--font-heading);
}

.roles-header .sub {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 14px;
  font-family: var(--font-body);
}

.roles-table {
  --el-table-header-text-color: #6b5a57;
  --el-table-header-bg-color: rgba(248, 243, 255, 0.9);
  --el-table-border-color: rgba(233, 229, 255, 0.8);
  --el-table-row-hover-bg-color: rgba(255, 245, 235, 0.7);
}

:deep(.roles-table .el-table__header-wrapper th) {
  font-weight: 600;
  font-size: 14px;
}

:deep(.roles-table .el-table__row) {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

:deep(.roles-table .el-table__row:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(220, 198, 255, 0.28);
}

:deep(.roles-table .el-tag) {
  border-radius: 999px;
}

.role-dialog :deep(.el-dialog) {
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 26px 56px rgba(186, 168, 255, 0.28);
  backdrop-filter: blur(18px);
}

.dialog-banner {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 18px 20px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(255, 207, 181, 0.8), rgba(255, 234, 219, 0.75));
  border: 1px solid rgba(255, 255, 255, 0.72);
}

.dialog-banner.vip {
  background: linear-gradient(135deg, rgba(255, 223, 185, 0.85), rgba(255, 244, 211, 0.78));
}

.dialog-banner h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #3c2b27;
}

.dialog-banner p {
  margin: 0;
  color: rgba(92, 69, 63, 0.85);
  font-size: 13px;
}

.banner-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.6);
  font-size: 24px;
}

.role-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px 20px;
}

.role-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.role-form :deep(.full-row) {
  grid-column: 1 / -1;
}

.role-form :deep(.el-form-item__label) {
  color: #5f4641;
  font-weight: 600;
}

.role-form :deep(.el-input__wrapper),
.role-form :deep(.el-textarea__inner) {
  border-radius: 14px;
  border: 1px solid rgba(233, 223, 255, 0.9);
  box-shadow: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.role-form :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(240, 139, 103, 0.8);
  box-shadow: 0 0 0 3px rgba(240, 139, 103, 0.18);
}

.role-form :deep(.el-select .el-input__wrapper) {
  padding: 0 12px;
}

.role-form :deep(.el-radio__inner) {
  border-color: rgba(240, 139, 103, 0.6);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 8px;
}

.dialog-footer :deep(.el-button) {
  padding: 10px 24px;
  border-radius: 999px;
}

.dialog-footer :deep(.el-button--primary) {
  background: linear-gradient(135deg, #f08b67, #f5a178);
  border: none;
  box-shadow: 0 18px 32px rgba(240, 139, 103, 0.3);
}

@media (max-width: 1024px) {
  .home-container {
    padding: 36px 20px 72px;
  }

  .content-columns {
    flex-direction: column;
    gap: 20px;
  }

  .left-pane,
  .right-pane {
    width: 100%;
  }

  .banner-body {
    flex-direction: column;
    align-items: flex-start;
  }

  .banner-status {
    flex-direction: row;
    align-items: center;
  }

  .meta-circle {
    width: 96px;
    height: 96px;
  }
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .announcement-panel {
    padding: 20px 18px;
  }

  .toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .invite-cta {
    flex-direction: column;
    align-items: flex-start;
  }

  .roles-card {
    border-radius: 20px;
  }
}
</style>
