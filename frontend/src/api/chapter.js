import request from './request'

export function getToc(bookId) {
  return request.get(`/chapter/toc/${bookId}`)
}

export function getChapter(chapterId) {
  return request.get(`/chapter/${chapterId}`)
}
