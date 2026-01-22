// ProjectList.jsx
import { useTranslation } from "react-i18next";

const formatAssignedEmployees = (assignedEmployeeIds, employeeIndex, t) => {
  if (!assignedEmployeeIds || assignedEmployeeIds.length === 0) {
    return t("project.none");
  }

  return assignedEmployeeIds
    .map((id) => {
      const emp = employeeIndex?.[id];
      if (!emp) return id;
      return emp.email || emp.name || id;
    })
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

  // NEW (only adds what’s needed)
  showUploadPhoto = false,
  onUploadPhoto,
}) => {
  const { t, i18n } = useTranslation();
  const locale = i18n.language === "fr" ? "fr-CA" : "en-CA";

  const openFilePicker = (projectIdentifier) => {
    if (!projectIdentifier) return;
    const el = document.getElementById(`upload-${projectIdentifier}`);
    el?.click();
  };

  return (
    <div className="border-2 border-black rounded-xl bg-white p-4 max-h-[400px] overflow-y-auto space-y-4">
      {projects.length === 0 && (
        <p className="text-black/60 text-center py-8">
          {t("project.noProjectsFound")}
        </p>
      )}

      {projects.map((project) => {
        const isArchived = project.state === "COMPLETE";

        return (
          <div
            key={project.projectIdentifier}
            className={`border border-black/10 rounded-lg p-4 hover:shadow-md transition-shadow relative ${
              isArchived ? "bg-gray-50 opacity-75" : ""
            }`}
          >
            <div className="absolute right-4 top-4 flex gap-2">
              {showReactivate && isArchived && (
                <button
                  type="button"
                  onClick={() => onReactivate?.(project)}
                  className="px-5 py-2 bg-blue-500 text-white rounded-lg 
                             hover:bg-blue-600 transition-all font-semibold"
                >
                  {t("project.reactivate")}
                </button>
              )}
              {showComplete && !isArchived && (
                <button
                  type="button"
                  onClick={() => onComplete?.(project)}
                  className="px-5 py-2 bg-green-500 text-white rounded-lg 
                             hover:bg-green-600 transition-all font-semibold"
                >
                  {t("project.markComplete")}
                </button>
              )}
              {showEdit && !isArchived && (
                <button
                  type="button"
                  onClick={() => onEdit?.(project)}
                  className="px-5 py-2 border-2 border-black 
                             text-black rounded-lg hover:bg-black hover:text-white 
                             transition-all font-semibold"
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
                    isArchived
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
                <strong className="text-black/60">{t("project.id")}:</strong>{" "}
                <span className="font-mono">{project.projectIdentifier}</span>
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
                  {project.projectType}
                </span>
              </p>

              <p>
                <strong className="text-black/60">
                  {t("project.startDate")}:
                </strong>{" "}
                {project.startDate}
              </p>

              <p>
                <strong className="text-black/60">{t("project.dueDate")}:</strong>{" "}
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
            </div>

            {project.description && (
              <p className="mt-2 text-sm">
                <strong className="text-black/60">
                  {t("project.description")}:
                </strong>{" "}
                {project.description}
              </p>
            )}

            {project.address && (
              <p className="mt-2 text-sm">
                <strong className="text-black/60">
                  {t("project.addressLabel")}:
                </strong>{" "}
                {project.address.streetAddress}, {project.address.city},{" "}
                {project.address.province}, {project.address.country}{" "}
                {project.address.postalCode}
              </p>
            )}

            <p>
              <strong className="text-black/60">{t("project.status")}:</strong>{" "}
              <span
                className={`px-2 py-1 rounded ${
                  (project.status || "PENDING") === "COMPLETED"
                    ? "bg-green-100 text-green-800"
                    : (project.status || "PENDING") === "IN_PROGRESS"
                    ? "bg-blue-100 text-blue-800"
                    : "bg-yellow-100 text-yellow-800"
                }`}
              >
                {project.status === "COMPLETED"
                  ? t("project.completed")
                  : project.status === "IN_PROGRESS"
                  ? t("project.inProgress")
                  : t("project.pending")}
              </span>
            </p>

            {showStatusControl && !isArchived && (
              <div className="mt-3">
                <label className="block text-sm font-semibold text-black/60 mb-1">
                  {t("project.updateStatus")}
                </label>

                <select
                  value={project.status || "PENDING"}
                  onChange={(e) => onUpdateStatus?.(project, e.target.value)}
                  className="w-full px-3 py-2 border-2 border-black/20 rounded-lg bg-white"
                >
                  <option value="PENDING">{t("project.pending")}</option>
                  <option value="IN_PROGRESS">{t("project.inProgress")}</option>
                  <option value="COMPLETED">{t("project.completed")}</option>
                </select>
              </div>
            )}

            {project.assignedEmployeeIds && project.assignedEmployeeIds.length > 0 && (
              <p className="mt-2 text-sm">
                <strong className="text-black/60">
                  {t("project.assignedEmployees")}:
                </strong>{" "}
                {formatAssignedEmployees(project.assignedEmployeeIds, employeeIndex, t)}
              </p>
            )}

            {project.photos?.length > 0 && (
              <p className="mt-2 text-sm">
                <strong className="text-black/60">{t("project.photos")}:</strong>{" "}
                {project.photos.length}
              </p>
            )}

            {/* NEW: Upload latest photo (only adds what’s needed) */}
            {showUploadPhoto && !isArchived && (
              <div className="mt-3">
                <input
                  id={`upload-${project.projectIdentifier}`}
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={(e) => {
                    const file = e.target.files?.[0];
                    if (file) onUploadPhoto?.(project, file);
                    e.target.value = "";
                  }}
                />
                <button
                  type="button"
                  onClick={() => openFilePicker(project.projectIdentifier)}
                  className="px-5 py-2 bg-yellow-400 text-black rounded-lg hover:bg-yellow-500 transition-all font-semibold"
                >
                  {t("project.uploadPhoto")}
                </button>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
};

export default ProjectList;
