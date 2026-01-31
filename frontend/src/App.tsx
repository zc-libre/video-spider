import { HomePage } from '@/pages/HomePage'
import { LoginPage } from '@/pages/LoginPage'
import { useAuth } from '@/hooks/useAuth'

function App() {
  const { isLoggedIn, loading, login, logout, username } = useAuth()

  if (!isLoggedIn) {
    return <LoginPage onLogin={login} loading={loading} />
  }

  return <HomePage onLogout={logout} username={username} />
}

export default App
