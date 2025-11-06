package rmi.calculator.client;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.*;
import rmi.calculator.common.MathService;
import rmi.calculator.common.TrigService;

/**
 * Giao diện máy tính client kết nối đến nhiều server RMI
 * 
 * CHỨC NĂNG:
 * - Kết nối đến MathServer (port 5050) để xử lý phép toán cơ bản: +, -, *, /, ^, √
 * - Kết nối đến TrigServer (port 5051) để xử lý hàm lượng giác: sin, cos, tan
 * - Tự động phân tuyến yêu cầu đến server phù hợp
 * - Log rõ ràng các thao tác và kết quả
 * 
 * QUY TRÌNH HOẠT ĐỘNG:
 * 1. Khi khởi tạo: Tự động kết nối đến cả 2 server (nếu server chưa chạy thì chỉ log lỗi)
 * 2. Người dùng nhấn phép toán: Client tự động gửi đến server phù hợp qua RMI
 * 3. Server xử lý và trả kết quả: Hiển thị trên màn hình và log
 */
public class MultiServerClientUI extends JFrame {
    // Các thành phần giao diện
    private final JTextField display = new JTextField("0");        // Màn hình hiển thị số
    private final JLabel expressionLabel = new JLabel(" ");         // Hiển thị biểu thức đang nhập
    private final JTextArea logArea = new JTextArea();              // Vùng log hoạt động

    // Các service RMI - kết nối đến server
    private MathService mathService;    // Service xử lý phép toán cơ bản (+, -, *, /, ^, √)
    private TrigService trigService;    // Service xử lý hàm lượng giác (sin, cos, tan)
    
    // Thông tin client
    private final String username;      // Tên người dùng
    private final String clientIp;      // IP của client
    private final String clientTag;    // Tag để log: username + IP
    private ClientLogger logger;        // Logger để ghi log

    // Thông tin kết nối server
    private final String mathHost;      // IP của MathServer
    private final int mathPort;        // Port của MathServer (mặc định 5050)
    private final String trigHost;     // IP của TrigServer
    private final int trigPort;        // Port của TrigServer (mặc định 5051)

    // Trạng thái máy tính
    private Double accumulator = null;      // Số tích lũy (số bên trái phép toán)
    private String pendingOp = null;       // Phép toán đang chờ: "+", "-", "*", "/", "^"
    private boolean resetInput = true;     // Cờ reset input khi nhận kết quả
    private String pendingUnary = null;    // Hàm một ngôi đang chờ: "sqrt", "sin", "cos", "tan"

