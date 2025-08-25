import React, { useEffect, useMemo, useState } from "react";
import {
    Person,
    getAdminPersons,
    getUnassignedPersons,
    getPersonsByDepartment,
    assignDepartment,
} from "../api/personApi";
import departmentApi from "../api/departmentApi";
import { Link } from "react-router-dom";

type Department = {
    id: number;
    name: string;
    parentDepartmentId?: number | null;
};

type FilterMode = "ALL" | "UNASSIGNED" | "BY_DEPARTMENT";

const PersonPage: React.FC = () => {
    const [persons, setPersons] = useState<Person[]>([]);
    const [departments, setDepartments] = useState<Department[]>([]);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");

    const [menuOpen, setMenuOpen] = useState(false);

    const [filterMode, setFilterMode] = useState<FilterMode>("ALL");
    const [selectedDepartmentId, setSelectedDepartmentId] = useState<number | "">(
        ""
    );
    const [search, setSearch] = useState("");

    const [assigning, setAssigning] = useState<number | null>(null);
    const [assignToDept, setAssignToDept] = useState<number | "">("");

    // ✅ Ortak yenileme (aktif filtreye göre)
    const handleRefresh = async () => {
        setLoading(true);
        setErr("");
        try {
            if (filterMode === "ALL") setPersons(await getAdminPersons());
            else if (filterMode === "UNASSIGNED")
                setPersons(await getUnassignedPersons());
            else if (selectedDepartmentId !== "" && selectedDepartmentId != null)
                setPersons(await getPersonsByDepartment(Number(selectedDepartmentId)));
            else setPersons([]);
        } catch (e: any) {
            setErr(e?.message ?? String(e));
        } finally {
            setLoading(false);
        }
    };

    // İlk yükleme
    useEffect(() => {
        (async () => {
            setLoading(true);
            setErr("");
            try {
                // ✅ Departmanları al ve normalize et
                const deptRes = await departmentApi.get<any[]>("/api/admin/departments");
                const normalized: Department[] = (Array.isArray(deptRes.data)
                        ? deptRes.data
                        : []
                ).map((d: any) => ({
                    id: d.id ?? d.departmentId ?? d.department_id,
                    name: d.name ?? d.displayName ?? d.departmentName,
                    parentDepartmentId:
                        d.parentDepartmentId ??
                        d.parent_department_id ??
                        d.parentId ??
                        null,
                }));
                setDepartments(normalized);

                // ✅ Person listesi (tümü)
                setPersons(await getAdminPersons());
            } catch (e: any) {
                setErr(e?.message ?? String(e));
            } finally {
                setLoading(false);
            }
        })();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // Filtre değişince liste çek
    useEffect(() => {
        handleRefresh();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [filterMode, selectedDepartmentId]);

    const filtered = useMemo(() => {
        if (!search.trim()) return persons;
        const q = search.toLowerCase();
        return persons.filter(
            (p) =>
                p.name?.toLowerCase().includes(q) ||
                p.surname?.toLowerCase().includes(q) ||
                p.email?.toLowerCase().includes(q) ||
                (p.phone || "").toLowerCase().includes(q)
        );
    }, [persons, search]);

    const deptName = (id: number | null) => {
        if (id == null) return "-";
        const d = departments.find((x) => x.id === id);
        return d ? d.name : `#${id}`;
    };

    // ✅ Kişiye tıklayınca mevcut departmanını ön-seç
    const handleAssignClick = (p: Person) => {
        setErr("");
        setAssigning(p.id);
        setAssignToDept(p.departmentId ?? "");
    };

    const handleAssignConfirm = async () => {
        if (assigning == null || assignToDept === "") return;
        try {
            setLoading(true);
            await assignDepartment(assigning, Number(assignToDept));
            await handleRefresh(); // ✅ aktif filtre ile yenile
            setAssigning(null);
            setAssignToDept("");
        } catch (e: any) {
            setErr(e?.message ?? String(e));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen p-6 bg-gradient-to-br from-[#0f0f23] via-[#1a1a2e] to-[#16213e] text-white">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="flex justify-between items-center mb-8 border-b border-white/10 pb-4">
                    <div>
                        <h1 className="text-4xl font-extrabold bg-gradient-to-r from-indigo-400 to-purple-500 bg-clip-text text-transparent">
                            Person Yönetimi
                        </h1>
                        <h2 className="text-lg text-gray-400">
                            Rolü PERSON olan üyeleri yönet
                        </h2>
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
                                    to="/admin"
                                    className="block px-5 py-3 hover:bg-indigo-500/20"
                                    onClick={() => setMenuOpen(false)}
                                >
                                    Admin Panel
                                </Link>
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

                {/* Filtreler */}
                <section className="mb-4 grid gap-3 md:flex md:items-end">
                    <div className="flex items-center gap-2">
                        <label className="text-sm text-gray-300">Filtre:</label>
                        <select
                            className="px-3 py-2 rounded-xl bg-[#141427] border border-white/10"
                            value={filterMode}
                            onChange={(e) => setFilterMode(e.target.value as FilterMode)}
                            disabled={loading}
                        >
                            <option value="ALL">Tümü (aktif)</option>
                            <option value="UNASSIGNED">Departmansız</option>
                            <option value="BY_DEPARTMENT">Departmana göre</option>
                        </select>
                    </div>

                    {filterMode === "BY_DEPARTMENT" && (
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-300">Departman:</label>
                            <select
                                className="px-3 py-2 rounded-xl bg-[#141427] border border-white/10"
                                value={selectedDepartmentId}
                                onChange={(e) =>
                                    setSelectedDepartmentId(
                                        e.target.value ? Number(e.target.value) : ""
                                    )
                                }
                                disabled={loading}
                            >
                                <option value="">Seçiniz…</option>
                                {departments.map((d) => (
                                    <option key={d.id} value={d.id}>
                                        {d.name}
                                    </option>
                                ))}
                            </select>
                        </div>
                    )}

                    <div className="ml-auto flex items-center gap-2">
                        <input
                            placeholder="Ara: ad, soyad, email, telefon…"
                            className="px-3 py-2 rounded-xl bg-[#141427] border border-white/10"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            disabled={loading}
                        />
                        <button
                            onClick={handleRefresh}
                            className="px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-100"
                            disabled={loading}
                        >
                            Yenile
                        </button>
                    </div>
                </section>

                {err && (
                    <div className="mb-4 rounded-xl border border-red-500 bg-red-950/40 px-4 py-3 text-sm">
                        Hata: {err}
                    </div>
                )}

                {/* Tablo */}
                <div className="bg-[#1a1a2e]/80 backdrop-blur p-6 rounded-2xl shadow-2xl border border-white/10 overflow-x-auto">
                    <table className="w-full border-collapse">
                        <thead>
                        <tr className="bg-gradient-to-r from-indigo-500 to-purple-600 text-left">
                            <th className="px-4 py-3">ID</th>
                            <th className="px-4 py-3">Ad</th>
                            <th className="px-4 py-3">Soyad</th>
                            <th className="px-4 py-3">E-posta</th>
                            <th className="px-4 py-3">Telefon</th>
                            <th className="px-4 py-3">Departman</th>
                            <th className="px-4 py-3 text-right">İşlem</th>
                        </tr>
                        </thead>
                        <tbody>
                        {loading && (
                            <tr>
                                <td colSpan={7} className="px-4 py-6 text-center text-gray-400">
                                    Yükleniyor…
                                </td>
                            </tr>
                        )}

                        {!loading && filtered.length === 0 && (
                            <tr>
                                <td colSpan={7} className="px-4 py-6 text-center text-gray-400">
                                    Kayıt yok.
                                </td>
                            </tr>
                        )}

                        {!loading &&
                            filtered.map((p, i) => (
                                <tr
                                    key={p.id}
                                    className={`transition hover:bg-indigo-500/10 ${
                                        i % 2 === 0 ? "bg-transparent" : "bg-white/5"
                                    }`}
                                >
                                    <td className="px-4 py-3">{p.id}</td>
                                    <td className="px-4 py-3">{p.name}</td>
                                    <td className="px-4 py-3">{p.surname}</td>
                                    <td className="px-4 py-3">{p.email}</td>
                                    <td className="px-4 py-3">{p.phone ?? "-"}</td>
                                    <td className="px-4 py-3">
                                        {deptName(p.departmentId ?? null)}
                                    </td>
                                    <td className="px-4 py-3">
                                        {assigning === p.id ? (
                                            <div className="flex items-center gap-2 justify-end">
                                                <select
                                                    className="px-3 py-2 rounded-xl bg-[#141427] border border-white/10"
                                                    value={assignToDept}
                                                    onChange={(e) =>
                                                        setAssignToDept(
                                                            e.target.value ? Number(e.target.value) : ""
                                                        )
                                                    }
                                                    disabled={loading}
                                                >
                                                    <option value="">Seçiniz…</option>
                                                    {departments.map((d) => (
                                                        <option key={d.id} value={d.id}>
                                                            {d.name}
                                                        </option>
                                                    ))}
                                                </select>
                                                <button
                                                    onClick={handleAssignConfirm}
                                                    className="px-3 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-sm"
                                                    disabled={loading || !assignToDept}
                                                >
                                                    Kaydet
                                                </button>
                                                <button
                                                    onClick={() => {
                                                        setAssigning(null);
                                                        setAssignToDept("");
                                                    }}
                                                    className="px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm"
                                                    disabled={loading}
                                                >
                                                    İptal
                                                </button>
                                            </div>
                                        ) : (
                                            <div className="flex justify-end">
                                                <button
                                                    onClick={() => handleAssignClick(p)} // ✅ kişi objesi ile çağır
                                                    className="px-3 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-sm"
                                                    disabled={loading}
                                                >
                                                    Departman Ata
                                                </button>
                                            </div>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default PersonPage;
