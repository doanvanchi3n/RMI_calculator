package rmi.calculator.server.common;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.function.Function;
import javax.swing.*;

public class ServerUI extends JFrame {
    private final JTextArea logArea = new JTextArea();
    private final JButton startButton = new JButton("Start Server");
    private final JButton stopButton = new JButton("Stop Server");

    private ServerLogger logger;
    private Remote serviceImpl;
    private Registry registry;
    private final int port;
    private final String bindingName;
    private final Function<ServerLogger, Remote> serviceFactory;

    public ServerUI(int port, String bindingName, Function<ServerLogger, Remote> serviceFactory) {
        super("RMI Server - " + bindingName);
        this.port = port;
        this.bindingName = bindingName;
        this.serviceFactory = serviceFactory;
        initUI();
        wireActions();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(startButton);
        btnPanel.add(stopButton);
        stopButton.setEnabled(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(btnPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopServer();
                dispose();
                System.exit(0);
            }
        });
    }

    private void wireActions() {
        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
    }

    private void startServer() {
        try {
            logger = new ServerLogger(logArea);
            String serverIp = getNetworkIpAddress();
            if (serverIp == null || serverIp.equals("127.0.0.1")) {
                logger.warning("Could not find network IP, using localhost. Remote clients may not be able to connect.");
                logger.warning("Please set java.rmi.server.hostname manually or ensure network interface is configured.");
                serverIp = "127.0.0.1";
            }
            System.setProperty("java.rmi.server.hostname", serverIp);
            ensureRegistry();
            serviceImpl = serviceFactory.apply(logger);
            registry.bind(bindingName, serviceImpl);
            logger.info("Server started and bound as '" + bindingName + "' on port " + port);
            logger.info("Server IP: " + serverIp + " (clients should use this IP to connect)");
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } catch (AlreadyBoundException ex) {
            if (logger == null) logger = new ServerLogger(logArea);
            logger.error("Name already bound: " + bindingName, ex);
        } catch (RemoteException ex) {
            if (logger == null) logger = new ServerLogger(logArea);
            logger.error("Failed to start server", ex);
        } catch (Exception ex) {
            if (logger == null) logger = new ServerLogger(logArea);
            logger.error("Unexpected error starting server", ex);
        }
    }
    
    /**
     * Lấy địa chỉ IP mạng thực tế (không phải localhost)
     * Điều này rất quan trọng để client từ máy khác có thể kết nối
     */
    private String getNetworkIpAddress() {
        try {
            String privateIp = null;
            String publicIp = null;
            
            // Thử lấy IP từ tất cả các network interface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                // Bỏ qua loopback và các interface không hoạt động
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Chỉ lấy IPv4, bỏ qua IPv6 và localhost
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        String ip = addr.getHostAddress();
                        // Ưu tiên địa chỉ private (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || 
                            (ip.startsWith("172.") && ip.matches("172\\.(1[6-9]|2[0-9]|3[01])\\..*"))) {
                            privateIp = ip;
                        } else if (publicIp == null) {
                            // Lưu IP công khai đầu tiên tìm được
                            publicIp = ip;
                        }
                    }
                }
            }
            
            // Ưu tiên trả về private IP, nếu không có thì dùng public IP
            if (privateIp != null) {
                return privateIp;
            }
            if (publicIp != null) {
                return publicIp;
            }
            
            // Nếu không tìm thấy, thử getLocalHost()
            try {
                InetAddress localhost = InetAddress.getLocalHost();
                String ip = localhost.getHostAddress();
                if (!ip.equals("127.0.0.1") && !ip.equals("::1")) {
                    return ip;
                }
            } catch (Exception e) {
                // Ignore
            }
            
            return null;
        } catch (SocketException e) {
            return null;
        }
    }

    private void stopServer() {
        if (registry != null) {
            try {
                if (serviceImpl != null) {
                    try {
                        registry.unbind(bindingName);
                    } catch (NotBoundException ignored) {
                    }
                    UnicastRemoteObject.unexportObject(serviceImpl, true);
                }
            } catch (RemoteException ex) {
                if (logger != null) logger.error("Error during server stop", ex);
            }
        }
        if (logger != null) logger.info("Server stopped");
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void ensureRegistry() throws RemoteException {
        try {
            registry = LocateRegistry.getRegistry(port);
            registry.list();
        } catch (RemoteException ex) {
            registry = LocateRegistry.createRegistry(port);
        }
    }
}


