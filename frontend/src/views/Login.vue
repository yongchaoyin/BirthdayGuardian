<template>
  <div class="login-container">
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
        router.push('/')
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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 20px;
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.link {
  text-align: center;
  margin-top: 10px;
  color: #666;
}

.link a {
  color: #409eff;
  text-decoration: none;
}
</style>
