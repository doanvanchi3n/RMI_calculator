package rmi.calculator.client;

import javax.swing.*;

/**
 * Client chính - điểm khởi đầu của ứng dụng máy tính RMI
 * 
 * QUY TRÌNH:
 * 1. Hiển thị dialog đăng nhập (Server IP + Username)
 * 2. Kết nối đến MathServer (port 5050) và TrigServer (port 5051) trên cùng host
 * 3. Khởi tạo giao diện máy tính với khả năng gọi RMI đến 2 server
 */
public class CalculatorClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Bước 1: Hiển thị dialog đăng nhập
            rmi.calculator.client.LoginDialog dialog = new rmi.calculator.client.LoginDialog(null);
            dialog.setVisible(true);
            if (!dialog.isApproved()) {
                System.exit(0);
                return;
            }
            
            // Bước 2: Lấy thông tin từ dialog
            String host = dialog.getHost();        // IP của server (ví dụ: 192.168.1.100)
            String username = dialog.getUsername(); // Tên người dùng
            
            // Bước 3: Tạo giao diện client kết nối đến 2 server
            // - MathServer: host:5050 (xử lý +, -, *, /, ^, √)
            // - TrigServer: host:5051 (xử lý sin, cos, tan)
            MultiServerClientUI ui = new MultiServerClientUI(host, 5050, host, 5051, username);
            ui.setVisible(true);
        });
    }
}


