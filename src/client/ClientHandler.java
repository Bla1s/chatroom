package client;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private static final Color[] COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN,
            Color.PINK, Color.YELLOW, Color.GRAY, Color.DARK_GRAY, Color.LIGHT_GRAY,
    };
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private BufferedWriter logWriter;
    private String clientUsername;
    private Color userColor;

    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            File logDir = new File("log");
            if (!logDir.exists()) {
                logDir.mkdir();
            }

            String currentMonth = new SimpleDateFormat("MMyyyy").format(new Date());
            File monthDir = new File(logDir, currentMonth);
            if (!monthDir.exists()) {
                monthDir.mkdir();
            }

            String currentDate = new SimpleDateFormat("ddMMyyyy").format(new Date());
            File logFile = new File(monthDir, "server_log_" + currentDate + ".txt");
            this.logWriter = new BufferedWriter(new FileWriter(logFile, true));
            this.clientUsername = bufferedReader.readLine();
            this.userColor = getRandomColor();
            clientHandlers.add(this);
            broadcastMessage(clientUsername + " has joined the chat!", true);
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            System.out.println("<" + timestamp + "> " + clientUsername + " has joined the chat!");
            logMessage("<" + timestamp + "> " + clientUsername + " has joined the chat!");


        } catch (Exception e) {
            closeEverything(socket,bufferedReader,bufferedWriter,logWriter);
            e.printStackTrace();
        }
    }
    public Color getUserColor() {
        return userColor;
    }

    private Color getRandomColor() {
        Random random = new Random();
        return COLORS[random.nextInt(COLORS.length)];
    }

    private String getColorHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    public void run() {
        String messageFromClient;

        while(socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                System.out.println("<" + timestamp + "> " + messageFromClient);
                logMessage("<" + timestamp + "> " + messageFromClient);

                int colonIndex = messageFromClient.indexOf(": ");
                if (colonIndex != -1) {
                    String actualMessage = messageFromClient.substring(colonIndex + 2);
                    if (actualMessage.startsWith("/pm")) {
                        handlePrivateMessage(actualMessage);
                    } else {
                        broadcastMessage(messageFromClient);
                    }
                } else {
                    broadcastMessage(messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket,bufferedReader,bufferedWriter,logWriter);
                break;
            }
        }
    }
    private void logMessage(String message) {
        try {
            logWriter.write(message);
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handlePrivateMessage(String message) {
        String[] messageParts = message.split(" ", 3);
        if (messageParts.length < 3) {
            return;
        }

        String targetUsername = messageParts[1];
        String privateMessage = messageParts[2];
        boolean userFound = false;

        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUsername.equals(targetUsername)) {
                try {
                    String formattedMessage = "CLIENTPM::" + getColorHex(userColor) + "::PM from "+clientUsername+": " + privateMessage;
                    clientHandler.bufferedWriter.write(formattedMessage);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                    userFound = true;
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter, logWriter);
                    e.printStackTrace();
                }
                break;
            }
        }

        if (userFound) {
            try {
                String formattedMessage = "CLIENTPM::" + getColorHex(userColor) + "::PM to"+targetUsername+": " + privateMessage;
                bufferedWriter.write(formattedMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter, logWriter);
                e.printStackTrace();
            }
        } else {
            try {
                String formattedMessage = "SERVER::" + getColorHex(Color.BLACK) + "::Can't send a PM to " + targetUsername + " because they are not connected.";
                bufferedWriter.write(formattedMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter, logWriter);
                e.printStackTrace();
            }
        }
    }

    private void broadcastMessage(String messageToSend, boolean isServerMessage){
        for(ClientHandler clientHandler : clientHandlers){
            try {
                String formattedMessage;
                if (isServerMessage) {
                    formattedMessage = "SERVER::" + getColorHex(Color.BLACK) + "::" + messageToSend;
                } else {
                    formattedMessage = "CLIENT::" + getColorHex(userColor) + "::" + clientUsername + "::" + messageToSend;
                }
                clientHandler.bufferedWriter.write(formattedMessage);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter, logWriter);
                e.printStackTrace();
            }
        }
    }
    private void broadcastMessage(String messageToSend){
        broadcastMessage(messageToSend, false);
    }

    private void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage(clientUsername + " has disconnected!", true);
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("<" + timestamp + "> " + clientUsername + " has disconnected!");
        logMessage("<" + timestamp + "> " + clientUsername + " has disconnected!");
    }
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter, BufferedWriter logWriter){
        removeClientHandler();
        try {
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(logWriter!=null){
                logWriter.close();
            }
            if(socket!=null){
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
