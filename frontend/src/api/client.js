import axios from 'axios';

const baseURL = import.meta.env.VITE_GATEWAY_URL || 'http://localhost:8080';

const client = axios.create({
  // baseURL: API_BASE,
  baseURL: baseURL,
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default client;