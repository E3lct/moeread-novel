import request from './request'

export function getBookList() {
  return request.get('/book/list')
}

// alias
export const getBookshelf = getBookList

export function searchBooks(keyword) {
  return request.get('/book/search', { params: { keyword } })
}

export function importTxt(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/book/import/txt', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function importZip(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/book/import/zip', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function updateBook(bookId, data) {
  return request.put(`/book/${bookId}`, data)
}

export function deleteBook(bookId) {
  return request.delete(`/book/${bookId}`)
}

export function uploadCover(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/book/cover', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
