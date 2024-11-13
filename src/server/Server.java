package server;

import client.ClientHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket){
        this.serverSocket =serverSocket;
    }

    public void startServer(){
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("SERVER: Server started on IP: " + ipAddress);
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }catch (IOException e ){
            throw new RuntimeException(e);
        }
    }
    public void closeServerSocket(){
        try {
            if(serverSocket!=null){
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
