# VladTech-Inc

> A comprehensive full-stack platform for managing home renovation and construction projects with portfolio showcase, client reviews, appointment scheduling, and project estimates.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-19-blue.svg)](https://react.dev/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green.svg)](https://spring.io/projects/spring-boot)

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Architecture](#architecture)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)
- [License](#license)

## Overview

VladTech-Inc is a modern web application designed for home renovation and construction companies to manage their entire workflow from client acquisition to project completion. The platform enables businesses to showcase their portfolio, collect client reviews, schedule appointments, generate project estimates, and manage projects with role-based access control.

Built with a microservices-inspired architecture, the application features a React-based frontend with Tailwind CSS for a responsive design, a Spring Boot backend with MongoDB for data persistence, and Auth0 for secure authentication and authorization.

## Features

### Role-Based Access Control
- **Admin**: Full system access, user management, project calendar, archive management
- **Employee**: Project management, client communication, estimate creation
- **Client**: View portfolio, submit reviews, book appointments, track projects

### Portfolio Management
- Create and showcase renovation projects with multiple images
- Archive portfolio items (soft delete) instead of permanent deletion
- Filter portfolio by project type (Interior, Kitchen, Bathroom, Exterior, etc.)
- Add comments and engage with portfolio items
- Image carousel for viewing multiple project photos
- Pagination for large portfolios (6 items per page)

### Review System
- Clients can submit reviews with ratings (0.0-5.0) and photos
- Admin moderation with visibility controls
- Send reviews to portfolio for public showcase
- Filter reviews by client name, rating, type, and comments
- Customer satisfaction percentage calculation
- Bad word filtering for comment moderation

### Appointment Scheduling
- Interactive calendar for booking appointments
- Auth0-integrated user authentication
- Email notifications via MailHog (development)
- Appointment management for admins and employees

### Estimate Calculator
- Pre-configured presets for common renovation types
- Line-item based estimates with customizable rates
- Project-specific calculators (Roofing, Siding, Kitchen, Bathroom, etc.)
- Tax and discount calculations
- Save and export estimates
- Admin-configurable pricing settings

### Project Management
- Project lifecycle tracking (Planning, In Progress, Completed)
- Project calendar view with date-based filtering
- Project statistics dashboard
- Archive completed projects
- Send projects to portfolio
- Document and image uploads (AWS S3 integration)

### Internationalization
- Multi-language support (English, French)
- Dynamic language switching
- Localized content throughout the application

### Testing
- Comprehensive E2E tests with Playwright
- Backend unit tests with JUnit and Mockito
- Jacoco code coverage reporting
- Multi-browser testing (Chromium, Mobile Safari)

## Tech Stack

### Frontend
- **Framework**: React 19 with Vite
- **Styling**: Tailwind CSS, Radix UI components
- **Icons**: Lucide React
- **HTTP Client**: Axios
- **Authentication**: Auth0 React SDK
- **Calendar**: FullCalendar
- **Animations**: Framer Motion
- **Internationalization**: i18next, react-i18next
- **Testing**: Playwright
- **Build Tool**: Vite

### Backend
- **Framework**: Spring Boot 3.5.7 (Java 17)
- **Database**: MongoDB 7
- **Authentication**: Auth0 (OAuth2/JWT Resource Server)
- **Security**: Spring Security with OAuth2
- **File Storage**: AWS S3, Local filesystem
- **Email**: Spring Boot Mail (MailHog for dev)
- **Mapping**: MapStruct
- **Validation**: Jakarta Bean Validation
- **Testing**: JUnit 5, Mockito, Spring Test
- **Code Coverage**: Jacoco
- **Build Tool**: Gradle

### DevOps
- **Containerization**: Docker, Docker Compose
- **Database**: MongoDB (containerized)
- **Mail Server**: MailHog (development)
- **Web Server**: Nginx (production frontend)

## Project Structure

```
VladTech-Inc/
├── vladtech-backend/              # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── org/example/vladtech/
│   │   │   │       ├── appointmentsubdomain/    # Appointment management
│   │   │   │       ├── contactsubdomain/        # Contact form
│   │   │   │       ├── estimatesubdomain/       # Estimate calculator
│   │   │   │       ├── filestorageservice/      # File upload service
│   │   │   │       ├── portfolio/               # Portfolio management
│   │   │   │       ├── projectsubdomain/        # Project management
│   │   │   │       ├── reviews/                 # Review system
│   │   │   │       ├── security/                # Security config
│   │   │   │       └── usersubdomain/           # User management
│   │   │   └── resources/
│   │   │       └── application.yml              # Configuration
│   │   └── test/                                # Unit & integration tests
│   ├── build.gradle                              # Gradle build config
│   └── Dockerfile
│
├── vladtech-frontend/             # React frontend
│   ├── src/
│   │   ├── api/                   # API client configuration
│   │   ├── assets/                # Static assets
│   │   ├── auth/                  # Auth0 integration
│   │   ├── components/            # Reusable React components
│   │   │   ├── estimates/         # Estimate calculator components
│   │   │   ├── portfolio/         # Portfolio components
│   │   │   ├── projects/          # Project management components
│   │   │   ├── reviews/           # Review components
│   │   │   └── userManagement/    # User/role management
│   │   ├── context/               # React context providers
│   │   ├── hooks/                 # Custom React hooks
│   │   ├── pages/                 # Page components
│   │   ├── translations/          # i18n translation files
│   │   ├── utils/                 # Utility functions
│   │   ├── App.jsx                # Main app component
│   │   ├── i18n.js                # i18n configuration
│   │   └── main.jsx               # Entry point
│   ├── tests/                     # Playwright E2E tests
│   │   ├── appointments/
│   │   ├── estimates/
│   │   ├── portfolio/
│   │   └── projects/
│   ├── playwright.config.ts       # Playwright configuration
│   ├── package.json
│   └── Dockerfile
│
├── pdf/                           # Project documentation
│   ├── E2E-INSTRUCTIONS.md
│   ├── IMPLEMENTATION-INSTRUCTIONS.md
│   ├── UI-UX-INSTRUCTIONS.md
│   └── 2.4.6-e2e-validation.md   # E2E test validation docs
│
├── docker-compose.yml             # Docker orchestration
└── README.md
```

## Prerequisites

### Required Software
- **Node.js**: >= 18.x
- **Java**: JDK 17 or higher
- **Docker**: >= 20.x (for containerized setup)
- **Docker Compose**: >= 2.x
- **MongoDB**: 7.x (if running locally)
- **Gradle**: 8.x (included via Gradle Wrapper)

### Accounts & Services
- **Auth0 Account**: For authentication (free tier available)
- **AWS Account**: For S3 file storage (optional, can use local storage)

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/VladLoghin/VladTech-Inc.git
cd VladTech-Inc
```

### 2. Backend Setup

```bash
cd vladtech-backend

# Install dependencies and build
./gradlew build

# Run tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport
```

### 3. Frontend Setup

```bash
cd vladtech-frontend

# Install dependencies
npm install

# Run development server
npm run dev

# Run E2E tests
npx playwright test

# View test report
npx playwright show-report
```

## Configuration

### Environment Variables

Create a `.env` file in the project root:

```env
# Auth0 Configuration
AUTH0_DOMAIN=your-domain.auth0.com
AUTH0_ISSUER_URI=https://your-domain.auth0.com/
AUTH0_AUDIENCE=https://vladtech/api

# Auth0 Management API
AUTH0_MGMT_CLIENT_ID=your_management_client_id
AUTH0_MGMT_CLIENT_SECRET=your_management_client_secret
AUTH0_MGMT_AUDIENCE=https://your-domain.auth0.com/api/v2/

# Auth0 Roles
AUTH0_ROLE_CLIENT=rol_client_id
AUTH0_ROLE_EMPLOYEE=rol_employee_id
AUTH0_ROLE_ADMIN=rol_admin_id
AUTH0_ROLE_DEFAULT=rol_default_id

# Database
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/vladtechdb

# File Storage (optional)
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_S3_BUCKET_NAME=your_bucket_name
AWS_REGION=us-east-1
```

### Auth0 Setup

1. Create an Auth0 application (Single Page Application)
2. Configure allowed callback URLs: `http://localhost:5173/callback`
3. Configure allowed logout URLs: `http://localhost:5173`
4. Create API with identifier: `https://vladtech/api`
5. Create roles: Admin, Employee, Client
6. Configure Auth0 Management API access

### Frontend Configuration

Update `vladtech-frontend/src/auth/auth_config.json`:

```json
{
  "domain": "your-domain.auth0.com",
  "clientId": "your_client_id",
  "audience": "https://vladtech/api"
}
```

## Running the Application

### Option 1: Docker Compose (Recommended)

```bash
# Start all services (MongoDB, Backend, Frontend, MailHog)
docker-compose up

# Access the application:
# - Frontend: http://localhost:5173
# - Backend API: http://localhost:8080
# - MailHog UI: http://localhost:8025
# - MongoDB: localhost:27017
```

### Option 2: Local Development

#### Terminal 1 - Backend
```bash
cd vladtech-backend
./gradlew bootRun
```

#### Terminal 2 - Frontend
```bash
cd vladtech-frontend
npm run dev
```

#### Terminal 3 - MongoDB
```bash
# Using Docker
docker run -d -p 27017:27017 --name vladtech-mongo mongo:7

# Or use local MongoDB installation
mongod --dbpath /path/to/data/db
```

## Testing

### Backend Tests

```bash
cd vladtech-backend

# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Frontend E2E Tests

```bash
cd vladtech-frontend

# Install Playwright browsers (first time only)
npx playwright install

# Run all tests
npx playwright test

# Run specific test file
npx playwright test tests/portfolio/portfolio-e2e.spec.ts

# Run tests in UI mode
npx playwright test --ui

# Run tests in headed mode (see browser)
npx playwright test --headed

# View test report
npx playwright show-report
```

### Test Coverage

The project includes comprehensive tests:

- **Backend**: JUnit + Mockito unit tests for all service layers
- **Frontend**: Playwright E2E tests covering:
  - Portfolio browsing, filtering, commenting, creation, archiving
  - Estimate calculator with presets
  - Project management and calendar
  - Review submission and moderation
  - Appointment scheduling
  - User authentication flows

## Architecture

### Backend Architecture

```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
MongoDB Database
```

- **Controllers**: Handle HTTP requests, authentication, validation
- **Services**: Implement business logic, transaction management
- **Repositories**: Spring Data MongoDB repositories
- **DTOs/Models**: Request/Response models with MapStruct mapping
- **Security**: JWT validation, role-based authorization with Spring Security

### Frontend Architecture

```
Pages (Route Components)
    ↓
Components (UI Elements)
    ↓
Context/Hooks (State Management)
    ↓
API Layer (HTTP Client)
    ↓
Backend REST API
```

- **Pages**: Top-level route components
- **Components**: Reusable UI components (Radix UI + Tailwind)
- **Context**: Global state (Language, Authentication)
- **Hooks**: Custom hooks for data fetching and logic
- **API**: Axios-based HTTP client with Auth0 token management

## API Documentation

### Base URL
- Development: `http://localhost:8080/api`
- Production: `https://your-domain.com/api`

### Authentication
All protected endpoints require a Bearer token from Auth0:

```
Authorization: Bearer <access_token>
```

### Key Endpoints

#### Portfolio
- `GET /portfolio` - Get all active portfolio items
- `POST /portfolio` - Create portfolio item (Admin)
- `PUT /portfolio/{id}` - Update portfolio item (Admin)
- `POST /portfolio/{id}/archive` - Archive portfolio item (Admin)
- `POST /portfolio/{id}/comment` - Add comment to portfolio item

#### Reviews
- `GET /reviews` - Get all reviews (Admin/Employee)
- `GET /reviews/visible` - Get visible reviews (Public)
- `POST /reviews` - Create review (Client)
- `PATCH /reviews/{id}/visibility` - Update visibility (Admin)
- `POST /reviews/{id}/send-to-portfolio` - Send to portfolio (Admin/Employee)

#### Projects
- `GET /projects` - Get all projects (Admin/Employee)
- `GET /projects/client` - Get client projects (Client)
- `POST /projects` - Create project (Admin/Employee)
- `PUT /projects/{id}` - Update project (Admin/Employee)
- `POST /projects/{id}/archive` - Archive project (Admin)

#### Estimates
- `POST /estimates/calculate` - Calculate estimate
- `GET /estimates/config` - Get calculator settings (Admin)
- `PUT /estimates/config` - Update settings (Admin)

#### Appointments
- `GET /appointments` - Get all appointments (Admin/Employee)
- `POST /appointments` - Create appointment
- `PUT /appointments/{id}` - Update appointment (Admin/Employee)
- `DELETE /appointments/{id}` - Cancel appointment

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards

- **Backend**: Follow Java code conventions, use Lombok, MapStruct
- **Frontend**: Follow React best practices, use functional components and hooks
- **Tests**: Write E2E tests for new features, maintain unit test coverage
- **Documentation**: Update README and inline documentation

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Team

VladTech-Inc - Team 3

## Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [React](https://react.dev/) - Frontend library
- [Auth0](https://auth0.com/) - Authentication platform
- [MongoDB](https://www.mongodb.com/) - Database
- [Playwright](https://playwright.dev/) - E2E testing
- [Radix UI](https://www.radix-ui.com/) - UI components
- [Tailwind CSS](https://tailwindcss.com/) - Styling framework
