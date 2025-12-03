import { useState, useEffect } from "react";
import { Button } from "../components/button";
import { Input } from "../components/input";
import { Textarea } from "../components/textarea";
import { Label } from "../components/label";
// Removed ImageWithFallback import
import { Send, LogIn } from "lucide-react";
import { motion } from "motion/react";

interface HomePageProps {
  onNavigate: (page: string) => void;
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

export default function HomePage({ onNavigate }: HomePageProps) {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    message: "",
  });
  const [currentSlide, setCurrentSlide] = useState(0);
  const [isRevealed, setIsRevealed] = useState(false);
  const [isNavbarDark, setIsNavbarDark] = useState(false);

  const scrollToSection = (sectionId: string) => {
    const element = document.getElementById(sectionId);
    element?.scrollIntoView({ behavior: "smooth" });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Form submitted:", formData);
    alert("Thank you for your message! We'll get back to you soon.");
    setFormData({ name: "", email: "", message: "" });
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

  // Get 3 images for current slide
  const getCurrentImages = () => {
    const startIndex = currentSlide * 3;
    return portfolioImages.slice(startIndex, startIndex + 3);
  };

  return (
    <div className="min-h-screen bg-white">
      {/* Navigation Bar - Changes to dark on scroll */}
      <nav className={`fixed top-0 left-0 right-0 z-50 backdrop-blur-sm border-b transition-all duration-300 ${
        isNavbarDark 
          ? "bg-black/95 border-white/10" 
          : "bg-white/95 border-black/10"
      }`}>
        <div className="container mx-auto px-8 py-6 flex justify-between items-center">
          <div className={`tracking-widest transition-colors ${
            isNavbarDark ? "text-white" : "text-black"
          }`}>VLADTECH</div>
          <div className="flex gap-12 items-center">
            <button
              onClick={() => scrollToSection("portfolio")}
              className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                isNavbarDark ? "text-white" : "text-black"
              }`}
            >
              PORTFOLIO
            </button>
            <button
              onClick={() => scrollToSection("about")}
              className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                isNavbarDark ? "text-white" : "text-black"
              }`}
            >
              ABOUT
            </button>
            <button
              onClick={() => scrollToSection("contact")}
              className={`hover:text-yellow-400 transition-colors text-sm tracking-wider ${
                isNavbarDark ? "text-white" : "text-black"
              }`}
            >
              CONTACT
            </button>
            <button
              onClick={() => alert("Login functionality coming soon!")}
              className={`flex items-center gap-2 transition-all px-6 py-2 tracking-wider text-sm ${
                isNavbarDark 
                  ? "bg-white text-black hover:bg-yellow-400" 
                  : "bg-black text-white hover:bg-yellow-400 hover:text-black"
              }`}
            >
              <LogIn className="h-4 w-4" />
              LOGIN
            </button>
          </div>
        </div>
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
          className="absolute top-32 left-24 max-w-xs"
        >
          <p className="text-sm text-black/60 leading-relaxed">
            Based in the heart of innovation—with expertise spanning construction, 
            engineering, and technology integration for tomorrow's infrastructure.
          </p>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, x: 50 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.3, duration: 0.8 }}
          className="absolute top-32 right-24 max-w-xs text-right"
        >
          <p className="text-sm text-black/60 leading-relaxed">
            From initial idea to final execution, we work with you to craft 
            solutions that stand the test of time. We develop. We get it done—really.
          </p>
        </motion.div>

        {/* Main Content */}
        <div className="container mx-auto px-8 relative">
          <div className="relative flex items-center justify-center">
            {/* Sliding VLADTECH Text - Continuous Marquee */}
            <div className="absolute inset-0 flex items-center justify-center overflow-hidden">
              <motion.div
                animate={{ x: ["-100%", "0%"] }}
                transition={{
                  duration: 20,
                  repeat: Infinity,
                  ease: "linear",
                }}
                className="flex whitespace-nowrap"
              >
                <h1 className="text-[180px] md:text-[240px] lg:text-[320px] tracking-tighter text-yellow-400 select-none leading-none mr-20">
                  VLADTECH
                </h1>
                <h1 className="text-[180px] md:text-[240px] lg:text-[320px] tracking-tighter text-yellow-400 select-none leading-none mr-20">
                  VLADTECH
                </h1>
                <h1 className="text-[180px] md:text-[240px] lg:text-[320px] tracking-tighter text-yellow-400 select-none leading-none">
                  VLADTECH
                </h1>
              </motion.div>
            </div>
            
            {/* Overlaid Image */}
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.5, duration: 0.8 }}
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
              Your Ideas. We Realize.
            </p>
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
                onClick={() => onNavigate("calendar")}
                className="bg-black text-white hover:bg-yellow-400 hover:text-black px-8 py-6 text-sm tracking-wider transition-all shadow-lg"
              >
                PORTFOLIO
              </Button>
            </motion.div>
            <motion.div whileHover={{ y: -8 }} transition={{ duration: 0.2 }}>
              <Button
                onClick={() => onNavigate("estimations")}
                className="bg-yellow-400 text-black hover:bg-black hover:text-white px-8 py-6 text-sm tracking-wider transition-all shadow-lg"
              >
                CREATE ESTIMATE
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
              PORTFOLIO
            </h2>
            <button
              onClick={() => onNavigate("portfolio")}
              className="text-white hover:text-yellow-400 tracking-wider transition-colors"
            >
              VIEW ALL →
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
                {portfolioImages.slice(slideIndex * 3, slideIndex * 3 + 3).map((image) => (
                  <div key={image.id} className="relative group overflow-hidden">
                    <img
                      src={image.url}
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
                  className={`w-2 h-2 rounded-full transition-all ${
                    currentSlide === index ? "bg-yellow-400 w-8" : "bg-white/40"
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
              ABOUT
            </motion.h2>
            <div className="grid md:grid-cols-2 gap-16 items-center">
              <motion.div
                initial={{ opacity: 0, x: -50 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true, amount: 0.3 }}
                transition={{ duration: 0.8, delay: 0.2 }}
              >
                <p className="text-lg text-black/70 mb-6 leading-relaxed">
                  At VLADTECH, we're committed to transforming your vision into reality. With decades 
                  of combined experience in construction, engineering, and technology integration, 
                  our team delivers excellence in every project.
                </p>
                <p className="text-lg text-black/70 mb-8 leading-relaxed">
                  We believe in innovation, quality, and building lasting relationships with our clients. 
                  From concept to completion, we're with you every step of the way.
                </p>
                <div className="grid grid-cols-3 gap-8 mt-12">
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.4, duration: 0.5 }}
                    className="text-center"
                  >
                    <div className="text-5xl text-yellow-400 mb-2">20+</div>
                    <div className="text-sm text-black/60 tracking-wide">PROJECTS</div>
                  </motion.div>
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.5, duration: 0.5 }}
                    className="text-center"
                  >
                    <div className="text-5xl text-yellow-400 mb-2">10+</div>
                    <div className="text-sm text-black/60 tracking-wide">MONTHS</div>
                  </motion.div>
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.6, duration: 0.5 }}
                    className="text-center"
                  >
                    <div className="text-5xl text-yellow-400 mb-2">98%</div>
                    <div className="text-sm text-black/60 tracking-wide">SATISFIED</div>
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
      <div id="contact" className="py-32 bg-gradient-to-b from-black via-gray-900 to-black relative overflow-hidden">
        {/* Ambient Background Effects */}
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-yellow-400/10 rounded-full blur-3xl"></div>
          <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-white/5 rounded-full blur-3xl"></div>
        </div>

        <div className="container mx-auto px-8 relative z-10">
          <div className="max-w-2xl mx-auto">
            <motion.h2
              initial={{ opacity: 0, y: 50 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.3 }}
              transition={{ duration: 0.8 }}
              className="text-center text-6xl md:text-7xl text-white mb-4 tracking-tight"
            >
              GET IN TOUCH
            </motion.h2>
            <motion.p
              initial={{ opacity: 0 }}
              whileInView={{ opacity: 1 }}
              viewport={{ once: true }}
              transition={{ delay: 0.2, duration: 0.6 }}
              className="text-center text-gray-400 mb-20 text-lg tracking-wide"
            >
              Let's build something extraordinary together
            </motion.p>
            
            {/* Contact Form - Glassmorphism */}
            <motion.div
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.3 }}
              transition={{ duration: 0.8, delay: 0.3 }}
              className="relative group"
            >
              <div className="absolute -inset-1 bg-gradient-to-r from-yellow-400/20 via-white/10 to-yellow-400/20 rounded-3xl blur-xl group-hover:blur-2xl transition-all duration-300 opacity-50"></div>
              <div className="relative bg-white/5 backdrop-blur-xl border border-white/10 p-10 rounded-3xl shadow-2xl">
                <form onSubmit={handleSubmit} className="space-y-6">
                  <div className="space-y-3">
                    <Label htmlFor="name" className="text-white/60 tracking-wider text-xs uppercase">Your Name</Label>
                    <Input
                      id="name"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      placeholder="John Doe"
                      required
                      className="bg-white/5 border-white/10 focus:border-yellow-400/50 text-white placeholder:text-white/30 backdrop-blur-xl h-12"
                    />
                  </div>
                  <div className="space-y-3">
                    <Label htmlFor="email" className="text-white/60 tracking-wider text-xs uppercase">Email Address</Label>
                    <Input
                      id="email"
                      type="email"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      placeholder="john@example.com"
                      required
                      className="bg-white/5 border-white/10 focus:border-yellow-400/50 text-white placeholder:text-white/30 backdrop-blur-xl h-12"
                    />
                  </div>
                  <div className="space-y-3">
                    <Label htmlFor="message" className="text-white/60 tracking-wider text-xs uppercase">Message</Label>
                    <Textarea
                      id="message"
                      value={formData.message}
                      onChange={(e) => setFormData({ ...formData, message: e.target.value })}
                      placeholder="Tell us about your vision..."
                      className="min-h-[140px] bg-white/5 border-white/10 focus:border-yellow-400/50 text-white placeholder:text-white/30 backdrop-blur-xl resize-none"
                      required
                    />
                  </div>
                  <Button
                    type="submit"
                    className="w-full bg-gradient-to-r from-yellow-400 to-yellow-500 hover:from-yellow-500 hover:to-yellow-600 text-black h-14 tracking-widest transition-all duration-300 shadow-lg shadow-yellow-400/20"
                  >
                    <Send className="mr-3 h-5 w-5" />
                    SEND MESSAGE
                  </Button>
                </form>
              </div>
            </motion.div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="bg-white border-t border-black/10 py-12">
        <div className="container mx-auto px-8 text-center">
          <p className="text-2xl mb-2 text-black tracking-widest">VLADTECH</p>
          <p className="text-black/40 tracking-wide">© 2025 VLADTECH. All rights reserved.</p>
          <p className="text-black/30 text-sm mt-2 tracking-wide">Your Ideas. We Realize.</p>
        </div>
      </footer>
    </div>
  );
}
