export function formatSalary(from, to) {
  const f = (n) => Number(n).toLocaleString('ru-RU')
  if (from && to) return `${f(from)} – ${f(to)} ₽`
  if (from) return `от ${f(from)} ₽`
  if (to) return `до ${f(to)} ₽`
  return 'з/п не указана'
}

// подписи специализаций, грейдов и т.п. — в dictionaries.jsx (приходят с бэка)

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
  return iso ? new Date(iso).toLocaleDateString('ru-RU') : ''
}

// бэк хранит голый ник без @
export function telegramUrl(handle) {
  return handle ? `https://t.me/${handle}` : null
}
