import React, { useState } from "react";
import { registerUser, RegisterRequest } from "../api/userApi";

const RegisterPage: React.FC = () => {
    // 🔹 Burayı güncelledik: name, surname, phone eklendi
    const [form, setForm] = useState<RegisterRequest>({
        name: "",
        surname: "",
        email: "",
        phone: "",
        password: "",
    });
    const [confirmPassword, setConfirmPassword] = useState("");

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (form.password !== confirmPassword) {
            alert("Şifreler eşleşmiyor, kontrol ediniz!");
            return;
        }

        try {
            await registerUser(form);
            alert("Kaydınız başarıyla oluşturuldu.");
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

                {/* 🔹 Yeni alanlar */}
                <input
                    type="text"
                    name="name"
                    placeholder="Ad"
                    value={form.name}
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                    required
                />

                <input
                    type="text"
                    name="surname"
                    placeholder="Soyad"
                    value={form.surname}
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                    required
                />

                <input
                    type="email"
                    name="email"
                    placeholder="Email"
                    value={form.email}
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                    required
                />

                <input
                    type="text"
                    name="phone"
                    placeholder="Telefon"
                    value={form.phone}
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                    required
                />

                <input
                    type="password"
                    name="password"
                    placeholder="Şifre"
                    value={form.password}
                    onChange={handleChange}
                    className="w-full mb-3 p-2 border rounded"
                    required
                />

                <input
                    type="password"
                    name="confirmPassword"
                    placeholder="Şifre Onay"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="w-full mb-3 p-2 border rounded"
                    required
                />

                <button
                    type="submit"
                    className="w-full bg-green-600 text-white p-2 rounded"
                >
                    Kayıt Ol
                </button>

                <div className="flex justify-end mt-3 text-sm text-blue-600">
                    <a href="/">Kayıtlı hesabınız mevcut mu?</a>
                </div>
            </form>
        </div>
    );
};

export default RegisterPage;
