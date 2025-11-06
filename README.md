# RMI Calculator - Hệ Thống Máy Tính Phân Tán

## 📋 Tổng Quan

Hệ thống máy tính khoa học sử dụng RMI (Remote Method Invocation) với kiến trúc **đa server**:
- **MathServer**: Xử lý phép toán cơ bản (+, -, *, /, ^, √)
- **TrigServer**: Xử lý hàm lượng giác (sin, cos, tan)
- **Client**: Kết nối đến cả 2 server và tự động phân tuyến yêu cầu

## 🎯 Quy Trình Hoạt Động

### Bước 1: Khởi động Server

1. **Chạy MathServer** (port 5050)
   ```
   Chạy: rmi.calculator.server.MathServer
   - Nhấn "Start Server" trong giao diện
   - Server sẽ binding "MathService" trên port 5050
   ```

2. **Chạy TrigServer** (port 5051)
   ```
   Chạy: rmi.calculator.server.TrigServer
   - Nhấn "Start Server" trong giao diện
   - Server sẽ binding "TrigService" trên port 5051
   ```

**Lưu ý**: 
- Có thể chạy 2 server trên cùng 1 máy (cổng khác nhau)
- Hoặc chạy trên 2 máy khác nhau (cùng cổng hoặc khác cổng)
- Nếu server chưa chạy, client vẫn hoạt động nhưng phép toán của server đó sẽ báo lỗi

### Bước 2: Khởi động Client

1. **Chạy Client**
   ```
   Chạy: rmi.calculator.client.CalculatorClient
   ```

2. **Đăng nhập**
   - Nhập **Server IP**: IP của máy chạy server (ví dụ: `192.168.1.100`)
   - Nhập **Username**: Tên người dùng (ví dụ: `user1`)
   - Nhấn **OK**

3. **Client tự động kết nối**
   - Kết nối đến MathServer tại `host:5050`
   - Kết nối đến TrigServer tại `host:5051`
   - Nếu server chưa chạy: chỉ log lỗi, không chặn ứng dụng

### Bước 3: Sử dụng Máy Tính

**Phép toán cơ bản** (gửi đến MathServer):
- `+`, `-`, `*`, `/`: Cộng, trừ, nhân, chia
- `x^y`: Lũy thừa
- `√`: Căn bậc 2

**Hàm lượng giác** (gửi đến TrigServer):
- `sin`, `cos`, `tan`: Sin, Cos, Tan

**Ví dụ sử dụng**:
1. Nhập `2` → Nhấn `+` → Nhấn `3` → Nhấn `=` → Kết quả: `5` (gửi đến MathServer)
2. Nhập `30` → Nhấn `sin` → Kết quả: `0.5` (gửi đến TrigServer)

## 📁 Cấu Trúc Thư Mục

```
src/rmi/calculator/
├── common/                    # Interface chung
│   ├── MathService.java      # Interface cho MathServer
│   ├── TrigService.java      # Interface cho TrigServer
│   └── CalculatorService.java # Interface cho server cũ (single)
│
├── server/                   # Mã nguồn server
│   ├── common/               # Code dùng chung
│   │   ├── ServerUI.java     # Giao diện server (start/stop)
│   │   └── ServerLogger.java # Logger cho server
│   │
│   ├── math/                 # Server toán học
│   │   └── MathServiceImpl.java
│   │
│   ├── trig/                 # Server lượng giác
│   │   └── TrigServiceImpl.java
│   │
│   ├── MathServer.java       # Entry point MathServer
│   ├── TrigServer.java       # Entry point TrigServer
│   ├── CalculatorServer.java # Server cũ (single - giữ lại để tương thích)
│   └── CalculatorServiceImpl.java
│
└── client/                   # Mã nguồn client
    ├── CalculatorClient.java      # Entry point client
    ├── MultiServerClientUI.java   # Giao diện máy tính (kết nối 2 server)
    ├── LoginDialog.java          # Dialog đăng nhập
    ├── ClientLogger.java         # Logger cho client
    ├── ClientUI.java             # Client cũ (single - giữ lại để tương thích)
    └── MultiServerLoginDialog.java # Login dialog cũ (không dùng nữa)
```

## 🔄 Luồng Xử Lý Yêu Cầu

```
Client (Người dùng nhấn phím)
    ↓
MultiServerClientUI (Phân tích phép toán)
    ↓
    ├─→ Phép toán 2 ngôi (+, -, *, /, ^, √)
    │       ↓
    │   computeMathRemote()
    │       ↓
    │   RMI Call → MathServer:5050
    │       ↓
    │   MathServiceImpl (xử lý)
    │       ↓
    │   Log: [clientId] [Math] op=...
    │       ↓
    │   Trả kết quả về Client
    │
    └─→ Hàm lượng giác (sin, cos, tan)
            ↓
        applyUnaryCompute()
            ↓
        RMI Call → TrigServer:5051
            ↓
        TrigServiceImpl (xử lý)
            ↓
        Log: [clientId] [Trig] op=...
            ↓
        Trả kết quả về Client
```

## 📊 Logging

**Server Log** (hiển thị trong ServerUI):
```
[2024-01-15 10:30:45.123] [INFO] [user1 192.168.1.50] [Math] op=add a=2.0 b=3.0
[2024-01-15 10:30:45.125] [INFO] [user1 192.168.1.50] result=5.0
```

**Client Log** (hiển thị trong MultiServerClientUI):
```
[2024-01-15 10:30:45.120] [INFO] [user1 192.168.1.50] Connected to MathService at 192.168.1.100:5050
[2024-01-15 10:30:45.126] [INFO] [user1 192.168.1.50] = result=5.0
```

## ⚙️ Cấu Hình

### Ports Mặc Định
- **MathServer**: `5050`
- **TrigServer**: `5051`

### Binding Names
- **MathService**: `"MathService"`
- **TrigService**: `"TrigService"`

## 🚀 Tính Năng Nổi Bật

1. **Phân tán tải**: Mỗi server xử lý nhóm phép toán riêng
2. **Tự động phân tuyến**: Client tự động gửi đến server phù hợp
3. **Tương thích ngược**: Server cũ (CalculatorServer) vẫn hoạt động
4. **Logging rõ ràng**: Mỗi request có tag client và nhóm phép toán
5. **Linh hoạt**: Server có thể chạy trên máy khác nhau

## 📝 Lưu Ý

- Đảm bảo firewall cho phép port 5050 và 5051
- Server phải chạy trước khi client kết nối
- Nếu một server down, các phép toán của server đó sẽ báo lỗi, nhưng server còn lại vẫn hoạt động bình thường
- Client tự động log lỗi kết nối, không chặn ứng dụng

