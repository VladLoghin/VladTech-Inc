import { useState, useEffect } from "react";
import { X, ChevronLeft, ChevronRight, Search, UserPlus, UserMinus, Settings, History } from "lucide-react";
import { api } from "../../api/http";
import { useAuth0 } from "@auth0/auth0-react";
import { useTranslation } from "react-i18next";

const RoleManagerModal = ({ isOpen, onClose, onSuccess }) => {
    const { getAccessTokenSilently } = useAuth0();
    const { t } = useTranslation();
    const [users, setUsers] = useState([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalUsers, setTotalUsers] = useState(0);
    const [loading, setLoading] = useState(false);
    const [actionLoading, setActionLoading] = useState(null);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [selectedRole, setSelectedRole] = useState("employee");
    const [mode, setMode] = useState("assign"); // "assign", "remove", or "history"
    const [searchQuery, setSearchQuery] = useState("");
    const [activeQuery, setActiveQuery] = useState("");
    const [changelog, setChangelog] = useState([]);
    const [historyPage, setHistoryPage] = useState(0);
    const [totalHistoryPages, setTotalHistoryPages] = useState(0);
    const [totalHistoryItems, setTotalHistoryItems] = useState(0);

    const perPage = 25;

    const roleConfig = {
        client: {
            label: t('roleManager.client'),
            assignEndpoint: "/role-assignment/users/{userId}/roles/client",
            removeEndpoint: "/role-assignment/users/{userId}/roles/client",
            bgColor: "#4ade80",
        },
        employee: {
            label: t('roleManager.employee'),
            assignEndpoint: "/role-assignment/users/{userId}/roles/employee",
            removeEndpoint: "/role-assignment/users/{userId}/roles/employee",
            bgColor: "#4ade80",
        },
        admin: {
            label: t('roleManager.admin'),
            assignEndpoint: "/role-assignment/users/{userId}/roles/admin",
            removeEndpoint: "/role-assignment/users/{userId}/roles/admin",
            bgColor: "#4ade80",
        },
    };

    const fetchChangelog = async (page = 0) => {
        setLoading(true);
        setError("");
        try {
            const token = await getAccessTokenSilently({
                authorizationParams: { audience: "https://vladtech/api" },
            });
            const response = await api.get("/role-assignment/changelog", {
                headers: { Authorization: `Bearer ${token}` },
                params: { page, perPage: 100 },
            });
            // Handle both array (old) and Page object (new) response formats
            const data = response.data;
            if (Array.isArray(data)) {
                setChangelog(data);
                setTotalHistoryPages(1);
                setTotalHistoryItems(data.length);
            } else {
                setChangelog(data.content || []);
                setTotalHistoryPages(data.totalPages || 0);
                setTotalHistoryItems(data.totalElements || 0);
            }
        } catch (err) {
            console.error("Error fetching changelog:", err);
            setError(t('roleManager.failedToLoadChangelog'));
        } finally {
            setLoading(false);
        }
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
                const response = await api.get(`/users/${selectedRole}s`, {
                    headers,
                    params: { page, perPage },
                });
                setUsers(response.data.users || []);
                setTotalUsers(response.data.total || 0);
            } else {
                if (query.trim()) {
                    const response = await api.get("/users/search", {
                        headers,
                        params: { query, page, perPage },
                    });
                    setUsers(response.data.users || []);
                    setTotalUsers(response.data.total || 0);
                } else {
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
            setError(t('roleManager.failedToLoadUsers'));
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
            if (mode === "history") {
                fetchChangelog();
            } else {
                fetchUsers(0);
            }
        }
    }, [isOpen, selectedRole, mode]);

    useEffect(() => {
        if (isOpen && currentPage > 0 && mode !== "history") {
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
            const endpoint =
                mode === "assign"
                    ? config.assignEndpoint.replace("{userId}", encodeURIComponent(userId))
                    : config.removeEndpoint.replace("{userId}", encodeURIComponent(userId));

            if (mode === "assign") {
                await api.patch(endpoint, {}, {
                    headers: { Authorization: `Bearer ${token}` },
                    params: { userName: userName || "" },
                });
                setSuccess(t('roleManager.roleAssignedTo', { role: config.label, name: userName || userId }));
            } else {
                await api.delete(endpoint, {
                    headers: { Authorization: `Bearer ${token}` },
                    params: { userName: userName || "" },
                });
                setSuccess(t('roleManager.roleRemovedFrom', { role: config.label, name: userName || userId }));
            }

            setUsers(users.filter((u) => u.user_id !== userId));
            setTotalUsers((prev) => prev - 1);

            if (onSuccess) {
                onSuccess();
            }
        } catch (err) {
            console.error(`Error ${mode}ing role:`, err);
            setError(mode === "assign" 
                ? t('roleManager.failedToAssign', { role: roleConfig[selectedRole].label })
                : t('roleManager.failedToRemove', { role: roleConfig[selectedRole].label })
            );
        } finally {
            setActionLoading(null);
        }
    };

    const getRoleButtonColor = (role) => {
        const isSelected = selectedRole === role;
        return isSelected ? "bg-green-400" : "bg-black/5 hover:bg-black/10";
    };

    const formatDate = (isoDate) => {
        if (!isoDate) return "";
        const date = new Date(isoDate);
        return date.toLocaleString();
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
                            {t('roleManager.title')}
                        </h2>
                        <p className="text-sm text-black/60 mt-1">
                            {mode === "history"
                                ? t('roleManager.changesRecorded', { count: changelog.length })
                                : mode === "assign"
                                    ? t('roleManager.usersAvailable', { count: totalUsers })
                                    : t('roleManager.usersWithRole', { count: totalUsers, role: roleConfig[selectedRole].label })}
                        </p>
                    </div>
                    <button onClick={onClose} className="p-2 hover:bg-black/5 rounded-lg transition-colors">
                        <X className="h-6 w-6" />
                    </button>
                </div>

                {/* Mode Toggle */}
                <div className="p-6 border-b border-black/10 space-y-4">
                    <div>
                        <p className="text-sm font-medium text-black/60 mb-2">{t('roleManager.action')}</p>
                        <div className="flex border-2 border-black rounded-lg overflow-hidden w-fit">
                            <button
                                onClick={() => setMode("assign")}
                                className={`px-6 py-2 font-semibold transition-all flex items-center gap-2 ${mode === "assign" ? "bg-black text-white" : "bg-white text-black hover:bg-gray-100"
                                    }`}
                            >
                                <UserPlus className="h-4 w-4" />
                                {t('roleManager.assign')}
                            </button>
                            <button
                                onClick={() => setMode("remove")}
                                className={`px-6 py-2 font-semibold transition-all flex items-center gap-2 ${mode === "remove" ? "bg-black text-white" : "bg-white text-black hover:bg-gray-100"
                                    }`}
                            >
                                <UserMinus className="h-4 w-4" />
                                {t('roleManager.remove')}
                            </button>
                            <button
                                onClick={() => setMode("history")}
                                className={`px-6 py-2 font-semibold transition-all flex items-center gap-2 ${mode === "history" ? "bg-black text-white" : "bg-white text-black hover:bg-gray-100"
                                    }`}
                            >
                                <History className="h-4 w-4" />
                                {t('roleManager.history')}
                            </button>
                        </div>
                    </div>

                    {/* Role Selection - only for assign/remove mode */}
                    {mode !== "history" && (
                        <div>
                            <p className="text-sm font-medium text-black/60 mb-2">{t('roleManager.selectRole')}</p>
                            <div className="flex gap-2">
                                <button
                                    onClick={() => setSelectedRole("client")}
                                    className={`px-4 py-2 rounded-lg transition-all font-medium ${getRoleButtonColor("client")}`}
                                >
                                    {t('roleManager.client')}
                                </button>
                                <button
                                    onClick={() => setSelectedRole("employee")}
                                    className={`px-4 py-2 rounded-lg transition-all font-medium ${getRoleButtonColor("employee")}`}
                                >
                                    {t('roleManager.employee')}
                                </button>
                                <button
                                    onClick={() => setSelectedRole("admin")}
                                    className={`px-4 py-2 rounded-lg transition-all font-medium ${getRoleButtonColor("admin")}`}
                                >
                                    {t('roleManager.admin')}
                                </button>
                            </div>
                        </div>
                    )}

                    {/* Search - only for assign mode */}
                    {mode === "assign" && (
                        <form onSubmit={handleSearch} className="flex gap-2">
                            <div className="flex-1 relative">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-black/40" />
                                <input
                                    type="text"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    placeholder={t('roleManager.searchPlaceholder')}
                                    className="w-full pl-10 pr-4 py-2 border border-black/20 rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400"
                                />
                            </div>
                            <button
                                type="submit"
                                className="px-6 py-2 bg-yellow-400 hover:bg-yellow-500 rounded-lg transition-colors font-semibold"
                            >
                                {t('roleManager.search')}
                            </button>
                            {activeQuery && (
                                <button
                                    type="button"
                                    onClick={handleClearSearch}
                                    className="px-4 py-2 border border-black/20 hover:bg-black/5 rounded-lg transition-colors"
                                >
                                    {t('roleManager.clear')}
                                </button>
                            )}
                        </form>
                    )}
                </div>

                {/* Messages */}
                {(success || error) && (
                    <div className="px-6 pt-4">
                        {success && (
                            <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded-lg flex items-center justify-between">
                                <span>{success}</span>
                                <button onClick={() => setSuccess("")}>
                                    <X className="h-4 w-4" />
                                </button>
                            </div>
                        )}
                        {error && (
                            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg flex items-center justify-between">
                                <span>{error}</span>
                                <button onClick={() => setError("")}>
                                    <X className="h-4 w-4" />
                                </button>
                            </div>
                        )}
                    </div>
                )}

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-6">
                    {loading ? (
                        <div className="flex items-center justify-center py-12">
                            <div className="animate-spin rounded-full h-12 w-12 border-4 border-yellow-400 border-t-transparent"></div>
                        </div>
                    ) : mode === "history" ? (
                        // Changelog view
                        changelog.length === 0 ? (
                            <div className="text-center py-12 text-black/60">
                                {t('roleManager.noChangesRecorded')}
                            </div>
                        ) : (
                            <div className="space-y-2">
                                {changelog.map((log, index) => (
                                    <div
                                        key={log.id || index}
                                        className={`border rounded-lg p-4 flex items-center justify-between ${log.action === "ASSIGNED"
                                            ? "border-green-200 bg-green-50"
                                            : "border-red-200 bg-red-50"
                                            }`}
                                    >
                                        <div className="flex items-center gap-4">
                                            <div
                                                className={`w-10 h-10 rounded-full flex items-center justify-center ${log.action === "ASSIGNED" ? "bg-green-400" : "bg-red-400"
                                                    }`}
                                            >
                                                {log.action === "ASSIGNED" ? (
                                                    <UserPlus className="h-5 w-5 text-white" />
                                                ) : (
                                                    <UserMinus className="h-5 w-5 text-white" />
                                                )}
                                            </div>
                                            <div>
                                                <p className="font-semibold">
                                                    <span
                                                        className={
                                                            log.action === "ASSIGNED" ? "text-green-700" : "text-red-700"
                                                        }
                                                    >
                                                        {log.action === "ASSIGNED" ? t('roleManager.assigned') : t('roleManager.removed')}
                                                    </span>{" "}
                                                    {log.roleName} {t('roleManager.role')}
                                                </p>
                                                <p className="text-sm text-black/80">
                                                    {log.userName || log.userId}
                                                </p>
                                                <p className="text-xs text-black/40">
                                                    ID: {encodeURIComponent(log.userId)}
                                                </p>
                                            </div>
                                        </div>
                                        <div className="text-sm text-black/50">{formatDate(log.performedAt)}</div>
                                    </div>
                                ))}
                            </div>
                        )
                    ) : // Users view
                        users.length === 0 ? (
                            <div className="text-center py-12 text-black/60">
                                {mode === "assign"
                                    ? activeQuery
                                        ? t('roleManager.noUsersFound')
                                        : t('roleManager.allUsersHaveRole', { role: roleConfig[selectedRole].label })
                                    : t('roleManager.noUsersHaveRole', { role: roleConfig[selectedRole].label })}
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
                                                <img src={user.picture} alt={user.name} className="w-12 h-12 rounded-full" />
                                            )}
                                            <div>
                                                <p className="font-semibold text-lg">{user.name || t('roleManager.noName')}</p>
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
                                                backgroundColor:
                                                    actionLoading === user.user_id
                                                        ? undefined
                                                        : mode === "remove"
                                                            ? undefined
                                                            : roleConfig[selectedRole].bgColor,
                                            }}
                                        >
                                            {actionLoading === user.user_id ? (
                                                <>
                                                    <div className="animate-spin rounded-full h-4 w-4 border-2 border-black border-t-transparent"></div>
                                                    {mode === "assign" ? t('roleManager.assigning') : t('roleManager.removing')}
                                                </>
                                            ) : mode === "assign" ? (
                                                <>
                                                    <UserPlus className="h-4 w-4" />
                                                    {t('roleManager.assignRole', { role: roleConfig[selectedRole].label })}
                                                </>
                                            ) : (
                                                <>
                                                    <UserMinus className="h-4 w-4" />
                                                    {t('roleManager.removeRole', { role: roleConfig[selectedRole].label })}
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
                        onClick={() => {
                            if (mode === "history") {
                                setHistoryPage(historyPage - 1);
                                fetchChangelog(historyPage - 1);
                            } else {
                                setCurrentPage(currentPage - 1);
                            }
                        }}
                        disabled={(mode === "history" ? historyPage === 0 : currentPage === 0) || loading}
                        className="flex items-center gap-2 px-4 py-2 border border-black/20 rounded-lg hover:bg-black/5 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        <ChevronLeft className="h-4 w-4" />
                        {t('roleManager.previous')}
                    </button>
                    <div className="text-sm text-black/60">
                        {mode === "history"
                            ? t('roleManager.pageOfTotal', { current: historyPage + 1, total: totalHistoryPages || 1, totalItems: totalHistoryItems })
                            : t('roleManager.pageOf', { current: currentPage + 1, total: totalPages || 1 })}
                    </div>
                    <button
                        onClick={() => {
                            if (mode === "history") {
                                setHistoryPage(historyPage + 1);
                                fetchChangelog(historyPage + 1);
                            } else {
                                setCurrentPage(currentPage + 1);
                            }
                        }}
                        disabled={(mode === "history" ? historyPage >= totalHistoryPages - 1 : currentPage >= totalPages - 1) || loading}
                        className="flex items-center gap-2 px-4 py-2 border border-black/20 rounded-lg hover:bg-black/5 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        {t('roleManager.next')}
                        <ChevronRight className="h-4 w-4" />
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RoleManagerModal;
