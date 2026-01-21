import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import Auth0ProviderWithConfig from './auth/Auth0ProviderWithConfig';
import './globals.css';
import './i18n'; // Initialize i18next

ReactDOM.createRoot(document.getElementById('root')).render(
  <Auth0ProviderWithConfig>
    <App />
  </Auth0ProviderWithConfig>
);
