package rmi.calculator.client;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class MultiServerLoginDialog extends JDialog {
    private final JTextField userField = new JTextField();
    private final JTextField mathHostField = new JTextField();
    private final JTextField mathPortField = new JTextField("5050");
    private final JTextField trigHostField = new JTextField();
    private final JTextField trigPortField = new JTextField("5051");
    private boolean approved = false;

    public MultiServerLoginDialog(Frame owner) {
        super(owner, "Connect to RMI Servers", true);
        initUI();
    }

    private void initUI() {
        setSize(460, 260);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Username:"), c);
        c.gridx = 1; c.gridy = 0; userField.setColumns(22); form.add(userField, c);

        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Math Host:"), c);
        c.gridx = 1; c.gridy = 1; mathHostField.setColumns(22); form.add(mathHostField, c);
        c.gridx = 2; c.gridy = 1; form.add(new JLabel("Port:"), c);
        c.gridx = 3; c.gridy = 1; mathPortField.setColumns(6); form.add(mathPortField, c);

        c.gridx = 0; c.gridy = 2; form.add(new JLabel("Trig Host:"), c);
        c.gridx = 1; c.gridy = 2; trigHostField.setColumns(22); form.add(trigHostField, c);
        c.gridx = 2; c.gridy = 2; form.add(new JLabel("Port:"), c);
        c.gridx = 3; c.gridy = 2; trigPortField.setColumns(6); form.add(trigPortField, c);

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
        if (userField.getText().trim().isEmpty() || mathHostField.getText().trim().isEmpty() || trigHostField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Username, Math Host and Trig Host", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Integer.parseInt(mathPortField.getText().trim());
            Integer.parseInt(trigPortField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ports must be numbers", "Invalid Port", JOptionPane.ERROR_MESSAGE);
            return;
        }
        approved = true;
        dispose();
    }

    public boolean isApproved() { return approved; }
    public String getUsername() { return userField.getText().trim(); }
    public String getMathHost() { return mathHostField.getText().trim(); }
    public int getMathPort() { return Integer.parseInt(mathPortField.getText().trim()); }
    public String getTrigHost() { return trigHostField.getText().trim(); }
    public int getTrigPort() { return Integer.parseInt(trigPortField.getText().trim()); }
}


