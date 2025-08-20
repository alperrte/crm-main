import React, { useEffect, useState } from "react";
import api from "../api"; // axios yerine api.ts kullanıyoruz

interface User {
    id: number;
    username: string;
    email: string;
    role: string;
}

const AdminPanel: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]);

    useEffect(() => {
        api.get("/admin/users", {
            headers: { Authorization: "Bearer " + localStorage.getItem("token") }
        }).then(res => setUsers(res.data));
    }, []);

    const deleteUser = (id: number) => {
        api.delete(`/admin/users/${id}`, {
            headers: { Authorization: "Bearer " + localStorage.getItem("token") }
        }).then(() => {
            setUsers(users.filter(u => u.id !== id));
        });
    };

    const updateRole = (id: number, role: string) => {
        api.put(`/admin/users/${id}/role?role=${role}`, {}, {
            headers: { Authorization: "Bearer " + localStorage.getItem("token") }
        }).then(() => {
            setUsers(users.map(u => (u.id === id ? { ...u, role } : u)));
        });
    };

    return (
        <div className="p-6">
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
                {users.map(u => (
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
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default AdminPanel;
