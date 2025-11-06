package rmi.calculator.server;

import javax.swing.*;
import rmi.calculator.server.common.ServerUI;
import rmi.calculator.server.math.MathServiceImpl;

/**
 * MathServer - Server xử lý các phép toán cơ bản
 * 
 * CHỨC NĂNG:
 * - Chạy trên port 5050
 * - Binding name: "MathService"
 * - Xử lý các phép toán: +, -, *, /, ^ (lũy thừa), √ (căn bậc 2)
 * 
 * CÁCH SỬ DỤNG:
 * 1. Chạy MathServer này trước
 * 2. Nhấn "Start Server" trong giao diện
 * 3. Client có thể kết nối đến server này để thực hiện phép toán cơ bản
 */
public class MathServer {
    public static void main(String[] args) {
        final int port = 5050;        // Cổng mặc định cho MathServer
        final String binding = "MathService"; // Tên binding trong RMI registry
        
        SwingUtilities.invokeLater(() -> {
            // Tạo giao diện server với factory tạo MathServiceImpl
            ServerUI ui = new ServerUI(port, binding, logger -> {
                try {
                    // Tạo implementation của MathService
                    return new MathServiceImpl(logger);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            ui.setVisible(true);
        });
    }
}


