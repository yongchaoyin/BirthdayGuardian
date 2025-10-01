<template>
  <Layout>
    <div class="profile-page">
      <div class="page-header">
        <h1>个人资料</h1>
      </div>
      
      <div class="profile-content">
        <!-- 基本信息 -->
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
            </div>
          </template>
          
          <div class="info-item">
            <label>邮箱：</label>
            <span>{{ userStore.user?.email }}</span>
          </div>
          
          <div class="info-item">
            <label>昵称：</label>
            <span>{{ userStore.user?.nickname || '未设置' }}</span>
          </div>
          
          <div class="info-item">
            <label>注册时间：</label>
            <span>{{ formatTime(userStore.user?.createTime) }}</span>
          </div>
          
          <div class="info-item">
            <label>账户状态：</label>
            <el-tag :type="userStore.user?.status === 1 ? 'success' : 'danger'">
              {{ userStore.user?.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
        </el-card>
        
        <!-- 密码设置 -->
        <el-card class="password-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">密码设置</span>
            </div>
          </template>
          
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            style="max-width: 400px"
          >
            <el-form-item label="新密码" prop="password">
              <el-input
                v-model="passwordForm.password"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                @click="handleSetPassword"
                :loading="passwordLoading"
              >
                设置密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 统计信息 -->
        <el-card class="stats-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">使用统计</span>
            </div>
          </template>
          
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-number">{{ stats.roleCount }}</div>
              <div class="stat-label">生日角色</div>
            </div>
            
            <div class="stat-item">
              <div class="stat-number">{{ stats.reminderCount }}</div>
              <div class="stat-label">提醒设置</div>
            </div>
            
            <div class="stat-item">
              <div class="stat-number">{{ stats.noticeCount }}</div>
              <div class="stat-label">通知记录</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import { roleApi } from '@/api/role'
import { reminderApi } from '@/api/reminder'
import { logApi } from '@/api/log'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'

const userStore = useUserStore()

const passwordFormRef = ref()
const passwordLoading = ref(false)

const passwordForm = reactive({
  password: '',
  confirmPassword: ''
})

const stats = reactive({
  roleCount: 0,
  reminderCount: 0,
  noticeCount: 0
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSetPassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    
    await authApi.setPassword({
      userId: userStore.user.id,
      password: passwordForm.password
    })
    
    ElMessage.success('密码设置成功')
    passwordForm.password = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value.resetFields()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    passwordLoading.value = false
  }
}

const loadStats = async () => {
  try {
    const userId = userStore.user?.id
    if (!userId) return
    
    // 加载角色统计
    const rolesResponse = await roleApi.getRoles(userId)
    if (rolesResponse.code === 200) {
      stats.roleCount = rolesResponse.data?.length || 0
    }
    
    // 加载提醒统计
    const remindersResponse = await reminderApi.getReminders(userId)
    if (remindersResponse.code === 200) {
      stats.reminderCount = remindersResponse.data?.length || 0
    }
    
    // 加载通知统计
    const logsResponse = await logApi.getLogs(userId)
    if (logsResponse.code === 200) {
      stats.noticeCount = logsResponse.data?.length || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const formatTime = (time) => {
  if (!time) return '未知'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card,
.password-card,
.stats-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item label {
  width: 80px;
  color: #606266;
  font-weight: 500;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>