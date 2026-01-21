import { useState, useEffect } from "react";
import { Button } from "../components/button.js";
// Removed Input, Textarea, Label imports - not needed for button-only contact section
import { Send, LogIn, LogOut, Menu, X } from "lucide-react";
import { motion } from "motion/react";
import { useAuth0 } from "@auth0/auth0-react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import getImageUrl from "../utils/getImageUrl.js";
import { api } from "../api/http.js";
import { useLanguage } from "../context/LanguageContext";


interface HomePageProps {
  onNavigate?: (page: string) => void;
  onOpenContactModal?: () => void;
  onOpenEstimateModal?: () => void;
}

const portfolioImages = [
  {
    id: 1,
    url: "https://images.unsplash.com/photo-1681216868987-b7268753b81c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtb2Rlcm4lMjBhcmNoaXRlY3R1cmUlMjBidWlsZGluZ3xlbnwxfHx8fDE3NjIyMTI5NjN8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Modern Glass Tower",
  },
  {
    id: 2,
    url: "https://images.unsplash.com/photo-1618385455730-2571c38966b7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjb25zdHJ1Y3Rpb24lMjB0ZWNobm9sb2d5fGVufDF8fHx8MTc2MjE1MjYyNXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Construction Tech",
  },
  {
    id: 3,
    url: "https://images.unsplash.com/photo-1629132497808-1c55ac45c4da?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxlbmdpbmVlcmluZyUyMHByb2plY3R8ZW58MXx8fHwxNzYyMjAwMTUxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Engineering Complex",
  },
  {
    id: 4,
    url: "https://images.unsplash.com/photo-1612272362018-4c4084eebdd4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxnbGFzcyUyMHNreXNjcmFwZXJ8ZW58MXx8fHwxNzYyMjI3MDEzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Glass Skyscraper",
  },
  {
    id: 5,
    url: "https://images.unsplash.com/photo-1572061971745-063e9cc83afc?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtb2Rlcm4lMjBjb25zdHJ1Y3Rpb258ZW58MXx8fHwxNzYyMTU1Mjk2fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Modern Construction",
  },
  {
    id: 6,
    url: "https://images.unsplash.com/photo-1523477593243-78bbf626fd3b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxidWlsZGluZyUyMGZhY2FkZXxlbnwxfHx8fDE3NjIyMjcwMTR8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Building Facade",
  },
  {
    id: 7,
    url: "https://images.unsplash.com/photo-1554793000-245d3a3c2a51?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx1cmJhbiUyMGFyY2hpdGVjdHVyZXxlbnwxfHx8fDE3NjIxNjY3MDB8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Urban Architecture",
  },
  {
    id: 8,
    url: "https://images.unsplash.com/photo-1720762256650-ea429f429226?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjb25jcmV0ZSUyMGJ1aWxkaW5nfGVufDF8fHx8MTc2MjIyNzAxNXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Concrete Building",
  },
  {
    id: 9,
    url: "https://images.unsplash.com/photo-1609627016501-b862497c7294?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzdGVlbCUyMHN0cnVjdHVyZXxlbnwxfHx8fDE3NjIyMjcwMTV8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Steel Structure",
  },
];

