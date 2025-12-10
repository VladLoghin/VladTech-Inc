import React, { useEffect, useState } from "react";
import ReviewCarousel from "../components/reviews/ReviewCarousel";
import SecondaryNavbar from "../components/SecondaryNavbar";
import { getAllVisibleReviews } from "../api/reviews/reviewsService";
import "../components/reviews/Review.css";
import ReviewModal from "../components/reviews/ReviewModal";
import ReviewDetailModal from "../components/reviews/ReviewDetailModal";
import { useAuth0 } from "@auth0/auth0-react";

const ReviewsPage = () => {
    const [reviews, setReviews] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [selectedReview, setSelectedReview] = useState(null);
    const [roles, setRoles] = useState([]);

    const { user } = useAuth0();

    useEffect(() => {
        if (user) {
            const userRoles = user["https://vladtech.com/roles"] || [];
            setRoles(userRoles);
        }
        fetchReviews();
    }, [user]);

    const fetchReviews = () => {
        getAllVisibleReviews()
            .then((data) => setReviews(data))
            .catch((err) => console.error("Failed to fetch reviews:", err));
    };

    const handleOpenModal = () => setShowModal(true);
    const handleCloseModal = () => setShowModal(false);
    const handleReviewClick = (review) => {
        setSelectedReview(review);
        setShowDetailModal(true);
    };

    const handleCloseDetailModal = () => {
        setShowDetailModal(false);
        setSelectedReview(null);
    };
    const isClient = roles.includes("Client");

    return (
        <div className="reviews-page" data-testid="reviews-page">
            <SecondaryNavbar />

            <div className="container mx-auto p-4">
                {isClient && (
                    <button
                        type="button"
                        style={{ backgroundColor: '#FCC700' }}
                        className="px-4 py-2 text-black rounded hover:bg-yellow-500 mb-4"
                        onClick={handleOpenModal}
                        data-testid="add-review-button"
                    >
                        Add Review
                    </button>
                )}

                <section className="mb-10" data-testid="reviews-carousel-section">
                    <h2 className="title">Customer Highlights</h2>
                    <ReviewCarousel
                        reviews={reviews}
                        onReviewClick={handleReviewClick}
                    />
                </section>
            </div>

            <ReviewModal
                open={showModal}
                onClose={handleCloseModal}
                onSubmitSuccess={(newReview) => {
                    setReviews((prev) => [newReview, ...prev]); // auto-refresh
                    handleCloseModal();
                }}
            />

            <ReviewDetailModal
                review={selectedReview}
                open={showDetailModal}
                onClose={handleCloseDetailModal}
            />
        </div>
    );
};

export default ReviewsPage;
