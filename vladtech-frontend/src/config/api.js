// API configuration - uses environment variable or defaults based on context
const getApiBaseUrl = () => {
    // Check for environment variable first (set at build time)
    if (import.meta.env.VITE_API_URL) {
        return import.meta.env.VITE_API_URL;
    }

    // In production (Docker), use relative URLs - Nginx proxies /api to backend
    if (import.meta.env.PROD) {
        return '';
    }

    // In development, use localhost
    return 'http://localhost:8080';
};

export const API_BASE_URL = getApiBaseUrl();

// Helper function to build API URLs
export const apiUrl = (path) => `${API_BASE_URL}${path}`;
