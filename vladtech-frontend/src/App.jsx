import {BrowserRouter as Router, Routes, Route, useLocation, useNavigate} from "react-router-dom";
import Auth0ProviderWithConfig from "./auth/Auth0ProviderWithConfig";

import Home from "./pages/Home";
import Dashboard from "./pages/Dashboard";
import Admin from "./pages/Admin";
import Employee from "./pages/Employee";
import Client from "./pages/Client";
import Navbar from "./components/Navbar";
import ProtectedRoute from "./auth/ProtectedRoute";
import ReviewsPage from "./pages/Reviews.jsx";
import PortfolioGallery from "./pages/PortfolioGallery";

function Layout({ children }) {
  const location = useLocation();
  const isHomePage = location.pathname === "/";
  const isPortfolioPage = location.pathname === "/portfolio";
  const isReviewsPage = location.pathname === "/reviews";
  const navigate = useNavigate();

  return (
      <Auth0ProviderWithConfig navigate={navigate}>
      {!isHomePage && !isPortfolioPage && !isReviewsPage && <Navbar />}
      {children}
      </Auth0ProviderWithConfig>
  );
}

function App() {
  return (
      <Router>
        <Layout>
      <Routes>
        {/* Public Route */}
        <Route path="/" element={<Home />} />

        {/* Any authenticated user */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        {/* Admin-only */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute roles={["Admin"]}>
              <Admin />
            </ProtectedRoute>
          }
        />

        {/* Employee-only */}
        <Route
          path="/employee"
          element={
            <ProtectedRoute roles={["Employee"]}>
              <Employee />
            </ProtectedRoute>
          }
        />

        {/* Client-only */}
        <Route
          path="/client"
          element={
            <ProtectedRoute roles={["Client"]}>
              <Client />
            </ProtectedRoute>
          }
        />
        <Route
            path="/reviews"
            element={<ReviewsPage />}
        />
        <Route
            path="/portfolio"
            element={<PortfolioGallery />}
        />
      </Routes>
        </Layout>
    </Router>
  );
}

export default App;
