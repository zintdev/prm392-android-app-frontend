# PRM392 Android – Chuẩn kiến trúc & quy ước coding (README)

> **Ngữ cảnh**: Android (Java, minSdk 21), Gradle Groovy DSL. Kiến trúc MVVM + Repository 1 tầng *data*. Giao tiếp Backend Spring Boot qua Retrofit, JWT lưu bằng `SessionManager`.

---

## 1) Mục tiêu

- Thống nhất cách code cho cả nhóm
- Tách bạch UI - Xử lý - Gọi data, dễ quản lý, test và bảo trì
- Chuẩn hóa kiến trúc

---

## 2) Vai trò các tầng

### UI (Activity/Fragment)
- Hiển trị layout và bắt sự kiện, dữ liệu 
- Gửi và nhận dữ liệu để render từ ViewModel

### ViewModel
- Điều phối luồng: nhận event từ UI và gọi xử lý ở mức độ dữ liệu (Repository)
- Đồng thời xử dụng Resource<T> để Loading/Success/Error

### Repository (data)
- Xử lý dữ liệu: Gọi Retrofit, đọc/lưu token qua SessionManager  
- Mapping: **RequestDomain → RequestDTO**, **DTO → DomainModel** (qua `Mapper`).  
- Chuẩn hóa lỗi (HTTP/IO/parse) -> trả Resource<Domain>

### Mapper
- Chuyển đổi RequestDomain -> RequestDTO, DTO -> DomainModel
- P/S: Tránh rò rĩ kiểu dữ liệu lên UI, dễ unit test(Thực tế)

### SessionManager
- Lưu/đọc token 
- Được `Interceptor` dùng để gắn `Authorization: Bearer <token>`.

### ApiClient + Api (Retrofit)
- Retrofit + OkHttp + Interceptor 
- Khai báo Api và gắn token vào request được gửi đi

---

## 3) Luồng tổng quát

```
Activity/Fragment (UI + nhận event)
  → ViewModel (điều phối; post Resource.Loading)
    → data/repository/*Repository (class cụ thể - chỉ 1 layer data)
      → mapper: DomainReq → DTOReq (nếu có body)
      → remote/api (Retrofit qua ApiClient + Interceptor JWT)
          → HTTP → Spring Boot: Controller → Service → JPA → PostgreSQL
          ← JSON (status + body)
      ← mapper: DTORsp → DomainModel
    ← ViewModel nhận Resource.Success/Error(domain data)
← UI observe → render (progress / adapter.submitList / error)
```

---

## 4) Quy tắc **Làm / Không làm**

**LÀM**
- UI chỉ thao tác UI; ViewModel chỉ điều phối.  
- `RepositoryImpl` chịu trách nhiệm dữ liệu: Retrofit, token, mapping, lỗi.  
- `Mapper` tách riêng để test tốt; **không** để DTO xuất hiện ở UI.  
- `Interceptor` gắn token tự động; bật **logging** ở build debug.

**KHÔNG LÀM**
- **Adapter** không gọi API / không chuyển domain → list phức tạp (chỉ bind).  
- Không pass **Context** lung tung vào repository/mapper.  
- Không dùng **DTO** ở UI; không lưu **token** trong ViewModel.

---

## 5) Luồng mẫu (use cases)

### 5.1 Đăng nhập
1. `LoginFragment` bắt click → `AuthViewModel.login(u, p)`.  
2. `AuthViewModel` post `Loading` → gọi `authRepository.login(LoginRequestDomain)`.  
3. `AuthRepository` → `AuthApi.login()` (Retrofit).  
4. Backend trả `200 {token,...}` hoặc `401`.  
5. Lưu token vào `SessionManager`, map **DTO → Domain**, trả `Success`.  
6. `AuthViewModel` post `Success` → UI điều hướng; `Error` → UI hiển thị thông báo.

### 5.2 Lấy danh sách blog
1. `BlogListFragment.onViewCreated()` → `BlogViewModel.load(page,size)`.  
2. `BlogViewModel` post `Loading` → `blogRepository.getBlogs(page,size)`.  
3. `BlogRepository` gọi `BlogApi.getBlogs(...)` (Interceptor tự gắn `Bearer <token>`).  
4. Map `List<BlogDto> → List<Blog>` → `Success`.  
5. UI nhận `Success` → `adapter.submitList(list)`.

---
