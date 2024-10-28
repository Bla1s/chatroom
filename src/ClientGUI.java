import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ClientGUI implements KeyListener {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private Client client;

    public ClientGUI(Client client) {
        this.client = client;
        frame = new JFrame("Chat Application");
        chatArea = new JTextArea();
        messageField = new JTextField(30);
        sendButton = new JButton("Send");
        messageField.addKeyListener(this);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(messageField);
        panel.add(sendButton);

        frame.setLayout(new BorderLayout(10, 10));
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        ((JComponent) frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String messageToSend = messageField.getText();
                if (!messageToSend.isEmpty()) {
                    client.sendMessage(messageToSend);
                    messageField.setText("");
                }
            }
        });

        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String messageToSend = messageField.getText();
            if (!messageToSend.isEmpty()) {
                client.sendMessage(messageToSend);
                messageField.setText("");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}