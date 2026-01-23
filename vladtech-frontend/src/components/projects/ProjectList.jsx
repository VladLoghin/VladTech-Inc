// src/components/projects/ProjectList.jsx
import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { api } from "../../api/http";

const formatAssignedEmployees = (assignedEmployeeIds, employeeIndex, t) => {
  if (!assignedEmployeeIds || assignedEmployeeIds.length === 0) return t("project.none");
  return assignedEmployeeIds
    .map((id) => employeeIndex?.[id]?.email || employeeIndex?.[id]?.name || id)
    .join(", ");
};

const formatArchivedAt = (archivedAt, locale) => {
  if (!archivedAt) return null;
  const date = new Date(archivedAt);
  return date.toLocaleString(locale, {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

// ✅ When axios baseURL = "/api", requests should be "/uploads/..." not "/api/uploads/..."
const toAxiosRelativePath = (photoUrl) => {
  if (!photoUrl) return "";
  if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) return photoUrl;

  // Ensure it starts with "/"
  let path = photoUrl.startsWith("/") ? photoUrl : `/${photoUrl}`;

  // If backend returned "/api/uploads/..", remove the leading "/api"
  if (path.startsWith("/api/")) {
    path = path.slice(4); // removes "/api"
  }

  return path; // now like "/uploads/projects/<id>"
};

const formatMoney = (amount, currency, locale) => {
  if (amount === null || amount === undefined || amount === "") return null;
  const num = typeof amount === "number" ? amount : Number(amount);
  if (Number.isNaN(num)) return null;
  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency: currency || "CAD",
  }).format(num);
};

const getStatusBadgeClasses = (status) => {
  const s = status || "PENDING";
  if (s === "COMPLETED") return "bg-green-100 text-green-800";
  if (s === "IN_PROGRESS") return "bg-blue-100 text-blue-800";
  return "bg-yellow-100 text-yellow-800";
};

const ModalShell = ({ title, onClose, children }) => {
  return (
    <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
      <div className="bg-white w-full max-w-lg rounded-xl border-2 border-black shadow-xl">
        <div className="flex items-center justify-between p-4 border-b border-black/10">
          <h3 className="text-lg font-bold">{title}</h3>
          <button
            type="button"
            onClick={onClose}
            className="text-xl font-bold leading-none px-2 hover:text-black/70"
            aria-label="Close"
          >
            ×
          </button>
        </div>
        <div className="p-4">{children}</div>
      </div>
    </div>
  );
};

