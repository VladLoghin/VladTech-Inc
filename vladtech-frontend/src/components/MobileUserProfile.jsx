// MobileUserProfile.jsx
import React, { useState } from "react";
import { Pencil } from "lucide-react";

export default function MobileUserProfile({ user, isNavbarDark = false, t }) {
  const [isEditingName, setIsEditingName] = useState(false);
  const [name, setName] = useState(user?.name || "");

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
            onClick={() => setIsEditingName(!isEditingName)}
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
          type="text"
          data-testid="user-menu-name-input"
          value={name}
          onChange={(e) => setName(e.target.value)}
          disabled={!isEditingName}
          placeholder={isEditingName ? "Enter your name" : "Name field - coming soon"}
          className={`w-full px-3 py-2 rounded text-sm ${
            isNavbarDark
              ? "bg-white/5 border border-white/10 text-gray-300 disabled:text-gray-500"
              : "bg-black/5 border border-black/10 text-gray-700 disabled:text-gray-400"
          } disabled:cursor-not-allowed`}
        />
      </div>
    </div>
  );
}
