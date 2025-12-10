const ProjectList = ({ projects }) => {
  return (
    <div className="space-y-6">
      {projects.map((project) => (
        <div
          key={project.projectIdentifier}
          className="border-2 border-black rounded-xl p-6 bg-white shadow-md hover:shadow-xl transition-shadow"
        >
          <h3 className="text-2xl font-bold mb-4 text-black">
            {project.name}
          </h3>

          <div className="grid grid-cols-2 gap-4 text-sm">
            <p>
              <strong className="text-black/60">ID:</strong>{" "}
              <span className="font-mono">{project.projectIdentifier}</span>
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
            <p className="mt-4">
              <strong className="text-black/60">Description:</strong>{" "}
              {project.description}
            </p>
          )}

          {project.address && (
            <p className="mt-2">
              <strong className="text-black/60">Address:</strong>{" "}
              {project.address.streetAddress}, {project.address.city},{" "}
              {project.address.province}, {project.address.country}{" "}
              {project.address.postalCode}
            </p>
          )}

          {project.assignedEmployeeIds?.length > 0 && (
            <p className="mt-2">
              <strong className="text-black/60">Assigned Employees:</strong>{" "}
              {project.assignedEmployeeIds.join(", ")}
            </p>
          )}

          {project.photos?.length > 0 && (
            <p className="mt-2">
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
