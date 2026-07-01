import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const username = computed(() => userInfo.value?.username || '')
  const nickname = computed(() => userInfo.value?.nickname || '')
  const userId = computed(() => userInfo.value?.userId || null)

  async function login(username, password) {
    const res = await authApi.login(username, password)
    token.value = res.data.token
    userInfo.value = {
      userId: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(userInfo.value))
  }

  async function register(username, password) {
    return authApi.register(username, password)
  }

  async function fetchProfile() {
    const res = await authApi.getProfile()
    userInfo.value = { ...userInfo.value, ...res.data }
    localStorage.setItem('user', JSON.stringify(userInfo.value))
    return res.data
  }

  async function updateProfile(data) {
    await authApi.updateProfile(data)
    userInfo.value = { ...userInfo.value, ...data }
    localStorage.setItem('user', JSON.stringify(userInfo.value))
  }

  // 实时预览背景设置（不调API，只更新本地状态）
  function updateBgPreview(data) {
    userInfo.value = { ...userInfo.value, ...data }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, userInfo, username, nickname, userId, login, register, fetchProfile, updateProfile, updateBgPreview, logout }
})
