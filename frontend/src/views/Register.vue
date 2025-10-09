<template>
  <div class="register-container">
    <div class="register-ambient ambient-one"></div>
    <div class="register-ambient ambient-two"></div>
    <div class="register-ambient ambient-three"></div>
    <el-card class="register-card">
      <h2 class="title">生日守护者 - 注册</h2>
      <el-form :model="form" :rules="rules" ref="registerForm">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="邮箱"
            prefix-icon="Message"
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
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="inviteCode">
          <el-input
            v-model="form.inviteCode"
            placeholder="推广码（选填）"
            prefix-icon="Promotion"
            clearable
          />
        </el-form-item>
        <p class="invite-hint">
          填写好友分享的推广码可帮助 TA 获得额外守护名额，你也将在注册成功后获得属于自己的推广码。
        </p>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'

const router = useRouter()
const route = useRoute()
const registerForm = ref(null)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  inviteCode: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度在6-20之间'))
  } else {
    if (form.confirmPassword !== '') {
      registerForm.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validateInviteCode = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const trimmed = value.trim()
  if (trimmed.length > 20) {
    callback(new Error('推广码长度不能超过20个字符'))
    return
  }
  if (!/^[A-Za-z0-9]+$/.test(trimmed)) {
    callback(new Error('推广码只支持字母或数字'))
    return
  }
  callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [{ required: true, validator: validatePass, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }],
  inviteCode: [{ validator: validateInviteCode, trigger: 'blur' }]
}

const handleRegister = async () => {
  try {
    await registerForm.value.validate()
    const payload = {
      username: form.username,
      email: form.email,
      password: form.password
    }
    if (form.inviteCode && form.inviteCode.trim()) {
      payload.inviteCode = form.inviteCode.trim().toUpperCase()
    }
    await register(payload)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '注册失败')
    } else if (error.message) {
      ElMessage.error(error.message)
    }
  }
}

onMounted(() => {
  const code = route.query.code
  if (typeof code === 'string' && code.trim()) {
    form.inviteCode = code.trim().toUpperCase()
  }
})
</script>

<style scoped>
.register-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  background: radial-gradient(circle at 18% 16%, rgba(255, 212, 182, 0.82), transparent 58%),
    radial-gradient(circle at 88% 12%, rgba(210, 229, 255, 0.7), transparent 55%),
    linear-gradient(180deg, rgba(255, 249, 241, 0.94), rgba(244, 236, 255, 0.92));
  overflow: hidden;
}

.register-ambient {
  position: absolute;
  border-radius: 50%;
  filter: blur(0px);
  opacity: 0.7;
}

.ambient-one {
  width: 260px;
  height: 260px;
  top: -80px;
  left: -110px;
  background: radial-gradient(circle, rgba(255, 187, 145, 0.65), transparent 70%);
}

.ambient-two {
  width: 220px;
  height: 220px;
  bottom: -110px;
  right: -70px;
  background: radial-gradient(circle, rgba(209, 190, 255, 0.62), transparent 70%);
}

.ambient-three {
  width: 180px;
  height: 180px;
  top: 38%;
  right: 22%;
  background: radial-gradient(circle, rgba(254, 222, 255, 0.5), transparent 70%);
}


.register-card {
  position: relative;
  z-index: 1;
  width: 380px;
  padding: 38px 32px;
  border-radius: 38px;
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
  letter-spacing: 1.8px;
  font-family: var(--font-heading);
}

.invite-hint {
  margin: -6px 0 18px;
  font-size: 12px;
  color: rgba(96, 72, 66, 0.75);
  line-height: 1.6;
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
  border: 1px solid rgba(255, 255, 255, 0.75);
  background: rgba(255, 255, 255, 0.68);
  box-shadow: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: rgba(240, 139, 103, 0.6);
  background: rgba(255, 255, 255, 0.95);
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
  .register-card {
    width: 100%;
    padding: 28px 20px;
    border-radius: 22px;
  }
}
</style>
