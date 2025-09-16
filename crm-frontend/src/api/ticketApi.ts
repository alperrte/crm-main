import axios from "axios";

// API instance
const ticketApi = axios.create({
    baseURL: "http://localhost:8084",
});

// Token güvenli okuma
const getToken = () => {
    const raw = localStorage.getItem("token");
    if (!raw) return null;
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
    email: string | null;   // müşteri email
    name: string | null;
    surname: string | null;
    phone: string | null;
    description: string;
    priority: string;
    active: boolean;
    createdDate?: string;
    closedDate?: string;
    employee?: boolean; // ✅ backend’den geliyor
    creatorPersonEmail?: string | null;   // ✅ çalışan email
    creatorPersonName?: string | null;
    creatorPersonSurname?: string | null;
    departmentName?: string;
}

export interface DeptTicket {
    id: number;
    customerEmail?: string;
    customerName?: string;
    customerSurname?: string;
    customerPhone?: string | null;
    issue: string;
    priority: string;
    active: boolean;
    createdDate?: string;
    closedDate?: string;

    // ✅ Backend’den gelen çalışan bilgileri
    creatorPersonEmail?: string | null;
    creatorPersonName?: string | null;
    creatorPersonSurname?: string | null;

    // 🔹 Backend’den gelen durum bilgileri
    status?: string;
    departmentId?: number;
    employee: boolean; // true = personel açtı, false = müşteri açtı

    // ✅ Üstlenen kişi bilgileri
    assigneeEmail?: string;
    assigneeName?: string;
    assigneeSurname?: string;

    // ✅ Devretme takibi için ek alanlar
    fromDepartmentId?: number;
    toDepartmentId?: number;
}

export interface InternalTicketRequest {
    issue: string;
    priority: string;
    categoryId?: number | null;     // user-panel için opsiyonel
    personId?: number | null;       // backend JWT’den alıyor
    departmentId?: number | null;   // departman ticketları için opsiyonel
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
    const arr = res.data as Array<any>;
    return arr.map((t) => {
        const isEmployee = t.employee === true;

        return {
            id: t.id,
            email: isEmployee ? t.creatorPersonEmail : t.customerEmail,
            name: isEmployee ? t.creatorPersonName : t.customerName,
            surname: isEmployee ? t.creatorPersonSurname : t.customerSurname,
            phone: isEmployee ? null : t.customerPhone,
            description: t.issue,
            priority: t.priority,
            active: t.active,
            createdDate: t.createdDate,
            closedDate: t.closedDate,
            employee: t.employee,
            creatorPersonEmail: t.creatorPersonEmail,
            departmentName: t.departmentName ?? "-", // ✅ departman adı ekledik
        };
    });
};

// ========== Departman Ticket API Fonksiyonları (PERSON/ADMIN) ==========
export const getDeptTickets = async (deptId: number): Promise<DeptTicket[]> => {
    const res = await ticketApi.get(`/api/departments/${deptId}/tickets`);
    return res.data;
};

export const takeTicket = async (ticketId: number, deptId: number): Promise<DeptTicket> => {
    const res = await ticketApi.put(`/api/departments/tickets/${ticketId}/take?deptId=${deptId}`);
    return res.data;
};

export const reassignTicket = async (
    ticketId: number,
    fromDeptId: number,
    newDeptId: number
): Promise<DeptTicket> => {
    const res = await ticketApi.put(
        `/api/departments/tickets/${ticketId}/reassign/${newDeptId}?fromDeptId=${fromDeptId}`
    );
    return res.data;
};

export const closeTicket = async (ticketId: number): Promise<DeptTicket> => {
    const res = await ticketApi.put(`/api/departments/tickets/${ticketId}/close`);
    return res.data;
};

// İç ticket oluştur (departman/çalışan)
export const createInternalTicket = async (
    data: InternalTicketRequest
): Promise<DeptTicket> => {
    const payload = {
        issue: data.issue,
        priority: data.priority,
        categoryId: data.categoryId ?? null,
        personId: data.personId ?? null,
        departmentId: data.departmentId ?? null
    };
    const res = await ticketApi.post(`/api/departments/tickets/internal`, payload, {
        headers: { "Content-Type": "application/json" },
    });
    return res.data;
};

// ========== USER Ticket API Fonksiyonları ==========
export const getUserTickets = async (): Promise<DeptTicket[]> => {
    const res = await ticketApi.get("/api/user-panel/tickets");
    return res.data;
};

export const createUserTicket = async (
    data: InternalTicketRequest
): Promise<DeptTicket> => {
    const payload = {
        issue: data.issue,
        priority: data.priority,
        categoryId: data.categoryId ?? null,
        personId: data.personId ?? null,        // ✅ backend null kabul edecek
        departmentId: data.departmentId ?? null // ✅ user için zorunlu değil
    };
    const res = await ticketApi.post(`/api/user-panel/tickets`, payload, {
        headers: { "Content-Type": "application/json" },
    });
    return res.data;
};

export default ticketApi;