// MobileUserProfile.jsx
import React, { useEffect, useRef, useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { Pencil } from "lucide-react";
import { api } from "../api/http";

export default function MobileUserProfile({ user, isNavbarDark = false, t }) {
  const { getAccessTokenSilently } = useAuth0();
  const [isEditingName, setIsEditingName] = useState(false);
  const [name, setName] = useState(user?.name || "");
  const [loadingName, setLoadingName] = useState(false);
  const [savingName, setSavingName] = useState(false);
  const [nameError, setNameError] = useState("");
  const inputRef = useRef(null);

  useEffect(() => {
    let mounted = true;
    const syncProfile = async () => {
      if (!user) return;
      try {
        setLoadingName(true);
        const token = await getAccessTokenSilently();
        await api.post("/public/sync-profile", null, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const response = await api.get("/public/profile", {
          headers: { Authorization: `Bearer ${token}` },
        });
        const match = response.data.match(/User Name: (.+)/);
        if (mounted) setName(match ? match[1] : user?.name || "");
      } catch (error) {
        console.error("Failed to sync/fetch profile (mobile):", error);
        if (mounted) setNameError(t?.("failedToLoadName") || "Failed to load name");
      } finally {
        if (mounted) setLoadingName(false);
      }
    };

    syncProfile();
    return () => { mounted = false; };
  }, [user, getAccessTokenSilently, t]);

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
          audience: "https://vladtech/api",
        },
      });

      await api.patch(
        "/public/profile/update-name",
        { name: name.trim() },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setIsEditingName(false);
    } catch (error) {
      console.error("Failed to save name (mobile):", error);
      setNameError(t?.("errors.saveName") || "Failed to save name");
    } finally {
      setSavingName(false);
    }
  };

  if (!user) return null;

  const initial = (user.name || user.email || "?").charAt(0).toUpperCase();

  return (
    <div
      className={`w-full p-4 rounded-lg border ${
        isNavbarDark
          ? "bg-white/5 text-white border-white/10"
          : "bg-black/5 text-black border-black/10"
      }`}
    >
      <div className="flex items-center gap-3 mb-4">
        <div className="w-10 h-10 rounded-full overflow-hidden border border-yellow-500 bg-yellow-400 flex-shrink-0">
          {user?.picture ? (
            <img src={user.picture} alt="avatar" className="w-full h-full object-cover" />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-xs font-bold text-black">
              {initial}
            </div>
          )}
        </div>

        <div className="min-w-0">
          <div className="font-semibold text-sm truncate">
            {user?.name || "User"}
          </div>
          <div className={`text-xs truncate ${isNavbarDark ? "text-white/60" : "text-black/60"}`}>
            {user?.["https://vladtech.com/roles"] || "Member"}
          </div>
        </div>
      </div>

      <hr className={isNavbarDark ? "border-white/10" : "border-black/10"} />

      {/* Editable name field - matching UserMenu style */}
      <div className="mt-4">
        <div className="flex items-center justify-between mb-2">
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
          value={name}
          onChange={(e) => setName(e.target.value)}
          disabled={!isEditingName || loadingName || savingName}
          placeholder={
            loadingName
              ? t?.("loading") || "Loading..."
              : isEditingName
              ? t?.("enterName") || "Enter your name"
              : t?.("nameComingSoon") || "Name field - coming soon"
          }
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
  );
}
