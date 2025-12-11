import React, { useState } from "react";
import { FaStar, FaRegStar } from "react-icons/fa";
import { useAuth0 } from "@auth0/auth0-react";
import "./Review.css";

const ReviewCard = ({ review, onClick }) => {
    const { clientId, comment, rating, photos } = review;
    const reviewId = review.id ?? review.reviewId;
    const initialVisible = review.visible ?? true;

    const { isAuthenticated, user, getAccessTokenSilently } = useAuth0();

    // Determine if user is Admin or Employee
    const canToggleVisibility =
        isAuthenticated &&
        Array.isArray(user?.["https://vladtech.com/roles"]) &&
        user["https://vladtech.com/roles"].some(
            (r) => r === "Admin" || r === "Employee"
        );


    const [isVisible, setIsVisible] = useState(initialVisible);
    const [saving, setSaving] = useState(false);

    // Image handling
    const photo = photos?.[0];
    const [imgSrc, setImgSrc] = useState(
        photo?.filename
            ? `http://localhost:8080/images/${photo.filename}`
            : "/images/placeholder.png"
    );
    const [errored, setErrored] = useState(false);

    const handleError = () => {
        if (!errored) {
            setImgSrc("/images/placeholder.png");
            setErrored(true);
        }
    };

    // Convert rating enum → number
    const ratingMap = {
        ONE: 1,
        TWO: 2,
        THREE: 3,
        FOUR: 4,
        FIVE: 5,
    };
    const numericRating = ratingMap[rating] || 0;

    const stars = Array.from({ length: 5 }, (_, i) =>
        i < numericRating ? (
            <FaStar key={i} className="star-icon" />
        ) : (
            <FaRegStar key={i} className="star-icon" />
        )
    );

    // Toggle review visibility for Admin/Employee
    const handleVisibilityToggle = async () => {
        if (!reviewId) {
            console.error("Missing reviewId; cannot update visibility.");
            return;
        }

        const nextValue = !isVisible;
        setIsVisible(nextValue);
        setSaving(true);

        try {
            const token = await getAccessTokenSilently();
            const res = await fetch(
                `http://localhost:8080/api/reviews/${reviewId}/visibility`,
                {
                    method: "PATCH",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({ visible: nextValue }),
                }
            );

            if (!res.ok) {
                throw new Error(`PATCH failed with status ${res.status}`);
            }
        } catch (err) {
            console.error("Failed to update visibility:", err);
            setIsVisible(!nextValue); // revert on error
        } finally {
            setSaving(false);
        }
    };

    return (
        <div
            className="review-card"
            data-testid="review-card"
            onClick={onClick}
            style={{ cursor: onClick ? "pointer" : "default" }}
        >
            {canToggleVisibility && (
                <label
                    style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "8px",
                        marginTop: "12px",
                    }}
                >
                    <input
                        type="checkbox"
                        checked={isVisible}
                        onChange={handleVisibilityToggle}
                        disabled={saving}
                        data-testid="review-visibility-toggle"
                    />
                    <span>{saving ? "Updating..." : "Visible"}</span>
                </label>
            )}
            {canToggleVisibility && !isVisible && (
                <div
                    style={{
                        marginBottom: "8px",
                        fontSize: "12px",
                        color: "#b45309",
                        fontWeight: 600,
                    }}
                >
                </div>
            )}

            <img
                src={imgSrc}
                alt={photo?.filename || clientId}
                onError={handleError}
                data-testid="review-image"
            />

            <p className="client-name" data-testid="review-client">
                {clientId}
            </p>

            <div className="stars" data-testid="review-stars">
                {stars.map((star, index) =>
                    star.type === FaStar ? (
                        <span key={index} data-testid="review-star-filled">
                            {star}
                        </span>
                    ) : (
                        <span key={index} data-testid="review-star-empty">
                            {star}
                        </span>
                    )
                )}
            </div>

            <p className="comment" data-testid="review-comment">
                {comment}
            </p>

            
        </div>
    );
};

export default ReviewCard;
