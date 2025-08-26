import React, { useEffect, useState } from "react";
import {
    createInternalTicket,
    InternalTicketRequest,
    getCategories,
    Category,
} from "../api/ticketApi";
import { getMyProfile, MyProfile } from "../api/personApi";

const CreateTicketPage: React.FC = () => {
    const [categories, setCategories] = useState<Category[]>([]);
    const [deptId, setDeptId] = useState<number | null>(null);

    const [ticket, setTicket] = useState<InternalTicketRequest>({
        issue: "",
        priority: "LOW",
        categoryId: undefined,
    });

    // İlk açılışta profil + kategori çek
    useEffect(() => {
        getMyProfile()
            .then((me: MyProfile) => {
                if (me?.department?.id) {
                    setDeptId(me.department.id);
                } else if (me?.departmentId) {
                    setDeptId(me.departmentId);
                }
            })
            .catch((e) => console.error("Profil alınamadı:", e));

        getCategories().then(setCategories).catch(console.error);
    }, []);

    const handleCreate = async () => {
        if (!deptId || !ticket.issue || !ticket.categoryId) {
            alert("⚠️ Lütfen açıklama ve kategori seçiniz!");
            return;
        }
        try {
            await createInternalTicket(deptId, ticket);
            alert("✅ Ticket oluşturuldu!");
            setTicket({ issue: "", priority: "LOW", categoryId: undefined });
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

                {/* Kategori seçimi */}
                <div>
                    <label className="block mb-2 font-semibold">Kategori</label>
                    <select
                        className="border p-2 rounded text-black w-full"
                        value={ticket.categoryId ?? ""}
                        onChange={(e) =>
                            setTicket({
                                ...ticket,
                                categoryId: e.target.value ? Number(e.target.value) : undefined,
                            })
                        }
                    >
                        <option value="">Kategori Seç</option>
                        {categories.map((c) => (
                            <option key={c.id} value={c.id}>
                                {c.displayName}
                            </option>
                        ))}
                    </select>
                </div>

                {/* Öncelik */}
                <div>
                    <label className="block mb-2 font-semibold">Öncelik</label>
                    <select
                        className="border p-2 rounded text-black w-full"
                        value={ticket.priority}
                        onChange={(e) =>
                            setTicket({ ...ticket, priority: e.target.value })
                        }
                    >
                        <option value="LOW">Düşük</option>
                        <option value="MEDIUM">Orta</option>
                        <option value="HIGH">Yüksek</option>
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
