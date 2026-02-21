import { useState, useEffect } from "react";
import { Button } from "../components/button.js";
import { Send } from "lucide-react";
import { motion } from "motion/react";
import { useAuth0 } from "@auth0/auth0-react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import getImageUrl from "../utils/getImageUrl.js";
import { api } from "../api/http.js";
import { useLanguage } from "../context/LanguageContext";

// ✅ USE THE SHARED NAVBAR (NO LOCAL <nav> in this page)
import Navbar from "../components/Navbar.jsx";

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
    url: "https://images.unsplash.com/photo-1618385455730-2571c38966b7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHxjb25zdHJ1Y3Rpb24lMjB0ZWNobm9sb2d5fGVufDF8fHx8MTc2MjE1MjYyNXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Construction Tech",
  },
  {
    id: 3,
    url: "https://images.unsplash.com/photo-1629132497808-1c55ac45c4da?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHxlbmdpbmVlcmluZyUyMHByb2plY3R8ZW58MXx8fHwxNzYyMjAwMTUxfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Engineering Complex",
  },
  {
    id: 4,
    url: "https://images.unsplash.com/photo-1612272362018-4c4084eebdd4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHxnbGFzcyUyMHNreXNjcmFwZXJ8ZW58MXx8fHwxNzYyMjI3MDEzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Glass Skyscraper",
  },
  {
    id: 5,
    url: "https://images.unsplash.com/photo-1572061971745-063e9cc83afc?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHxtb2Rlcm4lMjBjb25zdHJ1Y3Rpb258ZW58MXx8fHwxNzYyMTU1Mjk2fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Modern Construction",
  },
  {
    id: 6,
    url: "https://images.unsplash.com/photo-1523477593243-78bbf626fd3b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHxidWlsZGluZyUyMGZhY2FkZXxlbnwxfHx8fDE3NjIyMjcwMTR8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Building Facade",
  },
  {
    id: 7,
    url: "https://images.unsplash.com/photo-1554793000-245d3a3c2a51?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHx1cmJhbiUyMGFyY2hpdGVjdHVyZXxlbnwxfHx8fDE3NjIxNjY3MDB8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Urban Architecture",
  },
  {
    id: 8,
    url: "https://images.unsplash.com/photo-1720762256650-ea429f429226?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHxjb25jcmV0ZSUyMGJ1aWxkaW5nfGVufDF8fHx8MTc2MjIyNzAxNXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Concrete Building",
  },
  {
    id: 9,
    url: "https://images.unsplash.com/photo-1609627016501-b862497c7294?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHxzdGVlbCUyMHN0cnVjdHVyZXxlbnwxfHx8fDE3NjIyMjcwMTV8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    title: "Steel Structure",
  },
];

