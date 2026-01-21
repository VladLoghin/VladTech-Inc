//import axios from "axios";
import { api } from '../http';

const API_BASE = '/reviews';

export const getAllVisibleReviews = async (filters = {}) => {
  const params = {};

  // Only add non-empty filters
  if (filters.clientName?.trim()) {
    params.clientName = filters.clientName.trim();
  }
  if (filters.rating) {
    params.rating = filters.rating;
  }

  const res = await api.get(`${API_BASE}/visible`, { params });
  return res.data;
};

export const getAllReviews = async (token, filters = {}) => {
  const params = {};

  // Only add non-empty filters
  if (filters.clientName?.trim()) {
    params.clientName = filters.clientName.trim();
  }
  if (filters.rating) {
    params.rating = filters.rating;
  }

  const res = await api.get(`${API_BASE}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    params,
  });

  return res.data;
};

export const getMyReviews = async (token) => {
  const res = await api.get(`${API_BASE}/mine`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  return res.data;
};

export const deleteReview = async (reviewId, token) => {
  return api.delete(`${API_BASE}/${reviewId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};
