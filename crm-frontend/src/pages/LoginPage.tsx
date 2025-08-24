import React, { useState } from "react";
import userApi from "../api/userApi";
import { useNavigate } from "react-router-dom";
import PublicTicketForm from "../pages/PublicTicketForm"; // müşteri ticket formu

const LoginPage: React.FC = () => {
    const [form, setForm] = useState({ username: "", password: "" });
    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            const response = await userApi.post("/api/auth/login", {
                username: form.username,
                password: form.password,
            });

            const token = response.data.token || response.data.accessToken || response.data.jwt;
            if (!token) {
                alert("Token alınamadı!");
                console.error("Login response:", response.data);
                return;
            }

            localStorage.setItem("token", token);

            alert("Giriş başarılı!");
            console.log("JWT:", token);

            try {
                const payload = JSON.parse(atob(token.split(".")[1]));
                const role = payload.role;
                console.log("Decoded Role:", role);

                if (role === "ADMIN") {
                    navigate("/admin");
                } else {
                    navigate("/dashboard");
                }
            } catch (decodeError) {
                console.error("Token decode edilemedi:", decodeError);
            }
        } catch (error: any) {
            alert("Giriş yapılamadı! Kullanıcı adı veya parola hatalı.");
            console.error(error.response?.data || error.message);
        }
    };

    return (
        <div className="min-h-screen grid md:grid-cols-2">
            {/* 🔹 Sol taraf: Public Ticket Form */}
            <div className="flex items-center justify-center bg-gray-50 p-6">
                <div className="max-w-md w-full">
                    <PublicTicketForm />
                </div>
            </div>

            {/* 🔹 Sağ taraf: Login Form */}
            <div className="flex items-center justify-center p-6">
                <form onSubmit={handleSubmit} className="bg-white p-6 rounded-xl shadow-md w-96">
                    <div className="flex justify-center mb-4">
                        <div className="w-16 h-16 bg-blue-600 text-white flex items-center justify-center rounded-full text-2xl font-bold">
                            G
                        </div>
                    </div>

                    <h2 className="text-2xl font-bold mb-4 text-center">Tekrar hoşgeldiniz</h2>

                    <input
                        type="text"
                        name="username"
                        placeholder="Kullanıcı Adı"
                        onChange={handleChange}
                        className="w-full mb-3 p-2 border rounded"
                    />

                    <input
                        type="password"
                        name="password"
                        placeholder="Parola"
                        onChange={handleChange}
                        className="w-full mb-3 p-2 border rounded"
                    />

                    <button type="submit" className="w-full bg-blue-600 text-white p-2 rounded">
                        Giriş
                    </button>

                    <div className="flex justify-end mt-3 text-sm text-blue-600">
                        <a href="/register">Kayıt ol</a>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default LoginPage;
