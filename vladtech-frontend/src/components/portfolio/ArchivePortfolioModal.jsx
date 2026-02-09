import { useState, useEffect } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { useTranslation } from "react-i18next";
import { getAllPortfolioItems, archivePortfolioItem, unarchivePortfolioItem, getArchivedPortfolioItems } from "../../api/portfolio/portfolioService";
import { X, Archive, RotateCcw, AlertTriangle } from "lucide-react";
import getImageUrl from "../../utils/getImageUrl.js";
import { api } from "../../api/http";

export default function ArchivePortfolioModal({ isOpen, onClose, onSuccess }) {
  const { t } = useTranslation();
  const { getAccessTokenSilently } = useAuth0();
  const [activeTab, setActiveTab] = useState("active"); // "active" or "archived"
  const [portfolioItems, setPortfolioItems] = useState([]);
  const [archivedItems, setArchivedItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [processing, setProcessing] = useState(null);
  const [error, setError] = useState("");
  const [confirmAction, setConfirmAction] = useState(null); // { portfolioId, title, action: "archive" | "unarchive" }

  const fetchAllItems = async () => {
    setLoading(true);
    setError("");
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });
      
      const [active, archived] = await Promise.all([
        getAllPortfolioItems(),
        getArchivedPortfolioItems(token)
      ]);
      
      setPortfolioItems(active);
      setArchivedItems(archived);
    } catch (err) {
      console.error("Error fetching portfolio items:", err);
      setError("Failed to load portfolio items");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchAllItems();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen]);

  const handleAction = (portfolioId, title, action) => {
    setConfirmAction({ portfolioId, title, action });
  };

  const confirmActionHandler = async () => {
    if (!confirmAction) return;

    const { portfolioId, action } = confirmAction;
    setProcessing(portfolioId);
    setError("");
    setConfirmAction(null);

    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });

      if (action === "archive") {
        const item = portfolioItems.find(i => i.portfolioId === portfolioId);
        const reviewId = item?.reviewId;

        await archivePortfolioItem(portfolioId, token);

        // Reset review status if linked
        if (reviewId) {
          await api.patch(
            `/reviews/${reviewId}/set-create-portfolio`,
            {},
            { headers: { Authorization: `Bearer ${token}` } }
          );
        }

        // Move item from active to archived
        setPortfolioItems(prev => prev.filter(item => item.portfolioId !== portfolioId));
        if (item) {
          setArchivedItems(prev => [...prev, item]);
        }
      } else {
        // Unarchive
        const item = archivedItems.find(i => i.portfolioId === portfolioId);
        
        await unarchivePortfolioItem(portfolioId, token);

        // Move item from archived to active
        setArchivedItems(prev => prev.filter(item => item.portfolioId !== portfolioId));
        if (item) {
          setPortfolioItems(prev => [...prev, item]);
        }
      }

      onSuccess?.();
    } catch (err) {
      console.error(`Error ${confirmAction.action}ing portfolio item:`, err);
      setError(`Failed to ${confirmAction.action} portfolio item. Please try again.`);
    } finally {
      setProcessing(null);
    }
  };

  const cancelAction = () => {
    setConfirmAction(null);
  };

  if (!isOpen) return null;

  const currentItems = activeTab === "active" ? portfolioItems : archivedItems;
  const isArchiveAction = activeTab === "active";

  return (
    <>
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl max-w-2xl w-full max-h-[80vh] flex flex-col relative">
          <div className="p-6 border-b border-gray-200 flex items-center justify-between">
            <h2 className="text-2xl font-bold">{t('portfolio.manageArchive', { defaultValue: 'Manage Portfolio Archive' })}</h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              <X className="h-6 w-6" />
            </button>
          </div>

          {/* Tabs */}
          <div className="flex border-b border-gray-200">
            <button
              onClick={() => setActiveTab("active")}
              className={`flex-1 px-4 py-3 text-center font-semibold transition-colors ${
                activeTab === "active"
                  ? "border-b-2 border-red-500 text-red-600"
                  : "text-gray-500 hover:text-gray-700"
              }`}
            >
              <Archive className="h-4 w-4 inline mr-2" />
              {t('portfolio.activeItems', { defaultValue: 'Active Items' })} ({portfolioItems.length})
            </button>
            <button
              onClick={() => setActiveTab("archived")}
              className={`flex-1 px-4 py-3 text-center font-semibold transition-colors ${
                activeTab === "archived"
                  ? "border-b-2 border-green-500 text-green-600"
                  : "text-gray-500 hover:text-gray-700"
              }`}
            >
              <RotateCcw className="h-4 w-4 inline mr-2" />
              {t('portfolio.archivedItems', { defaultValue: 'Archived Items' })} ({archivedItems.length})
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
            ) : currentItems.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                {activeTab === "active" 
                  ? t('portfolio.noActiveItems', { defaultValue: 'No active portfolio items' })
                  : t('portfolio.noArchivedItems', { defaultValue: 'No archived portfolio items' })
                }
              </div>
            ) : (
              <div className="space-y-3">
                {currentItems.map((item) => (
                  <div
                    key={item.portfolioId}
                    className="flex items-center gap-4 p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
                  >
                    <img
                      src={getImageUrl(item.imageUrl)}
                      alt={item.title}
                      className="w-20 h-20 object-cover rounded"
                    />
                    <div className="flex-1">
                      <h3 className="font-semibold">{item.title}</h3>
                      <p className="text-sm text-gray-500">{item.type}</p>
                      <p className="text-xs text-gray-400">{item.comments?.length || 0} {t('portfolio.comments')}</p>
                    </div>
                    <button
                      onClick={() => handleAction(item.portfolioId, item.title, isArchiveAction ? "archive" : "unarchive")}
                      disabled={processing === item.portfolioId}
                      className={`px-4 py-2 text-white rounded-lg font-semibold disabled:opacity-50 flex items-center gap-2 ${
                        isArchiveAction 
                          ? "bg-red-500 hover:bg-red-600" 
                          : "bg-green-500 hover:bg-green-600"
                      }`}
                    >
                      {isArchiveAction ? <Archive className="h-4 w-4" /> : <RotateCcw className="h-4 w-4" />}
                      {processing === item.portfolioId 
                        ? (isArchiveAction ? "Archiving..." : "Restoring...") 
                        : (isArchiveAction ? t('archive', { defaultValue: 'Archive' }) : t('restore', { defaultValue: 'Restore' }))
                      }
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
              {t('close')}
            </button>
          </div>
        </div>
      </div>

      {/* Confirmation Modal */}
      {confirmAction && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-[60] p-4">
          <div className="bg-white rounded-xl max-w-md w-full shadow-2xl">
            <div className="p-6">
              <div className="flex items-center gap-4 mb-4">
                <div className={`flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center ${
                  confirmAction.action === "archive" ? "bg-red-100" : "bg-green-100"
                }`}>
                  {confirmAction.action === "archive" 
                    ? <AlertTriangle className="h-6 w-6 text-red-600" />
                    : <RotateCcw className="h-6 w-6 text-green-600" />
                  }
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">
                    {confirmAction.action === "archive" 
                      ? t('portfolio.confirmArchive', { defaultValue: 'Confirm Archive' })
                      : t('portfolio.confirmRestore', { defaultValue: 'Confirm Restore' })
                    }
                  </h3>
                  <p className="text-sm text-gray-500">
                    {confirmAction.action === "archive"
                      ? t('portfolio.archiveWarning', { defaultValue: 'This item will be hidden from the portfolio gallery' })
                      : t('portfolio.restoreInfo', { defaultValue: 'This item will be visible in the portfolio gallery again' })
                    }
                  </p>
                </div>
              </div>
              
              <p className="text-gray-700 mb-6">
                {confirmAction.action === "archive" 
                  ? `Are you sure you want to archive "${confirmAction.title}"?`
                  : `Are you sure you want to restore "${confirmAction.title}"?`
                }
              </p>

              <div className="flex gap-3">
                <button
                  onClick={cancelAction}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 font-semibold transition-colors"
                >
                  {t('cancel', { defaultValue: 'Cancel' })}
                </button>
                <button
                  onClick={confirmActionHandler}
                  className={`flex-1 px-4 py-2 text-white rounded-lg font-semibold transition-colors ${
                    confirmAction.action === "archive"
                      ? "bg-red-500 hover:bg-red-600"
                      : "bg-green-500 hover:bg-green-600"
                  }`}
                >
                  {confirmAction.action === "archive" 
                    ? t('archive', { defaultValue: 'Archive' })
                    : t('restore', { defaultValue: 'Restore' })
                  }
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
