package rmi.calculator.server;

import javax.swing.*;
import rmi.calculator.server.common.ServerUI;

public class CalculatorServer {
    public static void main(String[] args) {
        final int port = 5052; 
        final String binding = "CalculatorService";
        SwingUtilities.invokeLater(() -> {
            ServerUI ui = new ServerUI(port, binding, logger -> {
                try {
                    return new CalculatorServiceImpl(logger);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            ui.setVisible(true);
        });
    }
}


