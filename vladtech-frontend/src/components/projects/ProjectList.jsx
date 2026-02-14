import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAuth0 } from "@auth0/auth0-react";
import { api } from "../../api/http";
import SendToPortfolioModal from "./SendToPortfolioModal";
import { generateCsv, generatePdf } from "../../utils/exportUtils";
import { MapPin } from "lucide-react";
import {
  pinProject,
  unpinProject,
  isProjectPinned,
} from "../../api/projects/projectPinApi";
import {
  isValidStatusTransition,
  getStatusTransitionErrorMessage,
} from "../../utils/statusValidation";

const formatAddress = (addr) => {
  if (!addr) return null;
  const parts = [];
  if (addr.streetAddress) parts.push(addr.streetAddress);
  const cityProv = [addr.city, addr.province].filter(Boolean).join(", ");
  const cityProvPost = [cityProv, addr.postalCode].filter(Boolean).join(" ");
  if (cityProvPost) parts.push(cityProvPost);
  if (addr.country) parts.push(addr.country);
  return parts.length > 0 ? parts.join(", ") : null;
};

const formatAssignedEmployees = (
  assignedEmployeeIds,
  assignedEmployeeEmails,
  employeeIndex,
  t,
) => {
  // Prefer assignedEmployeeEmails if available - filter out any Auth0 IDs
  if (assignedEmployeeEmails && assignedEmployeeEmails.length > 0) {
    const validEmails = assignedEmployeeEmails.filter(
      (email) => email && !/^auth0\|/.test(email),
    );
    if (validEmails.length > 0) return validEmails.join(", ");
  }

  if (!assignedEmployeeIds || assignedEmployeeIds.length === 0)
    return t("project.none");
  const validIds = assignedEmployeeIds
    .map((id) => {
      const resolved = employeeIndex?.[id]?.email || employeeIndex?.[id]?.name;
      return resolved || null;
    })
    .filter(Boolean);

  return validIds.length > 0 ? validIds.join(", ") : t("project.none");
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
  if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://"))
    return photoUrl;

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

const formatEstimatedTime = (seconds) => {
  if (!seconds || seconds <= 0) return null;

  const SECONDS_IN_YEAR = 31536000; // 365 days
  const SECONDS_IN_MONTH = 2592000; // 30 days
  const SECONDS_IN_DAY = 86400;
  const SECONDS_IN_HOUR = 3600;

  let remaining = seconds;
  const parts = [];

  const years = Math.floor(remaining / SECONDS_IN_YEAR);
  if (years > 0) {
    parts.push(`${years}y`);
    remaining -= years * SECONDS_IN_YEAR;
  }

  const months = Math.floor(remaining / SECONDS_IN_MONTH);
  if (months > 0) {
    parts.push(`${months}mo`);
    remaining -= months * SECONDS_IN_MONTH;
  }

  const days = Math.floor(remaining / SECONDS_IN_DAY);
  if (days > 0) {
    parts.push(`${days}d`);
    remaining -= days * SECONDS_IN_DAY;
  }

  const hours = Math.floor(remaining / SECONDS_IN_HOUR);
  if (hours > 0) {
    parts.push(`${hours}h`);
  }

  return parts.length > 0 ? parts.join(" ") : null;
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
  isAdmin = false,

  // ✅ REQUIRED so Admin can load protected image endpoints
  getToken, // async () => string
}) => {
  const { t, i18n } = useTranslation();
  const { user } = useAuth0();
  const locale = i18n.language === "fr" ? "fr-CA" : "en-CA";

  const [uploadOpen, setUploadOpen] = useState(false);
  const [viewOpen, setViewOpen] = useState(false);
  const [activeProject, setActiveProject] = useState(null);
  const [portfolioModalOpen, setPortfolioModalOpen] = useState(false);
  const [exportOpen, setExportOpen] = useState(false);

  // Pin-related state
  const [pinnedProjects, setPinnedProjects] = useState(new Set());
  const [pinLoading, setPinLoading] = useState(false);

  const [comments, setComments] = useState("");
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);

  const [imageSrc, setImageSrc] = useState("");
  const [imageLoading, setImageLoading] = useState(false);
  const [imageError, setImageError] = useState("");

  // Status validation state
  const [statusWarning, setStatusWarning] = useState(null);

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

  const openPortfolioModal = (project) => {
    setActiveProject(project);
    setPortfolioModalOpen(true);
  };

  const handlePortfolioSuccess = () => {
    setPortfolioModalOpen(false);
    setActiveProject(null);
  };

  const openExport = (project) => {
    setActiveProject(project);
    setExportOpen(true);
  };

  const handleStatusChange = (project, newStatus) => {
    // Check if the transition is valid
    if (!isValidStatusTransition(project.status, newStatus)) {
      const errorMessage = getStatusTransitionErrorMessage(
        project.status,
        newStatus,
        t,
      );
      setStatusWarning({
        message: errorMessage,
        currentStatus: project.status,
        newStatus,
      });
      return;
    }

    // Valid transition, proceed with update
    onUpdateStatus?.(project, newStatus);
  };

  const cancelStatusChange = () => {
    setStatusWarning(null);
  };

  const submitUpload = async () => {
    if (!activeProject || (!file && !comments.trim())) return;
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
        setImageError(
          t("project.imageFailedToLoad", {
            defaultValue: "Image failed to load.",
          }),
        );
      } finally {
        setImageLoading(false);
      }
    };

    loadImage();

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [viewOpen, latestInfo?.photoUrl, getToken, t]);

  // Load pinned projects for current user
  useEffect(() => {
    const loadPinnedProjects = async () => {
      if (!getToken) return;

      try {
        const token = await getToken();
        if (!token) return;

        const pinStates = new Set();

        // Check pin status for each project
        for (const project of projects) {
          try {
            const isPinned = await isProjectPinned(
              project.projectIdentifier,
              token,
            );
            if (isPinned) {
              pinStates.add(project.projectIdentifier);
            }
          } catch (error) {
            console.error(
              `Error checking pin status for ${project.projectIdentifier}:`,
              error,
            );
          }
        }

        setPinnedProjects(pinStates);
      } catch (error) {
        console.error("Failed to load pinned projects:", error);
      }
    };

    loadPinnedProjects();
  }, [projects, getToken]);

  const handleTogglePin = async (project) => {
    if (!getToken) return;

    try {
      setPinLoading(true);
      const token = await getToken();
      if (!token) return;

      if (pinnedProjects.has(project.projectIdentifier)) {
        // Unpin
        await unpinProject(project.projectIdentifier, token);
        setPinnedProjects((prev) => {
          const newSet = new Set(prev);
          newSet.delete(project.projectIdentifier);
          return newSet;
        });
      } else {
        // Pin
        await pinProject(project.projectIdentifier, token);
        setPinnedProjects(
          (prev) => new Set([...prev, project.projectIdentifier]),
        );
      }
    } catch (error) {
      console.error("Error toggling project pin:", error);
    } finally {
      setPinLoading(false);
    }
  };

  // Sort projects with pinned ones first
  const sortedProjects = useMemo(() => {
    return [...projects].sort((a, b) => {
      const aPinned = pinnedProjects.has(a.projectIdentifier) ? 1 : 0;
      const bPinned = pinnedProjects.has(b.projectIdentifier) ? 1 : 0;
      return bPinned - aPinned;
    });
  }, [projects, pinnedProjects]);

  return (
    <>
      <div className="border-2 border-black rounded-xl bg-white p-4 space-y-4">
        {sortedProjects.length === 0 && (
          <p className="text-black/60 text-center py-8">
            {t("project.noProjectsFound")}
          </p>
        )}

        {sortedProjects.map((project) => {
          const isArchived = project.state === "COMPLETE";
          const hasInfo = (project.photos || []).length > 0;

          const estimatedCostFormatted = formatMoney(
            project.estimatedCost,
            project.estimatedCostCurrency || "CAD",
            locale,
          );

          return (
            <div
              key={project.projectIdentifier}
              className={`border border-black/10 rounded-lg p-4 hover:shadow-md transition-shadow relative flex flex-col ${isArchived ? "bg-gray-50 opacity-75" : ""
                }`}
            >
              {/* Buttons - grid layout on desktop, at bottom on mobile */}
              <div className="hidden sm:grid absolute right-4 top-4 gap-2 grid-cols-1 md:grid-cols-2 lg:grid-cols-3 max-w-[300px] lg:max-w-[450px]">
                {" "}
                {/* Send to Portfolio Button */}
                {isAdmin && !isArchived && (
                  <button
                    type="button"
                    onClick={() => openPortfolioModal(project)}
                    className="px-3 py-2 bg-yellow-400 text-black rounded-lg hover:bg-yellow-500 transition-all font-semibold text-sm text-center flex items-center justify-center"
                  >
                    {t("project.sendToPortfolio", {
                      defaultValue: "Send to Portfolio",
                    })}
                  </button>
                )}
                {/* Upload / View Information */}
                {showUploadInformation && !isArchived && (
                  <button
                    type="button"
                    onClick={() => openUpload(project)}
                    className="px-3 py-2 bg-black text-white rounded-lg hover:bg-black/80 transition-all font-semibold text-sm text-center flex items-center justify-center"
                  >
                    {t("project.uploadInformation", {
                      defaultValue: "Upload Information",
                    })}
                  </button>
                )}
                {showViewInformation && (
                  <button
                    type="button"
                    onClick={() => openView(project)}
                    className="px-3 py-2 border-2 border-black text-black rounded-lg hover:bg-black hover:text-white transition-all font-semibold text-sm text-center flex items-center justify-center"
                  >
                    {t("project.viewInformation", {
                      defaultValue: "View Information",
                    })}
                  </button>
                )}
                {/* Complete / Reactivate / Edit */}
                {showReactivate && isArchived && (
                  <button
                    type="button"
                    onClick={() => onReactivate?.(project)}
                    className="px-3 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-all font-semibold text-sm text-center flex items-center justify-center"
                  >
                    {t("project.reactivate")}
                  </button>
                )}
                {showComplete && !isArchived && (
                  <button
                    type="button"
                    onClick={() => onComplete?.(project)}
                    className="px-3 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-all font-semibold text-sm text-center flex items-center justify-center"
                  >
                    {t("project.markComplete")}
                  </button>
                )}
                {showEdit && !isArchived && (
                  <button
                    type="button"
                    onClick={() => onEdit?.(project)}
                    className="px-3 py-2 border-2 border-black text-black rounded-lg hover:bg-black hover:text-white transition-all font-semibold text-sm text-center flex items-center justify-center"
                  >
                    {t("edit")}
                  </button>
                )}
                {/* Pin / Unpin Button */}
                <button
                  type="button"
                  onClick={() => handleTogglePin(project)}
                  disabled={pinLoading}
                  className={`px-3 py-2 border-2 border-black rounded-lg transition-all font-semibold text-sm text-center flex items-center justify-center gap-2 ${pinnedProjects.has(project.projectIdentifier)
                    ? "bg-black text-white hover:bg-black/80"
                    : "bg-white text-black hover:bg-black hover:text-white"
                    } disabled:opacity-50`}
                >
                  <svg
                    className="w-4 h-4 flex-shrink-0"
                    fill={pinnedProjects.has(project.projectIdentifier) ? "currentColor" : "none"}
                    stroke="currentColor"
                    strokeWidth="2"
                    viewBox="0 0 24 24"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <line x1="12" y1="17" x2="12" y2="22"></line>
                    <path d="M5 17h14v-1.76a2 2 0 0 0-1.11-1.79l-1.79-.9A.5.5 0 0 1 16 12.1V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v7.1a.5.5 0 0 1-.1.3l-1.79.9A2 2 0 0 0 5 15.24Z"></path>
                  </svg>
                  {pinnedProjects.has(project.projectIdentifier)
                    ? t("project.unpin", { defaultValue: "Unpin" })
                    : t("project.pin", { defaultValue: "Pin" })}
                </button>
                {/* Single Export Button */}
                <button
                  type="button"
                  onClick={() => openExport(project)}
                  className="px-3 py-2 bg-white border-2 border-black text-black rounded-lg hover:bg-black hover:text-white transition-all font-semibold text-sm text-center flex items-center justify-center gap-2"
                  title={t("project.export")}
                >
                  <svg
                    className="w-4 h-4 flex-shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                    />
                  </svg>
                  {t("project.export")}
                </button>
              </div>

              {/* Content area with margin to avoid button overlap on desktop */}
              <div className="sm:mr-[210px] md:mr-[310px] lg:mr-[470px]">
                <div className="flex items-center gap-2 mb-2">
                  <h3 className="text-lg font-semibold">{project.name}</h3>
                  {project.state && (
                    <span
                      className={`text-xs px-2 py-1 rounded font-medium ${isArchived
                        ? "bg-gray-200 text-gray-700"
                        : "bg-green-100 text-green-700"
                        }`}
                    >
                      {isArchived ? t("project.archived") : t("project.active")}
                    </span>
                  )}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                  <p>
                    <strong className="text-black/60">
                      {t("project.id")}:
                    </strong>{" "}
                    <span className="font-mono">
                      {project.projectIdentifier}
                    </span>
                  </p>

                  {project.clientName && (
                    <p>
                      <strong className="text-black/60">
                        {t("project.clientLabel")}:
                      </strong>{" "}
                      <span className="bg-blue-100 px-2 py-1 rounded">
                        {project.clientName}
                      </span>
                    </p>
                  )}

                  <p>
                    <strong className="text-black/60">
                      {t("project.projectType")}:
                    </strong>{" "}
                    <span className="bg-yellow-100 px-2 py-1 rounded">
                      {t(`project.${project.projectType?.toLowerCase() || 'none'}`)}
                    </span>
                  </p>

                  <p className="md:col-span-2 flex items-center gap-2 mt-1">
                    <MapPin className="w-4 h-4 text-black/40" />
                    <span className="text-black/70 font-medium">
                      {formatAddress(project.address) || t("project.none")}
                    </span>
                  </p>

                  <p>
                    <strong className="text-black/60">
                      {t("project.priority")}:
                    </strong>{" "}
                    <span
                      className={`px-2 py-1 rounded ${project.priority === "URGENT"
                        ? "bg-red-100 text-red-800"
                        : project.priority === "HIGH"
                          ? "bg-orange-100 text-orange-800"
                          : project.priority === "LOW"
                            ? "bg-gray-100 text-gray-800"
                            : "bg-blue-100 text-blue-800"
                        }`}
                    >
                      {t(`project.priority${project.priority?.charAt(0).toUpperCase() + project.priority?.slice(1).toLowerCase() || 'Medium'}`)}
                    </span>
                  </p>

                  <p>
                    <strong className="text-black/60">
                      {t("project.estimatedCost")}:
                    </strong>{" "}
                    {estimatedCostFormatted ? (
                      <span>{estimatedCostFormatted}</span>
                    ) : (
                      <span className="text-gray-400">N/A</span>
                    )}
                  </p>

                  <p>
                    <strong className="text-black/60">
                      {t("project.estimatedTime", {
                        defaultValue: "Estimated Time",
                      })}
                      :
                    </strong>{" "}
                    {formatEstimatedTime(project.estimatedTime) ? (
                      <span>{formatEstimatedTime(project.estimatedTime)}</span>
                    ) : (
                      <span className="text-gray-400">N/A</span>
                    )}
                  </p>

                  <p>
                    <strong className="text-black/60">
                      {t("project.startDate")}:
                    </strong>{" "}
                    {project.startDate}
                  </p>

                  <p>
                    <strong className="text-black/60">
                      {t("project.dueDate")}:
                    </strong>{" "}
                    {project.dueDate}
                  </p>

                  {isArchived && project.archivedAt && (
                    <p>
                      <strong className="text-black/60">
                        {t("project.archivedAt")}:
                      </strong>{" "}
                      <span className="text-gray-600">
                        {formatArchivedAt(project.archivedAt, locale)}
                      </span>
                    </p>
                  )}

                  {/* Assigned employees */}
                  {project.assignedEmployeeIds?.length > 0 && (
                    <p className="md:col-span-2">
                      <strong className="text-black/60">
                        {t("project.assignedEmployees")}:
                      </strong>{" "}
                      {formatAssignedEmployees(
                        project.assignedEmployeeIds,
                        project.assignedEmployeeEmails,
                        employeeIndex,
                        t,
                      )}
                    </p>
                  )}

                  {/* Status badge/control */}
                  <p className="md:col-span-2">
                    <strong className="text-black/60">
                      {t("project.status")}:
                    </strong>{" "}
                    {showStatusControl ? (
                      <select
                        value={project.status || "PENDING"}
                        onChange={(e) =>
                          handleStatusChange(project, e.target.value)
                        }
                        className={`px-2 py-1 rounded border-none outline-none ${getStatusBadgeClasses(project.status)}`}
                      >
                        <option value="PENDING">{t("project.pending")}</option>
                        <option value="IN_PROGRESS">
                          {t("project.inProgress")}
                        </option>
                        <option value="COMPLETED">
                          {t("project.completed")}
                        </option>
                      </select>
                    ) : (
                      <span
                        className={`px-2 py-1 rounded ${getStatusBadgeClasses(project.status)}`}
                      >
                        {t(`project.${project.status === 'IN_PROGRESS' ? 'inProgress' : (project.status?.toLowerCase() || 'none')}`)}
                      </span>
                    )}
                  </p>
                </div>

                {showViewInformation && (
                  <p className="mt-3 text-sm text-black/60">
                    {hasInfo
                      ? t("project.informationAvailable", {
                        defaultValue: "Information uploaded.",
                      })
                      : t("project.noInformationYet", {
                        defaultValue: "No information uploaded yet.",
                      })}
                  </p>
                )}
              </div>

              {/* Mobile buttons at bottom */}
              <div className="flex sm:hidden gap-2 flex-wrap mt-4 pt-4 border-t border-black/10">
                {isAdmin && !isArchived && (
                  <button
                    type="button"
                    onClick={() => openPortfolioModal(project)}
                    className="flex-1 min-w-[140px] px-3 py-2 bg-yellow-400 text-black rounded-lg hover:bg-yellow-500 transition-all font-semibold text-sm text-center"
                  >
                    {t("project.sendToPortfolio", {
                      defaultValue: "Send to Portfolio",
                    })}
                  </button>
                )}

                {showUploadInformation && !isArchived && (
                  <button
                    type="button"
                    onClick={() => openUpload(project)}
                    className="flex-1 min-w-[140px] px-3 py-2 bg-black text-white rounded-lg hover:bg-black/80 transition-all font-semibold text-sm text-center"
                  >
                    {t("project.uploadInformation", {
                      defaultValue: "Upload Information",
                    })}
                  </button>
                )}

                {showViewInformation && (
                  <button
                    type="button"
                    onClick={() => openView(project)}
                    className="flex-1 min-w-[140px] px-3 py-2 border-2 border-black text-black rounded-lg hover:bg-black hover:text-white transition-all font-semibold text-sm text-center"
                  >
                    {t("project.viewInformation", {
                      defaultValue: "View Information",
                    })}
                  </button>
                )}

                {showReactivate && isArchived && (
                  <button
                    type="button"
                    onClick={() => onReactivate?.(project)}
                    className="flex-1 min-w-[140px] px-3 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-all font-semibold text-sm text-center"
                  >
                    {t("project.reactivate")}
                  </button>
                )}

                {showComplete && !isArchived && (
                  <button
                    type="button"
                    onClick={() => onComplete?.(project)}
                    className="flex-1 min-w-[140px] px-3 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-all font-semibold text-sm text-center"
                  >
                    {t("project.markComplete")}
                  </button>
                )}

                {showEdit && !isArchived && (
                  <button
                    type="button"
                    onClick={() => onEdit?.(project)}
                    className="flex-1 min-w-[140px] px-3 py-2 border-2 border-black text-black rounded-lg hover:bg-black hover:text-white transition-all font-semibold text-sm text-center"
                  >
                    {t("edit")}
                  </button>
                )}

                {/* Pin Button (Mobile) */}
                <button
                  type="button"
                  onClick={() => handleTogglePin(project)}
                  disabled={pinLoading}
                  className={`flex-1 min-w-[140px] px-3 py-2 border-2 border-black rounded-lg transition-all font-semibold text-sm text-center flex items-center justify-center gap-2 ${pinnedProjects.has(project.projectIdentifier)
                    ? "bg-black text-white hover:bg-black/80"
                    : "bg-white text-black hover:bg-black hover:text-white"
                    } disabled:opacity-50`}
                >
                  <svg
                    className="w-4 h-4 flex-shrink-0"
                    fill={pinnedProjects.has(project.projectIdentifier) ? "currentColor" : "none"}
                    stroke="currentColor"
                    strokeWidth="2"
                    viewBox="0 0 24 24"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <line x1="12" y1="17" x2="12" y2="22"></line>
                    <path d="M5 17h14v-1.76a2 2 0 0 0-1.11-1.79l-1.79-.9A.5.5 0 0 1 16 12.1V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v7.1a.5.5 0 0 1-.1.3l-1.79.9A2 2 0 0 0 5 15.24Z"></path>
                  </svg>
                  {pinnedProjects.has(project.projectIdentifier)
                    ? t("project.unpin", { defaultValue: "Unpin" })
                    : t("project.pin", { defaultValue: "Pin" })}
                </button>

                {/* Single Export Button (Mobile) */}
                <button
                  type="button"
                  onClick={() => openExport(project)}
                  className="flex-1 min-w-[140px] px-3 py-2 bg-white border-2 border-black text-black rounded-lg hover:bg-black hover:text-white transition-all font-semibold text-sm flex items-center justify-center gap-2"
                >
                  <svg
                    className="w-4 h-4"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                    />
                  </svg>
                  {t("project.export")}
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {uploadOpen && (
        <ModalShell
          title={t("project.uploadInformation", {
            defaultValue: "Upload Information",
          })}
          onClose={() => setUploadOpen(false)}
        >
          <div className="space-y-4">
            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="block text-sm font-semibold text-black/60">
                  {t("project.comments", { defaultValue: "Comments" })}
                </label>
                <span className={`text-xs font-medium ${comments.trim().split(/\s+/).filter(w => w).length > 200 ? "text-red-600" : "text-black/40"}`}>
                  {comments.trim().split(/\s+/).filter(w => w).length} / 200 words
                </span>
              </div>
              <textarea
                value={comments}
                onChange={(e) => {
                  const text = e.target.value;
                  const wordCount = text.trim().split(/\s+/).filter(w => w).length;
                  if (wordCount <= 200) {
                    setComments(text);
                  }
                }}
                className="w-full min-h-[110px] px-3 py-2 border-2 border-black/20 rounded-lg"
                placeholder={t("project.commentsPlaceholder", {
                  defaultValue: "Write what you did today...",
                })}
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-black/60 mb-2">
                {t("project.choosePhotoOptional", {
                  defaultValue: "Choose a photo (optional)",
                })}
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
                  {t("project.choosePhotoOptional", {
                    defaultValue: "Choose a photo (optional)",
                  })}
                </label>

                <span className="text-sm text-black/60">
                  {file
                    ? file.name
                    : t("project.noFileChosen", {
                      defaultValue: "No file chosen",
                    })}
                </span>
              </div>
            </div>

            <button
              type="button"
              onClick={submitUpload}
              disabled={(!file && !comments.trim()) || uploading}
              className={`w-full px-4 py-2 rounded-lg font-semibold ${(!file && !comments.trim()) || uploading
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
          title={t("project.viewInformation", {
            defaultValue: "View Information",
          })}
          onClose={() => setViewOpen(false)}
        >
          {!latestInfo ? (
            <p className="text-black/60">
              {t("project.noInformationYet", {
                defaultValue: "No information uploaded yet.",
              })}
            </p>
          ) : (
            <div className="space-y-4">
              <div className="border border-black/10 rounded-lg p-3 bg-gray-50">
                <p className="text-sm font-semibold mb-2">
                  {t("project.workProof", { defaultValue: "Work proof" })}
                </p>

                {imageLoading && (
                  <p className="text-black/60">
                    {t("project.loadingImage", {
                      defaultValue: "Loading image...",
                    })}
                  </p>
                )}

                {imageError && (
                  <p className="text-sm text-red-600">{imageError}</p>
                )}

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

      {exportOpen && activeProject && (
        <ModalShell
          title={t("project.export")}
          onClose={() => setExportOpen(false)}
        >
          <div className="flex flex-col gap-4">
            <div className="text-center mb-2">
              <p className="text-black/60 text-sm uppercase tracking-widest font-bold opacity-70">
                {t("project.exportFormatDescFull", {
                  defaultValue: "Select Format For",
                })}
              </p>
              <h4 className="text-xl font-light text-black mt-1">
                {activeProject.name}
              </h4>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <button
                type="button"
                onClick={() => {
                  generateCsv(
                    [activeProject],
                    `project_${activeProject.projectIdentifier}.csv`,
                    {
                      locale: i18n.language === "fr" ? "fr-CA" : "en-CA",
                    },
                  );
                }}
                className="flex flex-col items-center justify-center p-6 border-2 border-green-600 text-green-700 rounded-xl hover:bg-green-50 transition-all group shadow-sm hover:shadow-md"
              >
                <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                  <svg
                    className="w-6 h-6"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                    />
                  </svg>
                </div>
                <span className="font-bold">CSV</span>
                <span className="text-[10px] uppercase tracking-widest mt-1 opacity-60">
                  Spreadsheet
                </span>
              </button>

              <button
                type="button"
                onClick={() => {
                  const langSuffix = i18n.language === "fr" ? "-fr" : "-en";
                  generatePdf(
                    [activeProject],
                    `project_${activeProject.projectIdentifier}${langSuffix}.pdf`,
                    {
                      exporterName: user?.name || user?.email || "Staff",
                      title: t("project.singleReport", {
                        name: activeProject.name,
                      }),
                      locale: i18n.language === "fr" ? "fr-CA" : "en-CA",
                    },
                  );
                }}
                className="flex flex-col items-center justify-center p-6 border-2 border-red-600 text-red-700 rounded-xl hover:bg-red-50 transition-all group shadow-sm hover:shadow-md"
              >
                <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                  <svg
                    className="w-6 h-6"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"
                    />
                  </svg>
                </div>
                <span className="font-bold">PDF</span>
                <span className="text-[10px] uppercase tracking-widest mt-1 opacity-60">
                  Document
                </span>
              </button>
            </div>
          </div>
        </ModalShell>
      )}

      {/* Send to Portfolio Modal */}
      <SendToPortfolioModal
        project={activeProject}
        isOpen={portfolioModalOpen}
        onClose={() => setPortfolioModalOpen(false)}
        onSuccess={handlePortfolioSuccess}
        getToken={getToken}
      />

      {/* Status Transition Warning Modal */}
      {statusWarning && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <div className="bg-white w-full max-w-sm rounded-xl border-2 border-yellow-500 shadow-xl">
            <div className="flex items-center justify-between p-4 border-b border-yellow-200 bg-yellow-50">
              <div className="flex items-center gap-2">
                <svg
                  className="w-6 h-6 text-yellow-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 9v2m0 4v2m0-6a9 9 0 110 18 9 9 0 010-18z"
                  />
                </svg>
                <h3 className="text-lg font-bold text-yellow-900">
                  {t("project.invalidStatusTransition", {
                    defaultValue: "Invalid Status Change",
                  })}
                </h3>
              </div>
              <button
                type="button"
                onClick={cancelStatusChange}
                className="text-xl font-bold leading-none px-2 hover:text-black/70"
                aria-label="Close"
              >
                ×
              </button>
            </div>
            <div className="p-4">
              <p className="text-black/70 mb-6">{statusWarning.message}</p>
              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={cancelStatusChange}
                  className="flex-1 px-4 py-2 border-2 border-black text-black rounded-lg hover:bg-black/5 transition-all font-semibold"
                >
                  {t("common.dismiss", { defaultValue: "Dismiss" })}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default ProjectList;
