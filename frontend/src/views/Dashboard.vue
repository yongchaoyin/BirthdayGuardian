<template>
  <Layout>
    <div class="dashboard">
      <div class="page-header">
        <h1>仪表盘</h1>
        <p>欢迎回来，{{ userStore.user?.nickname || userStore.user?.email }}！</p>
      </div>
      
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon birthday">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalRoles }}</div>
              <div class="stat-label">生日角色</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon reminder">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalReminders }}</div>
              <div class="stat-label">提醒设置</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon today">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.todayBirthdays }}</div>
              <div class="stat-label">今日生日</div>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon upcoming">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.upcomingBirthdays }}</div>
              <div class="stat-label">即将到来</div>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 今日生日 -->
      <el-card class="content-card" v-if="todayBirthdays.length > 0">
        <template #header>
          <div class="card-header">
            <span class="card-title">🎉 今日生日</span>
          </div>
        </template>
        <div class="birthday-list">
          <div
            v-for="role in todayBirthdays"
            :key="role.id"
            class="birthday-item"
          >
            <div class="birthday-info">
              <div class="birthday-name">{{ role.name }}</div>
              <div class="birthday-date">
                {{ formatBirthday(role.birthday, role.isLunar) }}
                <el-tag v-if="role.isLunar" size="small" type="warning">农历</el-tag>
              </div>
            </div>
            <div class="birthday-age">
              {{ calculateAge(role.birthday) }}岁
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 即将到来的生日 -->
      <el-card class="content-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">📅 即将到来的生日</span>
            <el-button type="primary" @click="$router.push('/roles')">
              管理角色
            </el-button>
          </div>
        </template>
        <div v-if="upcomingBirthdays.length === 0" class="empty-state">
          <el-empty description="暂无即将到来的生日" />
        </div>
        <div v-else class="birthday-list">
          <div
            v-for="role in upcomingBirthdays"
            :key="role.id"
            class="birthday-item"
          >
            <div class="birthday-info">
              <div class="birthday-name">{{ role.name }}</div>
              <div class="birthday-date">
                {{ formatBirthday(role.birthday, role.isLunar) }}
                <el-tag v-if="role.isLunar" size="small" type="warning">农历</el-tag>
              </div>
            </div>
            <div class="birthday-countdown">
              还有 {{ getDaysUntilBirthday(role.birthday, role.isLunar) }} 天
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 最近通知 -->
      <el-card class="content-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">📧 最近通知</span>
            <el-button type="primary" @click="$router.push('/logs')">
              查看全部
            </el-button>
          </div>
        </template>
        <div v-if="recentLogs.length === 0" class="empty-state">
          <el-empty description="暂无通知记录" />
        </div>
        <div v-else class="log-list">
          <div
            v-for="log in recentLogs"
            :key="log.id"
            class="log-item"
          >
            <div class="log-info">
              <div class="log-message">{{ log.message }}</div>
              <div class="log-time">{{ formatTime(log.sendTime) }}</div>
            </div>
            <el-tag
              :type="log.sendStatus === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ log.sendStatus === 1 ? '成功' : '失败' }}
            </el-tag>
          </div>
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { roleApi } from '@/api/role'
import { reminderApi } from '@/api/reminder'
import { logApi } from '@/api/log'
import Layout from '@/components/Layout.vue'
import { User, Bell, Calendar, Clock } from '@element-plus/icons-vue'

const userStore = useUserStore()

const roles = ref([])
const reminders = ref([])
const logs = ref([])

const stats = reactive({
  totalRoles: 0,
  totalReminders: 0,
  todayBirthdays: 0,
  upcomingBirthdays: 0
})

// 今日生日
const todayBirthdays = computed(() => {
  const today = new Date()
  return roles.value.filter(role => {
    return isBirthdayToday(role.birthday, role.isLunar, today)
  })
})

// 即将到来的生日（7天内）
const upcomingBirthdays = computed(() => {
  const today = new Date()
  return roles.value
    .filter(role => {
      const days = getDaysUntilBirthday(role.birthday, role.isLunar)
      return days > 0 && days <= 7
    })
    .sort((a, b) => {
      const daysA = getDaysUntilBirthday(a.birthday, a.isLunar)
      const daysB = getDaysUntilBirthday(b.birthday, b.isLunar)
      return daysA - daysB
    })
})

// 最近通知（最多5条）
const recentLogs = computed(() => {
  return logs.value.slice(0, 5)
})

const loadData = async () => {
  try {
    const userId = userStore.user?.id
    if (!userId) return
    
    // 加载角色数据
    const rolesResponse = await roleApi.getRoles(userId)
    if (rolesResponse.code === 200) {
      roles.value = rolesResponse.data || []
      stats.totalRoles = roles.value.length
      stats.todayBirthdays = todayBirthdays.value.length
      stats.upcomingBirthdays = upcomingBirthdays.value.length
    }
    
    // 加载提醒数据
    const remindersResponse = await reminderApi.getReminders(userId)
    if (remindersResponse.code === 200) {
      reminders.value = remindersResponse.data || []
      stats.totalReminders = reminders.value.length
    }
    
    // 加载日志数据
    const logsResponse = await logApi.getLogs(userId)
    if (logsResponse.code === 200) {
      logs.value = logsResponse.data || []
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 判断是否是今天生日
const isBirthdayToday = (birthday, isLunar, today) => {
  const birthDate = new Date(birthday)
  if (isLunar) {
    // 农历生日处理（简化版，实际需要农历转换库）
    return false
  } else {
    return birthDate.getMonth() === today.getMonth() && 
           birthDate.getDate() === today.getDate()
  }
}

// 计算距离生日的天数
const getDaysUntilBirthday = (birthday, isLunar) => {
  const today = new Date()
  const birthDate = new Date(birthday)
  
  if (isLunar) {
    // 农历生日处理（简化版）
    return 0
  }
  
  // 设置今年的生日
  const thisYearBirthday = new Date(today.getFullYear(), birthDate.getMonth(), birthDate.getDate())
  
  // 如果今年生日已过，计算明年的
  if (thisYearBirthday < today) {
    thisYearBirthday.setFullYear(today.getFullYear() + 1)
  }
  
  const diffTime = thisYearBirthday - today
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

// 计算年龄
const calculateAge = (birthday) => {
  const today = new Date()
  const birthDate = new Date(birthday)
  let age = today.getFullYear() - birthDate.getFullYear()
  const monthDiff = today.getMonth() - birthDate.getMonth()
  
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }
  
  return age
}

// 格式化生日
const formatBirthday = (birthday, isLunar) => {
  const date = new Date(birthday)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

// 格式化时间
const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.page-header p {
  color: #909399;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.birthday {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.reminder {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.today {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.upcoming {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.content-card {
  margin-bottom: 24px;
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

.birthday-list, .log-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.birthday-item, .log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.birthday-name, .log-message {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.birthday-date, .log-time {
  font-size: 14px;
  color: #909399;
}

.birthday-age, .birthday-countdown {
  font-size: 14px;
  font-weight: 500;
  color: #409eff;
}

.empty-state {
  padding: 40px 0;
}
</style>