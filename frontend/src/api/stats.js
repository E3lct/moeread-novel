import request from './request'

export function getHeatmap(year) {
  return request.get('/stats/heatmap', { params: { year } })
}

export function getOverview() {
  return request.get('/stats/overview')
}

export function getHabit() {
  return request.get('/stats/habit')
}
