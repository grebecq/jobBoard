import { useMemo, useRef, useState } from 'react'
import { useDictionaries } from '../dictionaries.jsx'

const POPULAR = ['Java', 'Kotlin', 'Python', 'Go', 'JavaScript', 'TypeScript', 'React', 'Spring Boot',
  'PostgreSQL', 'Docker', 'Kubernetes', 'Kafka', 'C#', 'PHP', 'Vue.js', 'Android SDK']

export default function SkillPicker({ value, onChange, placeholder = 'Java, Kotlin, Docker…', max = 30, compact = false }) {
  const { skills, skillById, label } = useDictionaries()
  const [text, setText] = useState('')
  const [open, setOpen] = useState(false)
  const [active, setActive] = useState(0)
  const inputRef = useRef(null)

  const selected = new Set(value)

  const suggestions = useMemo(() => {
    const q = text.trim().toLowerCase()
    if (!q) {
      return POPULAR.map((name) => skills.find((s) => s.name === name))
        .filter((s) => s && !selected.has(s.id))
        .slice(0, compact ? 8 : 12)
    }
    return skills
      .filter((s) => !selected.has(s.id) && s.name.toLowerCase().includes(q))
      .sort((a, b) => a.name.toLowerCase().startsWith(q) === b.name.toLowerCase().startsWith(q)
        ? a.name.length - b.name.length
        : a.name.toLowerCase().startsWith(q) ? -1 : 1)
      .slice(0, 10)
  }, [text, skills, value, compact])

  function add(skill) {
    if (!skill || selected.has(skill.id) || value.length >= max) return
    onChange([...value, skill.id])
    setText('')
    setActive(0)
    inputRef.current?.focus()
  }

  function remove(id) {
    onChange(value.filter((x) => x !== id))
  }

  function onKeyDown(e) {
    if (e.key === 'ArrowDown') { e.preventDefault(); setActive((i) => Math.min(i + 1, suggestions.length - 1)) }
    else if (e.key === 'ArrowUp') { e.preventDefault(); setActive((i) => Math.max(i - 1, 0)) }
    else if (e.key === 'Enter') {
      if (open && suggestions[active]) { e.preventDefault(); add(suggestions[active]) }
    } else if (e.key === 'Backspace' && !text && value.length) {
      remove(value[value.length - 1])
    } else if (e.key === 'Escape') {
      setOpen(false)
    }
  }

  return (
    <div className={'skill-picker' + (compact ? ' compact' : '')}>
      <div className="sp-box" onClick={() => inputRef.current?.focus()}>
        {value.map((id) => (
          <span className="sp-chip" key={id}>
            {skillById[id]?.name || '#' + id}
            <button type="button" aria-label="Убрать" onClick={(e) => { e.stopPropagation(); remove(id) }}>×</button>
          </span>
        ))}
        <input
          ref={inputRef}
          value={text}
          placeholder={value.length ? '' : placeholder}
          onChange={(e) => { setText(e.target.value); setOpen(true); setActive(0) }}
          onFocus={() => setOpen(true)}
          onBlur={() => setTimeout(() => setOpen(false), 150)}
          onKeyDown={onKeyDown}
        />
      </div>
      {open && suggestions.length > 0 && (
        <div className="sp-menu">
          {!text && <div className="sp-hint">Популярные</div>}
          {suggestions.map((s, i) => (
            <button type="button" key={s.id}
              className={'sp-item' + (i === active ? ' active' : '')}
              onMouseDown={(e) => e.preventDefault()}
              onMouseEnter={() => setActive(i)}
              onClick={() => add(s)}>
              <span>{s.name}</span>
              <span className="sp-cat">{label('skillCategory', s.category)}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
