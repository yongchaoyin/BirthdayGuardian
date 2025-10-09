<template>
  <div class="login-container">
    <div class="login-ambient ambient-one"></div>
    <div class="login-ambient ambient-two"></div>
    <div class="login-ambient ambient-three"></div>
    <el-card class="login-card">
      <h2 class="title">生日守护者 - 登录</h2>
      <el-form :model="form" :rules="rules" ref="loginForm">
        <el-form-item prop="account">
          <el-input
            v-model="form.account"
            placeholder="用户名或邮箱"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { isMobileDevice } from '../utils/ua'

const router = useRouter()
const loginForm = ref(null)

const form = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await loginForm.value.validate()
    console.log('发送登录请求:', form)
    const res = await login(form)
    console.log('登录响应:', res)

    if (res.data && res.data.token) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      ElMessage.success('登录成功')

      // 根据角色跳转
      if (res.data.role === 'admin') {
        router.push('/admin')
      } else {
        router.push(isMobileDevice() ? '/h5' : '/home')
      }
    } else {
      ElMessage.error('登录失败：未获取到token')
    }
  } catch (error) {
    console.error('登录错误:', error)
    if (error.response) {
      console.error('响应数据:', error.response.data)
      const errMsg = error.response.data.message || '登录失败'
      ElMessage.error(errMsg)
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('网络错误，请检查后端服务')
    }
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  background: radial-gradient(circle at 10% 20%, rgba(255, 219, 197, 0.8), transparent 55%),
    radial-gradient(circle at 85% 15%, rgba(206, 218, 255, 0.7), transparent 50%),
    linear-gradient(180deg, rgba(255, 248, 241, 0.95), rgba(244, 237, 255, 0.92));
  overflow: hidden;
}

.login-ambient {
  position: absolute;
  border-radius: 50%;
  filter: blur(0px);
  opacity: 0.7;
}

.ambient-one {
  width: 280px;
  height: 280px;
  top: -90px;
  left: -120px;
  background: radial-gradient(circle, rgba(255, 194, 155, 0.65), transparent 70%);
}

.ambient-two {
  width: 240px;
  height: 240px;
  bottom: -120px;
  right: -80px;
  background: radial-gradient(circle, rgba(206, 191, 255, 0.6), transparent 70%);
}

.ambient-three {
  width: 180px;
  height: 180px;
  top: 40%;
  right: 20%;
  background: radial-gradient(circle, rgba(254, 226, 255, 0.5), transparent 70%);
}

.login-card {
  position: relative;
  z-index: 1;
  width: 380px;
  padding: 36px 32px;
  border-radius: 36px;
  background: var(--surface-strong);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: var(--shadow-elevated);
  backdrop-filter: blur(20px);
}

.title {
  text-align: center;
  margin-bottom: 32px;
  color: #352520;
  font-size: 26px;
  letter-spacing: 2px;
  font-family: var(--font-heading);
}

.link {
  text-align: center;
  margin-top: 16px;
  color: rgba(99, 78, 72, 0.85);
}

.link a {
  color: var(--primary-color);
}

:deep(.el-input__wrapper) {
  border-radius: 16px;
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.75);
  background: rgba(255, 255, 255, 0.68);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: rgba(240, 139, 103, 0.6);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 0 0 4px rgba(240, 139, 103, 0.18);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #f08b67, #f5a178);
  border: none;
  box-shadow: 0 16px 32px rgba(240, 139, 103, 0.35);
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #f3744b, #f58d67);
}

@media (max-width: 420px) {
  .login-card {
    width: 100%;
    padding: 28px 20px;
    border-radius: 22px;
  }
}
</style>