    public MultiServerClientUI(String mathHost, int mathPort, String trigHost, int trigPort, String username) {
        super("RMI Scientific Calculator - Multi-Server Client");
        this.mathHost = mathHost;
        this.mathPort = mathPort;
        this.trigHost = trigHost;
        this.trigPort = trigPort;
        this.username = username;
        this.clientIp = resolveLocalIp();
        this.clientTag = username + " " + clientIp;
        initUI();
        connect();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 600);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new GridLayout(3, 1));
        header.add(new JLabel("User: " + username));
        header.add(new JLabel("Client IP: " + clientIp));
        header.add(new JLabel("Math: " + mathHost + ":" + mathPort + "  |  Trig: " + trigHost + ":" + trigPort));

        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(display.getFont().deriveFont(Font.BOLD, 24f));

        expressionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        expressionLabel.setFont(expressionLabel.getFont().deriveFont(Font.PLAIN, 14f));

        JPanel keypad = buildKeypad();

        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(400, 200));

        JPanel center = new JPanel(new BorderLayout(8, 8));
        JPanel screen = new JPanel(new GridLayout(2,1));
        screen.add(expressionLabel);
        screen.add(display);
        center.add(screen, BorderLayout.NORTH);
        center.add(keypad, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(center, BorderLayout.CENTER);
        getContentPane().add(logScroll, BorderLayout.SOUTH);

        logger = new ClientLogger(logArea, clientTag);
    }

    private JPanel buildKeypad() {
        String[][] labels = {
                {"DEL", "AC", "+/-", "√"},
                {"7", "8", "9", "/"},
                {"4", "5", "6", "*"},
                {"1", "2", "3", "-"},
                {"0", ".", "x^y", "+"},
                {"sin", "cos", "tan", "="}
        };
        JPanel panel = new JPanel(new GridLayout(labels.length, labels[0].length, 6, 6));
        for (String[] row : labels) {
            for (String label : row) {
                JButton btn = new JButton(label);
                btn.addActionListener(e -> onButton(label));
                panel.add(btn);
            }
        }
        return panel;
    }

    private void onButton(String label) {
        switch (label) {
            case "0": case "1": case "2": case "3": case "4":
            case "5": case "6": case "7": case "8": case "9":
                appendDigit(label);
                break;
            case ".":
                appendDot();
                break;
            case "+/-":
                toggleSign();
                break;
            case "DEL": backspace(); break;
            case "AC": clearAll(); break;
            case "+": case "-": case "*": case "/":
                performBinaryOp(label);
                break;
            case "x^y":
                performBinaryOp("^");
                break;
            case "=":
                evaluateEquals();
                break;
            case "√":
                onUnaryPressed("sqrt");
                break;
            case "sin": case "cos": case "tan":
                onUnaryPressed(label);
                break;
            default:
                break;
        }
    }

    /**
     * Kết nối đến 2 server RMI (MathServer và TrigServer)
     * - Nếu server chưa chạy: chỉ log lỗi, không chặn ứng dụng
     * - Người dùng vẫn có thể dùng các phép toán của server đang chạy
     */
    private void connect() {
        // Kết nối đến MathServer (port 5050)
        try {
            Registry mathReg = LocateRegistry.getRegistry(mathHost, mathPort);
            mathService = (MathService) mathReg.lookup("MathService");
            logger.info("Connected to MathService at " + mathHost + ":" + mathPort);
        } catch (Exception ex) {
            logger.error("Failed to connect to MathService", ex);
            // Không hiển thị popup - để người dùng vẫn có thể dùng TrigServer
        }
        
        // Kết nối đến TrigServer (port 5051)
        try {
            Registry trigReg = LocateRegistry.getRegistry(trigHost, trigPort);
            trigService = (TrigService) trigReg.lookup("TrigService");
            logger.info("Connected to TrigService at " + trigHost + ":" + trigPort);
        } catch (Exception ex) {
            logger.error("Failed to connect to TrigService", ex);
            // Không hiển thị popup - để người dùng vẫn có thể dùng MathServer
        }
    }

    private void appendDigit(String d) {
        if (resetInput || display.getText().equals("0")) {
            display.setText(d);
            resetInput = false;
        } else {
            display.setText(display.getText() + d);
        }
        updateExpressionTyping();
    }

    private void appendDot() {
        if (resetInput) {
            display.setText("0.");
            resetInput = false;
        } else if (!display.getText().contains(".")) {
            display.setText(display.getText() + ".");
        }
        updateExpressionTyping();
    }

    private void toggleSign() {
        String t = display.getText();
        if (t.equals("0")) return;
        if (t.startsWith("-")) display.setText(t.substring(1)); else display.setText("-" + t);
    }

    private void clearEntry() {
        display.setText("0");
        resetInput = true;
        if (pendingUnary == null) expressionLabel.setText(" ");
    }

    private void clearAll() {
        accumulator = null;
        pendingOp = null;
        pendingUnary = null;
        clearEntry();
        expressionLabel.setText(" ");
    }

    private void backspace() {
        if (resetInput) return;
        String t = display.getText();
        if (t.length() <= 1 || (t.length() == 2 && t.startsWith("-"))) {
            display.setText("0");
            resetInput = true;
            return;
        }
        display.setText(t.substring(0, t.length() - 1));
    }

    private double currentValue() {
        try {
            return Double.parseDouble(display.getText());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number: '" + display.getText() + "'");
        }
    }

    /**
     * Xử lý phép toán 2 ngôi: +, -, *, /, ^
     * Tất cả phép toán 2 ngôi đều được gửi đến MathServer qua RMI
     */
    private void performBinaryOp(String op) {
        if (mathService == null) {
            logger.info("MathService not connected");
            return;
        }
        try {
            // Nếu có hàm một ngôi đang chờ, thực hiện nó trước
            if (pendingUnary != null && !resetInput) {
                double val = currentValue();
                double unaryRes = applyUnaryCompute(pendingUnary, val);
                display.setText(Double.toString(unaryRes));
                pendingUnary = null;
            }
            
            double val = currentValue();
            if (accumulator == null) {
                // Lần đầu: lưu số hiện tại vào accumulator
                accumulator = val;
            } else if (pendingOp != null && !resetInput) {
                // Đã có phép toán chờ: thực hiện ngay (ví dụ: 2 + 3 + 4)
                accumulator = computeMathRemote(accumulator, val, pendingOp);
                display.setText(Double.toString(accumulator));
            }
            
            // Lưu phép toán mới và chờ số tiếp theo
            pendingOp = op;
            resetInput = true;
            logger.info("Pending op='" + op + "' accumulator=" + accumulator + " input=" + val);
            updateExpressionAfterOp(op);
        } catch (Exception ex) {
            logger.error("Error during binary op", ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void evaluateEquals() {
        if (mathService == null) {
            logger.info("MathService not connected");
            return;
        }
        try {
            double rightVal = currentValue();
            if (pendingUnary != null) {
                rightVal = applyUnaryCompute(pendingUnary, rightVal);
                pendingUnary = null;
                display.setText(Double.toString(rightVal));
            }
            if (pendingOp == null) {
                expressionLabel.setText(" ");
                resetInput = true;
                return;
            }
            accumulator = computeMathRemote(accumulator == null ? 0.0 : accumulator, rightVal, pendingOp);
            display.setText(Double.toString(accumulator));
            logger.info("= result=" + accumulator);
            pendingOp = null;
            resetInput = true;
            expressionLabel.setText(" ");
        } catch (Exception ex) {
            logger.error("Error during equals", ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUnaryPressed(String fn) {
        // sqrt uses MathService; sin/cos/tan use TrigService
        if (("sqrt".equals(fn) && mathService == null) || (!"sqrt".equals(fn) && trigService == null)) {
            logger.info("Required service not connected");
            return;
        }
        if (resetInput || display.getText().equals("0")) {
            pendingUnary = fn;
            expressionLabel.setText(unaryName(fn) + "(" + (resetInput ? "" : display.getText()) + ")");
            resetInput = true;
            return;
        }
        try {
            double val = currentValue();
            double res = applyUnaryCompute(fn, val);
            display.setText(Double.toString(res));
            logger.info("Applied " + fn + "(" + val + ") = " + res);
            resetInput = true;
            expressionLabel.setText(fn + "(" + val + ")");
        } catch (RemoteException ex) {
            logger.error("Remote error in unary op", ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Remote Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Gọi hàm một ngôi qua RMI - tự động phân tuyến đến server phù hợp
     * - sqrt → MathServer
     * - sin, cos, tan → TrigServer
     */
    private double applyUnaryCompute(String fn, double val) throws RemoteException {
        switch (fn) {
            case "sqrt": return mathService.sqrt(val, clientTag);  // Gọi MathServer
            case "sin": return trigService.sin(val, clientTag);    // Gọi TrigServer
            case "cos": return trigService.cos(val, clientTag);    // Gọi TrigServer
            case "tan": return trigService.tan(val, clientTag);     // Gọi TrigServer
            default: throw new RemoteException("Unsupported unary: " + fn);
        }
    }

    /**
     * Gọi phép toán 2 ngôi qua RMI đến MathServer
     * Tất cả phép toán 2 ngôi đều do MathServer xử lý
     */
    private double computeMathRemote(double a, double b, String op) throws RemoteException {
        switch (op) {
            case "+": return mathService.add(a, b, clientTag);  // Gửi đến MathServer
            case "-": return mathService.sub(a, b, clientTag);  // Gửi đến MathServer
            case "*": return mathService.mul(a, b, clientTag);   // Gửi đến MathServer
            case "/": return mathService.div(a, b, clientTag);   // Gửi đến MathServer
            case "^": return mathService.pow(a, b, clientTag);  // Gửi đến MathServer
            default: throw new RemoteException("Unsupported op: " + op);
        }
    }

    private String resolveLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    private void updateExpressionAfterOp(String op) {
        String left = accumulator == null ? "" : trimTrailingZero(accumulator);
        expressionLabel.setText(left + " " + op + " ");
    }

    private String trimTrailingZero(double v) {
        String s = Double.toString(v);
        if (s.endsWith(".0")) return s.substring(0, s.length()-2);
        return s;
    }

    private void updateExpressionTyping() {
        if (pendingUnary != null) {
            String cur = resetInput ? "" : display.getText();
            expressionLabel.setText(unaryName(pendingUnary) + "(" + cur + ")");
        }
    }

    private String unaryName(String fn) {
        if ("sqrt".equals(fn)) return "√";
        return fn;
    }
}


