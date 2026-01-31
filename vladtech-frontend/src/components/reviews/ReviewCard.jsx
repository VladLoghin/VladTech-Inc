import React, { useState } from "react";
import { FaStar, FaRegStar } from "react-icons/fa";
import { useAuth0 } from "@auth0/auth0-react";
import { deleteReviewClient, deleteReviewAdmin } from "../../api/reviews/reviewsService.js";
import getImageUrl from "../../utils/getImageUrl.js";
import "./Review.css";
import { api } from "../../api/http";


const ReviewCard = ({ review, onClick, onDelete }) => {
    const { isAuthenticated, user, getAccessTokenSilently } = useAuth0();

    const { clientName, comment, rating, photos } = review;
    const reviewId = review.id ?? review.reviewId;
    const initialVisible = review.visible ?? false;
    const roles = user?.["https://vladtech.com/roles"] || [];
    const isClient = isAuthenticated && roles.includes("Client");
    const isAdmin = isAuthenticated && roles.includes("Admin");
    const isEmployee = isAuthenticated && roles.includes("Employee");

    const isOwner = isAuthenticated && user?.sub && review?.clientId === user.sub;

    // Admin can delete any review; clients can delete only their own
    const canDelete = isAdmin || (isClient && isOwner);

    const [deleting, setDeleting] = useState(false);

    const canToggleVisibility =
        isAuthenticated &&
        Array.isArray(user?.["https://vladtech.com/roles"]) &&
        user["https://vladtech.com/roles"].some(
            (r) => r === "Admin" || r === "Employee"
        );

    const canSendToPortfolio = isAuthenticated && (isAdmin || isEmployee);

    const [isVisible, setIsVisible] = useState(initialVisible);
    const [saving, setSaving] = useState(false);
    const [sending, setSending] = useState(false);
    const [portfolioError, setPortfolioError] = useState("");

    const photo = photos?.[0];
    const [imgSrc, setImgSrc] = useState(
        photo?.url
            ? getImageUrl(photo.url)
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

    console.log("ReviewCard rendered with review:", review);


    const handleVisibilityToggle = async () => {
        if (!reviewId) {
            console.error("Missing reviewId; cannot update visibility.");
            return;
        }

        const nextValue = !isVisible;
        setIsVisible(nextValue);
        setSaving(true);

        try {
            const token = await getAccessTokenSilently({
                authorizationParams: { audience: "https://vladtech/api" },
            });

            await api.patch(
                `/reviews/${reviewId}/visibility`,
                { visible: nextValue },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                }
            );
        } catch (err) {
            console.error("Failed to update visibility:", err);
            setIsVisible(!nextValue);
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

            const res = isAdmin
                ? await deleteReviewAdmin(reviewId, token)
                : await deleteReviewClient(reviewId, token);

            if (res.status !== 200 && res.status !== 204) {
                throw new Error(`Delete failed with status ${res.status}`);
            }

            // Call onDelete to notify parent component
            if (onDelete) {
                onDelete(reviewId);
            }
        } catch (err) {
            console.error("Failed to delete review:", err);
            alert("Failed to delete review. Please try again.");
        } finally {
            setDeleting(false);
        }
    };

    const handleSendToPortfolio = async (e) => {
        e.stopPropagation();
        if (!reviewId) return;

        setPortfolioError("");

        try {
            setSending(true);

            const token = await getAccessTokenSilently({
                authorizationParams: { audience: "https://vladtech/api" },
            });

            const res = await api.post(
                `/reviews/${reviewId}/send-to-portfolio`,
                {},
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                }
            );

            if (res.status !== 200 && res.status !== 204) {
                throw new Error(`Send failed with status ${res.status}`);
            }

            setPortfolioError(""); // Clear any previous errors on success
        } catch (err) {
            console.error("Failed to send review to portfolio:", err);
            
            // Check if error response has message from backend
            const errorMessage = err.response?.data?.message || err.response?.data || "Failed to send review to portfolio. Please try again.";
            setPortfolioError(errorMessage);
        } finally {
            setSending(false);
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

            {canSendToPortfolio && (
                <>
                    <button
                        type="button"
                        onClick={handleSendToPortfolio}
                        disabled={sending}
                        style={{
                            backgroundColor: "#2563eb",
                            color: "white",
                            padding: "8px 10px",
                            borderRadius: "8px",
                            marginTop: "10px",
                            width: "100%",
                            fontWeight: 700,
                            cursor: sending ? "not-allowed" : "pointer",
                            opacity: sending ? 0.7 : 1,
                        }}
                        data-testid="review-send-portfolio-button"
                    >
                        {sending ? "Sending..." : "Send to Portfolio"}
                    </button>
                    
                    {portfolioError && (
                        <div
                            style={{
                                marginTop: "8px",
                                padding: "8px",
                                backgroundColor: "#fee2e2",
                                color: "#dc2626",
                                borderRadius: "4px",
                                fontSize: "12px",
                                fontWeight: 600,
                            }}
                            data-testid="review-portfolio-error"
                        >
                            {portfolioError}
                        </div>
                    )}
                </>
            )}

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
