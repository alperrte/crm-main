// src/pages/RolesPage.tsx
import React, { useEffect, useState, useRef } from "react";
import api from "../api/userApi";
import { Link } from "react-router-dom";

interface User {
    id: number;
    name: string;
    surname: string;
    email: string;
    phone: string;
    role: string | null;
}

const RolesPage: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(false);
    const [openDropdown, setOpenDropdown] = useState<number | null>(null);
    const [menuOpen, setMenuOpen] = useState(false);
    const [confirmAction, setConfirmAction] = useState<{ userId: number; role: string } | null>(null);
    const buttonRefs = useRef<Record<number, HTMLButtonElement | null>>({});

    // ✅ Sayfalama için state
    const [currentPage, setCurrentPage] = useState(1);
    const usersPerPage = 6;

    // ✅ Rol filtresi için state → açılışta "Hepsi"
    const [roleFilter, setRoleFilter] = useState<string>("ALL");

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const res = await api.get("/admin/users");
            const withRole = res.data.filter((u: User) => u.role !== null);
            setUsers(withRole);
        } catch (err) {
            console.error("❌ Kullanıcılar alınamadı:", err);
        } finally {
            setLoading(false);
        }
    };

    const updateRole = async (id: number, role: string) => {
        try {
            await api.put(`/admin/users/${id}/role?role=${encodeURIComponent(role)}`);
            await fetchUsers();
        } catch (err) {
            console.error("❌ Rol güncellenemedi:", err);
        } finally {
            setOpenDropdown(null);
            setConfirmAction(null);
        }
    };

    // ✅ Rolleri UI için etiketle
    const roleLabel = (role: string | null) => {
        switch (role) {
            case "ADMIN": return "Yönetici";
            case "PERSON": return "Yetkili Çalışan";
            case "USER": return "Genel Kullanıcı";
            default: return "—";
        }
    };

    // ✅ Role göre filtrelenmiş kullanıcılar
    const filteredUsers = users.filter((u) => {
        if (roleFilter === "ALL") return true;
        return u.role === roleFilter;
    });

    // ✅ Gösterilecek kullanıcıları hesapla
    const indexOfLastUser = currentPage * usersPerPage;
    const indexOfFirstUser = indexOfLastUser - usersPerPage;
    const currentUsers = filteredUsers.slice(indexOfFirstUser, indexOfLastUser);
    const totalPages = Math.ceil(filteredUsers.length / usersPerPage);

    return (
        <div className="min-h-screen p-6 bg-gradient-to-br from-indigo-400 to-purple-600 flex justify-center">
            <div className="w-full max-w-6xl bg-white rounded-2xl shadow-2xl overflow-hidden">
                {/* Header */}
                <div className="flex justify-between items-center bg-gradient-to-r from-blue-400 to-cyan-400 text-white p-6">
                    <h1 className="text-2xl font-bold">Rol & Yetki Kontrolleri</h1>
                    <div className="flex items-center gap-4">
                        {/* ✅ Rol Filtresi */}
                        <select
                            value={roleFilter}
                            onChange={(e) => {
                                setRoleFilter(e.target.value);
                                setCurrentPage(1);
                            }}
                            className="px-4 py-2 rounded-lg text-gray-800"
                        >
                            <option value="ALL">Hepsi</option>
                            <option value="USER">Genel Kullanıcılar</option>
                            <option value="PERSON">Yetkili Çalışanlar</option>
                            <option value="ADMIN">Yöneticiler</option>
                        </select>

                        {/* Paneller Dropdown */}
                        <div className="relative">
                            <button
                                onClick={() => setMenuOpen(!menuOpen)}
                                className="px-5 py-2 rounded-full bg-white/20 border border-white/30 hover:bg-white/30 transition shadow"
                            >
                                Paneller ▼
                            </button>
                            {menuOpen && (
                                <div className="absolute right-0 mt-2 w-56 bg-white text-gray-800 rounded-lg shadow-lg overflow-hidden z-20">
                                    <Link to="/admin" className="block px-4 py-2 hover:bg-gray-100" onClick={() => setMenuOpen(false)}>Admin Paneli</Link>
                                    <Link to="/admin/departments" className="block px-4 py-2 hover:bg-gray-100" onClick={() => setMenuOpen(false)}>Departman Kontrol Paneli</Link>
                                    <Link to="/admin/create-user" className="block px-4 py-2 hover:bg-gray-100" onClick={() => setMenuOpen(false)}>Kişi Oluşturma Paneli</Link>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Table */}
                <div className="p-6">
                    {loading ? (
                        <p>Yükleniyor...</p>
                    ) : (
                        <>
                            <table className="w-full border-collapse rounded-xl shadow-lg">
                                <thead>
                                <tr className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white text-left text-sm">
                                    <th className="px-6 py-3">İşlemler</th>
                                    <th className="px-6 py-3">Ad</th>
                                    <th className="px-6 py-3">Soyad</th>
                                    <th className="px-6 py-3">Email</th>
                                    <th className="px-6 py-3">Telefon</th>
                                    <th className="px-6 py-3">Rol</th>
                                </tr>
                                </thead>
                                <tbody>
                                {currentUsers.map((u) => (
                                    <tr key={u.id} className="hover:bg-gray-50 relative">
                                        <td className="px-6 py-4 relative">
                                            <div className="relative inline-block text-left">
                                                <button
                                                    ref={(el) => { buttonRefs.current[u.id] = el; }}
                                                    onClick={() => setOpenDropdown(openDropdown === u.id ? null : u.id)}
                                                    className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg shadow font-medium transition"
                                                >
                                                    ⚙️ İşlemler ▼
                                                </button>

                                                {openDropdown === u.id && (
                                                    <div className="absolute left-0 mt-2 w-56 bg-white rounded-lg shadow-xl ring-1 ring-black ring-opacity-5 z-50">
                                                        <div className="py-1">
                                                            {/* Rol Ver ana menü */}
                                                            <div className="group relative">
                                                                <button
                                                                    className="flex justify-between items-center w-full px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-700"
                                                                >
                                                                    🎭 Rol Ver
                                                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                                                    </svg>
                                                                </button>
                                                                {/* Alt menü */}
                                                                <div className="absolute top-0 left-full ml-1 w-56 bg-white rounded-lg shadow-lg hidden group-hover:block">
                                                                    <button
                                                                        onClick={() => setConfirmAction({ userId: u.id, role: "USER" })}
                                                                        className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-700"
                                                                    >
                                                                        👤 Genel Kullanıcı
                                                                    </button>
                                                                    <button
                                                                        onClick={() => setConfirmAction({ userId: u.id, role: "PERSON" })}
                                                                        className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-700"
                                                                    >
                                                                        💼 Yetkili Çalışan
                                                                    </button>
                                                                    <button
                                                                        onClick={() => setConfirmAction({ userId: u.id, role: "ADMIN" })}
                                                                        className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-700"
                                                                    >
                                                                        🛡️ Yönetici
                                                                    </button>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">{u.name}</td>
                                        <td className="px-6 py-4">{u.surname}</td>
                                        <td className="px-6 py-4">{u.email}</td>
                                        <td className="px-6 py-4">{u.phone}</td>
                                        <td className="px-6 py-4">{roleLabel(u.role)}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>

                            {/* ✅ Pagination */}
                            {totalPages > 1 && (
                                <div className="flex justify-center items-center gap-4 mt-6">
                                    <button
                                        disabled={currentPage === 1}
                                        onClick={() => setCurrentPage((prev) => prev - 1)}
                                        className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300 disabled:opacity-50"
                                    >
                                        Önceki
                                    </button>
                                    <span>Sayfa {currentPage} / {totalPages}</span>
                                    <button
                                        disabled={currentPage === totalPages}
                                        onClick={() => setCurrentPage((prev) => prev + 1)}
                                        className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300 disabled:opacity-50"
                                    >
                                        Sonraki
                                    </button>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>

            {/* Confirm Modal */}
            {confirmAction && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/40 z-50">
                    <div className="bg-white p-6 rounded-xl shadow-lg w-96">
                        <p className="mb-4">
                            Bu kullanıcıya <b>{roleLabel(confirmAction.role)}</b> rolü vermek istediğinize emin misiniz?
                        </p>
                        <div className="flex justify-end gap-3">
                            <button
                                onClick={() => setConfirmAction(null)}
                                className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                            >
                                Hayır
                            </button>
                            <button
                                onClick={() => updateRole(confirmAction.userId, confirmAction.role)}
                                className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
                            >
                                Evet
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default RolesPage;
