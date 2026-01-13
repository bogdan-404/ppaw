import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';

function History() {
  const [history, setHistory] = useState([]);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    try {
      const response = await apiClient.get('/api/history');
      setHistory(response.data);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this work?')) {
      return;
    }
    try {
      await apiClient.delete(`/api/history/${id}`);
      loadHistory();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="container mt-4">
      <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4">
        <div className="container-fluid">
          <span className="navbar-brand">PPAW</span>
          <div>
            <button className="btn btn-link" onClick={() => navigate('/main')}>Main</button>
            <button className="btn btn-link" onClick={() => navigate('/plans')}>Plans</button>
            <button className="btn btn-link" onClick={() => {
              localStorage.removeItem('userId');
              navigate('/login');
            }}>Logout</button>
          </div>
        </div>
      </nav>

      <h2>History</h2>
      {error && <div className="alert alert-danger">{error}</div>}

      {history.length === 0 ? (
        <p>No saved works yet.</p>
      ) : (
        <div className="list-group">
          {history.map((work) => (
            <div key={work.id} className="list-group-item">
              <div className="d-flex justify-content-between align-items-start">
                <div>
                  <h5>{work.workType} - {work.style || 'N/A'}</h5>
                  <p><strong>Input:</strong> {work.inputText}</p>
                  <p><strong>Output:</strong> {work.outputText}</p>
                  <small className="text-muted">
                    {new Date(work.createdAt).toLocaleString()}
                  </small>
                </div>
                <button
                  className="btn btn-danger btn-sm"
                  onClick={() => handleDelete(work.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default History;
