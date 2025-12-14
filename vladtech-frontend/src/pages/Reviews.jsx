import React, { useEffect, useState, useCallback } from "react";
import ReviewCarousel from "../components/reviews/ReviewCarousel";
import ReviewModal from "../components/reviews/ReviewModal";
import ReviewDetailModal from "../components/reviews/ReviewDetailModal";
import {
    getAllVisibleReviews,
    getAllReviews,
    getMyReviews,
} from "../api/reviews/reviewsService";
import { useAuth0 } from "@auth0/auth0-react";
import { useNavigate } from "react-router-dom";
import "../components/reviews/Review.css";

const ReviewsPage = () => {
    const navigate = useNavigate();

    const {
        user,
        isAuthenticated,
        isLoading,
        getAccessTokenSilently,
    } = useAuth0();

    const [reviews, setReviews] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [showDetailModal, setShowDetailModal] = useState(false);
    const [selectedReview, setSelectedReview] = useState(null);
    const [showMine, setShowMine] = useState(false);

    const roles = user?.["https://vladtech.com/roles"] || [];
    const isClient = isAuthenticated && roles.includes("Client");
    const isStaff = isAuthenticated && roles.some(r => r === "Admin" || r === "Employee");

    const fetchReviews = useCallback(async () => {
        try {
            if (isStaff) {
                const token = await getAccessTokenSilently({
                    authorizationParams: { audience: "https://vladtech/api" },
                });
                setReviews(await getAllReviews(token));
                return;
            }

            if (isClient && showMine) {
                const token = await getAccessTokenSilently({
                    authorizationParams: { audience: "https://vladtech/api" },
                });
                setReviews(await getMyReviews(token));
                return;
            }

            setReviews(await getAllVisibleReviews());
        } catch (err) {
            console.error("Failed to fetch reviews:", err);
        }
    }, [isClient, isStaff, showMine, getAccessTokenSilently]);

    useEffect(() => {
        if (!isLoading) fetchReviews();
    }, [isLoading, fetchReviews]);

    return (
        <div className="reviews-page" data-testid="reviews-page">
            <nav className="fixed top-0 left-0 right-0 z-50 bg-black/60 backdrop-blur-xl border-b border-yellow-400/20">
                <div className="container mx-auto px-8 py-6 flex justify-between">
                    <button
                        onClick={() => navigate("/")}
                        className="text-2xl text-white hover:text-yellow-400"
                    >
                        VLADTECH
                    </button>

                    <div className="flex gap-12">
                        <button
                            onClick={() => navigate("/portfolio")}
                            className="text-white/40 hover:text-yellow-400"
                        >
                            PORTFOLIO
                        </button>
                        <button
                            onClick={() => navigate("/reviews")}
                            className="text-white border-b-2 border-yellow-400"
                        >
                            REVIEWS
                        </button>
                    </div>
                </div>
            </nav>

            <div className="container mx-auto p-4" style={{ marginTop: "120px" }}>
                {isClient && (
                    <div className="flex items-center justify-between mb-6">
                        <button
                            onClick={() => setShowModal(true)}
                            className="px-4 py-2 bg-yellow-400 text-black rounded font-semibold"
                        >
                            Add Review
                        </button>

                        <div className="flex items-center gap-3">
                            <button
                                type="button"
                                role="switch"
                                aria-checked={showMine}
                                onClick={() => setShowMine(prev => !prev)}
                                className={`
                                    relative inline-flex h-7 w-14 items-center rounded-full
                                    transition-colors duration-300
                                    ${showMine ? "bg-yellow-400" : "bg-black"}
                                `}
                            >
                                <span
                                    className={`
                                        inline-block h-5 w-5 transform rounded-full bg-white
                                        transition-transform duration-300
                                        ${showMine ? "translate-x-7" : "translate-x-1"}
                                    `}
                                />
                            </button>

                            <span className="text-sm font-semibold tracking-wide text-black select-none">
                            Show only my reviews
                            </span>
                        </div>
                    </div>
                )}

                <section>
                    <h2 className="title text-4xl font-extrabold tracking-wide text-black mb-6">Customer Highlights</h2>

                    <ReviewCarousel
                        reviews={reviews}
                        onReviewClick={(review) => {
                            setSelectedReview(review);
                            setShowDetailModal(true);
                        }}
                        onDelete={(deletedId) => {
                            setReviews(prev =>
                                prev.filter(r => (r.id ?? r.reviewId) !== deletedId)
                            );
                        }}
                    />
                </section>
            </div>

            <ReviewModal
                open={showModal}
                onClose={() => setShowModal(false)}
                onSubmitSuccess={async () => {
                    setShowModal(false);
                    await fetchReviews();
                }}
            />

            <ReviewDetailModal
                open={showDetailModal}
                review={selectedReview}
                onClose={() => {
                    setShowDetailModal(false);
                    setSelectedReview(null);
                }}
            />
        </div>
    );
};

export default ReviewsPage;
