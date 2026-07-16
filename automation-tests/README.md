# SWP391 — UI Automation Testing Suite

This directory contains the automated end-to-end (E2E) UI testing suite for the **SWP391 AI-Assisted Coding Audit Platform**. The tests are built using **Playwright Test** and run against the React/Vite frontend.

---

## 🚀 Covered Test Scenarios

The suite covers **6 automated tests** spanning three primary user flows of the application:

### 1. Authentication Flow (`tests/auth.spec.js`)
* **`should fail to login with invalid credentials`**
  * Verifies that entering a wrong username/password shows the correct warning box containing the text `"Invalid username or password"`.
* **`should login and logout successfully with valid credentials`**
  * Logs in with the test user `user1`/`user1`, verifies redirection to `/dashboard`, interacts with the header profile dropdown avatar, clicks **Logout**, and verifies redirection back to `/login`.

### 2. Practice Problems & Code Playground (`tests/problems.spec.js`)
* **`should browse problems list and select a problem`**
  * Navigates to `/problems`, waits for the grid to load, selects the first problem link, redirects to the solve panel (`/problems/:id`), and verifies that the description and Monaco code editor are loaded.
* **`should choose language and submit code solution`**
  * Directly navigates to a problem, waits for Monaco to initialize, selects the **Python** language from the dropdown, clicks the **Submit** button, and verifies the view transitions to show the evaluation results.

### 3. Competitive Programming Contest Arena (`tests/contests.spec.js`)
* **`should browse contests list and enter a contest arena`**
  * Navigates to `/contests`, waits for the async loading spinner to hide, finds the first contest card, and clicks **Enter Arena** to go to `/contests/:id`.
  * Checks if the user is registered for the contest; if not, automatically clicks **Register Now** and verifies the green checkmark badge appears.
  * If the contest is ongoing or ended, it automatically navigates to **Problems** (`/contests/:id/problems`) and **Rankings** (`/contests/:id/ranking`), verifying the ranking table is loaded.

### 4. Course Catalog & Shopping Cart (`tests/courses.spec.js`)
* **`should browse courses catalog and view course detail`**
  * Navigates to `/courses`, waits for the catalog grid, clicks the first course card, and verifies it navigates to the detailed view (`/courses/:id`) with registration options.
* **`should add a paid course to cart from list`**
  * Scans the course list, locates a paid course containing the "Add to cart" button, clicks it, and verifies that the header shopping cart badge counter increments by `+1` successfully.

---

## 🛠️ Setup & Running Instructions

### 1. Prerequisites
Ensure you have the backend (port 8080) and frontend (port 5173) running locally:
* Backend: `http://localhost:8080`
* Frontend: `http://localhost:5173`

### 2. Install Dependencies
Run the following command inside the `automation-tests` directory to install dependencies if you are running it on a new setup:
```bash
npm install
```

### 3. Run Tests
Run the entire suite in headless mode (default):
```bash
npm run test
```

### 4. Run Tests in Interactive UI Mode
To watch the tests execute visually in real-time and inspect DOM locators:
```bash
npx playwright test --ui
```

### 5. View Test Reports
After running the tests, view the detailed HTML report:
```bash
npm run report
```

---

## 📁 Project Structure

```
automation-tests/
├── tests/                     # Test script directory
│   ├── auth.spec.js           # Login & logout flow tests
│   ├── courses.spec.js        # Catalog browsing & shopping cart addition
│   └── problems.spec.js       # Code playground & submissions
├── playwright.config.js       # Playwright browser and base URL configs
└── package.json               # Package manifests and script runners
```
