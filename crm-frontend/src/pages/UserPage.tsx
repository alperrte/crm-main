// src/pages/UserPage.tsx
import React, { useEffect, useState } from "react";
import {
    getDeptTickets,
    takeTicket,
    reassignTicket,
    closeTicket,
    DeptTicket,
} from "../api/ticketApi";
import { getMyProfile, MyProfile } from "../api/personApi";
import { Link } from "react-router-dom";
import { getAllDepartments, Department } from "../api/departmentApi";

type FilterType = "ALL" | "MY_ASSIGNED" | "MY_CLOSED";

const UserPage: React.FC = () => {
    const [tickets, setTickets] = useState<DeptTicket[]>([]);
    const [loading, setLoading] = useState(false);
    const [filter, setFilter] = useState<FilterType>("ALL");

    // Kullanıcı bilgileri
    const [userInfo, setUserInfo] = useState<{
        personId?: number;
        name?: string;
        surname?: string;
        email?: string;
        role?: string;
    }>({});

    const [deptId, setDeptId] = useState<number | null>(null);
    const [departmentName, setDepartmentName] = useState<string | null>(null);

    // === Transfer Modal state ===
    const [transferOpen, setTransferOpen] = useState(false);
    const [transferTicketId, setTransferTicketId] = useState<number | null>(null);
    const [departments, setDepartments] = useState<Department[]>([]);
    const [selectedDeptId, setSelectedDeptId] = useState<number | "">("");

    // Departman listesini yükle
    const loadDepartments = async () => {
        try {
            const list = await getAllDepartments();
            setDepartments(list);
        } catch (e) {
            console.warn("Departmanlar alınamadı:", e);
            setDepartments([]);
        }
    };

    // Transfer modal aç
    const openTransfer = async (ticketId: number) => {
        setTransferTicketId(ticketId);
        setSelectedDeptId("");
        await loadDepartments();
        setTransferOpen(true);
    };
    const closeTransfer = () => {
        setTransferOpen(false);
        setTransferTicketId(null);
    };
    const confirmTransfer = async () => {
        if (!transferTicketId || !deptId || !selectedDeptId) {
            console.warn("⚠️ Devretmek için gerekli bilgiler eksik:", {
                transferTicketId, deptId, selectedDeptId,
            });
            return;
        }

        console.log("🔄 Devretme isteği gönderiliyor:", {
            ticketId: transferTicketId,
            fromDeptId: deptId,
            newDeptId: selectedDeptId,
        });

        try {
            await reassignTicket(transferTicketId, deptId, Number(selectedDeptId));
            console.log("✅ Devretme başarılı!");
            closeTransfer();
            fetchTickets();
        } catch (err) {
            console.error("❌ Devretme hatası:", err);
        }
    };

    // Ticketları getir
    const fetchTickets = async () => {
        if (!deptId) return;
        setLoading(true);
        try {
            if (filter === "ALL") {
                const data = await getDeptTickets(deptId);
                setTickets(data.filter((t) => t.status === "OPEN"));
            } else if (filter === "MY_ASSIGNED" && userInfo.personId) {
                const res = await fetch(
                    `http://localhost:8084/api/departments/me/assigned?personId=${userInfo.personId}`,
                    {
                        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                    }
                );
                const list = await res.json();
                setTickets(list.filter((t: DeptTicket) => t.status === "IN_PROGRESS"));
            } else if (filter === "MY_CLOSED" && userInfo.personId) {
                const res = await fetch(
                    `http://localhost:8084/api/departments/me/closed?personId=${userInfo.personId}`,
                    {
                        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                    }
                );
                const list = await res.json();
                setTickets(list.filter((t: DeptTicket) => t.status === "DONE"));
            }
        } catch (e) {
            console.error("Ticket yüklenemedi:", e);
        } finally {
            setLoading(false);
        }
    };

    // İlk açılış → deptId alma
    useEffect(() => {
        const raw = localStorage.getItem("token");
        let deptFromToken: number | null = null;

        if (raw) {
            try {
                const token = raw.startsWith('"') ? JSON.parse(raw) : raw;
                const payload = JSON.parse(atob(token.split(".")[1]));

                setUserInfo({
                    personId: payload.personId,
                    name: payload.name,
                    surname: payload.surname,
                    email: payload.sub || payload.email,
                    role: payload.role || payload.roles?.[0],
                });

                const claim = payload.deptId || payload.departmentId || payload.department_id;
                if (claim) {
                    deptFromToken = Number(claim);
                    setDeptId(deptFromToken);
                    localStorage.setItem("deptId", String(deptFromToken));
                }
            } catch (err) {
                console.error("Token decode hatası:", err);
            }
        }

        if (!deptFromToken) {
            const ls = localStorage.getItem("deptId");
            if (ls) {
                setDeptId(parseInt(ls, 10));
            } else {
                getMyProfile()
                    .then((me: MyProfile) => {
                        const d = me.department?.id ?? me.departmentId;
                        if (d) {
                            setDeptId(d);
                            localStorage.setItem("deptId", String(d));
                        }
                        if (me.department?.name) setDepartmentName(me.department.name);
                        setUserInfo((prev) => ({ ...prev, personId: me.id }));
                    })
                    .catch((e) => console.warn("Profil alınamadı:", e));
            }
        }
    }, []);

    // Departman veya filter değiştiğinde ticketları getir
    useEffect(() => {
        fetchTickets();
    }, [deptId, filter, userInfo.personId]);

    // İşlemler
    const handleTake = async (ticketId: number) => {
        if (!deptId) return;
        await takeTicket(ticketId, deptId);
        fetchTickets();
    };
    const handleClose = async (ticketId: number) => {
        await closeTicket(ticketId);
        fetchTickets();
    };

    // İstatistikler
    const stats = {
        total: tickets.length,
        high: tickets.filter((t) => t.priority === "HIGH").length,
        medium: tickets.filter((t) => t.priority === "MEDIUM").length,
        active: tickets.filter((t) => t.status === "OPEN").length,
    };

    // Ticketları müşteri / çalışan ayır
    const customerTickets = tickets.filter((t) => !t.employee);
    const employeeTickets = tickets.filter((t) => t.employee);

    // Tek tablo render
    const renderTable = (list: DeptTicket[], type: "CUSTOMER" | "EMPLOYEE") => (
        <div className="mb-8">
            <h2 className="text-lg font-semibold mb-2">
                {type === "CUSTOMER" ? "👤 Müşteri Ticketları" : "💼 Çalışan Ticketları"}
            </h2>
            <div className="bg-gray-800 rounded-lg shadow overflow-hidden">
                <table className="w-full text-left">
                    <thead className="bg-purple-700 text-white">
                    <tr>
                        <th className="p-3">İşlemler</th>
                        <th className="p-3">ID</th>
                        <th className="p-3">Konu</th>
                        <th className="p-3">Öncelik</th>
                        <th className="p-3">Durum</th>
                        <th className="p-3">Departman</th>
                        {type === "CUSTOMER" && (
                            <>
                                <th className="p-3">Müşteri Email</th>
                                <th className="p-3">Müşteri Adı</th>
                                <th className="p-3">Müşteri Soyadı</th>
                                <th className="p-3">Müşteri Telefon</th>
                            </>
                        )}
                    </tr>
                    </thead>
                    <tbody>
                    {list.length === 0 ? (
                        <tr>
                            <td colSpan={type === "CUSTOMER" ? 10 : 6} className="p-3 text-center">
                                Henüz ticket bulunmuyor.
                            </td>
                        </tr>
                    ) : (
                        list.map((t) => (
                            <tr key={t.id} className="border-b border-gray-700">
                                <td className="p-3 space-x-2">
                                    {filter === "ALL" && t.status === "OPEN" && (
                                        <button
                                            className="bg-green-500 text-white px-2 py-1 rounded"
                                            onClick={() => handleTake(t.id)}
                                        >
                                            Üstlen
                                        </button>
                                    )}
                                    {filter === "MY_ASSIGNED" && t.status === "IN_PROGRESS" && (
                                        <>
                                            <button
                                                className="bg-yellow-500 text-white px-2 py-1 rounded"
                                                onClick={() => openTransfer(t.id)}
                                            >
                                                Devret
                                            </button>
                                            <button
                                                className="bg-red-500 text-white px-2 py-1 rounded"
                                                onClick={() => handleClose(t.id)}
                                            >
                                                Kapat
                                            </button>
                                        </>
                                    )}
                                </td>
                                <td className="p-3">{t.id}</td>
                                <td className="p-3">{t.issue}</td>
                                <td className="p-3">{t.priority}</td>
                                <td className="p-3">
                                    {t.status === "OPEN" ? (
                                        <span className="bg-green-500 text-white px-2 py-1 rounded-full text-xs">Açık</span>
                                    ) : t.status === "IN_PROGRESS" ? (
                                        <span className="bg-blue-500 text-white px-2 py-1 rounded-full text-xs">Üstlenildi</span>
                                    ) : t.status === "TRANSFERRED" ? (
                                        <span className="bg-amber-500 text-white px-2 py-1 rounded-full text-xs">Devredildi</span>
                                    ) : (
                                        <span className="bg-gray-500 text-white px-2 py-1 rounded-full text-xs">Kapalı</span>
                                    )}
                                </td>
                                <td className="p-3">{t.departmentId ?? "—"}</td>
                                {type === "CUSTOMER" && (
                                    <>
                                        <td className="p-3">{t.customerEmail ?? "—"}</td>
                                        <td className="p-3">{t.customerName ?? "—"}</td>
                                        <td className="p-3">{t.customerSurname ?? "—"}</td>
                                        <td className="p-3">{t.customerPhone ?? "—"}</td>
                                    </>
                                )}
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );

    return (
        <div className="p-6 bg-gray-900 min-h-screen text-white">
            {/* Sağ üst kullanıcı bilgisi */}
            <div className="absolute top-4 right-6 text-sm text-gray-300">
                {userInfo.name} {userInfo.surname} ({userInfo.email}) - {userInfo.role}
            </div>

            <h1 className="text-2xl font-bold mb-6">
                📌 {departmentName ? `${departmentName} Departmanı Paneli` : "Departman Ticket Paneli"}
            </h1>

            {/* İstatistik kartları */}
            <div className="grid grid-cols-4 gap-4 mb-6">
                <div className="bg-gray-800 p-4 rounded-xl text-center shadow">
                    <h2 className="text-2xl font-bold">{stats.total}</h2>
                    <p className="text-gray-400 text-sm">Toplam Ticket</p>
                </div>
                <div className="bg-gray-800 p-4 rounded-xl text-center shadow">
                    <h2 className="text-2xl font-bold text-red-400">{stats.high}</h2>
                    <p className="text-gray-400 text-sm">Yüksek Öncelik</p>
                </div>
                <div className="bg-gray-800 p-4 rounded-xl text-center shadow">
                    <h2 className="text-2xl font-bold text-yellow-300">{stats.medium}</h2>
                    <p className="text-gray-400 text-sm">Orta Öncelik</p>
                </div>
                <div className="bg-gray-800 p-4 rounded-xl text-center shadow">
                    <h2 className="text-2xl font-bold text-green-400">{stats.active}</h2>
                    <p className="text-gray-400 text-sm">Aktif</p>
                </div>
            </div>

            {/* Butonlar */}
            <div className="mb-6 flex gap-2">
                <Link
                    to="/create-ticket"
                    className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600"
                >
                    ➕ Yeni Ticket Oluştur
                </Link>
                <button
                    onClick={() => setFilter("MY_ASSIGNED")}
                    className={`px-4 py-2 rounded-lg ${filter === "MY_ASSIGNED" ? "bg-green-500" : "bg-gray-700"} text-white`}
                >
                    Üstlendiklerim
                </button>
                <button
                    onClick={() => setFilter("MY_CLOSED")}
                    className={`px-4 py-2 rounded-lg ${filter === "MY_CLOSED" ? "bg-red-500" : "bg-gray-700"} text-white`}
                >
                    Kapattıklarım
                </button>
                <button
                    onClick={() => setFilter("ALL")}
                    className={`px-4 py-2 rounded-lg ${filter === "ALL" ? "bg-purple-500" : "bg-gray-700"} text-white`}
                >
                    Tümü
                </button>
            </div>

            {/* Ayrı tablolar */}
            {renderTable(customerTickets, "CUSTOMER")}
            {renderTable(employeeTickets, "EMPLOYEE")}

            {/* === Inline Transfer Modal === */}
            {transferOpen && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-2xl p-4 w-full max-w-md text-gray-900">
                        <h3 className="text-lg font-semibold mb-3">
                            Ticket #{transferTicketId} → Devret
                        </h3>

                        <select
                            className="w-full border rounded p-2 mb-3"
                            value={selectedDeptId}
                            onChange={(e) => setSelectedDeptId(Number(e.target.value))}
                        >
                            <option value="" disabled>
                                Departman seç
                            </option>
                            {departments.map((d) => (
                                <option key={d.id} value={d.id}>
                                    {d.name}
                                </option>
                            ))}
                        </select>

                        <div className="flex gap-2 justify-end">
                            <button
                                onClick={closeTransfer}
                                className="px-3 py-2 rounded bg-gray-200"
                            >
                                Vazgeç
                            </button>
                            <button
                                disabled={!selectedDeptId}
                                onClick={confirmTransfer}
                                className="px-3 py-2 rounded bg-yellow-400"
                            >
                                Devret
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserPage;
