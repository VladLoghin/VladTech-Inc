import { useState } from "react";
import { X, Star, Send, User, ArrowLeft } from "lucide-react";
import { Button } from "../components/button";
import { Textarea } from "../components/textarea";
import { motion, AnimatePresence } from "motion/react";

interface PortfolioGalleryProps {
  onNavigate: (page: string) => void;
}

interface PortfolioItem {
  id: number;
  url: string;
  title: string;
  rating: number;
  comments: Comment[];
}

interface Comment {
  id: number;
  user: string;
  text: string;
  timestamp: string;
}

const portfolioItems: PortfolioItem[] = [
  {
    id: 1,
    url: "https://images.unsplash.com/photo-1681216868987-b7268753b81c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtb2Rlcm4lMjBhcmNoaXRlY3R1cmUlMjBidWlsZGluZ3xlbnwxfHx8fDE3NjIyMTI5NjN8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Modern Glass Tower",
    rating: 4.8,
    comments: [
      { id: 1, user: "Sarah M.", text: "Stunning architecture! Love the clean lines.", timestamp: "2 hours ago" },
      { id: 2, user: "John D.", text: "The attention to detail is incredible.", timestamp: "5 hours ago" },
    ],
  },
  {
    id: 2,
    url: "https://images.unsplash.com/photo-1618385455730-2571c38966b7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjb25zdHJ1Y3Rpb24lMjB0ZWNobm9sb2d5fGVufDF8fHx8MTc2MjE1MjYyNXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Construction Tech Hub",
    rating: 4.9,
    comments: [
      { id: 1, user: "Mike R.", text: "State-of-the-art facility!", timestamp: "1 day ago" },
    ],
  },
  {
    id: 3,
    url: "https://images.unsplash.com/photo-1629132497808-1c55ac45c4da?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxlbmdpbmVlcmluZyUyMHByb2plY3R8ZW58MXx8fHwxNzYyMjAwMTUxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Engineering Complex",
    rating: 4.7,
    comments: [
      { id: 1, user: "Emma L.", text: "Impressive engineering work!", timestamp: "3 days ago" },
      { id: 2, user: "Alex K.", text: "The innovation here is next level.", timestamp: "4 days ago" },
    ],
  },
  {
    id: 4,
    url: "https://images.unsplash.com/photo-1612272362018-4c4084eebdd4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxnbGFzcyUyMHNreXNjcmFwZXJ8ZW58MXx8fHwxNzYyMjI3MDEzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Glass Skyscraper",
    rating: 4.6,
    comments: [
      { id: 1, user: "Lisa P.", text: "Beautiful integration with the cityscape.", timestamp: "1 week ago" },
    ],
  },
  {
    id: 5,
    url: "https://images.unsplash.com/photo-1572061971745-063e9cc83afc?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtb2Rlcm4lMjBjb25zdHJ1Y3Rpb258ZW58MXx8fHwxNzYyMTU1Mjk2fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Modern Construction",
    rating: 5.0,
    comments: [
      { id: 1, user: "David W.", text: "Perfect execution. 10/10", timestamp: "2 weeks ago" },
      { id: 2, user: "Nina S.", text: "Best project I've seen this year!", timestamp: "2 weeks ago" },
    ],
  },
  {
    id: 6,
    url: "https://images.unsplash.com/photo-1523477593243-78bbf626fd3b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxidWlsZGluZyUyMGZhY2FkZXxlbnwxfHx8fDE3NjIyMjcwMTR8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Building Facade",
    rating: 4.8,
    comments: [],
  },
  {
    id: 7,
    url: "https://images.unsplash.com/photo-1554793000-245d3a3c2a51?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx1cmJhbiUyMGFyY2hpdGVjdHVyZXxlbnwxfHx8fDE3NjIxNjY3MDB8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Urban Architecture",
    rating: 4.9,
    comments: [
      { id: 1, user: "Chris T.", text: "Outstanding work!", timestamp: "1 month ago" },
    ],
  },
  {
    id: 8,
    url: "https://images.unsplash.com/photo-1720762256650-ea429f429226?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjb25jcmV0ZSUyMGJ1aWxkaW5nfGVufDF8fHx8MTc2MjIyNzAxNXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Concrete Structure",
    rating: 4.7,
    comments: [],
  },
  {
    id: 9,
    url: "https://images.unsplash.com/photo-1609627016501-b862497c7294?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzdGVlbCUyMHN0cnVjdHVyZXxlbnwxfHx8fDE3NjIyMjcwMTV8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Steel Framework",
    rating: 4.8,
    comments: [
      { id: 1, user: "Rachel B.", text: "Incredible structural design!", timestamp: "1 month ago" },
    ],
  },
];

