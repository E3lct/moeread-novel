import request from './request'

export function getHeatmap(year) {
  return request.get('/stats/heatmap', { params: { year } })
}

export function getOverview() {
  return request.get('/stats/overview')
}

// alias
export const getStatsOverview = getOverview

export function getHabit() {
  return request.get('/stats/habit')
}

// alias
export const getWeeklyHabit = getHabit
