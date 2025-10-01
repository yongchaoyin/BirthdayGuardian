<template>
  <Layout>
    <div class="reminders-page">
      <div class="page-header">
        <h1>提醒设置</h1>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加提醒
        </el-button>
      </div>
      
      <el-card class="content-card">
        <el-table
          :data="reminders"
          v-loading="loading"
          stripe
          style="width: 100%"
        >
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              {{ getRoleName(row.roleId) }}
            </template>
          </el-table-column>
          <el-table-column label="提醒方式" width="120">
            <template #default="{ row }">
              <el-tag :type="getNotifyTypeTag(row.notifyType)">
                {{ getNotifyTypeText(row.notifyType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提前天数" width="100">
            <template #default="{ row }">
              {{ row.advanceDays }}天
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="reminders.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无提醒设置，点击上方按钮添加" />
        </div>
      </el-card>
      
      <!-- 添加提醒对话框 -->
      <el-dialog
        title="添加提醒"
        v-model="dialogVisible"
        width="500px"
        @close="resetForm"
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
        >
          <el-form-item label="选择角色" prop="roleId">
            <el-select
              v-model="form.roleId"
              placeholder="请选择要提醒的角色"
              style="width: 100%"
            >
              <el-option
                v-for="role in availableRoles"
                :key="role.id"
                :label="role.name"
                :value="role.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="提醒方式" prop="notifyType">
            <el-select
              v-model="form.notifyType"
              placeholder="请选择提醒方式"
              style="width: 100%"
            >
              <el-option label="邮件提醒" :value="1" />
              <!-- 可以扩展其他提醒方式 -->
            </el-select>
          </el-form-item>
          
          <el-form-item label="提前天数" prop="advanceDays">
            <el-input-number
              v-model="form.advanceDays"
              :min="0"
              :max="30"
              placeholder="提前几天提醒"
              style="width: 100%"
            />
            <div class="form-tip">设置为0表示当天提醒</div>
          </el-form-item>
        </el-form>
        
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { reminderApi } from '@/api/reminder'
import { roleApi } from '@/api/role'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'

const userStore = useUserStore()

const reminders = ref([])
const roles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  roleId: null,
  notifyType: 1,
  advanceDays: 1
})

const rules = {
  roleId: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  notifyType: [
    { required: true, message: '请选择提醒方式', trigger: 'change' }
  ],
  advanceDays: [
    { required: true, message: '请设置提前天数', trigger: 'blur' }
  ]
}

// 可用的角色（排除已设置相同提醒方式的角色）
const availableRoles = computed(() => {
  return roles.value.filter(role => {
    // 检查该角色是否已经设置了相同类型的提醒
    return !reminders.value.some(reminder => 
      reminder.roleId === role.id && reminder.notifyType === form.notifyType
    )
  })
})

const loadData = async () => {
  try {
    loading.value = true
    const userId = userStore.user?.id
    if (!userId) return
    
    // 加载提醒列表
    const remindersResponse = await reminderApi.getReminders(userId)
    if (remindersResponse.code === 200) {
      reminders.value = remindersResponse.data || []
    }
    
    // 加载角色列表
    const rolesResponse = await roleApi.getRoles(userId)
    if (rolesResponse.code === 200) {
      roles.value = rolesResponse.data || []
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  if (roles.value.length === 0) {
    ElMessage.warning('请先添加生日角色')
    return
  }
  dialogVisible.value = true
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.roleId = null
  form.notifyType = 1
  form.advanceDays = 1
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const data = {
      userId: userStore.user.id,
      roleId: form.roleId,
      notifyType: form.notifyType,
      advanceDays: form.advanceDays
    }
    
    await reminderApi.addReminder(data)
    ElMessage.success('添加成功')
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (reminder) => {
  try {
    const roleName = getRoleName(reminder.roleId)
    await ElMessageBox.confirm(
      `确定要删除"${roleName}"的提醒设置吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await reminderApi.deleteReminder(reminder.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getRoleName = (roleId) => {
  const role = roles.value.find(r => r.id === roleId)
  return role ? role.name : '未知角色'
}

const getNotifyTypeText = (type) => {
  const typeMap = {
    1: '邮件提醒'
  }
  return typeMap[type] || '未知'
}

const getNotifyTypeTag = (type) => {
  const tagMap = {
    1: 'primary'
  }
  return tagMap[type] || 'info'
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.reminders-page {
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>