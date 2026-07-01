<template>
  <div class="crop-overlay" v-if="show" @click.self="$emit('close')">
    <div class="crop-modal">
      <div class="crop-header">
        <span class="crop-title">裁剪封面</span>
        <button class="crop-close" @click="$emit('close')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
        </button>
      </div>
      <div class="crop-body">
        <div
          class="crop-frame"
          ref="frameRef"
          @mousedown="startDrag"
          @touchstart="startDrag"
          @wheel.prevent="onWheel"
        >
          <img
            :src="imageSrc"
            ref="imgRef"
            :style="{ width: imgW + 'px', height: imgH + 'px', left: offsetX + 'px', top: offsetY + 'px' }"
            @load="onImgLoad"
            @dragstart.prevent
          />
        </div>
        <div class="crop-controls">
          <span class="crop-control-label">缩放</span>
          <input type="range" min="100" max="500" step="10" v-model.number="zoomPct" @input="onZoomInput" />
          <span class="crop-zoom-val">{{ zoomPct }}%</span>
        </div>
      </div>
      <div class="crop-footer">
        <button class="btn btn-ghost" @click="$emit('close')">取消</button>
        <button class="btn btn-primary" @click="confirmCrop">确认裁剪</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  show: { type: Boolean, default: false },
  imageSrc: { type: String, default: '' }
})
const emit = defineEmits(['close', 'crop'])

const FRAME_W = 240
const FRAME_H = 320
const OUT_W = 360
const OUT_H = 480

const imgRef = ref(null)
const naturalW = ref(0)
const naturalH = ref(0)
const baseScale = ref(1)
const zoomPct = ref(100)
const offsetX = ref(0)
const offsetY = ref(0)

const imgW = ref(0)
const imgH = ref(0)

function getDisplayScale() {
  return baseScale.value * (zoomPct.value / 100)
}

function updateImgSize() {
  const s = getDisplayScale()
  imgW.value = naturalW.value * s
  imgH.value = naturalH.value * s
}

function centerImage() {
  offsetX.value = (FRAME_W - imgW.value) / 2
  offsetY.value = (FRAME_H - imgH.value) / 2
}

function clampX(x) {
  if (imgW.value <= FRAME_W) return (FRAME_W - imgW.value) / 2
  return Math.min(0, Math.max(FRAME_W - imgW.value, x))
}
function clampY(y) {
  if (imgH.value <= FRAME_H) return (FRAME_H - imgH.value) / 2
  return Math.min(0, Math.max(FRAME_H - imgH.value, y))
}

function onImgLoad() {
  const img = imgRef.value
  if (!img) return
  naturalW.value = img.naturalWidth
  naturalH.value = img.naturalHeight
  baseScale.value = Math.max(FRAME_W / naturalW.value, FRAME_H / naturalH.value)
  zoomPct.value = 100
  updateImgSize()
  centerImage()
}

function onZoomInput() {
  // 保持画面中心不变
  const oldScale = baseScale.value * ((zoomPct.value - (event?.detail || 0)) / 100)
  const oldScaleActual = imgW.value / naturalW.value
  const cx = (FRAME_W / 2 - offsetX.value) / oldScaleActual
  const cy = (FRAME_H / 2 - offsetY.value) / oldScaleActual
  updateImgSize()
  const newScale = getDisplayScale()
  offsetX.value = clampX(FRAME_W / 2 - cx * newScale)
  offsetY.value = clampY(FRAME_H / 2 - cy * newScale)
}

function onWheel(e) {
  const delta = e.deltaY > 0 ? -10 : 10
  const oldZoom = zoomPct.value
  zoomPct.value = Math.max(100, Math.min(500, zoomPct.value + delta))
  if (zoomPct.value !== oldZoom) {
    const oldScale = baseScale.value * (oldZoom / 100)
    const cx = (FRAME_W / 2 - offsetX.value) / oldScale
    const cy = (FRAME_H / 2 - offsetY.value) / oldScale
    updateImgSize()
    const newScale = getDisplayScale()
    offsetX.value = clampX(FRAME_W / 2 - cx * newScale)
    offsetY.value = clampY(FRAME_H / 2 - cy * newScale)
  }
}

