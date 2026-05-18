/** Categorii Rentix — sincronizate cu filtrarea backend */
export const LISTING_CATEGORIES = [
  'Haine',
  'Gadgeturi',
  'Scule',
  'Sport',
  'Console',
  'Auto',
  'Evenimente',
  'Casă & grădină',
  'Altele',
]

/** Starea fizică a produsului */
export const PRODUCT_CONDITIONS = [
  { value: 'NOU', label: 'Nou' },
  { value: 'CA_NOU', label: 'Ca nou' },
  { value: 'PUTIN_FOLOSIT', label: 'Puțin folosit' },
  { value: 'FOLOSIT', label: 'Folosit' },
  { value: 'UZAT', label: 'Uzat / defecte minore' },
]

export function conditionLabel(value) {
  return PRODUCT_CONDITIONS.find((c) => c.value === value)?.label || value || '—'
}
