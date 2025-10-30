package rmi.calculator.server;

import javax.swing.*;
import rmi.calculator.server.common.ServerUI;
import rmi.calculator.server.math.MathServiceImpl;

public class MathServer {
    public static void main(String[] args) {
        final int port = 5050; // Math server default
        final String binding = "MathService";
        SwingUtilities.invokeLater(() -> {
            ServerUI ui = new ServerUI(port, binding, logger -> {
                try {
                    return new MathServiceImpl(logger);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            ui.setVisible(true);
        });
    }
}


