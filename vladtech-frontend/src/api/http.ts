import axios from 'axios';

export const api = axios.create({
  baseURL: '/api',
});

// Interceptor to add language to all API requests
api.interceptors.request.use((config) => {
  const language = localStorage.getItem('language') || 'en';

  // Add language as a query parameter
  if (!config.params) {
    config.params = {};
  }
  config.params.lang = language;

  return config;
});

export const getLanguagePrefix = () => {
  const language = localStorage.getItem('language') || 'en';
  return language === 'fr' ? '/fr' : '';
};
