import { Video, Bell, LogOut, Settings } from 'lucide-react'
import { SearchBar } from './SearchBar'

interface HeaderProps {
  searchValue: string
  onSearchChange: (value: string) => void
  onSearch: (query: string) => void
  onLogout?: () => void
  onShowAdmin?: () => void
  username?: string
}

export function Header({ searchValue, onSearchChange, onSearch, onLogout, onShowAdmin, username }: HeaderProps) {
  return (
    <header className="sticky top-0 z-40 w-full bg-black/60 backdrop-blur-2xl border-b border-white/[0.06]">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between gap-4">
        {/* Logo */}
        <div className="flex items-center gap-2 cursor-pointer group">
          <div className="p-2 bg-gradient-to-br from-rose-600 to-violet-600 rounded-lg group-hover:shadow-lg group-hover:shadow-rose-600/20 transition-all">
            <Video className="w-6 h-6 text-white" />
          </div>
          <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-rose-500 to-violet-500 hidden sm:block">
            VideoHub
          </span>
        </div>

        {/* Search */}
        <div className="flex-1 max-w-2xl hidden md:block">
          <SearchBar value={searchValue} onChange={onSearchChange} onSearch={onSearch} />
        </div>

        {/* Actions */}
        <div className="flex items-center gap-3">
          <button className="p-2 text-slate-400 hover:text-slate-200 hover:bg-white/5 rounded-full transition-colors relative">
            <Bell className="w-6 h-6" />
            <span className="absolute top-1.5 right-1.5 w-2.5 h-2.5 bg-rose-500 rounded-full border-2 border-[#0a0a12]"></span>
          </button>

          {onShowAdmin && (
            <button
              onClick={onShowAdmin}
              className="p-2 text-slate-400 hover:text-slate-200 hover:bg-white/5 rounded-full transition-colors"
              title="管理面板"
            >
              <Settings className="w-5 h-5" />
            </button>
          )}

          {username && (
            <span className="text-sm text-slate-400 hidden sm:block">{username}</span>
          )}

          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-rose-500 to-violet-600 p-0.5 cursor-pointer hover:scale-105 transition-transform ring-2 ring-rose-500/20">
            <div className="w-full h-full bg-[#0a0a12] rounded-full flex items-center justify-center overflow-hidden">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" alt="User" />
            </div>
          </div>

          {onLogout && (
            <button
              onClick={onLogout}
              className="p-2 text-slate-400 hover:text-slate-200 hover:bg-white/5 rounded-full transition-colors"
              title="退出登录"
            >
              <LogOut className="w-5 h-5" />
            </button>
          )}
        </div>
      </div>

      {/* Mobile Search */}
      <div className="md:hidden px-4 pb-3">
        <SearchBar value={searchValue} onChange={onSearchChange} onSearch={onSearch} />
      </div>
    </header>
  )
}
