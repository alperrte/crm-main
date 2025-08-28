import React, { useEffect, useState } from "react";
import {
    createInternalTicket,
    InternalTicketRequest,
} from "../api/ticketApi";
import { getAllDepartments, Department } from "../api/departmentApi";

const CreateTicketPage: React.FC = () => {
    const [departments, setDepartments] = useState<Department[]>([]);
    const [ticket, setTicket] = useState<InternalTicketRequest>({
        issue: "",
        priority: "LOW",
        departmentId: undefined, // opsiyonel
    });

    // Departman listesini çek
    useEffect(() => {
        getAllDepartments()
            .then(setDepartments)
            .catch((e) => console.error("Departmanlar alınamadı:", e));
    }, []);

    const handleCreate = async () => {
        if (!ticket.issue || !ticket.departmentId) {
            alert("⚠️ Lütfen açıklama ve departman seçiniz!");
            return;
        }
        try {
            await createInternalTicket(ticket);
            alert("✅ Ticket oluşturuldu!");
            setTicket({ issue: "", priority: "LOW", departmentId: undefined }); // reset
        } catch (err) {
            console.error("Ticket oluşturulamadı:", err);
            alert("❌ Ticket oluşturulurken hata oluştu!");
        }
    };

    return (
        <div className="p-8 bg-gray-900 min-h-screen text-white">
            <h1 className="text-3xl font-bold mb-6">🎫 Yeni Ticket Oluştur</h1>

            <div className="bg-gray-800 p-6 rounded-lg shadow space-y-4 max-w-3xl mx-auto">
                {/* Açıklama */}
                <div>
                    <label className="block mb-2 font-semibold">Sorun Açıklaması</label>
                    <textarea
                        className="w-full p-3 rounded text-black"
                        rows={6}
                        placeholder="Detaylı olarak açıklayın..."
                        value={ticket.issue}
                        onChange={(e) => setTicket({ ...ticket, issue: e.target.value })}
                    />
                </div>

                {/* Öncelik */}
                <div>
                    <label className="block mb-2 font-semibold">Öncelik</label>
                    <select
                        className="border p-2 rounded text-black w-full"
                        value={ticket.priority}
                        onChange={(e) =>
                            setTicket({ ...ticket, priority: e.target.value as "LOW" | "MEDIUM" | "HIGH" })
                        }
                    >
                        <option value="LOW">Düşük</option>
                        <option value="MEDIUM">Orta</option>
                        <option value="HIGH">Yüksek</option>
                    </select>
                </div>

                {/* Departman seçimi */}
                <div>
                    <label className="block mb-2 font-semibold">Departman</label>
                    <select
                        className="border p-2 rounded text-black w-full"
                        value={ticket.departmentId ?? ""}
                        onChange={(e) =>
                            setTicket({
                                ...ticket,
                                departmentId: e.target.value ? Number(e.target.value) : undefined,
                            })
                        }
                    >
                        <option value="">Departman Seç</option>
                        {departments.map((d) => (
                            <option key={d.id} value={d.id}>
                                {d.name}
                            </option>
                        ))}
                    </select>
                </div>

                {/* Buton */}
                <div className="text-right">
                    <button
                        className="bg-blue-500 text-white px-6 py-2 rounded-lg font-semibold hover:bg-blue-600"
                        onClick={handleCreate}
                    >
                        Oluştur
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CreateTicketPage;
