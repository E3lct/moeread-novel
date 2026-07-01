import request from './request'

export function saveProgress(bookId, payloadOrChapterIndex, scrollPosition = 0) {
  const payload = typeof payloadOrChapterIndex === 'object'
    ? { bookId, ...payloadOrChapterIndex }
    : { bookId, chapterIndex: payloadOrChapterIndex, scrollPosition }
  return request.post('/progress/save', payload)
}

export function getProgress(bookId) {
  return request.get(`/progress/${bookId}`)
}
