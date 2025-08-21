import React, { useState } from "react";
import userApi from "../api/userApi";

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
            alert("Şifreler eşleşmiyor kontrol ediniz!");
            return;
        }

        try {
            const response = await userApi.post("/api/auth/register", {
                username: form.username,
                email: form.email,
                password: form.password,
            });

            alert("Kaydınız başarıyla oluşturulmuştur.");
            console.log(response.data);
            window.location.href = "/";
        } catch (error: any) {
            alert("Hata! Kayıt oluşturulamadı.");
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
                        K
                    </div>
                </div>

                <h2 className="text-2xl font-bold mb-4 text-center">Kayıt Ol</h2>

                <input
                    type="text"
                    name="username"
                    placeholder="Kullanıcı Adı"
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
                    placeholder="Şifre"
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                />

                <input
                    type="password"
                    name="confirmPassword"
                    placeholder="Şifre Onay"
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                />

                <button
                    type="submit"
                    className="w-full bg-green-600 text-white p-2 rounded"
                >
                    Kayıt ol
                </button>

                <div className="flex justify-end mt-3 text-sm text-blue-600">
                    <a href="/">Kayıtlı hesabınız mevcut mu?</a>
                </div>
            </form>
        </div>
    );
};

export default RegisterPage;
