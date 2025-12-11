import { useState, useEffect } from "react";
import { X, Star, Send } from "lucide-react";
import { Button } from "../components/button";
import { Textarea } from "../components/textarea";
import { motion, AnimatePresence } from "motion/react";
import { useNavigate } from "react-router-dom";

interface PortfolioItem {
  portfolioId: string;
  title: string;
  imageUrl: string;
  rating: number;
  comments: PortfolioComment[];
}

interface PortfolioComment {
  authorName: string;
  authorInitial: string;
  timeAgo: string;
  text: string;
}

export default function PortfolioGallery() {
  const navigate = useNavigate();
  const [portfolioItems, setPortfolioItems] = useState<PortfolioItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedItem, setSelectedItem] = useState<PortfolioItem | null>(null);
  const [newComment, setNewComment] = useState("");

  // Fetch portfolio items from backend
  useEffect(() => {
    const fetchPortfolioItems = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/portfolio");
        if (response.ok) {
          const data = await response.json();
          console.log("Portfolio data:", data);
          setPortfolioItems(data);
        } else {
          console.error("Failed to fetch portfolio items");
        }
      } catch (error) {
        console.error("Error fetching portfolio:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchPortfolioItems();
  }, []);

  const handleAddComment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedItem || !newComment.trim()) return;

    const newCommentObj: PortfolioComment = {
      authorName: "You",
      authorInitial: "Y",
      timeAgo: "Just now",
      text: newComment,
    };

    setSelectedItem({
      ...selectedItem,
      comments: [...selectedItem.comments, newCommentObj],
    });

    setNewComment("");
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="text-yellow-400 text-2xl tracking-wider">LOADING...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-black">
      {/* Glossy Navigation Bar */}
      <nav className="fixed top-0 left-0 right-0 z-50 bg-black/60 backdrop-blur-xl border-b border-yellow-400/20 shadow-2xl">
        <div className="container mx-auto px-8 py-6 flex items-center justify-between">
          <button
            onClick={() => navigate("/")}
            className="text-2xl text-white tracking-widest hover:text-yellow-400 transition-colors"
          >
            VLADTECH
          </button>

          <div className="absolute left-1/2 transform -translate-x-1/2 flex gap-12">
            <button
              onClick={() => navigate("/portfolio")}
              className="text-white hover:text-yellow-400 transition-colors tracking-wider text-sm border-b-2 border-yellow-400"
            >
              PORTFOLIO
            </button>
            <button
              onClick={() => navigate("/reviews")}
              className="text-white/40 hover:text-yellow-400 transition-colors tracking-wider text-sm"
            >
              REVIEWS
            </button>
          </div>
        </div>
      </nav>

      {/* Portfolio Grid - No gaps, starts right after navbar */}
      <div className="pt-[88px] h-screen overflow-hidden">
        <div className="grid grid-cols-3 gap-0 h-[calc(100vh-88px)]">
          {portfolioItems.map((item, index) => (
            <motion.div
              key={item.portfolioId}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: index * 0.05 }}
              whileHover={{ scale: 1.05, zIndex: 10, transition: { duration: 0.2 } }}
              onClick={() => setSelectedItem(item)}
              className="cursor-pointer overflow-hidden aspect-square relative group"
            >
              <img
                src={`http://localhost:8080${item.imageUrl}`}
                alt={item.title}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                <div className="absolute bottom-4 left-4 right-4">
                  <h3 className="text-white text-lg tracking-wide mb-2">{item.title}</h3>
                  <div className="flex items-center gap-2">
                    <Star className="h-4 w-4 text-yellow-400 fill-yellow-400" />
                    <span className="text-yellow-400">{item.rating}</span>
                  </div>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      </div>

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
              {/* Image Side */}
              <div className="relative bg-black">
                <img
                  src={`http://localhost:8080${selectedItem.imageUrl}`}
                  alt={selectedItem.title}
                  className="w-full h-full object-cover"
                />
              </div>

              {/* Comments Side */}
              <div className="flex flex-col max-h-[90vh] bg-black">
                {/* Header */}
                <div className="p-6 border-b border-yellow-400/20 flex items-center justify-between">
                  <div>
                    <h3 className="text-xl text-white tracking-wide">{selectedItem.title}</h3>
                    <div className="flex items-center gap-2 mt-1">
                      <Star className="h-5 w-5 text-yellow-400 fill-yellow-400" />
                      <span className="text-yellow-400">{selectedItem.rating} / 5.0</span>
                    </div>
                  </div>
                  <button
                    onClick={() => setSelectedItem(null)}
                    className="text-gray-400 hover:text-white transition-colors"
                  >
                    <X className="h-6 w-6" />
                  </button>
                </div>

                {/* Comments List */}
                <div className="flex-1 overflow-y-auto p-6 space-y-6">
                  {selectedItem.comments.length === 0 ? (
                    <p className="text-center text-gray-500 py-8">No comments yet. Be the first to comment!</p>
                  ) : (
                    selectedItem.comments.map((comment, idx) => (
                      <div key={idx} className="flex gap-3">
                        <div className="w-10 h-10 bg-yellow-400 rounded-full flex items-center justify-center flex-shrink-0">
                          <span className="text-black font-bold text-sm">{comment.authorInitial}</span>
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="text-white tracking-wide">{comment.authorName}</span>
                            <span className="text-gray-500 text-sm">{comment.timeAgo}</span>
                          </div>
                          <p className="text-gray-300">{comment.text}</p>
                        </div>
                      </div>
                    ))
                  )}
                </div>

                {/* Comment Input */}
                <form onSubmit={handleAddComment} className="p-6 border-t border-yellow-400/20">
                  <div className="flex gap-3">
                    <Textarea
                      value={newComment}
                      onChange={(e) => setNewComment(e.target.value)}
                      placeholder="Add a comment..."
                      className="flex-1 resize-none bg-black/50 border-yellow-400/20 focus:border-yellow-400 text-white placeholder:text-gray-500 rounded-xl"
                      rows={2}
                    />
                    <Button
                      type="submit"
                      disabled={!newComment.trim()}
                      className="bg-yellow-400 hover:bg-yellow-500 text-black h-auto px-6 disabled:opacity-50"
                    >
                      <Send className="h-5 w-5" />
                    </Button>
                  </div>
                </form>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
