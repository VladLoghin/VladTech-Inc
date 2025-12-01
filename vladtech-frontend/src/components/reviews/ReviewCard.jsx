import React from "react";
import { FaStar, FaRegStar } from "react-icons/fa";
import "./review.css";

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
        <div className="review-card">
            {imgSrc ? (
                <img
                    src={imgSrc}
                    alt={photo?.filename || clientId}
                    onError={(e) => {
                        e.target.src = "/images/placeholder.png"; // fallback image
                    }}
                />
            ) : null}

            <p className="client-name">{clientId}</p>

            <div className="stars">{stars}</div>

            <p className="comment">"{comment}"</p>
        </div>
    );
};

export default ReviewCard;
