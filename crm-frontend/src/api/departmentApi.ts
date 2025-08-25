import axios from "axios";

export interface Department {
    id: number;
    name: string;
    parentId?: number | null;
}

const departmentApi = axios.create({
    baseURL: "http://localhost:8081", // department-service (farklıysa değiştirin)
    headers: { "Content-Type": "application/json" },
});

departmentApi.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// ✅ Departmanları getir (normalize ederek)
export async function getAllDepartments(): Promise<Department[]> {
    const { data } = await departmentApi.get("/api/departments");
    // Backend alan adları değişken olabilir; normalize edelim
    const mapped: Department[] = (Array.isArray(data) ? data : []).map((d: any) => ({
        id: d.id ?? d.departmentId ?? d.department_id,
        name: d.name ?? d.displayName ?? d.departmentName,
        parentId:
            d.parentId ?? d.parent_department_id ?? d.parentDepartmentId ?? null,
    }));
    return mapped;
}

export default departmentApi;