const allReviews = portfolioItems.flatMap(item => 
  item.comments.map(comment => ({
    ...comment,
    projectTitle: item.title,
    projectRating: item.rating,
  }))
);

export default function PortfolioGallery({ onNavigate }: PortfolioGalleryProps) {
  const [view, setView] = useState<"portfolio" | "reviews">("portfolio");
  const [selectedItem, setSelectedItem] = useState<PortfolioItem | null>(null);
  const [newComment, setNewComment] = useState("");

  const handleAddComment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedItem || !newComment.trim()) return;
    
    // Add comment to selected item
    const newCommentObj: Comment = {
      id: Date.now(),
      user: "You",
      text: newComment,
      timestamp: "Just now",
    };
    
    setSelectedItem({
      ...selectedItem,
      comments: [...selectedItem.comments, newCommentObj],
    });
    
    setNewComment("");
  };

  return (
    <div className="min-h-screen bg-black">
      {/* Black Navigation Bar */}
      <nav className="fixed top-0 left-0 right-0 z-50 bg-black backdrop-blur-xl border-b border-yellow-400/20 shadow-2xl">
        <div className="container mx-auto px-8 py-6 flex items-center justify-between">
          <button
            onClick={() => onNavigate("home")}
            className="flex items-center gap-2 text-yellow-400 hover:text-yellow-300 transition-colors tracking-wider"
          >
            <ArrowLeft className="h-5 w-5" />
            BACK
          </button>
          
          <div className="flex gap-12">
            <button
              onClick={() => setView("portfolio")}
              className={`transition-colors tracking-wider ${
                view === "portfolio" ? "text-white" : "text-white/40"
              }`}
            >
              PORTFOLIO
            </button>
            <button
              onClick={() => setView("reviews")}
              className={`transition-colors tracking-wider ${
                view === "reviews" ? "text-white" : "text-white/40"
              }`}
            >
              REVIEWS
            </button>
          </div>
          
          <div className="w-20"></div> {/* Spacer */}
        </div>
      </nav>

      {/* Content */}
      <div className="pt-20">
        {view === "portfolio" ? (
          // Portfolio Grid - No Gaps
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-0">
            {portfolioItems.map((item) => (
              <motion.div
                key={item.id}
                whileHover={{ scale: 1.05, zIndex: 10, transition: { duration: 0.2 } }}
                onClick={() => setSelectedItem(item)}
                className="cursor-pointer overflow-hidden aspect-square relative group"
              >
              
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
        ) : (
          // Reviews View
          <div className="container mx-auto px-8 py-16">
            <div className="max-w-4xl mx-auto space-y-6">
              {allReviews.length === 0 ? (
                <p className="text-center text-gray-400 py-16">No reviews yet.</p>
              ) : (
                allReviews.map((review) => (
                  <motion.div
                    key={review.id}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="bg-black border border-yellow-400/20 rounded-2xl p-6 hover:border-yellow-400/40 transition-all"
                  >
                    <div className="flex items-start gap-4">
                      <div className="w-12 h-12 bg-gradient-to-br from-yellow-400 to-yellow-600 rounded-full flex items-center justify-center flex-shrink-0">
                        <User className="h-6 w-6 text-black" />
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="text-white tracking-wide">{review.user}</h4>
                          <span className="text-gray-500 text-sm">{review.timestamp}</span>
                        </div>
                        <p className="text-gray-300 mb-3">{review.text}</p>
                        <div className="flex items-center gap-4 text-sm">
                          <span className="text-gray-500">Project: {review.projectTitle}</span>
                          <div className="flex items-center gap-1">
                            <Star className="h-4 w-4 text-yellow-400 fill-yellow-400" />
                            <span className="text-yellow-400">{review.projectRating}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </motion.div>
                ))
              )}
            </div>
          </div>
        )}
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
                    selectedItem.comments.map((comment) => (
                      <div key={comment.id} className="flex gap-3">
                        <div className="w-10 h-10 bg-gradient-to-br from-yellow-400 to-yellow-600 rounded-full flex items-center justify-center flex-shrink-0">
                          <User className="h-5 w-5 text-black" />
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="text-white tracking-wide">{comment.user}</span>
                            <span className="text-gray-500 text-sm">{comment.timestamp}</span>
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
