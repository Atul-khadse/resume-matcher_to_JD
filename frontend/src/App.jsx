import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Login from './components/Login';
import Register from './components/Register';
import RecruiterDashboard from './components/RecruiterDashboard';
import CandidateDashboard from './components/CandidateDashboard';

export default function App() {
  const [user, setUser] = useState(null);
  const [authMode, setAuthMode] = useState('login'); // 'login' or 'register'

  useEffect(() => {
    const saved = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    if (saved && token) {
      setUser(JSON.parse(saved));
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar user={user} onLogout={handleLogout} />

      <main className="py-8">
        {!user ? (
          <div className="flex items-center justify-center min-h-[calc(100vh-140px)] px-4">
            {authMode === 'login' ? (
              <Login
                onLoginSuccess={(userData) => setUser(userData)}
                switchToRegister={() => setAuthMode('register')}
              />
            ) : (
              <Register
                onRegisterSuccess={(userData) => setUser(userData)}
                switchToLogin={() => setAuthMode('login')}
              />
            )}
          </div>
        ) : user.role === 'RECRUITER' ? (
          <RecruiterDashboard />
        ) : (
          <CandidateDashboard />
        )}
      </main>
    </div>
  );
}