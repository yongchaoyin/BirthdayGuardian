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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfo, changePassword, updateEmail } from '../api/user'

const router = useRouter()
const userInfo = ref({})
const passwordFormRef = ref(null)
const emailFormRef = ref(null)
const passwordLoading = ref(false)
const emailLoading = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const emailForm = reactive({
  email: '',
  password: ''
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

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
    // 同时更新localStorage中的用户信息
    localStorage.setItem('userInfo', JSON.stringify(res.data))
  } catch (error) {
    ElMessage.error('获取用户信息失败')
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

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.card-header {
  font-weight: bold;
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
</style>
