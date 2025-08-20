import React, { useState } from "react";
import api from "../api";

const LoginPage: React.FC = () => {
    const [form, setForm] = useState({ username: "", password: "" });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            const response = await api.post("/api/auth/login", {
                username: form.username,
                password: form.password,
            });

            const token = response.data.token;
            localStorage.setItem("token", token);

            alert("Login successful!");
            console.log("JWT:", token);

            // Giriş başarılı olunca yönlendirme yapabiliriz (şimdilik /register yerine dashboard vs.)
            // window.location.href = "/dashboard";
        } catch (error: any) {
            alert("Login failed!");
            console.error(error.response?.data || error.message);
        }
    };

    return (
        <div className="flex items-center justify-center h-screen bg-gray-100">
            <form onSubmit={handleSubmit} className="bg-white p-6 rounded-xl shadow-md w-96">
                <div className="flex justify-center mb-4">
                    <div className="w-16 h-16 bg-blue-600 text-white flex items-center justify-center rounded-full text-2xl font-bold">
                        L
                    </div>
                </div>

                <h2 className="text-2xl font-bold mb-4 text-center">Welcome back</h2>

                <input
                    type="text"
                    name="username"
                    placeholder="Username"
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                />

                <input
                    type="password"
                    name="password"
                    placeholder="Password"
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                />

                <button type="submit" className="w-full bg-blue-600 text-white p-2 rounded">
                    Sign in
                </button>

                <div className="flex justify-end mt-3 text-sm text-blue-600">
                    <a href="/register">Register</a>
                </div>
            </form>
        </div>
    );
};

export default LoginPage;
