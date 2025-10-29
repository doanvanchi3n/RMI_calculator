package rmi.calculator.server.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class ServerLogger {
    private final JTextArea logArea;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public ServerLogger(JTextArea logArea) {
        this.logArea = logArea;
    }

    public void info(String message) {
        append("INFO", message);
    }

    public void error(String message) {
        append("ERROR", message);
    }

    public void error(String message, Throwable t) {
        append("ERROR", message + " | " + t.getClass().getSimpleName() + ": " + t.getMessage());
    }

    private void append(String level, String message) {
        final String ts = sdf.format(new Date());
        final String line = String.format("[%s] [%s] %s\n", ts, level, message);
        if (SwingUtilities.isEventDispatchThread()) {
            logArea.append(line);
        } else {
            SwingUtilities.invokeLater(() -> logArea.append(line));
        }
    }
}


