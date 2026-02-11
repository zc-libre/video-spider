import { Search } from 'lucide-react'
import { useState, useEffect, useRef } from 'react'
import { videoApi } from '@/api/video'

interface SearchBarProps {
  value: string
  onChange: (value: string) => void
  onSearch: (query: string) => void
}

export function SearchBar({ value, onChange, onSearch }: SearchBarProps) {
  const [suggestions, setSuggestions] = useState<string[]>([])
  const [showSuggestions, setShowSuggestions] = useState(false)
  const [activeIndex, setActiveIndex] = useState(-1)
  const containerRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    const trimmed = value.trim()
    if (trimmed.length < 2) {
      setSuggestions([])
      return
    }
    const timer = setTimeout(async () => {
      try {
        const result = await videoApi.getSuggestions(trimmed)
        setSuggestions(result || [])
        setShowSuggestions(true)
        setActiveIndex(-1)
      } catch {
        setSuggestions([])
      }
    }, 300)
    return () => clearTimeout(timer)
  }, [value])

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setShowSuggestions(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setShowSuggestions(false)
    onSearch(value)
  }

  const handleSelect = (text: string) => {
    onChange(text)
    setShowSuggestions(false)
    onSearch(text)
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!showSuggestions || suggestions.length === 0) return

    if (e.key === 'ArrowDown') {
      e.preventDefault()
      setActiveIndex((prev) => (prev < suggestions.length - 1 ? prev + 1 : 0))
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      setActiveIndex((prev) => (prev > 0 ? prev - 1 : suggestions.length - 1))
    } else if (e.key === 'Enter' && activeIndex >= 0) {
      e.preventDefault()
      handleSelect(suggestions[activeIndex])
    } else if (e.key === 'Escape') {
      setShowSuggestions(false)
    }
  }

  return (
    <div ref={containerRef} className="relative w-full">
      <form onSubmit={handleSubmit}>
        <div className="relative group">
          <input
            ref={inputRef}
            type="text"
            value={value}
            onChange={(e) => onChange(e.target.value)}
            onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
            onKeyDown={handleKeyDown}
            placeholder="搜索视频..."
            className="w-full pl-12 pr-4 py-3 bg-white/5 border border-white/10 rounded-full focus:bg-white/[0.08] focus:ring-2 focus:ring-rose-500/50 focus:border-rose-500/30 focus:outline-none transition-all duration-300 text-slate-200 placeholder-slate-500"
          />
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-500 group-focus-within:text-rose-400 transition-colors" />
          <button
            type="submit"
            className="absolute right-2 top-1/2 -translate-y-1/2 bg-gradient-to-r from-rose-600 to-rose-500 hover:from-rose-500 hover:to-rose-400 text-white px-4 py-1.5 rounded-full text-sm font-medium transition-all shadow-lg shadow-rose-600/20"
          >
            搜索
          </button>
        </div>
      </form>

      {showSuggestions && suggestions.length > 0 && (
        <ul className="absolute z-50 w-full mt-2 bg-[#1a1a2e]/95 backdrop-blur-xl border border-white/10 rounded-2xl shadow-2xl shadow-black/40 overflow-hidden">
          {suggestions.map((text, index) => (
            <li
              key={text}
              onMouseDown={() => handleSelect(text)}
              onMouseEnter={() => setActiveIndex(index)}
              className={`flex items-center gap-3 px-4 py-2.5 cursor-pointer transition-colors ${
                index === activeIndex
                  ? 'bg-white/10 text-white'
                  : 'text-slate-300 hover:bg-white/5'
              }`}
            >
              <Search className="w-4 h-4 text-slate-500 shrink-0" />
              <span className="truncate text-sm">{text}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
