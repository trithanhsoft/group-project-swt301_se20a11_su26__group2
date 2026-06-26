import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import { AppProvider } from './context/AppContext';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { StudentDashboard } from './pages/StudentDashboard';
import { InstructorDashboard } from './pages/InstructorDashboard';
import { AdminDashboard } from './pages/AdminDashboard';
import { ApplyInstructor } from './pages/ApplyInstructor';
import { Courses } from './pages/Courses';
import { CourseDetail } from './pages/CourseDetail';
import { Problems } from './pages/Problems';
import { SolveProblem } from './pages/SolveProblem';
import { GlobalRanking } from './pages/GlobalRanking';
import { Contests } from './pages/Contests';
import { ContestOverview } from './pages/ContestOverview';
import { ContestProblems } from './pages/ContestProblems';
import { ContestProblemSolve } from './pages/ContestProblemSolve';
import { ContestSubmissions } from './pages/ContestSubmissions';
import { ContestRanking } from './pages/ContestRanking';
import { ShoppingCart } from './pages/ShoppingCart';
import { ContactUs } from './pages/ContactUs';
import { TermsOfService } from './pages/TermsOfService';
import { PrivacyPolicy } from './pages/PrivacyPolicy';
import { CookiesPolicy } from './pages/CookiesPolicy';

function App() {
  const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || 'your-google-client-id.apps.googleusercontent.com';

  return (
    <GoogleOAuthProvider clientId={googleClientId}>
      <AppProvider>
        <BrowserRouter basename={import.meta.env.BASE_URL}>
          <Routes>
            {/* Public Auth routes without shared Navbar/Footer */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            {/* Platform SPA routes wrapped inside responsive Layout shell */}
            <Route path="/" element={<Layout />}>
              <Route index element={<Home />} />

              {/* Student/User learning dashboard (accessible to all logged in users) */}
              <Route element={<ProtectedRoute allowedRoles={['student', 'instructor', 'admin']} />}>
                <Route path="dashboard" element={<StudentDashboard />} />
                <Route path="apply-instructor" element={<ApplyInstructor />} />
              </Route>

              {/* Instructor area (only for instructors) */}
              <Route element={<ProtectedRoute allowedRoles={['instructor']} />}>
                <Route path="instructor" element={<InstructorDashboard />} />
              </Route>

              {/* Admin Panel (only for admin) */}
              <Route element={<ProtectedRoute allowedRoles={['admin']} />}>
                <Route path="admin" element={<AdminDashboard />} />
                <Route path="admin/:tab" element={<AdminDashboard />} />
              </Route>
              
              {/* Courses Catalog & Details */}
              <Route path="courses" element={<Courses />} />
              <Route path="courses/:id" element={<CourseDetail />} />
              
              {/* Coding Arena problems set */}
              <Route path="problems" element={<Problems />} />
              <Route path="problems/:id" element={<SolveProblem />} />
              
              {/* Global ranks */}
              <Route path="rankings" element={<GlobalRanking />} />
              
              {/* Competitions */}
              <Route path="contests" element={<Contests />} />
              <Route path="contests/:id" element={<ContestOverview />} />
              <Route path="contests/:id/problems" element={<ContestProblems />} />
              <Route path="contests/:id/problems/:problemId" element={<ContestProblemSolve />} />
              <Route path="contests/:id/submissions" element={<ContestSubmissions />} />
              <Route path="contests/:id/ranking" element={<ContestRanking />} />
              
              {/* Shopping cart */}
              <Route path="shopping-cart" element={<ShoppingCart />} />
              
              {/* Footer legal and contact pages */}
              <Route path="contact" element={<ContactUs />} />
              <Route path="terms" element={<TermsOfService />} />
              <Route path="privacy" element={<PrivacyPolicy />} />
              <Route path="cookies" element={<CookiesPolicy />} />
            </Route>

            {/* Fallback redirect */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AppProvider>
    </GoogleOAuthProvider>
  );
}

export default App;
