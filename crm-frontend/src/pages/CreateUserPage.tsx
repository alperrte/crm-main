// src/pages/CreateUserPage.tsx
import React, { useEffect, useState, useRef } from "react";
import { Link } from "react-router-dom";
import api from "../api/userApi";
import personApi, { Person } from "../api/personApi";
import departmentApi, { Department } from "../api/departmentApi";

interface User {
    id: number;
    personId: number;
}

const CreateUserPage: React.FC = () => {
    const [persons, setPersons] = useState<Person[]>([]);
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(false);
    const [departments, setDepartments] = useState<Department[]>([]);
    const [modalOpen, setModalOpen] = useState(false);
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [confirmPersonId, setConfirmPersonId] = useState<number | null>(null);
    const [menuOpen, setMenuOpen] = useState(false);

    const [form, setForm] = useState({
        name: "",
        surname: "",
        email: "",
        phone: "",
        departmentId: "",
    });
    const [editForm, setEditForm] = useState<Person | null>(null);

    const [openDropdown, setOpenDropdown] = useState<number | null>(null);
    const buttonRefs = useRef<Record<number, HTMLButtonElement | null>>({});

    // ✅ Sayfalama state
    const [currentPage, setCurrentPage] = useState(1);
    const personsPerPage = 6;

    // ✅ Filtreleme state
    const [accessFilter, setAccessFilter] = useState<string>("ALL");

    useEffect(() => {
        void fetchPersons();
        void loadDepartments();
        void fetchUsers();
    }, []);

    const fetchPersons = async () => {
        setLoading(true);
        try {
            const res = await personApi.get("/api/persons");
            setPersons(res.data);
        } catch (err) {
            console.error("❌ Kişiler alınamadı:", err);
        } finally {
            setLoading(false);
        }
    };

    const fetchUsers = async () => {
        try {
            const res = await api.get("/admin/users");
            setUsers(res.data);
        } catch (err) {
            console.error("❌ Kullanıcılar alınamadı:", err);
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

    const getDepartmentName = (id: number | null | undefined) => {
        if (!id) return "—";
        const dep = departments.find((d) => d.id === id);
        return dep ? dep.name : `ID: ${id}`;
    };

    const isActiveUser = (personId: number) => {
        return users.some((u) => u.personId === personId);
    };

    const handleSubmit = async () => {
        try {
            await personApi.post("/api/persons", {
                name: form.name,
                surname: form.surname,
                email: form.email,
                phone: form.phone,
                departmentId: form.departmentId ? Number(form.departmentId) : null,
            });
            setForm({ name: "", surname: "", email: "", phone: "", departmentId: "" });
            setModalOpen(false);
            void fetchPersons();
        } catch (err) {
            console.error("❌ Yeni kişi eklenemedi:", err);
        }
    };

    const makeUser = async (personId: number) => {
        try {
            const password = prompt("Yeni kullanıcı için bir parola girin:");
            if (!password) return;

            await api.post(`/admin/users/from-person/${personId}`, { password });
            setConfirmPersonId(null);
            void fetchPersons();
            void fetchUsers();
        } catch (err) {
            console.error("❌ Kullanıcı oluşturulamadı:", err);
        }
    };

    const deactivateUser = async (personId: number) => {
        try {
            const user = users.find((u) => u.personId === personId);
            if (!user) return;
            if (!window.confirm("Bu kullanıcıyı pasifleştirmek istiyor musunuz?")) return;

            await api.delete(`/admin/users/${user.id}`);
            void fetchUsers();
        } catch (err) {
            console.error("❌ Kullanıcı pasifleştirilemedi:", err);
        }
    };

    const handleUpdate = async () => {
        if (!editForm) return;
        try {
            await personApi.put(`/api/persons/${editForm.id}`, editForm);
            setEditModalOpen(false);
            setEditForm(null);
            void fetchPersons();
        } catch (err) {
            console.error("❌ Kişi güncellenemedi:", err);
        }
    };

    // ✅ Filtrelenmiş kişiler
    const filteredPersons = persons.filter((p) => {
        if (accessFilter === "ALL") return true;
        if (accessFilter === "ACTIVE") return isActiveUser(p.id);
        if (accessFilter === "INACTIVE") return !isActiveUser(p.id);
        return true;
    });

    // ✅ Sayfalama hesaplamaları
    const indexOfLastPerson = currentPage * personsPerPage;
    const indexOfFirstPerson = indexOfLastPerson - personsPerPage;
    const currentPersons = filteredPersons.slice(indexOfFirstPerson, indexOfLastPerson);
    const totalPages = Math.ceil(filteredPersons.length / personsPerPage);

    return (
        <div className="min-h-screen p-6 bg-gradient-to-br from-indigo-400 to-purple-600 flex justify-center">
            <div className="w-full max-w-7xl bg-white rounded-2xl shadow-2xl overflow-hidden">
                {/* Header */}
                <div className="flex justify-between items-center bg-gradient-to-r from-blue-400 to-cyan-400 text-white p-6 relative">
                    <h1 className="text-2xl font-bold">Kişi Yönetimi</h1>
                    <div className="flex items-center gap-4">
                        {/* ✅ Filtre */}
                        <select
                            value={accessFilter}
                            onChange={(e) => { setAccessFilter(e.target.value); setCurrentPage(1); }}
                            className="px-4 py-2 rounded-lg text-gray-800"
                        >
                            <option value="ALL">Hepsi</option>
                            <option value="ACTIVE">Giriş Yetkisi Var</option>
                            <option value="INACTIVE">Giriş Yetkisi Yok</option>
                        </select>

                        <button
                            onClick={() => setModalOpen(true)}
                            className="px-5 py-2 rounded-full bg-white/20 border border-white/30 hover:bg-white/30 transition shadow"
                        >
                            + Yeni Kişi Oluştur
                        </button>

                        <div className="relative">
                            <button
                                onClick={() => setMenuOpen(!menuOpen)}
                                className="px-5 py-2 rounded-full bg-white/20 border border-white/30 hover:bg-white/30 transition shadow"
                            >
                                Paneller ▼
                            </button>
                            {menuOpen && (
                                <div className="absolute right-0 mt-2 w-56 bg-white text-gray-800 rounded-lg shadow-lg overflow-hidden z-20">
                                    <Link to="/admin" className="block px-4 py-2 hover:bg-gray-100">Admin Paneli</Link>
                                    <Link to="/admin/departments" className="block px-4 py-2 hover:bg-gray-100">Departman Kontrol Paneli</Link>
                                    <Link to="/admin/roles" className="block px-4 py-2 hover:bg-gray-100">Rol Kontrol Paneli</Link>
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
                            <table className="table-auto w-full border-collapse rounded-xl shadow-lg relative">
                                <thead>
                                <tr className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white text-left text-sm">
                                    <th className="px-6 py-3">İşlemler</th>
                                    <th className="px-6 py-3">Ad</th>
                                    <th className="px-6 py-3">Soyad</th>
                                    <th className="px-6 py-3">Email</th>
                                    <th className="px-6 py-3">Telefon</th>
                                    <th className="px-6 py-3">Departman</th>
                                    <th className="px-6 py-3">Giriş Yetkisi</th>
                                </tr>
                                </thead>
                                <tbody>
                                {currentPersons.map((p) => (
                                    <tr key={p.id} className="hover:bg-gray-50 relative">
                                        <td className="px-6 py-4 relative">
                                            <button
                                                ref={(el) => { buttonRefs.current[p.id] = el; }}
                                                onClick={() => setOpenDropdown(openDropdown === p.id ? null : p.id)}
                                                className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white px-4 py-2 rounded-full font-semibold"
                                            >
                                                ⚙️ İşlemler ▼
                                            </button>
                                            {openDropdown === p.id && (
                                                <div className="absolute mt-2 bg-white shadow-lg rounded-lg z-50">
                                                    {!isActiveUser(p.id) ? (
                                                        <button
                                                            onClick={(e) => { e.stopPropagation(); setConfirmPersonId(p.id); setOpenDropdown(null); }}
                                                            className="block px-4 py-2 hover:bg-gray-100 w-full text-left"
                                                        >
                                                            👤 Giriş Yetkisi Ver
                                                        </button>
                                                    ) : (
                                                        <button
                                                            onClick={(e) => { e.stopPropagation(); deactivateUser(p.id); setOpenDropdown(null); }}
                                                            className="block px-4 py-2 hover:bg-gray-100 w-full text-left text-red-600"
                                                        >
                                                            ❌ Giriş Yetkisini Al
                                                        </button>
                                                    )}
                                                    <button
                                                        onClick={(e) => { e.stopPropagation(); setEditForm(p); setEditModalOpen(true); setOpenDropdown(null); }}
                                                        className="block px-4 py-2 hover:bg-gray-100 w-full text-left"
                                                    >
                                                        ✏️ Düzenle
                                                    </button>
                                                </div>
                                            )}
                                        </td>
                                        <td className="px-6 py-4">{p.name}</td>
                                        <td className="px-6 py-4">{p.surname}</td>
                                        <td className="px-6 py-4">{p.email}</td>
                                        <td className="px-6 py-4">{p.phone}</td>
                                        <td className="px-6 py-4">{getDepartmentName(p.departmentId)}</td>
                                        <td className="px-6 py-4">
                                            {isActiveUser(p.id) ? (
                                                <span className="flex items-center text-green-600 font-medium">
                                                    <span className="w-2 h-2 bg-green-600 rounded-full mr-2"></span>
                                                    Var
                                                </span>
                                            ) : (
                                                <span className="flex items-center text-gray-500 italic">
                                                    <span className="w-2 h-2 bg-gray-400 rounded-full mr-2"></span>
                                                    Yok
                                                </span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>

                            {/* ✅ Pagination */}
                            {totalPages > 1 && (
                                <div className="flex justify-center items-center gap-2 mt-6">
                                    <button
                                        disabled={currentPage === 1}
                                        onClick={() => setCurrentPage((prev) => prev - 1)}
                                        className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300 disabled:opacity-50"
                                    >
                                        Önceki
                                    </button>

                                    {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                                        <button
                                            key={page}
                                            onClick={() => setCurrentPage(page)}
                                            className={`px-3 py-1 rounded ${
                                                currentPage === page
                                                    ? "bg-indigo-600 text-white"
                                                    : "bg-gray-200 hover:bg-gray-300"
                                            }`}
                                        >
                                            {page}
                                        </button>
                                    ))}

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
            {confirmPersonId !== null && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/40 z-50">
                    <div className="bg-white p-6 rounded-xl shadow-lg w-96">
                        <p className="mb-4">Bu kişiyi <b>Genel Kullanıcı</b> yapmak istediğinize emin misiniz?</p>
                        <div className="flex justify-end gap-3">
                            <button onClick={() => setConfirmPersonId(null)} className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300">Hayır</button>
                            <button onClick={() => makeUser(confirmPersonId)} className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">Evet</button>
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
                        <select
                            value={form.departmentId}
                            onChange={(e) => setForm({ ...form, departmentId: e.target.value })}
                            className="w-full p-2 border rounded mb-4"
                        >
                            <option value="">Departman Seç</option>
                            {departments.map((d) => (
                                <option key={d.id} value={String(d.id)}>{d.name}</option>
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
                        <input type="text" value={editForm.name ?? ""} onChange={(e) => setEditForm({ ...editForm, name: e.target.value })} placeholder="Ad" className="w-full p-2 border rounded mb-2" />
                        <input type="text" value={editForm.surname ?? ""} onChange={(e) => setEditForm({ ...editForm, surname: e.target.value })} placeholder="Soyad" className="w-full p-2 border rounded mb-2" />
                        <input type="email" value={editForm.email ?? ""} onChange={(e) => setEditForm({ ...editForm, email: e.target.value })} placeholder="Email" className="w-full p-2 border rounded mb-2" />
                        <input type="text" value={editForm.phone ?? ""} onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })} placeholder="Telefon" className="w-full p-2 border rounded mb-2" />
                        <select
                            value={editForm.departmentId != null ? String(editForm.departmentId) : ""}
                            onChange={(e) =>
                                setEditForm({
                                    ...editForm,
                                    departmentId: e.target.value ? Number(e.target.value) : null,
                                })
                            }
                            className="w-full p-2 border rounded mb-4"
                        >
                            <option value="">Departman Seç</option>
                            {departments.map((d) => (
                                <option key={d.id} value={String(d.id)}>{d.name}</option>
                            ))}
                        </select>
                        <div className="flex justify-end gap-3">
                            <button onClick={() => { setEditModalOpen(false); setEditForm(null); }} className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300">İptal</button>
                            <button onClick={handleUpdate} className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">Kaydet</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CreateUserPage;
