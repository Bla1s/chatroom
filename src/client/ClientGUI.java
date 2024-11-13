package client;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

import editorkit.WrapEditorKit;
import emotemanager.EmoteManager;

public class ClientGUI implements KeyListener {
    private JFrame frame;
    private JTextPane chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private Client client;
    private StyledDocument doc;
    private EmoteManager emoteManager;

    public ClientGUI(Client client) {
        this.client = client;
        frame = new JFrame("Chat Application");
        chatArea = new JTextPane();
        chatArea.setEditorKit(new WrapEditorKit());
        chatArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 12));
        messageField = new JTextField(30);
        sendButton = new JButton("Send");
        messageField.addKeyListener(this);

        chatArea.setEditable(false);
        doc = chatArea.getStyledDocument();

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

        emoteManager = new EmoteManager();
    }

    public void appendMessage(String message) {
        try {
            if (message.startsWith("SERVER::")) {
                String[] parts = message.split("::", 3);
                if (parts.length == 3) {
                    String serverMessage = parts[2];

                    SimpleAttributeSet serverStyle = new SimpleAttributeSet();
                    StyleConstants.setForeground(serverStyle, Color.BLACK);
                    StyleConstants.setBold(serverStyle, true);
                    doc.insertString(doc.getLength(), serverMessage + "\n", serverStyle);
                }
            } else if (message.startsWith("CLIENT::")) {
                String[] parts = message.split("::", 4);
                if (parts.length == 4) {
                    String colorHex = parts[1];
                    String username = parts[2];
                    String userMessage = parts[3];

                    SimpleAttributeSet usernameStyle = new SimpleAttributeSet();
                    Color userColor = Color.decode(colorHex);
                    StyleConstants.setForeground(usernameStyle, userColor);
                    StyleConstants.setBold(usernameStyle, true);

                    SimpleAttributeSet messageStyle = new SimpleAttributeSet();
                    StyleConstants.setForeground(messageStyle, Color.BLACK);

                    doc.insertString(doc.getLength(), username + ": ", usernameStyle);
                    insertMessageWithEmotes(userMessage, messageStyle);

                    SimpleAttributeSet paragraphStyle = new SimpleAttributeSet();
                    StyleConstants.setSpaceAbove(paragraphStyle, 0);
                    StyleConstants.setSpaceBelow(paragraphStyle, 0);
                    StyleConstants.setLineSpacing(paragraphStyle, 0);
                    StyleConstants.setAlignment(paragraphStyle, StyleConstants.ALIGN_LEFT);

                    doc.setParagraphAttributes(doc.getLength(),
                            username.length() + 2 + userMessage.length() + 1,
                            paragraphStyle,
                            false);
                }
            } else if (message.startsWith("CLIENTPM::")) {
                String[] parts = message.split("::", 3);
                if (parts.length == 3) {
                    String colorHex = parts[1];
                    String userMessage = parts[2];

                    SimpleAttributeSet pmStyle = new SimpleAttributeSet();
                    Color userColor = Color.decode(colorHex);
                    StyleConstants.setForeground(pmStyle, userColor);
                    StyleConstants.setBold(pmStyle, true);

                    insertMessageWithEmotes(userMessage, pmStyle);
                }
            }

            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void insertMessageWithEmotes(String message, SimpleAttributeSet style) {
        Map<String, Icon> emoteMap = emoteManager.getEmoteMap();
        int lastIndex = 0;

        try {
            for (Map.Entry<String, Icon> entry : emoteMap.entrySet()) {
                String emoteCode = entry.getKey();
                Icon emoteIcon = entry.getValue();

                int index;
                while ((index = message.indexOf(emoteCode, lastIndex)) != -1) {
                    if (index > lastIndex) {
                        doc.insertString(doc.getLength(), message.substring(lastIndex, index), style);
                    }

                    SimpleAttributeSet iconAttr = new SimpleAttributeSet();
                    StyleConstants.setIcon(iconAttr, emoteIcon);
                    doc.insertString(doc.getLength(), " ", iconAttr);

                    lastIndex = index + emoteCode.length();
                }
            }

            if (lastIndex < message.length()) {
                doc.insertString(doc.getLength(), message.substring(lastIndex), style);
            }

            SimpleAttributeSet leftAlign = new SimpleAttributeSet();
            StyleConstants.setAlignment(leftAlign, StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(doc.getLength(), message.length(), leftAlign, false);

            doc.insertString(doc.getLength(), "\n", style);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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