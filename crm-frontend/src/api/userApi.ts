import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8083", // user-service backend portu
    headers: { "Content-Type": "application/json" },
});

// 🔑 Tüm isteklerde JWT token otomatik ekle
api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;

/* ================================
   ✅ Kayıt için DTO & Fonksiyon
================================ */
export interface RegisterRequest {
    name: string;
    surname: string;
    email: string;
    phone: string;
    password: string;
}

export async function registerUser(req: RegisterRequest) {
    const { data } = await api.post("/api/auth/register", req);
    return data;
}

/* ================================
   ✅ Admin → Kullanıcıyı PERSON yap
================================ */
export async function makeUserPerson(userId: number) {
    const { data } = await api.put(`/admin/users/${userId}/make-person`);
    return data;
}
