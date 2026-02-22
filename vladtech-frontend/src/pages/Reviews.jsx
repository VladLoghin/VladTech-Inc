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
import { useTranslation } from "react-i18next";
import Navbar from "../components/Navbar";
import "../components/reviews/Review.css";
import { api } from "../api/http"; // ✅ ADD

const ReviewsPage = () => {
  const { user, isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
  const { t } = useTranslation();

  const [reviews, setReviews] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedReview, setSelectedReview] = useState(null);
  const [showMine, setShowMine] = useState(false);
  
  // Filter state
  const [filters, setFilters] = useState({
    clientName: "",
    minRating: "",
    maxRating: "",
    type: "",
    comment: "",
  });

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
        
        if (filters.minRating || filters.maxRating) {
          const ratingOrder = ["ONE", "TWO", "THREE", "FOUR", "FIVE"];
          const minIdx = filters.minRating ? ratingOrder.indexOf(filters.minRating) : 0;
          const maxIdx = filters.maxRating ? ratingOrder.indexOf(filters.maxRating) : ratingOrder.length - 1;
          const effectiveMin = Math.min(minIdx, maxIdx);
          const effectiveMax = Math.max(minIdx, maxIdx);
          filtered = filtered.filter(r => {
            const idx = ratingOrder.indexOf(r.rating);
            return idx >= effectiveMin && idx <= effectiveMax;
          });
        }
        
        if (filters.type) {
          filtered = filtered.filter(r => r.type === filters.type);
        }
        
        if (filters.comment) {
          filtered = filtered.filter(r => 
            r.comment?.toLowerCase().includes(filters.comment.toLowerCase())
          );
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
    setFilters({ clientName: "", minRating: "", maxRating: "", type: "", comment: "" });
  };

  const [hasUnreviewedProjects, setHasUnreviewedProjects] = useState(false);
  const [loadingProjects, setLoadingProjects] = useState(false);

  const checkUnreviewedProjects = useCallback(async () => {
    if (!isClient || isLoading) return;
    setLoadingProjects(true);
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });

      const [projectsRes, myReviews] = await Promise.all([
        api.get("/projects/client/completed", {
          headers: { Authorization: `Bearer ${token}` },
        }),
        getMyReviews(token),
      ]);

      const completedProjects = Array.isArray(projectsRes.data) ? projectsRes.data : [];
      const reviewedProjectIds = new Set(
        myReviews.map((r) => r.projectId).filter(Boolean)
      );

      const hasUnreviewed = completedProjects.some(
        (p) => !reviewedProjectIds.has(p.projectIdentifier)
      );

      setHasUnreviewedProjects(hasUnreviewed);
    } catch (err) {
      console.error("Failed to check unreviewed projects:", err);
      setHasUnreviewedProjects(false);
    } finally {
      setLoadingProjects(false);
    }
  }, [isClient, isLoading, getAccessTokenSilently]);

  useEffect(() => {
    checkUnreviewedProjects();
  }, [checkUnreviewedProjects]);

  return (
    <>
      <Navbar isNavbarDark={true} />

      <main className="reviews-page" data-testid="reviews-page" style={{ marginTop: "120px" }}>
        <h1 className="sr-only">{t("nav.reviews")}</h1>
        <div className="container mx-auto p-4">
          <h2 className="title text-4xl font-extrabold tracking-wide text-black mb-4">
            {t("reviews.customerHighlights")}
          </h2>

          {/* Filter Controls */}
          <div className="bg-gradient-to-r from-black via-gray-900 to-black text-white border border-yellow-300/70 shadow-lg shadow-yellow-100 rounded-xl p-6 mb-8">
            <div className="flex flex-wrap items-center justify-between gap-4 w-full">
              {/* Left: Search and filters */}
              <div className="flex flex-wrap items-center gap-4">
                <input
                  type="text"
                  placeholder={t("reviews.searchByName")}
                  value={filters.clientName}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, clientName: e.target.value }))
                  }
                  className="px-5 py-3 rounded-full border border-gray-300 text-sm text-gray-900 placeholder-gray-500 bg-white focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-300 shadow-sm scroll-mt-[100px]"
                />

                <input
                  type="text"
                  placeholder={t("reviews.searchByComment")}
                  value={filters.comment}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, comment: e.target.value }))
                  }
                  className="px-5 py-3 rounded-full border border-gray-300 text-sm text-gray-900 placeholder-gray-500 bg-white focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-300 shadow-sm"
                />

                <div className="flex items-center gap-2">
                  <select
                    value={filters.minRating}
                    onChange={(e) =>
                      setFilters((prev) => ({ ...prev, minRating: e.target.value }))
                    }
                    className="px-4 py-3 rounded-full border border-gray-300 text-sm text-gray-900 bg-white focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-300 shadow-sm"
                  >
                    <option value="">{t("reviews.minRating")}</option>
                    <option value="ONE">★☆☆☆☆</option>
                    <option value="TWO">★★☆☆☆</option>
                    <option value="THREE">★★★☆☆</option>
                    <option value="FOUR">★★★★☆</option>
                    <option value="FIVE">★★★★★</option>
                  </select>
                  <span className="text-white text-sm font-semibold">{t("reviews.to")}</span>
                  <select
                    value={filters.maxRating}
                    onChange={(e) =>
                      setFilters((prev) => ({ ...prev, maxRating: e.target.value }))
                    }
                    className="px-4 py-3 rounded-full border border-gray-300 text-sm text-gray-900 bg-white focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-300 shadow-sm"
                  >
                    <option value="">{t("reviews.maxRating")}</option>
                    <option value="ONE">★☆☆☆☆</option>
                    <option value="TWO">★★☆☆☆</option>
                    <option value="THREE">★★★☆☆</option>
                    <option value="FOUR">★★★★☆</option>
                    <option value="FIVE">★★★★★</option>
                  </select>
                </div>

                <select
                  value={filters.type}
                  onChange={(e) =>
                    setFilters((prev) => ({ ...prev, type: e.target.value }))
                  }
                  className="px-5 py-3 rounded-full border border-gray-300 text-sm text-gray-900 bg-white focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-300 shadow-sm"
                >
                  <option value="">{t("reviews.allTypes")}</option>
                  <option value="Interior">{t("reviews.interior")}</option>
                  <option value="Kitchen">{t("reviews.kitchen")}</option>
                  <option value="Bathroom">{t("reviews.bathroom")}</option>
                  <option value="Exterior/Yard">{t("reviews.exteriorYard")}</option>
                </select>

                <button
                  onClick={handleReset}
                  className="px-5 py-3 rounded-full border border-gray-300 text-sm font-semibold text-gray-900 bg-white hover:bg-yellow-100 hover:border-yellow-400 shadow-sm transition-colors duration-300"
                >
                  {t("reviews.reset")}
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
                      {t("reviews.showOnlyMine")}
                    </span>
                  </label>

                  {/* Add Review Button */}
                  {hasUnreviewedProjects && !loadingProjects && (
                    <button
                      onClick={() => setShowModal(true)}
                      data-testid="Add Review"
                      className="px-5 py-3 rounded-full bg-yellow-400 text-black font-semibold hover:bg-yellow-500 shadow-sm transition-colors duration-300"
                    >
                      {t("reviews.addReview")}
                    </button>
                  )}
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
              onDelete={async (deletedId) => {
                setReviews((prev) =>
                  prev.filter((r) => (r.id ?? r.reviewId) !== deletedId)
                );
                await checkUnreviewedProjects();
              }}
            />
          </section>
        </div>

      </main>

      <ReviewModal
        open={showModal}
        onClose={() => setShowModal(false)}
        onSubmitSuccess={async () => {
          setShowModal(false);
          await fetchReviews();
          await checkUnreviewedProjects();
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
    </>
  );
};

export default ReviewsPage;