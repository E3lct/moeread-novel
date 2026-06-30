import request from './request'

export function getTagList() {
  return request.get('/tag/list')
}

export function createTag(name, color) {
  return request.post('/tag', { name, color })
}

export function deleteTag(tagId) {
  return request.delete(`/tag/${tagId}`)
}

export function addBookTag(bookId, tagId) {
  return request.post('/tag/book', { bookId, tagId })
}

export function removeBookTag(bookId, tagId) {
  return request.delete('/tag/book', { data: { bookId, tagId } })
}
