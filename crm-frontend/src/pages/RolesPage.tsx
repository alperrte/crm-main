import React, { useEffect, useState, useRef } from "react";
import api from "../api/userApi";
import { Link } from "react-router-dom";

interface User {
    id: number;
    name: string;
    surname: string;
    email: string;
    phone: string;
    role: string;
}

const RolesPage: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(false);
    const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);
    const [openDropdown, setOpenDropdown] = useState<number | null>(null);
    const [dropdownPos, setDropdownPos] = useState<{ top: number; left: number } | null>(null);

    const [menuOpen, setMenuOpen] = useState(false);
    const buttonRefs = useRef<Record<number, HTMLButtonElement | null>>({});

    // ✅ Yeni kullanıcı oluşturma modal state
    const [createModalOpen, setCreateModalOpen] = useState(false);
    const [newUser, setNewUser] = useState({
        name: "",
        surname: "",
        email: "",
        phone: "",
        password: "",
    });

    // ✅ Kullanıcıları getir
    const fetchUsers = async () => {
        setLoading(true);
        try {
            const res = await api.get("/admin/users"); // interceptor zaten token ekliyor
            console.log("👉 Kullanıcılar:", res.data);
            setUsers(res.data);
        } catch (err) {
            console.error("❌ Kullanıcılar alınamadı:", err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    // ✅ Rol güncelleme
    const updateRole = async (id: number, role: string) => {
        try {
            await api.put(`/admin/users/${id}/role?role=${role}`);
            setUsers((prev) =>
                prev.map((u) => (u.id === id ? { ...u, role } : u))
            );
        } catch (err) {
            console.error("❌ Rol güncellenemedi:", err);
        } finally {
            setOpenDropdown(null);
        }
    };

    // ✅ Person yap
    const makePerson = async (id: number) => {
        try {
            await api.put(`/admin/users/${id}/make-person`);
            setUsers((prev) =>
                prev.map((u) => (u.id === id ? { ...u, role: "PERSON" } : u))
            );
            setOpenDropdown(null);
        } catch (err) {
            console.error("❌ makePerson hatası:", err);
        }
    };

    // ✅ Kullanıcı sil
    const deleteUser = async (id: number) => {
        try {
            await api.delete(`/admin/users/${id}`);
            setUsers((prev) => prev.filter((u) => u.id !== id));
        } catch (err) {
            console.error("❌ Kullanıcı silinemedi:", err);
        } finally {
            setConfirmDeleteId(null);
        }
    };

    // ✅ Yeni kullanıcı oluştur
    const createUser = async () => {
        try {
            const res = await api.post("/admin/users", newUser);
            setUsers((prev) => [...prev, res.data]);
            setCreateModalOpen(false);
            setNewUser({
                name: "",
                surname: "",
                email: "",
                phone: "",
                password: "",
            });
        } catch (err) {
            console.error("❌ Yeni kullanıcı eklenemedi:", err);
        }
    };

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

                    {/* Kontroller dropdown */}
                    <div className="relative flex gap-4 items-center">
                        <button
                            onClick={() => setCreateModalOpen(true)}
                            className="px-5 py-2 rounded-full bg-white/20 border border-white/30 hover:bg-white/30 transition shadow"
                        >
                            + Kullanıcı Oluştur
                        </button>
                        <button
                            onClick={() => setMenuOpen(!menuOpen)}
                            className="px-5 py-2 rounded-full bg-white/20 border border-white/30 hover:bg-white/30 transition shadow"
                        >
                            Kontroller
                        </button>
                        {menuOpen && (
                            <div className="absolute right-0 mt-2 w-56 bg-white text-gray-800 rounded-lg shadow-lg overflow-hidden z-20">
                                <Link to="/admin" className="block px-4 py-2 hover:bg-gray-100">Admin Panel</Link>
                                <Link to="/admin/departments" className="block px-4 py-2 hover:bg-gray-100">Departman Kontrolleri</Link>
                                <Link to="/admin/persons" className="block px-4 py-2 hover:bg-gray-100">Person Kontrolleri</Link>
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
                                <th className="px-6 py-3">Ad</th>
                                <th className="px-6 py-3">Soyad</th>
                                <th className="px-6 py-3">Email</th>
                                <th className="px-6 py-3">Telefon</th>
                                <th className="px-6 py-3">Rol</th>
                                <th className="px-6 py-3">İşlemler</th>
                            </tr>
                            </thead>
                            <tbody>
                            {users.map((u) => (
                                <tr key={u.id} className="hover:bg-gray-50 transition transform hover:-translate-y-0.5">
                                    <td className="px-6 py-4">{u.id}</td>
                                    <td className="px-6 py-4">{u.name}</td>
                                    <td className="px-6 py-4">{u.surname}</td>
                                    <td className="px-6 py-4">{u.email}</td>
                                    <td className="px-6 py-4">{u.phone}</td>
                                    <td className="px-6 py-4">
                                        <span className={`px-4 py-2 rounded-full text-sm font-semibold uppercase ${roleBadgeClass(u.role)}`}>
                                            {u.role}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4">
                                        <button
                                            ref={(el) => { buttonRefs.current[u.id] = el; }}
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
                                    <td colSpan={7} className="text-center text-gray-500 py-10">Kayıt yok</td>
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
                    <button onClick={() => updateRole(openDropdown, "ADMIN")} className="block w-full text-left px-5 py-3 hover:bg-gray-100">Admin Yap</button>
                    <button onClick={() => makePerson(openDropdown)} className="block w-full text-left px-5 py-3 hover:bg-gray-100">Person Yap</button>
                    <button onClick={() => updateRole(openDropdown, "USER")} className="block w-full text-left px-5 py-3 hover:bg-gray-100">User Yap</button>
                    <button onClick={() => setConfirmDeleteId(openDropdown)} className="block w-full text-left px-5 py-3 text-red-600 hover:bg-red-50">Sil</button>
                </div>
            )}

            {/* Silme onay kutusu */}
            {confirmDeleteId !== null && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/40">
                    <div className="bg-white p-6 rounded-xl shadow-lg w-80">
                        <p className="mb-4">Bu kullanıcıyı silmek istediğinize emin misiniz?</p>
                        <div className="flex justify-end space-x-4">
                            <button onClick={() => setConfirmDeleteId(null)} className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300">Hayır</button>
                            <button onClick={() => deleteUser(confirmDeleteId)} className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700">Evet</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Yeni kullanıcı oluştur modal */}
            {createModalOpen && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/40">
                    <div className="bg-white p-6 rounded-xl shadow-lg w-[400px]">
                        <h2 className="text-xl font-semibold mb-4">Yeni Kullanıcı Oluştur</h2>
                        <div className="space-y-3">
                            <input type="text" placeholder="Ad"
                                   className="w-full border px-3 py-2 rounded"
                                   value={newUser.name}
                                   onChange={(e) => setNewUser({ ...newUser, name: e.target.value })}/>
                            <input type="text" placeholder="Soyad"
                                   className="w-full border px-3 py-2 rounded"
                                   value={newUser.surname}
                                   onChange={(e) => setNewUser({ ...newUser, surname: e.target.value })}/>
                            <input type="email" placeholder="Email"
                                   className="w-full border px-3 py-2 rounded"
                                   value={newUser.email}
                                   onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}/>
                            <input type="text" placeholder="Telefon"
                                   className="w-full border px-3 py-2 rounded"
                                   value={newUser.phone}
                                   onChange={(e) => setNewUser({ ...newUser, phone: e.target.value })}/>
                            <input type="password" placeholder="Şifre"
                                   className="w-full border px-3 py-2 rounded"
                                   value={newUser.password}
                                   onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}/>
                        </div>
                        <div className="flex justify-end space-x-4 mt-4">
                            <button onClick={() => setCreateModalOpen(false)} className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300">İptal</button>
                            <button onClick={createUser} className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">Oluştur</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default RolesPage;
