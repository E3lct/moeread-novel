const EPUB_CACHE_NAME = 'moeread-epub-v1'
const memoryCache = new Map()
const warming = new Set()

export function epubUrl(bookOrUrl) {
  const raw = typeof bookOrUrl === 'string' ? bookOrUrl : bookOrUrl?.epubUrl
  if (!raw) return ''
  return raw.startsWith('http') ? raw : '/api' + raw
}

export function epubReaderUrl(bookOrUrl) {
  const raw = typeof bookOrUrl === 'string' ? bookOrUrl : (bookOrUrl?.epubOpfUrl || bookOrUrl?.epubUrl)
  if (!raw) return ''
  return raw.startsWith('http') ? raw : '/api' + raw
}

export async function loadEpubSource(url) {
  const normalized = epubUrl(url)
  if (!normalized) throw new Error('EPUB 地址为空')
  if (memoryCache.has(normalized)) return memoryCache.get(normalized).slice(0)
  const response = await getCachedResponse(normalized)
  const buffer = await response.arrayBuffer()
  memoryCache.set(normalized, buffer)
  return buffer.slice(0)
}

export async function fastEpubSource(url) {
  const normalized = epubReaderUrl(url)
  if (!normalized) throw new Error('EPUB 地址为空')
  if (/\.opf($|[?#])/i.test(normalized)) return normalized
  if (memoryCache.has(normalized)) return memoryCache.get(normalized).slice(0)
  const cached = await cachedResponse(normalized)
  if (cached) {
    const buffer = await cached.arrayBuffer()
    memoryCache.set(normalized, buffer)
    return buffer.slice(0)
  }
  warmEpubCache(normalized)
  return normalized
}

export async function warmEpubCache(bookOrUrl) {
  const url = epubUrl(bookOrUrl)
  if (!url || memoryCache.has(url) || warming.has(url)) return
  warming.add(url)
  try {
    const response = await getCachedResponse(url)
    const buffer = await response.arrayBuffer()
    memoryCache.set(url, buffer)
  } catch (e) {
    console.warn('EPUB 预热失败:', e)
  } finally {
    warming.delete(url)
  }
}

export function warmEpubBooks(books, limit = 4) {
  const epubs = (books || []).filter(book => book?.sourceType === 'epub' && book?.epubUrl).slice(0, limit)
  if (!epubs.length) return
  const run = () => {
    for (const book of epubs) warmEpubCache(book)
  }
  if ('requestIdleCallback' in window) {
    window.requestIdleCallback(run, { timeout: 1800 })
  } else {
    window.setTimeout(run, 600)
  }
}

async function getCachedResponse(url) {
  const request = new Request(url, { credentials: 'same-origin' })
  if ('caches' in window) {
    const cache = await caches.open(EPUB_CACHE_NAME)
    const cached = await cache.match(request)
    if (cached) return cached
    const response = await fetch(url, { credentials: 'same-origin', cache: 'force-cache' })
    if (!response.ok) throw new Error('EPUB 文件读取失败')
    cache.put(request, response.clone()).catch(() => {})
    return response
  }
  const response = await fetch(url, { credentials: 'same-origin', cache: 'force-cache' })
  if (!response.ok) throw new Error('EPUB 文件读取失败')
  return response
}

async function cachedResponse(url) {
  if (!('caches' in window)) return null
  const cache = await caches.open(EPUB_CACHE_NAME)
  return cache.match(new Request(url, { credentials: 'same-origin' }))
}
