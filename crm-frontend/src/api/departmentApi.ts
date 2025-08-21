// src/api/departmentApi.ts
import axios from "axios";

const departmentApi = axios.create({
    baseURL: "http://localhost:8081",
    headers: {
        "Content-Type": "application/json",
    },
});

// 🔑 Sadece mevcut JWT’yi ekle (department-service yeni token üretmez)
departmentApi.interceptors.request.use((config) => {
    const token = localStorage.getItem("token"); // user-service’ten alınmış JWT
    if (token) {
        config.headers = config.headers ?? {};
        (config.headers as any).Authorization = `Bearer ${token}`;
    }
    return config;
});

export default departmentApi;
