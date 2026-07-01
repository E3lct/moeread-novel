import request from './request'

export function getSourcePresets() {
  return request.get('/source/presets')
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

export function updateSourceEnabled(sourceId, enabled) {
  return request.put(`/source/${sourceId}/enabled`, { enabled })
}

export function deleteSource(sourceId) {
  return request.delete(`/source/${sourceId}`)
}

export function importFromSource(data) {
  return request.post('/source/import', data)
}
