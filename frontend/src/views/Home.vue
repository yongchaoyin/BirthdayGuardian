<template>
  <div class="home-container">
    <el-container>
      <el-header class="header">
        <h2>生日守护者</h2>
        <div class="user-info">
          <span>{{ userInfo.username }}</span>
          <el-button @click="goToProfile" size="small" type="primary">个人中心</el-button>
          <el-button @click="handleLogout" size="small" type="danger">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <!-- 公告展示区域 -->
        <div v-if="announcements.length > 0" class="announcements-section">
          <el-card v-for="announcement in announcements" :key="announcement.id" class="announcement-card">
            <template #header>
              <div class="announcement-header">
                <span class="announcement-title">📢 {{ announcement.title }}</span>
                <span class="announcement-time">{{ formatDate(announcement.createTime) }}</span>
              </div>
            </template>
            <div class="announcement-content">{{ announcement.content }}</div>
          </el-card>
        </div>

        <div class="welcome-banner" :class="{ vip: membershipInfo.vipActive }">
          <div class="banner-text">
            <h3>Hi {{ userInfo.username || '守护者' }}，一起守护挚爱的日子</h3>
            <p>{{ membershipMessage }}</p>
          </div>
          <div class="banner-meta">
            <el-tag :type="membershipInfo.vipActive ? 'success' : 'warning'" effect="light">
              {{ membershipLabel }}
            </el-tag>
            <span class="slot-info">还能添加 {{ availableSlots }} 位亲友</span>
          </div>
        </div>

        <div class="toolbar">
          <el-button
            type="primary"
            @click="showAddDialog"
            :disabled="membershipInfo.currentCount >= membershipInfo.maxRoleCount"
          >
            添加守护对象
          </el-button>
          <span class="toolbar-hint">
            {{ membershipInfo.currentCount }} / {{ membershipInfo.maxRoleCount }} 位亲友已加入守护
          </span>
        </div>

        <div class="membership-rules">
          <el-card class="rule-card">
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

        <el-table :data="roles" style="width: 100%" border>
          <el-table-column prop="roleType" label="角色类型" width="120" />
          <el-table-column prop="roleName" label="角色名称" width="120" />
          <el-table-column prop="birthDate" label="出生日期" width="120" />
          <el-table-column label="日历类型" width="100">
            <template #default="scope">
              {{ scope.row.calendarType === 1 ? '阳历' : '阴历' }}
            </template>
          </el-table-column>
          <el-table-column prop="lunarBirthDate" label="阴历生日" width="120" />
          <el-table-column prop="remindDays" label="提前提醒天数" width="120" />
          <el-table-column prop="rolePhone" label="角色电话" width="140" />
          <el-table-column prop="remark" label="备注" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-main>
    </el-container>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '添加角色'"
      width="500px"
    >
      <el-form :model="roleForm" :rules="roleRules" ref="roleFormRef" label-width="120px">
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
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="roleForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, addRole, updateRole, deleteRole } from '../api/role'
import { getActiveAnnouncements } from '../api/announcement'
import { getUserInfo } from '../api/user'

const router = useRouter()
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
  remindDays: [{ required: true, message: '请输入提前提醒天数', trigger: 'blur' }]
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

const availableSlots = computed(() => Math.max(0, membershipInfo.maxRoleCount - membershipInfo.currentCount))

const membershipLabel = computed(() => (membershipInfo.vipActive ? 'VIP守护礼遇' : '温馨体验计划'))

const membershipMessage = computed(() => {
  if (membershipInfo.vipActive) {
    return 'VIP会员可同时守护 20 位亲友，我们会在每一个重要时刻为你点亮提醒。'
  }
  return '当前可守护 ' + membershipInfo.maxRoleCount + ' 位亲友，升级VIP可拥有更多贴心提醒与未来短信服务。'
})

onMounted(() => {
  loadUserProfile()
  loadRoles()
  loadAnnouncements()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f7f4ff 0%, #ffffff 60%, #fef6ff 100%);
  padding-bottom: 40px;
}

.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome-banner {
  background: linear-gradient(120deg, #fdf0ff, #f2f9ff);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.12);
}

.welcome-banner.vip {
  background: linear-gradient(120deg, #fff5e6, #ffe9f5);
  box-shadow: 0 12px 24px rgba(255, 153, 102, 0.18);
}

.banner-text h3 {
  margin: 0;
  font-size: 22px;
  color: #333;
}

.banner-text p {
  margin: 8px 0 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.banner-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.slot-info {
  font-size: 13px;
  color: #888;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.toolbar-hint {
  font-size: 13px;
  color: #666;
}

.membership-rules {
  margin-bottom: 20px;
}

.rule-card ul {
  margin: 0;
  padding-left: 20px;
  color: #555;
  line-height: 1.8;
}

.rule-card li::marker {
  color: #ff7aa8;
}

.rule-card a {
  color: #409eff;
}

.header h2::after {
  content: ' 🎂';
}

.announcements-section {
  margin-bottom: 30px;
}

.announcement-card {
  margin-bottom: 15px;
  border-left: 4px solid #667eea;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-title {
  font-weight: bold;
  font-size: 16px;
  color: #333;
}

.announcement-time {
  font-size: 12px;
  color: #999;
}

.announcement-content {
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
