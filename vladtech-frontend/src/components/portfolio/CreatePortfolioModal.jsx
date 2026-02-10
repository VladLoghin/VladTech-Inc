import { useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { useTranslation } from "react-i18next";
import { createPortfolioItem } from "../../api/portfolio/portfolioService";
import { X, Upload, Trash2, Plus } from "lucide-react";
import { api } from "../../api/http";

export default function CreatePortfolioModal({ isOpen, onClose, onSuccess }) {
  const { t } = useTranslation();
  const { getAccessTokenSilently } = useAuth0();
  const [formData, setFormData] = useState({
    title: "",
    type: "Interior",
  });
  const [imageFiles, setImageFiles] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  const portfolioTypes = [
    "Interior",
    "Kitchen",
    "Bathroom",
    "Exterior/Yard"
  ];

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    const maxSize = 10 * 1024 * 1024; // 10MB

    for (const file of files) {
      if (file.size > maxSize) {
        setError("One or more images exceed 10MB. Please compress or resize.");
        return;
      }
    }

    setError("");
    const newFiles = [...imageFiles, ...files];
    setImageFiles(newFiles);

    // Create previews for the new files
    files.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreviews((prev) => [...prev, reader.result]);
      };
      reader.readAsDataURL(file);
    });
  };

  const removeImage = (index) => {
    setImageFiles((prev) => prev.filter((_, i) => i !== index));
    setImagePreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (imageFiles.length === 0) {
      setError("Please select at least one image file");
      return;
    }

    setIsSubmitting(true);

    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      });

      // Upload all images
      const uploadedUrls = [];
      for (const file of imageFiles) {
        const formDataUpload = new FormData();
        formDataUpload.append("file", file);

        const uploadResponse = await api.post("/portfolio/upload", formDataUpload, {
          headers: { Authorization: `Bearer ${token}` },
        });

        uploadedUrls.push(uploadResponse.data.imageUrl);
      }

      // Create portfolio item with all uploaded image URLs
      await createPortfolioItem(
        formData.title,
        uploadedUrls[0], // Primary image
        uploadedUrls,     // All images
        formData.type,
        token
      );

      // Reset form
      setFormData({ title: "", type: "Interior" });
      setImageFiles([]);
      setImagePreviews([]);
      onSuccess?.();
      onClose();
    } catch (err) {
      console.error("Error creating portfolio item:", err);
      setError("Failed to create portfolio item. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetAndClose = () => {
    setFormData({ title: "", type: "Interior" });
    setImageFiles([]);
    setImagePreviews([]);
    setError("");
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl max-w-lg w-full p-6 relative max-h-[90vh] overflow-y-auto">
        <button
          onClick={resetAndClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600"
        >
          <X className="h-6 w-6" />
        </button>

        <h2 className="text-2xl font-bold mb-6">{t('portfolio.createItem')}</h2>

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

            {/* Image previews grid */}
            {imagePreviews.length > 0 && (
              <div className="grid grid-cols-3 gap-2 mb-3">
                {imagePreviews.map((preview, index) => (
                  <div key={index} className="relative group">
                    <img
                      src={preview}
                      alt={`Preview ${index + 1}`}
                      className="w-full h-24 object-cover rounded-lg border border-gray-200"
                    />
                    {index === 0 && (
                      <span className="absolute top-1 left-1 bg-yellow-400 text-black text-xs px-1.5 py-0.5 rounded font-bold">
                        Main
                      </span>
                    )}
                    <button
                      type="button"
                      onClick={() => removeImage(index)}
                      className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                    >
                      <Trash2 className="h-3 w-3" />
                    </button>
                  </div>
                ))}
              </div>
            )}

            {/* Upload area */}
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-yellow-400 transition-colors">
              <input
                type="file"
                accept="image/*"
                multiple
                onChange={handleImageChange}
                className="hidden"
                id="image-upload"
              />
              <label htmlFor="image-upload" className="cursor-pointer flex flex-col items-center">
                {imagePreviews.length > 0 ? (
                  <>
                    <Plus className="h-8 w-8 text-gray-400 mb-2" />
                    <span className="text-sm text-gray-600">
                      {t('portfolio.addMoreImages', { defaultValue: 'Add more images' })}
                    </span>
                  </>
                ) : (
                  <>
                    <Upload className="h-12 w-12 text-gray-400 mb-2" />
                    <span className="text-sm text-gray-600">
                      {t('portfolio.clickUpload')}
                    </span>
                    <span className="text-xs text-gray-500 mt-1">
                      {t('portfolio.fileFormat')}
                    </span>
                  </>
                )}
              </label>
            </div>
            {imagePreviews.length > 0 && (
              <p className="text-xs text-gray-500 mt-1">
                {imagePreviews.length} image{imagePreviews.length !== 1 ? 's' : ''} selected. First image will be the main thumbnail.
              </p>
            )}
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={resetAndClose}
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
              {isSubmitting ? "Creating..." : t('portfolio.create')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
