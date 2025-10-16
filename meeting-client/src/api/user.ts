import { get, post, put, upload } from '@/utils/request'
import type { UserInfo } from '@/stores/user'

/**
 * 登录参数
 */
export interface LoginParams {
  userName: string
  password: string
  captchaCode: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

/**
 * 用户 API
 */
export const userApi = {
  /**
   * 获取验证码
   */
  getCaptcha: () => get<string>('/user/captcha'),

  /**
   * 用户登录
   */
  login: (data: LoginParams) => post<LoginResponse>('/user/login', data),

  /**
   * 用户注册
   */
  register: (data: any) => post('/user/register', data),

  /**
   * 获取当前用户信息
   */
  getCurrentUser: () => get<UserInfo>('/user/current'),

  /**
   * 更新用户信息
   */
  updateUserInfo: (data: any) => put('/user/update', data),

  /**
   * 修改密码
   */
  updatePassword: (data: any) => put('/user/password', data),

  /**
   * 上传头像
   */
  uploadAvatar: (formData: FormData) => upload<string>('/user/avatar', formData)
}
