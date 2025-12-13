import React, {useEffect, useState} from "react";
import { FaStar, FaRegStar, FaTimes } from "react-icons/fa";
import "./Review.css";

const ReviewDetailModal = ({ review, open, onClose }) => {

    if (!open || !review) return null;

    const { clientId, clientName, comment, rating, photos } = review;
    const photo = photos?.[0];
    const [imgSrc, setImgSrc] = useState(
        photo?.filename ? `http://localhost:8080/images/${photo.filename}` : "/images/placeholder.png"
    );
    const [errored, setErrored] = useState(false);

    const handleError = () => {
        if (!errored) {
            setImgSrc("/images/placeholder.png");
            setErrored(true);
        }
    };


    const ratingMap = {
        ONE: 1,
        TWO: 2,
        THREE: 3,
        FOUR: 4,
        FIVE: 5
    };

    const numericRating = ratingMap[rating] || 0;

    const stars = Array.from({ length: 5 }, (_, i) =>
        i < numericRating ? <FaStar key={i} className="star-icon" /> : <FaRegStar key={i} className="star-icon" />
    );

    const handleBackdropClick = (e) => {
        if (e.target === e.currentTarget) {
            onClose();
        }
    };

    return (
        <div
            className="review-modal-backdrop"
            onClick={handleBackdropClick}
            data-testid="review-detail-modal-backdrop"
        >
            <div className="review-modal-content enlarged" data-testid="review-detail-modal">
                <button
                    className="review-modal-close"
                    onClick={onClose}
                    aria-label="Close modal"
                    data-testid="review-detail-close-button"
                >
                    <FaTimes />
                </button>

                <div className="review-card enlarged">
                    <img
                        src={imgSrc}
                        alt={photo?.filename}
                        onError={handleError}
                        data-testid="review-detail-image"
                    />

                    <p className="client-name" data-testid="review-detail-client">
                        {clientName}
                    </p>

                    <div className="stars enlarged" data-testid="review-detail-stars">
                        {stars}
                    </div>

                    <p className="comment enlarged" data-testid="review-detail-comment">
                        {comment}
                    </p>
                </div>
            </div>
        </div>
    );
};

export default ReviewDetailModal;