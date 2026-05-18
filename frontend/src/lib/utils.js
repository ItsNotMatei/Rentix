import { clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs) {
  return twMerge(clsx(inputs))
}

// Gol în dev = proxy Vite (cookies HttpOnly same-origin). Production: setează VITE_API_URL.
export const API_BASE = import.meta.env.VITE_API_URL ?? ''
export const GOOGLE_MAPS_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY || ''
export const CLOUDINARY_CLOUD = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME || 'dn2hvsk0o'
export const CLOUDINARY_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET || 'rentix_presets'
