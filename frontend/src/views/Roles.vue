<template>
  <Layout>
    <div class="roles-page">
      <div class="page-header">
        <h1>生日角色管理</h1>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加角色
        </el-button>
      </div>
      
      <el-card class="content-card">
        <el-table
          :data="roles"
          v-loading="loading"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column label="生日" width="150">
            <template #default="{ row }">
              {{ formatBirthday(row.birthday) }}
              <el-tag v-if="row.isLunar" size="small" type="warning" style="margin-left: 8px">
                农历
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="年龄" width="80">
            <template #default="{ row }">
              {{ calculateAge(row.birthday) }}岁
            </template>
          </el-table-column>
          <el-table-column label="距离生日" width="100">
            <template #default="{ row }">
              {{ getDaysUntilBirthday(row.birthday, row.isLunar) }}天
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="showEditDialog(row)"
              >
                编辑
              </el-button>
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
        
        <div v-if="roles.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无生日角色，点击上方按钮添加" />
        </div>
      </el-card>
      
      <!-- 添加/编辑对话框 -->
      <el-dialog
        :title="dialogTitle"
        v-model="dialogVisible"
        width="500px"
        @close="resetForm"
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
        >
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入姓名" />
          </el-form-item>
          
          <el-form-item label="生日" prop="birthday">
            <el-date-picker
              v-model="form.birthday"
              type="date"
              placeholder="请选择生日"
              style="width: 100%"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          
          <el-form-item label="历法">
            <el-radio-group v-model="form.isLunar">
              <el-radio :value="false">公历</el-radio>
              <el-radio :value="true">农历</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="备注">
            <el-input
              v-model="form.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注（可选）"
            />
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
import { roleApi } from '@/api/role'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'

const userStore = useUserStore()

const roles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const formRef = ref()

const form = reactive({
  id: null,
  name: '',
  birthday: '',
  isLunar: false,
  remark: ''
})

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 50, message: '姓名长度不能超过50个字符', trigger: 'blur' }
  ],
  birthday: [
    { required: true, message: '请选择生日', trigger: 'change' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑角色' : '添加角色')

const loadRoles = async () => {
  try {
    loading.value = true
    const userId = userStore.user?.id
    if (!userId) return
    
    const response = await roleApi.getRoles(userId)
    if (response.code === 200) {
      roles.value = response.data || []
    }
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
}

const showEditDialog = (role) => {
  isEdit.value = true
  form.id = role.id
  form.name = role.name
  form.birthday = role.birthday
  form.isLunar = role.isLunar
  form.remark = role.remark || ''
  dialogVisible.value = true
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.id = null
  form.name = ''
  form.birthday = ''
  form.isLunar = false
  form.remark = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const data = {
      userId: userStore.user.id,
      name: form.name,
      birthday: form.birthday,
      isLunar: form.isLunar,
      remark: form.remark
    }
    
    if (isEdit.value) {
      data.id = form.id
      await roleApi.updateRole(data)
      ElMessage.success('更新成功')
    } else {
      await roleApi.addRole(data)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    loadRoles()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (role) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色"${role.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await roleApi.deleteRole(role.id)
    ElMessage.success('删除成功')
    loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
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

// 计算距离生日的天数
const getDaysUntilBirthday = (birthday, isLunar) => {
  const today = new Date()
  const birthDate = new Date(birthday)
  
  if (isLunar) {
    // 农历生日处理（简化版）
    return '农历'
  }
  
  // 设置今年的生日
  const thisYearBirthday = new Date(today.getFullYear(), birthDate.getMonth(), birthDate.getDate())
  
  // 如果今年生日已过，计算明年的
  if (thisYearBirthday < today) {
    thisYearBirthday.setFullYear(today.getFullYear() + 1)
  }
  
  const diffTime = thisYearBirthday - today
  const days = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  return days === 0 ? '今天' : days
}

// 格式化生日
const formatBirthday = (birthday) => {
  return new Date(birthday).toLocaleDateString('zh-CN')
}

// 格式化时间
const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.roles-page {
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
</style>