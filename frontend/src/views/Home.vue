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

        <div class="toolbar">
          <el-button type="primary" @click="showAddDialog">添加角色</el-button>
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
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="提前提醒天数" prop="remindDays">
          <el-input-number v-model="roleForm.remindDays" :min="1" :max="30" />
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, addRole, updateRole, deleteRole } from '../api/role'
import { getActiveAnnouncements } from '../api/announcement'

const router = useRouter()
const roles = ref([])
const announcements = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const roleFormRef = ref(null)
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

const roleForm = reactive({
  id: null,
  roleType: '',
  roleName: '',
  birthDate: '',
  calendarType: 1,
  remindDays: 3,
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
    roles.value = res.data
  } catch (error) {
    ElMessage.error('加载角色列表失败')
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
  if (roles.value.length >= 20) {
    ElMessage.warning('最多只能添加20个角色')
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

onMounted(() => {
  loadRoles()
  loadAnnouncements()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f5f5;
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

.toolbar {
  margin-bottom: 20px;
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
