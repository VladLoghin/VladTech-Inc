import { useEffect, useRef, useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { Pencil } from "lucide-react";
import { AnimatePresence, motion } from "motion/react"; // eslint-disable-line no-unused-vars
import { api } from "../api/http";

export default function UserMenu({ user, isNavbarDark = false, t }) {
  const { getAccessTokenSilently } = useAuth0();
  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [isEditingName, setIsEditingName] = useState(false);
  const [loadingName, setLoadingName] = useState(false);
  const [savingName, setSavingName] = useState(false);
  const [nameError, setNameError] = useState("");
  const inputRef = useRef(null);
  const ref = useRef(null);

  useEffect(() => {
    if (!open) return;

    const onMouseDown = (e) => {
      if (!ref.current) return;
      if (!ref.current.contains(e.target)) setOpen(false);
    };

    document.addEventListener("mousedown", onMouseDown);
    return () => document.removeEventListener("mousedown", onMouseDown);
  }, [open]);

  // Fetch profile name when menu opens
  useEffect(() => {
    const syncProfile = async () => {
      if (open && user) {
        try {
          const token = await getAccessTokenSilently();

          // Sync profile first
          await api.post("/public/sync-profile", null, {
            headers: { Authorization: `Bearer ${token}` },
          });

          setLoadingName(true);
          const response = await api.get("/public/profile", {
            headers: { Authorization: `Bearer ${token}` },
          });

          const match = response.data.match(/User Name: (.+)/);
          setName(match ? match[1] : "");
        } catch (error) {
          console.error("Failed to sync/fetch profile:", error);
          setNameError(t?.("failedToLoadName") || "Failed to load name");
        } finally {
          setLoadingName(false);
        }
      }
    };

    syncProfile();
  }, [open, user, getAccessTokenSilently, t]);

  const handleSaveName = async () => {
    if (!name?.trim()) {
      setNameError(t?.("errors.invalidName") || "Name cannot be empty");
      return;
    }

    setSavingName(true);
    setNameError("");
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: "https://vladtech/api", // ensure audience is requested
        },
      });

      await api.patch(
        "/public/profile/update-name",
        { name: name.trim() }, // ← Only send name, not userId
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setIsEditingName(false);
    } catch (error) {
      console.error("Failed to save name:", error);
      setNameError(t?.("errors.saveName") || "Failed to save name");
    } finally {
      setSavingName(false);
    }
  };

  const initialLetter = (user?.name || user?.email || "?").charAt(0).toUpperCase();

  return (
    <div className="relative inline-flex" ref={ref}>
      <button
        type="button"
        data-testid="user-menu-toggle"
        onClick={(e) => { e.stopPropagation(); setOpen(v => !v); }}
        className="w-8 h-8 rounded-full overflow-hidden border-2 border-yellow-500 bg-yellow-400 flex-shrink-0"
      >
        {user?.picture ? (
          <img src={user.picture} alt="avatar" className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full bg-yellow-400 text-black flex items-center justify-center text-xs font-bold">
            {initialLetter}
          </div>
        )}
      </button>

      {user && (
        <AnimatePresence>
          {open && (
            <motion.div
              data-testid="user-menu-panel"
              initial={{ opacity: 0, y: -10, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -10, scale: 0.98 }}
              className={`absolute right-0 mt-2 w-64 rounded-lg shadow-lg z-[99999] ${
                isNavbarDark ? "bg-black/95 border border-white/20" : "bg-white/95 border border-black/10"
              }`}
            >
              <div className="p-4">
                <div className="flex items-center gap-3 mb-4">
                  <div className="w-12 h-12 rounded-full overflow-hidden border-2 border-yellow-400">
                    {user?.picture ? (
                      <img src={user.picture} alt="avatar" className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full bg-yellow-400 text-black flex items-center justify-center text-sm font-bold">
                        {initialLetter}
                      </div>
                    )}
                  </div>

                  <div className="min-w-0">
                    <p className={`font-semibold truncate ${isNavbarDark ? "text-white" : "text-black"}`}>
                      {user?.name || "User"}
                    </p>
                    <p className={`text-xs truncate ${isNavbarDark ? "text-gray-400" : "text-gray-600"}`}>
                      {user?.email || ""}
                    </p>
                  </div>
                </div>

                <hr className={isNavbarDark ? "border-white/10" : "border-black/10"} />

                <div className="mt-4">
                  <div className="flex items-center justify-between mb-1">
                    <label className={`block text-xs font-medium ${isNavbarDark ? "text-gray-400" : "text-gray-600"}`}>
                      {t?.("name") || "Name"}
                    </label>
                    <button
                      type="button"
                      data-testid="edit-name-button"
                      onClick={() => {
                        setIsEditingName(true);
                        setTimeout(() => inputRef.current?.focus(), 0);
                      }}
                      className={`p-1 rounded transition-colors ${
                        isNavbarDark
                          ? "text-gray-500 hover:text-gray-300 hover:bg-white/5"
                          : "text-gray-400 hover:text-gray-600 hover:bg-black/5"
                      }`}
                    >
                      <Pencil className="h-3 w-3" />
                    </button>
                  </div>
                  <input
                    ref={inputRef}
                    type="text"
                    data-testid="user-menu-name-input"
                    disabled={!isEditingName || loadingName || savingName}
                    placeholder={
                      loadingName
                        ? t?.("loading") || "Loading..."
                        : isEditingName
                        ? t?.("enterName") || "Enter your name"
                        : t?.("nameComingSoon") || "Name field - coming soon"
                    }
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className={`w-full px-3 py-2 rounded text-sm ${
                      isNavbarDark
                        ? "bg-white/5 border border-white/10 text-gray-300 disabled:text-gray-500"
                        : "bg-black/5 border border-black/10 text-gray-700 disabled:text-gray-400"
                    } disabled:cursor-not-allowed`}
                  />

                  {isEditingName && (
                    <div className="flex gap-2 mt-2">
                      <button
                        type="button"
                        data-testid="save-name-button"
                        onClick={handleSaveName}
                        disabled={savingName}
                        className={`px-3 py-1 rounded text-xs font-medium ${
                          isNavbarDark
                            ? "bg-yellow-400 text-black hover:bg-yellow-300"
                            : "bg-yellow-500 text-black hover:bg-yellow-400"
                        } disabled:opacity-60 disabled:cursor-not-allowed`}
                      >
                        {savingName ? t?.("saving") || "Saving..." : t?.("save") || "Save"}
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setIsEditingName(false);
                          setName(user?.name || "");
                          setNameError("");
                        }}
                        className={`px-3 py-1 rounded text-xs font-medium ${
                          isNavbarDark
                            ? "text-gray-300 hover:bg-white/10"
                            : "text-gray-700 hover:bg-black/5"
                        }`}
                      >
                        {t?.("cancel") || "Cancel"}
                      </button>
                    </div>
                  )}

                  {nameError && (
                    <p className={`mt-1 text-xs ${isNavbarDark ? "text-red-300" : "text-red-600"}`}>
                      {nameError}
                    </p>
                  )}
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      )}
    </div>
  );
}