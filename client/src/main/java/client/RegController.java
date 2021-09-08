package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {
    @FXML
    private TextField login;
    @FXML
    private PasswordField password;
    @FXML
    private TextField nickname;
    @FXML
    private TextArea info;
    @FXML
    private Button tryToReg;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }

    public void tryReg(ActionEvent actionEvent) {
        String log = login.getText().trim();
        String pass = password.getText().trim();
        String nick = nickname.getText().trim();

        controller.tryToReg(log, pass, nick);
    }

    public void regResult(boolean flag) {
        info.appendText(flag ? "Регистрация прошла успешно\n" : "Логин или никнейм уже занят\n");
    }
}
