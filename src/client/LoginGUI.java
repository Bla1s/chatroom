package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class LoginGUI {
    private JFrame frame;
    private JTextField usernameField;
    private JTextField ipAddressField;
    private JButton loginButton;
    private Client client;

    public LoginGUI() {
        frame = new JFrame("Login");
        usernameField = new JTextField(15);
        ipAddressField = new JTextField(15);
        loginButton = new JButton("Login");

        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        frame.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        frame.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(new JLabel("IP Address:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        frame.add(ipAddressField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        frame.add(loginButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String ipAddress = ipAddressField.getText();
                if (!username.isEmpty()&& !ipAddress.isEmpty()) {
                    frame.dispose();
                    try {
                        Socket socket = new Socket(ipAddress, 1234);
                        client = new Client(socket, username);
                        client.listenForMessage();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}