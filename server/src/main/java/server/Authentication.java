package server;

public interface Authentication {
    String getNick(String log, String pass);
    boolean registration(String log, String pass, String nick);
}
