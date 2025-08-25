import axios from "axios";

export interface Person {
    id: number;
    name: string;
    surname: string;
    email: string;
    phone?: string | null;
    active: boolean;
    departmentId?: number | null;
}

const personApi = axios.create({
    baseURL: "http://localhost:8082", // person-service
    headers: { "Content-Type": "application/json" },
});

// 🔑 JWT ekleyelim
personApi.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default personApi;

// ✅ Aktif tüm personları getir (admin ucu)
export async function getAdminPersons(): Promise<Person[]> {
    const { data } = await personApi.get("/api/admin/persons");
    return data;
}

// ✅ Departman atanmamış personları getir
export async function getUnassignedPersons(): Promise<Person[]> {
    const { data } = await personApi.get("/api/admin/persons/unassigned");
    return data;
}

// ✅ Belirli departmandaki personlar
export async function getPersonsByDepartment(depId: number): Promise<Person[]> {
    const { data } = await personApi.get(`/api/admin/persons/department/${depId}`);
    return data;
}

// ✅ Departman atama
export async function assignDepartment(personId: number, departmentId: number) {
    const { data } = await personApi.put(`/api/admin/persons/${personId}/department`, {
        departmentId,
    });
    return data;
}
