const formatAssignedEmployees = (assignedEmployeeIds, employeeIndex) => {
  if (!assignedEmployeeIds || assignedEmployeeIds.length === 0) {
    return "None";
  }

  return assignedEmployeeIds
    .map((id) => {
      const emp = employeeIndex?.[id];
      if (!emp) {
        return id;
      }
      return emp.email || emp.name || id;
    })
    .join(", ");
};

const formatArchivedAt = (archivedAt) => {
  if (!archivedAt) return null;
  const date = new Date(archivedAt);
  return date.toLocaleString("en-US", {
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
}) => {

  return (
    <div className="border-2 border-black rounded-xl bg-white p-4 max-h-[400px] overflow-y-auto space-y-4">
      {projects.length === 0 && (
        <p className="text-black/60 text-center py-8">No projects found.</p>
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
                  Reactivate
                </button>
              )}
              {showComplete && !isArchived && (
                <button
                  type="button"
                  onClick={() => onComplete?.(project)}
                  className="px-5 py-2 bg-green-500 text-white rounded-lg 
                             hover:bg-green-600 transition-all font-semibold"
                >
                  Mark Complete
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
                  Edit
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
                  {isArchived ? "ARCHIVED" : "ACTIVE"}
                </span>
              )}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
              <p>
                <strong className="text-black/60">ID:</strong>{" "}
                <span className="font-mono">{project.projectIdentifier}</span>
              </p>

              {project.clientName && (
                <p>
                  <strong className="text-black/60">Client:</strong>{" "}
                  <span className="bg-blue-100 px-2 py-1 rounded">
                    {project.clientName}
                  </span>
                </p>
              )}

              <p>
                <strong className="text-black/60">Type:</strong>{" "}
                <span className="bg-yellow-100 px-2 py-1 rounded">
                  {project.projectType}
                </span>
              </p>

              <p>
                <strong className="text-black/60">Start Date:</strong>{" "}
                {project.startDate}
              </p>

              <p>
                <strong className="text-black/60">Due Date:</strong>{" "}
                {project.dueDate}
              </p>

              {isArchived && project.archivedAt && (
                <p>
                  <strong className="text-black/60">Archived:</strong>{" "}
                  <span className="text-gray-600">
                    {formatArchivedAt(project.archivedAt)}
                  </span>
                </p>
              )}
            </div>

            {project.description && (
              <p className="mt-2 text-sm">
                <strong className="text-black/60">Description:</strong>{" "}
                {project.description}
              </p>
            )}

            {project.address && (
              <p className="mt-2 text-sm">
                <strong className="text-black/60">Address:</strong>{" "}
                {project.address.streetAddress}, {project.address.city},{" "}
                {project.address.province}, {project.address.country}{" "}
                {project.address.postalCode}
              </p>
            )}

            <p>
              <strong className="text-black/60">Status:</strong>{" "}
              <span
                className={`px-2 py-1 rounded ${
                  (project.status || "PENDING") === "COMPLETED"
                    ? "bg-green-100 text-green-800"
                    : (project.status || "PENDING") === "IN_PROGRESS"
                    ? "bg-blue-100 text-blue-800"
                    : "bg-yellow-100 text-yellow-800"
                }`}
              >
                {project.status || "PENDING"}
              </span>
            </p>

            {showStatusControl && !isArchived && (
  <div className="mt-3">
    <label className="block text-sm font-semibold text-black/60 mb-1">
      Update Status
    </label>

    <select
      value={project.status || "PENDING"}
      onChange={(e) => onUpdateStatus?.(project, e.target.value)}
      className="w-full px-3 py-2 border-2 border-black/20 rounded-lg bg-white"
    >
      <option value="PENDING">PENDING</option>
      <option value="IN_PROGRESS">IN_PROGRESS</option>
      <option value="COMPLETED">COMPLETED</option>
    </select>
  </div>
)}


            {project.assignedEmployeeIds &&
              project.assignedEmployeeIds.length > 0 && (
                <p className="mt-2 text-sm">
                  <strong className="text-black/60">Assigned Employees:</strong>{" "}
                  {formatAssignedEmployees(
                    project.assignedEmployeeIds,
                    employeeIndex
                  )}
                </p>
              )}
            {project.photos?.length > 0 && (
              <p className="mt-2 text-sm">
                <strong className="text-black/60">Photos:</strong>{" "}
                {project.photos.length}
              </p>
            )}
          </div>
        );
      })}
    </div>
  );
};

export default ProjectList;
