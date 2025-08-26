    import axios from "axios";

    /** Temel Person tipi */
    export interface Person {
        id: number;
        name: string;
        surname: string;
        email: string;
        phone?: string | null;
        active: boolean;
        departmentId?: number | null;
    }

    /** Profil yanıtında departman nesnesi de dönebileceği için genişlettik */
    export interface MyProfile extends Person {
        department?: { id: number; name: string } | null;
    }

    const personApi = axios.create({
        baseURL: "http://localhost:8082", // person-service
        headers: { "Content-Type": "application/json" },
    });

    // 🔑 JWT ekle (localStorage'da token tırnaklı saklandıysa da çalışır)
    personApi.interceptors.request.use((config) => {
        const raw = localStorage.getItem("token");
        if (raw) {
            let token = raw;
            try {
                // Bazı yerlerde localStorage.setItem ile JSON.stringify edilmiş olabilir
                if (raw.startsWith('"')) token = JSON.parse(raw);
            } catch {
                // no-op
            }
            config.headers = config.headers ?? {};
            (config.headers as any).Authorization = `Bearer ${token}`;
        }
        return config;
    });

    export default personApi;

    /* =========================
     * Admin uçları
     * ======================= */

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
        const { data } = await personApi.put(`/api/admin/persons/${personId}/departments`, {
            departmentId,
        });
        return data;
    }

    /* =========================
     * Kullanıcı (person) uçları
     * ======================= */

    // ✅ Giriş yapan kişinin profili; varsa deptId'yi localStorage'a yazar
    export async function getMyProfile(): Promise<MyProfile> {
        // ❗️ Artık doğru path: /api/persons/me
        const { data } = await personApi.get<MyProfile>("/api/persons/me");
        const deptId = data?.department?.id ?? data?.departmentId ?? null;
        if (deptId) localStorage.setItem("deptId", String(deptId));
        return data;
    }

    // ✅ localStorage'daki deptId'yi oku (yoksa null döner)
    export function getDeptIdCached(): number | null {
        const s = localStorage.getItem("deptId");
        return s ? parseInt(s, 10) : null;
    }
