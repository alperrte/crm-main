import React, { useState, useEffect } from "react";
import { createPublicTicket, getCategories, Category, PublicTicketFormData } from "../api/ticketApi";

const PublicTicketForm: React.FC = () => {
    const [form, setForm] = useState<PublicTicketFormData>({
        email: "",
        firstName: "",
        lastName: "",
        phone: "",
        description: "",
        categoryId: "",
        priority: "MEDIUM",
    });

    const [categories, setCategories] = useState<Category[]>([]);
    const [message, setMessage] = useState("");

    useEffect(() => {
        getCategories().then(setCategories).catch((e) => console.error(e));
    }, []);

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
    ) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setMessage("");
        try {
            await createPublicTicket(form);
            setMessage("Ticket başarıyla oluşturuldu!");
            setForm({
                email: "",
                firstName: "",
                lastName: "",
                phone: "",
                description: "",
                categoryId: "",
                priority: "MEDIUM",
            });
        } catch (err) {
            setMessage("Ticket oluşturulurken hata oluştu.");
        }
    };

    return (
        <div className="p-6 max-w-lg mx-auto">
            <h2 className="text-xl font-bold mb-4">Ticket Oluştur</h2>
            <form onSubmit={handleSubmit} className="space-y-3">
                <input type="email" name="email" placeholder="Email"
                       value={form.email} onChange={handleChange} className="border p-2 w-full" required />
                <input type="text" name="firstName" placeholder="Ad"
                       value={form.firstName} onChange={handleChange} className="border p-2 w-full" required />
                <input type="text" name="lastName" placeholder="Soyad"
                       value={form.lastName} onChange={handleChange} className="border p-2 w-full" required />
                <input type="text" name="phone" placeholder="Telefon"
                       value={form.phone} onChange={handleChange} className="border p-2 w-full" />
                <textarea name="description" placeholder="Sorununuzu açıklayın"
                          value={form.description} onChange={handleChange} className="border p-2 w-full" required />

                {/* ✅ Kategoriler */}
                <select name="categoryId" value={form.categoryId} onChange={handleChange}
                        className="border p-2 w-full" required>
                    <option value="">Kategori Seç</option>
                    {categories.map((c) => (
                        <option key={c.id} value={c.id}>
                            {c.displayName}
                        </option>
                    ))}
                </select>

                {/* ✅ Öncelik */}
                <select name="priority" value={form.priority} onChange={handleChange} className="border p-2 w-full">
                    <option value="LOW">Düşük</option>
                    <option value="MEDIUM">Orta</option>
                    <option value="HIGH">Yüksek</option>
                </select>

                <button type="submit" className="bg-blue-500 text-white px-4 py-2 rounded">
                    Gönder
                </button>
            </form>
            {message && <p className="mt-3 text-green-600">{message}</p>}
        </div>
    );
};

export default PublicTicketForm;
