import React, { useEffect, useState } from "react";
import api from "../api/userApi";                 // user-service (kullanıcı yönetimi)
import { getAdminTickets, AdminTicket } from "../api/ticketApi"; // ticket-service (8084)
import { Link } from "react-router-dom";

interface User {
    id: number;
    username: string;
    email: string;
    role: string;
}

const AdminPanel: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [tickets, setTickets] = useState<AdminTicket[]>([]);
    const [tErr, setTErr] = useState<string>("");

    // Userları çek (user-service)
    useEffect(() => {
        api
            .get("/admin/users", {
                headers: { Authorization: "Bearer " + localStorage.getItem("token") },
            })
            .then((res) => setUsers(res.data))
            .catch(console.error);
    }, []);

    // Ticketları çek (ticket-service)
    useEffect(() => {
        getAdminTickets()
            .then(setTickets)
            .catch((e) => setTErr(String(e?.message ?? e)));
    }, []);

    // User işlemleri (user-service)
    const deleteUser = (id: number) => {
        api
            .delete(`/admin/users/${id}`, {
                headers: { Authorization: "Bearer " + localStorage.getItem("token") },
            })
            .then(() => {
                setUsers((prev) => prev.filter((u) => u.id !== id));
            })
            .catch(console.error);
    };

    const updateRole = (id: number, role: string) => {
        api
            .put(
                `/admin/users/${id}/role?role=${role}`,
                {},
                { headers: { Authorization: "Bearer " + localStorage.getItem("token") } }
            )
            .then(() => {
                setUsers((prev) => prev.map((u) => (u.id === id ? { ...u, role } : u)));
            })
            .catch(console.error);
    };

    return (
        <div className="p-6 space-y-10">
            {/* Kullanıcı Yönetimi */}
            <div>
                <h2 className="text-xl font-bold mb-4">Admin Panel - Kullanıcı Yönetimi</h2>
                <table className="table-auto w-full border">
                    <thead>
                    <tr>
                        <th className="border px-2 py-1">ID</th>
                        <th className="border px-2 py-1">Kullanıcı Adı</th>
                        <th className="border px-2 py-1">Email</th>
                        <th className="border px-2 py-1">Rol</th>
                        <th className="border px-2 py-1">İşlemler</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.map((u) => (
                        <tr key={u.id}>
                            <td className="border px-2 py-1">{u.id}</td>
                            <td className="border px-2 py-1">{u.username}</td>
                            <td className="border px-2 py-1">{u.email}</td>
                            <td className="border px-2 py-1">{u.role}</td>
                            <td className="border px-2 py-1">
                                <button
                                    onClick={() => deleteUser(u.id)}
                                    className="bg-red-500 text-white px-2 py-1 mr-2 rounded"
                                >
                                    Sil
                                </button>
                                <button
                                    onClick={() => updateRole(u.id, "ADMIN")}
                                    className="bg-blue-500 text-white px-2 py-1 mr-2 rounded"
                                >
                                    Admin Yap
                                </button>
                                <button
                                    onClick={() => updateRole(u.id, "PERSON")}
                                    className="bg-green-500 text-white px-2 py-1 rounded"
                                >
                                    Person Yap
                                </button>
                                <button
                                    onClick={() => updateRole(u.id, "USER")}
                                    className="bg-yellow-600 text-white px-2 py-1 ml-2 rounded"
                                >
                                    User Yap
                                </button>
                                <button style={{ marginLeft: 8 }}>
                                    <Link to="/admin/departments">Departman Kontrolleri</Link>
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {/* Ticket Yönetimi */}
            <div>
                <h2 className="text-xl font-bold mb-4">Admin Panel - Ticket Yönetimi</h2>
                {tErr && <div className="mb-3 text-red-600">Ticketlar alınamadı: {tErr}</div>}
                <table className="table-auto w-full border">
                    <thead>
                    <tr>
                        <th className="border px-2 py-1">ID</th>
                        <th className="border px-2 py-1">Tarih</th>
                        <th className="border px-2 py-1">Müşteri</th>
                        <th className="border px-2 py-1">Email</th>
                        <th className="border px-2 py-1">Telefon</th>
                        <th className="border px-2 py-1">Açıklama</th>
                        <th className="border px-2 py-1">Öncelik</th>
                        <th className="border px-2 py-1">Aktif</th>
                    </tr>
                    </thead>
                    <tbody>
                    {tickets.map((t) => (
                        <tr key={t.id}>
                            <td className="border px-2 py-1">{t.id}</td>
                            <td className="border px-2 py-1">
                                {t.createdDate ? new Date(t.createdDate).toLocaleString() : "-"}
                            </td>
                            <td className="border px-2 py-1">
                                {t.name} {t.surname}
                            </td>
                            <td className="border px-2 py-1">{t.email}</td>
                            <td className="border px-2 py-1">{t.phone ?? "-"}</td>
                            <td className="border px-2 py-1">{t.description}</td>
                            <td className="border px-2 py-1">{t.priority}</td>
                            <td className="border px-2 py-1">{t.active ? "Evet" : "Hayır"}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default AdminPanel;
