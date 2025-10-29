package rmi.calculator.server;

import javax.swing.*;
import rmi.calculator.server.common.ServerUI;
import rmi.calculator.server.trig.TrigServiceImpl;

public class TrigServer {
    public static void main(String[] args) {
        final int port = 5051; // Trig server default
        final String binding = "TrigService";
        SwingUtilities.invokeLater(() -> {
            ServerUI ui = new ServerUI(port, binding, logger -> {
                try {
                    return new TrigServiceImpl(logger);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            ui.setVisible(true);
        });
    }
}