function startDrag(e) {
  e.preventDefault()
  const isTouch = e.type === 'touchstart'
  const startX = isTouch ? e.touches[0].clientX : e.clientX
  const startY = isTouch ? e.touches[0].clientY : e.clientY
  const startOffX = offsetX.value
  const startOffY = offsetY.value

  function onMove(ev) {
    const isT = ev.type === 'touchmove'
    const x = isT ? ev.touches[0].clientX : ev.clientX
    const y = isT ? ev.touches[0].clientY : ev.clientY
    offsetX.value = clampX(startOffX + (x - startX))
    offsetY.value = clampY(startOffY + (y - startY))
    if (isT) ev.preventDefault()
  }
  function onEnd() {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onEnd)
    document.removeEventListener('touchmove', onMove)
    document.removeEventListener('touchend', onEnd)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onEnd)
  document.addEventListener('touchmove', onMove, { passive: false })
  document.addEventListener('touchend', onEnd)
}

function confirmCrop() {
  const s = getDisplayScale()
  let srcX = -offsetX.value / s
  let srcY = -offsetY.value / s
  let srcW = FRAME_W / s
  let srcH = FRAME_H / s
  // 边界保护
  if (srcX < 0) { srcW += srcX; srcX = 0 }
  if (srcY < 0) { srcH += srcY; srcY = 0 }
  if (srcX + srcW > naturalW.value) srcW = naturalW.value - srcX
  if (srcY + srcH > naturalH.value) srcH = naturalH.value - srcY

  const canvas = document.createElement('canvas')
  canvas.width = OUT_W
  canvas.height = OUT_H
  const ctx = canvas.getContext('2d')
  ctx.drawImage(imgRef.value, srcX, srcY, srcW, srcH, 0, 0, OUT_W, OUT_H)
  canvas.toBlob((blob) => {
    emit('crop', blob)
    emit('close')
  }, 'image/jpeg', 0.9)
}

// 重置状态当打开时
watch(() => props.show, (val) => {
  if (val && props.imageSrc) {
    nextTick(() => {
      if (imgRef.value && imgRef.value.complete) {
        onImgLoad()
      }
    })
  }
})
</script>

<style scoped>
.crop-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.crop-modal {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.25);
  width: 320px;
  max-width: calc(100vw - 32px);
}
.crop-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px 12px;
  border-bottom: 1px solid #E5E7EB;
}
.crop-title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
}
.crop-close {
  width: 28px; height: 28px;
  border: 0; background: transparent;
  border-radius: 50%;
  color: #6B7280;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.crop-close:hover { background: #F3F4F6; }
.crop-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.crop-frame {
  width: 240px;
  height: 320px;
  position: relative;
  overflow: hidden;
  background: #1F2937;
  cursor: grab;
  border-radius: 8px;
  user-select: none;
}
.crop-frame:active { cursor: grabbing; }
.crop-frame::before,
.crop-frame::after {
  content: '';
  position: absolute;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.7);
  z-index: 10;
  pointer-events: none;
}
.crop-frame::before {
  top: 0; left: 0;
  border-right: 0; border-bottom: 0;
}
.crop-frame::after {
  bottom: 0; right: 0;
  border-left: 0; border-top: 0;
}
.crop-frame img {
  position: absolute;
  pointer-events: none;
  user-select: none;
}
.crop-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
.crop-control-label {
  font-size: 13px;
  color: #6B7280;
  flex-shrink: 0;
}
.crop-controls input[type="range"] {
  flex: 1;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: #E5E7EB;
  border-radius: 3px;
  outline: none;
}
.crop-controls input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 16px; height: 16px;
  border-radius: 50%;
  background: #F59E0B;
  cursor: pointer;
}
.crop-zoom-val {
  font-size: 13px;
  font-weight: 600;
  color: #D97706;
  min-width: 40px;
  text-align: right;
}
.crop-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 20px 16px;
  border-top: 1px solid #E5E7EB;
}
.btn {
  padding: 7px 18px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
}
.btn-ghost {
  background: transparent;
  color: #6B7280;
}
.btn-ghost:hover { background: #F3F4F6; }
.btn-primary {
  background: #F59E0B;
  color: #fff;
}
.btn-primary:hover { background: #D97706; }
</style>
