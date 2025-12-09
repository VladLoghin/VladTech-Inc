import React, { useState } from "react";
import { FaStar, FaRegStar } from "react-icons/fa";
import "./Review.css";

const ReviewCard = ({ review, onClick }) => {
    const { clientId, comment, rating, photos } = review;

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

    return (
        <div className="review-card"
             data-testid="review-card"
             onClick={onClick}
             style={{ cursor: onClick ? 'pointer' : 'default' }}
        >
            <img
                src={imgSrc}
                alt={photo?.filename || clientId}
                onError={handleError}
                data-testid="review-image"
            />

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

            <p className="comment" data-testid="review-comment">{comment}</p>
        </div>
    );
};

export default ReviewCard;
