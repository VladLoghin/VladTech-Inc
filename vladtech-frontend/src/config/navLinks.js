export const navLinks = [
  {
    label: "Home",
    path: "/",
    roles: "ANY",      // visible to everyone
  },
  {
    label: "Dashboard",
    path: "/dashboard",
    roles: "AUTH",     // visible to any logged-in user
  },
  {
    label: "Admin Panel",
    path: "/admin",
    roles: ["Admin"],  // only admins
  },
  {
    label: "Employee Tools",
    path: "/employee",
    roles: ["Employee", "Admin"], // allow admin too if you want
  },
  {
    label: "Client Area",
    path: "/client",
    roles: ["Client"],
  },
];
