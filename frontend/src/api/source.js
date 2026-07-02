import request from './request'

export function getSourcePresets() {
  return request.get('/source/presets')
}

export function getSourceSubscriptions() {
  return request.get('/source/subscriptions')
}

export function getSourceList() {
  return request.get('/source/list')
}

export function addPresetSource(sourceKey) {
  return request.post(`/source/preset/${sourceKey}`)
}

export function addCustomSource(data) {
  return request.post('/source', data)
}

export function addSourceBatch(data) {
  return request.post('/source/batch', data)
}

export function subscribeSource(url) {
  return request.post('/source/subscribe', { url })
}

export function updateSourceEnabled(sourceId, enabled) {
  return request.put(`/source/${sourceId}/enabled`, { enabled })
}

export function deleteSource(sourceId) {
  return request.delete(`/source/${sourceId}`)
}

export function importFromSource(data) {
  return request.post('/source/import', data)
}

export function searchSourceBooks(keyword, sourceId = 0) {
  return request.get('/source/search', { params: { keyword, sourceId: sourceId || undefined } })
}
