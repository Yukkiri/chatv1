package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private final int PORT = 4592;
    private ServerSocket serverSocket;
    private Socket socket;
    private List<ClientHandler> clients;

    private Authentication auth;


    public Server() {
        clients = new CopyOnWriteArrayList<>();
        auth = new SimpleAuth();
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server OK");

            while (true){
                socket = serverSocket.accept();
                System.out.println("Client connected " + socket.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMessage(ClientHandler author, String message){
        message = String.format("[ %s ] : %s ", author.getNick(), message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public Authentication getAuth(){
        return auth;
    }
}