export default function HomePage({ onNavigate, onOpenContactModal, onOpenEstimateModal }: HomePageProps) {
  const { loginWithRedirect, logout, isAuthenticated, user, getAccessTokenSilently } = useAuth0();
  const { language, toggleLanguage } = useLanguage();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [currentSlide, setCurrentSlide] = useState(0);
  const [isRevealed, setIsRevealed] = useState(false);
  const [isNavbarDark, setIsNavbarDark] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const [token, setToken] = useState<string>("");
  useEffect(() => {
    if (isAuthenticated) {
      getAccessTokenSilently({
                authorizationParams: {
                    audience: "https://vladtech/api",
                },
            }).then(t => {
        setToken(t);
        //console.log("Token:", t);
      });
    }
  }, [isAuthenticated, getAccessTokenSilently]);

  // Debug: Log user roles and JWT token
  useEffect(() => {
    const logTokenInfo = async () => {
      if (user) {
        //console.log("🔐 User roles:", user["https://vladtech.com/roles"]);
        //console.log("👤 Full user object:", user);

        try {
          const token = await getAccessTokenSilently({
                authorizationParams: {
                    audience: "https://vladtech/api",
                },
            });
          //console.log("🔑 JWT Token:", token);
        } catch (error) {
          console.error("Failed to get access token:", error);
        }
      }
    };

    logTokenInfo();
  }, [user, getAccessTokenSilently]);

  // NEW: dynamic stats
  const [projectCount, setProjectCount] = useState<number | null>(null);
  const [ageValue, setAgeValue] = useState<string>("10+");
  const [ageUnit, setAgeUnit] = useState<string>("MONTHS");
  const [satisfactionPercentage, setSatisfactionPercentage] = useState<number | null>(null);
  const [publicMessage, setPublicMessage] = useState<string>("");

  // Fetch project count from backend
  useEffect(() => {
  const fetchProjectCount = async () => {
    try {
      const response = await api.get("/projects/count");
      setProjectCount(response.data);
    } catch (error) {
      console.error("Failed to fetch project count:", error);
      setProjectCount(0);
    }
  };

  fetchProjectCount();
}, []);

  // Fetch satisfaction percentage from backend
  useEffect(() => {
    const fetchSatisfactionPercentage = async () => {
      try {
        const response = await api.get("/reviews/satisfaction-percentage");
        setSatisfactionPercentage(response.data);
      } catch (error) {
        console.error("Failed to fetch satisfaction percentage:", error);
        setSatisfactionPercentage(98); // fallback to default
      }
    };

    fetchSatisfactionPercentage();
  }, []);

  // Fetch public hello message from backend
  useEffect(() => {
    const fetchPublicMessage = async () => {
      try {
        const response = await api.get("/public/hello");
        setPublicMessage(response.data);
      } catch (error) {
        console.error("Failed to fetch public message:", error);
      }
    };

    fetchPublicMessage();
  }, []);

  const scrollToSection = (sectionId: string) => {
    const element = document.getElementById(sectionId);
    element?.scrollIntoView({ behavior: "smooth" });
  };

  // Auto-swipe carousel every 4 seconds
  useEffect(() => {
    setIsRevealed(true);
    const interval = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % 3); // 3 slides total (9 images / 3 per slide)
    }, 4000);
    return () => clearInterval(interval);
  }, []);

  // Navbar color change on scroll
  useEffect(() => {
    const handleScroll = () => {
      const portfolioSection = document.getElementById("portfolio");
      if (portfolioSection) {
        const rect = portfolioSection.getBoundingClientRect();
        // If portfolio section is at or above the navbar
        if (rect.top <= 100) {
          setIsNavbarDark(true);
        } else {
          setIsNavbarDark(false);
        }
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // NEW: compute dynamic company age (months/years)
  useEffect(() => {
    const foundingDate = new Date("2024-01-01"); // adjust if needed
    const now = new Date();

    const yearsDiff = now.getFullYear() - foundingDate.getFullYear();
    const monthsDiff = now.getMonth() - foundingDate.getMonth();
    const totalMonths = yearsDiff * 12 + monthsDiff;

    if (totalMonths < 12) {
      const displayMonths = Math.max(totalMonths, 1); // never show 0
      setAgeValue(`${displayMonths}+`);
      setAgeUnit("MONTHS");
    } else {
      const years = Math.floor(totalMonths / 12);
      setAgeValue(`${years}+`);
      setAgeUnit(years === 1 ? "YEAR" : "YEARS");
    }
  }, []);

  // Get 3 images for current slide (kept as you had it)
  const getCurrentImages = () => {
    const startIndex = currentSlide * 3;
    return portfolioImages.slice(startIndex, startIndex + 3);
  };

  // Normalize roles and compute flags
  const rawRoles = user?.["https://vladtech.com/roles"];
  const roles: string[] = Array.isArray(rawRoles)
    ? rawRoles
    : typeof rawRoles === "string"
      ? [rawRoles]
      : [];

  const isAdmin = roles.includes("Admin");
  const isEmployee = roles.includes("Employee");
  const isClient = roles.includes("Client");

  return (
    <div className="min-h-screen bg-white">
      <nav
        className={`fixed top-0 left-0 right-0 z-50 backdrop-blur-sm border-b transition-all duration-300 ${isNavbarDark ? "bg-black/95 border-white/10" : "bg-white/95 border-black/10"
          }`}
      >
        <div className="container mx-auto px-8 py-6 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <div
              className={`tracking-widest transition-colors ${isNavbarDark ? "text-white" : "text-black"
                }`}
            >
              VLADTECH
            </div>

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
            className={`md:hidden transition-colors ${isNavbarDark ? "text-white" : "text-black"
              }`}
          >
            {isMobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>

          {/* Desktop Menu */}
          <div className="hidden md:flex gap-12 items-center">
            <button
              onClick={() => scrollToSection("portfolio")}
              className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                }`}
            >
              {t('nav.portfolio')}
            </button>
            <button
              onClick={() => scrollToSection("about")}
              className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                }`}
            >
              {t('nav.about')}
            </button>
            <button
              onClick={() => scrollToSection("contact")}
              className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                }`}
            >
              {t('nav.contact')}
            </button>

            {/* Role-based navigation links */}
            {isAuthenticated && user?.["https://vladtech.com/roles"]?.includes("Admin") && (
              <button
                onClick={() => navigate("/admin")}
                className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                  }`}
              >
                {t('nav.adminPanel')}
              </button>
            )}

            {isAuthenticated && user?.["https://vladtech.com/roles"]?.includes("Employee") && (
              <button
                onClick={() => navigate("/employee")}
                className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                  }`}
              >
                {t('nav.employeeTools')}
              </button>
            )}

          {/* {isAuthenticated && user?.["https://vladtech.com/roles"]?.includes("Client") && (
            <button
            onClick={() => navigate("/client")}
            className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
              }`}
            >
            CLIENT AREA
            </button>
          )} */}

            {isAuthenticated && !user?.["https://vladtech.com/roles"]?.includes("Admin") && (
              <button
                onClick={() => navigate("/dashboard")}
                className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                  }`}
              >
                {t('nav.dashboard')}
              </button>
            )}

            {/* Role badge */}
            {isAuthenticated && (isAdmin || isEmployee || isClient) && (
              <span
                className={`px-3 py-1 text-xs uppercase tracking-wider border rounded-full ${isNavbarDark ? "border-yellow-400 text-yellow-300" : "border-black/30 text-black/70"
                  }`}
              >
                {[
                  isAdmin ? "Admin" : null,
                  isEmployee ? "Employee" : null,
                  isClient ? "Client" : null,
                ]
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
                className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm ${isNavbarDark
                  ? "bg-white text-black hover:bg-yellow-400"
                  : "bg-black text-white hover:bg-yellow-400 hover:text-black"
                  }`}
              >
                <LogIn className="h-4 w-4" />
                {t('nav.login')}
              </button>
            ) : (
              <button
                onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
                className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm ${isNavbarDark
                  ? "bg-white text-black hover:bg-yellow-400"
                  : "bg-black text-white hover:bg-yellow-400 hover:text-black"
                  }`}
              >
                <LogOut className="h-4 w-4" />
                {t('nav.logout')}
              </button>
            )}
          </div>
        </div>

        {/* Mobile Menu Dropdown */}
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{
            opacity: isMobileMenuOpen ? 1 : 0,
            height: isMobileMenuOpen ? "auto" : 0
          }}
          transition={{ duration: 0.3, ease: "easeInOut" }}
          className={`md:hidden border-t overflow-hidden ${isNavbarDark ? "border-white/10 bg-black/95" : "border-black/10 bg-white/95"
            } backdrop-blur-sm`}
        >
          <div className="container mx-auto px-8 py-4 flex flex-col gap-4">
            <button
              onClick={() => {
                scrollToSection("portfolio");
                setIsMobileMenuOpen(false);
              }}
              className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                }`}
            >
              {t('nav.portfolio')}
            </button>
            <button
              onClick={() => {
                scrollToSection("about");
                setIsMobileMenuOpen(false);
              }}
              className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                }`}
            >
              {t('nav.about')}
            </button>
            <button
              onClick={() => {
                scrollToSection("contact");
                setIsMobileMenuOpen(false);
              }}
              className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                }`}
            >
              {t('nav.contact')}
            </button>

            {/* Role-based navigation - Mobile */}
            {isAuthenticated && user?.["https://vladtech.com/roles"]?.includes("Admin") && (
              <button
                onClick={() => {
                  navigate("/admin");
                  setIsMobileMenuOpen(false);
                }}
                className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                  }`}
              >
                {t('nav.adminPanel')}
              </button>
            )}

            {isAuthenticated && user?.["https://vladtech.com/roles"]?.includes("Employee") && (
              <button
                onClick={() => {
                  navigate("/employee");
                  setIsMobileMenuOpen(false);
                }}
                className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                  }`}
              >
                {t('nav.employeeTools')}
              </button>
            )}

            {/*isAuthenticated && user?.["https://vladtech.com/roles"]?.includes("Client") && (
              <button
                onClick={() => {
                  navigate("/client");
                  setIsMobileMenuOpen(false);
                }}
                className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                  }`}
              >
                CLIENT AREA
              </button>
            )*/}

            {isAuthenticated && !user?.["https://vladtech.com/roles"]?.includes("Admin") && (
              <button
                onClick={() => {
                  navigate("/dashboard");
                  setIsMobileMenuOpen(false);
                }}
                className={`text-left hover:text-yellow-400 transition-colors text-sm tracking-wider ${isNavbarDark ? "text-white" : "text-black"
                  }`}
              >
                {t('nav.dashboard')}
              </button>
            )}

            {!isAuthenticated ? (
              <button
                onClick={() => {
                  loginWithRedirect();
                  setIsMobileMenuOpen(false);
                }}
                className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm justify-center ${isNavbarDark
                  ? "bg-white text-black hover:bg-yellow-400"
                  : "bg-black text-white hover:bg-yellow-400 hover:text-black"
                  }`}
              >
                <LogIn className="h-4 w-4" />
                {t('nav.login')}
              </button>
            ) : (
              <button
                onClick={() => {
                  logout({ logoutParams: { returnTo: window.location.origin } });
                  setIsMobileMenuOpen(false);
                }}
                className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm justify-center ${isNavbarDark
                  ? "bg-white text-black hover:bg-yellow-400"
                  : "bg-black text-white hover:bg-yellow-400 hover:text-black"
                  }`}
              >
                <LogOut className="h-4 w-4" />
                {t('nav.logout')}
              </button>
            )}
          </div>
        </motion.div>
      </nav>

      {/* Hero Section - Editorial Style */}
      <div className="relative min-h-screen bg-white flex items-center justify-center overflow-hidden pt-20">
        {/* Decorative Dots */}
        <motion.div
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ delay: 0.2, duration: 0.5 }}
          className="absolute top-32 left-12 w-4 h-4 bg-yellow-400 rounded-full"
        ></motion.div>
        <motion.div
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ delay: 0.4, duration: 0.5 }}
          className="absolute top-40 right-16 w-4 h-4 bg-yellow-400 rounded-full"
        ></motion.div>
        <motion.div
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ delay: 0.6, duration: 0.5 }}
          className="absolute bottom-32 right-32 w-4 h-4 bg-yellow-400 rounded-full"
        ></motion.div>

        {/* Small Text Blocks */}
        <motion.div
          initial={{ opacity: 0, x: -50 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.3, duration: 0.8 }}
          className="absolute top-32 left-24 max-w-xs hidden md:block"
        >
          <p className="text-sm text-black/60 leading-relaxed">
            {t('home.expertiseText')}
          </p>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, x: 50 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.3, duration: 0.8 }}
          className="absolute top-32 right-24 max-w-xs text-right hidden md:block"
        >
          <p className="text-sm text-black/60 leading-relaxed">
            {t('home.executionText')}
          </p>
        </motion.div>

        {/* Main Content */}
        <div className="container mx-auto px-8 relative">
          <div className="relative flex items-center justify-center">
            {/* Sliding VLADTECH Text - Continuous Marquee */}
            <div className="absolute inset-0 flex items-center justify-center overflow-hidden select-none">
              <motion.div
                className="flex whitespace-nowrap"
                animate={{ x: ["0%", "50%"] }}
                transition={{
                  duration: 90,
                  ease: "linear",
                  repeat: Infinity,
                }}
              >
                <h1 className="text-[180px] md:text-[240px] lg:text-[320px] tracking-tighter text-yellow-400 leading-none mr-40">
                  VLADTECH
                </h1>
                <h1 className="text-[180px] md:text-[240px] lg:text-[320px] tracking-tighter text-yellow-400 leading-none mr-40">
                  VLADTECH
                </h1>
                <h1 className="text-[180px] md:text-[240px] lg:text-[320px] tracking-tighter text-yellow-400 leading-none mr-40">
                  VLADTECH
                </h1>

                <h1 className="text-[180px] md:text-[240px] lg:text-[320px] tracking-tighter text-yellow-400 leading-none mr-40">
                  VLADTECH
                </h1>
                <h1 className="text-[180px] md:text-[240px] lg:text-[320px] tracking-tighter text-yellow-400 leading-none mr-40">
                  VLADTECH
                </h1>
                <h1 className="text-[180px] md:text-[240px] lg:text-[320px] tracking-tighter text-yellow-400 leading-none mr-40">
                  VLADTECH
                </h1>
              </motion.div>
            </div>



            {/* Overlaid Image */}
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.5, duration: 1.6 }}
              className="relative z-10"
            >
              <div className="w-64 h-80 md:w-80 md:h-96 relative">
                <img
                  src="https://images.unsplash.com/photo-1681216868987-b7268753b81c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtb2Rlcm4lMjBhcmNoaXRlY3R1cmUlMjBidWlsZGluZ3xlbnwxfHx8fDE3NjE5NjgwMjZ8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
                  alt="Modern Architecture"
                  className="w-full h-full object-cover rounded-2xl shadow-2xl"
                />
              </div>
            </motion.div>
          </div>

          {/* Slogan */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.8, duration: 0.6 }}
            className="text-center mt-8"
          >
            <p className="text-xl md:text-2xl text-black/70 tracking-wide">
              {t('home.yourIdeas')}
            </p>
            {publicMessage && (
              <p className="text-lg md:text-xl text-yellow-400 tracking-wide mt-4 font-semibold">
                {publicMessage}
              </p>
            )}
          </motion.div>

          {/* CTA Buttons with Hover Raise Effect */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 1, duration: 0.6 }}
            className="flex gap-4 justify-center mt-12"
          >
            <motion.div whileHover={{ y: -8 }} transition={{ duration: 0.2 }}>
              <Button
                onClick={() => navigate("/portfolio")}
                className="bg-black text-white hover:bg-yellow-400 hover:text-black px-8 py-6 text-sm tracking-wider transition-all shadow-lg"
              >
                {t('home.portfolio')}
              </Button>
            </motion.div>
            <motion.div whileHover={{ y: -8 }} transition={{ duration: 0.2 }}>
              <Button
                onClick={onOpenEstimateModal}
                className="bg-yellow-400 text-black hover:bg-black hover:text-white px-8 py-6 text-sm tracking-wider transition-all shadow-lg"
              >
                {t('home.createEstimate')}
              </Button>
            </motion.div>
          </motion.div>
        </div>
      </div>

      {/* Portfolio Section */}
      <motion.div
        id="portfolio"
        initial={{ opacity: 0 }}
        animate={{ opacity: isRevealed ? 1 : 0 }}
        transition={{ duration: 0.8 }}
        className="py-32 bg-black relative"
      >
        <div className="container mx-auto px-8">
          <div className="flex items-center justify-between mb-12">
            <h2 className="text-6xl md:text-7xl text-white tracking-tight">{t('home.featuredWork')}</h2>
            <button
              onClick={() => navigate("/portfolio")}
              className="text-white hover:text-yellow-400 tracking-wider transition-colors"
            >
              {t('home.viewGallery')} →
            </button>
          </div>



          {/* Carousel - 3 Images Side by Side with Fade Animation */}
          <div className="relative max-w-7xl mx-auto h-[400px] md:h-[500px]">
            {[0, 1, 2].map((slideIndex) => (
              <motion.div
                key={slideIndex}
                initial={{ opacity: 0 }}
                animate={{ opacity: currentSlide === slideIndex ? 1 : 0 }}
                transition={{ duration: 1, ease: "easeInOut" }}
                className="absolute inset-0 grid grid-cols-3 gap-0"
                style={{ pointerEvents: currentSlide === slideIndex ? "auto" : "none" }}
              >
                {portfolioImages
                  .slice(slideIndex * 3, slideIndex * 3 + 3)
                  .map((image) => (
                    <div key={image.id} className="relative group overflow-hidden">
                      <img
                        src={getImageUrl(image.url)}
                        alt={image.title}
                        className="w-full h-full object-cover"
                      />
                    </div>
                  ))}
              </motion.div>
            ))}

            {/* Slide Indicators */}
            <div className="absolute bottom-6 left-1/2 -translate-x-1/2 flex gap-2 z-10">
              {[0, 1, 2].map((index) => (
                <button
                  key={index}
                  onClick={() => setCurrentSlide(index)}
                  className={`w-2 h-2 rounded-full transition-all ${currentSlide === index ? "bg-yellow-400 w-8" : "bg-white/40"
                    }`}
                />
              ))}
            </div>
          </div>
        </div>
      </motion.div>

      {/* About Section with Scroll Reveal */}
      <div id="about" className="py-32 bg-white">
        <div className="container mx-auto px-8">
          <div className="max-w-5xl mx-auto">
            <motion.h2
              initial={{ opacity: 0, y: 50 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.3 }}
              transition={{ duration: 0.8 }}
              className="text-center text-6xl md:text-7xl text-black mb-20 tracking-tight"
            >
              {t('home.about')}
            </motion.h2>
            <div className="grid md:grid-cols-2 gap-16 items-center">
              <motion.div
                initial={{ opacity: 0, x: -50 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true, amount: 0.3 }}
                transition={{ duration: 0.8, delay: 0.2 }}
              >
                <p className="text-lg text-black/70 mb-6 leading-relaxed">
                  {t('home.aboutParagraph1')}
                </p>
                <p className="text-lg text-black/70 mb-8 leading-relaxed">
                  {t('home.aboutParagraph2')}
                </p>
                <div className="grid grid-cols-3 gap-8 mt-12">
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.4, duration: 0.5 }}
                    className="text-center"
                  >
                    <div className="text-5xl text-yellow-400 mb-2">
                      {projectCount !== null ? `${projectCount}+` : "…"}
                    </div>
                    <div className="text-sm text-black/60 tracking-wide">{t('home.projectsCompleted')}</div>
                  </motion.div>
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.5, duration: 0.5 }}
                    className="text-center"
                  >
                    <div className="text-5xl text-yellow-400 mb-2">{ageValue}</div>
                    <div className="text-sm text-black/60 tracking-wide">{ageUnit === 'MONTHS' ? t('home.months') : ageUnit === 'YEAR' ? t('home.years') : t('home.years')}</div>
                  </motion.div>
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.6, duration: 0.5 }}
                    className="text-center"
                  >
                    <div className="text-5xl text-yellow-400 mb-2">
                      {satisfactionPercentage !== null ? `${Math.round(satisfactionPercentage)}%` : "…"}
                    </div>
                    <div className="text-sm text-black/60 tracking-wide">{t('home.satisfactionRate')}</div>
                  </motion.div>
                </div>
              </motion.div>
              <motion.div
                initial={{ opacity: 0, x: 50 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true, amount: 0.3 }}
                transition={{ duration: 0.8, delay: 0.2 }}
                className="relative"
              >
                <div className="aspect-square bg-yellow-400 rounded-2xl shadow-2xl flex items-center justify-center">
                  <div className="text-black text-8xl tracking-tighter opacity-20">W</div>
                </div>
              </motion.div>
            </div>
          </div>
        </div>
      </div>

      {/* Contact Section - Liquid Glass Apple Style with Scroll Reveal */}
      <div
        id="contact"
        className="py-32 bg-gradient-to-b from-black via-gray-900 to-black relative overflow-hidden"
      >
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-yellow-400/10 rounded-full blur-3xl"></div>
          <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-white/5 rounded-full blur-3xl"></div>
        </div>

        <div className="container mx-auto px-8 relative z-10">
          <div className="max-w-2xl mx-auto text-center">
            <motion.h2
              initial={{ opacity: 0, y: 50 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.3 }}
              transition={{ duration: 0.8 }}
              className="text-center text-6xl md:text-7xl text-white mb-4 tracking-tight"
            >
              {t('home.getInTouch')}
            </motion.h2>
            <motion.p
              initial={{ opacity: 0 }}
              whileInView={{ opacity: 1 }}
              viewport={{ once: true }}
              transition={{ delay: 0.2, duration: 0.6 }}
              className="text-center text-gray-400 mb-20 text-lg tracking-wide"
            >
              {t('home.readyToStart')}
            </motion.p>

            <motion.div
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.3 }}
              transition={{ duration: 0.8, delay: 0.3 }}
            >
              <Button
                onClick={onOpenContactModal}
                className="w-full max-w-md mx-auto bg-gradient-to-r from-yellow-400 to-yellow-500 hover:from-yellow-500 hover:to-yellow-600 text-black h-14 tracking-widest transition-all duration-300 shadow-lg shadow-yellow-400/20"
              >
                <Send className="mr-3 h-5 w-5" />
                {t('home.contactUs')}
              </Button>
            </motion.div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="bg-white border-t border-black/10 py-12">
        <div className="container mx-auto px-8 text-center">
          <p className="text-2xl mb-2 text-black tracking-widest">VLADTECH</p>
          <p className="text-black/40 tracking-wide">{t('home.allRights')}</p>
          <p className="text-black/30 text-sm mt-2 tracking-wide">Your Ideas. We Realize.</p>
        </div>
      </footer>
    </div>
  );
}
