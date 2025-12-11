import React, { useEffect, useState } from "react";
import ReviewCarousel from "../components/reviews/ReviewCarousel";
import SecondaryNavbar from "../components/SecondaryNavbar";
import { getAllVisibleReviews, getAllReviews } from "../api/reviews/reviewsService";
import "../components/reviews/Review.css";
import ReviewModal from "../components/reviews/ReviewModal";
import ReviewDetailModal from "../components/reviews/ReviewDetailModal";
import { useAuth0 } from "@auth0/auth0-react";
import { useNavigate } from "react-router-dom";

const ReviewsPage = () => {
    const navigate = useNavigate();
    const [reviews, setReviews] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [selectedReview, setSelectedReview] = useState(null);
    const [roles, setRoles] = useState([]);

    const { user, isAuthenticated } = useAuth0();

    const isStaff =
        isAuthenticated &&
        Array.isArray(user?.["https://vladtech.com/roles"]) &&
        user["https://vladtech.com/roles"].some(
            (r) => r === "Admin" || r === "Employee"
        );

    useEffect(() => {
        if (user) {
            const userRoles = user["https://vladtech.com/roles"] || [];
            setRoles(userRoles);
        }

        fetchReviews();
    }, [user]);

    const fetchReviews = () => {
        const fetchFn = isStaff ? getAllReviews : getAllVisibleReviews;

        fetchFn()
            .then((data) => setReviews(data))
            .catch((err) => console.error("Failed to fetch reviews:", err));
    };

;

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
            {/* Glossy Navigation Bar */}
            <nav className="fixed top-0 left-0 right-0 z-50 bg-black/60 backdrop-blur-xl border-b border-yellow-400/20 shadow-2xl">
                <div className="container mx-auto px-8 py-6 flex items-center justify-between">
                    <button
                        onClick={() => navigate("/")}
                        className="text-2xl text-white tracking-widest hover:text-yellow-400 transition-colors"
                    >
                        VLADTECH
                    </button>

                    <div className="absolute left-1/2 transform -translate-x-1/2 flex gap-12">
                        <button
                            onClick={() => navigate("/portfolio")}
                            className="text-white/40 hover:text-yellow-400 transition-colors tracking-wider text-sm"
                        >
                            PORTFOLIO
                        </button>
                        <button
                            onClick={() => navigate("/reviews")}
                            className="text-white hover:text-yellow-400 transition-colors tracking-wider text-sm border-b-2 border-yellow-400"
                        >
                            REVIEWS
                        </button>
                    </div>
                </div>
            </nav>

            <div className="container mx-auto p-4" style={{ marginTop: "100px" }}>
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
                    setReviews((prev) => [newReview, ...prev]); 
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
