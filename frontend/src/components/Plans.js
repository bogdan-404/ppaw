import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';

function Plans() {
  const [plans, setPlans] = useState([]);
  const [subscription, setSubscription] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    loadPlans();
    loadSubscription();
  }, []);

  const loadPlans = async () => {
    try {
      const response = await apiClient.get('/api/plans');
      setPlans(response.data);
    } catch (err) {
      setError(err.message);
    }
  };

  const loadSubscription = async () => {
    try {
      const response = await apiClient.get('/api/subscription');
      setSubscription(response.data);
    } catch (err) {
      // Ignore if no subscription
    }
  };

  const handlePay = async (planCode) => {
    try {
      setError('');
      setSuccess('');
      await apiClient.post('/api/subscription/pay', { planCode });
      setSuccess('Subscription activated successfully!');
      loadSubscription();
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  };

  const handleCancel = async () => {
    try {
      setError('');
      setSuccess('');
      await apiClient.post('/api/subscription/cancel');
      setSuccess('Subscription canceled');
      loadSubscription();
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
            <button className="btn btn-link" onClick={() => navigate('/history')}>History</button>
            <button className="btn btn-link" onClick={() => {
              localStorage.removeItem('userId');
              navigate('/login');
            }}>Logout</button>
          </div>
        </div>
      </nav>

      <h2>Subscription Plans</h2>
      {subscription && (
        <div className="alert alert-info">
          Current Plan: {subscription.planName} ({subscription.status})
        </div>
      )}
      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="row">
        {plans.map((plan) => (
          <div key={plan.id} className="col-md-4 mb-4">
            <div className="card">
              <div className="card-body">
                <h5 className="card-title">{plan.name}</h5>
                <p className="card-text">${plan.priceCents / 100}</p>
                <ul>
                  {plan.limits && Object.entries(plan.limits).map(([key, value]) => (
                    <li key={key}><strong>{key}:</strong> {value}</li>
                  ))}
                </ul>
                <button
                  className="btn btn-primary"
                  onClick={() => handlePay(plan.code)}
                  disabled={subscription?.planCode === plan.code}
                >
                  {subscription?.planCode === plan.code ? 'Current Plan' : 'Achita'}
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {subscription && subscription.status === 'ACTIVE' && (
        <div className="mt-4">
          <button className="btn btn-danger" onClick={handleCancel}>
            Cancel Subscription
          </button>
        </div>
      )}
    </div>
  );
}

export default Plans;
