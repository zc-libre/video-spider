import { Video, LogOut, Settings } from 'lucide-react'
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
    <header className="sticky top-0 z-40 w-full bg-black/72 backdrop-blur-[20px] border-b border-[rgba(84,84,88,0.65)]">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between gap-4">
        {/* Logo */}
        <div className="flex items-center gap-2 cursor-pointer group">
          <div className="p-2 bg-[#0A84FF] rounded-xl transition-opacity group-hover:opacity-80">
            <Video className="w-6 h-6 text-white" />
          </div>
          <span className="text-xl font-bold text-[#F5F5F7] hidden sm:block">
            VideoHub
          </span>
        </div>

        {/* Search */}
        <div className="flex-1 max-w-2xl hidden md:block">
          <SearchBar value={searchValue} onChange={onSearchChange} onSearch={onSearch} />
        </div>

        {/* Actions */}
        <div className="flex items-center gap-3">
          {onShowAdmin && (
            <button
              onClick={onShowAdmin}
              className="p-2 text-[#8E8E93] hover:text-[#F5F5F7] hover:bg-[#2C2C2E] rounded-full transition-colors"
              title="管理面板"
            >
              <Settings className="w-5 h-5" />
            </button>
          )}

          {username && (
            <span className="text-sm text-[#8E8E93] hidden sm:block">{username}</span>
          )}

          <div className="w-10 h-10 rounded-full p-0.5 cursor-pointer ring-2 ring-[#0A84FF]/30 hover:ring-[#0A84FF]/60 transition-all">
            <div className="w-full h-full bg-[#2C2C2E] rounded-full flex items-center justify-center overflow-hidden">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" alt="User" />
            </div>
          </div>

          {onLogout && (
            <button
              onClick={onLogout}
              className="p-2 text-[#8E8E93] hover:text-[#F5F5F7] hover:bg-[#2C2C2E] rounded-full transition-colors"
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
