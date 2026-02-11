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
            className="w-full pl-10 pr-16 py-2.5 bg-[#1C1C1E] rounded-xl focus:bg-[#2C2C2E] focus:ring-2 focus:ring-[#0A84FF]/50 focus:outline-none transition-colors text-[#F5F5F7] placeholder-[#636366] text-sm"
          />
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#636366] group-focus-within:text-[#0A84FF] transition-colors" />
          <button
            type="submit"
            className="absolute right-1.5 top-1/2 -translate-y-1/2 bg-[#0A84FF] hover:bg-[#0A84FF]/80 text-white px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors"
          >
            搜索
          </button>
        </div>
      </form>

      {showSuggestions && suggestions.length > 0 && (
        <ul className="absolute z-50 w-full mt-2 bg-[#2C2C2E] border border-[rgba(84,84,88,0.65)] rounded-xl shadow-2xl shadow-black/50 overflow-hidden">
          {suggestions.map((text, index) => (
            <li
              key={text}
              onMouseDown={() => handleSelect(text)}
              onMouseEnter={() => setActiveIndex(index)}
              className={`flex items-center gap-3 px-4 py-2.5 cursor-pointer transition-colors ${
                index === activeIndex
                  ? 'bg-[#3A3A3C] text-[#F5F5F7]'
                  : 'text-[#8E8E93] hover:bg-[#3A3A3C]/50'
              }`}
            >
              <Search className="w-4 h-4 text-[#636366] shrink-0" />
              <span className="truncate text-sm">{text}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
