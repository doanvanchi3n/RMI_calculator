package rmi.calculator.client;

import javax.swing.*;

public class CalculatorClient {
    public static void main(String[] args) {
        final int port = 5050;
        final String binding = "CalculatorService";
        SwingUtilities.invokeLater(() -> {
            LoginDialog dialog = new LoginDialog(null);
            dialog.setVisible(true);
            if (!dialog.isApproved()) {
                System.exit(0);
                return;
            }
            String host = dialog.getHost();
            String username = dialog.getUsername();
            ClientUI ui = new ClientUI(host, port, binding, username);
            ui.setVisible(true);
        });
    }
}


