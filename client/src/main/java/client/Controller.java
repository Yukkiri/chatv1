package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private HBox authPanel;
    @FXML
    private TextField log;
    @FXML
    private PasswordField pass;
    @FXML
    private Button sendData;
    @FXML
    private HBox messagePanel;
    @FXML
    private TextArea chat;
    @FXML
    private TextArea message;
    @FXML
    private Button send;

    private static Socket socket;
    private static final int PORT = 4592;
    private static final String IP = "localhost";
    private static DataInputStream input;
    private DataOutputStream out;
    private boolean isAuth;

    private String nick;

    @FXML
    public void sendMessage() {
        try {
            out.writeUTF(message.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        message.clear();
        message.requestFocus();
    }

    @FXML
    public void sendMessageTA(KeyEvent keyEvent){
        if(keyEvent.isShiftDown()&&keyEvent.getCode().equals(KeyCode.ENTER)){
            sendMessage();
        } else if(keyEvent.getCode().equals(KeyCode.ENTER)){
            message.appendText("");
        }
    }

    @FXML
    public void tryAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()){
            connect();
        }

        String messageToServer = String.format("/auth %s %s", log.getText().trim(), pass.getText().trim());
        try {
            out.writeUTF(messageToServer);
            pass.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Platform.runLater(() ->{message.requestFocus();});

        setIsAuth(false);

    }

    private void connect(){
        try {
            socket = new Socket(IP, PORT);
            input = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{

                try {
                    //authentication
                    while (true){
                        String in = input.readUTF();
                        if(in.startsWith("/")){
                            if(in.equalsIgnoreCase("/end")){
                                throw new RuntimeException("Disconnected by server");
                            }
                            if(in.startsWith("/authOk")){
                                nick = in.split("\\s")[1];
                                setIsAuth(true);
                                break;
                            }
                        } else {
                            chat.appendText(in + "\n");
                        }

                    }

                    //work
                    while (true){
                        String in = input.readUTF();
                        if(in.startsWith("/")) {
                            if (in.equalsIgnoreCase("/end")) {
                                throw new RuntimeException("Disconnected by server");
                            }
                        }
                        chat.appendText("Server: " + in + "\n");

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setIsAuth(false);
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

    public void setIsAuth(boolean isAuth){
        this.isAuth = isAuth;
        messagePanel.setVisible(isAuth);
        messagePanel.setManaged(isAuth);
        authPanel.setVisible(!isAuth);
        authPanel.setManaged(!isAuth);

        if(!isAuth){
            nick = "";
        }

        chat.clear();
        message.clear();
    }
}
