import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { dictionariesApi } from './api.js'

const EMPTY = {
  specializations: [], grades: [], experiences: [], workFormats: [],
  employmentTypes: [], skillCategories: [], skills: [],
}

const DictCtx = createContext(null)

/**
 * Справочники (специализации, грейды, навыки…) грузятся один раз на всё приложение.
 * Подписи живут на бэке в enum-ах — фронт их не хардкодит.
 */
export function DictionariesProvider({ children }) {
  const [data, setData] = useState(EMPTY)
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    dictionariesApi.get()
      .then(setData)
      .catch(() => {})
      .finally(() => setLoaded(true))
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
