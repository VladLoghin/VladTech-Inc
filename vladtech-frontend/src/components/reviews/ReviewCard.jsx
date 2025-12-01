import React, { useEffect } from "react";
import { FaStar, FaRegStar } from "react-icons/fa";
import "./review.css";

const ReviewCard = ({ review }) => {
    const { clientId, comment, rating, photos } = review;
    const photo = photos?.[0]; // take the first photo if available

    // Debug logs
    useEffect(() => {
        console.log("Review data received:", review);
        if (photo?.filename) {
            console.log("Photo filename:", photo.filename);
        } else {
            console.warn("No photo provided for this review.");
        }
    }, [review, photo]);

    // Map enum rating to number
    const ratingMap = {
        ONE: 1,
        TWO: 2,
        THREE: 3,
        FOUR: 4,
        FIVE: 5
    };
    const numericRating = ratingMap[rating] || 0;

    // Generate stars
    const stars = Array.from({ length: 5 }, (_, i) =>
        i < numericRating ? (
            <FaStar key={i} style={{ color: "gold", stroke: "black", strokeWidth: 10 }} />
        ) : (
            <FaRegStar key={i} style={{ color: "gold", stroke: "black", strokeWidth: 10 }} />
        )
    );

    // Determine image src (static file)
    const imgSrc = photo?.filename
        ? `http://localhost:8080/images/${photo.filename}`
        : null;

    if (!imgSrc) {
        console.warn("No image source could be determined for review:", review);
    }

    return (
        <div className="review-card p-4 shadow-lg rounded-lg flex flex-col items-center text-center max-w-xs mx-auto hover:shadow-xl transition-shadow duration-300">
            {imgSrc ? (
                <img
                    src={imgSrc}
                    alt={photo?.filename || clientId}
                    className="w-24 h-24 object-cover mb-3"
                    onError={(e) => {
                        console.error("Failed to load image:", e.target.src);
                        e.target.src = "/images/placeholder.png"; // fallback image
                    }}
                />
            ) : (
                <p>No image available</p>
            )}
            <p className="client-name">{clientId}</p>
            <div className="stars">{stars}</div>
            <p className="comment">"{comment}"</p>
        </div>
    );
};

export default ReviewCard;