export default function HomePage({
  onNavigate,
  onOpenContactModal,
  onOpenEstimateModal,
}: HomePageProps) {
  const { loginWithRedirect, logout, isAuthenticated, user, getAccessTokenSilently } = useAuth0();
  const { language, toggleLanguage } = useLanguage();
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [currentSlide, setCurrentSlide] = useState(0);
  const [isRevealed, setIsRevealed] = useState(false);
  const [isNavbarDark, setIsNavbarDark] = useState(false);

  const [token, setToken] = useState<string>("");

  useEffect(() => {
    if (isAuthenticated) {
      getAccessTokenSilently({
        authorizationParams: { audience: "https://vladtech/api" },
      }).then((tok) => setToken(tok));
    }
  }, [isAuthenticated, getAccessTokenSilently]);

  // Dynamic stats
  const [projectCount, setProjectCount] = useState<number | null>(null);
  const [ageValue, setAgeValue] = useState<string>("10+");
  const [ageUnit, setAgeUnit] = useState<string>("MONTHS");
  const [satisfactionPercentage, setSatisfactionPercentage] = useState<number | null>(null);

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

  useEffect(() => {
    const fetchSatisfactionPercentage = async () => {
      try {
        const response = await api.get("/reviews/satisfaction-percentage");
        setSatisfactionPercentage(response.data);
      } catch (error) {
        console.error("Failed to fetch satisfaction percentage:", error);
        setSatisfactionPercentage(98);
      }
    };
    fetchSatisfactionPercentage();
  }, []);

 

  const scrollToSection = (sectionId: string) => {
    const element = document.getElementById(sectionId);
    element?.scrollIntoView({ behavior: "smooth" });
  };

  // Auto carousel
  useEffect(() => {
    setIsRevealed(true);
    const interval = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % 3);
    }, 4000);
    return () => clearInterval(interval);
  }, []);

  // Navbar color change on scroll
  useEffect(() => {
    const handleScroll = () => {
      const portfolioSection = document.getElementById("portfolio");
      if (!portfolioSection) return;

      const rect = portfolioSection.getBoundingClientRect();
      setIsNavbarDark(rect.top <= 100);
    };

    window.addEventListener("scroll", handleScroll);
    handleScroll(); // init
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // Compute company age
  useEffect(() => {
    const foundingDate = new Date("2026-01-01"); // adjust if needed
    const now = new Date();

    const yearsDiff = now.getFullYear() - foundingDate.getFullYear();
    const monthsDiff = now.getMonth() - foundingDate.getMonth();
    const totalMonths = yearsDiff * 12 + monthsDiff;

    if (totalMonths < 12) {
      const displayMonths = Math.max(totalMonths, 1);
      setAgeValue(`${displayMonths}+`);
      setAgeUnit("MONTHS");
    } else {
      const years = Math.floor(totalMonths / 12);
      setAgeValue(`${years}+`);
      setAgeUnit(years === 1 ? "YEAR" : "YEARS");
    }
  }, []);

  return (
    <div className="min-h-screen bg-white">
      {/* ✅ THE ONLY NAVBAR ON THIS PAGE */}
      <Navbar
        isNavbarDark={isNavbarDark}
        showHomeLinks={true}
        onScrollToSection={scrollToSection}
      />

      <main>
      {/* Hero Section */}
      <div className="relative min-h-screen bg-white flex items-center justify-center overflow-hidden pt-20">
        {/* Decorative Dots */}
        <motion.div
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ delay: 0.2, duration: 0.5 }}
          className="absolute top-32 left-12 w-4 h-4 bg-yellow-400 rounded-full"
        />
        <motion.div
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ delay: 0.4, duration: 0.5 }}
          className="absolute top-40 right-16 w-4 h-4 bg-yellow-400 rounded-full"
        />
        <motion.div
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ delay: 0.6, duration: 0.5 }}
          className="absolute bottom-32 right-32 w-4 h-4 bg-yellow-400 rounded-full"
        />

        {/* Side text blocks */}
        <motion.div
          initial={{ opacity: 0, x: -50 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.3, duration: 0.8 }}
          className="absolute top-32 left-24 max-w-xs hidden md:block"
        >
          <p className="text-sm text-black/60 leading-relaxed">{t("home.expertiseText")}</p>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, x: 50 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.3, duration: 0.8 }}
          className="absolute top-32 right-24 max-w-xs text-right hidden md:block"
        >
          <p className="text-sm text-black/60 leading-relaxed">{t("home.executionText")}</p>
        </motion.div>

        {/* Main Content */}
        <div className="container mx-auto px-8 relative">
          <div className="relative flex items-center justify-center">
            {/* Sliding VLADTECH Text */}
            <div className="absolute inset-0 flex items-center justify-center overflow-hidden select-none" aria-hidden="true" role="presentation">
              <motion.div
                className="flex whitespace-nowrap"
                animate={{ x: ["0%", "50%"] }}
                transition={{ duration: 90, ease: "linear", repeat: Infinity }}
              >
                {Array.from({ length: 6 }).map((_, i) => (
                  <span
                    key={i}
                    className="text-[180px] md:text-[240px] lg:text-[320px] tracking-tighter text-yellow-400 leading-none mr-40"
                  >
                    VLADTECH
                  </span>
                ))}
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
              {t("home.yourIdeas")}
            </p>

          </motion.div>

          {/* CTA Buttons */}
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
                {t("home.portfolio")}
              </Button>
            </motion.div>

            <motion.div whileHover={{ y: -8 }} transition={{ duration: 0.2 }}>
              <Button
                onClick={onOpenEstimateModal}
                className="bg-yellow-400 text-black hover:bg-black hover:text-white px-8 py-6 text-sm tracking-wider transition-all shadow-lg"
              >
                {t("home.createEstimate")}
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
            <h2 className="text-6xl md:text-7xl text-white tracking-tight">
              {t("home.featuredWork")}
            </h2>
            <button
              onClick={() => navigate("/portfolio")}
              className="text-white hover:text-yellow-400 tracking-wider transition-colors"
            >
              {t("home.viewGallery")} →
            </button>
          </div>

          <div className="relative max-w-7xl mx-auto h-[400px] md:h-[500px]">
            {[0, 1, 2].map((slideIndex) => (
              <motion.div
                key={slideIndex}
                initial={{ opacity: 0 }}
                animate={{ opacity: currentSlide === slideIndex ? 1 : 0 }}
                transition={{ duration: 1, ease: "easeInOut" }}
                className="absolute inset-0 grid grid-cols-3 gap-0"
                style={{ pointerEvents: currentSlide === slideIndex ? "auto" : "none" }}
                aria-hidden={currentSlide !== slideIndex}
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

            <div className="absolute bottom-6 left-1/2 -translate-x-1/2 flex gap-2 z-10" role="group" aria-label="Portfolio slides">
              {[0, 1, 2].map((index) => (
                <button
                  key={index}
                  onClick={() => setCurrentSlide(index)}
                  aria-current={currentSlide === index ? "true" : undefined}
                  aria-label={`Go to slide ${index + 1}`}
                  className={`w-2 h-2 rounded-full transition-all ${
                    currentSlide === index ? "bg-yellow-400 w-8" : "bg-white/40"
                  }`}
                />
              ))}
            </div>
          </div>
        </div>
      </motion.div>

      {/* About Section */}
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
              {t("home.about")}
            </motion.h2>

            <div className="grid md:grid-cols-2 gap-16 items-center">
              <motion.div
                initial={{ opacity: 0, x: -50 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true, amount: 0.3 }}
                transition={{ duration: 0.8, delay: 0.2 }}
              >
                <p className="text-lg text-black/70 mb-6 leading-relaxed">
                  {t("home.aboutParagraph1")}
                </p>
                <p className="text-lg text-black/70 mb-8 leading-relaxed">
                  {t("home.aboutParagraph2")}
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
                    <div className="text-sm text-black/60 tracking-wide">
                      {t("home.projectsCompleted")}
                    </div>
                  </motion.div>

                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.5, duration: 0.5 }}
                    className="text-center"
                  >
                    <div className="text-5xl text-yellow-400 mb-2">{ageValue}</div>
                    <div className="text-sm text-black/60 tracking-wide">
                      {ageUnit === "MONTHS" ? t("home.months") : t("home.years")}
                    </div>
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
                    <div className="text-sm text-black/60 tracking-wide">
                      {t("home.satisfactionRate")}
                    </div>
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
                <div className="aspect-square rounded-2xl shadow-2xl overflow-hidden bg-white flex items-center justify-center">
                  <p className="text-9xl md:text-[140px] font-bold text-yellow-400 tracking-tighter">VT</p>
                </div>
              </motion.div>
            </div>
          </div>
        </div>
      </div>

      {/* Contact */}
      <div
        id="contact"
        className="py-32 bg-gradient-to-b from-black via-gray-900 to-black relative overflow-hidden"
      >
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-yellow-400/10 rounded-full blur-3xl" />
          <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-white/5 rounded-full blur-3xl" />
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
              {t("home.getInTouch")}
            </motion.h2>

            <motion.p
              initial={{ opacity: 0 }}
              whileInView={{ opacity: 1 }}
              viewport={{ once: true }}
              transition={{ delay: 0.2, duration: 0.6 }}
              className="text-center text-gray-400 mb-20 text-lg tracking-wide"
            >
              {t("home.readyToStart")}
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
                <Send className="mr-3 h-5 w-5" aria-hidden="true" />
                {t("home.contactUs")}
              </Button>
            </motion.div>
          </div>
        </div>
      </div>

      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-black/10 py-12">
        <div className="container mx-auto px-8 text-center">
          <p className="text-2xl mb-2 text-black tracking-widest">VLADTECH</p>
          <p className="text-black/40 tracking-wide">{t("home.allRights")}</p>
          <p className="text-black/30 text-sm mt-2 tracking-wide">Your Ideas. We Realize.</p>
        </div>
      </footer>
    </div>
  );
}
