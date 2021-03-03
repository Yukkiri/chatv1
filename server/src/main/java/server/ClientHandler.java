package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;

    private DataInputStream input;
    private DataOutputStream out;

    private String nick;

    public ClientHandler(Server server, Socket socket){
        try {
            this.server = server;
            this.socket = socket;

            input = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //authentication
                    while (true){
                        String in = input.readUTF();
                        if(in.startsWith("/")){
                            if(in.equalsIgnoreCase("/end")){
                                System.out.println("Client disconnected");
                                out.writeUTF("/end");
                                throw new RuntimeException("Disconnected by server");
                            }
                            if(in.startsWith("/auth")){
                                String[] tokens = in.split("\\s", 3);
                                String newNick = server.getAuth().getNick(tokens[1], tokens[2]);
                                if(newNick != null){
                                    nick = newNick;
                                    server.subscribe(this);
                                    sendMessage("/authOk " + nick);
                                    break;
                                } else {
                                    sendMessage("Неверный логин/пароль");
                                }
                            }
                        }
                    }


                    //work
                    while (true) {
                        String in = input.readUTF();
                        if (in.equalsIgnoreCase("/end")) {
                            out.writeUTF("/end");
                            break;
                        }
                        server.broadcastMessage(this, in);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    System.out.println("Client disconnected");
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(String message){
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick(){
        return nick;
    }
}
