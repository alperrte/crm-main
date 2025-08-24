import React, { useEffect, useState, useRef } from "react";
import api from "../api/userApi";
import { Link } from "react-router-dom";

interface User {
    id: number;
    username: string;
    email: string;
    role: string;
}

const RolesPage: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(false);
    const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);
    const [openDropdown, setOpenDropdown] = useState<number | null>(null);
    const [dropdownPos, setDropdownPos] = useState<{ top: number; left: number } | null>(null);

    // 🔹 Kontroller menüsü için state
    const [menuOpen, setMenuOpen] = useState(false);

    // 🔹 Buton referanslarını map olarak tutuyoruz
    const buttonRefs = useRef<Record<number, HTMLButtonElement | null>>({});

    // Kullanıcıları getir
    const fetchUsers = async () => {
        setLoading(true);
        try {
            const res = await api.get("/admin/users", {
                headers: { Authorization: "Bearer " + localStorage.getItem("token") },
            });
            setUsers(res.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    // Rol güncelleme
    const updateRole = async (id: number, role: string) => {
        try {
            await api.put(
                `/admin/users/${id}/role?role=${role}`,
                {},
                { headers: { Authorization: "Bearer " + localStorage.getItem("token") } }
            );
            setUsers((prev) =>
                prev.map((u) => (u.id === id ? { ...u, role } : u))
            );
        } catch (err) {
            console.error(err);
        } finally {
            setOpenDropdown(null);
        }
    };

    // Kullanıcı silme
    const deleteUser = async (id: number) => {
        try {
            await api.delete(`/admin/users/${id}`, {
                headers: { Authorization: "Bearer " + localStorage.getItem("token") },
            });
            setUsers((prev) => prev.filter((u) => u.id !== id));
        } catch (err) {
            console.error(err);
        } finally {
            setConfirmDeleteId(null);
        }
    };

    // Badge renkleri
    const roleBadgeClass = (role: string) => {
        switch (role) {
            case "ADMIN":
                return "bg-gradient-to-r from-red-500 to-orange-500 text-white";
            case "PERSON":
                return "bg-gradient-to-r from-teal-400 to-green-500 text-white";
            case "USER":
                return "bg-gradient-to-r from-pink-400 to-purple-500 text-white";
            default:
                return "bg-gray-300 text-gray-800";
        }
    };

    // Dropdown pozisyonunu hesapla
    const handleDropdownToggle = (id: number) => {
        if (openDropdown === id) {
            setOpenDropdown(null);
            return;
        }
        const btn = buttonRefs.current[id];
        if (btn) {
            const rect = btn.getBoundingClientRect();
            setDropdownPos({ top: rect.bottom + window.scrollY, left: rect.left });
        }
        setOpenDropdown(id);
    };

    return (
        <div className="min-h-screen p-6 bg-gradient-to-br from-indigo-400 to-purple-600 flex justify-center">
            <div className="w-full max-w-6xl bg-white rounded-2xl shadow-2xl overflow-hidden">
                {/* Header */}
                <div className="bg-gradient-to-r from-blue-400 to-cyan-400 text-white p-8 flex justify-between items-center relative">
                    <div className="text-center flex-1">
                        <h1 className="text-3xl font-light mb-2">Rol Kontrolleri</h1>
                        <p className="opacity-90 text-lg">Kullanıcı rollerini yönetin ve düzenleyin</p>
                    </div>

                    {/* 🔹 Kontroller dropdown menüsü */}
                    <div className="relative">
                        <button
                            onClick={() => setMenuOpen(!menuOpen)}
                            className="px-5 py-2 rounded-full bg-white/20 border border-white/30 hover:bg-white/30 transition shadow"
                        >
                            Kontroller
                        </button>
                        {menuOpen && (
                            <div className="absolute right-0 mt-2 w-56 bg-white text-gray-800 rounded-lg shadow-lg overflow-hidden z-20">
                                <Link
                                    to="/admin"
                                    className="block px-4 py-2 hover:bg-gray-100"
                                >
                                    Admin Panel
                                </Link>
                                <a
                                    href="http://localhost:3000/admin/departments"
                                    className="block px-4 py-2 hover:bg-gray-100"
                                >
                                    Departman Kontrolleri
                                </a>
                            </div>
                        )}
                    </div>
                </div>

                {/* Table */}
                <div className="p-6 overflow-x-auto">
                    {loading ? (
                        <p>Yükleniyor...</p>
                    ) : (
                        <table className="w-full border-collapse rounded-xl shadow-lg">
                            <thead>
                            <tr className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white text-left text-sm tracking-wide">
                                <th className="px-6 py-3">ID</th>
                                <th className="px-6 py-3">Kullanıcı Adı</th>
                                <th className="px-6 py-3">Email</th>
                                <th className="px-6 py-3">Rol</th>
                                <th className="px-6 py-3">İşlemler</th>
                            </tr>
                            </thead>
                            <tbody>
                            {users.map((u) => (
                                <tr
                                    key={u.id}
                                    className="hover:bg-gray-50 transition transform hover:-translate-y-0.5"
                                >
                                    <td className="px-6 py-4">{u.id}</td>
                                    <td className="px-6 py-4">{u.username}</td>
                                    <td className="px-6 py-4">{u.email}</td>
                                    <td className="px-6 py-4">
                                            <span
                                                className={`px-4 py-2 rounded-full text-sm font-semibold uppercase ${roleBadgeClass(
                                                    u.role
                                                )}`}
                                            >
                                                {u.role}
                                            </span>
                                    </td>
                                    <td className="px-6 py-4">
                                        <button
                                            ref={(el) => {
                                                buttonRefs.current[u.id] = el;
                                            }}
                                            onClick={() => handleDropdownToggle(u.id)}
                                            className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white px-4 py-2 rounded-full font-semibold flex items-center gap-2"
                                        >
                                            İşlemler ▼
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {users.length === 0 && (
                                <tr>
                                    <td colSpan={5} className="text-center text-gray-500 py-10">
                                        Kayıt yok
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>

            {/* Floating Dropdown */}
            {openDropdown !== null && dropdownPos && (
                <div
                    className="fixed bg-white rounded-xl shadow-xl z-50 min-w-[200px]"
                    style={{ top: dropdownPos.top + 5, left: dropdownPos.left }}
                >
                    <button
                        onClick={() => updateRole(openDropdown, "ADMIN")}
                        className="block w-full text-left px-5 py-3 hover:bg-gray-100"
                    >
                        Admin Yap
                    </button>
                    <button
                        onClick={() => updateRole(openDropdown, "PERSON")}
                        className="block w-full text-left px-5 py-3 hover:bg-gray-100"
                    >
                        Person Yap
                    </button>
                    <button
                        onClick={() => updateRole(openDropdown, "USER")}
                        className="block w-full text-left px-5 py-3 hover:bg-gray-100"
                    >
                        User Yap
                    </button>
                    <button
                        onClick={() => setConfirmDeleteId(openDropdown)}
                        className="block w-full text-left px-5 py-3 text-red-600 hover:bg-red-50"
                    >
                        Sil
                    </button>
                </div>
            )}

            {/* Silme Onay Kutusu */}
            {confirmDeleteId !== null && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/40">
                    <div className="bg-white p-6 rounded-xl shadow-lg w-80">
                        <p className="mb-4">Bu kullanıcıyı silmek istediğinize emin misiniz?</p>
                        <div className="flex justify-end space-x-4">
                            <button
                                onClick={() => setConfirmDeleteId(null)}
                                className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                            >
                                Hayır
                            </button>
                            <button
                                onClick={() => deleteUser(confirmDeleteId)}
                                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
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
