import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Plans from './components/Plans';
import Main from './components/Main';
import History from './components/History';

function App() {
  const [userId, setUserId] = useState(localStorage.getItem('userId'));
  
  useEffect(() => {
    // Listen for login event from Login component
    const handleLogin = () => {
      setUserId(localStorage.getItem('userId'));
    };
    
    window.addEventListener('userLoggedIn', handleLogin);
    window.addEventListener('storage', handleLogin);
    
    return () => {
      window.removeEventListener('userLoggedIn', handleLogin);
      window.removeEventListener('storage', handleLogin);
    };
  }, []);
  
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/plans" element={userId ? <Plans /> : <Navigate to="/login" />} />
        <Route path="/main" element={userId ? <Main /> : <Navigate to="/login" />} />
        <Route path="/history" element={userId ? <History /> : <Navigate to="/login" />} />
        <Route path="/" element={userId ? <Navigate to="/main" /> : <Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;
