import { BrowserRouter as Router, Routes, Route, useLocation } from "react-router-dom";

import Home from "./pages/Home";
import Dashboard from "./pages/Dashboard";
import Admin from "./pages/Admin";
import Employee from "./pages/Employee";
import Client from "./pages/Client";
import Navbar from "./components/Navbar";
import ProtectedRoute from "./auth/ProtectedRoute";
import ReviewsPage from "./pages/Reviews.jsx";

function Layout({ children }) {
  const location = useLocation();
  const isHomePage = location.pathname === "/";

  return (
    <>
      {!isHomePage && <Navbar />}
      {children}
    </>
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
      </Routes>
        </Layout>
    </Router>

  );
}

export default App;
