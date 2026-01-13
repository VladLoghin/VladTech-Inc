//import axios from "axios";
import { api } from "../http";

const API_BASE = "/reviews";

export const getAllVisibleReviews = async () => {
    try {
        const res = await api.get(`${API_BASE}/visible`);
        return res.data;
    } catch (err) {
        throw err;
    }
};

export const getAllReviews = async (token) => {
    const res = await api.get(`${API_BASE}`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
    return res.data;
};


export const getMyReviews = async (token) => {
    try {
        const res = await api.get(`${API_BASE}/mine`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return res.data;
    } catch (err) {
        throw err;
    }
};

export const deleteReview = async (reviewId, token) => {
    return api.delete(`${API_BASE}/${reviewId}`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
};
