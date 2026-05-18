const listeners = new Set()

export function subscribeToast(listener) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

function emit(toast) {
  listeners.forEach((fn) => fn(toast))
}

export const toast = {
  success(message, duration = 4000) {
    emit({ id: Date.now(), message, type: 'success', duration })
  },
  error(message, duration = 5000) {
    emit({ id: Date.now() + 1, message, type: 'error', duration })
  },
  info(message, duration = 4000) {
    emit({ id: Date.now() + 2, message, type: 'info', duration })
  },
}
