package rmi.calculator.server;

import javax.swing.*;
import rmi.calculator.server.common.ServerUI;
import rmi.calculator.server.trig.TrigServiceImpl;

/**
 * TrigServer - Server xử lý các hàm lượng giác
 * 
 * CHỨC NĂNG:
 * - Chạy trên port 5051
 * - Binding name: "TrigService"
 * - Xử lý các hàm lượng giác: sin, cos, tan
 * 
 * CÁCH SỬ DỤNG:
 * 1. Chạy TrigServer này (có thể chạy đồng thời với MathServer)
 * 2. Nhấn "Start Server" trong giao diện
 * 3. Client có thể kết nối đến server này để thực hiện phép tính lượng giác
 * 
 * LƯU Ý: Server này có thể chạy trên máy khác hoặc cùng máy với MathServer
 */
public class TrigServer {
    public static void main(String[] args) {
        final int port = 5051;        // Cổng mặc định cho TrigServer (khác với MathServer)
        final String binding = "TrigService"; // Tên binding trong RMI registry
        
        SwingUtilities.invokeLater(() -> {
            // Tạo giao diện server với factory tạo TrigServiceImpl
            ServerUI ui = new ServerUI(port, binding, logger -> {
                try {
                    // Tạo implementation của TrigService
                    return new TrigServiceImpl(logger);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            ui.setVisible(true);
        });
    }
}


