import React, { useEffect, useState } from "react";
import {
    createUserTicket,
    getUserTickets,
    DeptTicket,
    getCategories,
    Category,
} from "../api/ticketApi";
import { useNavigate } from "react-router-dom";

const UserPanel: React.FC = () => {
    const [tickets, setTickets] = useState<DeptTicket[]>([]);
    const [categories, setCategories] = useState<Category[]>([]);
    const [form, setForm] = useState({
        issue: "",
        priority: "LOW",
        categoryId: "",
    });

    const navigate = useNavigate();

    // 🔹 Çıkış işlemi
    const handleLogout = () => {
        localStorage.removeItem("token");
        navigate("/login");
    };

    // 🔹 Tarih formatlama helper
    const formatDate = (dateString?: string) => {
        if (!dateString) return "-";
        try {
            return new Date(dateString).toLocaleDateString("tr-TR", {
                year: "numeric",
                month: "2-digit",
                day: "2-digit",
            });
        } catch {
            return dateString;
        }
    };

    // 🔹 Kategorileri yükle
    useEffect(() => {
        getCategories().then(setCategories).catch(() => setCategories([]));
    }, []);

    // 🔹 Benim ticketlarımı getir
    const loadTickets = async () => {
        try {
            const res = await getUserTickets();
            setTickets(res);
        } catch (err) {
            console.error("Ticket yüklenemedi:", err);
        }
    };

    useEffect(() => {
        loadTickets();
    }, []);

    // 🔹 Ticket oluştur
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await createUserTicket({
                issue: form.issue,
                priority: form.priority,
                categoryId: form.categoryId ? Number(form.categoryId) : undefined,
                personId: undefined,
                departmentId: undefined,
            });
            setForm({ issue: "", priority: "LOW", categoryId: "" });
            await loadTickets();
        } catch (err) {
            console.error("Ticket oluşturulamadı:", err);
        }
    };

    return (
        <div className="p-6 bg-gray-900 min-h-screen text-white relative">
            {/* 🔹 Çıkış butonu */}
            <button
                onClick={handleLogout}
                className="absolute top-6 right-6 bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg shadow-md transition"
            >
                Çıkış Yap
            </button>

            <h1 className="text-3xl font-bold mb-8 text-center bg-gradient-to-r from-purple-500 to-blue-500 bg-clip-text text-transparent">
                🎫 Kullanıcı Paneli
            </h1>

            {/* Ticket Açma Formu */}
            <form
                onSubmit={handleSubmit}
                className="bg-gray-800 p-6 rounded-2xl shadow-lg mb-8 max-w-2xl mx-auto"
            >
                <h2 className="text-xl font-semibold mb-4">📝 Yeni Ticket Aç</h2>

                <input
                    type="text"
                    placeholder="Konu / Sorun"
                    className="w-full p-3 mb-4 rounded-lg text-black focus:outline-none focus:ring-2 focus:ring-purple-500"
                    value={form.issue}
                    onChange={(e) => setForm({ ...form, issue: e.target.value })}
                    required
                />

                <select
                    className="w-full p-3 mb-4 rounded-lg text-black focus:outline-none focus:ring-2 focus:ring-purple-500"
                    value={form.priority}
                    onChange={(e) => setForm({ ...form, priority: e.target.value })}
                >
                    <option value="LOW">🟢 Düşük</option>
                    <option value="MEDIUM">🟡 Orta</option>
                    <option value="HIGH">🔴 Yüksek</option>
                </select>

                <select
                    className="w-full p-3 mb-4 rounded-lg text-black focus:outline-none focus:ring-2 focus:ring-purple-500"
                    value={form.categoryId}
                    onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                    required
                >
                    <option value="">📂 Kategori seç</option>
                    {categories.map((c) => (
                        <option key={c.id} value={c.id}>
                            {c.displayName}
                        </option>
                    ))}
                </select>

                <button
                    type="submit"
                    className="w-full bg-gradient-to-r from-blue-500 to-purple-600 px-4 py-2 rounded-lg text-white font-semibold shadow-md hover:opacity-90 transition"
                >
                    🚀 Gönder
                </button>
            </form>

            {/* Kullanıcı Ticketları */}
            <div className="bg-gray-800 p-6 rounded-2xl shadow-lg max-w-4xl mx-auto">
                <h2 className="text-xl font-semibold mb-4">📂 Açtığım Ticketlar</h2>
                {tickets.length === 0 ? (
                    <p className="text-gray-400">Henüz ticket açmadınız.</p>
                ) : (
                    <table className="w-full text-left border-collapse">
                        <thead className="bg-purple-700 text-white">
                        <tr>
                            <th className="p-3">ID</th>
                            <th className="p-3">Konu</th>
                            <th className="p-3">Öncelik</th>
                            <th className="p-3">Durum</th>
                            <th className="p-3">Tarih</th>
                        </tr>
                        </thead>
                        <tbody>
                        {tickets.map((t) => (
                            <tr
                                key={t.id}
                                className="border-b border-gray-600 hover:bg-gray-700 transition"
                            >
                                <td className="p-3">{t.id}</td>
                                <td className="p-3">{t.issue}</td>
                                <td className="p-3">{t.priority}</td>
                                <td className="p-3">{t.status}</td>
                                <td className="p-3">{formatDate(t.createdDate)}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default UserPanel;
