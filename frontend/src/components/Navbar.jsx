import React from 'react';
import { LogOut, Briefcase, FileText } from 'lucide-react';

export default function Navbar({ user, onLogout }) {
  return (
    <nav className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between sticky top-0 z-50">
      <div className="flex items-center space-x-3">
        <div className="p-2 bg-indigo-600 rounded-lg text-white">
          <Briefcase className="w-5 h-5" />
        </div>
        <span className="font-bold text-lg text-slate-800 tracking-tight">MatchAI</span>
        <span className="text-xs font-semibold px-2 py-0.5 rounded bg-slate-100 text-slate-600 uppercase">
          Microservices
        </span>
      </div>

      <div className="flex items-center space-x-4">
        {user ? (
          <>
            <div className="text-right">
              <p className="text-sm font-semibold text-slate-800">{user.fullName}</p>
              <p className="text-xs text-indigo-600 font-medium tracking-wide">{user.role}</p>
            </div>
            <button
              onClick={onLogout}
              className="flex items-center space-x-1 text-sm bg-slate-100 hover:bg-red-50 hover:text-red-600 text-slate-700 font-medium px-3 py-2 rounded-lg transition"
            >
              <LogOut className="w-4 h-4" />
              <span>Logout</span>
            </button>
          </>
        ) : null}
      </div>
    </nav>
  );
}