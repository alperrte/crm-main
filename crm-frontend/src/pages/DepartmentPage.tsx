// src/pages/DepartmentsPage.tsx
import React, { useEffect, useState } from "react";
import departmentApi from "../api/departmentApi";

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

    const remove = async (id: number) => {
        if (!window.confirm("Silmek istediğinize emin misiniz?")) return;
        await departmentApi.delete(`/api/admin/departments/${id}`);
        fetchAll();
    };

    return (
        <div style={{ padding: 24 }}>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16 }}>
                <h2>Departman Kontrolleri</h2>
                <button onClick={openCreate}>+ Oluştur</button>
            </div>

            {loading ? (
                <p>Yükleniyor…</p>
            ) : (
                <table width="100%" cellPadding={8} style={{ borderCollapse: "collapse" }}>
                    <thead>
                    <tr style={{ textAlign: "left", borderBottom: "1px solid #ddd" }}>
                        <th>ID</th>
                        <th>Ad</th>
                        <th>Üst Departman</th>
                        <th>İşlemler</th>
                    </tr>
                    </thead>
                    <tbody>
                    {items.map((d) => (
                        <tr key={d.id} style={{ borderBottom: "1px solid #f0f0f0" }}>
                            <td>{d.id}</td>
                            <td>{d.name}</td>
                            <td>{d.parentDepartmentId ?? "-"}</td>
                            <td>
                                <button onClick={() => openEdit(d)} style={{ marginRight: 8 }}>
                                    Güncelle
                                </button>
                                <button onClick={() => remove(d.id)}>Sil</button>
                            </td>
                        </tr>
                    ))}
                    {items.length === 0 && (
                        <tr>
                            <td colSpan={4} style={{ padding: 16, textAlign: "center", color: "#777" }}>
                                Kayıt yok
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            )}

            {showForm && (
                <div
                    style={{
                        position: "fixed",
                        inset: 0,
                        background: "rgba(0,0,0,0.35)",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        padding: 16,
                    }}
                    onClick={closeForm}
                >
                    <div
                        style={{ background: "#fff", padding: 20, borderRadius: 8, minWidth: 360 }}
                        onClick={(e) => e.stopPropagation()}
                    >
                        <h3 style={{ marginTop: 0 }}>{isEdit ? "Departman Güncelle" : "Yeni Departman"}</h3>
                        <form onSubmit={submitForm}>
                            <div style={{ marginBottom: 12 }}>
                                <label>
                                    Ad<br />
                                    <input
                                        type="text"
                                        value={form.name}
                                        onChange={(e) => setForm({ ...form, name: e.target.value })}
                                        required
                                    />
                                </label>
                            </div>
                            <div style={{ marginBottom: 12 }}>
                                <label>
                                    Üst Departman ID (opsiyonel)<br />
                                    <input
                                        type="number"
                                        value={form.parentDepartmentId ?? ""}
                                        onChange={(e) =>
                                            setForm({
                                                ...form,
                                                parentDepartmentId: e.target.value === "" ? null : Number(e.target.value),
                                            })
                                        }
                                    />
                                </label>
                            </div>

                            <div style={{ display: "flex", gap: 8, justifyContent: "flex-end" }}>
                                <button type="button" onClick={closeForm}>
                                    İptal
                                </button>
                                <button type="submit">{isEdit ? "Kaydet" : "Oluştur"}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DepartmentsPage;
