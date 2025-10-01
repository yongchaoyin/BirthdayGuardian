<template>
  <Layout>
    <div class="logs-page">
      <div class="page-header">
        <h1>通知日志</h1>
        <el-button @click="loadLogs" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      
      <el-card class="content-card">
        <el-table
          :data="logs"
          v-loading="loading"
          stripe
          style="width: 100%"
        >
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              {{ getRoleName(row.roleId) }}
            </template>
          </el-table-column>
          <el-table-column prop="message" label="通知内容" show-overflow-tooltip />
          <el-table-column label="发送状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.sendStatus === 1 ? 'success' : 'danger'">
                {{ row.sendStatus === 1 ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发送时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.sendTime) }}
            </template>
          </el-table-column>
          <el-table-column label="错误信息" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.sendStatus === 0" class="error-message">
                {{ row.errorMsg || '发送失败' }}
              </span>
              <span v-else class="success-message">发送成功</span>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="logs.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无通知日志" />
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { logApi } from '@/api/log'
import { roleApi } from '@/api/role'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'

const userStore = useUserStore()

const logs = ref([])
const roles = ref([])
const loading = ref(false)

const loadLogs = async () => {
  try {
    loading.value = true
    const userId = userStore.user?.id
    if (!userId) return
    
    const response = await logApi.getLogs(userId)
    if (response.code === 200) {
      logs.value = response.data || []
    }
  } catch (error) {
    ElMessage.error('加载通知日志失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    const userId = userStore.user?.id
    if (!userId) return
    
    const response = await roleApi.getRoles(userId)
    if (response.code === 200) {
      roles.value = response.data || []
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

const getRoleName = (roleId) => {
  const role = roles.value.find(r => r.id === roleId)
  return role ? role.name : '未知角色'
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(async () => {
  await loadRoles()
  await loadLogs()
})
</script>

<style scoped>
.logs-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.content-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.error-message {
  color: #f56c6c;
}

.success-message {
  color: #67c23a;
}
</style>