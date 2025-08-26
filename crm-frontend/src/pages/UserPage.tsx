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

const UserPage: React.FC = () => {
    const [tickets, setTickets] = useState<DeptTicket[]>([]);
    const [loading, setLoading] = useState(false);

    // Kullanıcı bilgileri
    const [userInfo, setUserInfo] = useState<{
        name?: string;
        surname?: string;
        email?: string;
        role?: string;
    }>({});

    const [deptId, setDeptId] = useState<number | null>(null);
    const [departmentName, setDepartmentName] = useState<string | null>(null);

    const fetchTickets = async (d: number) => {
        setLoading(true);
        try {
            const data = await getDeptTickets(d);
            setTickets(data);
        } catch (e) {
            console.error("Ticket yüklenemedi:", e);
        } finally {
            setLoading(false);
        }
    };

    // İlk açılış
    useEffect(() => {
        const raw = localStorage.getItem("token");
        if (raw) {
            try {
                const token = raw.startsWith('"') ? JSON.parse(raw) : raw;
                const payload = JSON.parse(atob(token.split(".")[1]));
                setUserInfo({
                    name: payload.name,
                    surname: payload.surname,
                    email: payload.sub || payload.email,
                    role: payload.role || payload.roles?.[0],
                });
                const claim =
                    payload.deptId || payload.departmentId || payload.department_id;
                if (claim) setDeptId(Number(claim));
            } catch (err) {
                console.error("Token decode hatası:", err);
            }
        }

        const ls = localStorage.getItem("deptId");
        if (!ls) {
            getMyProfile()
                .then((me: MyProfile) => {
                    const d = me.department?.id ?? me.departmentId;
                    if (d) setDeptId(d);
                    if (me.department?.name) setDepartmentName(me.department.name);
                })
                .catch((e) => console.warn("Profil alınamadı:", e));
        } else {
            setDeptId(parseInt(ls, 10));
        }
    }, []);

    useEffect(() => {
        if (deptId) fetchTickets(deptId);
    }, [deptId]);

    // İşlem fonksiyonları
    const handleTake = async (ticketId: number) => {
        if (!deptId) return;
        await takeTicket(ticketId, deptId);
        fetchTickets(deptId);
    };
    const handleReassign = async (ticketId: number, newDeptId: number) => {
        if (!deptId) return;
        await reassignTicket(ticketId, deptId, newDeptId);
        fetchTickets(deptId);
    };
    const handleClose = async (ticketId: number) => {
        if (!deptId) return;
        await closeTicket(ticketId);
        fetchTickets(deptId);
    };

    // İstatistikler
    const stats = {
        total: tickets.length,
        high: tickets.filter((t) => t.priority === "HIGH").length,
        medium: tickets.filter((t) => t.priority === "MEDIUM").length,
        active: tickets.filter((t) => t.status === "OPEN").length,
    };

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

            {/* Yeni ticket yönlendirme */}
            <div className="mb-6">
                <Link
                    to="/create-ticket"
                    className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600"
                >
                    ➕ Yeni Ticket Oluştur
                </Link>
            </div>

            {/* Ticket Tablosu */}
            <div className="bg-gray-800 rounded-lg shadow overflow-hidden">
                <table className="w-full text-left">
                    <thead className="bg-purple-700 text-white">
                    <tr>
                        <th className="p-3">ID</th>
                        <th className="p-3">Konu</th>
                        <th className="p-3">Öncelik</th>
                        <th className="p-3">Durum</th>
                        <th className="p-3">Departman</th>
                        <th className="p-3">İşlemler</th>
                    </tr>
                    </thead>
                    <tbody>
                    {loading ? (
                        <tr>
                            <td colSpan={6} className="p-3 text-center">
                                Yükleniyor...
                            </td>
                        </tr>
                    ) : tickets.length === 0 ? (
                        <tr>
                            <td colSpan={6} className="p-3 text-center">
                                Henüz ticket bulunmuyor.
                            </td>
                        </tr>
                    ) : (
                        tickets.map((t) => (
                            <tr key={t.id} className="border-b border-gray-700">
                                <td className="p-3">{t.id}</td>
                                <td className="p-3">{t.issue}</td>
                                <td className="p-3">{t.priority}</td>
                                <td className="p-3">
                                    {t.status === "OPEN" ? (
                                        <span className="bg-green-500 text-white px-2 py-1 rounded-full text-xs">
                        Açık
                      </span>
                                    ) : (
                                        <span className="bg-gray-500 text-white px-2 py-1 rounded-full text-xs">
                        Kapalı
                      </span>
                                    )}
                                </td>
                                <td className="p-3">{t.departmentId ?? "—"}</td>
                                <td className="p-3 space-x-2">
                                    <button
                                        className="bg-green-500 text-white px-2 py-1 rounded"
                                        onClick={() => handleTake(t.id)}
                                    >
                                        Üstlen
                                    </button>
                                    <button
                                        className="bg-yellow-500 text-white px-2 py-1 rounded"
                                        onClick={() => handleReassign(t.id, 2)}
                                    >
                                        Devret
                                    </button>
                                    <button
                                        className="bg-red-500 text-white px-2 py-1 rounded"
                                        onClick={() => handleClose(t.id)}
                                    >
                                        Kapat
                                    </button>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default UserPage;
