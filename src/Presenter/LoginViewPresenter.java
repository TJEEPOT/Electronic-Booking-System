package Presenter;

import Model.User;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import Model.Database.*;

public class LoginViewPresenter extends Application implements Initializable {

    private DatabaseManager db = new DatabaseManager();
    private User            currentUser;

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button loginButton;
    @FXML private Label errorMessage;

    //this method takes the user provided credentials and checks them against the db, returning a User object to pass
    // to the next screen if the credentials are correct, and null if incorrect.
    public void loginButtonPressed(ActionEvent event)
    {
        currentUser = db.authenticateUser(username.getText(), password.getText());
        //we don't want customers getting access to this thing yet
        if (currentUser == null || currentUser.accountType == User.AccountType.CUSTOMER)
        {
            errorMessage.setOpacity(1.00);
            username.setText("");
            password.setText("");
            currentUser = null;
        }
        else
        {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../View/DashboardView.fxml"));
            Parent dashboardViewParent = null;
            try {
                dashboardViewParent = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Scene eventViewScene = new Scene(dashboardViewParent);

            //access the controller and call a method
            DashboardViewPresenter presenter = loader.getController();
            //pass the current user to the new window
            presenter.initData(currentUser);

            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
            window.setScene(eventViewScene);
            window.setTitle("Dashboard");
            window.show();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){}

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("../View/LoginView.fxml"));
        primaryStage.setTitle("EBS Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
