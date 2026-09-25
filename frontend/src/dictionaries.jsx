import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { dictionariesApi } from './api.js'

const EMPTY = {
  specializations: [], grades: [], experiences: [], workFormats: [],
  employmentTypes: [], skillCategories: [], skills: [],
}

const DictCtx = createContext(null)

export function DictionariesProvider({ children }) {
  const [data, setData] = useState(EMPTY)
  const [loaded, setLoaded] = useState(false)

  // при запуске бэкенд может ещё подниматься, поэтому пробуем несколько раз
  useEffect(() => {
    let timer
    let attempt = 0
    const load = () => dictionariesApi.get()
      .then((d) => { setData(d); setLoaded(true) })
      .catch(() => {
        if (++attempt < 20) timer = setTimeout(load, 3000)
        else setLoaded(true)
      })
    load()
    return () => clearTimeout(timer)
  }, [])

  const value = useMemo(() => {
    const index = (list) => Object.fromEntries(list.map((o) => [o.value, o.label]))
    const labels = {
      specialization: index(data.specializations),
      grade: index(data.grades),
      experience: index(data.experiences),
      workFormat: index(data.workFormats),
      employmentType: index(data.employmentTypes),
      skillCategory: index(data.skillCategories),
    }
    const skillById = Object.fromEntries(data.skills.map((s) => [s.id, s]))
    return {
      ...data,
      loaded,
      skillById,
      label: (kind, value) => (value ? labels[kind]?.[value] || value : ''),
    }
  }, [data, loaded])

  return <DictCtx.Provider value={value}>{children}</DictCtx.Provider>
}

export const useDictionaries = () => useContext(DictCtx)
