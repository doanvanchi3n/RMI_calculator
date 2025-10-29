package rmi.calculator.server;

import javax.swing.*;

public class CalculatorServer {
    public static void main(String[] args) {
        final int port = 5050;
        final String binding = "CalculatorService";
        SwingUtilities.invokeLater(() -> {
            ServerUI ui = new ServerUI(port, binding);
            ui.setVisible(true);
        });
    }
}


