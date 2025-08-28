import React, { useEffect, useState } from "react";
import {
    getDeptTickets,
    takeTicket,
    reassignTicket,
    closeTicket,
    DeptTicket,
} from "../api/ticketApi";
import { getMyProfile, MyProfile } from "../api/personApi";
import { Link, useNavigate } from "react-router-dom";
import { getAllDepartments, Department } from "../api/departmentApi";

type FilterType = "ALL" | "MY_ASSIGNED" | "MY_CLOSED" | "MY_TRANSFERRED" | "INCOMING_TRANSFERRED";

const UserPage: React.FC = () => {
    const [tickets, setTickets] = useState<DeptTicket[]>([]);
    const [loading, setLoading] = useState(false);
    const [filter, setFilter] = useState<FilterType>("ALL");

    const [userInfo, setUserInfo] = useState<{
        personId?: number;
        name?: string;
        surname?: string;
        email?: string;
    }>({});

    const [deptId, setDeptId] = useState<number | null>(null);
    const [departmentName, setDepartmentName] = useState<string | null>(null);

    const [transferOpen, setTransferOpen] = useState(false);
    const [transferTicketId, setTransferTicketId] = useState<number | null>(null);
    const [departments, setDepartments] = useState<Department[]>([]);
    const [selectedDeptId, setSelectedDeptId] = useState<number | "">("");

    const navigate = useNavigate();

    // Çıkış işlemi
    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("deptId");
        navigate("/login");
    };

    // Departman listesini yükle
    const loadDepartments = async () => {
        try {
            const list = await getAllDepartments();
            setDepartments(list);
        } catch {
            setDepartments([]);
        }
    };

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

    // ✅ Ticket devretme
    const confirmTransfer = async () => {
        if (!transferTicketId || !deptId || !selectedDeptId) return;
        try {
            await reassignTicket(transferTicketId, deptId, Number(selectedDeptId));
            await fetchTickets();
            closeTransfer();
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
                setTickets(data);
            } else if (filter === "MY_ASSIGNED" && userInfo.personId) {
                const res = await fetch(
                    `http://localhost:8084/api/departments/me/assigned?personId=${userInfo.personId}`,
                    { headers: { Authorization: `Bearer ${localStorage.getItem("token")}` } }
                );
                setTickets(await res.json());
            } else if (filter === "MY_CLOSED" && userInfo.personId) {
                const res = await fetch(
                    `http://localhost:8084/api/departments/me/closed?personId=${userInfo.personId}`,
                    { headers: { Authorization: `Bearer ${localStorage.getItem("token")}` } }
                );
                setTickets(await res.json());
            } else if (filter === "MY_TRANSFERRED" && userInfo.personId) {
                const res = await fetch(
                    `http://localhost:8084/api/departments/me/transferred?personId=${userInfo.personId}`,
                    { headers: { Authorization: `Bearer ${localStorage.getItem("token")}` } }
                );
                setTickets(await res.json());
            } else if (filter === "INCOMING_TRANSFERRED") {
                const data = await getDeptTickets(deptId);
                setTickets(data.filter((t: DeptTicket) => t.status === "OPEN" && t.departmentId === deptId));
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
                        setUserInfo(prev => ({ ...prev, personId: me.id }));
                    })
                    .catch(() => {});
            }
        }
    }, []);

    useEffect(() => {
        fetchTickets();
    }, [deptId, filter, userInfo.personId]);

    const handleTake = async (ticketId: number) => {
        if (!deptId) return;
        await takeTicket(ticketId, deptId);
        fetchTickets();
    };

    const handleClose = async (ticketId: number) => {
        await closeTicket(ticketId);
        fetchTickets();
    };

    // Öncelik badge componenti
    const priorityBadge = (priority: string) => {
        switch (priority) {
            case "HIGH":
                return <span className="px-3 py-1 rounded-full text-xs font-semibold bg-red-500 text-white">YÜKSEK</span>;
            case "MEDIUM":
                return <span className="px-3 py-1 rounded-full text-xs font-semibold bg-gradient-to-r from-yellow-400 to-pink-500 text-black">ORTA</span>;
            case "LOW":
            default:
                return <span className="px-3 py-1 rounded-full text-xs font-semibold bg-green-500 text-white">DÜŞÜK</span>;
        }
    };

    // Aktiflik durum ikonu
    const statusDot = (active?: boolean) => (
        <span className={`inline-block w-3 h-3 rounded-full ${active ? "bg-green-500" : "bg-red-500"}`}></span>
    );

    // ✅ Müşteri ve çalışan ticket ayrımı
    const customerTickets = tickets.filter((t) => !t.employee);
    const employeeTickets = tickets.filter((t) => t.employee);

    const renderTable = (list: DeptTicket[], type: "CUSTOMER" | "EMPLOYEE") => (
        <div className="mb-8">
            <h2 className="text-lg font-semibold mb-2">
                {type === "CUSTOMER" ? "👤 Müşteri Ticketları" : "💼 Çalışan Ticketları"}
            </h2>
            <div className="bg-gray-800 rounded-lg shadow overflow-hidden">
                <table className="w-full text-left">
                    <thead className="bg-purple-700 text-white">
                    <tr>
                        {filter !== "MY_CLOSED" && filter !== "MY_TRANSFERRED" && <th className="p-3">İşlemler</th>}
                        <th className="p-3">ID</th>
                        <th className="p-3">Konu</th>
                        <th className="p-3">Öncelik</th>
                        <th className="p-3">Durum</th>
                        <th className="p-3">Aktiflik</th>
                        {type === "CUSTOMER" && (
                            <>
                                <th className="p-3">Email</th>
                                <th className="p-3">Ad</th>
                                <th className="p-3">Soyad</th>
                                <th className="p-3">Telefon</th>
                            </>
                        )}
                        {filter === "INCOMING_TRANSFERRED" && <th className="p-3">Geldiği Departman</th>}
                    </tr>
                    </thead>
                    <tbody>
                    {list.length === 0 ? (
                        <tr>
                            <td colSpan={12} className="p-3 text-center">Henüz ticket bulunmuyor.</td>
                        </tr>
                    ) : (
                        list.map(t => (
                            <tr key={t.id} className="border-b border-gray-700">
                                {filter !== "MY_CLOSED" && filter !== "MY_TRANSFERRED" && (
                                    <td className="p-3 space-x-2">
                                        {filter === "ALL" && t.status === "OPEN" && (
                                            <button className="bg-green-500 text-white px-2 py-1 rounded"
                                                    onClick={() => handleTake(t.id)}>Üstlen</button>
                                        )}
                                        {filter === "MY_ASSIGNED" && t.status === "IN_PROGRESS" && (
                                            <>
                                                <button className="bg-yellow-500 text-white px-2 py-1 rounded"
                                                        onClick={() => openTransfer(t.id)}>Devret</button>
                                                <button className="bg-red-500 text-white px-2 py-1 rounded"
                                                        onClick={() => handleClose(t.id)}>Kapat</button>
                                            </>
                                        )}
                                        {filter === "INCOMING_TRANSFERRED" && t.status === "OPEN" && (
                                            <button className="bg-green-500 text-white px-2 py-1 rounded"
                                                    onClick={() => handleTake(t.id)}>Üstlen</button>
                                        )}
                                    </td>
                                )}
                                <td className="p-3">{t.id}</td>
                                <td className="p-3">{t.issue}</td>
                                <td className="p-3">{priorityBadge(t.priority)}</td>
                                <td className="p-3">{t.status}</td>
                                <td className="p-3">{statusDot(t.active)}</td>
                                {type === "CUSTOMER" && (
                                    <>
                                        <td className="p-3">{t.customerEmail || "—"}</td>
                                        <td className="p-3">{t.customerName || "—"}</td>
                                        <td className="p-3">{t.customerSurname || "—"}</td>
                                        <td className="p-3">{t.customerPhone || "—"}</td>
                                    </>
                                )}
                                {filter === "INCOMING_TRANSFERRED" && <td className="p-3">{t.fromDepartmentId || "?"}</td>}
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
            <div className="absolute top-4 right-6 text-sm text-gray-300 flex items-center gap-2">
                {userInfo.name} {userInfo.surname} ({userInfo.email})
                {departmentName && ` - ${departmentName}`}
                <button
                    onClick={handleLogout}
                    className="ml-3 bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded"
                >
                    Çıkış Yap
                </button>
            </div>

            <h1 className="text-2xl font-bold mb-6">
                📌 {departmentName ? `${departmentName} Departmanı Paneli` : "Departman Ticket Paneli"}
            </h1>

            <div className="mb-6 flex gap-2">
                <Link to="/create-ticket" className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600">
                    ➕ Yeni Ticket Oluştur
                </Link>
                <button onClick={() => setFilter("MY_ASSIGNED")}
                        className={`px-4 py-2 rounded-lg ${filter === "MY_ASSIGNED" ? "bg-green-500" : "bg-gray-700"} text-white`}>
                    Üstlendiklerim
                </button>
                <button onClick={() => setFilter("MY_CLOSED")}
                        className={`px-4 py-2 rounded-lg ${filter === "MY_CLOSED" ? "bg-red-500" : "bg-gray-700"} text-white`}>
                    Kapattıklarım
                </button>
                <button onClick={() => setFilter("MY_TRANSFERRED")}
                        className={`px-4 py-2 rounded-lg ${filter === "MY_TRANSFERRED" ? "bg-amber-500" : "bg-gray-700"} text-white`}>
                    Devrettiklerim
                </button>
                <button onClick={() => setFilter("INCOMING_TRANSFERRED")}
                        className={`px-4 py-2 rounded-lg ${filter === "INCOMING_TRANSFERRED" ? "bg-blue-500" : "bg-gray-700"} text-white`}>
                    Devredilenler
                </button>
                <button onClick={() => setFilter("ALL")}
                        className={`px-4 py-2 rounded-lg ${filter === "ALL" ? "bg-purple-500" : "bg-gray-700"} text-white`}>
                    Tümü
                </button>
            </div>

            {renderTable(customerTickets, "CUSTOMER")}
            {renderTable(employeeTickets, "EMPLOYEE")}

            {transferOpen && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-2xl p-4 w-full max-w-md text-gray-900">
                        <h3 className="text-lg font-semibold mb-3">Ticket #{transferTicketId} → Devret</h3>
                        <select
                            className="w-full border rounded p-2 mb-3"
                            value={selectedDeptId}
                            onChange={(e) => setSelectedDeptId(Number(e.target.value))}
                        >
                            <option value="" disabled>Departman seç</option>
                            {departments.map(d => (
                                <option key={d.id} value={d.id}>{d.name}</option>
                            ))}
                        </select>
                        <div className="flex gap-2 justify-end">
                            <button onClick={closeTransfer} className="px-3 py-2 rounded bg-gray-200">Vazgeç</button>
                            <button disabled={!selectedDeptId} onClick={confirmTransfer}
                                    className="px-3 py-2 rounded bg-yellow-400">Devret</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserPage;
