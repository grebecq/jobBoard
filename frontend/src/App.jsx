import { useState } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import AuthModal from './components/AuthModal.jsx'
import Home from './pages/Home.jsx'
import VacancyDetail from './pages/VacancyDetail.jsx'
import CandidateDashboard from './pages/CandidateDashboard.jsx'
import EmployerDashboard from './pages/EmployerDashboard.jsx'
import VacancyApplications from './pages/VacancyApplications.jsx'
import { useAuth } from './auth.jsx'

function ProtectedRoute({ role, children }) {
  const { isAuthenticated, hasRole, loading } = useAuth()
  if (loading) return <div className="spinner" />
  if (!isAuthenticated) return <Navigate to="/" replace />
  if (role && !hasRole(role) && !hasRole('ADMIN')) return <Navigate to="/" replace />
  return children
}

export default function App() {
  const [authOpen, setAuthOpen] = useState(null)

  return (
    <>
      <Navbar onAuth={setAuthOpen} />

      <Routes>
        <Route path="/" element={<Home onAuth={setAuthOpen} />} />
        <Route path="/vacancy/:id" element={<VacancyDetail onAuth={setAuthOpen} />} />
        <Route
          path="/my/applications"
          element={
            <ProtectedRoute role="CANDIDATE">
              <CandidateDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my/vacancies"
          element={
            <ProtectedRoute role="EMPLOYER">
              <EmployerDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my/vacancies/:id/applications"
          element={
            <ProtectedRoute role="EMPLOYER">
              <VacancyApplications />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>

      <footer>© 2026 Вакант</footer>

      {authOpen && <AuthModal mode={authOpen} onClose={() => setAuthOpen(null)} />}
    </>
  )
}
