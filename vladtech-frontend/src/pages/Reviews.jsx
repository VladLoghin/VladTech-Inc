import React, { useEffect, useState } from "react";
import ReviewCarousel from "../components/reviews/ReviewCarousel";
import SecondaryNavbar from "../components/SecondaryNavbar";
import { getAllVisibleReviews } from "../api/reviews/reviewsService";
import "../components/reviews/Review.css";
import ReviewModal from "../components/reviews/ReviewModal";
import { useAuth0 } from "@auth0/auth0-react";

const ReviewsPage = () => {
    const [reviews, setReviews] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [roles, setRoles] = useState([]);

    const { user, isAuthenticated } = useAuth0();

    useEffect(() => {
        // Fetch roles safely
        if (user) {
            const userRoles = user["https://vladtech.com/roles"] || [];
            setRoles(userRoles);
        }

        // Fetch reviews
        fetchReviews();
    }, [user]);

    const fetchReviews = () => {
        getAllVisibleReviews()
            .then((data) => setReviews(data))
            .catch((err) => console.error("Failed to fetch reviews:", err));
    };

    const handleOpenModal = () => setShowModal(true);
    const handleCloseModal = () => setShowModal(false);

    const isClient = roles.includes("Client");

    return (
        <div className="reviews-page" data-testid="reviews-page">
            <SecondaryNavbar />

            <div className="container mx-auto p-4">
                {isClient && (
                    <button
                        type="button"
                        className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 mb-4"
                        onClick={handleOpenModal}
                        data-testid="add-review-button"
                    >
                        Add Review
                    </button>
                )}

                <section className="mb-10" data-testid="reviews-carousel-section">
                    <h2 className="title">Customer Highlights</h2>
                    <ReviewCarousel reviews={reviews} />
                </section>
            </div>

            {/* Pass the open prop and onSubmitSuccess to refresh reviews */}
            <ReviewModal
                open={showModal}
                onClose={handleCloseModal}
                onSubmitSuccess={() => {
                    fetchReviews(); // Refresh the carousel
                    handleCloseModal(); // Close modal after successful submission
                }}
            />
        </div>
    );
};

export default ReviewsPage;
