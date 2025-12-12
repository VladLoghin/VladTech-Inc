import { useState, useEffect } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { getAllPortfolioItems, deletePortfolioItem } from "../../api/portfolio/portfolioService";
import { X, Trash2 } from "lucide-react";

export default function DeletePortfolioModal({ isOpen, onClose, onSuccess }) {
  const { getAccessTokenSilently } = useAuth0();
  const [portfolioItems, setPortfolioItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [deleting, setDeleting] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isOpen) {
      fetchPortfolioItems();
    }
  }, [isOpen]);

  const fetchPortfolioItems = async () => {
    setLoading(true);
    setError("");
    try {
      const items = await getAllPortfolioItems();
      setPortfolioItems(items);
    } catch (err) {
      console.error("Error fetching portfolio items:", err);
      setError("Failed to load portfolio items");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (portfolioId, title) => {
    if (!confirm(`Are you sure you want to delete "${title}"?`)) {
      return;
    }

    setDeleting(portfolioId);
    setError("");

    try {
      const token = await getAccessTokenSilently();
      await deletePortfolioItem(portfolioId, token);
      
      // Remove from local state
      setPortfolioItems(portfolioItems.filter(item => item.portfolioId !== portfolioId));
      onSuccess?.();
    } catch (err) {
      console.error("Error deleting portfolio item:", err);
      setError("Failed to delete portfolio item. Please try again.");
    } finally {
      setDeleting(null);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl max-w-2xl w-full max-h-[80vh] flex flex-col relative">
        <div className="p-6 border-b border-gray-200 flex items-center justify-between">
          <h2 className="text-2xl font-bold">Delete Portfolio Item</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        {error && (
          <div className="mx-6 mt-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
            {error}
          </div>
        )}

        <div className="flex-1 overflow-y-auto p-6">
          {loading ? (
            <div className="text-center py-8 text-gray-500">Loading portfolio items...</div>
          ) : portfolioItems.length === 0 ? (
            <div className="text-center py-8 text-gray-500">No portfolio items found</div>
          ) : (
            <div className="space-y-3">
              {portfolioItems.map((item) => (
                <div
                  key={item.portfolioId}
                  className="flex items-center gap-4 p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
                >
                  <img
                    src={`http://localhost:8080${item.imageUrl}`}
                    alt={item.title}
                    className="w-20 h-20 object-cover rounded"
                  />
                  <div className="flex-1">
                    <h3 className="font-semibold">{item.title}</h3>
                    <p className="text-sm text-gray-500">Rating: {item.rating}/5.0</p>
                    <p className="text-xs text-gray-400">{item.comments?.length || 0} comments</p>
                  </div>
                  <button
                    onClick={() => handleDelete(item.portfolioId, item.title)}
                    disabled={deleting === item.portfolioId}
                    className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg font-semibold disabled:opacity-50 flex items-center gap-2"
                  >
                    <Trash2 className="h-4 w-4" />
                    {deleting === item.portfolioId ? "Deleting..." : "Delete"}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="p-6 border-t border-gray-200">
          <button
            onClick={onClose}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 font-semibold"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
