import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import AdminPanel from "./admin/AdminPanel";
import DepartmentPage from "./pages/DepartmentPage";

function App() {
    return (
        <Router>
            <Routes>
                {/* default olarak /login sayfasına yönlendirme */}
                <Route path="/" element={<Navigate to="/login" />} />

                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/admin" element={<AdminPanel />} />
                <Route path="/admin/departments" element={<DepartmentPage/>} />

            </Routes>
        </Router>
    );
}

export default App;