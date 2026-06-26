import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useApp } from '../context/AppContext';
import { Unauthorized } from '../pages/Unauthorized';

interface ProtectedRouteProps {
  allowedRoles?: ('student' | 'instructor' | 'admin')[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ allowedRoles }) => {
  const { user } = useApp();

  // 1. If not authenticated, redirect to /login
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // 2. If authenticated but role not allowed, render Unauthorized page
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Unauthorized />;
  }

  // 3. Render nested routes
  return <Outlet />;
};
