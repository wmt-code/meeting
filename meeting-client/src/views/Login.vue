<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>会议系统登录</h1>
        <p>Meeting Client</p>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="userName">
          <el-input
            v-model="loginForm.userName"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captchaCode"
              placeholder="验证码"
              prefix-icon="Key"
              size="large"
              @keyup.enter="handleLogin"
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" />
              <span v-else>加载中...</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <el-link type="primary" @click="goRegister">还没有账号？立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref<FormInstance>()

// 登录表单
const loginForm = reactive({
  userName: '',
  password: '',
  captchaCode: ''
})

// 表单验证规则
const loginRules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 验证码 URL
const captchaUrl = ref<string>('')

// 加载状态
const loading = ref<boolean>(false)

/**
 * 获取验证码
 */
const getCaptcha = async () => {
  try {
    const data = await userApi.getCaptcha()
    captchaUrl.value = 'data:image/png;base64,' + data
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

/**
 * 刷新验证码
 */
const refreshCaptcha = () => {
  getCaptcha()
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await userApi.login(loginForm)
      await userStore.login(res.token, res.userInfo)
      ElMessage.success('登录成功')
      router.push('/home')
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
      refreshCaptcha()
    } finally {
      loading.value = false
    }
  })
}

/**
 * 跳转注册
 */
const goRegister = () => {
  router.push('/register')
}

onMounted(() => {
  getCaptcha()
})
</script>

<style scoped lang="scss">
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  h1 {
    font-size: 28px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: #999;
  }
}

.login-form {
  .captcha-row {
    display: flex;
    gap: 12px;
    width: 100%;
  }

  .captcha-image {
    flex-shrink: 0;
    width: 120px;
    height: 40px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    cursor: pointer;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    span {
      font-size: 12px;
      color: #999;
    }

    &:hover {
      border-color: #409eff;
    }
  }

  .login-btn {
    width: 100%;
    margin-top: 12px;
  }
}

.login-footer {
  text-align: center;
  margin-top: 16px;
}
</style>
