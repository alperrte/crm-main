import React, { useEffect, useState } from "react";
import departmentApi from "../api/departmentApi";
import { Link } from "react-router-dom";

type Department = {
    id: number;
    name: string;
    parentDepartmentId?: number | null;
};

const emptyForm: Omit<Department, "id"> = { name: "", parentDepartmentId: null };

const DepartmentsPage: React.FC = () => {
    const [items, setItems] = useState<Department[]>([]);
    const [loading, setLoading] = useState(false);

    const [showForm, setShowForm] = useState(false);
    const [isEdit, setIsEdit] = useState(false);
    const [editId, setEditId] = useState<number | null>(null);
    const [form, setForm] = useState<Omit<Department, "id">>(emptyForm);

    // ✅ Dropdown state
    const [menuOpen, setMenuOpen] = useState(false);

    // ✅ Silme onay için state
    const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);

    // ✅ Expand/collapse state
    const [expanded, setExpanded] = useState<Record<number, boolean>>({});

    const fetchAll = async () => {
        setLoading(true);
        try {
            const res = await departmentApi.get<Department[]>("/api/admin/departments");
            setItems(res.data);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAll();
    }, []);

    const openCreate = () => {
        setIsEdit(false);
        setEditId(null);
        setForm(emptyForm);
        setShowForm(true);
    };

    const openEdit = (row: Department) => {
        setIsEdit(true);
        setEditId(row.id);
        setForm({ name: row.name, parentDepartmentId: row.parentDepartmentId ?? null });
        setShowForm(true);
    };

    const closeForm = () => setShowForm(false);

    const submitForm = async (e: React.FormEvent) => {
        e.preventDefault();
        if (isEdit && editId != null) {
            await departmentApi.put(`/api/admin/departments/${editId}`, form);
        } else {
            await departmentApi.post(`/api/admin/departments`, form);
        }
        closeForm();
        fetchAll();
    };

    // ✅ Silme
    const deleteDepartment = async (id: number) => {
        try {
            await departmentApi.delete(`/api/admin/departments/${id}`);
            setItems((prev) => prev.filter((d) => d.id !== id));
        } catch (err) {
            console.error(err);
        } finally {
            setConfirmDeleteId(null);
        }
    };

    // ✅ Departmanları ayırma
    const mainDepartments = items.filter((d) => !d.parentDepartmentId);
    const getChildren = (parentId: number) =>
        items.filter((d) => d.parentDepartmentId === parentId);

    const toggleExpand = (id: number) => {
        setExpanded((prev) => ({ ...prev, [id]: !prev[id] }));
    };

    return (
        <div className="min-h-screen p-6 bg-gradient-to-br from-indigo-400 to-purple-600 flex justify-center items-start">
            <div className="w-full max-w-5xl bg-white rounded-2xl shadow-2xl overflow-hidden">
                {/* Header */}
                <div className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white p-6 flex justify-between items-center relative">
                    <h1 className="text-3xl font-light tracking-wide">Departman Kontrolleri</h1>

                    <div className="flex items-center gap-4">
                        {/* 🔹 Kontroller dropdown menüsü */}
                        <div className="relative">
                            <button
                                onClick={() => setMenuOpen(!menuOpen)}
                                className="px-5 py-2 rounded-full bg-white/20 border border-white/30 hover:bg-white/30 transition shadow"
                            >
                                Kontroller
                            </button>
                            {menuOpen && (
                                <div className="absolute right-0 mt-2 w-48 bg-white text-gray-800 rounded-lg shadow-lg overflow-hidden z-20">
                                    <Link
                                        to="/admin"
                                        className="block px-4 py-2 hover:bg-gray-100"
                                    >
                                        Admin Panel
                                    </Link>
                                    <a
                                        href="http://localhost:3000/admin/roles"
                                        className="block px-4 py-2 hover:bg-gray-100"
                                    >
                                        Rol Kontrolleri
                                    </a>
                                    <a
                                        href="http://localhost:3000/admin/persons"
                                        className="block px-4 py-2 hover:bg-gray-100"
                                    >
                                        Person Kontrolleri
                                    </a>
                                </div>
                            )}
                        </div>

                        {/* 🔹 +Oluştur butonu */}
                        <button
                            onClick={openCreate}
                            className="flex items-center gap-2 bg-white/20 border-2 border-white/30 px-5 py-2 rounded-full hover:bg-white/30 transition shadow"
                        >
                            <svg
                                width="18"
                                height="18"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                            >
                                <line x1="12" y1="5" x2="12" y2="19" />
                                <line x1="5" y1="12" x2="19" y2="12" />
                            </svg>
                            + Oluştur
                        </button>
                    </div>
                </div>

                {/* Table */}
                <div className="p-6">
                    {loading ? (
                        <p>Yükleniyor…</p>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full border-collapse rounded-xl shadow-lg overflow-hidden">
                                <thead>
                                <tr className="bg-gray-100 text-gray-700 uppercase text-sm tracking-wider">
                                    <th className="px-6 py-3 text-left">ID</th>
                                    <th className="px-6 py-3 text-left">Ad</th>
                                    <th className="px-6 py-3 text-left">Üst Departman</th>
                                    <th className="px-6 py-3 text-left">İşlemler</th>
                                </tr>
                                </thead>
                                <tbody>
                                {mainDepartments.length === 0 && (
                                    <tr>
                                        <td colSpan={4} className="text-center text-gray-500 py-10">
                                            <h3 className="text-lg font-medium">Kayıt yok</h3>
                                        </td>
                                    </tr>
                                )}

                                {mainDepartments.map((dept) => (
                                    <React.Fragment key={dept.id}>
                                        {/* Ana departman satırı */}
                                        <tr className="hover:bg-gray-50">
                                            <td className="px-6 py-4 font-semibold text-indigo-600 w-20">
                                                {dept.id}
                                            </td>
                                            <td
                                                className="px-6 py-4 font-bold text-gray-900 cursor-pointer"
                                                onClick={() => toggleExpand(dept.id)}
                                            >
                                                {expanded[dept.id] ? "▼ " : "▶ "} {dept.name}
                                            </td>
                                            <td className="px-6 py-4 italic text-gray-500">-</td>
                                            <td className="px-6 py-4">
                                                <div className="flex gap-3">
                                                    <button
                                                        onClick={() => openEdit(dept)}
                                                        className="px-4 py-1 rounded-full text-sm font-medium bg-gradient-to-r from-yellow-400 to-yellow-300 text-gray-800 hover:shadow-lg transition"
                                                    >
                                                        Güncelle
                                                    </button>
                                                    <button
                                                        onClick={() => setConfirmDeleteId(dept.id)}
                                                        className="px-4 py-1 rounded-full text-sm font-medium bg-gradient-to-r from-red-500 to-red-400 text-white hover:shadow-lg transition"
                                                    >
                                                        Sil
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>

                                        {/* Alt departmanlar */}
                                        {expanded[dept.id] &&
                                            getChildren(dept.id).map((child) => (
                                                <tr key={child.id} className="bg-gray-50">
                                                    <td className="px-6 py-4 text-indigo-400">{child.id}</td>
                                                    <td className="px-6 py-4 pl-10">↳ {child.name}</td>
                                                    <td className="px-6 py-4">{dept.name}</td>
                                                    <td className="px-6 py-4">
                                                        <div className="flex gap-3">
                                                            <button
                                                                onClick={() => openEdit(child)}
                                                                className="px-4 py-1 rounded-full text-sm font-medium bg-gradient-to-r from-yellow-400 to-yellow-300 text-gray-800 hover:shadow-lg transition"
                                                            >
                                                                Güncelle
                                                            </button>
                                                            <button
                                                                onClick={() => setConfirmDeleteId(child.id)}
                                                                className="px-4 py-1 rounded-full text-sm font-medium bg-gradient-to-r from-red-500 to-red-400 text-white hover:shadow-lg transition"
                                                            >
                                                                Sil
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))}
                                    </React.Fragment>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>

            {/* Modal: Create / Update */}
            {showForm && (
                <div
                    className="fixed inset-0 flex items-center justify-center bg-black/50"
                    onClick={closeForm}
                >
                    <div
                        className="bg-white p-6 rounded-xl shadow-xl w-96"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <h3 className="text-xl font-semibold mb-4">
                            {isEdit ? "Departman Güncelle" : "Yeni Departman"}
                        </h3>
                        <form onSubmit={submitForm} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium mb-1">Ad</label>
                                <input
                                    type="text"
                                    value={form.name}
                                    onChange={(e) => setForm({ ...form, name: e.target.value })}
                                    required
                                    className="w-full border rounded px-3 py-2"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium mb-1">
                                    Üst Departman (boş bırakılırsa ana departman olur)
                                </label>
                                <select
                                    value={form.parentDepartmentId ?? ""}
                                    onChange={(e) =>
                                        setForm({
                                            ...form,
                                            parentDepartmentId:
                                                e.target.value === "" ? null : Number(e.target.value),
                                        })
                                    }
                                    className="w-full border rounded px-3 py-2"
                                >
                                    <option value="">(Ana Departman Olarak Ekle)</option>
                                    {mainDepartments.map((d) => (
                                        <option key={d.id} value={d.id}>
                                            {d.name}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="flex justify-end gap-3">
                                <button
                                    type="button"
                                    onClick={closeForm}
                                    className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                                >
                                    İptal
                                </button>
                                <button
                                    type="submit"
                                    className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
                                >
                                    {isEdit ? "Kaydet" : "Oluştur"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Modal: Delete Confirm */}
            {confirmDeleteId !== null && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/40">
                    <div className="bg-white p-6 rounded-xl shadow-lg w-80">
                        <p className="mb-4">Bu departmanı silmek istediğinize emin misiniz?</p>
                        <div className="flex justify-end space-x-4">
                            <button
                                onClick={() => setConfirmDeleteId(null)}
                                className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                            >
                                Hayır
                            </button>
                            <button
                                onClick={() => deleteDepartment(confirmDeleteId)}
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

export default DepartmentsPage;
