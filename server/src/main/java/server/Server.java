package server;

import commands.Commands;

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
                new ClientHandler(this, socket);
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
        message = String.format("[%s] : %s ", author.getNick(), message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastSendingClientList();
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastSendingClientList();
    }

    public Authentication getAuth(){
        return auth;
    }

    public void sendPrivate(ClientHandler author, String receiver, String message){
        message = String.format("[%s] -> [%s] : %s", author.getNick(), receiver, message);
        for (ClientHandler client : clients) {
            if(client.getNick().equals(receiver)) {
                client.sendMessage(message);
                if(!client.equals(author)){
                    author.sendMessage(message);
                }
                return;
            }
        }
        author.sendMessage("[Server]: Пользователя " + receiver + " не существует!");
    }

    public boolean isLogAuth(String log){
        for (ClientHandler client : clients) {
            if(client.getLog().equals(log)){
                return true;
            }
        }
        return false;
    }

    public void broadcastSendingClientList(){
        StringBuilder clientList = new StringBuilder(Commands.CLIENT_LIST);
        for (ClientHandler client : clients) {
            clientList.append(" ").append(client.getNick());
        }

        String message = clientList.toString().trim();

        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
