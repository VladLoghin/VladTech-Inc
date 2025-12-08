# VladTech-Inc

Home Renovation Company

## Overview
VladTech-Inc is a full-stack web application for managing small construction projects (starting from home, office or commercial space renovations), engineering workflows, and client communications. It features a modern React frontend, a robust Spring Boot backend, and secure authentication via Auth0.

## Features
- Dynamic project management and statistics
- Role-based access: Admin, Employee, Client
- Responsive UI with mobile hamburger menu
- Contact Us modal with authentication and animation
- End-to-end tests with Playwright (Chromium & Mobile Safari)
- Modern UI components (Radix UI, lucide-react)
- Backend API for project data

## Technologies
- **Frontend:** React 19, Vite, Tailwind CSS, Radix UI, lucide-react
- **Backend:** Spring Boot (Java)
- **Auth:** Auth0
- **Testing:** Playwright

## Getting Started

### Prerequisites
- Node.js >= 18
- Java 17+ (for backend)
- npm

### Setup
1. **Clone the repo:**
   ```sh
   git clone https://github.com/VladLoghin/VladTech-Inc.git
   cd VladTech-Inc
   ```
2. **Install frontend dependencies:**
   ```sh
   cd vladtech-frontend
   npm install
   ```
3. **Start frontend dev server:**
   ```sh
   npm run dev
   ```
4. **Start backend:**
   ```sh
   cd ..
   ./gradlew bootRun
   ```

### Running Tests
- **End-to-end tests:**
  ```sh
  npx playwright test --headed --reporter=list
  ```
- **Mobile view:**
  ```sh
  npx playwright test --project="Mobile Safari" --headed
  ```

## Project Structure
```
VladTech-Inc/
├── vladtech-frontend/   # React frontend
│   ├── src/
│   ├── tests/
│   └── ...
├── src/                 # Spring Boot backend
│   ├── main/java/org/example/vladtech/
│   └── ...
├── README.md
├── package.json
├── build.gradle
└── ...
```

## Auth Roles
- **Admin:** Full access to all features
- **Employee:** Project management tools
- **Client:** Project dashboard and contact


© 2025 VladTech-Inc. All rights reserved.
