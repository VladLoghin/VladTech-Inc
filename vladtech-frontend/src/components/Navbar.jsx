// Navbar.jsx
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth0 } from "@auth0/auth0-react";
import { useTranslation } from "react-i18next";
import { LogIn, LogOut, Menu, X } from "lucide-react";
import { useLanguage } from "../context/LanguageContext";
import UserMenu from "./UserMenu.jsx";
import MobileUserProfile from "./MobileUserProfile.jsx";
import { motion } from "motion/react";

const Navbar = ({ isNavbarDark = false, onScrollToSection = null, showHomeLinks = false }) => {
  const { loginWithRedirect, logout, isAuthenticated, user } = useAuth0();
  const { language, toggleLanguage } = useLanguage();
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navRef = useRef(null);

  // close on outside click
  useEffect(() => {
    if (!isMobileMenuOpen) return;
    const onPointerDown = (e) => {
      if (!navRef.current) return;
      if (!navRef.current.contains(e.target)) setIsMobileMenuOpen(false);
    };
    document.addEventListener("pointerdown", onPointerDown);
    return () => document.removeEventListener("pointerdown", onPointerDown);
  }, [isMobileMenuOpen]);

  // roles
  const rawRoles = user?.["https://vladtech.com/roles"];
  const roles = Array.isArray(rawRoles) ? rawRoles : typeof rawRoles === "string" ? [rawRoles] : [];
  const isAdmin = roles.includes("Admin");
  const isEmployee = roles.includes("Employee");
  const isClient = roles.includes("Client");

  const closeAnd = (fn) => () => {
    fn?.();
    setIsMobileMenuOpen(false);
  };

  const scrollTo = (id) => closeAnd(() => onScrollToSection?.(id))();

  return (
    <nav
      ref={navRef}
      className={`fixed top-0 left-0 right-0 z-[9999] backdrop-blur-sm border-b transition-all duration-300 ${
        isNavbarDark ? "bg-black/95 border-white/10" : "bg-white/95 border-black/10"
      }`}
    >
      {/* TOP BAR */}
      <div className="container mx-auto px-8 py-6 flex items-center justify-between">
        {/* LEFT cluster (matches hosted screenshot) */}
        <div className="flex items-center gap-4 min-w-0">
          <button
            onClick={() => navigate("/")}
            className={`tracking-widest transition-colors truncate ${
              isNavbarDark ? "text-white hover:text-yellow-400" : "text-black hover:text-yellow-400"
            }`}
          >
            VLADTECH
          </button>

          {/* Language toggle (reverted look + placement) */}
          <div
            onClick={toggleLanguage}
            className={`relative flex items-center w-20 h-8 rounded-full cursor-pointer transition-all ${
              isNavbarDark ? "bg-white/20" : "bg-black/20"
            }`}
            aria-label="Toggle language"
            role="switch"
            aria-checked={language === "fr"}
          >
            <span
              className={`absolute left-2 text-xs font-semibold transition-all ${
                language === "en"
                  ? isNavbarDark
                    ? "text-white"
                    : "text-black"
                  : "text-gray-400"
              }`}
            >
              EN
            </span>

            <span
              className={`absolute right-2 text-xs font-semibold transition-all ${
                language === "fr"
                  ? isNavbarDark
                    ? "text-white"
                    : "text-black"
                  : "text-gray-400"
              }`}
            >
              FR
            </span>

            <div
              className={`absolute w-6 h-6 rounded-full bg-yellow-400 shadow-md transition-all duration-300 ${
                language === "en" ? "left-1" : "left-[calc(100%-1.75rem)]"
              }`}
            />
          </div>
        </div>

        {/* RIGHT cluster */}
        <div className="flex items-center gap-6">
          {/* Desktop links */}
          <div className="hidden md:flex gap-12 items-center">
            {showHomeLinks && onScrollToSection && (
              <>
                <button
                  onClick={() => onScrollToSection("portfolio")}
                  className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                    isNavbarDark ? "text-white" : "text-black"
                  }`}
                >
                  {t("nav.portfolio")}
                </button>
                <button
                  onClick={() => onScrollToSection("about")}
                  className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                    isNavbarDark ? "text-white" : "text-black"
                  }`}
                >
                  {t("nav.about")}
                </button>
                <button
                  onClick={() => onScrollToSection("contact")}
                  className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                    isNavbarDark ? "text-white" : "text-black"
                  }`}
                >
                  {t("nav.contact")}
                </button>
              </>
            )}

            {isAuthenticated && isAdmin && (
              <button
                onClick={() => navigate("/admin")}
                className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                {t("nav.adminPanel")}
              </button>
            )}

            {isAuthenticated && isEmployee && (
              <button
                onClick={() => navigate("/employee")}
                className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                {t("nav.employeeTools")}
              </button>
            )}

            {isAuthenticated && !isAdmin && (
              <button
                onClick={() => navigate("/dashboard")}
                className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                {t("nav.dashboard")}
              </button>
            )}

            {isAuthenticated && (isAdmin || isEmployee || isClient) && (
              <span
                className={`px-3 py-1 text-xs uppercase tracking-wider border rounded-full ${
                  isNavbarDark ? "border-yellow-400 text-yellow-300" : "border-black/30 text-black/70"
                }`}
              >
                {[isAdmin ? "Admin" : null, isEmployee ? "Employee" : null, isClient ? "Client" : null]
                  .filter(Boolean)
                  .join(" · ")}
              </span>
            )}

            {isAuthenticated && <UserMenu user={user} isNavbarDark={isNavbarDark} t={t} />}

            {!isAuthenticated ? (
              <button
                onClick={() => loginWithRedirect()}
                className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm ${
                  isNavbarDark
                    ? "bg-white text-black hover:bg-yellow-400"
                    : "bg-black text-white hover:bg-yellow-400 hover:text-black"
                }`}
              >
                <LogIn className="h-4 w-4" />
                {t("nav.login")}
              </button>
            ) : (
              <button
                onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
                className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm ${
                  isNavbarDark
                    ? "bg-white text-black hover:bg-yellow-400"
                    : "bg-black text-white hover:bg-yellow-400 hover:text-black"
                }`}
              >
                <LogOut className="h-4 w-4" />
                {t("nav.logout")}
              </button>
            )}
          </div>

          {/* Mobile hamburger */}
          <button
            type="button"
            onClick={() => setIsMobileMenuOpen((v) => !v)}
            className={`md:hidden transition-colors ${isNavbarDark ? "text-white" : "text-black"}`}
            aria-expanded={isMobileMenuOpen}
          >
            {isMobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </div>
      </div>

      {/* Mobile dropdown */}
      <motion.div
        initial={{ opacity: 0, height: 0 }}
        animate={{ opacity: isMobileMenuOpen ? 1 : 0, height: isMobileMenuOpen ? "auto" : 0 }}
        transition={{ duration: 0.25, ease: "easeInOut" }}
        className={`md:hidden border-t ${isMobileMenuOpen ? "overflow-visible" : "overflow-hidden"} ${
          isNavbarDark ? "border-white/10 bg-black/95" : "border-black/10 bg-white/95"
        }`}
      >
        <div className="container mx-auto px-8 py-4 flex flex-col gap-4">
          {/* THIS is the missing piece you want: profile block INSIDE menu */}
          {isAuthenticated && (
            <MobileUserProfile
              user={user}
              isNavbarDark={isNavbarDark}
              t={t}
            />
          )}

          {showHomeLinks && onScrollToSection && (
            <>
              <button
                onClick={() => scrollTo("portfolio")}
                className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                {t("nav.portfolio")}
              </button>
              <button
                onClick={() => scrollTo("about")}
                className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                {t("nav.about")}
              </button>
              <button
                onClick={() => scrollTo("contact")}
                className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                {t("nav.contact")}
              </button>
            </>
          )}

          {isAuthenticated && isAdmin && (
            <button
              onClick={closeAnd(() => navigate("/admin"))}
              className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                isNavbarDark ? "text-white" : "text-black"
              }`}
            >
              {t("nav.adminPanel")}
            </button>
          )}

          {isAuthenticated && isEmployee && (
            <button
              onClick={closeAnd(() => navigate("/employee"))}
              className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                isNavbarDark ? "text-white" : "text-black"
              }`}
            >
              {t("nav.employeeTools")}
            </button>
          )}

          {isAuthenticated && !isAdmin && (
            <button
              onClick={closeAnd(() => navigate("/dashboard"))}
              className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                isNavbarDark ? "text-white" : "text-black"
              }`}
            >
              {t("nav.dashboard")}
            </button>
          )}

          {!isAuthenticated ? (
            <button
              onClick={closeAnd(() => loginWithRedirect())}
              className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm justify-center ${
                isNavbarDark
                  ? "bg-white text-black hover:bg-yellow-400"
                  : "bg-black text-white hover:bg-yellow-400 hover:text-black"
              }`}
            >
              <LogIn className="h-4 w-4" />
              {t("nav.login")}
            </button>
          ) : (
            <button
              onClick={closeAnd(() => logout({ logoutParams: { returnTo: window.location.origin } }))}
              className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm justify-center ${
                isNavbarDark
                  ? "bg-white text-black hover:bg-yellow-400"
                  : "bg-black text-white hover:bg-yellow-400 hover:text-black"
              }`}
            >
              <LogOut className="h-4 w-4" />
              {t("nav.logout")}
            </button>
          )}
        </div>
      </motion.div>
    </nav>
  );
};

export default Navbar;
