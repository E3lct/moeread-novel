import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useReaderStore = defineStore('reader', () => {
  // 阅读器设置
  const fontSize = ref(parseInt(localStorage.getItem('reader_fontSize')) || 18)
  const lineHeight = ref(parseFloat(localStorage.getItem('reader_lineHeight')) || 1.8)
  const theme = ref(localStorage.getItem('reader_theme') || 'warm')
  const themes = {
    warm: { bg: '#FAF8F3', text: '#3D2E1A', name: '暖白' },
    paper: { bg: '#F5F0E8', text: '#5B4636', name: '纸黄' },
    green: { bg: '#E8F0E0', text: '#2D3E2A', name: '护眼' },
    dark: { bg: '#1A1A1A', text: '#CCCCCC', name: '夜间' }
  }

  function setFontSize(size) {
    fontSize.value = size
    localStorage.setItem('reader_fontSize', size)
  }

  function setLineHeight(h) {
    lineHeight.value = h
    localStorage.setItem('reader_lineHeight', h)
  }

  function setTheme(t) {
    theme.value = t
    localStorage.setItem('reader_theme', t)
  }

  return { fontSize, lineHeight, theme, themes, setFontSize, setLineHeight, setTheme }
})
