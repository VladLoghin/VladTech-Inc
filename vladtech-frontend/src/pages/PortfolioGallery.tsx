import { useState, useEffect } from "react";
import { X, Send, ChevronLeft, ChevronRight, ChevronDown } from "lucide-react";
import { Button } from "../components/button.js";
import { Textarea } from "../components/textarea.js";
import { motion, AnimatePresence } from "motion/react";
import { useAuth0 } from "@auth0/auth0-react";
import { useTranslation } from "react-i18next";
import { addComment } from "../api/portfolio/portfolioService.js";
import getImageUrl from "../utils/getImageUrl.js";
import {api} from "../api/http.js";
import Navbar from "../components/Navbar";

interface PortfolioItem {
  portfolioId: string;
  reviewId?: string;
  title: string;
  imageUrl: string;
  imageUrls?: string[];
  type: string;
  reviewerName?: string; // Optional: name of reviewer if sent from review
  comments: PortfolioComment[];
}

interface PortfolioComment {
  authorName: string;
  authorUserId: string;
  timestamp: string;
  text: string;
}

const PORTFOLIO_TYPES = ["All", "Interior", "Kitchen", "Bathroom", "Exterior/Yard"];

export default function PortfolioGallery() {
  const { isAuthenticated, user, getAccessTokenSilently, loginWithRedirect } = useAuth0();
  const { t } = useTranslation();
  const [portfolioItems, setPortfolioItems] = useState<PortfolioItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedItem, setSelectedItem] = useState<PortfolioItem | null>(null);
  const [newComment, setNewComment] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 6;
  const [selectedType, setSelectedType] = useState<string>("All");
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  // Helper function to calculate time ago
  const getTimeAgo = (timestamp: string): string => {
    const now = new Date();
    const commentTime = new Date(timestamp);
    const diffInMs = now.getTime() - commentTime.getTime();
    const diffInMinutes = Math.floor(diffInMs / 60000);
    const diffInHours = Math.floor(diffInMinutes / 60);
    const diffInDays = Math.floor(diffInHours / 24);

    if (diffInMinutes < 1) return "Just now";
    if (diffInMinutes < 60) return `${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''} ago`;
    if (diffInHours < 24) return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
    return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;
  };

  // Helper function to get author initial
  const getAuthorInitial = (authorName: string): string => {
    return authorName.charAt(0).toUpperCase();
  };

  // Fetch portfolio items from backend
  useEffect(() => {
  const fetchPortfolioItems = async () => {
    try {
      const type = selectedType === "All" ? undefined : selectedType;
      const response = await api.get("/portfolio", { params: type ? { type } : {} });
      console.log("Selected type:", selectedType);
      console.log("Portfolio data received:", response.data);
      console.log("Number of items:", response.data.length);
      setPortfolioItems(response.data);
      setCurrentPage(1); // Reset to page 1 when filter changes
    } catch (error) {
      console.error("Error fetching portfolio:", error);
    } finally {
      setLoading(false);
    }
  };

  fetchPortfolioItems();
}, [selectedType]);

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedItem || !newComment.trim()) return;

    // Check if user is authenticated
    if (!isAuthenticated) {
      loginWithRedirect({
        appState: { returnTo: window.location.pathname }
      });
      return;
    }

    setIsSubmitting(true);

    try {
      // Get access token
      const accessToken = await getAccessTokenSilently({
                authorizationParams: {
                    audience: "https://vladtech/api",
                },
            });

      // Get user's nickname or name
      const authorName = user?.nickname || user?.name || user?.email || "Anonymous User";

      // Call API to add comment
      const newCommentDto = await addComment(
        selectedItem.portfolioId,
        newComment.trim(),
        authorName,
        accessToken
      );

      // Update the selected item with the new comment
      const updatedItem = {
        ...selectedItem,
        comments: [...selectedItem.comments, newCommentDto],
      };

      setSelectedItem(updatedItem);

      // Update the portfolio items list
      setPortfolioItems((items) =>
        items.map((item) =>
          item.portfolioId === selectedItem.portfolioId ? updatedItem : item
        )
      );

      setNewComment("");
    } catch (error: any) {
      console.error("Error adding comment:", error);
      
      // Handle specific error cases
      if (error.response?.status === 401) {
        alert("Your session has expired. Please log in again.");
        loginWithRedirect({
          appState: { returnTo: window.location.pathname }
        });
      } else if (error.response?.status === 403) {
        alert("You don't have permission to comment. Only clients and admins can comment on portfolio items.");
      } else {
        alert("Failed to add comment. Please try again.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  // Calculate pagination
  const totalPages = Math.ceil(portfolioItems.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentItems = portfolioItems.slice(startIndex, endIndex);

  const handleNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-yellow-400 text-2xl tracking-wider">{t("loading")}</div>
      </div>
    );
  }

  return (
    <>
      <Navbar
        isNavbarDark={true}
        rightSlot={
          <div className="relative">
            <button
              onClick={() => setIsDropdownOpen(!isDropdownOpen)}
              className="flex items-center gap-2 px-4 py-2 bg-yellow-400 text-black rounded-full text-sm font-medium hover:bg-yellow-500 transition-all"
            >
              {selectedType === "All" ? t("portfolio.filterAll") : selectedType === "Interior" ? t("reviews.interior") : selectedType === "Kitchen" ? t("reviews.kitchen") : selectedType === "Bathroom" ? t("reviews.bathroom") : t("reviews.exteriorYard")}
              <ChevronDown className={`h-4 w-4 transition-transform ${isDropdownOpen ? 'rotate-180' : ''}`} role="presentation" focusable="false" />
            </button>

            <AnimatePresence>
              {isDropdownOpen && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  transition={{ duration: 0.2 }}
                  className="absolute right-0 mt-2 w-56 bg-black border border-yellow-400/30 rounded-lg shadow-2xl overflow-hidden z-50"
                >
                  {PORTFOLIO_TYPES.map((type) => (
                    <button
                      key={type}
                      onClick={() => {
                        setSelectedType(type);
                        setIsDropdownOpen(false);
                      }}
                      className={`w-full text-left px-4 py-3 text-sm transition-all ${
                        selectedType === type
                          ? "bg-yellow-400 text-black font-semibold"
                          : "text-white hover:bg-yellow-400/10"
                      }`}
                    >
                      {type === "All" ? t("portfolio.filterAll") : type === "Interior" ? t("reviews.interior") : type === "Kitchen" ? t("reviews.kitchen") : type === "Bathroom" ? t("reviews.bathroom") : t("reviews.exteriorYard")}
                    </button>
                  ))}
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        }
      />

      {/* Portfolio Grid - No gaps, starts right after navbar */}
      <main className="min-h-screen bg-black pt-[88px] flex flex-col">
        <h1 className="sr-only">{t("nav.portfolio")}</h1>
        <div className="flex-1">
          <div className="grid grid-cols-3 gap-0">
            {currentItems.map((item, index) => (
              <motion.button
                key={item.portfolioId}
                type="button"
                initial={false}
                animate={{ opacity: 1 }}
                whileHover={{ scale: 1.05, zIndex: 10, transition: { duration: 0.2 } }}
                onClick={() => { setCurrentImageIndex(0); setSelectedItem(item); }}
                aria-label={`View ${item.title}`}
                className="overflow-hidden aspect-square relative group cursor-pointer border-0 p-0 bg-transparent text-left focus-visible:ring-2 focus-visible:ring-yellow-400 focus-visible:ring-inset focus-visible:z-10 scroll-mt-[100px]"
              >
                <img
                  src={getImageUrl(item.imageUrl)}
                  alt={item.title}
                  className="w-full h-full object-cover"
                />
                {/* Reviewer Name - Always visible in top-left corner */}
                {item.reviewerName && (
                  <div className="absolute top-4 left-4 bg-black/70 backdrop-blur-sm px-3 py-1.5 rounded-full">
                    <p className="text-white text-sm font-medium tracking-wide">{item.reviewerName}</p>
                  </div>
                )}
                <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" aria-hidden="true">
                  <div className="absolute bottom-4 left-4 right-4">
                    <h3 className="text-white text-lg tracking-wide mb-2">{item.title}</h3>
                  </div>
                </div>
              </motion.button>
            ))}
          </div>
        </div>

        {/* Pagination Controls */}
        {totalPages > 1 && (
          <div className="py-8 flex items-center justify-center gap-4 border-t border-yellow-400/20">
            <button
              onClick={handlePrevPage}
              disabled={currentPage === 1}
              aria-label="Previous page"
              className="p-2 rounded-lg bg-yellow-400/10 hover:bg-yellow-400/20 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronLeft className="h-6 w-6 text-yellow-400" role="presentation" focusable="false" />
            </button>
            
            <div className="flex items-center gap-2">
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                <button
                  key={page}
                  onClick={() => {
                    setCurrentPage(page);
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                  }}
                  className={`w-10 h-10 rounded-lg transition-colors ${
                    currentPage === page
                      ? 'bg-yellow-400 text-black font-bold'
                      : 'bg-yellow-400/10 text-yellow-400 hover:bg-yellow-400/20'
                  }`}
                >
                  {page}
                </button>
              ))}
            </div>

            <button
              onClick={handleNextPage}
              disabled={currentPage === totalPages}
              aria-label="Next page"
              className="p-2 rounded-lg bg-yellow-400/10 hover:bg-yellow-400/20 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronRight className="h-6 w-6 text-yellow-400" role="presentation" focusable="false" />
            </button>
          </div>
        )}
      </main>

      {/* Instagram-like Comment Modal */}
      <AnimatePresence>
        {selectedItem && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/95 backdrop-blur-sm z-50 flex items-center justify-center p-4"
            onClick={() => setSelectedItem(null)}
          >
            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              transition={{ type: "spring", damping: 25, stiffness: 300 }}
              onClick={(e) => e.stopPropagation()}
              className="bg-black border border-yellow-400/30 rounded-3xl overflow-hidden max-w-5xl w-full max-h-[90vh] grid md:grid-cols-2 shadow-2xl"
            >
              {/* Image Side with Carousel */}
              <div className="relative bg-black">
                {(() => {
                  const images = selectedItem.imageUrls && selectedItem.imageUrls.length > 0
                    ? selectedItem.imageUrls
                    : [selectedItem.imageUrl];
                  return (
                    <>
                      <img
                        src={getImageUrl(images[currentImageIndex] ?? images[0] ?? selectedItem.imageUrl)}
                        alt={selectedItem.title}
                        className="w-full h-full object-cover"
                      />
                      {images.length > 1 && (
                        <>
                          <button
                            onClick={() => setCurrentImageIndex((prev) => (prev - 1 + images.length) % images.length)}
                            aria-label="Previous image"
                            className="absolute left-2 top-1/2 -translate-y-1/2 bg-black/60 hover:bg-black/80 text-white rounded-full p-2 transition-colors"
                          >
                            <ChevronLeft className="h-5 w-5" aria-hidden="true" />
                          </button>
                          <button
                            onClick={() => setCurrentImageIndex((prev) => (prev + 1) % images.length)}
                            aria-label="Next image"
                            className="absolute right-2 top-1/2 -translate-y-1/2 bg-black/60 hover:bg-black/80 text-white rounded-full p-2 transition-colors"
                          >
                            <ChevronRight className="h-5 w-5" aria-hidden="true" />
                          </button>
                          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
                            {images.map((_, idx) => (
                              <button
                                key={idx}
                                onClick={() => setCurrentImageIndex(idx)}
                                aria-label={`Go to image ${idx + 1}`}
                                aria-current={idx === currentImageIndex ? "true" : undefined}
                                className={`w-2.5 h-2.5 rounded-full transition-colors ${
                                  idx === currentImageIndex ? 'bg-yellow-400' : 'bg-white/50 hover:bg-white/80'
                                }`}
                              />
                            ))}
                          </div>
                        </>
                      )}
                    </>
                  );
                })()}
              </div>

              {/* Comments Side */}
              <div className="flex flex-col max-h-[90vh] bg-black">
                {/* Header */}
                <div className="p-6 border-b border-yellow-400/20 flex items-center justify-between">
                  <div>
                    <h3 className="text-xl text-white tracking-wide">{selectedItem.title}</h3>
                  </div>
                  <button
                    onClick={() => setSelectedItem(null)}
                    aria-label="Close modal"
                    className="text-gray-400 hover:text-white transition-colors"
                  >
                    <X className="h-6 w-6" aria-hidden="true" />
                  </button>
                </div>

                {/* Comments List */}
                <div className="flex-1 overflow-y-auto p-6 space-y-6">
                  {selectedItem.comments.length === 0 ? (
                    <p className="text-center text-gray-500 py-8">{t("portfolio.noComments")}</p>
                  ) : (
                    selectedItem.comments.map((comment, idx) => (
                      <div key={idx} className="flex gap-3">
                        <div className="w-10 h-10 bg-yellow-400 rounded-full flex items-center justify-center flex-shrink-0">
                          <span className="text-black font-bold text-sm">{getAuthorInitial(comment.authorName)}</span>
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="text-white tracking-wide">{comment.authorName}</span>
                            <span className="text-gray-500 text-sm">{getTimeAgo(comment.timestamp)}</span>
                          </div>
                          <p className="text-gray-300">{comment.text}</p>
                        </div>
                      </div>
                    ))
                  )}
                </div>

                {/* Comment Input */}
                {isAuthenticated ? (
                  <form onSubmit={handleAddComment} className="p-6 border-t border-yellow-400/20">
                    <div className="flex gap-3">
                      <Textarea
                        value={newComment}
                      onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setNewComment(e.target.value)}
                        placeholder={t("portfolio.addComment")}
                        className="flex-1 resize-none bg-black/50 border-yellow-400/20 focus:border-yellow-400 text-white placeholder:text-gray-500 rounded-xl"
                        rows={2}
                        disabled={isSubmitting}
                      />
                      <Button
                        type="submit"
                        disabled={!newComment.trim() || isSubmitting}
                        className="bg-yellow-400 hover:bg-yellow-500 text-black h-auto px-6 disabled:opacity-50"
                      >
                        <Send className="h-5 w-5" aria-hidden="true" />
                      </Button>
                    </div>
                  </form>
                ) : (
                  <div className="p-6 border-t border-yellow-400/20">
                    <div className="text-center">
                      <p className="text-gray-400 mb-3">{t("portfolio.signInToComment")}</p>
                      <Button
                        onClick={() => loginWithRedirect({
                          appState: { returnTo: window.location.pathname }
                        })}
                        className="bg-yellow-400 hover:bg-yellow-500 text-black px-6"
                      >
                        {t("portfolio.signIn")}
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
