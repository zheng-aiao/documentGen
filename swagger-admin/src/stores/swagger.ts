import { defineStore } from 'pinia'

export type SwaggerItem = {
  id: string
  name: string
  url: string
  remark?: string
  isDefault?: boolean
}

const LS_KEY = 'swagger-items'
const LS_CUR = 'swagger-current'

export const useSwaggerStore = defineStore('swagger', {
  state: () => ({
    items: [] as SwaggerItem[],
    currentId: '' as string
  }),
  getters: {
    current(state) {
      return state.items.find(x => x.id === state.currentId) || null
    }
  },
  actions: {
    add(item: SwaggerItem) {
      this.items.push(item)
      if (item.isDefault) this.currentId = item.id
      this.persist()
    },
    update(id: string, patch: Partial<SwaggerItem>) {
      const i = this.items.findIndex(x => x.id === id)
      if (i > -1) {
        const wasDefault = this.items[i].isDefault
        this.items[i] = { ...this.items[i], ...patch }
        if (!wasDefault && this.items[i].isDefault) this.currentId = id
        this.persist()
      }
    },
    remove(id: string) {
      this.items = this.items.filter(x => x.id !== id)
      if (this.currentId === id) this.currentId = this.items[0]?.id || ''
      this.persist()
    },
    setCurrent(id: string) {
      this.currentId = id
      this.persist()
    },
    load() {
      try {
        const raw = localStorage.getItem(LS_KEY)
        if (raw) this.items = JSON.parse(raw)
        const cur = localStorage.getItem(LS_CUR)
        if (cur) this.currentId = cur
        if (!this.currentId && this.items.length) {
          this.currentId = this.items.find(x => x.isDefault)?.id || this.items[0].id
        }
      } catch {}
    },
    persist() {
      localStorage.setItem(LS_KEY, JSON.stringify(this.items))
      localStorage.setItem(LS_CUR, this.currentId)
    }
  }
})