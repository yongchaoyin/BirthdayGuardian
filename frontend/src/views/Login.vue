<template>
  <div class="login-container">
    <div class="login-form">
      <div class="form-title">
        <el-icon class="title-icon"><Calendar /></el-icon>
        生日守护者
      </div>
      <p class="form-subtitle">登录您的账户</p>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="email">
          <el-input
            v-model="loginForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="loginType">
          <el-radio-group v-model="loginForm.loginType">
            <el-radio value="code">验证码登录</el-radio>
            <el-radio value="password">密码登录</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="loginForm.loginType === 'code'" prop="code">
          <div class="code-input">
            <el-input
              v-model="loginForm.code"
              placeholder="请输入验证码"
              prefix-icon="Lock"
              size="large"
            />
            <el-button
              :disabled="codeDisabled"
              @click="sendCode"
              size="large"
            >
              {{ codeButtonText }}
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item v-if="loginForm.loginType === 'password'" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-button"
          >
            登录
          </el-button>
        </el-form-item>
        
        <div class="form-footer">
          <span>还没有账户？</span>
          <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)
const countdown = ref(0)

const loginForm = reactive({
  email: '',
  loginType: 'code',
  code: '',
  password: ''
})

const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const codeDisabled = computed(() => countdown.value > 0)
const codeButtonText = computed(() => 
  countdown.value > 0 ? `${countdown.value}s后重发` : '发送验证码'
)

const sendCode = async () => {
  if (!loginForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  
  try {
    await authApi.sendCode(loginForm.email)
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const loginData = {
      email: loginForm.email,
      loginType: loginForm.loginType
    }
    
    if (loginForm.loginType === 'code') {
      loginData.code = loginForm.code
    } else {
      loginData.password = loginForm.password
    }
    
    const result = await userStore.login(loginData)
    
    if (result.success) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-form {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.form-title {
  text-align: center;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.title-icon {
  font-size: 32px;
  color: #409eff;
}

.form-subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 30px;
}

.code-input {
  display: flex;
  gap: 10px;
}

.code-input .el-input {
  flex: 1;
}

.login-button {
  width: 100%;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  color: #909399;
}

.form-footer .el-link {
  margin-left: 5px;
}
</style>