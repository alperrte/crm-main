import React, { useEffect, useState } from "react";
import { getAdminTickets, AdminTicket } from "../api/ticketApi";
import { Link } from "react-router-dom";

const AdminPanel: React.FC = () => {
    const [tickets, setTickets] = useState<AdminTicket[]>([]);
    const [tErr, setTErr] = useState<string>("");
    const [menuOpen, setMenuOpen] = useState(false);

    // Ticketları çek
    useEffect(() => {
        getAdminTickets()
            .then(setTickets)
            .catch((e) => setTErr(String(e?.message ?? e)));
    }, []);

    // Priority badge class
    const priorityClass = (p: string) => {
        switch (p) {
            case "HIGH":
                return "bg-gradient-to-r from-red-500 to-orange-500 text-white px-3 py-1 rounded-full text-xs font-semibold shadow";
            case "MEDIUM":
                return "bg-gradient-to-r from-yellow-400 to-pink-400 text-gray-800 px-3 py-1 rounded-full text-xs font-semibold shadow";
            case "LOW":
                return "bg-gradient-to-r from-cyan-400 to-blue-500 text-white px-3 py-1 rounded-full text-xs font-semibold shadow";
            default:
                return "bg-gray-400 text-white px-3 py-1 rounded-full text-xs font-semibold";
        }
    };

    // Status badge class
    const statusClass = (active: boolean) =>
        active
            ? "bg-gradient-to-r from-green-400 to-emerald-600 text-white px-3 py-1 rounded-full text-xs font-semibold shadow"
            : "bg-gradient-to-r from-gray-400 to-gray-600 text-white px-3 py-1 rounded-full text-xs font-semibold shadow";

    return (
        <div className="min-h-screen p-6 bg-gradient-to-br from-[#0f0f23] via-[#1a1a2e] to-[#16213e] text-white">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="flex justify-between items-center mb-8 border-b border-white/10 pb-4">
                    <div>
                        <h1 className="text-4xl font-extrabold bg-gradient-to-r from-indigo-400 to-purple-500 bg-clip-text text-transparent">
                            Admin Panel
                        </h1>
                        <h2 className="text-lg text-gray-400">Ticket Yönetimi</h2>
                    </div>

                    {/* Kontroller Dropdown */}
                    <div className="relative">
                        <button
                            onClick={() => setMenuOpen(!menuOpen)}
                            className="bg-gradient-to-r from-indigo-500 to-purple-600 px-5 py-2 rounded-full font-semibold shadow hover:scale-105 transition"
                        >
                            Kontroller ▼
                        </button>
                        {menuOpen && (
                            <div className="absolute right-0 mt-2 w-56 bg-[#1a1a2e]/95 backdrop-blur rounded-xl shadow-lg z-50 border border-white/10 overflow-hidden">
                                <Link
                                    to="/admin/departments"
                                    className="block px-5 py-3 hover:bg-indigo-500/20"
                                    onClick={() => setMenuOpen(false)}
                                >
                                    Departman Kontrolleri
                                </Link>
                                <Link
                                    to="/admin/roles"
                                    className="block px-5 py-3 hover:bg-indigo-500/20"
                                    onClick={() => setMenuOpen(false)}
                                >
                                    Rol Kontrolleri
                                </Link>
                            </div>
                        )}
                    </div>
                </div>

                {/* Stats bar */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                    <div className="bg-indigo-500/10 border border-white/10 p-4 rounded-xl text-center">
                        <div className="text-2xl font-bold text-indigo-400">{tickets.length}</div>
                        <div className="text-sm text-gray-400">Toplam Ticket</div>
                    </div>
                    <div className="bg-indigo-500/10 border border-white/10 p-4 rounded-xl text-center">
                        <div className="text-2xl font-bold text-red-400">
                            {tickets.filter((t) => t.priority === "HIGH").length}
                        </div>
                        <div className="text-sm text-gray-400">Yüksek Öncelik</div>
                    </div>
                    <div className="bg-indigo-500/10 border border-white/10 p-4 rounded-xl text-center">
                        <div className="text-2xl font-bold text-yellow-400">
                            {tickets.filter((t) => t.priority === "MEDIUM").length}
                        </div>
                        <div className="text-sm text-gray-400">Orta Öncelik</div>
                    </div>
                    <div className="bg-indigo-500/10 border border-white/10 p-4 rounded-xl text-center">
                        <div className="text-2xl font-bold text-green-400">
                            {tickets.filter((t) => t.active).length}
                        </div>
                        <div className="text-sm text-gray-400">Aktif</div>
                    </div>
                </div>

                {/* Ticket Table */}
                <div className="bg-[#1a1a2e]/80 backdrop-blur p-6 rounded-2xl shadow-2xl border border-white/10 overflow-x-auto">
                    {tErr && (
                        <div className="mb-3 text-red-400 font-semibold">
                            Ticketlar alınamadı: {tErr}
                        </div>
                    )}
                    <table className="w-full border-collapse">
                        <thead>
                        <tr className="bg-gradient-to-r from-indigo-500 to-purple-600 text-left">
                            <th className="px-4 py-3">ID</th>
                            <th className="px-4 py-3">Tarih</th>
                            <th className="px-4 py-3">Müşteri</th>
                            <th className="px-4 py-3">Email</th>
                            <th className="px-4 py-3">Telefon</th>
                            <th className="px-4 py-3">Açıklama</th>
                            <th className="px-4 py-3">Öncelik</th>
                            <th className="px-4 py-3">Aktif</th>
                        </tr>
                        </thead>
                        <tbody>
                        {tickets.map((t, i) => (
                            <tr
                                key={t.id}
                                className={`transition hover:bg-indigo-500/10 ${
                                    i % 2 === 0 ? "bg-transparent" : "bg-white/5"
                                }`}
                            >
                                <td className="px-4 py-3">{t.id}</td>
                                <td className="px-4 py-3">
                                    {t.createdDate
                                        ? new Date(t.createdDate).toLocaleString()
                                        : "-"}
                                </td>
                                <td className="px-4 py-3">{t.name} {t.surname}</td>
                                <td className="px-4 py-3">{t.email}</td>
                                <td className="px-4 py-3">{t.phone ?? "-"}</td>
                                <td className="px-4 py-3 max-w-xs truncate hover:whitespace-normal hover:bg-[#1a1a2e]/95 hover:rounded-lg hover:p-2 hover:shadow-xl">
                                    {t.description}
                                </td>
                                <td className="px-4 py-3">
                                        <span className={priorityClass(t.priority)}>
                                            {t.priority}
                                        </span>
                                </td>
                                <td className="px-4 py-3">
                                        <span className={statusClass(t.active)}>
                                            {t.active ? "Evet" : "Hayır"}
                                        </span>
                                </td>
                            </tr>
                        ))}
                        {tickets.length === 0 && (
                            <tr>
                                <td
                                    colSpan={8}
                                    className="text-center text-gray-400 py-10"
                                >
                                    Ticket bulunamadı
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default AdminPanel;
