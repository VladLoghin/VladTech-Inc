import { useState, useEffect } from "react";
import { X, ChevronLeft, ChevronRight, Search, UserPlus, UserMinus, Settings } from "lucide-react";
import { api } from "../../api/http";
import { useAuth0 } from "@auth0/auth0-react";

const RoleManagerModal = ({ isOpen, onClose, onSuccess }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [users, setUsers] = useState([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalUsers, setTotalUsers] = useState(0);
    const [loading, setLoading] = useState(false);
    const [actionLoading, setActionLoading] = useState(null);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [selectedRole, setSelectedRole] = useState("employee");
    const [mode, setMode] = useState("assign"); // "assign" or "remove"
    const [searchQuery, setSearchQuery] = useState("");
    const [activeQuery, setActiveQuery] = useState("");

    const perPage = 25;

    const roleConfig = {
        client: {
            label: "Client",
            assignEndpoint: "/role-assignment/users/{userId}/roles/client",
            removeEndpoint: "/role-assignment/users/{userId}/roles/client",
            bgColor: "#4ade80",
        },
        employee: {
            label: "Employee",
            assignEndpoint: "/role-assignment/users/{userId}/roles/employee",
            removeEndpoint: "/role-assignment/users/{userId}/roles/employee",
            bgColor: "#4ade80",
        },
        admin: {
            label: "Admin",
            assignEndpoint: "/role-assignment/users/{userId}/roles/admin",
            removeEndpoint: "/role-assignment/users/{userId}/roles/admin",
            bgColor: "#4ade80",
        },
    };

    const fetchUsers = async (page, query = "") => {
        setLoading(true);
        setError("");

        try {
            const token = await getAccessTokenSilently({
                authorizationParams: { audience: "https://vladtech/api" },
            });

            const headers = { Authorization: `Bearer ${token}` };

            if (mode === "remove") {
                // For remove mode: get users who HAVE this role
                const response = await api.get(`/users/${selectedRole}s`, {
                    headers,
                    params: { page, perPage },
                });
                setUsers(response.data.users || []);
                setTotalUsers(response.data.total || 0);
            } else {
                // For assign mode: search or get all users, then filter out those with role
                if (query.trim()) {
                    const response = await api.get("/users/search", {
                        headers,
                        params: { query, page, perPage },
                    });
                    setUsers(response.data.users || []);
                    setTotalUsers(response.data.total || 0);
                } else {
                    // Get all users and filter out those who already have the role
                    const allUsersResponse = await api.get("/users/search", {
                        headers,
                        params: { query: "*", page, perPage },
                    });

                    const roleUsersResponse = await api.get(`/users/${selectedRole}s`, {
                        headers,
                        params: { page: 0, perPage: 100 },
                    });

                    const roleUserIds = new Set(
                        (roleUsersResponse.data.users || []).map((u) => u.user_id)
                    );

                    const filteredUsers = (allUsersResponse.data.users || []).filter(
                        (u) => !roleUserIds.has(u.user_id)
                    );

                    setUsers(filteredUsers);
                    setTotalUsers(filteredUsers.length);
                }
            }
        } catch (err) {
            console.error("Error fetching users:", err);
            setError("Failed to load users");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (isOpen) {
            setCurrentPage(0);
            setActiveQuery("");
            setSearchQuery("");
            setSuccess("");
            setError("");
            fetchUsers(0);
        }
    }, [isOpen, selectedRole, mode]);

    useEffect(() => {
        if (isOpen && currentPage > 0) {
            fetchUsers(currentPage, activeQuery);
        }
    }, [currentPage]);

    const totalPages = Math.ceil(totalUsers / perPage);

    const handleSearch = (e) => {
        e.preventDefault();
        setCurrentPage(0);
        setActiveQuery(searchQuery);
        fetchUsers(0, searchQuery);
    };

    const handleClearSearch = () => {
        setSearchQuery("");
        setActiveQuery("");
        setCurrentPage(0);
        fetchUsers(0);
    };

    const handleRoleAction = async (userId, userName) => {
        setActionLoading(userId);
        setError("");
        setSuccess("");

        try {
            const token = await getAccessTokenSilently({
                authorizationParams: { audience: "https://vladtech/api" },
            });

            const config = roleConfig[selectedRole];
            const endpoint = mode === "assign"
                ? config.assignEndpoint.replace("{userId}", encodeURIComponent(userId))
                : config.removeEndpoint.replace("{userId}", encodeURIComponent(userId));

            if (mode === "assign") {
                await api.patch(endpoint, {}, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setSuccess(`${config.label} role assigned to ${userName || userId}`);
            } else {
                await api.delete(endpoint, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                setSuccess(`${config.label} role removed from ${userName || userId}`);
            }

            // Remove user from list
            setUsers(users.filter((u) => u.user_id !== userId));
            setTotalUsers((prev) => prev - 1);

            if (onSuccess) {
                onSuccess();
            }
        } catch (err) {
            console.error(`Error ${mode}ing role:`, err);
            setError(`Failed to ${mode} ${roleConfig[selectedRole].label} role`);
        } finally {
            setActionLoading(null);
        }
    };

    const getRoleButtonColor = (role) => {
        const isSelected = selectedRole === role;
        return isSelected ? "bg-green-400" : "bg-black/5 hover:bg-black/10";
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl shadow-2xl max-w-5xl w-full max-h-[90vh] flex flex-col">
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-black/10">
                    <div>
                        <h2 className="text-2xl font-bold tracking-tight flex items-center gap-2">
                            <Settings className="h-6 w-6" />
                            Role Manager
                        </h2>
                        <p className="text-sm text-black/60 mt-1">
                            {totalUsers} users {mode === "assign" ? "available for assignment" : `with ${roleConfig[selectedRole].label} role`}
                        </p>
                    </div>
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-black/5 rounded-lg transition-colors"
                    >
                        <X className="h-6 w-6" />
                    </button>
                </div>

                {/* Mode Toggle and Role Selection */}
                <div className="p-6 border-b border-black/10 space-y-4">
                    {/* Mode Toggle */}
                    <div>
                        <p className="text-sm font-medium text-black/60 mb-2">Action:</p>
                        <div className="flex border-2 border-black rounded-lg overflow-hidden w-fit">
                            <button
                                onClick={() => setMode("assign")}
                                className={`px-6 py-2 font-semibold transition-all flex items-center gap-2 ${mode === "assign"
                                    ? "bg-black text-white"
                                    : "bg-white text-black hover:bg-gray-100"
                                    }`}
                            >
                                <UserPlus className="h-4 w-4" />
                                Assign Role
                            </button>
                            <button
                                onClick={() => setMode("remove")}
                                className={`px-6 py-2 font-semibold transition-all flex items-center gap-2 ${mode === "remove"
                                    ? "bg-black text-white"
                                    : "bg-white text-black hover:bg-gray-100"
                                    }`}
                            >
                                <UserMinus className="h-4 w-4" />
                                Remove Role
                            </button>
                        </div>
                    </div>

                    {/* Role Selection */}
                    <div>
                        <p className="text-sm font-medium text-black/60 mb-2">Select role:</p>
                        <div className="flex gap-2">
                            <button
                                onClick={() => setSelectedRole("client")}
                                className={`px-4 py-2 rounded-lg transition-all font-medium ${getRoleButtonColor("client")}`}
                            >
                                Client
                            </button>
                            <button
                                onClick={() => setSelectedRole("employee")}
                                className={`px-4 py-2 rounded-lg transition-all font-medium ${getRoleButtonColor("employee")}`}
                            >
                                Employee
                            </button>
                            <button
                                onClick={() => setSelectedRole("admin")}
                                className={`px-4 py-2 rounded-lg transition-all font-medium ${getRoleButtonColor("admin")}`}
                            >
                                Admin
                            </button>
                        </div>
                    </div>

                    {/* Search (only for assign mode) */}
                    {mode === "assign" && (
                        <form onSubmit={handleSearch} className="flex gap-2">
                            <div className="flex-1 relative">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-black/40" />
                                <input
                                    type="text"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    placeholder="Search users by email, name, or ID..."
                                    className="w-full pl-10 pr-4 py-2 border border-black/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400"
                                />
                            </div>
                            <button
                                type="submit"
                                className="px-6 py-2 bg-yellow-400 hover:bg-yellow-500 rounded-lg transition-colors font-semibold"
                            >
                                Search
                            </button>
                            {activeQuery && (
                                <button
                                    type="button"
                                    onClick={handleClearSearch}
                                    className="px-4 py-2 border border-black/20 hover:bg-black/5 rounded-lg transition-colors"
                                >
                                    Clear
                                </button>
                            )}
                        </form>
                    )}
                </div>

                {/* Success/Error Messages */}
                {(success || error) && (
                    <div className="px-6 pt-4">
                        {success && (
                            <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded-lg flex items-center justify-between">
                                <span>{success}</span>
                                <button onClick={() => setSuccess("")} className="text-green-700 hover:text-green-900">
                                    <X className="h-4 w-4" />
                                </button>
                            </div>
                        )}
                        {error && (
                            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg flex items-center justify-between">
                                <span>{error}</span>
                                <button onClick={() => setError("")} className="text-red-700 hover:text-red-900">
                                    <X className="h-4 w-4" />
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {/* User List */}
                <div className="flex-1 overflow-y-auto p-6">
                    {loading ? (
                        <div className="flex items-center justify-center py-12">
                            <div className="animate-spin rounded-full h-12 w-12 border-4 border-yellow-400 border-t-transparent"></div>
                        </div>
                    ) : users.length === 0 ? (
                        <div className="text-center py-12 text-black/60">
                            {mode === "assign"
                                ? activeQuery
                                    ? "No users found matching your search"
                                    : `All users already have the ${roleConfig[selectedRole].label} role`
                                : `No users have the ${roleConfig[selectedRole].label} role`}
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {users.map((user, index) => (
                                <div
                                    key={user.user_id || index}
                                    className="border border-black/10 rounded-lg p-4 hover:bg-black/5 transition-colors flex items-center justify-between"
                                >
                                    <div className="flex items-center gap-4">
                                        {user.picture && (
                                            <img
                                                src={user.picture}
                                                alt={user.name}
                                                className="w-12 h-12 rounded-full"
                                            />
                                        )}
                                        <div>
                                            <p className="font-semibold text-lg">
                                                {user.name || "No name"}
                                            </p>
                                            <p className="text-sm text-black/60">{user.email}</p>
                                            <p className="text-xs text-black/40 mt-1">
                                                ID: {encodeURIComponent(user.user_id)}
                                            </p>
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => handleRoleAction(user.user_id, user.name)}
                                        disabled={actionLoading === user.user_id}
                                        className={`px-4 py-2 rounded-lg font-semibold transition-all flex items-center gap-2 ${actionLoading === user.user_id
                                            ? "bg-gray-300 cursor-not-allowed"
                                            : mode === "remove"
                                                ? "bg-red-500 hover:bg-red-600 text-white"
                                                : "text-black"
                                            }`}
                                        style={{
                                            backgroundColor: actionLoading === user.user_id
                                                ? undefined
                                                : mode === "remove"
                                                    ? undefined
                                                    : roleConfig[selectedRole].bgColor
                                        }}
                                    >
                                        {actionLoading === user.user_id ? (
                                            <>
                                                <div className="animate-spin rounded-full h-4 w-4 border-2 border-black border-t-transparent"></div>
                                                {mode === "assign" ? "Assigning..." : "Removing..."}
                                            </>
                                        ) : mode === "assign" ? (
                                            <>
                                                <UserPlus className="h-4 w-4" />
                                                Assign {roleConfig[selectedRole].label}
                                            </>
                                        ) : (
                                            <>
                                                <UserMinus className="h-4 w-4" />
                                                Remove {roleConfig[selectedRole].label}
                                            </>
                                        )}
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Pagination */}
                <div className="border-t border-black/10 p-6 flex items-center justify-between">
                    <button
                        onClick={() => setCurrentPage(currentPage - 1)}
                        disabled={currentPage === 0 || loading}
                        className="flex items-center gap-2 px-4 py-2 border border-black/20 rounded-lg hover:bg-black/5 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        <ChevronLeft className="h-4 w-4" />
                        Previous
                    </button>

                    <div className="text-sm text-black/60">
                        Page {currentPage + 1} of {totalPages || 1}
                    </div>

                    <button
                        onClick={() => setCurrentPage(currentPage + 1)}
                        disabled={currentPage >= totalPages - 1 || loading}
                        className="flex items-center gap-2 px-4 py-2 border border-black/20 rounded-lg hover:bg-black/5 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        Next
                        <ChevronRight className="h-4 w-4" />
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RoleManagerModal;
