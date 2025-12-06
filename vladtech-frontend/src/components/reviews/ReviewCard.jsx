import React from "react";
import { FaStar, FaRegStar } from "react-icons/fa";
import "./Review.css";

const ReviewCard = ({ review }) => {
    const { clientId, comment, rating, photos } = review;

    // Safely pick first photo
    const photo = photos?.[0];
    const imgSrc = photo?.filename
        ? `http://localhost:8080/images/${photo.filename}`
        : null;

    // Map enum rating to number once
    const ratingMap = {
        ONE: 1,
        TWO: 2,
        THREE: 3,
        FOUR: 4,
        FIVE: 5
    };

    const numericRating = ratingMap[rating] || 0;

    // Generate star JSX safely
    const stars = Array.from({ length: 5 }, (_, i) =>
        i < numericRating ? (
            <FaStar key={i} className="star-icon" />
        ) : (
            <FaRegStar key={i} className="star-icon" />
        )
    );

    return (
        <div className="review-card" data-testid="review-card">
            {imgSrc && (
                <img
                    src={imgSrc}
                    alt={photo?.filename || clientId}
                    onError={(e) => { e.target.src = "/images/placeholder.png"; }}
                    data-testid="review-image"
                />
            )}

            <p className="client-name" data-testid="review-client">{clientId}</p>

            <div className="stars" data-testid="review-stars">
                {stars.map((star, index) =>
                    star.type === FaStar ? (
                        <span key={index} data-testid="review-star-filled">{star}</span>
                    ) : (
                        <span key={index} data-testid="review-star-empty">{star}</span>
                    )
                )}
            </div>

            <p className="comment" data-testid="review-comment">"{comment}"</p>
        </div>
    );
};

export default ReviewCard;
