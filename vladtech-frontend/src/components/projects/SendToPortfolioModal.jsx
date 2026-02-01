import { useState } from "react";
import { useTranslation } from "react-i18next";
import { api } from "../../api/http";

const portfolioTypes = ["Interior", "Kitchen", "Bathroom", "Exterior/Yard"];

const SendToPortfolioModal = ({ project, isOpen, onClose, onSuccess, getToken }) => {
  const { t } = useTranslation();
  const [type, setType] = useState("Interior");
  const [imageFile, setImageFile] = useState(null);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");
  const [preview, setPreview] = useState(null);

  const handleImageChange = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      setImageFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreview(reader.result);
      };
      reader.readAsDataURL(file);
      setError("");
    }
  };

  const handleSubmit = async () => {
    if (!imageFile) {
      setError(t("project.pleaseSelectImage", { defaultValue: "Please select an image" }));
      return;
    }

    try {
      setSending(true);
      setError("");

      const token = await getToken();
      
      const formData = new FormData();
      formData.append("type", type);
      formData.append("image", imageFile);

      const response = await api.post(
        `/projects/${project.projectIdentifier}/send-to-portfolio`,
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
          },
        }
      );

      if (response.status === 200) {
        onSuccess?.();
        onClose();
      }
    } catch (err) {
      console.error("Failed to send to portfolio:", err);
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        t("project.failedToSendToPortfolio", { defaultValue: "Failed to send to portfolio" })
      );
    } finally {
      setSending(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
      <div className="bg-white w-full max-w-md rounded-xl border-2 border-black shadow-xl max-h-[70vh] flex flex-col">
        <div className="flex items-center justify-between p-4 border-b border-black/10 flex-shrink-0">
          <h3 className="text-lg font-bold">
            {t("project.sendToPortfolio", { defaultValue: "Send to Portfolio" })}
          </h3>
          <button
            type="button"
            onClick={onClose}
            className="text-3xl font-bold leading-none px-2 hover:text-red-600 transition-colors"
            aria-label="Close"
          >
            ×
          </button>
        </div>

        <div className="p-4 space-y-4 overflow-y-auto flex-1">
          {/* Project Name */}
          <div>
            <p className="text-sm text-black/60 mb-1">
              {t("project.project", { defaultValue: "Project" })}:
            </p>
            <p className="font-semibold">{project.name}</p>
          </div>

          {/* Portfolio Type */}
          <div>
            <label className="block text-sm font-semibold mb-2">
              {t("project.portfolioType", { defaultValue: "Portfolio Type" })} *
            </label>
            <select
              value={type}
              onChange={(e) => setType(e.target.value)}
              className="w-full px-4 py-2 border-2 border-black/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400"
            >
              {portfolioTypes.map((portfolioType) => (
                <option key={portfolioType} value={portfolioType}>
                  {portfolioType}
                </option>
              ))}
            </select>
          </div>

          {/* Image Upload */}
          <div>
            <label className="block text-sm font-semibold mb-2">
              {t("project.selectImage", { defaultValue: "Select Image" })} *
            </label>
            <input
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              className="hidden"
              id="portfolio-image-upload"
            />
            <label
              htmlFor="portfolio-image-upload"
              className="inline-block px-4 py-2 bg-black text-white rounded-lg hover:bg-black/80 cursor-pointer font-semibold"
            >
              {t("project.chooseImage", { defaultValue: "Choose Image" })}
            </label>
            {imageFile && (
              <p className="mt-2 text-sm text-black/60">
                {imageFile.name}
              </p>
            )}
          </div>

          {/* Image Preview */}
          {preview && (
            <div className="mt-4">
              <p className="text-sm font-semibold mb-2">
                {t("project.preview", { defaultValue: "Preview" })}:
              </p>
              <img
                src={preview}
                alt="Preview"
                className="w-full max-h-48 object-contain rounded-lg border-2 border-black/10"
              />
            </div>
          )}

          {/* Error Message */}
          {error && (
            <div className="p-3 bg-red-100 border border-red-300 rounded-lg text-red-700 text-sm">
              {error}
            </div>
          )}

          {/* Submit Button */}
          <button
            type="button"
            onClick={handleSubmit}
            disabled={!imageFile || sending}
            className={`w-full px-4 py-3 rounded-lg font-semibold ${
              !imageFile || sending
                ? "bg-gray-200 text-gray-500 cursor-not-allowed"
                : "bg-yellow-400 text-black hover:bg-yellow-500"
            }`}
          >
            {sending
              ? t("project.sending", { defaultValue: "Sending..." })
              : t("project.sendToPortfolio", { defaultValue: "Send to Portfolio" })}
          </button>
        </div>
      </div>
    </div>
  );
};

export default SendToPortfolioModal;
