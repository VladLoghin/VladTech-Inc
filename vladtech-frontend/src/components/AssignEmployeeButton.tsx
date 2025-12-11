import { useAuth0 } from "@auth0/auth0-react";
import axios from "axios";
import { useState } from "react";

interface AssignEmployeeButtonProps {
  projectIdentifier: string;
  onAssigned: () => void;
}

interface EmployeeSummary {
  userId: string;
  name: string;
  email: string;
}

const AssignEmployeeButton: React.FC<AssignEmployeeButtonProps> = ({
  projectIdentifier,
  onAssigned,
}) => {
  const { getAccessTokenSilently } = useAuth0();

  const [isOpen, setIsOpen] = useState(false);
  const [employees, setEmployees] = useState<EmployeeSummary[]>([]);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState("");
  const [loadingEmployees, setLoadingEmployees] = useState(false);
  const [assigning, setAssigning] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const openModal = async () => {
    setIsOpen(true);
    setError(null);
    setSelectedEmployeeId("");

    try {
      setLoadingEmployees(true);
      const token = await getAccessTokenSilently();
      const res = await axios.get<EmployeeSummary[]>(
        "http://localhost:8080/api/employee/list",
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      setEmployees(res.data);
    } catch (err) {
      console.error("Error fetching employees", err);
      setError("Failed to load employees from Auth0.");
    } finally {
      setLoadingEmployees(false);
    }
  };

  const closeModal = () => {
    setIsOpen(false);
    setError(null);
  };

  const handleAssign = async () => {
    if (!selectedEmployeeId) {
      setError("Please select an employee.");
      return;
    }

    try {
      setAssigning(true);
      setError(null);
      const token = await getAccessTokenSilently();

      await axios.post(
        `http://localhost:8080/api/projects/${projectIdentifier}/assign/${selectedEmployeeId}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      onAssigned();
      closeModal();
    } catch (err: any) {
      console.error("Error assigning employee", err);

      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Failed to assign employee.";

      setError(msg);
    } finally {
      setAssigning(false);
    }
  };

  return (
    <>
      <button
        onClick={openModal}
        className="bg-yellow-400 hover:bg-yellow-500 text-black px-4 py-2 rounded-lg text-sm font-semibold shadow"
      >
        Assign
      </button>

      {isOpen && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-lg p-6 w-full max-w-md">
            <h3 className="text-xl font-bold mb-4">
              Assign employee to {projectIdentifier}
            </h3>

            {loadingEmployees ? (
              <p className="text-sm text-black/60">Loading employees…</p>
            ) : employees.length === 0 ? (
              <p className="text-sm text-black/60">
                No employees found in Auth0 with the Employee role.
              </p>
            ) : (
              <div className="mb-4">
                <label className="block text-sm font-semibold mb-1">
                  Select Employee
                </label>
                <select
                  value={selectedEmployeeId}
                  onChange={(e) => setSelectedEmployeeId(e.target.value)}
                  className="w-full border border-black/20 rounded-lg px-3 py-2 text-sm"
                >
                  <option value="">Choose an employee…</option>
                  {employees.map((emp) => (
                    <option key={emp.userId} value={emp.userId}>
                      {emp.name || emp.email} ({emp.email})
                    </option>
                  ))}
                </select>
              </div>
            )}

            {error && (
              <p className="text-sm text-red-600 mb-3">
                {error}
              </p>
            )}

            <div className="flex justify-end gap-3 mt-4">
              <button
                onClick={closeModal}
                className="px-4 py-2 rounded-lg border border-black/20 text-sm"
                disabled={assigning}
              >
                Cancel
              </button>
              <button
                onClick={handleAssign}
                className="px-4 py-2 rounded-lg bg-yellow-400 hover:bg-yellow-500 text-sm font-semibold"
                disabled={assigning || employees.length === 0}
              >
                {assigning ? "Assigning…" : "Confirm"}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default AssignEmployeeButton;