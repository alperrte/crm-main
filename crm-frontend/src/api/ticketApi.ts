import axios from "axios";

// API instance
const ticketApi = axios.create({
    baseURL: "http://localhost:8084",
});

// Token güvenli okuma
const getToken = () => {
    const raw = localStorage.getItem("token");
    if (!raw) return null;
    // Eğer yanlışlıkla JSON.stringify ile kaydedildiyse
    return raw.startsWith('"') ? JSON.parse(raw) : raw;
};

// ✅ Her istek öncesi Authorization ekle
ticketApi.interceptors.request.use((config) => {
    const token = getToken();
    if (token) {
        config.headers = config.headers || {};
        config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
});

// ========== Types ==========
export interface Category {
    id: number;
    displayName: string;
}

export interface PublicTicketFormData {
    email: string;
    firstName: string;
    lastName: string;
    phone?: string;
    description: string;   // backend: issue
    priority: string;
    categoryId: number | string;
}

export interface AdminTicket {
    id: number;
    email: string;
    name: string;
    surname: string;
    phone: string | null;
    description: string;
    priority: string;
    active: boolean;
    createdDate?: string;
}

// ========== API Fonksiyonları ==========

// Public ticket oluşturma
export const createPublicTicket = async (data: PublicTicketFormData) => {
    const payload = {
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        phone: data.phone,
        issue: data.description,
        priority: data.priority,
        categoryId: Number(data.categoryId),
    };
    const res = await ticketApi.post("/api/tickets/public", payload, {
        headers: { "Content-Type": "application/json" },
    });
    return res.data;
};

// Kategori listesi
export const getCategories = async (): Promise<Category[]> => {
    const res = await ticketApi.get("/api/categories");
    const arr = res.data as Array<{ id: number; displayName?: string; name?: string; key?: string }>;
    return arr.map((c) => ({
        id: c.id,
        displayName: c.displayName ?? c.name ?? String(c.key ?? ""),
    }));
};

// Admin ticket listesi
export const getAdminTickets = async (): Promise<AdminTicket[]> => {
    const res = await ticketApi.get("/api/admin/tickets");
    const arr = res.data as Array<{
        id: number;
        customerEmail: string;
        customerName: string;
        customerSurname: string;
        customerPhone: string | null;
        issue: string;
        priority: string;
        active: boolean;
        createdDate?: string;
    }>;
    return arr.map((t) => ({
        id: t.id,
        email: t.customerEmail,
        name: t.customerName,
        surname: t.customerSurname,
        phone: t.customerPhone,
        description: t.issue,
        priority: t.priority,
        active: t.active,
        createdDate: t.createdDate,
    }));
};

export default ticketApi;
