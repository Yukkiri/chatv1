package client;


import commands.Commands;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public ListView<String> clientList;
    @FXML
    public Button regButton;
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

    private Stage stage;

    private Stage regStage;

    private RegController regController;

    private String chatTitle = "ICQ2021";

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
    public void sendMessageTA(KeyEvent keyEvent) {
        if (keyEvent.isShiftDown() && keyEvent.getCode().equals(KeyCode.ENTER)) {
            sendMessage();
        } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            message.appendText("");
        }
    }

    @FXML
    public void tryAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        String messageToServer = String.format("%s %s %s", Commands.AUTH, log.getText().trim(), pass.getText().trim());
        try {
            out.writeUTF(messageToServer);
            pass.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) chat.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF(Commands.END);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        setIsAuth(false);

    }

    private void connect() {
        try {
            socket = new Socket(IP, PORT);
            input = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {

                try {
                    //authentication
                    while (true) {
                        String in = input.readUTF();
                        if (in.startsWith("/")) {
                            if (in.equalsIgnoreCase(Commands.END)) {
                                throw new RuntimeException("Disconnected by server");
                            }
                            if (in.startsWith(Commands.AUTH_OK)) {
                                nick = in.split("\\s")[1];
                                setIsAuth(true);
                                chat.clear();
                                break;
                            }
                            if (in.equals(Commands.REGISTRATION_OK)) {
                                regController.regResult(true);
                            }
                            if (in.equals(Commands.REGISTRATION_FAILED)) {
                                regController.regResult(false);
                            }
                        } else {
                            chat.appendText(in + "\n");
                        }

                    }

                    //work
                    while (true) {
                        String in = input.readUTF();

                        if (in.startsWith("/")) {
                            if (in.equalsIgnoreCase(Commands.END)) {
                                throw new RuntimeException("Disconnected by server");
                            }
                            if (in.startsWith(Commands.CLIENT_LIST)) {
                                String[] tokens = in.split("\\s");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < tokens.length; i++) {
                                        clientList.getItems().add(tokens[i]);
                                    }
                                });
                            }
                        } else {
                            chat.appendText(in + "\n");
                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
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

    public void setIsAuth(boolean isAuth) {
        this.isAuth = isAuth;
        messagePanel.setVisible(isAuth);
        messagePanel.setManaged(isAuth);
        authPanel.setVisible(!isAuth);
        authPanel.setManaged(!isAuth);
        clientList.setVisible(isAuth);
        clientList.setManaged(isAuth);

        if (!isAuth) {
            nick = "";
        }

        setTitle(nick);
        chat.clear();
        message.clear();
    }

    private void setTitle(String title) {
        Platform.runLater(() -> {
            if (title.equals("")) {
                stage.setTitle(chatTitle);
            } else {
                stage.setTitle(String.format("%s [%s]", chatTitle, title));
            }
        });
    }

    @FXML
    public void clientListPressed(MouseEvent mouseEvent) {
        String text = message.getText();
        String user = clientList.getSelectionModel().getSelectedItems().toString();
        user = user.substring(1, user.length() - 1);
        String messageToUser = String.format("%s %s %s", Commands.WHISPER, user, text);
        message.clear();
        message.setText(messageToUser);
    }

    public void wantToReg(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage = new Stage();
            regStage.setTitle(chatTitle + " - регистрация");
            regStage.setScene(new Scene(root, 400, 300));

            regStage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(String login, String password, String nickname) {
        String message = String.format("%s %s %s %s", Commands.REGISTRATION, login, password, nickname);
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
