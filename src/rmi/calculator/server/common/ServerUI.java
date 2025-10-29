package rmi.calculator.server.common;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
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
            String serverIp = java.net.InetAddress.getLocalHost().getHostAddress();
            System.setProperty("java.rmi.server.hostname", serverIp);
            ensureRegistry();
            serviceImpl = serviceFactory.apply(logger);
            registry.bind(bindingName, serviceImpl);
            logger.info("Server started and bound as '" + bindingName + "' on port " + port + " (hostname=" + System.getProperty("java.rmi.server.hostname") + ")");
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } catch (AlreadyBoundException ex) {
            if (logger == null) logger = new ServerLogger(logArea);
            logger.error("Name already bound: " + bindingName, ex);
        } catch (RemoteException ex) {
            if (logger == null) logger = new ServerLogger(logArea);
            logger.error("Failed to start server", ex);
        } catch (java.net.UnknownHostException ex) {
            if (logger == null) logger = new ServerLogger(logArea);
            logger.error("Cannot resolve local host for RMI hostname", ex);
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


