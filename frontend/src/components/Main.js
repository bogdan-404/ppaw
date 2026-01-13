import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';

function Main() {
  const [text, setText] = useState('');
  const [style, setStyle] = useState('simple');
  const [output, setOutput] = useState('');
  const [availableStyles, setAvailableStyles] = useState(['simple']);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const loadSubscription = useCallback(async () => {
    try {
      const response = await apiClient.get('/api/subscription');
      const plansResponse = await apiClient.get('/api/plans');
      const currentPlan = plansResponse.data.find(p => p.code === response.data.planCode);
      if (currentPlan?.limits?.allowed_styles) {
        const styles = currentPlan.limits.allowed_styles.split(',');
        setAvailableStyles(styles);
        setStyle(prevStyle => {
          if (!styles.includes(prevStyle)) {
            return styles[0] || 'simple';
          }
          return prevStyle;
        });
      }
    } catch (err) {
      setError('Failed to load subscription');
    }
  }, []);

  useEffect(() => {
    loadSubscription();
  }, [loadSubscription]);

  const handleSummarize = async () => {
    setError('');
    setOutput('');
    setLoading(true);
    try {
      const response = await apiClient.post('/api/text/summarize', { text, style });
      setOutput(response.data.output);
    } catch (err) {
      const message = err.response?.data?.message || err.message;
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  const handleRewrite = async () => {
    setError('');
    setOutput('');
    setLoading(true);
    try {
      const response = await apiClient.post('/api/text/rewrite', { text, style });
      setOutput(response.data.output);
    } catch (err) {
      const message = err.response?.data?.message || err.message;
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-4">
      <nav className="navbar navbar-expand-lg navbar-light bg-light mb-4">
        <div className="container-fluid">
          <span className="navbar-brand">PPAW</span>
          <div>
            <button className="btn btn-link" onClick={() => navigate('/plans')}>Plans</button>
            <button className="btn btn-link" onClick={() => navigate('/history')}>History</button>
            <button className="btn btn-link" onClick={() => {
              localStorage.removeItem('userId');
              navigate('/login');
            }}>Logout</button>
          </div>
        </div>
      </nav>

      <h2>Text Summarizer & Rewriter</h2>
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="mb-3">
        <label className="form-label">Text Input</label>
        <textarea
          className="form-control"
          rows="10"
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Enter your text here..."
        />
      </div>

      <div className="mb-3">
        <label className="form-label">Style</label>
        <select
          className="form-control"
          value={style}
          onChange={(e) => setStyle(e.target.value)}
        >
          {availableStyles.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
      </div>

      <div className="mb-3">
        <button
          className="btn btn-primary me-2"
          onClick={handleSummarize}
          disabled={!text || loading}
        >
          {loading ? 'Processing...' : 'Summarize'}
        </button>
        <button
          className="btn btn-success"
          onClick={handleRewrite}
          disabled={!text || loading}
        >
          {loading ? 'Processing...' : 'Rewrite'}
        </button>
      </div>

      {output && (
        <div className="mt-4">
          <label className="form-label">Output</label>
          <textarea
            className="form-control"
            rows="10"
            value={output}
            readOnly
          />
        </div>
      )}
    </div>
  );
}

export default Main;
