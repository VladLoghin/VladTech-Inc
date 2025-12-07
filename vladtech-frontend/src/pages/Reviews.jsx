import React, { useEffect, useState } from "react";
import ReviewCarousel from "../components/reviews/ReviewCarousel.tsx";
import SecondaryNavbar from "../components/SecondaryNavbar";
import { getAllVisibleReviews } from "../api/reviews/reviewsService";
import "../components/reviews/Review.css";

const ReviewsPage = () => {
    const [reviews, setReviews] = useState([]);

    useEffect(() => {
        getAllVisibleReviews()
            .then((data) => setReviews(data))
            .catch((err) => console.error("Failed to fetch reviews:", err));
    }, []);

    return (
        <div className="reviews-page" data-testid="reviews-page">
            <SecondaryNavbar />

            <div className="container mx-auto p-4">
                <section className="mb-10" data-testid="reviews-carousel-section">
                    <h2 className="title">Customer Highlights</h2>
                    <ReviewCarousel />
                </section>
            </div>
        </div>
    );
};

export default ReviewsPage;
