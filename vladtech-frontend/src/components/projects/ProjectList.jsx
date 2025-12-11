import UpdateProjectModal from "./UpdateProjectModal.jsx";

const ProjectList = ({ projects }) => {
  return (
    <div className="border-2 border-black rounded-xl bg-white p-4 max-h-[400px] overflow-y-auto space-y-4">
      {projects.length === 0 && (
        <p className="text-black/60 text-center py-8">
          No projects found.
        </p>
      )}

      {projects.map((project) => (
        <div
          key={project.projectIdentifier}
          className="border border-black/10 rounded-lg p-4 hover:shadow-md transition-shadow"
        >
          <button
            type="button"
            onClick={() => { }}
            className="float-right px-8 py-3 border-2 border-black text-black rounded-lg hover:bg-black hover:text-white transition-all font-semibold"
          >
            Edit
          </button>

          <h3 className="text-lg font-semibold mb-2">{project.name}</h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
            <p>
              <strong className="text-black/60">ID:</strong>{" "}
              <span className="font-mono">
                {project.projectIdentifier}
              </span>
            </p>
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

          {project.assignedEmployeeIds &&
            project.assignedEmployeeIds.length > 0 && (
              <p className="mt-2 text-sm">
                <strong className="text-black/60">
                  Assigned Employees:
                </strong>{" "}
                {project.assignedEmployeeIds.join(", ")}
              </p>
            )}

          {project.photos && project.photos.length > 0 && (
            <p className="mt-2 text-sm">
              <strong className="text-black/60">Photos:</strong>{" "}
              {project.photos.length}
            </p>
          )}


        </div>
      ))}
    </div>
  );
};

export default ProjectList;
