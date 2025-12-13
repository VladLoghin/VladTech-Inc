import axios from "axios";

const API_BASE = "http://localhost:8080/api/reviews";

export const getAllVisibleReviews = async () => {
    try {
        const res = await axios.get(`${API_BASE}/visible`);
        return res.data;
    } catch (err) {
        throw err;
    }
};

export const getAllReviews = async (token) => {
    const res = await axios.get(`${API_BASE}`, {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
    return res.data;
};


export const getMyReviews = async (token) => {
    try {
        const res = await axios.get(`${API_BASE}/mine`, {
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
    return fetch(`http://localhost:8080/api/reviews/${reviewId}`, {
        method: "DELETE",
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });
};
