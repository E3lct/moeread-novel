import request from './request'

export function getToc(bookId) {
  return request.get(`/chapter/toc/${bookId}`)
}

// alias
export const getChapterList = getToc

export function getChapter(chapterId) {
  return request.get(`/chapter/${chapterId}`)
}

// alias
export const getChapterContent = getChapter
