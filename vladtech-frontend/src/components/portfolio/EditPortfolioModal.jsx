import { useState, useEffect } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { useTranslation } from "react-i18next";
import { updatePortfolioItem } from "../../api/portfolio/portfolioService";
import { X, Upload, Trash2, Plus, Search } from "lucide-react";
import { api } from "../../api/http";
import getImageUrl from "../../utils/getImageUrl.js";

export default function EditPortfolioModal({ isOpen, onClose, onSuccess, portfolioItem: externalItem }) {
  const { t } = useTranslation();
  const { getAccessTokenSilently } = useAuth0();
  const [formData, setFormData] = useState({ title: "", type: "Interior" });
  const [existingImageUrls, setExistingImageUrls] = useState([]);
  const [newImageFiles, setNewImageFiles] = useState([]);
  const [newImagePreviews, setNewImagePreviews] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  // Portfolio item selection (when no item is passed externally)
  const [portfolioItems, setPortfolioItems] = useState([]);
  const [selectedItem, setSelectedItem] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [loadingItems, setLoadingItems] = useState(false);

  const portfolioItem = externalItem || selectedItem;

  const portfolioTypes = ["Interior", "Kitchen", "Bathroom", "Exterior/Yard"];

  // Fetch portfolio items when modal opens without an external item
  useEffect(() => {
    if (isOpen && !externalItem) {
      const fetchItems = async () => {
        setLoadingItems(true);
        try {
          const response = await api.get("/portfolio");
          setPortfolioItems(response.data);
        } catch (err) {
          console.error("Error fetching portfolio items:", err);
        } finally {
          setLoadingItems(false);
        }
      };
      fetchItems();
      setSelectedItem(null);
      setSearchQuery("");
    }
  }, [isOpen, externalItem]);

  useEffect(() => {
    if (portfolioItem && isOpen) {
      setFormData({
        title: portfolioItem.title || "",
        type: portfolioItem.type || "Interior",
      });
      // Use imageUrls if available, otherwise fall back to single imageUrl
      const urls = portfolioItem.imageUrls && portfolioItem.imageUrls.length > 0
        ? portfolioItem.imageUrls
        : portfolioItem.imageUrl ? [portfolioItem.imageUrl] : [];
      setExistingImageUrls(urls);
      setNewImageFiles([]);
      setNewImagePreviews([]);
      setError("");
    }
  }, [portfolioItem, isOpen]);

  const handleNewImageChange = (e) => {
    const files = Array.from(e.target.files);
    const maxSize = 10 * 1024 * 1024;

    for (const file of files) {
      if (file.size > maxSize) {
        setError("One or more images exceed 10MB. Please compress or resize.");
        return;
      }
    }

    setError("");
    setNewImageFiles((prev) => [...prev, ...files]);

    files.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setNewImagePreviews((prev) => [...prev, reader.result]);
      };
      reader.readAsDataURL(file);
    });
  };

  const removeExistingImage = (index) => {
    setExistingImageUrls((prev) => prev.filter((_, i) => i !== index));
  };

  const removeNewImage = (index) => {
    setNewImageFiles((prev) => prev.filter((_, i) => i !== index));
    setNewImagePreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const totalImages = existingImageUrls.length + newImageFiles.length;
    if (totalImages === 0) {
      setError("Please keep or add at least one image.");
      return;
    }

    setIsSubmitting(true);

    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });

      // Upload new images
      const uploadedUrls = [];
      for (const file of newImageFiles) {
        const formDataUpload = new FormData();
        formDataUpload.append("file", file);

        const uploadResponse = await api.post("/portfolio/upload", formDataUpload, {
          headers: { Authorization: `Bearer ${token}` },
        });

        uploadedUrls.push(uploadResponse.data.imageUrl);
      }

      // Combine existing + newly uploaded URLs
      const allImageUrls = [...existingImageUrls, ...uploadedUrls];

      await updatePortfolioItem(
        portfolioItem.portfolioId,
        formData.title,
        allImageUrls,
        formData.type,
        token
      );

      onSuccess?.();
      onClose();
    } catch (err) {
      console.error("Error updating portfolio item:", err);
      setError("Failed to update portfolio item. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  const filteredItems = portfolioItems.filter((item) =>
    item.title.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Step 1: Select a portfolio item if none is provided
  if (!portfolioItem) {
    return (
      <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl max-w-lg w-full p-6 relative max-h-[90vh] overflow-y-auto">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-gray-400 hover:text-gray-600"
          >
            <X className="h-6 w-6" />
          </button>

          <h2 className="text-2xl font-bold mb-4">
            {t('portfolio.selectItem')}
          </h2>

          <div className="relative mb-4">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder={t('portfolio.searchItems')}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400"
            />
          </div>

          {loadingItems ? (
            <p className="text-center text-gray-500 py-8">Loading...</p>
          ) : filteredItems.length === 0 ? (
            <p className="text-center text-gray-500 py-8">No portfolio items found.</p>
          ) : (
            <div className="space-y-2 max-h-96 overflow-y-auto">
              {filteredItems.map((item) => (
                <button
                  key={item.portfolioId}
                  onClick={() => setSelectedItem(item)}
                  className="w-full flex items-center gap-3 p-3 border border-gray-200 rounded-lg hover:bg-yellow-50 hover:border-yellow-400 transition-all text-left"
                >
                  <img
                    src={getImageUrl(item.imageUrl)}
                    alt={item.title}
                    className="w-16 h-16 object-cover rounded-lg flex-shrink-0"
                  />
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-sm truncate">{item.title}</p>
                    <p className="text-xs text-gray-500">{item.type}</p>
                    <p className="text-xs text-gray-400">
                      {(item.imageUrls?.length || 1)} image{(item.imageUrls?.length || 1) !== 1 ? 's' : ''}
                    </p>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    );
  }

  // Step 2: Edit form

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl max-w-lg w-full p-6 relative max-h-[90vh] overflow-y-auto">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600"
        >
          <X className="h-6 w-6" />
        </button>

        <h2 className="text-2xl font-bold mb-6">
          {t('portfolio.editItem')}
        </h2>

        {error && (
          <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold mb-2">
              {t('portfolio.title')} <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-semibold mb-2">
              Type <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400"
              required
            >
              {portfolioTypes.map((type) => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-semibold mb-2">
              {t('portfolio.image')} <span className="text-red-500">*</span>
            </label>

            {/* Existing images */}
            {existingImageUrls.length > 0 && (
              <div className="mb-3">
                <p className="text-xs font-semibold text-gray-500 mb-1 uppercase">
                  {t('portfolio.currentImages')}
                </p>
                <div className="grid grid-cols-3 gap-2">
                  {existingImageUrls.map((url, index) => (
                    <div key={`existing-${index}`} className="relative group">
                      <img
                        src={getImageUrl(url)}
                        alt={`Current ${index + 1}`}
                        className="w-full h-24 object-cover rounded-lg border border-gray-200"
                      />
                      {index === 0 && existingImageUrls.length + newImageFiles.length > 1 && (
                        <span className="absolute top-1 left-1 bg-yellow-400 text-black text-xs px-1.5 py-0.5 rounded font-bold">
                          Main
                        </span>
                      )}
                      <button
                        type="button"
                        onClick={() => removeExistingImage(index)}
                        className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                      >
                        <Trash2 className="h-3 w-3" />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* New image previews */}
            {newImagePreviews.length > 0 && (
              <div className="mb-3">
                <p className="text-xs font-semibold text-gray-500 mb-1 uppercase">
                  {t('portfolio.newImages')}
                </p>
                <div className="grid grid-cols-3 gap-2">
                  {newImagePreviews.map((preview, index) => (
                    <div key={`new-${index}`} className="relative group">
                      <img
                        src={preview}
                        alt={`New ${index + 1}`}
                        className="w-full h-24 object-cover rounded-lg border-2 border-green-300"
                      />
                      <button
                        type="button"
                        onClick={() => removeNewImage(index)}
                        className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                      >
                        <Trash2 className="h-3 w-3" />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Upload area */}
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center hover:border-yellow-400 transition-colors">
              <input
                type="file"
                accept="image/*"
                multiple
                onChange={handleNewImageChange}
                className="hidden"
                id="edit-image-upload"
              />
              <label htmlFor="edit-image-upload" className="cursor-pointer flex flex-col items-center">
                <Plus className="h-8 w-8 text-gray-400 mb-1" />
                <span className="text-sm text-gray-600">
                  {t('portfolio.addMoreImages')}
                </span>
              </label>
            </div>
            <p className="text-xs text-gray-500 mt-1">
              {existingImageUrls.length + newImageFiles.length} image{(existingImageUrls.length + newImageFiles.length) !== 1 ? 's' : ''} total. First image will be the main thumbnail.
            </p>
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 font-semibold"
              disabled={isSubmitting}
            >
              {t('cancel')}
            </button>
            <button
              type="submit"
              className="flex-1 px-4 py-2 bg-yellow-400 hover:bg-yellow-500 text-black rounded-lg font-semibold disabled:opacity-50"
              disabled={isSubmitting}
            >
              {isSubmitting ? t('saving') : t('save')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
