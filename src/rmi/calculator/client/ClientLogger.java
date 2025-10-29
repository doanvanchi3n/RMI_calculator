package rmi.calculator.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class ClientLogger {
    private final JTextArea logArea;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final String clientTag; // username + IP

    public ClientLogger(JTextArea logArea, String clientTag) {
        this.logArea = logArea;
        this.clientTag = clientTag;
    }

    public void info(String message) {
        append("INFO", message);
    }

    public void error(String message, Throwable t) {
        append("ERROR", message + " | " + t.getClass().getSimpleName() + ": " + t.getMessage());
    }

    private void append(String level, String message) {
        final String ts = sdf.format(new Date());
        final String line = String.format("[%s] [%s] [%s] %s\n", ts, level, clientTag, message);
        if (SwingUtilities.isEventDispatchThread()) {
            logArea.append(line);
        } else {
            SwingUtilities.invokeLater(() -> logArea.append(line));
        }
    }
}


