import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import AdminPanel from "./admin/AdminPanel";
import DepartmentPage from "./pages/DepartmentPage";
import RolesPage from "./pages/RolesPage";
import UserPage from "./pages/UserPage";
import PublicTicketForm from "./pages/PublicTicketForm";
import CreateTicketPage from "./pages/CreateTicketPage";   // ✅ yeni eklendi
import CreateUserPage from "./pages/CreateUserPage";       // ✅ yeni eklendi
import UserPanel from "./pages/UserPanel";                 // ✅ yeni eklendi

// Basit koruma: token yoksa login'e at
function RequireAuth({ children }: { children: React.ReactNode }) {
    const token = localStorage.getItem("token");
    if (!token) return <Navigate to="/login" replace />;
    return <>{children}</>;
}

function App() {
    return (
        <Router>
            <Routes>
                {/* default olarak /login sayfasına yönlendirme */}
                <Route path="/" element={<Navigate to="/login" />} />

                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                {/* Admin tarafı korumalı */}
                <Route
                    path="/admin"
                    element={
                        <RequireAuth>
                            <AdminPanel />
                        </RequireAuth>
                    }
                />
                <Route
                    path="/admin/departments"
                    element={
                        <RequireAuth>
                            <DepartmentPage />
                        </RequireAuth>
                    }
                />
                <Route
                    path="/admin/roles"
                    element={
                        <RequireAuth>
                            <RolesPage />
                        </RequireAuth>
                    }
                />
                {/* ✅ Yeni kullanıcı oluşturma sayfası */}
                <Route
                    path="/admin/create-user"
                    element={
                        <RequireAuth>
                            <CreateUserPage />
                        </RequireAuth>
                    }
                />

                {/* ✅ Departman user paneli */}
                <Route
                    path="/user"
                    element={
                        <RequireAuth>
                            <UserPage />
                        </RequireAuth>
                    }
                />

                {/* ✅ Yeni ticket oluşturma sayfası */}
                <Route
                    path="/create-ticket"
                    element={
                        <RequireAuth>
                            <CreateTicketPage />
                        </RequireAuth>
                    }
                />

                {/* ✅ Genel USER paneli */}
                <Route
                    path="/user-panel"
                    element={
                        <RequireAuth>
                            <UserPanel />
                        </RequireAuth>
                    }
                />

                {/* Public ticket formu */}
                <Route path="/ticket" element={<PublicTicketForm />} />
            </Routes>
        </Router>
    );
}

export default App;
