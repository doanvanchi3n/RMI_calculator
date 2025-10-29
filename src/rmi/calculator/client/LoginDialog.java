package rmi.calculator.client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.*;

public class LoginDialog extends JDialog {
    private final JTextField hostField = new JTextField();
    private final JTextField userField = new JTextField();
    private boolean approved = false;
    private final JLabel ipHint = new JLabel();

    public LoginDialog(Frame owner) {
        super(owner, "Connect to RMI Server", true);
        initUI();
    }

    private void initUI() {
        setSize(380, 180);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Server IP:"), c);
        c.gridx = 1; c.gridy = 0; hostField.setColumns(18); form.add(hostField, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Username:"), c);
        c.gridx = 1; c.gridy = 1; userField.setColumns(18); form.add(userField, c);

        // Current machine IP hint row
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; ipHint.setText("Your IP: " + resolveLocalIp()); form.add(ipHint, c);
        c.gridwidth = 1;

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        buttons.add(ok);
        buttons.add(cancel);

        ok.addActionListener(this::onOk);
        cancel.addActionListener(e -> { approved = false; dispose(); });

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
    }

    private void onOk(ActionEvent e) {
        String host = hostField.getText() == null ? "" : hostField.getText().trim();
        String user = userField.getText() == null ? "" : userField.getText().trim();
        if (host.isEmpty() || user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Server IP and Username", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        approved = true;
        dispose();
    }

    public boolean isApproved() {
        return approved;
    }

    public String getHost() {
        return hostField.getText().trim();
    }

    public String getUsername() {
        return userField.getText().trim();
    }

    private String resolveLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}


