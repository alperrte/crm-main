import React, { useState } from "react";
import api from "../api";

const RegisterPage: React.FC = () => {
    const [form, setForm] = useState({
        username: "",
        email: "",
        password: "",
        confirmPassword: "",
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (form.password !== form.confirmPassword) {
            alert("Passwords do not match!");
            return;
        }

        try {
            const response = await api.post("/api/auth/register", {
                username: form.username,
                email: form.email,
                password: form.password,
            });

            alert("Registration successful!");
            console.log(response.data);
            window.location.href = "/";
        } catch (error: any) {
            alert("Registration failed!");
            console.error(error.response?.data || error.message);
        }
    };

    return (
        <div className="flex items-center justify-center h-screen bg-gray-100">
            <form
                onSubmit={handleSubmit}
                className="bg-white p-6 rounded-xl shadow-md w-96"
            >
                <div className="flex justify-center mb-4">
                    <div className="w-16 h-16 bg-green-600 text-white flex items-center justify-center rounded-full text-2xl font-bold">
                        R
                    </div>
                </div>

                <h2 className="text-2xl font-bold mb-4 text-center">Register</h2>

                <input
                    type="text"
                    name="username"
                    placeholder="Username"
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                />

                <input
                    type="email"
                    name="email"
                    placeholder="Email"
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

                <input
                    type="password"
                    name="confirmPassword"
                    placeholder="Confirm Password"
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                />

                <button
                    type="submit"
                    className="w-full bg-green-600 text-white p-2 rounded"
                >
                    Register
                </button>

                <div className="flex justify-end mt-3 text-sm text-blue-600">
                    <a href="/">Already have an account?</a>
                </div>
            </form>
        </div>
    );
};

export default RegisterPage;
