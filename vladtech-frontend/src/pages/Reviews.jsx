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

  const { user, isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();

  const [reviews, setReviews] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedReview, setSelectedReview] = useState(null);
  const [showMine, setShowMine] = useState(false);
  
  // Filter state
  const [filters, setFilters] = useState({
    clientName: "",
    rating: "",
  });

  // ✅ Robust roles parsing + normalization
  const rawRoles = user?.["https://vladtech.com/roles"];
  const rolesArray = Array.isArray(rawRoles)
    ? rawRoles
    : typeof rawRoles === "string"
      ? [rawRoles]
      : [];

  const roles = rolesArray
    .filter(Boolean)
    .map((r) => String(r).trim())
    .map((r) => r.replace(/^ROLE_/, ""))
    .map((r) => r.toLowerCase());

  const isClient = isAuthenticated && roles.includes("client");
  const isStaff =
    isAuthenticated && (roles.includes("admin") || roles.includes("employee"));

  const fetchReviews = useCallback(async () => {
    try {
      if (isStaff) {
        const token = await getAccessTokenSilently({
          authorizationParams: { audience: "https://vladtech/api" },
        });
        setReviews(await getAllReviews(token, filters));
        return;
      }

      if (isClient && showMine) {
        const token = await getAccessTokenSilently({
          authorizationParams: { audience: "https://vladtech/api" },
        });
        const myReviews = await getMyReviews(token);
        
        // Apply client-side filtering for "my reviews"
        let filtered = myReviews;
        
        if (filters.clientName) {
          filtered = filtered.filter(r => 
            r.clientName?.toLowerCase().includes(filters.clientName.toLowerCase())
          );
        }
        
        if (filters.rating) {
          filtered = filtered.filter(r => r.rating === filters.rating);
        }
        
        setReviews(filtered);
        return;
      }

      setReviews(await getAllVisibleReviews(filters));
    } catch (err) {
      console.error("Failed to fetch reviews:", err);
    }
  }, [isClient, isStaff, showMine, filters, getAccessTokenSilently]);

  useEffect(() => {
    if (!isLoading) fetchReviews();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLoading, isClient, isStaff, showMine, filters]);

  const handleReset = () => {
    setFilters({ clientName: "", rating: "" });
  };

  return (
    <>
      <div className="reviews-page" data-testid="reviews-page">
        <nav className="fixed top-0 left-0 right-0 z-50 bg-black border-b border-yellow-400/20 backdrop-blur-xl">
          <div className="container mx-auto px-8 py-6 flex items-center">
            <button
              onClick={() => navigate("/")}
              className="text-2xl text-white hover:text-yellow-400"
            >
              VLADTECH
            </button>

            <div className="flex flex-1 justify-center gap-12">
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

            <div className="w-14" aria-hidden="true" />
          </div>
        </nav>

        <div className="container mx-auto p-4" style={{ marginTop: "120px" }}>
          <h2 className="title text-4xl font-extrabold tracking-wide text-black mb-4">
            Customer Highlights
          </h2>

          {/* Filter Controls */}
          <div className="bg-gradient-to-r from-black via-gray-900 to-black text-white border border-yellow-300/70 shadow-lg shadow-yellow-100 rounded-xl p-6 mb-8">
            <div className="flex flex-wrap items-center justify-between gap-4 w-full">
              {/* Left: Search and filters */}
              <div className="flex flex-wrap items-center gap-4">
                <input
                  type="text"
                  placeholder="Search by Name"
                  value={filters.clientName}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, clientName: e.target.value }))
                  }
                  className="px-5 py-3 rounded-full border border-gray-300 text-sm text-gray-900 placeholder-gray-500 bg-white focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-300 shadow-sm"
                />

                <select
                  value={filters.rating}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, rating: e.target.value }))
                  }
                  className="px-5 py-3 rounded-full border border-gray-300 text-sm text-gray-900 bg-white focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-300 shadow-sm"
                >
                  <option value="">All ratings</option>
                  <option value="FIVE">★★★★★</option>
                  <option value="FOUR">★★★★☆</option>
                  <option value="THREE">★★★☆☆</option>
                  <option value="TWO">★★☆☆☆</option>
                  <option value="ONE">★☆☆☆☆</option>
                </select>

                <button
                  onClick={handleReset}
                  className="px-5 py-3 rounded-full border border-gray-300 text-sm font-semibold text-gray-900 bg-white hover:bg-yellow-100 hover:border-yellow-400 shadow-sm transition-colors duration-300"
                >
                  Reset
                </button>
              </div>

              {/* Right: Toggle and Add Review button (client only) */}
              {isClient && (
                <div className="flex flex-wrap items-center gap-4">
                  {/* Toggle */}
                  <label className="inline-flex items-center gap-3 cursor-pointer select-none">
                    <input
                      type="checkbox"
                      className="sr-only peer"
                      checked={showMine}
                      onChange={(e) => setShowMine(e.target.checked)}
                      aria-label="Show only my reviews"
                    />

                    <div
                      className="
                        relative h-7 w-14 rounded-full
                        border-2 transition-colors duration-300
                      "
                      style={{
                        backgroundColor: showMine ? "#FBBF24" : "#D1D5DB",
                        borderColor: showMine ? "#F59E0B" : "#9CA3AF",
                      }}
                    >
                      <div
                        className="
                          absolute top-0.5 h-5 w-5 rounded-full bg-white
                          transition-all duration-300 ease-in-out
                        "
                        style={{
                          left: showMine ? "1.5rem" : "0.125rem",
                        }}
                      />
                    </div>

                    <span className="text-sm font-semibold text-white whitespace-nowrap">
                      Show only my reviews
                    </span>
                  </label>

                  {/* Add Review Button */}
                  <button
                    onClick={() => setShowModal(true)}
                    data-testid="Add Review"
                    className="px-5 py-3 rounded-full bg-yellow-400 text-black font-semibold hover:bg-yellow-500 shadow-sm transition-colors duration-300"
                  >
                    Add Review
                  </button>
                </div>
              )}
            </div>
          </div>

          <section>
            <ReviewCarousel
              reviews={reviews}
              onReviewClick={(review) => {
                setSelectedReview(review);
                setShowDetailModal(true);
              }}
              onDelete={(deletedId) => {
                setReviews((prev) =>
                  prev.filter((r) => (r.id ?? r.reviewId) !== deletedId)
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
    </>
  );
};

export default ReviewsPage;
