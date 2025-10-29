package rmi.calculator.client;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.*;
import rmi.calculator.common.CalculatorService;

public class ClientUI extends JFrame {
    private final JTextField display = new JTextField("0");
    private final JLabel expressionLabel = new JLabel(" ");
    private final JTextArea logArea = new JTextArea();

    private CalculatorService service;
    private final String username;
    private final String clientIp;
    private final String clientTag;
    private ClientLogger logger;

    private final String host;
    private final int port;
    private final String bindingName;

    private Double accumulator = null;
    private String pendingOp = null; // "+", "-", "*", "/", "^"
    private boolean resetInput = true;
    private String pendingUnary = null; // "sqrt", "sin", "cos", "tan"

    public ClientUI(String host, int port, String bindingName, String username) {
        super("RMI Scientific Calculator - Client");
        this.host = host;
        this.port = port;
        this.bindingName = bindingName;
        this.username = username;
        this.clientIp = resolveLocalIp();
        this.clientTag = username + " " + clientIp;
        initUI();
        connect();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 560);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.add(new JLabel("User: " + username));
        header.add(new JLabel("Client IP: " + clientIp));

        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(display.getFont().deriveFont(Font.BOLD, 24f));

        expressionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        expressionLabel.setFont(expressionLabel.getFont().deriveFont(Font.PLAIN, 14f));

        JPanel keypad = buildKeypad();

        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(360, 180));

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

    private void connect() {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            service = (CalculatorService) registry.lookup(bindingName);
            logger.info("Connected to server '" + bindingName + "' at " + host + ":" + port);
        } catch (Exception ex) {
            logger.error("Failed to connect to server", ex);
            JOptionPane.showMessageDialog(this, "Cannot connect to server: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
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

    private void performBinaryOp(String op) {
        if (service == null) {
            logger.info("Service not connected");
            return;
        }
        try {
            // If there is a pending unary applied to current input, resolve it first
            if (pendingUnary != null && !resetInput) {
                double val = currentValue();
                double unaryRes = applyUnaryCompute(pendingUnary, val);
                display.setText(Double.toString(unaryRes));
                pendingUnary = null;
            }
            double val = currentValue();
            if (accumulator == null) {
                accumulator = val;
            } else if (pendingOp != null && !resetInput) {
                accumulator = computeRemote(accumulator, val, pendingOp);
                display.setText(Double.toString(accumulator));
            }
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
        if (service == null) {
            logger.info("Service not connected");
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
                // only unary
                expressionLabel.setText(" ");
                resetInput = true;
                return;
            }
            accumulator = computeRemote(accumulator == null ? 0.0 : accumulator, rightVal, pendingOp);
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
        if (service == null) {
            logger.info("Service not connected");
            return;
        }
        // If user hasn't typed a number or just started a new entry, go into unary-building mode
        if (resetInput || display.getText().equals("0")) {
            pendingUnary = fn;
            expressionLabel.setText(unaryName(fn) + "(" + (resetInput ? "" : display.getText()) + ")");
            resetInput = true; // next digit replaces 0
            return;
        }
        // Otherwise apply immediately to current value
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

    private double applyUnaryCompute(String fn, double val) throws RemoteException {
        switch (fn) {
            case "sqrt": return service.sqrt(val, clientTag);
            case "sin": return service.sin(val, clientTag);
            case "cos": return service.cos(val, clientTag);
            case "tan": return service.tan(val, clientTag);
            default: throw new RemoteException("Unsupported unary: " + fn);
        }
    }

    private double computeRemote(double a, double b, String op) throws RemoteException {
        switch (op) {
            case "+": return service.add(a, b, clientTag);
            case "-": return service.sub(a, b, clientTag);
            case "*": return service.mul(a, b, clientTag);
            case "/": return service.div(a, b, clientTag);
            case "^": return service.pow(a, b, clientTag);
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


