import { BrowserRouter as Router, Routes, Route } from "react-router-dom";

import Home from "./pages/Home";
import Dashboard from "./pages/Dashboard";
import Admin from "./pages/Admin";
import Employee from "./pages/Employee";
import Client from "./pages/Client";
import Navbar from "./components/Navbar";
import ProtectedRoute from "./auth/ProtectedRoute";

function App() {
  return (
      <Router>
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
      </Routes>
    </Router>
  );
}

export default App;
