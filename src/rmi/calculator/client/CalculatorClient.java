package rmi.calculator.client;

import javax.swing.*;

public class CalculatorClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            rmi.calculator.client.LoginDialog dialog = new rmi.calculator.client.LoginDialog(null);
            dialog.setVisible(true);
            if (!dialog.isApproved()) {
                System.exit(0);
                return;
            }
            String host = dialog.getHost();
            String username = dialog.getUsername();
            // Use old login: same host for both services, default ports 5050 (Math) and 5051 (Trig)
            MultiServerClientUI ui = new MultiServerClientUI(host, 5050, host, 5051, username);
            ui.setVisible(true);
        });
    }
}


