# CI/CD Setup for VladTech Full-Stack E2E Tests

## Overview

This project uses **Docker Compose** to run the full stack (frontend, backend, MongoDB, MailHog) in GitHub Actions CI to enable Playwright E2E tests.

## Architecture

### Local Development
- **Frontend**: Vite dev server on `http://localhost:5173`
- **Backend**: Spring Boot on `http://localhost:8080`
- **MongoDB**: Port 27017
- **MailHog**: Web UI on port 8025, SMTP on port 1025

### CI Environment
Same ports, but services run in Docker containers using:
- `docker-compose.yml` - Production configuration
- `docker-compose.ci.yml` - CI overrides for development mode

## Key Files

### 1. `.github/workflows/frontend-ci.yml`
Main CI workflow that:
1. Builds frontend with Vite
2. Runs ESLint and Prettier checks
3. Starts full stack with Docker Compose
4. Runs Playwright E2E tests
5. Uploads test reports

### 2. `docker-compose.ci.yml`
Override file for CI that:
- Uses `Dockerfile.dev` for frontend (dev server, not production build)
- Exposes frontend on port 5173 (Vite dev server)
- Mounts source code for live testing

### 3. `vladtech-frontend/Dockerfile.dev`
Development Dockerfile that runs `npm run dev` instead of building for production.

### 4. `vladtech-frontend/playwright.config.ts`
Playwright configuration that:
- Uses `baseURL: http://localhost:5173`
- Disables `webServer` in CI (since Docker Compose handles it)
- Enables `webServer` locally for convenience

## Running Tests Locally

### Option 1: With Docker Compose (Recommended - matches CI exactly)
```bash
# Start all services
docker-compose -f docker-compose.yml -f docker-compose.ci.yml up -d --build

# Wait for services to be ready (30-60 seconds)
# Check logs if needed:
docker-compose -f docker-compose.yml -f docker-compose.ci.yml logs -f

# Run Playwright tests from host
cd vladtech-frontend
npx playwright test

# Cleanup
docker-compose -f docker-compose.yml -f docker-compose.ci.yml down -v
```

### Option 2: Without Docker (Faster, but needs manual setup)
```bash
# Terminal 1: Start backend (requires MongoDB running separately)
cd vladtech-backend
./gradlew bootRun

# Terminal 2: Start frontend
cd vladtech-frontend
npm run dev

# Terminal 3: Run tests
cd vladtech-frontend
npx playwright test
```

## Troubleshooting CI Failures

### Tests fail with "ERR_CONNECTION_REFUSED"

**Symptoms**: 
```
Error: page.goto: net::ERR_CONNECTION_REFUSED at http://localhost:5173/
```

**Causes & Solutions**:

1. **Services not started**: Check CI logs for Docker Compose startup
   - Look for: "Waiting for services to be ready..."
   - Verify all services started: `docker-compose ps` output in logs

2. **Frontend not ready**: Frontend might need more time to start
   - Check "Show frontend logs" in CI output
   - Look for Vite dev server message: "Local: http://localhost:5173/"

3. **Backend not responding**: Backend might be failing to start
   - Check "Show backend logs" in CI output
   - Look for Spring Boot startup completion
   - Common issues:
     - MongoDB connection failures
     - Missing Auth0 environment variables
     - Port conflicts

4. **Auth0 secrets missing**: Add these to GitHub Secrets:
   ```
   AUTH0_ISSUER_URI
   AUTH0_AUDIENCE
   AUTH0_DOMAIN
   AUTH0_MGMT_CLIENT_ID
   AUTH0_MGMT_CLIENT_SECRET
   AUTH0_MGMT_AUDIENCE
   AUTH0_ROLE_CLIENT
   AUTH0_ROLE_EMPLOYEE
   AUTH0_ROLE_ADMIN
   AUTH0_ROLE_DEFAULT
   ```

### MailHog tests fail

Some tests require MailHog for email verification:
```
Error: MailHog not reachable at http://localhost:8025
```

**Solution**: MailHog should start automatically via Docker Compose. Check if:
- MailHog container is running: `docker-compose ps mailhog`
- Port 8025 is accessible
- No port conflicts in CI environment

### Docker build fails

**Symptoms**: Build fails during `docker-compose up`

**Solutions**:
1. Check if `vladtech-frontend/Dockerfile.dev` exists
2. Verify `docker-compose.ci.yml` is in repo root
3. Check for syntax errors in Docker files
4. Review build logs for missing dependencies

### Slow CI runs

**Current behavior**: CI spins up full stack, so expect 3-5 minutes total

**Optimization tips**:
- Use Docker layer caching (already enabled via `actions/setup-java` and `setup-node`)
- Consider splitting into separate jobs (build + test)
- Run only changed tests (Playwright supports `--only-changed`)

## Environment Variables

### Required for CI (GitHub Secrets)
All Auth0 configuration must be added as secrets in GitHub repo settings.

### Frontend Configuration
Hardcoded in `src/auth/Auth0ProviderWithConfig.jsx`:
- Auth0 Domain: `dev-ljz84r2xvrlnftfv.ca.auth0.com`
- Client ID: `sDVdjRgneqMMYuQm8njufqcG0yrPV2j6`
- Audience: `https://vladtech/api`

### Backend Configuration
See `docker-compose.yml` and `.env` file for all environment variables.

## Playwright Configuration

### Test Isolation
- `workers: 1` in CI - tests run sequentially to avoid conflicts
- `retries: 2` in CI - flaky tests get 2 retries
- `fullyParallel: true` locally - faster feedback

### Timeouts
- Web server startup: 120 seconds
- MongoDB: 30 seconds
- Backend: 120 seconds  
- Frontend: 60 seconds
- Additional stabilization: 10 seconds

Total startup time: ~2-3 minutes

## Debugging Failed Tests

### View Playwright traces
1. Download `playwright-report` artifact from failed CI run
2. Unzip and run: `npx playwright show-trace path/to/trace.zip`
3. Interactive trace viewer shows:
   - Screenshots at each step
   - Network requests
   - Console logs
   - Timeline of actions

### Check service logs in CI
All CI runs include:
- Container status (`docker-compose ps`)
- Backend logs
- Frontend logs

Look for these in the CI output under "Show running containers", "Show backend logs", etc.

## Common Issues and Fixes

| Issue | Solution |
|-------|----------|
| Port conflicts locally | Stop other services or change ports in docker-compose |
| Auth0 login fails in tests | Verify Auth0 credentials in `.env` and GitHub Secrets |
| Slow Docker builds | Use `docker system prune` to clean up old images |
| Tests timeout | Increase timeout in playwright.config.ts or wait longer in CI |
| Database state issues | Ensure tests clean up data or use unique test data |

## Further Reading

- [Playwright Documentation](https://playwright.dev/docs/intro)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Vite Server Options](https://vitejs.dev/config/server-options.html)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