const ProjectList = ({
  projects,
  onEdit,
  onComplete,
  onReactivate,
  employeeIndex,
  showEdit = true,
  showComplete = false,
  showReactivate = false,
  showStatusControl = false,
  onUpdateStatus,

  showUploadInformation = false,
  onUploadInformation,
  showViewInformation = false,

  // ✅ REQUIRED so Admin can load protected image endpoints
  getToken, // async () => string
}) => {
  const { t, i18n } = useTranslation();
  const locale = i18n.language === "fr" ? "fr-CA" : "en-CA";

  const [uploadOpen, setUploadOpen] = useState(false);
  const [viewOpen, setViewOpen] = useState(false);
  const [activeProject, setActiveProject] = useState(null);

  const [comments, setComments] = useState("");
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);

  const [imageSrc, setImageSrc] = useState("");
  const [imageLoading, setImageLoading] = useState(false);
  const [imageError, setImageError] = useState("");

  const fileInputId = useMemo(() => {
    if (!activeProject?.projectIdentifier) return "project-info-file";
    return `project-info-file-${activeProject.projectIdentifier}`;
  }, [activeProject]);

  const latestInfo = useMemo(() => {
    if (!activeProject) return null;
    const photos = activeProject.photos || [];
    return photos.length > 0 ? photos[0] : null;
  }, [activeProject]);

  const openUpload = (project) => {
    setActiveProject(project);
    setComments("");
    setFile(null);
    setUploadOpen(true);
  };

  const openView = (project) => {
    setActiveProject(project);
    setViewOpen(true);
  };

  const submitUpload = async () => {
    if (!activeProject || !file) return;
    try {
      setUploading(true);
      await onUploadInformation?.(activeProject, file, comments);
      setUploadOpen(false);
    } finally {
      setUploading(false);
    }
  };

  // ✅ Load protected image as blob with auth header
  useEffect(() => {
    let objectUrl = "";

    const loadImage = async () => {
      if (!viewOpen) return;

      setImageSrc("");
      setImageError("");

      if (!latestInfo?.photoUrl) return;

      try {
        setImageLoading(true);

        if (!getToken) {
          throw new Error("Missing token provider (getToken prop)");
        }

        const token = await getToken();
        if (!token) throw new Error("Missing token");

        const path = toAxiosRelativePath(latestInfo.photoUrl);

        const res = await api.get(path, {
          responseType: "blob",
          headers: { Authorization: `Bearer ${token}` },
        });

        objectUrl = URL.createObjectURL(res.data);
        setImageSrc(objectUrl);
      } catch (e) {
        console.error("Failed to load image:", e);
        setImageError(t("project.imageFailedToLoad", { defaultValue: "Image failed to load." }));
      } finally {
        setImageLoading(false);
      }
    };

    loadImage();

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [viewOpen, latestInfo?.photoUrl, getToken, t]);

  return (
    <>
      <div className="border-2 border-black rounded-xl bg-white p-4 max-h-[400px] overflow-y-auto space-y-4">
        {projects.length === 0 && (
          <p className="text-black/60 text-center py-8">{t("project.noProjectsFound")}</p>
        )}

        {projects.map((project) => {
          const isArchived = project.state === "COMPLETE";
          const hasInfo = (project.photos || []).length > 0;

          const estimatedCostFormatted = formatMoney(
            project.estimatedCost,
            project.estimatedCostCurrency || "CAD",
            locale
          );

          return (
            <div
              key={project.projectIdentifier}
              className={`border border-black/10 rounded-lg p-4 hover:shadow-md transition-shadow relative ${
                isArchived ? "bg-gray-50 opacity-75" : ""
              }`}
            >
              <div className="absolute right-4 top-4 flex gap-2 flex-wrap justify-end">
                {/* Upload / View Information (your work) */}
                {showUploadInformation && !isArchived && (
                  <button
                    type="button"
                    onClick={() => openUpload(project)}
                    className="px-4 py-2 bg-black text-white rounded-lg hover:bg-black/80 transition-all font-semibold"
                  >
                    {t("project.uploadInformation", { defaultValue: "Upload Information" })}
                  </button>
                )}

                {showViewInformation && (
                  <button
                    type="button"
                    onClick={() => openView(project)}
                    className="px-4 py-2 border-2 border-black text-black rounded-lg hover:bg-black hover:text-white transition-all font-semibold"
                  >
                    {t("project.viewInformation", { defaultValue: "View Information" })}
                  </button>
                )}

                {/* Complete / Reactivate / Edit (teammate + existing behavior) */}
                {showReactivate && isArchived && (
                  <button
                    type="button"
                    onClick={() => onReactivate?.(project)}
                    className="px-5 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-all font-semibold"
                  >
                    {t("project.reactivate")}
                  </button>
                )}

                {showComplete && !isArchived && (
                  <button
                    type="button"
                    onClick={() => onComplete?.(project)}
                    className="px-5 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-all font-semibold"
                  >
                    {t("project.markComplete")}
                  </button>
                )}

                {showEdit && !isArchived && (
                  <button
                    type="button"
                    onClick={() => onEdit?.(project)}
                    className="px-5 py-2 border-2 border-black text-black rounded-lg hover:bg-black hover:text-white transition-all font-semibold"
                  >
                    {t("edit")}
                  </button>
                )}
              </div>

              <div className="flex items-center gap-2 mb-2">
                <h3 className="text-lg font-semibold">{project.name}</h3>
                {project.state && (
                  <span
                    className={`text-xs px-2 py-1 rounded font-medium ${
                      isArchived ? "bg-gray-200 text-gray-700" : "bg-green-100 text-green-700"
                    }`}
                  >
                    {isArchived ? t("project.archived") : t("project.active")}
                  </span>
                )}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                <p>
                  <strong className="text-black/60">{t("project.id")}:</strong>{" "}
                  <span className="font-mono">{project.projectIdentifier}</span>
                </p>

                <p>
                  <strong className="text-black/60">{t("project.projectType")}:</strong>{" "}
                  <span className="bg-yellow-100 px-2 py-1 rounded">{project.projectType}</span>
                </p>

                <p>
                  <strong className="text-black/60">{t("project.estimatedCost")}:</strong>{" "}
                  {estimatedCostFormatted ? (
                    <span>{estimatedCostFormatted}</span>
                  ) : (
                    <span className="text-gray-400">N/A</span>
                  )}
                </p>

                <p>
                  <strong className="text-black/60">{t("project.startDate")}:</strong> {project.startDate}
                </p>

                <p>
                  <strong className="text-black/60">{t("project.dueDate")}:</strong> {project.dueDate}
                </p>

                {isArchived && project.archivedAt && (
                  <p>
                    <strong className="text-black/60">{t("project.archivedAt")}:</strong>{" "}
                    <span className="text-gray-600">{formatArchivedAt(project.archivedAt, locale)}</span>
                  </p>
                )}

                {/* Assigned employees (your existing helper) */}
                {project.assignedEmployeeIds?.length > 0 && (
                  <p className="md:col-span-2">
                    <strong className="text-black/60">{t("project.assignedEmployees")}:</strong>{" "}
                    {formatAssignedEmployees(project.assignedEmployeeIds, employeeIndex, t)}
                  </p>
                )}

                {/* Status badge (teammate) */}
                <p className="md:col-span-2">
                  <strong className="text-black/60">{t("project.status")}:</strong>{" "}
                  <span className={`px-2 py-1 rounded ${getStatusBadgeClasses(project.status)}`}>
                    {project.status === "COMPLETED"
                      ? t("project.completed")
                      : project.status === "IN_PROGRESS"
                      ? t("project.inProgress")
                      : t("project.pending")}
                  </span>
                </p>
              </div>

              {showViewInformation && (
                <p className="mt-3 text-sm text-black/60">
                  {hasInfo
                    ? t("project.informationAvailable", { defaultValue: "Information uploaded." })
                    : t("project.noInformationYet", { defaultValue: "No information uploaded yet." })}
                </p>
              )}
            </div>
          );
        })}
      </div>

      {uploadOpen && (
        <ModalShell
          title={t("project.uploadInformation", { defaultValue: "Upload Information" })}
          onClose={() => setUploadOpen(false)}
        >
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-semibold text-black/60 mb-1">
                {t("project.comments", { defaultValue: "Comments" })}
              </label>
              <textarea
                value={comments}
                onChange={(e) => setComments(e.target.value)}
                className="w-full min-h-[110px] px-3 py-2 border-2 border-black/20 rounded-lg"
                placeholder={t("project.commentsPlaceholder", {
                  defaultValue: "Write what you did today...",
                })}
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-black/60 mb-2">
                {t("project.choosePhoto", { defaultValue: "Choose a photo" })}
              </label>

              <input
                id={fileInputId}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={(e) => setFile(e.target.files?.[0] || null)}
              />

              <div className="flex items-center gap-3">
                <label
                  htmlFor={fileInputId}
                  className="inline-flex items-center justify-center px-4 py-2 rounded-lg bg-black text-white font-semibold cursor-pointer hover:bg-black/80"
                >
                  {t("project.choosePhoto", { defaultValue: "Choose a photo" })}
                </label>

                <span className="text-sm text-black/60">
                  {file ? file.name : t("project.noFileChosen", { defaultValue: "No file chosen" })}
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={submitUpload}
              disabled={!file || uploading}
              className={`w-full px-4 py-2 rounded-lg font-semibold ${
                !file || uploading
                  ? "bg-gray-200 text-gray-500 cursor-not-allowed"
                  : "bg-yellow-400 text-black hover:bg-yellow-500"
              }`}
            >
              {uploading
                ? t("project.uploading", { defaultValue: "Uploading..." })
                : t("submit", { defaultValue: "Submit" })}
            </button>
          </div>
        </ModalShell>
      )}

      {viewOpen && (
        <ModalShell
          title={t("project.viewInformation", { defaultValue: "View Information" })}
          onClose={() => setViewOpen(false)}
        >
          {!latestInfo ? (
            <p className="text-black/60">
              {t("project.noInformationYet", { defaultValue: "No information uploaded yet." })}
            </p>
          ) : (
            <div className="space-y-4">
              <div className="border border-black/10 rounded-lg p-3 bg-gray-50">
                <p className="text-sm font-semibold mb-2">
                  {t("project.workProof", { defaultValue: "Work proof" })}
                </p>

                {imageLoading && (
                  <p className="text-black/60">
                    {t("project.loadingImage", { defaultValue: "Loading image..." })}
                  </p>
                )}

                {imageError && <p className="text-sm text-red-600">{imageError}</p>}

                {imageSrc && (
                  <img
                    src={imageSrc}
                    alt={t("project.workProof", { defaultValue: "Work proof" })}
                    className="w-full rounded-lg border border-black/10"
                  />
                )}
              </div>

              <div>
                <p className="text-sm font-semibold text-black/60 mb-1">
                  {t("project.comments", { defaultValue: "Comments" })}
                </p>
                <p className="text-sm">
                  {latestInfo.description?.trim()
                    ? latestInfo.description
                    : t("project.noComments", { defaultValue: "No comments." })}
                </p>
              </div>
            </div>
          )}
        </ModalShell>
      )}
    </>
  );
};

export default ProjectList;
