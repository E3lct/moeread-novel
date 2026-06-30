import request from './request'

export function register(username, password) {
  return request.post('/user/register', { username, password })
}

export function login(username, password) {
  return request.post('/user/login', { username, password })
}

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function changePassword(oldPassword, newPassword) {
  return request.put('/user/password', { oldPassword, newPassword })
}
