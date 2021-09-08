package server;

import commands.Commands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;

    private DataInputStream input;
    private DataOutputStream out;

    private String nick;
    private String log;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;

            input = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);
                    //authentication
                    while (true) {
                        String in = input.readUTF();
                        if (in.startsWith(Commands.AUTH)) {
                            String[] tokens = in.split("\\s", 3);
                            String newNick = server.getAuth().getNick(tokens[1], tokens[2]);
                            log = tokens[1];
                            if (newNick != null) {
                                if (!server.isLogAuth(log)) {
                                    nick = newNick;
                                    sendMessage(String.format("%s %s", Commands.AUTH_OK, nick));
                                    server.subscribe(this);
                                    break;
                                } else {
                                    sendMessage("Учетная запись занята!");
                                }
                            } else {
                                sendMessage("Неверный логин/пароль");
                            }
                        } else if (in.startsWith("/")) {
                            serviceMsg(in);
                        }
                    }

                    socket.setSoTimeout(0);

                    //work
                    while (true) {
                        String in = input.readUTF();

                        //service commands
                        if (in.startsWith("/")) {
                            serviceMsg(in);
                        } else {
                            server.broadcastMessage(this, in);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    try {
                        out.writeUTF(Commands.END);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
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

    private void serviceMsg(String msg) throws IOException {
        String[] tokens = msg.split("\\s");
        String command = tokens[0].trim();
        switch (command) {
            case Commands.END:
                System.out.println("Client disconnected");
                out.writeUTF(Commands.END);
                break;
            case Commands.WHISPER:
                whisperMsg(msg);
                break;
            case Commands.REGISTRATION:
                regTry(msg);
                break;
            default:
                break;
        }
    }

    private void whisperMsg(String in) {
        String[] tokens = in.split("\\s", 3);
        if (tokens.length >= 3) {
            String receiver = tokens[1].trim();
            String message = tokens[2].trim();
            server.sendPrivate(this, receiver, message);
        }
    }


    private void regTry(String in) {
        String[] tokens = in.split("\\s");
        boolean isRegSuccessful = server.getAuth().registration(tokens[1], tokens[2], tokens[3]);
        if (isRegSuccessful) {
            sendMessage(Commands.REGISTRATION_OK);
        } else {
            sendMessage(Commands.REGISTRATION_FAILED);
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public String getLog() {
        return log;
    }
}
