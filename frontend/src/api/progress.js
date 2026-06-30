import request from './request'

export function saveProgress(bookId, chapterIndex, scrollPosition) {
  return request.post('/progress/save', { bookId, chapterIndex, scrollPosition })
}

export function getProgress(bookId) {
  return request.get(`/progress/${bookId}`)
}
