export function formatSalary(from, to) {
  const f = (n) => Number(n).toLocaleString('ru-RU')
  if (from && to) return `${f(from)} – ${f(to)} ₽`
  if (from) return `от ${f(from)} ₽`
  if (to) return `до ${f(to)} ₽`
  return 'з/п не указана'
}

export const APPLICATION_STATUS = {
  PENDING:  { cls: 'pending',  label: 'Отправлен' },
  VIEWED:   { cls: 'viewed',   label: 'Просмотрен' },
  INVITED:  { cls: 'invited',  label: 'Приглашение' },
  REJECTED: { cls: 'rejected', label: 'Отказ' },
}
export function applicationStatus(status) {
  return APPLICATION_STATUS[status] || { cls: 'pending', label: status }
}

export function formatDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const opts = { day: 'numeric', month: 'long' }
  if (d.getFullYear() !== new Date().getFullYear()) opts.year = 'numeric'
  return d.toLocaleDateString('ru-RU', opts)
}

export function telegramUrl(handle) {
  return handle ? `https://t.me/${handle}` : null
}
