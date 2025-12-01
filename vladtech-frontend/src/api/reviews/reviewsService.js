import axios from "axios";

const API_BASE = "http://localhost:8080/api/v1/reviews";

export const getAllVisibleReviews = async () => {
    try {
        const res = await axios.get(`${API_BASE}/visible`);
        return res.data;
    } catch (err) {
        console.error("Failed to fetch reviews:", err);
        throw err;
    }
};
