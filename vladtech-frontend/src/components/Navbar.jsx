import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth0 } from "@auth0/auth0-react";
import { LogIn, LogOut, Menu, X } from "lucide-react";
// eslint-disable-next-line no-unused-vars
import { motion } from "motion/react";
import { useLanguage } from "../context/LanguageContext";

const Navbar = ({ isNavbarDark = false, onScrollToSection = null, showHomeLinks = false }) => {
  const { loginWithRedirect, logout, isAuthenticated, user } = useAuth0();
  const { language, toggleLanguage } = useLanguage();
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  // Get user roles from Auth0 custom claim
  const rawRoles = user?.["https://vladtech.com/roles"];
  const roles = Array.isArray(rawRoles)
    ? rawRoles
    : typeof rawRoles === "string"
      ? [rawRoles]
      : [];

  const isAdmin = roles.includes("Admin");
  const isEmployee = roles.includes("Employee");
  const isClient = roles.includes("Client");

  return (
    <>
      <nav
        className={`fixed top-0 left-0 right-0 z-50 backdrop-blur-sm border-b transition-all duration-300 ${
          isNavbarDark ? "bg-black/95 border-white/10" : "bg-white/95 border-black/10"
        }`}
      >
        <div className="container mx-auto px-8 py-6 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate("/")}
              className={`tracking-widest transition-colors ${
                isNavbarDark ? "text-white hover:text-yellow-400" : "text-black hover:text-yellow-400"
              }`}
            >
              VLADTECH
            </button>

            {/* Language Toggle - Always Visible */}
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

          {/* Hamburger Menu Button - Mobile Only */}
          <button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className={`md:hidden transition-colors ${isNavbarDark ? "text-white" : "text-black"}`}
          >
            {isMobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>

          {/* Desktop Menu */}
          <div className="hidden md:flex gap-12 items-center">
            {/* Show home-specific links if on homepage */}
            {showHomeLinks && onScrollToSection && (
              <>
                <button
                  onClick={() => onScrollToSection("portfolio")}
                  className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                    isNavbarDark ? "text-white" : "text-black"
                  }`}
                >
                  PORTFOLIO
                </button>
                <button
                  onClick={() => onScrollToSection("about")}
                  className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                    isNavbarDark ? "text-white" : "text-black"
                  }`}
                >
                  ABOUT
                </button>
                <button
                  onClick={() => onScrollToSection("contact")}
                  className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                    isNavbarDark ? "text-white" : "text-black"
                  }`}
                >
                  CONTACT
                </button>
              </>
            )}

            {/* Role-based navigation links */}
            {isAuthenticated && user?.["https://vladtech.com/roles"]?.includes("Admin") && (
              <button
                onClick={() => navigate("/admin")}
                className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                ADMIN PANEL
              </button>
            )}

            {isAuthenticated && user?.["https://vladtech.com/roles"]?.includes("Employee") && (
              <button
                onClick={() => navigate("/employee")}
                className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                EMPLOYEE TOOLS
              </button>
            )}

            {isAuthenticated && !user?.["https://vladtech.com/roles"]?.includes("Admin") && (
              <button
                onClick={() => navigate("/dashboard")}
                className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                DASHBOARD
              </button>
            )}

            {/* Role badge */}
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

            {isAuthenticated && (
              <div className="w-8 h-8 rounded-full overflow-hidden border-2 border-yellow-400">
                {user?.picture ? (
                  <img src={user.picture} alt="avatar" className="w-full h-full object-cover" />
                ) : (
                  <div className="w-full h-full bg-yellow-400/20" />
                )}
              </div>
            )}

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
                LOGIN
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
                LOGOUT
              </button>
            )}
          </div>
        </div>

        {/* Mobile Menu Dropdown */}
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{
            opacity: isMobileMenuOpen ? 1 : 0,
            height: isMobileMenuOpen ? "auto" : 0,
          }}
          transition={{ duration: 0.3, ease: "easeInOut" }}
          className={`md:hidden border-t overflow-hidden ${
            isNavbarDark ? "border-white/10 bg-black/95" : "border-black/10 bg-white/95"
          } backdrop-blur-sm`}
        >
          <div className="container mx-auto px-8 py-4 flex flex-col gap-4">
            {/* Show home-specific links if on homepage */}
            {showHomeLinks && onScrollToSection && (
              <>
                <button
                  onClick={() => {
                    onScrollToSection("portfolio");
                    setIsMobileMenuOpen(false);
                  }}
                  className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                    isNavbarDark ? "text-white" : "text-black"
                  }`}
                >
                  PORTFOLIO
                </button>
                <button
                  onClick={() => {
                    onScrollToSection("about");
                    setIsMobileMenuOpen(false);
                  }}
                  className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                    isNavbarDark ? "text-white" : "text-black"
                  }`}
                >
                  ABOUT
                </button>
                <button
                  onClick={() => {
                    onScrollToSection("contact");
                    setIsMobileMenuOpen(false);
                  }}
                  className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                    isNavbarDark ? "text-white" : "text-black"
                  }`}
                >
                  CONTACT
                </button>
              </>
            )}

            {/* Role-based navigation - Mobile */}
            {isAuthenticated && user?.["https://vladtech.com/roles"]?.includes("Admin") && (
              <button
                onClick={() => {
                  navigate("/admin");
                  setIsMobileMenuOpen(false);
                }}
                className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                ADMIN PANEL
              </button>
            )}

            {isAuthenticated && user?.["https://vladtech.com/roles"]?.includes("Employee") && (
              <button
                onClick={() => {
                  navigate("/employee");
                  setIsMobileMenuOpen(false);
                }}
                className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                EMPLOYEE TOOLS
              </button>
            )}

            {isAuthenticated && !user?.["https://vladtech.com/roles"]?.includes("Admin") && (
              <button
                onClick={() => {
                  navigate("/dashboard");
                  setIsMobileMenuOpen(false);
                }}
                className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                  isNavbarDark ? "text-white" : "text-black"
                }`}
              >
                DASHBOARD
              </button>
            )}

            {!isAuthenticated ? (
              <button
                onClick={() => {
                  loginWithRedirect();
                  setIsMobileMenuOpen(false);
                }}
                className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm justify-center ${
                  isNavbarDark
                    ? "bg-white text-black hover:bg-yellow-400"
                    : "bg-black text-white hover:bg-yellow-400 hover:text-black"
                }`}
              >
                <LogIn className="h-4 w-4" />
                LOGIN
              </button>
            ) : (
              <button
                onClick={() => {
                  logout({ logoutParams: { returnTo: window.location.origin } });
                  setIsMobileMenuOpen(false);
                }}
                className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm justify-center ${
                  isNavbarDark
                    ? "bg-white text-black hover:bg-yellow-400"
                    : "bg-black text-white hover:bg-yellow-400 hover:text-black"
                }`}
              >
                <LogOut className="h-4 w-4" />
                LOGOUT
              </button>
            )}
          </div>
        </motion.div>
      </nav>
    </>
  );
};

export default Navbar;
