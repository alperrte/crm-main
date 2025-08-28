// src/pages/DepartmentsPage.tsx
import React, { useEffect, useState } from "react";
import departmentApi from "../api/departmentApi";
import { Link } from "react-router-dom";

type Department = {
    id: number;
    name: string;
    parentDepartmentId?: number | null;
    children?: Department[];
};

const emptyForm: Omit<Department, "id" | "children"> = {
    name: "",
    parentDepartmentId: null,
};

const DepartmentsPage: React.FC = () => {
    const [items, setItems] = useState<Department[]>([]);
    const [loading, setLoading] = useState(false);

    const [showForm, setShowForm] = useState(false);
    const [isEdit, setIsEdit] = useState(false);
    const [editId, setEditId] = useState<number | null>(null);
    const [form, setForm] = useState<Omit<Department, "id" | "children">>(emptyForm);

    const [menuOpen, setMenuOpen] = useState(false);
    const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);
    const [expanded, setExpanded] = useState<Record<number, boolean>>({});
    const [openDropdown, setOpenDropdown] = useState<number | null>(null);
    const [dropdownPos, setDropdownPos] = useState<{ top: number; left: number } | null>(null);

    const fetchAll = async () => {
        setLoading(true);
        try {
            const res = await departmentApi.get<Department[]>("/api/admin/departments");
            const flat = res.data;

            const map: Record<number, Department> = {};
            flat.forEach((d) => (map[d.id] = { ...d, children: [] }));

            const roots: Department[] = [];
            flat.forEach((d) => {
                if (d.parentDepartmentId && map[d.parentDepartmentId]) {
                    map[d.parentDepartmentId].children!.push(map[d.id]);
                } else {
                    roots.push(map[d.id]);
                }
            });

            setItems(roots);
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
        setForm({
            name: row.name,
            parentDepartmentId: row.parentDepartmentId ?? null,
        });
        setShowForm(true);
    };

    const openSubCreate = (parentId: number) => {
        setIsEdit(false);
        setEditId(null);
        setForm({
            name: "",
            parentDepartmentId: parentId,
        });
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

    const deleteDepartment = async (id: number) => {
        try {
            await departmentApi.delete(`/api/admin/departments/${id}`);
            fetchAll();
        } catch (err) {
            console.error(err);
        } finally {
            setConfirmDeleteId(null);
        }
    };

    const toggleExpand = (id: number) => {
        setExpanded((prev) => ({ ...prev, [id]: !prev[id] }));
    };

    // ✅ Dropdown artık fixed pozisyonla ekrana çiziliyor
    const toggleDropdown = (id: number, e: React.MouseEvent) => {
        e.stopPropagation();
        if (openDropdown === id) {
            setOpenDropdown(null);
            setDropdownPos(null);
            return;
        }

        const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
        setDropdownPos({
            top: rect.bottom + window.scrollY,
            left: rect.left + window.scrollX,
        });

        setOpenDropdown(id);
    };

    // ✅ Recursive render
    const renderDepartment = (dep: Department, parentName: string | null = null, level = 0) => (
        <React.Fragment key={dep.id}>
            <tr className={level > 0 ? "bg-gray-50" : ""}>
                <td className="px-6 py-4 relative">
                    <button
                        onClick={(e) => toggleDropdown(dep.id, e)}
                        className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white px-4 py-1 rounded-full font-semibold text-sm"
                    >
                        ⚙️ İşlemler ▼
                    </button>

                    {openDropdown === dep.id && dropdownPos && (
                        <div
                            className="fixed min-w-[14rem] bg-white shadow-lg rounded-lg z-50 max-h-96 overflow-y-auto"
                            style={{ top: dropdownPos.top, left: dropdownPos.left }}
                        >
                            <button
                                onClick={() => openEdit(dep)}
                                className="block px-4 py-2 hover:bg-gray-100 w-full text-left"
                            >
                                🔄 Güncelle
                            </button>
                            <button
                                onClick={() => openSubCreate(dep.id)}
                                className="block px-4 py-2 hover:bg-gray-100 w-full text-left"
                            >
                                ➕ Alt Departman Ekle
                            </button>
                            <button
                                onClick={() => setConfirmDeleteId(dep.id)}
                                className="block px-4 py-2 text-red-600 hover:bg-red-50 w-full text-left"
                            >
                                ❌ Sil
                            </button>
                        </div>
                    )}
                </td>

                <td
                    className="px-6 py-4 font-bold text-gray-900 cursor-pointer"
                    onClick={() => toggleExpand(dep.id)}
                >
                    {dep.children && dep.children.length > 0
                        ? expanded[dep.id]
                            ? "▼ "
                            : "▶ "
                        : ""}
                    {level > 0 && "↳ "} {dep.name}
                </td>
                <td className="px-6 py-4 italic text-gray-500">
                    {parentName ?? "-"}
                </td>
            </tr>

            {expanded[dep.id] &&
                dep.children?.map((child) =>
                    renderDepartment(child, dep.name, level + 1)
                )}
        </React.Fragment>
    );

    return (
        <div className="min-h-screen p-6 bg-gradient-to-br from-indigo-400 to-purple-600 flex justify-center items-start">
            <div className="w-full max-w-5xl bg-white rounded-2xl shadow-2xl overflow-hidden">
                {/* Header */}
                <div className="bg-gradient-to-r from-indigo-500 to-purple-600 text-white p-6 flex justify-between items-center relative">
                    <h1 className="text-3xl font-light tracking-wide">Departman Kontrolleri</h1>

                    <div className="flex items-center gap-4">
                        <button
                            onClick={openCreate}
                            className="flex items-center gap-2 bg-white/20 border-2 border-white/30 px-5 py-2 rounded-full hover:bg-white/30 transition shadow"
                        >
                            + Oluştur
                        </button>

                        <div className="relative">
                            <button
                                onClick={() => setMenuOpen(!menuOpen)}
                                className="px-5 py-2 rounded-full bg-white/20 border border-white/30 hover:bg-white/30 transition shadow"
                            >
                                Paneller
                            </button>
                            {menuOpen && (
                                <div className="absolute right-0 mt-2 w-48 bg-white text-gray-800 rounded-lg shadow-lg overflow-hidden z-20">
                                    <Link to="/admin" className="block px-4 py-2 hover:bg-gray-100">
                                        Admin Paneli
                                    </Link>
                                    <Link to="/admin/roles" className="block px-4 py-2 hover:bg-gray-100">
                                        Rol Kontrol Paneli
                                    </Link>
                                    <Link to="/admin/create-user" className="block px-4 py-2 hover:bg-gray-100">
                                        Kişi Oluşturma Paneli
                                    </Link>
                                </div>
                            )}
                        </div>
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
                                    <th className="px-6 py-3 text-left">İşlemler</th>
                                    <th className="px-6 py-3 text-left">Ad</th>
                                    <th className="px-6 py-3 text-left">Üst Departman</th>
                                </tr>
                                </thead>
                                <tbody>
                                {items.length === 0 && (
                                    <tr>
                                        <td colSpan={4} className="text-center text-gray-500 py-10">
                                            <h3 className="text-lg font-medium">Kayıt yok</h3>
                                        </td>
                                    </tr>
                                )}
                                {items.map((dep) => renderDepartment(dep))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>

            {/* Modal: Create / Update */}
            {showForm && (
                <div className="fixed inset-0 flex items-center justify-center bg-black/50" onClick={closeForm}>
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
                                <label className="block text-sm font-medium mb-1">Üst Departman</label>
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
                                    {items.map((d) => (
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
