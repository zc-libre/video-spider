import { Video, Bell } from 'lucide-react'
import { SearchBar } from './SearchBar'

interface HeaderProps {
  searchValue: string
  onSearchChange: (value: string) => void
  onSearch: (query: string) => void
}

export function Header({ searchValue, onSearchChange, onSearch }: HeaderProps) {
  return (
    <header className="sticky top-0 z-40 w-full bg-white/80 backdrop-blur-md border-b border-slate-200">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between gap-4">
        {/* Logo */}
        <div className="flex items-center gap-2 cursor-pointer group">
          <div className="p-2 bg-blue-600 rounded-lg group-hover:bg-blue-700 transition-colors">
            <Video className="w-6 h-6 text-white" />
          </div>
          <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-purple-600 hidden sm:block">
            VideoHub
          </span>
        </div>

        {/* Search */}
        <div className="flex-1 max-w-2xl hidden md:block">
          <SearchBar value={searchValue} onChange={onSearchChange} onSearch={onSearch} />
        </div>

        {/* Actions */}
        <div className="flex items-center gap-3">
          <button className="p-2 text-slate-600 hover:bg-slate-100 rounded-full transition-colors relative">
            <Bell className="w-6 h-6" />
            <span className="absolute top-1.5 right-1.5 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-white"></span>
          </button>

          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 p-0.5 cursor-pointer hover:scale-105 transition-transform">
            <div className="w-full h-full bg-white rounded-full flex items-center justify-center overflow-hidden">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" alt="User" />
            </div>
          </div>
        </div>
      </div>

      {/* Mobile Search */}
      <div className="md:hidden px-4 pb-3">
        <SearchBar onSearch={onSearch} />
      </div>
    </header>
  )
}
