import React, { useState } from "react";
import { FaStar, FaRegStar } from "react-icons/fa";
import { useAuth0 } from "@auth0/auth0-react";
import { deleteReview } from "../../api/reviews/reviewsService.js";
import "./Review.css";

const ReviewCard = ({ review, onClick, onDelete }) => {
    const { isAuthenticated, user, getAccessTokenSilently } = useAuth0();

    const { clientId, clientName, comment, rating, photos } = review;
    const reviewId = review.id ?? review.reviewId;
    const initialVisible = review.visible ?? false;
    const roles = user?.["https://vladtech.com/roles"] || [];
    const isClient = isAuthenticated && roles.includes("Client");

    const isOwner = isAuthenticated && user?.sub && review?.clientId === user.sub;

    const canDelete = isClient && isOwner;

    const [deleting, setDeleting] = useState(false);

    const canToggleVisibility =
        isAuthenticated &&
        Array.isArray(user?.["https://vladtech.com/roles"]) &&
        user["https://vladtech.com/roles"].some(
            (r) => r === "Admin" || r === "Employee"
        );


    const [isVisible, setIsVisible] = useState(initialVisible);
    const [saving, setSaving] = useState(false);

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

    const handleDelete = async (e) => {
        e.stopPropagation();
        if (!reviewId) return;

        try {
            setDeleting(true);

            const token = await getAccessTokenSilently({
                authorizationParams: {
                    audience: "https://vladtech/api",
                },
            });

            const res = await deleteReview(reviewId, token);

            if(!res.ok) {
                throw new Error(`Delete failed with status ${res.status}`);
            }

            onDelete?.(reviewId);
        } catch (err) {
            console.error("Failed to delete review:", err);
        } finally {
            setDeleting(false);
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
                    onClick={(e) => e.stopPropagation()}
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
                alt={photo?.filename}
                onError={handleError}
                data-testid="review-image"
            />

            <p className="client-name" data-testid="review-client">
                {clientName}
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

            {canDelete && (
    <button
        type="button"
        onClick={handleDelete}
        disabled={deleting}
        style={{
            backgroundColor: "#dc2626",
            color: "white",
            padding: "8px 10px",
            borderRadius: "8px",
            marginTop: "10px",
            width: "100%",
            fontWeight: 700,
            cursor: deleting ? "not-allowed" : "pointer",
            opacity: deleting ? 0.7 : 1,
        }}
        data-testid="review-delete-button"
    >
        {deleting ? "Deleting..." : "Delete"}
    </button>
)}

        </div>
    );
};

export default ReviewCard;
