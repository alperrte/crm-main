// src/pages/CreateUserPage.tsx
import React, { useEffect, useState, useRef } from "react";
import { Link } from "react-router-dom";
import api from "../api/userApi";
import departmentApi, { Department } from "../api/departmentApi";

interface User {
    id: number;
    name: string;
    surname: string;
    email: string;
    phone: string;
    role: string | null;
    departmentId?: number | null;   // ✅ eklendi
    departmentName?: string | null;
}

const CreateUserPage: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(false);
    const [departments, setDepartments] = useState<Department[]>([]);
    const [modalOpen, setModalOpen] = useState(false);
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [passwordModalOpen, setPasswordModalOpen] = useState(false);
    const [confirmUserId, setConfirmUserId] = useState<number | null>(null);
    const [menuOpen, setMenuOpen] = useState(false);
    const [form, setForm] = useState({
        name: "",
        surname: "",
        email: "",
        phone: "",
        password: "",
        departmentId: "",
    });
    const [editForm, setEditForm] = useState<User | null>(null);
    const [passwordForm, setPasswordForm] = useState({ id: 0, password: "" });
    const [openDropdown, setOpenDropdown] = useState<number | null>(null);
    const buttonRefs = useRef<Record<number, HTMLButtonElement | null>>({});

    useEffect(() => {
        fetchUsers();
        loadDepartments();
    }, []);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const res = await api.get("/admin/users");
            const noRoleUsers = res.data.filter((u: User) => !u.role);
            setUsers(noRoleUsers);
        } catch (err) {
            console.error("❌ Kullanıcılar alınamadı:", err);
        } finally {
            setLoading(false);
        }
    };

    const loadDepartments = async () => {
        try {
            const res = await departmentApi.get("/api/departments");
            setDepartments(res.data);
        } catch (err) {
            console.error("❌ Departmanlar alınamadı:", err);
        }
    };

    const handleSubmit = async () => {
        try {
            await api.post("/admin/users", form);
            setForm({ name: "", surname: "", email: "", phone: "", password: "", departmentId: "" });
            setModalOpen(false);
            fetchUsers();
        } catch (err) {
            console.error("❌ Yeni kişi eklenemedi:", err);
        }
    };

    const makeUser = async (id: number) => {
        try {
            await api.put(`/admin/users/${id}/role?role=USER`);
            setConfirmUserId(null);
            fetchUsers();
        } catch (err) {
            console.error("❌ Kullanıcı rolü atanamadı:", err);
        }
    };

    const handleUpdate = async () => {
        if (!editForm) return;
        try {
            await api.put(`/admin/users/${editForm.id}`, editForm);
            setEditModalOpen(false);
            setEditForm(null);
            fetchUsers();
        } catch (err) {
            console.error("❌ Kullanıcı güncellenemedi:", err);
        }
    };

    const handlePasswordUpdate = async () => {
        if (!passwordForm.id || !passwordForm.password) return;
        try {
            await api.put(`/admin/users/${passwordForm.id}`, { password: passwordForm.password });
            setPasswordModalOpen(false);
            setPasswordForm({ id: 0, password: "" });
            fetchUsers();
        } catch (err) {
            console.error("❌ Parola güncellenemedi:", err);
        }
    };

    return (
        <div className="min-h-screen p-6 bg-gradient-to-br from-indigo-400 to-purple-600 flex justify-center">
            <div className="w-full max-w-6xl bg-white rounded-2xl shadow-2xl overflow-hidden">
                {/* Header */}
                <div className="flex justify-between items-center bg-gradient-to-r from-blue-400 to-cyan-400 text-white p-6 relative">
                    <h1 className="text-2xl font-bold">Kişi Yönetimi</h1>
                    <div className="flex items-center gap-4">
                        <button
                            onClick={() => setModalOpen(true)}
                            className="px-5 py-2 rounded-full bg-white/20 border border-white/30 hover:bg-white/30 transition shadow"
                        >
                            + Yeni Kişi Oluştur
                        </button>

                        {/* Kontroller Dropdown */}
                        <div className="relative">
                            <button
                                onClick={() => setMenuOpen(!menuOpen)}
                                className="px-5 py-2 rounded-full bg-white/20 border border-white/30 hover:bg-white/30 transition shadow"
                            >
                                Kontroller ▼
                            </button>
                            {menuOpen && (
                                <div className="absolute right-0 mt-2 w-56 bg-white text-gray-800 rounded-lg shadow-lg overflow-hidden z-20">
                                    <Link to="/admin" className="block px-4 py-2 hover:bg-gray-100">Admin Panel</Link>
                                    <Link to="/admin/departments" className="block px-4 py-2 hover:bg-gray-100">Departman Kontrolleri</Link>
                                    <Link to="/admin/roles" className="block px-4 py-2 hover:bg-gray-100">Rol Kontrolleri</Link>
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
                        <table className="table-auto w-full border-collapse rounded-xl shadow-lg relative">
                            <thead>
                            <tr className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white text-left text-sm">
                                <th className="px-6 py-3">İşlemler</th>
                                <th className="px-6 py-3">Ad</th>
                                <th className="px-6 py-3">Soyad</th>
                                <th className="px-6 py-3">Email</th>
                                <th className="px-6 py-3">Telefon</th>
                                <th className="px-6 py-3">Departman</th>
                            </tr>
                            </thead>
                            <tbody>
                            {users.map((u) => (
                                <tr key={u.id} className="hover:bg-gray-50 relative">
                                    <td className="px-6 py-4 relative">
                                        <button
                                            ref={(el) => { buttonRefs.current[u.id] = el; }}
                                            onClick={() => setOpenDropdown(openDropdown === u.id ? null : u.id)}
                                            className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white px-4 py-2 rounded-full font-semibold"
                                        >
                                            İşlemler ▼
                                        </button>
                                        {openDropdown === u.id && (
                                            <div className="absolute mt-2 bg-white shadow-lg rounded-lg z-50">
                                                <button
                                                    onClick={(e) => { e.stopPropagation(); setConfirmUserId(u.id); setOpenDropdown(null); }}
                                                    className="block px-4 py-2 hover:bg-gray-100 w-full text-left"
                                                >
                                                    Kullanıcı Yap
                                                </button>
                                                <button
                                                    onClick={(e) => { e.stopPropagation(); setEditForm(u); setEditModalOpen(true); setOpenDropdown(null); }}
                                                    className="block px-4 py-2 hover:bg-gray-100 w-full text-left"
                                                >
                                                    Düzenle
                                                </button>
                                                <button
                                                    onClick={(e) => { e.stopPropagation(); setPasswordForm({ id: u.id, password: "" }); setPasswordModalOpen(true); setOpenDropdown(null); }}
                                                    className="block px-4 py-2 hover:bg-gray-100 w-full text-left"
                                                >
                                                    Parola Güncelle
                                                </button>
                                            </div>
                                        )}
                                    </td>
                                    <td className="px-6 py-4">{u.name}</td>
                                    <td className="px-6 py-4">{u.surname}</td>
                                    <td className="px-6 py-4">{u.email}</td>
                                    <td className="px-6 py-4">{u.phone}</td>
                                    <td className="px-6 py-4">
                                        {u.departmentName ?? (u.departmentId ? `ID: ${u.departmentId}` : "—")}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>

            {/* Confirm Modal */}
            {confirmUserId !== null && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/40 z-50">
                    <div className="bg-white p-6 rounded-xl shadow-lg w-96">
                        <p className="mb-4">Bu kişiyi <b>Genel Kullanıcı</b> yapmak istediğinize emin misiniz?</p>
                        <div className="flex justify-end gap-3">
                            <button onClick={() => setConfirmUserId(null)} className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300">Hayır</button>
                            <button onClick={() => makeUser(confirmUserId)} className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">Evet</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Yeni Kişi Oluştur Modal */}
            {modalOpen && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/40 z-50">
                    <div className="bg-white p-6 rounded-xl shadow-lg w-96">
                        <h2 className="text-lg font-bold mb-4">Yeni Kişi Oluştur</h2>
                        <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Ad" className="w-full p-2 border rounded mb-2" />
                        <input type="text" value={form.surname} onChange={(e) => setForm({ ...form, surname: e.target.value })} placeholder="Soyad" className="w-full p-2 border rounded mb-2" />
                        <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="Email" className="w-full p-2 border rounded mb-2" />
                        <input type="text" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="Telefon" className="w-full p-2 border rounded mb-2" />
                        <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="Parola" className="w-full p-2 border rounded mb-2" />
                        <select value={form.departmentId} onChange={(e) => setForm({ ...form, departmentId: e.target.value })} className="w-full p-2 border rounded mb-4">
                            <option value="">Departman Seç</option>
                            {departments.map((d) => (
                                <option key={d.id} value={d.id}>{d.name}</option>
                            ))}
                        </select>
                        <div className="flex justify-end gap-3">
                            <button onClick={() => setModalOpen(false)} className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300">İptal</button>
                            <button onClick={handleSubmit} className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">Kaydet</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Düzenle Modal */}
            {editModalOpen && editForm && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/40 z-50">
                    <div className="bg-white p-6 rounded-xl shadow-lg w-96">
                        <h2 className="text-lg font-bold mb-4">Kişiyi Düzenle</h2>
                        <input type="text" value={editForm.name} onChange={(e) => setEditForm({ ...editForm, name: e.target.value })} placeholder="Ad" className="w-full p-2 border rounded mb-2" />
                        <input type="text" value={editForm.surname} onChange={(e) => setEditForm({ ...editForm, surname: e.target.value })} placeholder="Soyad" className="w-full p-2 border rounded mb-2" />
                        <input type="email" value={editForm.email} onChange={(e) => setEditForm({ ...editForm, email: e.target.value })} placeholder="Email" className="w-full p-2 border rounded mb-2" />
                        <input type="text" value={editForm.phone} onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })} placeholder="Telefon" className="w-full p-2 border rounded mb-2" />
                        <select value={editForm.departmentId ?? ""} onChange={(e) => setEditForm({ ...editForm, departmentId: Number(e.target.value) })} className="w-full p-2 border rounded mb-4">
                            <option value="">Departman Seç</option>
                            {departments.map((d) => (
                                <option key={d.id} value={d.id}>{d.name}</option>
                            ))}
                        </select>
                        <div className="flex justify-end gap-3">
                            <button onClick={() => { setEditModalOpen(false); setEditForm(null); }} className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300">İptal</button>
                            <button onClick={handleUpdate} className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">Kaydet</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Parola Güncelle Modal */}
            {passwordModalOpen && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/40 z-50">
                    <div className="bg-white p-6 rounded-xl shadow-lg w-96">
                        <h2 className="text-lg font-bold mb-4">Parola Güncelle</h2>
                        <input type="password" value={passwordForm.password} onChange={(e) => setPasswordForm({ ...passwordForm, password: e.target.value })} placeholder="Yeni Parola" className="w-full p-2 border rounded mb-4" />
                        <div className="flex justify-end gap-3">
                            <button onClick={() => { setPasswordModalOpen(false); setPasswordForm({ id: 0, password: "" }); }} className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300">İptal</button>
                            <button onClick={handlePasswordUpdate} className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">Güncelle</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CreateUserPage;
